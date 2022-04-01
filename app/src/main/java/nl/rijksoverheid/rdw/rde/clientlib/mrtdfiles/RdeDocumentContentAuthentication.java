package nl.rijksoverheid.rdw.rde.clientlib.mrtdfiles;

import org.jmrtd.Util;
import org.jmrtd.lds.SODFile;

import java.security.*;
import java.util.Arrays;

public class RdeDocumentContentAuthentication
{
    public boolean isAuthentic(final SODFile sodFile, final int dataGroupTag, final byte[] dataGroupContent) throws NoSuchAlgorithmException
    {
        if (sodFile == null)
            throw new IllegalArgumentException();
        if (dataGroupContent == null)
            throw new IllegalArgumentException();

        try
        {
            var messageDigest = MessageDigest.getInstance(sodFile.getDigestAlgorithm());
            var fileDigest = messageDigest.digest(dataGroupContent);
            //var dataGroupNumber = LDSFileUtil.lookupDataGroupNumberByTag(dataGroupTag);
            var expectedDigest = sodFile.getDataGroupHashes().get(dataGroupTag);
            return Arrays.equals(fileDigest, expectedDigest);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    public boolean isAuthentic(final SODFile sodFile) throws GeneralSecurityException
    {
        if (sodFile == null)
            throw new IllegalArgumentException();

        Signature signature;
        try
        {
            var dsCertificate = sodFile.getDocSigningCertificate();
            signature = Util.getSignature(sodFile.getDocSigningCertificate().getSigAlgName());
            signature.initVerify(dsCertificate.getPublicKey());
        }
        catch (SignatureException signatureException)
        {
            signatureException.printStackTrace();
            return false;
        }

        // N.B.: sodFile.getEContent() - computes the digest of eContent and matches the value with the digest message stored in the signed attributes
        signature.update(sodFile.getEContent());
        return signature.verify(sodFile.getEncryptedDigest());
    }

    public void throwIfNotAuthentic(final SODFile sodFile) throws GeneralSecurityException
    {
        if (sodFile == null)
            throw new IllegalArgumentException();

        if (!isAuthentic(sodFile))
            throw new IllegalStateException();
    }

    public void throwIfNotAuthentic(final SODFile sodFile, final int dataGroupTag, final byte[] dataGroupContent) throws NoSuchAlgorithmException
    {
        if (sodFile == null)
            throw new IllegalArgumentException();

        if (dataGroupContent == null)
            throw new IllegalArgumentException();

        if (!isAuthentic(sodFile, dataGroupTag, dataGroupContent))
            throw new IllegalStateException();
    }
}
