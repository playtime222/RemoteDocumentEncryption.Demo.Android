package nl.rijksoverheid.rdw.rde.clientlib.messaging;

public class MessageFile {
    private final String filename;
    private final byte[] content;

    public MessageFile(String filename, byte[] content) {
        this.filename = filename;
        this.content = content;
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getContent() {
        return content;
    }
}
