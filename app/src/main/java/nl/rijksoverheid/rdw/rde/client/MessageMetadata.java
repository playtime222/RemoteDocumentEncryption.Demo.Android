package nl.rijksoverheid.rdw.rde.client;

//TODO validate
public class MessageMetadata
{
    private long Id;
    private String whoFrom;
    private String whenSent; //ISO sortable datetime
    private String shortNote;
    private String url;

    public String getUrl()
    {
        return url;
    }

    public void setUrl(final String url)
    {
        this.url = url;
    }

    public MessageMetadata()
    {
    }

    public MessageMetadata(final long id, final String whoFrom, final String whenSent, final String shortNote)
    {
        Id = id;
        this.whoFrom = whoFrom;
        this.whenSent = whenSent;
        this.shortNote = shortNote;
    }

    public long getId()
    {
        return Id;
    }

    public void setId(final long id)
    {
        Id = id;
    }

    public String getWhoFrom()
    {
        return whoFrom;
    }

    public void setWhoFrom(final String whoFrom)
    {
        this.whoFrom = whoFrom;
    }

    public String getWhenSent()
    {
        return whenSent;
    }

    public void setWhenSent(final String whenSent)
    {
        this.whenSent = whenSent;
    }

    public String getShortNote()
    {
        return shortNote;
    }

    public void setShortNote(final String shortNote)
    {
        this.shortNote = shortNote;
    }
}



