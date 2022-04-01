package nl.rijksoverheid.rdw.rde.clienksdk;

import java.security.PrivateKey;
import javax.crypto.SecretKey;

//Testing...
@Deprecated
public class StartSessionResult
{
    public StartSessionResult(final SecretKey aesKey, final PrivateKey pcdPrivateKey)
    {
        if (aesKey == null)
            throw new IllegalArgumentException();
        if (pcdPrivateKey == null)
            throw new IllegalArgumentException();

        this.aesKey = aesKey;
        this.pcdPrivateKey = pcdPrivateKey;
    }

    public final SecretKey aesKey;
    public final PrivateKey pcdPrivateKey;
}
