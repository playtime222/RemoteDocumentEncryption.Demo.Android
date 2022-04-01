package nl.rijksoverheid.rdw.rde.clientlib.mrtdfiles;

import org.jmrtd.lds.ChipAuthenticationInfo;
import org.jmrtd.lds.ChipAuthenticationPublicKeyInfo;

public class CaSessionInfo
{
    private final ChipAuthenticationPublicKeyInfo caPublicKeyInfo;
    private final ChipAuthenticationInfo caInfo;

    public CaSessionInfo(final ChipAuthenticationPublicKeyInfo caPublicKeyInfo, final ChipAuthenticationInfo caInfo)
    {
        if (caPublicKeyInfo == null)
            throw new IllegalArgumentException();
        if (caInfo == null)
            throw new IllegalArgumentException();

        this.caPublicKeyInfo = caPublicKeyInfo;
        this.caInfo = caInfo;
    }

    public ChipAuthenticationPublicKeyInfo getCaPublicKeyInfo()
    {
        return caPublicKeyInfo;
    }

    public ChipAuthenticationInfo getCaInfo()
    {
        return caInfo;
    }

    public void dump()
    {
        System.out.println("Dump CA session args >>>");
        System.out.println(getCaPublicKeyInfo().getKeyId());
        System.out.println(getCaInfo().getObjectIdentifier());
        System.out.println(getCaPublicKeyInfo().getObjectIdentifier());
        System.out.println(getCaPublicKeyInfo().getSubjectPublicKey());
        System.out.println("<<< Dump CA session args");
    }
}
