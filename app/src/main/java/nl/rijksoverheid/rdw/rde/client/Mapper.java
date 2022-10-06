package nl.rijksoverheid.rdw.rde.client;

import nl.rijksoverheid.rdw.rde.crypto.*;
import nl.rijksoverheid.rdw.rde.documents.*;
import nl.rijksoverheid.rdw.rde.remoteapi.*;
import nl.rijksoverheid.rdw.rde.messaging.*;
import nl.rijksoverheid.rdw.rde.messaging.zipV2.*;
import nl.rijksoverheid.rdw.rde.mrtdfiles.*;

//public class Mapper
//{
//    public static MessageMetadata map(final ReceivedMessageListItem value)
//    {
//        if (value == null)
//            throw new IllegalArgumentException();
//
//        final var result = new MessageMetadata();
//        result.setId(value.getId());
//        result.setShortNote(value.getNote());
//        result.setWhenSent(value.getWhenSent());
//        result.setWhoFrom(value.getSenderEmail());
//        //result.setDocumentDisplayName(value.getDocumentDisplayName());
//        return result;
//    }
//}
