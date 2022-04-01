package nl.rijksoverheid.rdw.rde.clienksdk;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import nl.rijksoverheid.rdw.rde.clientlib.apdusimulator.AesApduResponseEncoder;

public class SimulatorEncoderTests
{

    protected IvParameterSpec getIV(Key ksEnc, int ssc) throws GeneralSecurityException
    {
        var cipher = Cipher.getInstance("AES/ECB/NoPadding", new BouncyCastleProvider());
        cipher.init(Cipher.ENCRYPT_MODE, ksEnc);
        var encodedSsc = new byte[16];
        encodedSsc[15] = (byte)ssc;
        var encryptedSSC = cipher.doFinal(encodedSsc);
        return new IvParameterSpec(encryptedSSC);
    }

    @Test
    public void ciphers() throws GeneralSecurityException
    {
        var p = new BouncyCastleProvider();
        var encrypted = Hex.decode("6D9F6F6FDA79FF285C2C1D3AEA1FFD03");
        var ksEnc = new SecretKeySpec(Hex.decode("7319D1537EF2FE5CB46AFCFF2DF33B521F3A0C4FA92212D98EB49D9CD6BB8916"), "AES");

        {
            var c1 = Cipher.getInstance(AesApduResponseEncoder.DO87_CIPHER, p);
            c1.init(Cipher.DECRYPT_MODE, ksEnc, new IvParameterSpec(new byte[16]));
            var actual = c1.doFinal(encrypted);
            System.out.println("Actual:" + Hex.toHexString(actual));
        }
        {
            var c1 = Cipher.getInstance(AesApduResponseEncoder.DO87_CIPHER, p);
            var iv = new byte[16];
            iv[15] = 1;
            c1.init(Cipher.DECRYPT_MODE, ksEnc, new IvParameterSpec(iv));
            var actual = c1.doFinal(encrypted);
            System.out.println("Actual:" + Hex.toHexString(actual));
        }
        {
            var c1 = Cipher.getInstance(AesApduResponseEncoder.DO87_CIPHER, p);
            var iv = getIV(ksEnc, 0);
            c1.init(Cipher.DECRYPT_MODE, ksEnc, iv);
            var actual = c1.doFinal(encrypted);
            System.out.println("Actual:" + Hex.toHexString(actual));
        }
        {
            var c1 = Cipher.getInstance(AesApduResponseEncoder.DO87_CIPHER, p);
            var iv = getIV(ksEnc, 1);
            c1.init(Cipher.DECRYPT_MODE, ksEnc, iv);
            var actual = c1.doFinal(encrypted);
            System.out.println("Actual:" + Hex.toHexString(actual));
        }
        {
            var c1 = Cipher.getInstance(AesApduResponseEncoder.DO87_CIPHER, p);
            var iv = getIV(ksEnc, 2);
            c1.init(Cipher.DECRYPT_MODE, ksEnc, iv);
            var actual = c1.doFinal(encrypted);
            System.out.println("Actual:" + Hex.toHexString(actual));
        }
    }

    @Test
    public void EncodeLength01() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException
    {
        encodedLengthTest(1, "1101");
    }

    @Test
    public void EncodeLength08() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException
    {
        encodedLengthTest(8, "1101");
    }

    @Test
    public void EncodeLength16() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException
    {
        encodedLengthTest(16, "2101");
    }

    @Test
    public void EncodeLength257() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException
    {
        encodedLengthTest(257, "82011101");
    }

    private void encodedLengthTest(final int inputLength, final String expected) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException
    {
        var encoder = new AesApduResponseEncoder(new byte[16], new byte[16]);
        var paddedLength = encoder.getPaddedLength(inputLength);
        System.out.println("PaddedLength: "+paddedLength);
        var actual = encoder.getEncodedDo87Size(paddedLength);
        System.out.println("Actual  : "+ Hex.toHexString(actual));
        System.out.println("Expected: "+ expected);
        Assert.assertArrayEquals(Hex.decode(expected), actual);
    }

    @Test
    public void ApduResponseWrite1() throws IOException, GeneralSecurityException
    {
        ApduResponseWriteTest(
                "7319D1537EF2FE5CB46AFCFF2DF33B521F3A0C4FA92212D98EB49D9CD6BB8916",
                "ADCBA368FD14A836908252EF76D09BAD2766C5FFB2FE7857F468676FC4B293E0",
                1,
                "8711016D9F6F6FDA79FF285C2C1D3AEA1FFD03990290008E089C3B7B89BB7849929000"
        );
    }

