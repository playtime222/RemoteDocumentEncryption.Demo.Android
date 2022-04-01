package nl.rijksoverheid.rdw.rde.clienksdk;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.jmrtd.Util;
import org.junit.Assert;
import org.junit.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

//        // calculate and write mac
//        initMac(Signature.MODE_SIGN);
//        updateMac(ssc, (short)0, (short)ssc.length);
//        createMacFinal(apdu,
//                (short) 0,
//                apdu_p,
//                apdu,
//                (short)(apdu_p+2));
public class MacTests
{
    @Test
    public void Calc1() throws NoSuchAlgorithmException, InvalidKeyException
    {
        macAlgo("9C3B7B89BB784992",
                Hex.decode("8711016D9F6F6FDA79FF285C2C1D3AEA1FFD0399029000"),
                Hex.decode("ADCBA368FD14A836908252EF76D09BAD2766C5FFB2FE7857F468676FC4B293E0"),
                (byte) 2);
    }

    @Test
    public void Calc2() throws NoSuchAlgorithmException, InvalidKeyException
    {
        macAlgo("7048FC7718CE63EA",
                Hex.decode("8711014B5C5A3C53ADC759BD71A9C0E0D034FB99029000"),
                Hex.decode("DDF1D6A72D17D96F44DE970FE952F69E54D292666EC8EF31BF7F6C6866C84A5B"),
                (byte) 2);
    }

    private void macAlgo(final String expectedString, final byte[] content, final byte[] ksMac, final byte ssc) throws NoSuchAlgorithmException, InvalidKeyException
    {
        var encodedSsc = new byte[16];
        encodedSsc[15]= ssc;

        var macInput = Util.pad(content, 16);

        var mac = Mac.getInstance("AESCMAC", new BouncyCastleProvider());
        mac.init(new SecretKeySpec(ksMac, "AESCMAC"));
        mac.update(encodedSsc);
        mac.update(macInput);
        var full = mac.doFinal();

        var actual = Arrays.copyOf(full, 8);

        System.out.println("Full    : "+ Hex.toHexString(full));
        System.out.println("Actual  : "+ Hex.toHexString(actual));
        System.out.println("Expected: "+ expectedString);

        Assert.assertArrayEquals(Hex.decode(expectedString), actual);
    }
}
