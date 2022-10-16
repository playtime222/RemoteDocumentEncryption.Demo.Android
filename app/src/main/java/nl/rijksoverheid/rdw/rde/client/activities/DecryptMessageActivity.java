package nl.rijksoverheid.rdw.rde.client.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import net.sf.scuba.smartcards.CardServiceException;
import net.sf.scuba.util.Hex;

import org.jmrtd.BACKey;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

import javax.crypto.spec.SecretKeySpec;

import nl.rijksoverheid.rdw.rde.client.AppSharedPreferences;
import nl.rijksoverheid.rdw.rde.client.MenuItemHandler;
import nl.rijksoverheid.rdw.rde.client.R;
import nl.rijksoverheid.rdw.rde.client.SimpleDecryptedMessage;
import nl.rijksoverheid.rdw.rde.client.activities.Errors.ShowErrorActivity;
import nl.rijksoverheid.rdw.rde.client.lib.AndroidRdeDocument;
import nl.rijksoverheid.rdw.rde.client.lib.RdeServerProxy;
import nl.rijksoverheid.rdw.rde.crypto.*;
import nl.rijksoverheid.rdw.rde.messaging.*;
import nl.rijksoverheid.rdw.rde.messaging.zipV2.*;

public class DecryptMessageActivity extends AppCompatActivity
{
    ActivityResultLauncher<Intent> nfcSettingsLauncher;
    private long messageId;
    public static final String DECRYPT_MESSAGE_ID = "DECRYPT_MESSAGE_URL";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (new MenuItemHandler().onOptionsItemSelected(item, this))
            return true;

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState)
    {
        nfcSettingsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->
        {
            // nothing to do
        });

        getSupportActionBar().setTitle(R.string.appbar_title);
        getSupportActionBar().setSubtitle("Decrypt Message");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment_read_document);
        messageId = getIntent().getLongExtra(DECRYPT_MESSAGE_ID, -1);
    }

    @Override
    protected void onNewIntent(final Intent intent)
    {
        if (intent == null)
            throw new IllegalArgumentException();

        super.onNewIntent(intent);

        messageId = getIntent().getLongExtra(DECRYPT_MESSAGE_ID, -1);

        if (messageId == -1)
            return;

        if (!NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()))
            return;

        final var storedBacKey = new AppSharedPreferences(this).readBacKey();
        if (!storedBacKey.isComplete())
            return; //TODO show bac not complete - go to Enrollment

        final var bacKey = storedBacKey.toBACKey();

        final Tag tag = intent.getExtras().getParcelable(NfcAdapter.EXTRA_TAG);

        if (tag == null)
            return; //TODO failed

        try
        {
            var sp = new AppSharedPreferences(this);
            final var authToken = sp.readApiToken();

            final var getResult = new RdeServerProxy().getMessage("" + messageId, authToken);

            if (getResult.isError()) {
                ShowErrorActivity.show("Could not contact server or obtain message: " + getResult.getMessage() + "/" + getResult.getMessage(), this);
                return;
            }

            //TODO stop using url style in bodies
            final byte[] message = Base64.getUrlDecoder().decode(getResult.getData().getContentBase64());

            var decoder = new ZipMessageDecoder();

            var mca = decoder.decodeRdeSessionArgs(message);

            final var response = getApduResponseForDecryption(bacKey, tag, mca);
            System.out.println("RESPONSE  :" + Hex.toHexString(response));
            final var key = Crypto.GetAes256SecretKeyFromResponse(response);
            System.out.println("SECRET KEY:" + Hex.toHexString(key));
            final var theKey = new SecretKeySpec(key, "AES");
            final var decryptedMessage = decoder.decode(theKey);

            final var nextIntent = new Intent(getApplicationContext(), ShowMessageActivity.class);

            final var simple = new SimpleDecryptedMessage();
            simple.setId(messageId);
            simple.setWhenSent(getResult.getData().getWhenSent());
            simple.setWhoFrom(getResult.getData().getSenderEmail());
            simple.setShortNote(decryptedMessage.getNote());

            if (decryptedMessage.getFiles().length > 0) {
                simple.setFile1Name(decryptedMessage.getFiles()[0].getFilename());
                simple.setFile1Text(getTextFromFile(decryptedMessage.getFiles()[0].getContent()));
            }

            nextIntent.putExtra(ShowMessageActivity.ExtraTag, simple);
            startActivity(nextIntent);
        }
        catch (GeneralSecurityException e)
        {
            ShowErrorActivity.show("Could not access MRTD: " + e, this);
            e.printStackTrace();
        }
        catch (IOException e)
        {
            ShowErrorActivity.show("Could not access MRTD: " + e, this);
            e.printStackTrace();
        }
        catch (CardServiceException e)
        {
            ShowErrorActivity.show("Could not access MRTD: " + e, this);
            e.printStackTrace();
        }
    }

    String getTextFromFile(byte[] fileContent) {
        try{
            return new String(fileContent, StandardCharsets.UTF_8);
        }
        catch(Exception ex)
        {
            System.out.println("File content is not UTF-8 test: " + ex);
            return Hex.toHexString(fileContent);
        }
    }

    private byte[] getApduResponseForDecryption(final BACKey bacKey, final Tag tag, final MessageCipherInfo mca) throws IOException, CardServiceException, GeneralSecurityException {

        byte[] dg14content;
        try (final var doc = new AndroidRdeDocument()) {
            try {
                doc.open(tag, bacKey);
            }
            catch(IllegalStateException __)
            {
                ShowErrorActivity.show("Could not connect to MRTD with either PACE or BAC when reading DG14.",this);
                return new byte[0];
            }
            dg14content = doc.getFileContent(14);
        }

        try (final var doc = new AndroidRdeDocument())
        {
            try {
                doc.open(tag, bacKey);
            }
            catch(IllegalStateException __)
            {
                ShowErrorActivity.show("Could not connect to MRTD with either PACE or BAC when attempting decrypt.",this);
                return new byte[0];
            }
            return doc.getApduResponseForDecryption(mca, dg14content);
        }
    }

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

        messageId = getIntent().getLongExtra(DECRYPT_MESSAGE_ID,-1);
        var intent = new Intent(getApplicationContext(), DecryptMessageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        var pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, new String[][]{new String[]{"android.nfc.tech.IsoDep"}});
    }
}