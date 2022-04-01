package nl.rijksoverheid.rdw.rde.clienksdk;

import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.jmrtd.Util;
import org.jmrtd.lds.ChipAuthenticationInfo;
import org.jmrtd.lds.icao.DG14File;
import org.jmrtd.protocol.EACCAProtocol;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPublicKey;

import nl.rijksoverheid.rdw.rde.clientlib.mrtdfiles.Dg14Reader;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class Dg14ReaderTests
{

    //final String content = "6E8201D9318201D530820184060904007F000702020102308201753082011D06072A8648CE3D020130820110020101303406072A8648CE3D0101022900D35E472036BC4FB7E13C785ED201E065F98FCFA6F6F40DEF4F92B9EC7893EC28FCD412B1F1B32E27305404283EE30B568FBAB0F883CCEBD46D3F3BB8A2A73513F5EB79DA66190EB085FFA9F492F375A97D860EB40428520883949DFDBC42D3AD198640688A6FE13F41349554B49ACC31DCCD884539816F5EB4AC8FB1F1A604510443BD7E9AFB53D8B85289BCC48EE5BFE6F20137D10A087EB6E7871E2A10A599C710AF8D0D39E2061114FDD05545EC1CC8AB4093247F77275E0743FFED117182EAA9C77877AAAC6AC7D35245D1692E8EE1022900D35E472036BC4FB7E13C785ED201E065F98FCFA5B68F12A32D482EC7EE8658E98691555B44C5931102010103520004710DA6DAB5B770920D3D4D6807B02A13059BEFB4926E2D00CFDE4B4471571473A582934BBE92059800663578C83419E3563FE3E8AF3AE58B521D3741693C9CE19B312392CB00F59AF086863186706396300F060A04007F00070202030204020101300D060804007F00070202020201013012060A04007F0007020204020402010202010E30170606678108010105020101060A04007F00070101040103";
    final String encoded = "6E8201D9318201D5300D060804007F0007020202020101300F060A04007F000702020302040201013012060A04007F0007020204020402010202010E30170606678108010105020101060A04007F0007010104010330820184060904007F000702020102308201753082011D06072A8648CE3D020130820110020101303406072A8648CE3D0101022900D35E472036BC4FB7E13C785ED201E065F98FCFA6F6F40DEF4F92B9EC7893EC28FCD412B1F1B32E27305404283EE30B568FBAB0F883CCEBD46D3F3BB8A2A73513F5EB79DA66190EB085FFA9F492F375A97D860EB40428520883949DFDBC42D3AD198640688A6FE13F41349554B49ACC31DCCD884539816F5EB4AC8FB1F1A604510443BD7E9AFB53D8B85289BCC48EE5BFE6F20137D10A087EB6E7871E2A10A599C710AF8D0D39E2061114FDD05545EC1CC8AB4093247F77275E0743FFED117182EAA9C77877AAAC6AC7D35245D1692E8EE1022900D35E472036BC4FB7E13C785ED201E065F98FCFA5B68F12A32D482EC7EE8658E98691555B44C5931102010103520004710DA6DAB5B770920D3D4D6807B02A13059BEFB4926E2D00CFDE4B4471571473A582934BBE92059800663578C83419E3563FE3E8AF3AE58B521D3741693C9CE19B312392CB00F59AF086863186706396";

    @Test
    public void ctor() throws IOException
    {
        var is = new ByteArrayInputStream(Hex.decode(encoded));
        var adapted = new Dg14Reader(new DG14File(is));

        System.out.println(adapted.getCaSessionInfo().getCaInfo());
        System.out.println(adapted.getCaSessionInfo().getCaInfo().getObjectIdentifier());
        System.out.println(adapted.getCaSessionInfo().getCaPublicKeyInfo());
        System.out.println(adapted.getCaSessionInfo().getCaPublicKeyInfo().getObjectIdentifier());
        System.out.println(adapted.getCaSessionInfo().getCaPublicKeyInfo().getProtocolOIDString());

//        is = new ByteArrayInputStream(Hex.decode(encoded));
//        var encodedVersion = new DG14File(is);

        // OID: 0.4.0.127.0.7.2.2.3.2.4
        // ChipAuthenticationInfo [protocol: id-CA-ECDH-AES-CBC-CMAC-256, version: 1, keyId: -]
        // 0.4.0.127.0.7.2.2.3.2.4
        // ChipAuthenticationPublicKeyInfo [protocol: id-PK-ECDH, chipAuthenticationPublicKey: EC [brainpoolP320r1], keyId: -]
        // 0.4.0.127.0.7.2.2.1.2
        // id-PK-ECDH
    }

    @Test
    public void SimpleKsEncTest() throws GeneralSecurityException
    {
        var z = Base64.decode("MIIBdTCCAR0GByqGSM49AgEwggEQAgEBMDQGByqGSM49AQECKQDTXkcgNrxPt+E8eF7SAeBl+Y/Ppvb0De9PkrnseJPsKPzUErHxsy4nMFQEKD7jC1aPurD4g8zr1G0/O7iipzUT9et52mYZDrCF/6n0kvN1qX2GDrQEKFIIg5Sd/bxC060ZhkBoim/hP0E0lVS0mswx3M2IRTmBb160rI+x8aYEUQRDvX6a+1PYuFKJvMSO5b/m8gE30QoIfrbnhx4qEKWZxxCvjQ054gYRFP3QVUXsHMirQJMkf3cnXgdD/+0RcYLqqcd4d6qsasfTUkXRaS6O4QIpANNeRyA2vE+34Tx4XtIB4GX5j8+lto8Soy1ILsfuhljphpFVW0TFkxECAQEDUgAElKPsrXMwqvMtIwCRfkFIvGIp0hFQamPxHeG7VhBI5cBpDR0sgQ6ubISrGdS/6EtFNp52bDD8tBh89IDgdZR5C/f/kPqdzCFg5emk4967B2A=");
        //SecretKey ksEnc = Util.deriveKey(z, "AES", 256, null, 1, PassportService.NO_PACE_KEY_REFERENCE);

        var publicKeySpec = new X509EncodedKeySpec(z);
        var keyFactory = KeyFactory.getInstance("ECDH", new BouncyCastleProvider());
        var publicKey = keyFactory.generatePublic(publicKeySpec);
        var typedPublicKey = (BCECPublicKey)publicKey;

        ECPoint generator = typedPublicKey.getParams().getGenerator();
        BigInteger affineX = generator.getAffineX();
        var fortyBytes = affineX.toByteArray();
        var string = Hex.toHexString(fortyBytes);
        var ksEnc = Util.deriveKey(fortyBytes, "AES", 256, null, Util.ENC_MODE);

        //TODO get result data for an assert here.
    }

    @Test
    public void MatchingKsEnc()
            throws GeneralSecurityException, IOException
    {
        var is = new ByteArrayInputStream(Hex.decode(encoded));
        var dg14 = new Dg14Reader(new DG14File(is));
        var result = getNewKsEnc(dg14.getCaSessionInfo().getCaInfo().getObjectIdentifier(), dg14.getCaSessionInfo().getCaPublicKeyInfo().getSubjectPublicKey());
        var replay = getKsEnc(dg14.getCaSessionInfo().getCaInfo().getObjectIdentifier(), dg14.getCaSessionInfo().getCaPublicKeyInfo().getSubjectPublicKey(), result.pcdPrivateKey);

        Assert.assertArrayEquals(result.aesKey.getEncoded(), replay.getEncoded());
    }

    public static final String DH = "DH";
    public static final String ECDH = "ECDH";


    //Testing...
    //Now creating ksMac and ksEnc at the same time without repeating the
    @Deprecated
    public static SecretKey getKsEnc(String caProtocolOid, PublicKey piccPublicKey, PrivateKey pcdPrivateKey) throws GeneralSecurityException
    {
        if (caProtocolOid == null)
            throw new IllegalArgumentException();
        if (piccPublicKey == null)
            throw new IllegalArgumentException();

        final var agreementAlg = ChipAuthenticationInfo.toKeyAgreementAlgorithm(caProtocolOid); //Only returns EC or ECDH
        final var sharedSecret = EACCAProtocol.computeSharedSecret(agreementAlg, piccPublicKey, pcdPrivateKey);

        final var cipherAlg = ChipAuthenticationInfo.toCipherAlgorithm(caProtocolOid);
        final var keyLength = ChipAuthenticationInfo.toKeyLength(caProtocolOid);
        return Util.deriveKey(sharedSecret, cipherAlg, keyLength, Util.ENC_MODE);
    }


    //Testing...
    //This simulates how the EACCA protocol creates the pcdPrivateKay and hence the ksEnc/ksMac
    @Deprecated
    public static StartSessionResult getNewKsEnc(final String oid, final PublicKey piccPublicKey) throws GeneralSecurityException
    {
        if (oid == null)
            throw new IllegalArgumentException();
        if (piccPublicKey == null)
            throw new IllegalArgumentException();

        final var agreementAlg = ChipAuthenticationInfo.toKeyAgreementAlgorithm(oid);
        final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(agreementAlg, new BouncyCastleProvider());
        keyPairGenerator.initialize(getAlgorithmParameterSpec(piccPublicKey, agreementAlg));
        final var pcdKeyPair = keyPairGenerator.generateKeyPair();
        final var pcdPrivateKey = pcdKeyPair.getPrivate();
        final var sharedSecret = EACCAProtocol.computeSharedSecret(agreementAlg, piccPublicKey, pcdPrivateKey);
        final var cipherAlg = ChipAuthenticationInfo.toCipherAlgorithm(oid);
        final var keyLength = ChipAuthenticationInfo.toKeyLength(oid);
        return new StartSessionResult(Util.deriveKey(sharedSecret, cipherAlg, keyLength, Util.ENC_MODE), pcdPrivateKey);
    }

    //Testing...
    @Deprecated
    private static AlgorithmParameterSpec getAlgorithmParameterSpec(final PublicKey piccPublicKey, final String agreementAlg)
    {
        if (DH.equals(agreementAlg) && piccPublicKey instanceof DHPublicKey)
            return ((DHPublicKey)piccPublicKey).getParams();

        if (ECDH.equals(agreementAlg) && piccPublicKey instanceof ECPublicKey)
            return ((ECPublicKey)piccPublicKey).getParams();

        throw new IllegalStateException("Cannot get parameters.");
    }

//    @Test
//    public void KsEncTest() throws GeneralSecurityException
//    {
//        var expectedKsEnc = new byte[]{-25, -104, -59, -72, -117, -78, -124, -63, 123, 44, 45, 42, -109, -42, 18, -77, -25, -104, -59, -72, -117, -78, -124, -63};
//        var z = Base64.decode("MIIBdTCCAR0GByqGSM49AgEwggEQAgEBMDQGByqGSM49AQECKQDTXkcgNrxPt+E8eF7SAeBl+Y/Ppvb0De9PkrnseJPsKPzUErHxsy4nMFQEKD7jC1aPurD4g8zr1G0/O7iipzUT9et52mYZDrCF/6n0kvN1qX2GDrQEKFIIg5Sd/bxC060ZhkBoim/hP0E0lVS0mswx3M2IRTmBb160rI+x8aYEUQRDvX6a+1PYuFKJvMSO5b/m8gE30QoIfrbnhx4qEKWZxxCvjQ054gYRFP3QVUXsHMirQJMkf3cnXgdD/+0RcYLqqcd4d6qsasfTUkXRaS6O4QIpANNeRyA2vE+34Tx4XtIB4GX5j8+lto8Soy1ILsfuhljphpFVW0TFkxECAQEDUgAElKPsrXMwqvMtIwCRfkFIvGIp0hFQamPxHeG7VhBI5cBpDR0sgQ6ubISrGdS/6EtFNp52bDD8tBh89IDgdZR5C/f/kPqdzCFg5emk4967B2A=");
//        //var file = Base64.decode("boIB2TGCAdUwggGEBgkEAH8ABwICAQIwggF1MIIBHQYHKoZIzj0CATCCARACAQEwNAYHKoZIzj0BAQIpANNeRyA2vE+34Tx4XtIB4GX5j8+m9vQN70+Suex4k+wo/NQSsfGzLicwVAQoPuMLVo+6sPiDzOvUbT87uKKnNRP163naZhkOsIX/qfSS83WpfYYOtAQoUgiDlJ39vELTrRmGQGiKb+E/QTSVVLSazDHczYhFOYFvXrSsj7HxpgRRBEO9fpr7U9i4Uom8xI7lv+byATfRCgh+tueHHioQpZnHEK+NDTniBhEU/dBVRewcyKtAkyR/dydeB0P/7RFxguqpx3h3qqxqx9NSRdFpLo7hAikA015HIDa8T7fhPHhe0gHgZfmPz6W2jxKjLUgux+6GWOmGkVVbRMWTEQIBAQNSAARxDabatbdwkg09TWgHsCoTBZvvtJJuLQDP3ktEcVcUc6WCk0u+kgWYAGY1eMg0GeNWP+Porzrli1IdN0FpPJzhmzEjkssA9ZrwhoYxhnBjljAPBgoEAH8ABwICAwIEAgEBMA0GCAQAfwAHAgICAgEBMBIGCgQAfwAHAgIEAgQCAQICAQ4wFwYGZ4EIAQEFAgEBBgoEAH8ABwEBBAED");
//        //var length = 450;
//
//        var value = Hex.decodeStrict("322a2fab5b0c993783f787e56ff841ea2333f042");
//
//
//        var actual = new byte[0];
//
////        getKsEnc(
////                "0.4.0.127.0.7.2.2.1.2",
////                "0.4.0.127.0.7.2.2.3.2.4",
////                new BCECPublicKey( );
//
//
//        var
//
//        var ksEnc = Util.deriveKey(z, "AES", 256, Util.ENC_MODE);
//        var actualKsEnc = ksEnc.getEncoded();
//        //Assert.assertArrayEquals(expectedKsEnc, actualKsEnc);
//
//        ksEnc = Util.deriveKey(z, "AES", 256, null, Util.ENC_MODE);
//        Assert.assertArrayEquals(expectedKsEnc, actualKsEnc);
//
//
////        for (var i = 1; i <= 477; i++)
////        {
////            var actual = new RdeDocumentSimulator().getSimulatedAPDUResponse(file, i, "AES", z);
////            System.out.println("Actual:" + Base64.toBase64String(actual));
////
////            if (Arrays.areEqual(expected, actual))
////            {
////                System.out.println("ANSWER: " + i);
////                return;
////            }
////
////            System.out.println("NOPE: " + i);
////        }
//        //Assert.assertArrayEquals(expected, actual);
//    }
//
//    public SecretKey getKsEnc(String oid, String publicKeyOID, PublicKey piccPublicKey)
//            throws GeneralSecurityException
//    {
//        if (piccPublicKey == null)
//        {
//            throw new IllegalArgumentException("PICC public key is null");
//        }
//
//        var agreementAlg = ChipAuthenticationInfo.toKeyAgreementAlgorithm(oid);
//        if (agreementAlg == null)
//        {
//            throw new IllegalArgumentException("Unknown agreement algorithm");
//        }
//        if (!("ECDH".equals(agreementAlg) || "DH".equals(agreementAlg)))
//        {
//            throw new IllegalArgumentException("Unsupported agreement algorithm, expected ECDH or DH, found " + agreementAlg);
//        }
//
//        if (oid == null)
//        {
//            oid = inferChipAuthenticationOIDfromPublicKeyOID(publicKeyOID);
//        }
//
//        AlgorithmParameterSpec params = null;
//        if ("DH".equals(agreementAlg))
//        {
//            DHPublicKey piccDHPublicKey = (DHPublicKey) piccPublicKey;
//            params = piccDHPublicKey.getParams();
//        } else if ("ECDH".equals(agreementAlg))
//        {
//            ECPublicKey piccECPublicKey = (ECPublicKey) piccPublicKey;
//            params = piccECPublicKey.getParams();
//        }
//
//        /* Generate the inspection system's ephemeral key pair. */
//        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(agreementAlg, new BouncyCastleProvider());
//        keyPairGenerator.initialize(params);
//        KeyPair pcdKeyPair = keyPairGenerator.generateKeyPair();
//        PublicKey pcdPublicKey = pcdKeyPair.getPublic();
//        PrivateKey pcdPrivateKey = pcdKeyPair.getPrivate();
//
//        //sendPublicKey(service, wrapper, oid, keyId, pcdPublicKey);
//        //byte[] keyHash = getKeyHash(agreementAlg, pcdPublicKey);
//
//        byte[] sharedSecret = computeSharedSecret(agreementAlg, piccPublicKey, pcdPrivateKey);
//
//        //wrapper = restartSecureMessaging(oid, sharedSecret, maxTranceiveLength, shouldCheckMAC);
//        String cipherAlg = ChipAuthenticationInfo.toCipherAlgorithm(oid);
//        int keyLength = ChipAuthenticationInfo.toKeyLength(oid);
//
//        SecretKey ksEnc = Util.deriveKey(sharedSecret, cipherAlg, keyLength, Util.ENC_MODE);
//        //SecretKey ksMac = Util.deriveKey(sharedSecret, cipherAlg, keyLength, Util.MAC_MODE);
//
//        return ksEnc;
//    }
//
//    private static String inferChipAuthenticationOIDfromPublicKeyOID(String publicKeyOID) {
//        if (SecurityInfo.ID_PK_ECDH.equals(publicKeyOID)) {
//            /*
//             * This seems to work for French passports (generation 2013, 2014),
//             * but it is best effort.
//             */
//            //LOGGER.warning("Could not determine ChipAuthentication algorithm, defaulting to id-CA-ECDH-3DES-CBC-CBC");
//            return SecurityInfo.ID_CA_ECDH_3DES_CBC_CBC;
//        } else if (SecurityInfo.ID_PK_DH.equals(publicKeyOID)) {
//            /*
//             * Not tested. Best effort.
//             */
//            //LOGGER.warning("Could not determine ChipAuthentication algorithm, defaulting to id-CA-DH-3DES-CBC-CBC");
//            return SecurityInfo.ID_CA_DH_3DES_CBC_CBC;
//        } else {
//            throw new IllegalStateException();
//            //LOGGER.warning("No ChipAuthenticationInfo and unsupported ChipAuthenticationPublicKeyInfo public key OID " + publicKeyOID);
//        }
//    }
}
