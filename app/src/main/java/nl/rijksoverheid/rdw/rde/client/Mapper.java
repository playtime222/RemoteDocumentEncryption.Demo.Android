package nl.rijksoverheid.rdw.rde.client;


import nl.rijksoverheid.rdw.rde.clientlib.remoteapi.*;

public class Mapper
{
    public static MessageMetadata map(final MessageInfoDto value)
    {
        if (value == null)
            throw new IllegalArgumentException();

        final var result = new MessageMetadata();
        result.setId(value.getId());
        result.setShortNote(value.getNote());
        result.setWhenSent(value.getWhenSent());
        result.setWhoFrom(value.getFrom());
        result.setUrl(value.getUrl());
        return result;
    }
}
