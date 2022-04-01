package nl.rijksoverheid.rdw.rde.clientlib.remoteapi;

public class MessageInfoDto
{
    private long id;
    private String whenSent;
    private String from;
    private String document;
    private String note;
    private String url;

    public MessageInfoDto() {
    }

    public long getId()
    {
        return id;
    }

    public void setId(final long id)
    {
        this.id = id;
    }

    public String getWhenSent()
    {
        return whenSent;
    }

    public void setWhenSent(final String whenSent)
    {
        this.whenSent = whenSent;
    }

    public String getFrom()
    {
        return from;
    }

    public void setFrom(final String from)
    {
        this.from = from;
    }

    public String getDocument()
    {
        return document;
    }

    public void setDocument(final String to)
    {
        this.document = to;
    }

    public String getNote()
    {
        return note;
    }

    public void setNote(final String note)
    {
        this.note = note;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(final String url)
    {
        this.url = url;
    }
}
