package nl.rijksoverheid.rdw.rde.client;

import org.bouncycastle.util.encoders.Base64;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;

import net.sf.scuba.smartcards.CardServiceException;

import org.jmrtd.BACKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

import nl.rijksoverheid.rdw.rde.client.lib.AndroidRdeDocument;
import nl.rijksoverheid.rdw.rde.client.lib.RdeServerProxy;
import nl.rijksoverheid.rdw.rde.documents.*;
import nl.rijksoverheid.rdw.rde.remoteapi.*;

public class EnrollmentReadDocumentActivity extends AppCompatActivity {
    ActivityResultLauncher<Intent> nfcSettingsLauncher;

    //public static final String ENROLLMENT_ID_EXTRA_TAG = "ENROLLMENT_ID";
    public static final String DISPLAY_NAME_EXTRA_TAG = "ENROLLMENT_DISPLAY_NAME";
    private String authToken;
    private String displayName;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        nfcSettingsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->
        {
            // nothing to do
        });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment_read_document);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        if (intent == null)
            throw new IllegalArgumentException();

        super.onNewIntent(intent);
        authToken = intent.getStringExtra(ScanApiTokenActivity.API_TOKEN_EXTRA_TAG);

        if (!NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()))
            return;

        final var bacKeyStorage = new BacKeyStorage();
        bacKeyStorage.read(intent);
        final var bacKey = bacKeyStorage.getValue();
        Tag tag = intent.getExtras().getParcelable(NfcAdapter.EXTRA_TAG);

        if (tag == null)
            return; //TODO failed

        try {
//            try(var d = new RdeDocument())
//            {
//                d.open(tag, bacKey);
//                d.doTestRbCall();
//            }

            try {
                final var args = getEnrollmentArgs(tag, bacKey, new UserSelectedEnrollmentArgs(14, 8));
                final var dto = new DocumentEnrolmentRequestArgs();

                dto.setDisplayName(displayName);
                dto.setFileId(args.getShortFileId());
                dto.setFileContentsBase64(Base64.toBase64String(args.getFileContents()));
                dto.setReadLength(args.getFileReadLength());
                dto.setDataGroup14Base64(Base64.toBase64String(args.getDataGroup14()));

                //EndToEndTest.Test(bacKey, tag, args);

                var result = new RdeServerProxy().enrol(dto, authToken);

                if (result.isError()) {
                    //TODO show and an error...
                    return;
                }

                final var nextIntent = new Intent(getApplicationContext(), MessagesListActivity.class);

                bacKeyStorage.write(PreferenceManager.getDefaultSharedPreferences(this));
                startActivity(nextIntent);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GeneralRdeException e) {
                e.printStackTrace();
            }
        } finally {
            //TODO Why was this forced?
        }
    }

    private RdeDocumentEnrollmentInfo getEnrollmentArgs(final Tag tag, final BACKey bacKey,
                                                        final UserSelectedEnrollmentArgs args)
            throws GeneralSecurityException, IOException, GeneralRdeException {
        try (final var doc = new AndroidRdeDocument()) {
            doc.open(tag, bacKey);
            return doc.getEnrollmentArgs(args);
        } catch (CardServiceException ex) {
            ex.printStackTrace();
            throw new GeneralRdeException(ex);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //TODO hits here first. Set field?
//        var bacKey = new BacKeyStorage().read(getIntent());
//        Tag tag = getIntent().getExtras().getParcelable(NfcAdapter.EXTRA_TAG);

        displayName = getIntent().getStringExtra(DISPLAY_NAME_EXTRA_TAG);

        final var nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
        if (nfcAdapter == null)
            // nfc not available on the device
            return;

        if (!nfcAdapter.isEnabled()) {
            nfcSettingsLauncher.launch(new Intent(Settings.ACTION_NFC_SETTINGS));
            return;
        }

        final var intent = new Intent(getApplicationContext(), EnrollmentReadDocumentActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final var pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, new String[][]{new String[]{"android.nfc.tech.IsoDep"}});
    }
}