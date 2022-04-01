package nl.rijksoverheid.rdw.rde.clientlib.apdusimulator;

import net.sf.scuba.smartcards.CardServiceException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jmrtd.Util;
import org.jmrtd.lds.ChipAuthenticationInfo;
import org.jmrtd.protocol.EACCAProtocol;

import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

import nl.rijksoverheid.rdw.rde.clientlib.mrtdfiles.*;

public class RdeDocumentSimulator
{
    //From the database
    public byte[] getSimulatedAPDUResponse(final byte[] dg14Content, final byte[] pcdPrivateKey, final byte[] fileContent, final int fileReadLength) throws IOException, GeneralSecurityException, CardServiceException
    {
        if (dg14Content == null)
            throw new IllegalArgumentException();
        if (pcdPrivateKey == null)
            throw new IllegalArgumentException();
        if (fileContent == null)
            throw new IllegalArgumentException();
        if (fileReadLength < 1)
            throw new IllegalArgumentException();

        final var dg14 = new Dg14Reader(dg14Content);
        return getSimulatedAPDUResponse(
                dg14.getCaSessionInfo().getCaInfo().getObjectIdentifier(),
                dg14.getCaSessionInfo().getCaPublicKeyInfo().getSubjectPublicKey(),
                decodePcdPrivateKey(pcdPrivateKey),
                fileContent,
                fileReadLength
        );
    }

    private static PrivateKey decodePcdPrivateKey(final byte[] buffer) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        final var keyFactory = KeyFactory.getInstance("EC", new BouncyCastleProvider());
        final var keySpec = new PKCS8EncodedKeySpec(buffer);
        return keyFactory.generatePrivate(keySpec);
    }

    private byte[] getSimulatedAPDUResponse(final String caProtocolOid, final PublicKey piccPublicKey, final PrivateKey pcdPrivateKey, final byte[] file, final int readLength)
            throws GeneralSecurityException, IOException
    {
        final var agreementAlg = ChipAuthenticationInfo.toKeyAgreementAlgorithm(caProtocolOid); //Only returns EC or ECDH
        final var sharedSecret = EACCAProtocol.computeSharedSecret(agreementAlg, piccPublicKey, pcdPrivateKey);
        final var cipherAlg = ChipAuthenticationInfo.toCipherAlgorithm(caProtocolOid);
        final var keyLength = ChipAuthenticationInfo.toKeyLength(caProtocolOid);

        final var ksEnc = Util.deriveKey(sharedSecret, cipherAlg, keyLength, Util.ENC_MODE);
        final var ksMac = Util.deriveKey(sharedSecret, cipherAlg, keyLength, Util.MAC_MODE);

        //Encoder is hard-coded to AES so we just need the bytes of ksEnc/Mac
        byte[] ksEncBuffer = ksEnc.getEncoded();
        byte[] ksMacBuffer = ksMac.getEncoded();
        return new AesApduResponseEncoder(ksEncBuffer,ksMacBuffer).write(Arrays.copyOf(file, readLength));
    }
}

