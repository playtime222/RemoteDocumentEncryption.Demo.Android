package nl.rijksoverheid.rdw.rde.client.activities;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import net.sf.scuba.smartcards.CardServiceException;
import net.sf.scuba.util.Hex;

import org.jmrtd.BACKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

import nl.rijksoverheid.rdw.rde.client.R;
import nl.rijksoverheid.rdw.rde.client.lib.AndroidRdeDocument;
import nl.rijksoverheid.rdw.rde.documents.GeneralRdeException;
import nl.rijksoverheid.rdw.rde.documents.RdeDocumentEnrollmentInfo;
import nl.rijksoverheid.rdw.rde.documents.UserSelectedEnrollmentArgs;
import nl.rijksoverheid.rdw.rde.messaging.MessageCipherInfo;
import nl.rijksoverheid.rdw.rde.messaging.RdeMessageDecryptionInfo;
import nl.rijksoverheid.rdw.rde.messaging.RdeMessageParameters;
import nl.rijksoverheid.rdw.rde.mrtdfiles.Dg14Reader;

public class TestRdeRoundTripActivity extends AppCompatActivity
{
    ActivityResultLauncher<Intent> nfcSettingsLauncher;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState)
    {
        nfcSettingsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->
        {
            // nothing to do
        });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment_read_document);
    }

    @Override
    protected void onNewIntent(final Intent intent)
    {
        if (intent == null)
            throw new IllegalArgumentException();

        super.onNewIntent(intent);

        if (!NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()))
            return;

        final Tag tag = intent.getExtras().getParcelable(NfcAdapter.EXTRA_TAG);

        if (tag == null)
            throw new IllegalStateException("Test failed. No NfcAdaptor Tag.");

        final String DOCUMENT_NUMBER = "SPECI2014";
        final String DATE_OF_BIRTH = "650310";
        final String DATE_OF_EXPIRY = "240309";
        final var SPEC2014BacKey = new BACKey(DOCUMENT_NUMBER,DATE_OF_BIRTH,DATE_OF_EXPIRY);

        try
        {
            byte[] dg14content;
            try (final var doc = new AndroidRdeDocument()) {
                doc.open(tag, SPEC2014BacKey);
                dg14content = doc.getFileContent(14);
            }

            final var args = new UserSelectedEnrollmentArgs(14, 64);
//          args.setDisplayName("default");

//            //Not relevent...
//            RdeDocumentEnrollmentInfo enrollmentInfo;
//            try (final var doc = new AndroidRdeDocument()) {
//                doc.open(tag, SPEC2014BacKey);
//                enrollmentInfo = doc.getEnrollmentArgs(args, dg14content);
//            }

            RdeMessageParameters rbResult;
            try (final var doc = new AndroidRdeDocument()) {
                doc.open(tag, SPEC2014BacKey);
                rbResult = doc.doTestRbCall(new Dg14Reader(dg14content), args.getShortFileId(), args.getFileByteCount());
            }

            //TODO pcd pub key and from send message
            var mci = new MessageCipherInfo();
            var rdeInfo = new RdeMessageDecryptionInfo();
            rdeInfo.setPcdPublicKey(Hex.toHexString(rbResult.getEphemeralPublicKey()));
            rdeInfo.setCommand(Hex.toHexString(rbResult.getWrappedCommand()));
            mci.setRdeInfo(rdeInfo);

            byte[] decryptRbResponse;
            try (final var doc = new AndroidRdeDocument()) {
                doc.open(tag, SPEC2014BacKey);
                decryptRbResponse = doc.getApduResponseForDecryption(mci, dg14content);
            }
            var decryptRbResponseHex = Hex.toHexString(decryptRbResponse);
            var rbResultHex = Hex.toHexString(rbResult.getWrappedResponse());

            System.out.println("Encrypt Response: " + rbResultHex);
            System.out.println("Decrypt Response: " + decryptRbResponseHex);

            if (!rbResultHex.equals(decryptRbResponseHex))
                System.out.println("FAIL! FAIL! FAIL! FAIL! FAIL! FAIL! FAIL! FAIL! FAIL! ");
            else
                System.out.println("SUCCESS!");




        }
        catch (GeneralSecurityException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (CardServiceException e)
        {
            e.printStackTrace();
        }
        catch (GeneralRdeException e)
        {
            e.printStackTrace();
        }
    }

//    private static byte[] getApduResponseForDecryption(final BACKey bacKey, final Tag tag, final MessageCipherInfo mca) throws IOException, CardServiceException, GeneralSecurityException, GeneralRdeException {
//        byte[] dg14content;
//        try (final var doc = new AndroidRdeDocument()) {
//            doc.open(tag, bacKey);
//            dg14content = doc.getFileContent(14);
//        }
//        var dg14 = new Dg14Reader(dg14content);
//
//        try (final var doc = new AndroidRdeDocument())
//        {
//            doc.open(tag, bacKey);
//            return doc.getApduResponseForDecryption(mca, dg14);
//        }
//    }

    @Override
    protected void onResume()
    {
        super.onResume();

        var nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
        if (nfcAdapter == null)
            // nfc not available on the device
            return;

        if (!nfcAdapter.isEnabled())
        {
            nfcSettingsLauncher.launch(new Intent(Settings.ACTION_NFC_SETTINGS));
            return;
        }

        var intent = new Intent(getApplicationContext(), TestRdeRoundTripActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        var pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, new String[][]{new String[]{"android.nfc.tech.IsoDep"}});
    }
}