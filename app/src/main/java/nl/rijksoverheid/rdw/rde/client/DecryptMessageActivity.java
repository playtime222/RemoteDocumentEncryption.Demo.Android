package nl.rijksoverheid.rdw.rde.client;

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
import net.sf.scuba.util.Hex;

import org.jmrtd.BACKey;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import javax.crypto.spec.SecretKeySpec;

import nl.rijksoverheid.rdw.rde.apdusimulator.*;
import nl.rijksoverheid.rdw.rde.client.lib.RdeDocument;
import nl.rijksoverheid.rdw.rde.client.lib.RdeServerProxy;
import nl.rijksoverheid.rdw.rde.crypto.*;
import nl.rijksoverheid.rdw.rde.documents.*;
import nl.rijksoverheid.rdw.rde.remoteapi.*;
import nl.rijksoverheid.rdw.rde.messaging.*;
import nl.rijksoverheid.rdw.rde.messaging.zipV2.*;
import nl.rijksoverheid.rdw.rde.mrtdfiles.*;

public class DecryptMessageActivity extends AppCompatActivity
{
    ActivityResultLauncher<Intent> nfcSettingsLauncher;
    private String messageId;
    public static final String ExtraTag = "DECRYPT_MESSAGE_URL";

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState)
    {
        nfcSettingsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->
        {
            // nothing to do
        });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment_read_document);
        messageId = getIntent().getStringExtra(ExtraTag);
    }

    @Override
    protected void onNewIntent(final Intent intent)
    {
        if (intent == null)
            throw new IllegalArgumentException();

        super.onNewIntent(intent);

        if (messageId == null)
            return;

        if (!NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()))
            return;

        final var bacKeyStorage = new BacKeyStorage();
        bacKeyStorage.read(PreferenceManager.getDefaultSharedPreferences(this));
        final var bacKey = bacKeyStorage.getValue();

        final Tag tag = intent.getExtras().getParcelable(NfcAdapter.EXTRA_TAG);

        if (tag == null)
            return; //TODO failed

        try
        {
            final var getResult = new RdeServerProxy().GetMessage(messageId);

            if (getResult.isError())
                return;

            final var message = getResult.getContent();

            var decoder = new ZipMessageDecoder();

            var mca = decoder.decodeRdeSessionArgs(message);

            final var response = getApduResponseForDecryption(bacKey, tag, mca);
            System.out.println("RESPONSE  :" + Hex.toHexString(response));
            final var key = new RbResponseToSecretKeyConverter().convert(response);
            System.out.println("SECRET KEY:" + Hex.toHexString(key));
            //System.out.println("CONTENT   :" + Hex.toHexString(content));
            final var theKey = new SecretKeySpec(key, "AES");
            final var decryptedMessage = decoder.decode(theKey);

            final var nextIntent = new Intent(getApplicationContext(), ShowMessageActivity.class);

            final var simple = new SimpleDecryptedMessage();
            //simple.setId();
            //simple.setWhenSent(decryptedMessage.get);
            //simple.setWhenSent(decryptedMessage.get);
            simple.setShortNote(decryptedMessage.getNote());
            simple.setFile1Text(new String(decryptedMessage.getFiles()[0].getContent(), StandardCharsets.UTF_8));

            nextIntent.putExtra(ShowMessageActivity.ExtraTag, simple);
            startActivity(nextIntent);
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
    }

    private static byte[] getApduResponseForDecryption(final BACKey bacKey, final Tag tag, final RdeSessionArgs mca) throws IOException, CardServiceException, GeneralSecurityException
    {
        try (final var doc = new RdeDocument())
        {
            doc.open(tag, bacKey);
            return doc.getApduResponseForDecryption(mca);
        }
    }

//    private RdeDocumentEnrollmentInfo getEnrollmentArgs(Tag tag, BACKey bacKey, UserSelectedEnrollmentArgs args)
//            throws GeneralSecurityException, IOException, CardServiceException
//    {
//        try (var doc = new RdeDocument())
//        {
//            doc.open(tag, bacKey);
//            return doc.getEnrollmentArgs(args);
//        } catch (Exception ex)
//        {
//            ex.printStackTrace();
//            throw ex;
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

        messageId = getIntent().getStringExtra(ExtraTag);

        var intent = new Intent(getApplicationContext(), nl.rijksoverheid.rdw.rde.client.DecryptMessageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        var pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, new String[][]{new String[]{"android.nfc.tech.IsoDep"}});
    }
}