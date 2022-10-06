package nl.rijksoverheid.rdw.rde.client.lib.data;

import com.google.type.DateTime;

public class ReceivedMessageListItem {
    private long id;
    private String documentDisplayName;
    private String whenSent;
    private String senderEmail;
    private String note;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDocumentDisplayName() {
        return documentDisplayName;
    }

    public void setDocumentDisplayName(String documentDisplayName) {
        this.documentDisplayName = documentDisplayName;
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
}
