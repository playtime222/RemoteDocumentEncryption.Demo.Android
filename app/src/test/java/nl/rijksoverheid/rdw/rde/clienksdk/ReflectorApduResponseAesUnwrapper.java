package nl.rijksoverheid.rdw.rde.clienksdk;

import net.sf.scuba.smartcards.ResponseAPDU;

import org.bouncycastle.util.encoders.Hex;
import org.jmrtd.protocol.AESSecureMessagingWrapper;
import org.jmrtd.protocol.SecureMessagingWrapper;

import java.lang.reflect.InvocationTargetException;
import java.security.GeneralSecurityException;
import java.util.Objects;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ReflectorApduResponseAesUnwrapper
{
    private static final int ONLY_USED_FOR_COMMAND_WRAPPING_MAX_TRANCEIVE_LENGTH = 1;

    public byte[] unwrap(SecretKey ksEnc, SecretKey ksMac, boolean shouldCheckMAC, long ssc, byte[] response)
            throws GeneralSecurityException, NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        var m = SecureMessagingWrapper.class.getMethod("unwrap", ResponseAPDU.class);
        m.setAccessible(true);
        var wrapper = new AESSecureMessagingWrapper(ksEnc, ksMac, ONLY_USED_FOR_COMMAND_WRAPPING_MAX_TRANCEIVE_LENGTH, shouldCheckMAC, ssc);
        dump(wrapper);
        var result = (ResponseAPDU)m.invoke(wrapper, new ResponseAPDU(response));

        dump(wrapper);
        return Objects.requireNonNull(result).getBytes();
    }

//    public static void dump(final SecureMessagingWrapper wrapper)
//    {
//        if (wrapper instanceof AESSecureMessagingWrapper)
//        {
//            dump((AESSecureMessagingWrapper) wrapper);
//            return;
//        }
//
//        dump((DESedeSecureMessagingWrapper)wrapper);
//    }

    private static void dump(final AESSecureMessagingWrapper wrapper)
    {
        final var encryptionKey = (SecretKeySpec) wrapper.getEncryptionKey();
        final var macKey = wrapper.getMACKey().getEncoded();
        final var counter = wrapper.getEncodedSendSequenceCounter(); //IV
        final var padding = wrapper.getPadLength();
        final var ksEnc = encryptionKey.getEncoded();
        System.out.println("Wrapper Type : AESSecureMessagingWrapper");
        System.out.println("KsEnc   : " + ksEnc.length + ", " + Hex.toHexString(ksEnc));
        System.out.println("KsMac   : " + macKey.length + ", " + Hex.toHexString(macKey));
        System.out.println("Counter : " + Hex.toHexString(counter));
        System.out.println("Padding : " + padding);
    }

//    private static void dump(final DESedeSecureMessagingWrapper wrapper)
//    {
//        final var encryptionKey = (SecretKeySpec) wrapper.getEncryptionKey();
//        final var macKey = wrapper.getMACKey().getEncoded();
//        final var counter = wrapper.getEncodedSendSequenceCounter(); //IV
//        final var padding = wrapper.getPadLength();
//        final var ksEnc = encryptionKey.getEncoded();
//        System.out.println("Wrapper Type : DESedeSecureMessagingWrapper");
//        System.out.println("KsEnc   : " + ksEnc.length + ", " + Hex.toHexString(ksEnc));
//        System.out.println("KsMac   : " + macKey.length + ", " + Hex.toHexString(macKey));
//        System.out.println("Counter : " + Hex.toHexString(counter));
//        System.out.println("Padding : " + padding);
//    }
}