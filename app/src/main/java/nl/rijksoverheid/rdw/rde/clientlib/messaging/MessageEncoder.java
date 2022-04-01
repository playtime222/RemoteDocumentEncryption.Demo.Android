package nl.rijksoverheid.rdw.rde.clientlib.messaging;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface MessageEncoder {

    byte[] encode(final MessageContentArgs messageArgs, final RdeSessionArgs messageCryptoArgs, final SecretKey secretKey)
            throws IOException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException;
}
