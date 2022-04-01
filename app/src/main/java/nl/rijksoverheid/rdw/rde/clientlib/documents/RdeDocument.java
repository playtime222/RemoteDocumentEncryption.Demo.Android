package nl.rijksoverheid.rdw.rde.clientlib.documents;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import androidx.annotation.NonNull;

import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;
import net.sf.scuba.smartcards.CommandAPDU;
import net.sf.scuba.smartcards.ISO7816;
import net.sf.scuba.util.Hex;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jmrtd.BACKey;
import org.jmrtd.PassportService;
import org.jmrtd.lds.ChipAuthenticationInfo;
import org.jmrtd.lds.SODFile;
import org.jmrtd.lds.icao.DG14File;
import org.jmrtd.protocol.AESSecureMessagingWrapper;
import org.jmrtd.protocol.DESedeSecureMessagingWrapper;
import org.jmrtd.protocol.EACCAAPDUSender;
import org.jmrtd.protocol.EACCAProtocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.spec.SecretKeySpec;

import nl.rijksoverheid.rdw.rde.clientlib.messaging.RdeSessionArgs;
import nl.rijksoverheid.rdw.rde.clientlib.mrtdfiles.*;

//Interactions with the NFC
public class RdeDocument implements AutoCloseable
{
    private CardService cardService;
    private PassportService passportService;
    private IsoDep isoDep;

    private final RdeDocumentContentAuthentication authenticator = new RdeDocumentContentAuthentication();
    private SODFile sodFile;
    private Dg14Reader dg14Reader;
    private byte[] dg14Content;

    private int toFileIdentifier(int shortFileIdentifer)
    {
        if (1 > shortFileIdentifer || shortFileIdentifer > 14)
            throw new IllegalArgumentException();

        return shortFileIdentifer + 0x100;
    }

    public void open(Tag tag, BACKey bacKey)
            throws CardServiceException
    {
        if (tag == null)
            throw new IllegalArgumentException();
        if (bacKey == null)
            throw new IllegalArgumentException();

        if (isOpen())
            throw new IllegalStateException();

        isoDep = IsoDep.get(tag);
        isoDep.setTimeout(100000); //Long cos debugging
        cardService = CardService.getInstance(isoDep);
        passportService = new PassportService(cardService, RdeDocumentConfig.TRANCEIVE_LENGTH_FOR_SECURE_MESSAGING, RdeDocumentConfig.MAX_BLOCK_SIZE, true, true);
        passportService.open();
        passportService.sendSelectApplet(false);
        passportService.doBAC(bacKey);
    }

    private boolean isOpen()
    {
        return isoDep != null && passportService != null && cardService != null;
    }

    public void close() throws IOException
    {
        if (passportService != null)
            passportService.close();
        if (cardService != null)
            cardService.close();
        if (isoDep != null)
            isoDep.close();
    }

    //TODO args -> which DGs et al?
    //TODO allow user to select a different dg as Fid/cont
    public RdeDocumentEnrollmentInfo getEnrollmentArgs(final UserSelectedEnrollmentArgs args)
            throws GeneralSecurityException, IOException, CardServiceException
    {
        if (args == null)
            throw new IllegalArgumentException();

        if (!isOpen())
            throw new IllegalStateException();

        getDg14();

        var fileContent = getFileContent(args.getShortFileId());
        dumpArgsAndContent(args, fileContent);

        var caSessionArgs = getDg14().getCaSessionInfo();
        caSessionArgs.dump();

        //All parameters from DG14
        var caSessionInfo = passportService.doEACCA(
                caSessionArgs.getCaPublicKeyInfo().getKeyId(),
                caSessionArgs.getCaInfo().getObjectIdentifier(),
                caSessionArgs.getCaPublicKeyInfo().getObjectIdentifier(),
                caSessionArgs.getCaPublicKeyInfo().getSubjectPublicKey());

        //dump((AESSecureMessagingWrapper) caSessionInfo.getWrapper());

        var encryptedCommand = createEncryptedRbCommand(args.getShortFileId(), args.getFileByteCount());

        var result = new RdeDocumentEnrollmentInfo();
        //result.setEnrollmentId(...);
        result.setDisplayName(args.getDisplayName());
        result.setShortFileId(args.getShortFileId());
        result.setDataGroup14(dg14Content);
        result.setFileContents(fileContent);
        result.setFileReadLength(args.getFileByteCount());
        result.setEncryptedCommand(encryptedCommand);
        result.setPcdPrivateKey(caSessionInfo.getPCDPrivateKey().getEncoded());
        result.setPcdPublicKey(caSessionInfo.getPCDPublicKey().getEncoded());

        //TODO demo only
        result.setRbResponse(getApduResponseForDecryption(encryptedCommand));

        return result;
    }

