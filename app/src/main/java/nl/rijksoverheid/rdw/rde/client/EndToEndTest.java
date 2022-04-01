//package nl.rijksoverheid.rdw.rde.client;
//
//import android.nfc.Tag;
//
//import net.sf.scuba.smartcards.CardServiceException;
//import net.sf.scuba.util.Hex;
//
//import org.jmrtd.BACKey;
//
//import java.io.IOException;
//import java.security.GeneralSecurityException;
//import java.util.Arrays;
//
//import nl.rijksoverheid.rdw.rde.clientsdk.documents.RdeDocumentEnrollmentInfo;
//import nl.rijksoverheid.rdw.rde.messaging.MessageCryptoArgs;
//
//public class EndToEndTest
//{
//    public static void Test(final BACKey bacKey, final Tag tag, final RdeDocumentEnrollmentInfo enrollmentArgs)
//            throws CardServiceException, GeneralSecurityException, IOException
//    {
//        if (bacKey == null)
//            throw new IllegalArgumentException();
//        if (tag == null)
//            throw new IllegalArgumentException();
//        if (enrollmentArgs == null)
//            throw new IllegalArgumentException();
//
//
//
//        //Remove this test of the simulated response
//        final var mca = new MessageCryptoArgs();
//        mca.setPcdPublicKey(enrollmentArgs.getPcdPrivateKey());
//        mca.setCaEncryptedCommand(enrollmentArgs.getEncryptedCommand());
////        mca.setFid(enrollmentArgs.getFid());
////        mca.setFidByteCount(enrollmentArgs.getFcont().length);
//
//        //Copied from original enrollment call
//        final var cardResponse = enrollmentArgs.getRbResponse();
//        System.out.println("EXPECTED RESPONSE: " + enrollmentArgs.getRbResponse().length + " bytes, " + Hex.toHexString(enrollmentArgs.getRbResponse()));
//
////        var secondCardResponse = getRbResponseDecrypt(bacKey, tag, mca);
////        System.out.println("DECRYPT RESPONSE: " + secondCardResponse.length + " bytes, " + Extras.toBas64String(secondCardResponse));
////
////        if (Arrays.equals(secondCardResponse, cardResponse))
////            System.out.println("Decrypt response MATCH!");
////        else
////            throw new IllegalStateException("Decrypt response does not match enrollment response.");
//
//        //>>>> Start of remote encryption
//
//        final var simulatedResponse =  new TestRdeDocumentSimulator().getSimulatedAPDUResponse(enrollmentArgs);
//
//        System.out.println("SIMULATED RESPONSE: " + simulatedResponse.length + " bytes, "+ Hex.toHexString(simulatedResponse));
//
//        if (!Arrays.equals(cardResponse, simulatedResponse))
//            throw new IllegalStateException("Simulated response does not match.");
//    }
//
////    public static byte[] getBytes(final RdeDocumentEnrollmentInfo enrollmentArgs) throws IOException, GeneralSecurityException, CardServiceException
////    {
////        var is = new ByteArrayInputStream(enrollmentArgs.getDataGroup14());
////        var dg14 = new Dg14Adaptor(new DG14File(is));
////        var keyFactory = KeyFactory.getInstance("EC");
////        var keySpec = new PKCS8EncodedKeySpec(enrollmentArgs.getPcdPrivateKey());
////        var privateKey = keyFactory.generatePrivate(keySpec);
////
////        var simulatedResponse = new RdeDocumentSimulator().getSimulatedAPDUResponse(
////                dg14.getCaSessionInfo().getCaInfo().getObjectIdentifier(),
////                dg14.getCaSessionInfo().getCaPublicKeyInfo().getSubjectPublicKey(),
////                privateKey,
////                enrollmentArgs.getFileContents(),
////                enrollmentArgs.getActualFileReadLength()
////        );
////
////        return simulatedResponse;
////    }
//
////    private byte[] getRbResponseDecrypt(final BACKey bacKey, final Tag tag, final MessageCryptoArgs mca) throws CardServiceException, NoSuchAlgorithmException, InvalidKeySpecException, IOException
////    {
////        try(var doc = new RdeDocument())
////        {
////            doc.open(tag, bacKey);
////            return doc.getApduResponseForDecryption(mca);
////        }
////        catch(Exception ex)
////        {
////            ex.printStackTrace();
////            throw ex;
////        }
////    }
//
////    @Override
////    protected void onResume()
////    {
////        super.onResume();
////
////        var nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
////        if (nfcAdapter == null)
////            // nfc not available on the device
////            return;
////
////        if (!nfcAdapter.isEnabled())
////        {
////            nfcSettingsLauncher.launch(new Intent(Settings.ACTION_NFC_SETTINGS));
////            return;
////        }
////
////        var intent = new Intent(getApplicationContext(), EnrollmentReadDocumentActivity.class);
////        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
////
////        var pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
////        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, new String[][]{new String[]{"android.nfc.tech.IsoDep"}});
////
////    }
//
//
//}
