package nl.rijksoverheid.rdw.rde.client.lib.data;

public class ChipAuthenticationPublicKeyInfo {
    //EC or ECDH. ASN.1 DER Format
    private String publicKeyBase64;

    public String getPublicKeyBase64() {
        return publicKeyBase64;
    }

    public void setPublicKeyBase64(String publicKeyBase64) {
        this.publicKeyBase64 = publicKeyBase64;
    }
}