    private void dumpArgsAndContent(final UserSelectedEnrollmentArgs args, final byte[] fileContent)
    {
        System.out.println("FILE ID: " + args.getShortFileId());
        System.out.println("FILE N: " + args.getFileByteCount());
        System.out.println("FILE CONTENT: " + fileContent.length + " bytes, " + Hex.toHexString(fileContent));
    }

//    @Deprecated
//    private void testRbCall(final byte[] encryptedCommand, final RdeDocumentEnrollmentInfo result) throws CardServiceException
//    {
//        var rb = getApduResponseForEnrollment(encryptedCommand);
//        System.out.println("TEST RB RESPONSE: " + Extras.toHexString(rb));
//        result.setRbResponse(rb);
//    }

    private byte[] getFileContent(final int shortFileId) throws IOException, CardServiceException, GeneralSecurityException
    {
        byte[] fileContent;
        if (shortFileId == PassportService.SFI_DG14)
            fileContent = dg14Content;
        else
        {
            fileContent = readFileContent(shortFileId);
            authenticator.throwIfNotAuthentic(getEfSod(), shortFileId, fileContent);
        }
        return fileContent;
    }

    private Dg14Reader getDg14() throws IOException, CardServiceException, GeneralSecurityException
    {
        if (dg14Reader == null)
        {
            dg14Content = readFileContent(PassportService.SFI_DG14);
            authenticator.throwIfNotAuthentic(getEfSod(), PassportService.SFI_DG14, dg14Content);
            try (var is = new ByteArrayInputStream(dg14Content))
            {
                dg14Reader = new Dg14Reader(new DG14File(is));
            }
        }
        return dg14Reader;
    }

    private SODFile getEfSod() throws CardServiceException, IOException, GeneralSecurityException
    {
        if (sodFile == null)
        {
            try (var is = passportService.getInputStream(PassportService.EF_SOD, RdeDocumentConfig.MAX_BLOCK_SIZE))
            {
                sodFile = new SODFile(is);
            }
            authenticator.throwIfNotAuthentic(sodFile);
        }
        return sodFile;
    }
    private byte[] readFileContent(int shortFileId) throws IOException, CardServiceException
    {
        var fileId = toFileIdentifier(shortFileId);
        try (var stream = passportService.getInputStream((short) fileId, RdeDocumentConfig.MAX_BLOCK_SIZE))
        {
            return readAllBytes(stream);
            //TODO PA
        }
    }

