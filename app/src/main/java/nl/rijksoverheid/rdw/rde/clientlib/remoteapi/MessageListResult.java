package nl.rijksoverheid.rdw.rde.clientlib.remoteapi;

public class MessageListResult
{
    private boolean error;
    private MessageInfoDto[] items;

    public MessageListResult()
    {
    }

    //Also parameters or an id

    public MessageListResult(final boolean error)
    {
        this.error = error;
    }

    public boolean isError()
    {
        return error;
    }

    public void setError(final boolean error)
    {
        this.error = error;
    }

    public MessageInfoDto[] getItems()
    {
        return items;
    }

    public void setItems(final MessageInfoDto[] items)
    {
        this.items = items;
    }
}

