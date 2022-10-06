package nl.rijksoverheid.rdw.rde.client.lib.data;

public class ReceivedMessage {
    private long id;
    private String whenSent;
    private String senderEmail;
    private String note;
    private String contentBase64;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getWhenSent() {
        return whenSent;
    }

    public void setWhenSent(String whenSent) {
        this.whenSent = whenSent;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getContentBase64() {
        return contentBase64;
    }

    public void setContentBase64(String contentBase64) {
        this.contentBase64 = contentBase64;
    }
}
