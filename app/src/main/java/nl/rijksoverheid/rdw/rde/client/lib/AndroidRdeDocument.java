package nl.rijksoverheid.rdw.rde.client.lib;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;
import net.sf.scuba.smartcards.CommandAPDU;
import net.sf.scuba.smartcards.ISO7816;
import net.sf.scuba.util.Hex;

import nl.rijksoverheid.rdw.rde.casessionutilities.RdeMessageParameters;
import nl.rijksoverheid.rdw.rde.client.SecureWrapperDebug;
import nl.rijksoverheid.rdw.rde.documents.RdeDocumentConfig;
import nl.rijksoverheid.rdw.rde.documents.RdeDocumentEnrollmentInfo;
import nl.rijksoverheid.rdw.rde.documents.UserSelectedEnrollmentArgs;
import nl.rijksoverheid.rdw.rde.mrtdfiles.Dg14Reader;
import nl.rijksoverheid.rdw.rde.mrtdfiles.RdeDocumentContentAuthentication;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jmrtd.BACKey;
import org.jmrtd.PACEKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.CardAccessFile;
import org.jmrtd.lds.ChipAuthenticationInfo;
import org.jmrtd.lds.PACEInfo;
import org.jmrtd.lds.SODFile;
import org.jmrtd.protocol.EACCAAPDUSender;
import org.jmrtd.protocol.EACCAProtocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Optional;

import nl.rijksoverheid.rdw.rde.messaging.*;

//Interactions with the NFC
public class AndroidRdeDocument implements AutoCloseable //, RdeDocument
{
    private CardService cardService;
    private PassportService passportService;
    private IsoDep isoDep;

    private final RdeDocumentContentAuthentication authenticator = new RdeDocumentContentAuthentication();
    private SODFile sodFile;

    private int toFileIdentifier(int shortFileIdentifier) {
        if (1 > shortFileIdentifier || shortFileIdentifier > 14)
            throw new IllegalArgumentException();

        return shortFileIdentifier + 0x100;
    }

    public void open(Tag tag, BACKey bacKey)
            throws CardServiceException, GeneralSecurityException, IOException {
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

        if (doPace(bacKey))
            return;

        //else try BAC
        try {
            passportService.doBAC(bacKey);
        }
        catch (CardServiceException ex)
        {
            System.out.println("BAC failed.");
            System.out.println(ex);
            throw ex;
        }
    }

    private boolean doPace(BACKey bacKey) throws IOException, GeneralSecurityException
    {
        try {
            var paceInfo = findPaceSecurityInfo();
            if (!paceInfo.isPresent())
                return false;

            //TODO use Card Authentication Number
            final var p = paceInfo.get();
            var paceKey = PACEKeySpec.createMRZKey(bacKey);
            passportService.doPACE(paceKey, p.getObjectIdentifier(), PACEInfo.toParameterSpec(p.getParameterId()), p.getParameterId());
            return true;
        }
        catch (CardServiceException ex) {
                System.out.println("PACE failed.");
                System.out.println(ex);
                return false;
        }
    }

    private Optional<PACEInfo> findPaceSecurityInfo() throws IOException
    {
        try(final var stream = passportService.getInputStream(PassportService.EF_CARD_ACCESS))
        {
            final var f = new CardAccessFile(stream);
            final var items = f.getSecurityInfos();
            return items == null ?
                    Optional.empty() :
                    items.stream()
                    .filter(x -> x instanceof PACEInfo)
                    .map(x -> (PACEInfo)x)
                    .findFirst();
        }
        catch (CardServiceException ex) {
            System.out.println("Could not read PACE security info.");
            System.out.println(ex);
            return Optional.empty();
        }
    }

    private boolean isOpen() {
        return isoDep != null && passportService != null && cardService != null;
    }
    public void close() throws IOException {
        if (passportService != null)
            passportService.close();
        if (cardService != null)
            cardService.close();
        if (isoDep != null)
            isoDep.close();
    }

