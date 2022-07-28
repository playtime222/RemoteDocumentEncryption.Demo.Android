package nl.rijksoverheid.rdw.rde.client;

import org.jmrtd.BACKey;

public class StoredBacKey {

    public StoredBacKey(String docId, String dob, String expiry) {
        DocId = docId;
        Expiry = expiry;
        Dob = dob;
    }

    public String DocId;
    public String Expiry;
    public String Dob;

    public boolean isComplete()
    {
        return DocId != null && !DocId.isEmpty()
                && Expiry != null && !Expiry.isEmpty()
                && Dob != null && !Dob.isEmpty();
    }

    public BACKey toBACKey()
    {
        return new BACKey(DocId, Dob, Expiry);
    }
}
