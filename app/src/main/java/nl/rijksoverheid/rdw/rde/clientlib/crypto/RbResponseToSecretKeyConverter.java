package nl.rijksoverheid.rdw.rde.clientlib.crypto;

import net.sf.scuba.util.Hex;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class RbResponseToSecretKeyConverter
{
    public static final String SALT = "12345678";
    public static final String CONVERTER_ALGORITHM = "PBKDF2WithHmacSHA256";

    public byte[] convert(final byte[] response) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        if (response == null || response.length == 0)
            throw new IllegalArgumentException();

        final var password = getSHA256Hex(response).toCharArray();
        final var salt = SALT.getBytes();
        final var spec = new PBEKeySpec(password, salt, 2, 256); //TODO iterations and keyLength as settings?
        final var factory = SecretKeyFactory.getInstance(CONVERTER_ALGORITHM);
        final var result = factory.generateSecret(spec);
        return result.getEncoded();
    }

    private static String getSHA256Hex(final byte[] buffer) throws NoSuchAlgorithmException
    {
        final var messageDigest = MessageDigest.getInstance("SHA-256");
        return Hex.toHexString(messageDigest.digest(buffer));
    }
}
