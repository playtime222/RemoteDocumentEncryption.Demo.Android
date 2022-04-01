package nl.rijksoverheid.rdw.rde.clientlib.mrtdfiles;

import org.jmrtd.lds.ChipAuthenticationInfo;
import org.jmrtd.lds.ChipAuthenticationPublicKeyInfo;
import org.jmrtd.lds.SecurityInfo;
import org.jmrtd.lds.icao.DG14File;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


//Proxy and adaptor
public class Dg14Reader
{
    private final DG14File dg14File;
    private List<ChipAuthenticationInfo> caInfoList;
    private List<ChipAuthenticationPublicKeyInfo> caPublicKeyList;
    private CaSessionInfo caSessionInfo;

    public Dg14Reader(final byte[] buffer) throws IOException {
        try(final var is = new ByteArrayInputStream(buffer))
        {
            dg14File = new DG14File(is);
        }
    }

    public Dg14Reader(final DG14File dg14File)
    {
        if (dg14File == null)
            throw new IllegalArgumentException();

        this.dg14File = dg14File;
    }

    public CaSessionInfo getCaSessionInfo()
    {
        if (caSessionInfo == null)
        {
            caInfoList = getChipAuthenticationInfos();
            caPublicKeyList = getChipAuthenticationPublicKeyInfos();
            caSessionInfo = findCaSessionInfo();
        }

        return caSessionInfo;
    }

    private CaSessionInfo findCaSessionInfo()
    {
        for (var pk : caPublicKeyList)
        {
            var chipAuthenticationInfo = getChipAuthenticationInfo(pk.getKeyId(), caInfoList);
            if (chipAuthenticationInfo == null)
            {
                continue;
            }
            return new CaSessionInfo(pk, chipAuthenticationInfo);
        }
        throw new IllegalStateException("Cannot find item.");
    }

    private ChipAuthenticationInfo getChipAuthenticationInfo(final BigInteger publicKeyId, final List<ChipAuthenticationInfo> chipAuthenticationInfos)
    {
        for (var i : chipAuthenticationInfos)
        {
            if (publicKeyId == null || publicKeyId.equals(i.getKeyId()))
            {
                return i;
            }
        }
        return null;
    }

    private List<ChipAuthenticationInfo> getChipAuthenticationInfos()
    {
        var result = new ArrayList<ChipAuthenticationInfo>();
        for (SecurityInfo securityInfo : dg14File.getSecurityInfos())
        {
            if (securityInfo instanceof ChipAuthenticationInfo)
                result.add((ChipAuthenticationInfo) securityInfo);
        }
        return result;
    }

    private List<ChipAuthenticationPublicKeyInfo> getChipAuthenticationPublicKeyInfos()
    {
        var result = new ArrayList<ChipAuthenticationPublicKeyInfo>();
        for (var i : dg14File.getSecurityInfos())
        {
            if (i instanceof ChipAuthenticationPublicKeyInfo)
                result.add((ChipAuthenticationPublicKeyInfo) i);
        }
        return result;
    }
}
