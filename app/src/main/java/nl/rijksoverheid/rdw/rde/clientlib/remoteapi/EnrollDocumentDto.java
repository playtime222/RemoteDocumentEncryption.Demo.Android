package nl.rijksoverheid.rdw.rde.clientlib.remoteapi;

public class EnrollDocumentDto {

    private String enrollmentId;

    //TODO Optional mnemonic only
    private String displayName;

    private byte[] dataGroup14;
    private byte[] documentSecurityObject;

    private byte[] pcdPrivateKey; //For RDE

    private byte[] pcdPublicKey; //For decrypt
    private byte[] encryptedCommand; //For decrypt

    //TODO allow multiple
    private int shortFileId; //Field on document which is the target of the RB call.
    private byte[] fileContents; //Field on document which is the target of the RB call.
    private int fileReadLength;
    private byte[] rbResponse;

    public byte[] getPcdPublicKey()
    {
        return pcdPublicKey;
    }

    public void setPcdPublicKey(final byte[] pcdPublicKey)
    {
        this.pcdPublicKey = pcdPublicKey;
    }

    public String getEnrollmentId()
    {
        return enrollmentId;
    }

    public void setEnrollmentId(final String enrollmentId)
    {
        this.enrollmentId = enrollmentId;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(final String displayName)
    {
        this.displayName = displayName;
    }

    public byte[] getDataGroup14()
    {
        return dataGroup14;
    }

    public void setDataGroup14(final byte[] dataGroup14)
    {
        this.dataGroup14 = dataGroup14;
    }

    public byte[] getDocumentSecurityObject()
    {
        return documentSecurityObject;
    }

    public void setDocumentSecurityObject(final byte[] documentSecurityObject)
    {
        this.documentSecurityObject = documentSecurityObject;
    }

    public byte[] getPcdPrivateKey()
    {
        return pcdPrivateKey;
    }

    public void setPcdPrivateKey(final byte[] pcdPrivateKey)
    {
        this.pcdPrivateKey = pcdPrivateKey;
    }

    public byte[] getEncryptedCommand()
    {
        return encryptedCommand;
    }

    public void setEncryptedCommand(final byte[] encryptedCommand)
    {
        this.encryptedCommand = encryptedCommand;
    }

    public int getShortFileId()
    {
        return shortFileId;
    }

    public void setShortFileId(final int shortFileId)
    {
        this.shortFileId = shortFileId;
    }

    public byte[] getFileContents()
    {
        return fileContents;
    }

    public void setFileContents(final byte[] fileContents)
    {
        this.fileContents = fileContents;
    }

    public int getFileReadLength()
    {
        return fileReadLength;
    }

    public void setFileReadLength(final int fileReadLength)
    {
        this.fileReadLength = fileReadLength;
    }

    public byte[] getRbResponse()
    {
        return rbResponse;
    }

    public void setRbResponse(final byte[] rbResponse)
    {
        this.rbResponse = rbResponse;
    }
}