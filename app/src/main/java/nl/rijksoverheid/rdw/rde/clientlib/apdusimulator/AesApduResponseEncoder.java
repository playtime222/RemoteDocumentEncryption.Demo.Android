package nl.rijksoverheid.rdw.rde.clientlib.apdusimulator;

import net.sf.scuba.util.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jmrtd.Util;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


/*
* This implementation is ONLY for simulating the RB call for RDE. It is not a general implementation.
* */
public class AesApduResponseEncoder
//PassportCrypto
{
    public static final String DO87_CIPHER = "AES/CBC/NoPadding";
    public static final String IV_CIPHER = "AES/ECB/NoPadding";
    public static final String MAC_ALGO = "AESCMAC";
    public static final String AES_KEY_SPEC_NAME = "AES";
    private static final int BLOCK_SIZE = 16; //Plain text block size cos AES and AESCMAC
    private static final byte SW1 = (byte)0x90;
    private static final byte SW2 = 0x00;

    private static final byte DATA_BLOCK_START_TAG = (byte) 0x87;
    private static final byte DATA_BLOCK_LENGTH_END_TAG = (byte) 0x01;

    public static final int MAC_LENGTH = 0x08;
    public static final int MAC_BLOCK_START_TAG = 0x8e;

    private static final byte[] RESPONSE_RESULT_BLOCK = new byte[]{(byte) 0x99, (byte) 0x02, SW1, SW2};

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final Cipher cipher;
    private final Mac mac;

    private static final byte[] ssc ={ 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,2}; //0 when CA session started, first command is 1, first response is 2.

    public AesApduResponseEncoder(final byte[] ksEnc, byte[] ksMac)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException
    {
        var provider = new BouncyCastleProvider();
        cipher = Cipher.getInstance(DO87_CIPHER, provider);

        var iv = getIv(ksEnc, provider);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(ksEnc, AES_KEY_SPEC_NAME), new IvParameterSpec(iv));

        mac = Mac.getInstance(MAC_ALGO, provider);
        mac.init(new SecretKeySpec(ksMac, MAC_ALGO));
    }

    private byte[] getIv(final byte[] ksEnc, final BouncyCastleProvider provider) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException
    {
        var sscIVCipher = Cipher.getInstance(IV_CIPHER, provider);
        sscIVCipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(ksEnc, AES_KEY_SPEC_NAME));
        return  sscIVCipher.doFinal(ssc);
    }

    public byte[] write(final byte[] response)
            throws BadPaddingException, IllegalBlockSizeException, IOException
    {
        System.out.println("Response:" + Hex.toHexString(response));

        writeDo87(response);
        writeDo99();
        writeMac();
        outputStream.write(new byte[]{SW1, SW2}); //Again...
        return outputStream.toByteArray();
    }

    private void writeMac() throws IOException
    {
        System.out.println("MAC this: " + Hex.toHexString(outputStream.toByteArray()));
        mac.update(ssc);
        var macValue = mac.doFinal(Util.pad(outputStream.toByteArray(), BLOCK_SIZE));
        outputStream.write(new byte[]{(byte) MAC_BLOCK_START_TAG, MAC_LENGTH});
        outputStream.write(macValue, 0, MAC_LENGTH);
    }

    private void writeDo99() throws IOException
    {
        outputStream.write(RESPONSE_RESULT_BLOCK);
    }

    private void writeDo87(final byte[] response)
            throws IOException, BadPaddingException, IllegalBlockSizeException
    {
        if (response.length == 0)
            return;

        var encodedData = getEncodedData(response);

        outputStream.write(DATA_BLOCK_START_TAG);
        outputStream.write(getEncodedDo87Size(encodedData.length));
        outputStream.write(encodedData);
    }

    //TODO make private. Only public for tests.
    public byte[] getEncodedDo87Size(final int paddedDo87Length)
    {
        final int MIN_LONG_FORM_SIZE = 0x80;

        final var actualLength = paddedDo87Length + 1; //Cos of the 0x01 tag
        //Short form
        if (actualLength < MIN_LONG_FORM_SIZE)
            return new byte[] { (byte) actualLength, DATA_BLOCK_LENGTH_END_TAG};

        //1 or 2 byte Long form
        final var lenOfLen = actualLength > 0xff ? 2 : 1;
        final var result = new byte[lenOfLen+2];
        result[0] = (byte) (MIN_LONG_FORM_SIZE + lenOfLen);
        var p = 1;

        for (var i = lenOfLen - 1; i >= 0; i--)
            result[p++] = (byte) ((actualLength >>> (i * 8)) & 0xff);

        result[p++] = DATA_BLOCK_LENGTH_END_TAG;
        return result;
    }


    private byte[] getEncodedData(final byte[] response)
            throws BadPaddingException, IllegalBlockSizeException, IOException
    {
        if (response.length == 0)
            return response;

        return cipher.doFinal( getAlignedPlainText(response));
    }

    private byte[] getAlignedPlainText(final byte[] buffer)
    {
        var paddedLength = getPaddedLength(buffer.length);

        if (paddedLength == buffer.length)
            return buffer;

        return Util.pad(buffer, paddedLength);
    }

    public int getPaddedLength(final int bufferSize)
    {
        return ((bufferSize + BLOCK_SIZE) / BLOCK_SIZE) * BLOCK_SIZE;
    }
}
