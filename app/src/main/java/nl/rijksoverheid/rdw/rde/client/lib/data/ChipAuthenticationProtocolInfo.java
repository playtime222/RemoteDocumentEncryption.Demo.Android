package nl.rijksoverheid.rdw.rde.client.lib.data;

public class ChipAuthenticationProtocolInfo {
    private String protocolOid;
    private ChipAuthenticationPublicKeyInfo publicKeyInfo;

    public String getProtocolOid() {
        return protocolOid;
    }

    public void setProtocolOid(String protocolOid) {
        this.protocolOid = protocolOid;
    }

    public ChipAuthenticationPublicKeyInfo getPublicKeyInfo() {
        return publicKeyInfo;
    }

    public void setPublicKeyInfo(ChipAuthenticationPublicKeyInfo publicKeyInfo) {
        this.publicKeyInfo = publicKeyInfo;
    }
}
