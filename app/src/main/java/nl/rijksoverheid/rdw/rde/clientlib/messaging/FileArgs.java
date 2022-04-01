package nl.rijksoverheid.rdw.rde.clientlib.messaging;

//TODO validation
public class FileArgs
{
    private String name;
    private byte[] content;

    public FileArgs()
    {
    }

    public FileArgs(final String name, final byte[] content)
    {
        this.name = name;
        this.content = content;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public byte[] getContent()
    {
        return content;
    }

    public void setContent(final byte[] content)
    {
        this.content = content;
    }

    //TODO MimeType?



}


