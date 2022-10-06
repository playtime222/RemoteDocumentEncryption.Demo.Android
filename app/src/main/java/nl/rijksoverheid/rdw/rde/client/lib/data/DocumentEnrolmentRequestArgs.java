package nl.rijksoverheid.rdw.rde.client.lib.data;

public class DocumentEnrolmentRequestArgs {
    String displayName;

    /// <summary>
    /// Security descriptions
    /// </summary>
    String dataGroup14Base64;

    /// <summary>
    /// AKA Ef.Sod
    /// </summary>
    String documentSecurityObjectBase64;

    //Extracted from DG14 for convenience.
    ChipAuthenticationProtocolInfo chipAuthenticationProtocolInfo;

    //Extracted from Ef.Sod for convenience.
    DocumentSecurityObjectInfo documentSecurityObjectInfo;

    //TODO multiple
    int fileId;
    String fileContentsBase64;
    int fileReadLength;
}

