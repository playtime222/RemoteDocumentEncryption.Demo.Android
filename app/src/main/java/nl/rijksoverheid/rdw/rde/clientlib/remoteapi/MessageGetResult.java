package nl.rijksoverheid.rdw.rde.clientlib.remoteapi;

public class MessageGetResult
{
    private boolean error;
    private long id;

    private byte[] content;

    public MessageGetResult(final boolean error)
    {
        this.error = error;
    }

    public MessageGetResult()
    {
    }

    public boolean isError()
    {
        return error;
    }

    public void setError(final boolean error)
    {
        this.error = error;
    }

    public long getId()
    {
        return id;
    }

    public void setId(final long id)
    {
        this.id = id;
    }

    public byte[] getContent()
    {
        return content;
    }

    public void setContent(final byte[] content)
    {
        this.content = content;
    }
}