    private void ApduResponseWriteTest(final String ksEncString, final String ksMacString, final int requestedLength, String expectedWrappedResponse) throws IOException, GeneralSecurityException
    {
        var ksEnc = Hex.decode(ksEncString);
        var ksMac = Hex.decode(ksMacString);

        var result = new AesApduResponseEncoder(ksEnc, ksMac).write(Arrays.copyOf(Hex.decode(hexEncodedDg14) , requestedLength));

        System.out.println("Wrapped : " + Hex.toHexString(result));
        System.out.println("Expected: " + expectedWrappedResponse.toLowerCase(Locale.ROOT));
        Assert.assertArrayEquals(Hex.decode(expectedWrappedResponse), result);
        //assertIsDg14Content(result);
    }

    //From SPECI2014 passport
    static final String hexEncodedDg14 = "6E8201D9318201D5300D060804007F0007020202020101300F060A04007F000702020302040201013012060A04007F0007020204020402010202010E30170606678108010105020101060A04007F0007010104010330820184060904007F000702020102308201753082011D06072A8648CE3D020130820110020101303406072A8648CE3D0101022900D35E472036BC4FB7E13C785ED201E065F98FCFA6F6F40DEF4F92B9EC7893EC28FCD412B1F1B32E27305404283EE30B568FBAB0F883CCEBD46D3F3BB8A2A73513F5EB79DA66190EB085FFA9F492F375A97D860EB40428520883949DFDBC42D3AD198640688A6FE13F41349554B49ACC31DCCD884539816F5EB4AC8FB1F1A604510443BD7E9AFB53D8B85289BCC48EE5BFE6F20137D10A087EB6E7871E2A10A599C710AF8D0D39E2061114FDD05545EC1CC8AB4093247F77275E0743FFED117182EAA9C77877AAAC6AC7D35245D1692E8EE1022900D35E472036BC4FB7E13C785ED201E065F98FCFA5B68F12A32D482EC7EE8658E98691555B44C5931102010103520004710DA6DAB5B770920D3D4D6807B02A13059BEFB4926E2D00CFDE4B4471571473A582934BBE92059800663578C83419E3563FE3E8AF3AE58B521D3741693C9CE19B312392CB00F59AF086863186706396";
    private void assertIsDg14Content(byte[] unwrappedResponse)
    {
        //Ignore SW1/2 at end of response
        Assert.assertArrayEquals(Arrays.copyOf(Hex.decode(hexEncodedDg14), unwrappedResponse.length - 2), Arrays.copyOf(unwrappedResponse, unwrappedResponse.length - 2));
    }


//    case (byte) 0x87:
//    case (byte) 0x85: //Similar to an 87...
//    case (byte) 0x99:
//    case (byte) 0x8E:

    /*
    *
I/System.out: Wrapper Type : AESSecureMessagingWrapper
I/System.out: KsEnc   : 32, 78230C78BE4BCA1A28C3A3F691240314135E60E2CE87F06DA0CE532052BBC109
I/System.out: KsMac   : 32, 8DCC02511238AF4AE1DD5B5AB6B46E8A04223CFA379BF7D6A3F06149C00C3A2A
Counter : 00000000000000000000000000000000
Padding : 16
I/System.out: RESPONSE (wrapped):   244 bytes, 8781E1015A209F323EF8796598011DCAB65200DBB7E5917678C0D14E5ADB366FDE0F1A8B73CC8C020E798067E3A123E00A67751B5C1A2FA369EB8BD701A4ED44C5432D9D8F348D30EDD06AFF6BFE067F82E3F6552723E843F2CCA2ED432C2EA1B3147C785189B82C9E81EECD402D6A0EE5C7E14E51CA3AAAD27F996141279ABD61EDB1CB1AFECD1C97F179529212BAE05AE621024B6815C6B83C3B21DB9EAB62333214BD7D41B920787FD4D1097693AF44837E6ADB975829548BD50145A5FE05B8E78C08903099D149AE61179B569D89B330BC65236BA5EE420015028CF8B7A4DCD2D6A7990290008E0868F40DA717B5CEDA9000
I/System.out: RESPONSE (unwrapped): 225 bytes, 6E8201D9318201D530820184060904007F000702020102308201753082011D06072A8648CE3D020130820110020101303406072A8648CE3D0101022900D35E472036BC4FB7E13C785ED201E065F98FCFA6F6F40DEF4F92B9EC7893EC28FCD412B1F1B32E27305404283EE30B568FBAB0F883CCEBD46D3F3BB8A2A73513F5EB79DA66190EB085FFA9F492F375A97D860EB40428520883949DFDBC42D3AD198640688A6FE13F41349554B49ACC31DCCD884539816F5EB4AC8FB1F1A604510443BD7E9AFB53D8B85289BCC48EE5BFE6F20137D10A087EB6E7871E2A10A599C7109000
    *
    *
    * */


