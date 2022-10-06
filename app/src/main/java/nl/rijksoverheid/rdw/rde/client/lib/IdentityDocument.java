package nl.rijksoverheid.rdw.rde.client.lib;

public class IdentityDocument {
    private IdentityDocumentService[] services;

    public IdentityDocumentService[] getServices() {
        return services;
    }

    public void setServices(IdentityDocumentService[] services) {
        this.services = services;
    }

//    private static final String enrollmentUrl = "https://192.168.178.12:45455/api/mobiledevices/documents";
//    //Urls in the list should are fully qualified.
//    private static final String messageListUrl = "https://192.168.178.12:45455/api/mobiledevices/messages/received";
//    private static final String messageUrl = "https://192.168.178.12:45455/api/mobiledevices/messages/received/";
}
