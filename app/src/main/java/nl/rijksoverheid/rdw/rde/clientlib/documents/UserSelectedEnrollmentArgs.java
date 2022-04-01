package nl.rijksoverheid.rdw.rde.clientlib.documents;


/**
 * User choices
 * */
//TODO validation
public class UserSelectedEnrollmentArgs
{
    private int shortFileId;
    private int fileByteCount;
    private String displayName;

    public UserSelectedEnrollmentArgs(final int shortFileId, final int fileByteCount)
    {
        this.shortFileId = shortFileId;
        this.fileByteCount = fileByteCount;
    }

    public UserSelectedEnrollmentArgs()
    {
    }

    public int getShortFileId()
    {
        return shortFileId;
    }

    public void setShortFileId(final int shortFileId)
    {
        this.shortFileId = shortFileId;
    }

    public int getFileByteCount()
    {
        return fileByteCount;
    }

    public void setFileByteCount(final int fileByteCount)
    {
        this.fileByteCount = fileByteCount;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(final String displayName)
    {
        this.displayName = displayName;
    }
}
