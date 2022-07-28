package nl.rijksoverheid.rdw.rde.client;

import org.jmrtd.protocol.AESSecureMessagingWrapper;
import org.jmrtd.protocol.DESedeSecureMessagingWrapper;
import org.jmrtd.protocol.SecureMessagingWrapper;

import javax.crypto.spec.SecretKeySpec;

public class SecureWrapperDebug {

    public static void dump(SecureMessagingWrapper wrapper) {
        if (wrapper instanceof AESSecureMessagingWrapper)
            dump((AESSecureMessagingWrapper) wrapper);
        else
            dump((DESedeSecureMessagingWrapper) wrapper);
    }

    private static void dump(final AESSecureMessagingWrapper wrapper) {
        final var encryptionKey = (SecretKeySpec) wrapper.getEncryptionKey();
        final var macKey = wrapper.getMACKey();
        final long counter = wrapper.getSendSequenceCounter();
        final var encodedCounter = wrapper.getEncodedSendSequenceCounter(); //IV
        final var padding = wrapper.getPadLength();
        final var ksEnc = encryptionKey.getEncoded();
        System.out.println("Wrapper Type : AESSecureMessagingWrapper");
        System.out.println("KsEnc   : " + encryptionKey.getAlgorithm() + " " + ksEnc.length * 8 + ", " + org.bouncycastle.util.encoders.Hex.toHexString(ksEnc));
        System.out.println("KsMac   : " + macKey.getAlgorithm() + " " + macKey.getEncoded().length * 8 + ", " + org.bouncycastle.util.encoders.Hex.toHexString(macKey.getEncoded()));
        System.out.println("Counter : " + counter);
        System.out.println("Encoded Counter : " + org.bouncycastle.util.encoders.Hex.toHexString(encodedCounter));
        System.out.println("Padding : " + padding);
    }

    private static void dump(final DESedeSecureMessagingWrapper wrapper) {
        final var encryptionKey = (SecretKeySpec) wrapper.getEncryptionKey();
        final var macKey = wrapper.getMACKey();
        final var counter = wrapper.getSendSequenceCounter();
        final var encodedCounter = wrapper.getEncodedSendSequenceCounter(); //IV
        final var padding = wrapper.getPadLength();
        final var ksEnc = encryptionKey.getEncoded();
        System.out.println("Wrapper Type : DESedeSecureMessagingWrapper");
        System.out.println("KsEnc   : " + encryptionKey.getAlgorithm() + " " + ksEnc.length * 8 + ", " + org.bouncycastle.util.encoders.Hex.toHexString(ksEnc));
        System.out.println("KsMac   : " + macKey.getAlgorithm() + " " + macKey.getEncoded().length * 8 + ", " + org.bouncycastle.util.encoders.Hex.toHexString(macKey.getEncoded()));
        System.out.println("Counter : " + counter);
        System.out.println("Encoded Counter : " + org.bouncycastle.util.encoders.Hex.toHexString(encodedCounter));
        System.out.println("Padding : " + padding);
    }
}
