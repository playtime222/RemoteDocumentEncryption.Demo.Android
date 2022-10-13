//package nl.rijksoverheid.rdw.rde.client.activities;
//
//import android.app.PendingIntent;
//import android.content.Intent;
//import android.nfc.NfcAdapter;
//import android.nfc.Tag;
//import android.os.Bundle;
//import android.provider.Settings;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import net.sf.scuba.smartcards.CardServiceException;
//import net.sf.scuba.util.Hex;
//
//import org.jmrtd.BACKey;
//import org.jmrtd.PACEKeySpec;
//import org.jmrtd.PassportService;
//
//import java.io.IOException;
//import java.security.GeneralSecurityException;
//
//import nl.rijksoverheid.rdw.rde.casessionutilities.RdeMessageParameters;
//import nl.rijksoverheid.rdw.rde.client.R;
//import nl.rijksoverheid.rdw.rde.client.lib.AndroidRdeDocument;
//import nl.rijksoverheid.rdw.rde.documents.UserSelectedEnrollmentArgs;
//import nl.rijksoverheid.rdw.rde.messaging.MessageCipherInfo;
//import nl.rijksoverheid.rdw.rde.messaging.RdeMessageDecryptionInfo;
//import nl.rijksoverheid.rdw.rde.mrtdfiles.Dg14Reader;
//
//public class TestRdeRoundTripDrivingLicenceActivity extends AppCompatActivity
//{
//    ActivityResultLauncher<Intent> nfcSettingsLauncher;
//
//    @Override
//    protected void onCreate(@Nullable final Bundle savedInstanceState)
//    {
//        nfcSettingsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->
//        {
//            // nothing to do
//        });
//
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_enrollment_read_document);
//    }
//
//    @Override
//    protected void onNewIntent(final Intent intent)
//    {
//        if (intent == null)
//            throw new IllegalArgumentException();
//
//        super.onNewIntent(intent);
//
//        if (!NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()))
//            return;
//
//        final Tag tag = intent.getExtras().getParcelable(NfcAdapter.EXTRA_TAG);
//
//        if (tag == null)
//            throw new IllegalStateException("Test failed. No NfcAdaptor Tag.");
//
//        final String DOCUMENT_NUMBER = "D1NLD250949621112HF7W55VL42L21";
//        //MRZ_PACE_KEY_REFERENCE, - Doesnt have an MRZ key
//        //CAN_PACE_KEY_REFERENCE, -> Sending general authenticate failed, PCD side exception in authentication token generation step,
//        //PIN_PACE_KEY_REFERENCE, -> Sending MSE AT failed, PICC side error in static PACE key derivation step
//        //PUK_PACE_KEY_REFERENCE -> key not found
//        final var paceKeySpec = new PACEKeySpec(DOCUMENT_NUMBER, PassportService.CAN_PACE_KEY_REFERENCE);
//
//        var file = 2;
//        var length = 100;
//        try
//        {
//            byte[] dg14content;
//            try (final var doc = new AndroidRdeDocument()) {
//                doc.open(tag, paceKeySpec);
//                dg14content = doc.getFileContent(14);
//                //System.out.println("Target : " + file + " -> " + doc.getFileContent(file));
//            }
//
//            System.out.println("DG14 : " +Hex.toHexString(dg14content));
//
//            final var args = new UserSelectedEnrollmentArgs(file, length);
//
//            RdeMessageParameters rbResult;
//            try (final var doc = new AndroidRdeDocument()) {
//                doc.open(tag, paceKeySpec);
//                rbResult = doc.doTestRbCall(new Dg14Reader(dg14content), args.getShortFileId(), args.getFileByteCount());
//            }
//
//            System.out.println("Wrapped DG" + file + " response: " + Hex.toHexString(rbResult.getWrappedResponse()));
//
//            //TODO pcd pub key and from send message
//            var mci = new MessageCipherInfo();
//            var rdeInfo = new RdeMessageDecryptionInfo();
//            rdeInfo.setPcdPublicKey(Hex.toHexString(rbResult.getEphemeralPublicKey()));
//            rdeInfo.setCommand(Hex.toHexString(rbResult.getWrappedCommand()));
//            mci.setRdeInfo(rdeInfo);
//
//            byte[] decryptRbResponse;
//            try (final var doc = new AndroidRdeDocument()) {
//                doc.open(tag, paceKeySpec);
//                decryptRbResponse = doc.getApduResponseForDecryption(mci, dg14content);
//            }
//            var decryptRbResponseHex = Hex.toHexString(decryptRbResponse);
//            var rbResultHex = Hex.toHexString(rbResult.getWrappedResponse());
//
//            System.out.println("Encrypt Response: " + rbResultHex);
//            System.out.println("Decrypt Response: " + decryptRbResponseHex);
//
//            if (!rbResultHex.equals(decryptRbResponseHex))
//                System.out.println("FAIL! FAIL! FAIL! FAIL! FAIL! FAIL! FAIL! FAIL! FAIL! ");
//            else
//                System.out.println("SUCCESS!");
//        }
//        catch (GeneralSecurityException e)
//        {
//            e.printStackTrace();
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//        catch (CardServiceException e)
//        {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    protected void onResume()
//    {
//        super.onResume();
//
//        var nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
//        if (nfcAdapter == null)
//            // nfc not available on the device
//            return;
//
//        if (!nfcAdapter.isEnabled())
//        {
//            nfcSettingsLauncher.launch(new Intent(Settings.ACTION_NFC_SETTINGS));
//            return;
//        }
//
//        var intent = new Intent(getApplicationContext(), TestRdeRoundTripDrivingLicenceActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        var pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, new String[][]{new String[]{"android.nfc.tech.IsoDep"}});
//    }
//}