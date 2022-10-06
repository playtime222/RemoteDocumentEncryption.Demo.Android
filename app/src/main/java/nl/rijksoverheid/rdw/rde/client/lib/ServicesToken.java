package nl.rijksoverheid.rdw.rde.client.lib;

//Contents of linking QR code
public class ServicesToken {
    private String authToken;
    private String identityUrl;

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getIdentityUrl() {
        return identityUrl;
    }

    public void setIdentityUrl(String identityUrl) {
        this.identityUrl = identityUrl;
    }
}