    //TODO args -> which DGs et al?
    //TODO allow user to select a different dg as Fid/cont
    //@Override
    public RdeDocumentEnrollmentInfo getEnrollmentArgs(final UserSelectedEnrollmentArgs args, byte[] dg14Content)
            throws GeneralSecurityException, IOException, CardServiceException {
        if (args == null)
            throw new IllegalArgumentException();

        if (!isOpen())
            throw new IllegalStateException();

        try {
            var fileContent = getFileContent(args.getShortFileId());
            dumpArgsAndContent(args, fileContent);

            var caSessionArgs = new Dg14Reader(dg14Content).getCaSessionInfo();
            caSessionArgs.dump();

            var result = new RdeDocumentEnrollmentInfo();
            result.setDisplayName(args.getDisplayName());
            result.setShortFileId(args.getShortFileId());
            result.setDataGroup14(dg14Content);
            result.setFileContents(fileContent);
            result.setFileReadLength(args.getFileByteCount());
            result.setDocumentSecurityObject(sodFile.getEncoded());

            return result;
        }
        catch (CardServiceException e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    private void dumpArgsAndContent(final UserSelectedEnrollmentArgs args, final byte[] fileContent) {
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

    public byte[] getFileContent(final int shortFileId) throws IOException, CardServiceException, GeneralSecurityException {
        byte[] fileContent;
//        if (shortFileId == PassportService.SFI_DG14)
//            fileContent = dg14Content;
//        else {
            fileContent = readFileContent(shortFileId);
            authenticator.throwIfNotAuthentic(getEfSod(), shortFileId, fileContent);
//        }
        return fileContent;
    }

//    public Dg14Reader getDg14() throws IOException, CardServiceException, GeneralSecurityException {
//        if (dg14Reader == null) {
//            dg14Content = readFileContent(PassportService.SFI_DG14);
//            authenticator.throwIfNotAuthentic(getEfSod(), PassportService.SFI_DG14, dg14Content);
//            try (var is = new ByteArrayInputStream(dg14Content)) {
//                dg14Reader = new Dg14Reader(new DG14File(is));
//            }
//        }
//        return dg14Reader;
//    }

    private SODFile getEfSod() throws CardServiceException, IOException, GeneralSecurityException {
        if (sodFile == null) {
            try (var is = passportService.getInputStream(PassportService.EF_SOD, RdeDocumentConfig.MAX_BLOCK_SIZE)) {
                sodFile = new SODFile(is);
            }
            authenticator.throwIfNotAuthentic(sodFile);
        }
        return sodFile;
    }

    private byte[] readFileContent(int shortFileId) throws IOException, CardServiceException {
        var fileId = toFileIdentifier(shortFileId);
        try (var stream = passportService.getInputStream((short) fileId, RdeDocumentConfig.MAX_BLOCK_SIZE)) {
            return readAllBytes(stream);
        }
    }

    private static byte[] readAllBytes(final InputStream inputStream) throws IOException {
        final var buffer = new byte[4096];
        try (final var result = new ByteArrayOutputStream(4096)) {
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, bytesRead);
            }
            result.flush();
            return result.toByteArray();
        }
    }

    //@Override
    public byte[] getApduResponseForDecryption(MessageCipherInfo mca, byte[] dg14Content)
            throws GeneralSecurityException, IOException, CardServiceException {

        final var dg14 = new Dg14Reader(dg14Content);
        final var caProtocolOid = dg14.getCaSessionInfo().getCaInfo().getObjectIdentifier();
        final var keyAgreementAlgorithm = ChipAuthenticationInfo.toKeyAgreementAlgorithm(caProtocolOid);
        final var pcdPublicKey = getPublicKey(keyAgreementAlgorithm, org.bouncycastle.util.encoders.Hex.decode(mca.getRdeMessageDecryptionInfo().getPcdPublicKey()));
        final var encryptedCommand = org.bouncycastle.util.encoders.Hex.decode(mca.getRdeMessageDecryptionInfo().getCommand());

        System.out.println("DECRYPT PUB KEY      : " + pcdPublicKey);
        System.out.println("DECRYPT CHALLENGE    : " + encryptedCommand.length + " bytes " + mca.getRdeMessageDecryptionInfo().getCommand());

        try {
            EACCAProtocol.sendPublicKey(new EACCAAPDUSender(cardService), passportService.getWrapper(), caProtocolOid, null, pcdPublicKey);
        } catch (CardServiceException e) {
            e.printStackTrace();
            throw e;
        }

        byte[] response;
        try {
            var command = new CommandAPDU(encryptedCommand);
            response = cardService.transmit(command).getBytes();
        } catch (CardServiceException e) {
            e.printStackTrace();
            throw e;
        }

        System.out.println("DECRYPT RESPONSE     : " + response.length + " bytes, " + Hex.toHexString(response));
        return response;
    }

    private PublicKey getPublicKey(final String caEphemeralPublicKeyAlgorithm, final byte[] pcdPublicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        var publicKeySpec = new X509EncodedKeySpec(pcdPublicKey);
        var keyFactory = KeyFactory.getInstance(caEphemeralPublicKeyAlgorithm, new BouncyCastleProvider());
        return keyFactory.generatePublic(publicKeySpec);
    }

    public RdeMessageParameters doTestRbCall(Dg14Reader dg14, int shortFieldId, int readLength) throws CardServiceException, IOException, GeneralSecurityException {
        if (!isOpen())
            throw new IllegalStateException();

        var session = passportService.doEACCA(
                dg14.getCaSessionInfo().getCaPublicKeyInfo().getKeyId(),
                dg14.getCaSessionInfo().getCaInfo().getObjectIdentifier(),
                dg14.getCaSessionInfo().getCaPublicKeyInfo().getObjectIdentifier(),
                dg14.getCaSessionInfo().getCaPublicKeyInfo().getSubjectPublicKey()
        );

        var result = new RdeMessageParameters();
        result.setEphemeralPublicKey(session.getPCDPublicKey().getEncoded());

        System.out.println("CA SESSION >>>>");
        System.out.println("PCD Pri Key: " +  session.getPCDPrivateKey().getAlgorithm() + " " + Hex.toHexString(session.getPCDPrivateKey().getEncoded()));
        System.out.println("PCD Pub Key: " +  session.getPCDPublicKey().getAlgorithm() + " " + Hex.toHexString(session.getPCDPublicKey().getEncoded()));
        SecureWrapperDebug.dump(session.getWrapper());
        System.out.println("<<<< CA SESSION");

        SecureWrapperDebug.dump(session.getWrapper());

        var command = createRbCommandAPDU(shortFieldId, readLength);

        System.out.println("File :"+ shortFieldId + " " + Hex.toHexString(new byte[] {(byte)shortFieldId}));
        System.out.println("Length :"+ readLength + " " + readLength);
        System.out.println("CommandApdu :"+ Hex.toHexString(command.getBytes()));
        var wrappedCommand = passportService.getWrapper().wrap(command);
        result.setWrappedCommand(wrappedCommand.getBytes());
        System.out.println("Wrapped CommandApdu :"+ Hex.toHexString(wrappedCommand.getBytes()));

        var response = cardService.transmit(wrappedCommand);
        System.out.println("RESPONSE DATA (wrapped):   " + response.getData().length + " bytes, " + Hex.toHexString(response.getData()));
        System.out.println("RESPONSE (wrapped):   " + response.getBytes().length + " bytes, " + Hex.toHexString(response.getBytes()));
        result.setWrappedResponse(response.getBytes());
        SecureWrapperDebug.dump(session.getWrapper());

        var unwrappedResponse = passportService.getWrapper().unwrap(response);
        System.out.println("RESPONSE (unwrapped): " + unwrappedResponse.getBytes().length + " bytes, " + Hex.toHexString(unwrappedResponse.getBytes()));

        SecureWrapperDebug.dump(session.getWrapper());

        return result;
    }

    private CommandAPDU createRbCommandAPDU(final int shortFileId, final int fidByteCount) {
        int sfi = 0x80 | (shortFileId & 0xFF);
        return new CommandAPDU(ISO7816.CLA_ISO7816, ISO7816.INS_READ_BINARY, (byte) sfi, 0, fidByteCount);
    }
}
