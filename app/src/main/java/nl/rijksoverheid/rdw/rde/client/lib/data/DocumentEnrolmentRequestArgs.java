package nl.rijksoverheid.rdw.rde.client.lib.data;

public class DocumentEnrolmentRequestArgs {
    private String displayName;

    /// <summary>
    /// Security descriptions
    /// </summary>
    private String dataGroup14Base64;

    /// <summary>
    /// AKA Ef.Sod
    /// </summary>
    //private String documentSecurityObjectBase64;

    //Extracted from DG14 for convenience.
    private ChipAuthenticationProtocolInfo chipAuthenticationProtocolInfo;

    //Extracted from Ef.Sod for convenience.
    //private DocumentSecurityObjectInfo documentSecurityObjectInfo;

    //TODO multiple
    private int fileId;
    private String fileContentsBase64;
    private int fileReadLength;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDataGroup14Base64() {
        return dataGroup14Base64;
    }

    public void setDataGroup14Base64(String dataGroup14Base64) {
        this.dataGroup14Base64 = dataGroup14Base64;
    }

    public ChipAuthenticationProtocolInfo getChipAuthenticationProtocolInfo() {
        return chipAuthenticationProtocolInfo;
    }

    public void setChipAuthenticationProtocolInfo(ChipAuthenticationProtocolInfo chipAuthenticationProtocolInfo) {
        this.chipAuthenticationProtocolInfo = chipAuthenticationProtocolInfo;
    }


    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public String getFileContentsBase64() {
        return fileContentsBase64;
    }

    public void setFileContentsBase64(String fileContentsBase64) {
        this.fileContentsBase64 = fileContentsBase64;
    }

    public int getFileReadLength() {
        return fileReadLength;
    }

    public void setFileReadLength(int fileReadLength) {
        this.fileReadLength = fileReadLength;
    }
}

