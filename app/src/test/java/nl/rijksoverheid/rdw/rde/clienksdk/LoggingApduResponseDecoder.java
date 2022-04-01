//package nl.rijksoverheid.rdw.rde.clienksdk;
//
//import net.sf.scuba.smartcards.ResponseAPDU;
//import net.sf.scuba.util.Hex;
//
//import org.jmrtd.Util;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.security.GeneralSecurityException;
//import java.util.Arrays;
//
//import javax.crypto.Cipher;
//import javax.crypto.Mac;
//import javax.crypto.SecretKey;
//import javax.crypto.spec.IvParameterSpec;
//import javax.crypto.spec.SecretKeySpec;
//
//public class LoggingApduResponseDecoder
//{
//    //private int maxTranceiveLength;
//
//    private final boolean shouldCheckMAC;
//
//    //private long ssc;
//
//    private final transient Cipher cipher;
//    private final transient Mac mac;
//
//    private final SecretKey ksEnc;
//    private SecretKey ksMac;
//
//    //private transient Cipher sscIVCipher;
//    //private Cipher cipher;
//    //private Key ksEnc;
//    private final long ssc;
//    private final IvParameterSpec iv; // = new IvParameterSpec(new byte[16]);
//
//    public LoggingApduResponseDecoder(byte[] ksEnc, byte[] ksMac, /*int maxTranceiveLength,*/ boolean shouldCheckMAC, long ssc)
//            throws GeneralSecurityException
//    {
//        //this.maxTranceiveLength = maxTranceiveLength;
//        this.shouldCheckMAC = shouldCheckMAC;
//        this.ksEnc = new SecretKeySpec(ksEnc, "AES");
//        //this.ksMac = new SecretKeySpec(ksMac, "AESCMAC");
//        this.ssc = ssc
//        //        +1
//        ;
//        this.iv = new IvParameterSpec(getEncodedSendSequenceCounter());
//        this.cipher = Util.getCipher("AES/CBC/NoPadding");
//        this.mac = Util.getMac("AESCMAC");
//        //this.sscIVCipher = Util.getCipher("AES/ECB/NoPadding", Cipher.ENCRYPT_MODE, ksEnc);
//    }
//
//    //Taken from private function JMRTD
//    public ResponseAPDU unwrap(byte[] rapdu) throws GeneralSecurityException, IOException
//    {
//        if (rapdu == null || rapdu.length < 2)
//        {
//            throw new IllegalArgumentException("Invalid response APDU");
//        }
//
//        cipher.init(Cipher.DECRYPT_MODE, ksEnc, iv);
//
//        byte[] data = new byte[0];
//        byte[] cc = null;
//        short sw = 0;
//        var inputStream = new DataInputStream(new ByteArrayInputStream(rapdu));
//        try
//        {
//            boolean isFinished = false;
//            while (!isFinished)
//            {
//                int tag = inputStream.readByte();
//                System.out.println("Read byte: " + Hex.toHexString(new byte[]{(byte) tag}));
//                switch (tag)
//                {
//                    case (byte) 0x87:
//                        data = readDO87(inputStream, false);
//                        break;
//                    case (byte) 0x85:
//                        data = readDO87(inputStream, true);
//                        break;
//                    case (byte) 0x99:
//                        sw = readDO99(inputStream);
//                        break;
//                    case (byte) 0x8E:
//                        cc = readDO8E(inputStream);
//                        isFinished = true;
//                        break;
//                    default:
//                        throw new IllegalStateException("Unexpected tag " + Integer.toHexString(tag));
//                        //LOGGER.warning("Unexpected tag " + Integer.toHexString(tag));
//                }
//            }
//        } finally
//        {
//            inputStream.close();
//        }
//        if (shouldCheckMAC() && !checkMac(rapdu, cc))
//        {
//            throw new IllegalStateException("Invalid MAC");
//        }
//        var bOut = new ByteArrayOutputStream();
//        bOut.write(data, 0, data.length);
//        bOut.write((sw & 0xFF00) >> 8);
//        bOut.write(sw & 0x00FF);
//        return new ResponseAPDU(bOut.toByteArray());
//    }
//
//    public boolean shouldCheckMAC()
//    {
//        return shouldCheckMAC;
//    }
//
//    /**
//     * Checks the MAC.
//     *
//     * @param rapdu the bytes of the response APDU, including the {@code 0x8E} tag, the length of the MAC, the MAC itself, and the status word
//     * @param cc    the MAC sent by the other party
//     * @return whether the computed MAC is identical
//     * @throws GeneralSecurityException on security related error
//     */
//    protected boolean checkMac(byte[] rapdu, byte[] cc) throws GeneralSecurityException
//    {
//        try
//        {
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
//            dataOutputStream.write(getEncodedSendSequenceCounter());
//            byte[] paddedData = Util.pad(rapdu, 0, rapdu.length - 2 - 8 - 2, getPadLength());
//            dataOutputStream.write(paddedData, 0, paddedData.length);
//            dataOutputStream.flush();
//            dataOutputStream.close();
//            mac.init(ksMac);
//            byte[] cc2 = mac.doFinal(byteArrayOutputStream.toByteArray());
//
//            if (cc2.length > 8 && cc.length == 8)
//            {
//                byte[] newCC2 = new byte[8];
//                System.arraycopy(cc2, 0, newCC2, 0, newCC2.length);
//                cc2 = newCC2;
//            }
//
//            return Arrays.equals(cc, cc2);
//        } catch (IOException ioe)
//        {
//            //LOGGER.log(Level.WARNING, "Exception checking MAC", ioe);
//            return false;
//        }
//    }
//
//
//    /**
//     * Returns the type of secure messaging wrapper (in this case {@code "AES"}).
//     *
//     * @return the type of secure messaging wrapper
//     */
//    public String getType()
//    {
//        return "AES";
//    }
//
//    /**
//     * Returns the length (in bytes) to use for padding.
//     * For AES this is 16.
//     *
//     * @return the length to use for padding
//     */
//    //@Override
//    public int getPadLength()
//    {
//        return 16;
//    }
//
//    /**
//     * Returns the current value of the send sequence counter.
//     *
//     * @return the current value of the send sequence counter.
//     */
////    public long getSendSequenceCounter()
////    {
////        return ssc;
////    }
//
//    /**
//     * Returns the send sequence counter as bytes, making sure
//     * the 128 bit (16 byte) block-size is used.
//     *
//     * @return the send sequence counter as a 16 byte array
//     */
//    public byte[] getEncodedSendSequenceCounter()
//    {
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(16);
//        try
//        {
//            byteArrayOutputStream.write(0x00);
//            byteArrayOutputStream.write(0x00);
//            byteArrayOutputStream.write(0x00);
//            byteArrayOutputStream.write(0x00);
//            byteArrayOutputStream.write(0x00);
//            byteArrayOutputStream.write(0x00);
//            byteArrayOutputStream.write(0x00);
//            byteArrayOutputStream.write(0x00);
//
//            /* A long will take 8 bytes. */
//            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
//            dataOutputStream.writeLong(ssc);
//            dataOutputStream.close();
//            return byteArrayOutputStream.toByteArray();
//        } catch (IOException ioe)
//        {
//            /* Never happens. */
//            //LOGGER.log(Level.FINE, "Error writing to stream", ioe);
//        } finally
//        {
//            try
//            {
//                byteArrayOutputStream.close();
//            } catch (IOException ioe)
//            {
//                //LOGGER.log(Level.FINE, "Error closing stream", ioe);
//            }
//        }
//        return null;
//    }
//
//    /**
//     * Encodes the expected length value to a byte array for inclusion in wrapped APDUs.
//     * The result is a byte array of length 1 or 2.
//     *
//     * @param le a non-negative expected length
//     * @return a byte array with the encoded expected length
//     */
////    private byte[] encodeLe(int le)
////    {
////        if (0 <= le && le <= 256)
////        {
////            /* NOTE: Both 0x00 and 0x100 are mapped to 0x00. */
////            return new byte[]{(byte) le};
////        } else
////        {
////            return new byte[]{(byte) ((le & 0xFF00) >> 8), (byte) (le & 0xFF)};
////        }
////    }
//
//    /**
//     * Reads a data object.
//     * The {@code 0x87} tag has already been read.
//     *
//     * @param inputStream the stream to read from
//     * @param do85        whether to expect a {@code 0x85} (including an extra 1 length) data object.
//     * @return the bytes that were read
//     * @throws IOException              on error reading from the stream
//     * @throws GeneralSecurityException on error decrypting the data
//     */
//    private byte[] readDO87(DataInputStream inputStream, boolean do85) throws IOException, GeneralSecurityException
//    {
//        /* Read length... */
//        int length = 0;
//        int buf = inputStream.readUnsignedByte();
//        System.out.println("Read byte: " + Hex.toHexString(new byte[]{(byte) buf}));
//        if ((buf & 0x00000080) != 0x00000080)
//        {
//            /* Short form */
//            length = buf;
//            System.out.println("Short form length:" + length);
//            if (!do85)
//            {
//                buf = inputStream.readUnsignedByte(); /* should be 0x01... */
//                if (buf != 0x01)
//                {
//                    throw new IllegalStateException("DO'87 expected 0x01 marker, found " + Integer.toHexString(buf & 0xFF));
//                }
//            }
//        }
//        else
//        {
//            /* Long form */
//            int lengthBytesCount = buf & 0x0000007F;
//            System.out.println("Long form length of length:" + lengthBytesCount);
//            for (int i = 0; i < lengthBytesCount; i++)
//            {
//                length = (length << 8) | inputStream.readUnsignedByte();
//                System.out.println("Long form length:" + length);
//            }
//            if (!do85)
//            {
//                buf = inputStream.readUnsignedByte(); /* should be 0x01... */
//                if (buf != 0x01)
//                {
//                    throw new IllegalStateException("DO'87 expected 0x01 marker");
//                }
//            }
//        }
//
//
//        if (!do85)
//        {
//            System.out.println("Take off 1 for a do87");
//            length--; /* takes care of the extra 0x01 marker... */
//        }
//
//        System.out.println("Final length:" + length);
//
//        /* Read, decrypt, unpad the data... */
//        byte[] ciphertext = new byte[length];
//        inputStream.readFully(ciphertext);
//        System.out.println("Encrypted buffer :" + ciphertext.length + ", " + Hex.toHexString(ciphertext));
//
//        byte[] paddedData = cipher.doFinal(ciphertext);
//        System.out.println("Decrypted and padded:"  + paddedData.length + ", " + Hex.toHexString(paddedData));
//
//        //Padded with 0x00 after marker of (bytes[i] & 0xFF) != 0x80
//        byte[] unpad = Util.unpad(paddedData);
//        System.out.println("Decrypted:"  + unpad.length + ", " + Hex.toHexString(unpad));
//        return unpad;
//    }
//
//    /**
//     * Reads a data object.
//     * The {@code 0x99} tag has already been read.
//     *
//     * @param inputStream the stream to read from
//     * @return the status word
//     * @throws IOException on error reading from the stream
//     */
//    private short readDO99(DataInputStream inputStream) throws IOException
//    {
//        int length = inputStream.readUnsignedByte();
//        if (length != 2)
//        {
//            throw new IllegalStateException("DO'99 wrong length");
//        }
//        byte sw1 = inputStream.readByte();
//        byte sw2 = inputStream.readByte();
//        return (short) (((sw1 & 0x000000FF) << 8) | (sw2 & 0x000000FF));
//    }
//
//    /**
//     * Reads a data object.
//     * This assumes that the {@code 0x8E} tag has already been read.
//     *
//     * @param inputStream the stream to read from
//     * @return the bytes that were read
//     * @throws IOException on error
//     */
//    private byte[] readDO8E(DataInputStream inputStream) throws IOException
//    {
//        int length = inputStream.readUnsignedByte();
//        if (length != 8 && length != 16)
//        {
//            throw new IllegalStateException("DO'8E wrong length for MAC: " + length);
//        }
//        byte[] cc = new byte[length];
//        inputStream.readFully(cc);
//        return cc;
//    }
//}