    /*
    * 87 block type
    * 81 & 7F == 1
    * E10109A13C5DA3927FB752283E0F79D086BA0D344AC0E91D0273FF950AD5BD3E8E4F0B2E55DA6D25BC5C3D14EFEBF5405F666482D480F8D80D12FE2F36120352F
    * 87
    * 31 = 49
    * 7C957AF41D8A9CC3260D42F10A9CE962EF049EF4CACE0243B73FCA9A
    * 966BB6BEB08EF40FF9CC4
    * 99 ?
    * 4E567057BD0CF4D39C22FA10361D38C956163DFCC0D298D05BD939F0FAE25D5B10700C336DEF6DAF4A20C3C45E4DF49664CAD6384BDEA6EC1C68438A3032975027898B3DD2571B90B45556C268E251C283752911EC85842165D74EDFFB4E409675791D000F
    * 87
    * 51
    * CE58D561CBC832BF1810EEB4173A8C0FC2
    * 99 ?
    * 0290008E085A2EC4BB430B0E8F
    *
    *
    * 9000 - sw 1/2
    * */
    @Test
    public void unwrapTest1() throws GeneralSecurityException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        // ResponseAPDU: 35 bytes, SW=9000 ?
        // Wrapper Type : AESSecureMessagingWrapper
        // KsEnc   : 32, 70CB16D37355A7F5BDD9C21D237D4B5B127171EC8BA622AFBFD4EDFA8367E95B
        // KsMac   : 32, 4AB55C5D1F520FB9364419FEF42AC4364FCAF156F1218C2E8ABA83D1DFF570D4
        // Padding : 16
        // Counter : 00000000000000000000000000000000
        // RESPONSE (wrapped):   244 bytes, 8781E10121DB1D75B4AE1D167ADE6EAB951A82AF37F1EC828A819E088856B41CFC9D0E55CE04394A7A33A494BA6EEA6852D8D566172481FF6CCE1C029FE49A60E9B9B4674F58613CFBA84EF63ABBBB5D5F1CE39B8B9A1450861C23336FA144920B54E53B1B1C7B4A8415735ACA9031EBB19734592A2534FE6E22D6810DD89E8E8976252400C3F71777E2B0F1DBB8688504B0585131E61835079277824EBB6C16131F88B5E53B77139D1C28B15981A3F52817B8759C3E7C46DB0F3D2397006ABAAF8B8593C046AA35C6279E99BFC252B58D6D9591CB71BF6943F812B1B53372DC8C39D5F7990290008E08CA281AA04AED46159000
        // RESPONSE (unwrapped): 225 bytes, 6E8201D9318201D530820184060904007F000702020102308201753082011D06072A8648CE3D020130820110020101303406072A8648CE3D0101022900D35E472036BC4FB7E13C785ED201E065F98FCFA6F6F40DEF4F92B9EC7893EC28FCD412B1F1B32E27305404283EE30B568FBAB0F883CCEBD46D3F3BB8A2A73513F5EB79DA66190EB085FFA9F492F375A97D860EB40428520883949DFDBC42D3AD198640688A6FE13F41349554B49ACC31DCCD884539816F5EB4AC8FB1F1A604510443BD7E9AFB53D8B85289BCC48EE5BFE6F20137D10A087EB6E7871E2A10A599C7109000

