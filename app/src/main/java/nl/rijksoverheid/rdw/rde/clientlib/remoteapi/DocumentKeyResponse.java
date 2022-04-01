package nl.rijksoverheid.rdw.rde.clientlib.remoteapi;

public class DocumentKeyResponse {
    private String outerSecretKeyBase64;

    public String getOuterSecretKeyBase64() {
        return outerSecretKeyBase64;
    }

    public void setOuterSecretKeyBase64(String outerSecretKeyBase64) {
        this.outerSecretKeyBase64 = outerSecretKeyBase64;
    }
}