    private static byte[] readAllBytes(final InputStream inputStream) throws IOException
    {
        final var buffer = new byte[4096];
        try(final var result = new ByteArrayOutputStream(4096))
        {
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1)
            {
                result.write(buffer, 0, bytesRead);
            }
            result.flush();
            return result.toByteArray();
        }
    }

    public byte[] getApduResponseForDecryption(RdeSessionArgs mca)
            throws GeneralSecurityException, CardServiceException, IOException
    {
        var keyAgreementAlgorithm = ChipAuthenticationInfo.toKeyAgreementAlgorithm(mca.getCaProtocolOid());
        var publicKey = getPublicKey(keyAgreementAlgorithm, mca.getPcdPublicKey());

        System.out.println("DECRYPT SESSION PUB KEY: >>>");
        System.out.println("" + publicKey);
        System.out.println("<<< DECRYPT SESSION PUB KEY");

        //System.out.println(dg14Adaptor.getCaSessionInfo().getCaInfo().getObjectIdentifier());
        EACCAProtocol.sendPublicKey(new EACCAAPDUSender(cardService), passportService.getWrapper(), getDg14().getCaSessionInfo().getCaInfo().getObjectIdentifier(), null, publicKey);
        System.out.println("DECRYPT SESSION WRAPPER: >>>");
        dump((DESedeSecureMessagingWrapper)passportService.getWrapper());
        System.out.println("<<< DECRYPT SESSION WRAPPER");

        return getApduResponseForDecryption(mca.getCaEncryptedCommand());
    }

    private PublicKey getPublicKey(final String caEphemeralPublicKeyAlgorithm, final byte[] pcdPublicKey) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        var publicKeySpec = new X509EncodedKeySpec(pcdPublicKey);
        var keyFactory = KeyFactory.getInstance(caEphemeralPublicKeyAlgorithm, new BouncyCastleProvider());
        return keyFactory.generatePublic(publicKeySpec);
    }

    private static void dump(final AESSecureMessagingWrapper wrapper)
    {
        final var encryptionKey = (SecretKeySpec) wrapper.getEncryptionKey();
        final var macKey = wrapper.getMACKey().getEncoded();
        final var counter = wrapper.getEncodedSendSequenceCounter(); //IV
        final var padding = wrapper.getPadLength();
        final var ksEnc = encryptionKey.getEncoded();
        System.out.println("Wrapper Type : AESSecureMessagingWrapper");
        System.out.println("KsEnc   : " + ksEnc.length + ", " + org.bouncycastle.util.encoders.Hex.toHexString(ksEnc));
        System.out.println("KsMac   : " + macKey.length + ", " + org.bouncycastle.util.encoders.Hex.toHexString(macKey));
        System.out.println("Counter : " + org.bouncycastle.util.encoders.Hex.toHexString(counter));
        System.out.println("Padding : " + padding);
    }

    private static void dump(final DESedeSecureMessagingWrapper wrapper)
    {
        final var encryptionKey = (SecretKeySpec) wrapper.getEncryptionKey();
        final var macKey = wrapper.getMACKey().getEncoded();
        final var counter = wrapper.getEncodedSendSequenceCounter(); //IV
        final var padding = wrapper.getPadLength();
        final var ksEnc = encryptionKey.getEncoded();
        System.out.println("Wrapper Type : DESedeSecureMessagingWrapper");
        System.out.println("KsEnc   : " + ksEnc.length + ", " + org.bouncycastle.util.encoders.Hex.toHexString(ksEnc));
        System.out.println("KsMac   : " + macKey.length + ", " + org.bouncycastle.util.encoders.Hex.toHexString(macKey));
        System.out.println("Counter : " + org.bouncycastle.util.encoders.Hex.toHexString(counter));
        System.out.println("Padding : " + padding);
    }

    //Must start new session first
    @Deprecated
    public void doTestRbCall() throws CardServiceException, IOException, GeneralSecurityException
    {
        if (!isOpen())
            throw new IllegalStateException();

//        var rbCommand = createRbCommandAPDU(14, 250);
//        var response = cardService.transmit(rbCommand);
//        System.out.println("RESPONSE (not a CA session):   " + response.getBytes().length + " bytes, " + Extras.toHexString(response.getBytes()));

        //Now a wrapped one
        var dg14 = getDg14();

        passportService.doEACCA(
                dg14.getCaSessionInfo().getCaPublicKeyInfo().getKeyId(),
                dg14.getCaSessionInfo().getCaInfo().getObjectIdentifier(),
                dg14.getCaSessionInfo().getCaPublicKeyInfo().getObjectIdentifier(),
                dg14.getCaSessionInfo().getCaPublicKeyInfo().getSubjectPublicKey()
        );
        dump((AESSecureMessagingWrapper)passportService.getWrapper()); //TODO still hard-coded to AES

        var wrappedCommand = passportService.getWrapper().wrap(createRbCommandAPDU(14, 16));
        var response = cardService.transmit(wrappedCommand);
        System.out.println("RESPONSE (wrapped):   " + response.getBytes().length + " bytes, " + Hex.toHexString(response.getBytes()));

        var unwrappedResponse = passportService.getWrapper().unwrap(response);
        System.out.println("RESPONSE (unwrapped): " + unwrappedResponse.getBytes().length + " bytes, " + Hex.toHexString(unwrappedResponse.getBytes()));
    }


    private byte[] createEncryptedRbCommand(final int shortFileId, final int fidByteCount)
    {
        var command = createRbCommandAPDU(shortFileId, fidByteCount);
        return passportService.getWrapper().wrap(command).getBytes();
    }

    @NonNull
    private CommandAPDU createRbCommandAPDU(final int shortFileId, final int fidByteCount)
    {
        int sfi = 0x80 | (shortFileId & 0xFF);
        return new CommandAPDU(ISO7816.CLA_ISO7816, ISO7816.INS_READ_BINARY, (byte) sfi, 0, fidByteCount);
    }

    private byte[] getApduResponseForDecryption(byte[] encryptedCommand)
            throws CardServiceException
    {
        var command = new CommandAPDU(encryptedCommand);
        var response = cardService.transmit(command);
        System.out.println("DECRYPT CHALLENGE (wrapped, base64): " + encryptedCommand.length + " bytes " + Hex.toHexString(encryptedCommand));
        System.out.println("DECRYPT RESPONSE (wrapped, base64):   " + response.getBytes().length + " bytes, " + Hex.toHexString(response.getBytes()));
        return response.getBytes();
    }

//    //TODO surely this should be used to create the encrypted command?
//    private byte[] getApduResponseForDecryption(final int shortFileId, final int fidByteCount)
//            throws CardServiceException
//    {
//        var wrapped = createEncryptedRbCommand(fidByteCount, shortFileId);
//        return getApduResponseForDecryption(wrapped);
//    }
}
