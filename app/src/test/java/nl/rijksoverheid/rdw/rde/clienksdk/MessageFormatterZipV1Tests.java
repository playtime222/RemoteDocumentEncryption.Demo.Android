package nl.rijksoverheid.rdw.rde.clienksdk;

import net.sf.scuba.util.Hex;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import nl.rijksoverheid.rdw.rde.clientlib.messaging.*;
import nl.rijksoverheid.rdw.rde.clientlib.messaging.zipV2.ZipMessageEncoder;

public class MessageFormatterZipV1Tests {

    @Test
    public void dumpExampleMessage()
            throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException
    {
        var messageCryptoArgs = new RdeSessionArgs();
        messageCryptoArgs.setDocumentDisplayName("Enrolled MRTD display name");
        messageCryptoArgs.setCipher("AES/CBC/PKCS5Padding");
        messageCryptoArgs.setCaEncryptedCommand(new byte[0]);
        messageCryptoArgs.setPcdPublicKey(new byte[0]);
        messageCryptoArgs.setCaProtocolOid("oid...");
        var messageContentArgs = new MessageContentArgs();
        messageContentArgs.add(new FileArgs("a file", "some content".getBytes(StandardCharsets.UTF_8)));
        messageContentArgs.setUnencryptedNote("a note");

        final var key = new byte[32];
        var r = new Random();
        r.nextBytes(key);
        final var secretKey = new SecretKeySpec(key, "AES");

        var f = new ZipMessageEncoder();
        var result = f.encode(messageContentArgs, messageCryptoArgs, secretKey);
        System.out.println("SECRET KEY:" + Hex.toHexString(key));
        System.out.println("IV        :" + Hex.toHexString(messageCryptoArgs.getIv()));
        System.out.println("RESULT    :" + Hex.toHexString(result));

        System.out.println("SECRET KEY:" +  java.util.Base64.getEncoder().encodeToString(key));
        System.out.println("IV        :" + java.util.Base64.getEncoder().encodeToString(messageCryptoArgs.getIv()));
        System.out.println("RESULT    :" + java.util.Base64.getEncoder().encodeToString(result));
    }
}