        unwrapTest1("8781E10121DB1D75B4AE1D167ADE6EAB951A82AF37F1EC828A819E088856B41CFC9D0E55CE04394A7A33A494BA6EEA6852D8D566172481FF6CCE1C029FE49A60E9B9B4674F58613CFBA84EF63ABBBB5D5F1CE39B8B9A1450861C23336FA144920B54E53B1B1C7B4A8415735ACA9031EBB19734592A2534FE6E22D6810DD89E8E8976252400C3F71777E2B0F1DBB8688504B0585131E61835079277824EBB6C16131F88B5E53B77139D1C28B15981A3F52817B8759C3E7C46DB0F3D2397006ABAAF8B8593C046AA35C6279E99BFC252B58D6D9591CB71BF6943F812B1B53372DC8C39D5F7990290008E08CA281AA04AED46159000",
                  "6E8201D9318201D530820184060904007F000702020102308201753082011D06072A8648CE3D020130820110020101303406072A8648CE3D0101022900D35E472036BC4FB7E13C785ED201E065F98FCFA6F6F40DEF4F92B9EC7893EC28FCD412B1F1B32E27305404283EE30B568FBAB0F883CCEBD46D3F3BB8A2A73513F5EB79DA66190EB085FFA9F492F375A97D860EB40428520883949DFDBC42D3AD198640688A6FE13F41349554B49ACC31DCCD884539816F5EB4AC8FB1F1A604510443BD7E9AFB53D8B85289BCC48EE5BFE6F20137D10A087EB6E7871E2A10A599C7109000",
                "70CB16D37355A7F5BDD9C21D237D4B5B127171EC8BA622AFBFD4EDFA8367E95B",
                "4AB55C5D1F520FB9364419FEF42AC4364FCAF156F1218C2E8ABA83D1DFF570D4");
    }

    private void unwrapTest1(final String wrapped, final String unwrapped, final String ksEnc, final String ksMac) throws GeneralSecurityException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        unwrapTest1(Hex.decode(wrapped),Hex.decode(unwrapped),Hex.decode(ksEnc),Hex.decode(ksMac));
    }

    private void unwrapTest1(final byte[] wrapped, final byte[] unwrapped, final byte[] ksEnc, final byte[] ksMac) throws GeneralSecurityException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        //1 will be incremented to 2 during the unwrap
        var f = new ReflectorApduResponseAesUnwrapper();
        var u = f.unwrap(new SecretKeySpec(ksEnc, "AES"), new SecretKeySpec(ksMac, "AESMAC"), false, 1, wrapped);
        System.out.println("actual: "+ Hex.toHexString(unwrapped));
        System.out.println("expected: "+ Hex.toHexString(unwrapped));
        Assert.assertArrayEquals(unwrapped, u);
    }


    @Test
    public void unwrapTest2() throws GeneralSecurityException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        unwrapTest2("8781E10121DB1D75B4AE1D167ADE6EAB951A82AF37F1EC828A819E088856B41CFC9D0E55CE04394A7A33A494BA6EEA6852D8D566172481FF6CCE1C029FE49A60E9B9B4674F58613CFBA84EF63ABBBB5D5F1CE39B8B9A1450861C23336FA144920B54E53B1B1C7B4A8415735ACA9031EBB19734592A2534FE6E22D6810DD89E8E8976252400C3F71777E2B0F1DBB8688504B0585131E61835079277824EBB6C16131F88B5E53B77139D1C28B15981A3F52817B8759C3E7C46DB0F3D2397006ABAAF8B8593C046AA35C6279E99BFC252B58D6D9591CB71BF6943F812B1B53372DC8C39D5F7990290008E08CA281AA04AED46159000",
                "6E8201D9318201D530820184060904007F000702020102308201753082011D06072A8648CE3D020130820110020101303406072A8648CE3D0101022900D35E472036BC4FB7E13C785ED201E065F98FCFA6F6F40DEF4F92B9EC7893EC28FCD412B1F1B32E27305404283EE30B568FBAB0F883CCEBD46D3F3BB8A2A73513F5EB79DA66190EB085FFA9F492F375A97D860EB40428520883949DFDBC42D3AD198640688A6FE13F41349554B49ACC31DCCD884539816F5EB4AC8FB1F1A604510443BD7E9AFB53D8B85289BCC48EE5BFE6F20137D10A087EB6E7871E2A10A599C7109000",
                "70CB16D37355A7F5BDD9C21D237D4B5B127171EC8BA622AFBFD4EDFA8367E95B",
                "4AB55C5D1F520FB9364419FEF42AC4364FCAF156F1218C2E8ABA83D1DFF570D4");
    }

    private void unwrapTest2(final String wrapped, final String unwrapped, final String ksEnc, final String ksMac) throws GeneralSecurityException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        unwrapTest2(Hex.decode(wrapped),Hex.decode(unwrapped),Hex.decode(ksEnc),Hex.decode(ksMac));
    }

    private void unwrapTest2(final byte[] wrapped, final byte[] unwrapped, final byte[] ksEnc, final byte[] ksMac) throws GeneralSecurityException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        var f = new ReflectorApduResponseAesUnwrapper();
        var u = f.unwrap(new SecretKeySpec(ksEnc, "AES"), new SecretKeySpec(ksMac, "AESMAC"), false, 1, wrapped);
        System.out.println("actual: "+ Hex.toHexString(unwrapped));
        System.out.println("expected: "+ Hex.toHexString(unwrapped));
        Assert.assertArrayEquals(unwrapped, u);
    }
}
