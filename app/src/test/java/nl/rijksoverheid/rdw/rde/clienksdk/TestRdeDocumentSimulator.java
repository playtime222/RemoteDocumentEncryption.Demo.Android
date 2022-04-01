package nl.rijksoverheid.rdw.rde.clienksdk;

import net.sf.scuba.smartcards.CardServiceException;

import java.io.IOException;
import java.security.GeneralSecurityException;

import nl.rijksoverheid.rdw.rde.clientlib.apdusimulator.RdeDocumentSimulator;
import nl.rijksoverheid.rdw.rde.clientlib.documents.RdeDocumentEnrollmentInfo;

@Deprecated
public class TestRdeDocumentSimulator
{
    @Deprecated
    public byte[] getSimulatedAPDUResponse(final RdeDocumentEnrollmentInfo enrollmentArgs) throws IOException, GeneralSecurityException, CardServiceException {
        if (enrollmentArgs == null)
            throw new IllegalArgumentException();

        return new RdeDocumentSimulator().getSimulatedAPDUResponse(
                enrollmentArgs.getDataGroup14(),
                enrollmentArgs.getPcdPrivateKey(),
                enrollmentArgs.getFileContents(),
                enrollmentArgs.getFileReadLength()
        );
    }

//
//    //For testing - this is what the protocol does for a new session
//    @Deprecated
//    public SecretKey getKsEnc(final RdeDocumentEnrollmentInfo enrollmentArgs) throws IOException, GeneralSecurityException
//    {
//        if (enrollmentArgs == null)
//            throw new IllegalArgumentException();
//
//        final var dg14 = new Dg14Reader(enrollmentArgs.getDataGroup14());
//        final var privateKey = Extras.getPcdPrivateKey(enrollmentArgs.getPcdPrivateKey());
//        return getKsEnc(dg14.getCaSessionInfo().getCaInfo().getObjectIdentifier(), dg14.getCaSessionInfo().getCaPublicKeyInfo().getSubjectPublicKey() /*, privateKey*/).;
//    }
//
//    //Testing...
//    @Deprecated
//    public StartSessionResult getNewKsEnc(final String oid, final PublicKey piccPublicKey) throws GeneralSecurityException
//    {
//        if (oid == null)
//            throw new IllegalArgumentException();
//        if (piccPublicKey == null)
//            throw new IllegalArgumentException();
//
//        final var agreementAlg = ChipAuthenticationInfo.toKeyAgreementAlgorithm(oid);
//        final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(agreementAlg, new BouncyCastleProvider());
//        keyPairGenerator.initialize(getAlgorithmParameterSpec(piccPublicKey, agreementAlg));
//        final var pcdKeyPair = keyPairGenerator.generateKeyPair();
//        final var pcdPrivateKey = pcdKeyPair.getPrivate();
//        final var sharedSecret = EACCAProtocol.computeSharedSecret(agreementAlg, piccPublicKey, pcdPrivateKey);
//        final var cipherAlg = ChipAuthenticationInfo.toCipherAlgorithm(oid);
//        final var keyLength = ChipAuthenticationInfo.toKeyLength(oid);
//        return new StartSessionResult(Util.deriveKey(sharedSecret, cipherAlg, keyLength, Util.ENC_MODE), pcdPrivateKey);
//    }
//
//
//
//    private static AlgorithmParameterSpec getAlgorithmParameterSpec(final PublicKey piccPublicKey, final String agreementAlg)
//    {
//        if (DH.equals(agreementAlg) && piccPublicKey instanceof DHPublicKey)
//            return ((DHPublicKey)piccPublicKey).getParams();
//
//        if (ECDH.equals(agreementAlg) && piccPublicKey instanceof ECPublicKey)
//            return ((ECPublicKey)piccPublicKey).getParams();
//
//        throw new IllegalStateException("Cannot get parameters.");
//    }
}
