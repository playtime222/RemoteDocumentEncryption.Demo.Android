package nl.rijksoverheid.rdw.rde.client;

import java.io.IOException;
import java.util.Base64;

import nl.rijksoverheid.rdw.rde.client.lib.data.ChipAuthenticationProtocolInfo;
import nl.rijksoverheid.rdw.rde.client.lib.data.ChipAuthenticationPublicKeyInfo;
import nl.rijksoverheid.rdw.rde.client.lib.data.DocumentEnrolmentRequestArgs;
import nl.rijksoverheid.rdw.rde.client.lib.data.ReceivedMessageListItem;
import nl.rijksoverheid.rdw.rde.documents.*;
import nl.rijksoverheid.rdw.rde.mrtdfiles.*;

public class Mapper
{
    public static MessageMetadata map(final ReceivedMessageListItem value)
    {
        if (value == null)
            throw new IllegalArgumentException();

        final var result = new MessageMetadata();
        result.setId(value.getId());
        result.setShortNote(value.getNote());
        result.setWhenSent(value.getWhenSent());
        result.setWhoFrom(value.getSenderEmail());
        return result;
    }

    public static DocumentEnrolmentRequestArgs map(RdeDocumentEnrollmentInfo value) throws IOException {
        if (value == null)
            throw new IllegalArgumentException();

        final var result = new DocumentEnrolmentRequestArgs();
        result.setDisplayName(value.getDisplayName());
        result.setDataGroup14Base64(Base64.getUrlEncoder().encodeToString(value.getDataGroup14()));

        var dg14 = new Dg14Reader(value.getDataGroup14());

        var capki = new ChipAuthenticationPublicKeyInfo();
        capki.setPublicKeyBase64(Base64.getUrlEncoder().encodeToString(dg14.getCaSessionInfo().getCaPublicKeyInfo().getSubjectPublicKey().getEncoded()));

        var capi = new ChipAuthenticationProtocolInfo();
        capi.setProtocolOid(dg14.getCaSessionInfo().getCaInfo().getObjectIdentifier());
        capi.setPublicKeyInfo(capki);

        result.setChipAuthenticationProtocolInfo(capi);
        result.setFileId(value.getShortFileId());
        result.setFileContentsBase64(Base64.getUrlEncoder().encodeToString(value.getFileContents()));
        result.setFileReadLength(value.getFileReadLength());

        return result;
    }
}
