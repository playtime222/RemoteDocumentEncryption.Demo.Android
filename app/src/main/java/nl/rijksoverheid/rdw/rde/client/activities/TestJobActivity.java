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
import nl.rijksoverheid.rdw.rde.documents.UserSelectedEnrollmentArgs;

public class TestJobActivity extends AppCompatActivity
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

        if (!NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()))
            return;

        byte[] dg14content;
        try
        {
            var userArgs = new UserSelectedEnrollmentArgs();
            userArgs.setShortFileId(2);
            userArgs.setFileByteCount(10);
            userArgs.setDisplayName("The Enrollment Test Activity");
            try (final var doc = new AndroidRdeDocument()) {
                //doc.open(tag, SPEC2014BacKey);
                dg14content = doc.doJobTest();
                System.out.println("Actual:  " + Hex.toHexString(dg14content));
                final var HexEncodedDg14 = "6E8201D9318201D5300D060804007F0007020202020101300F060A04007F000702020302040201013012060A04007F0007020204020402010202010E30170606678108010105020101060A04007F0007010104010330820184060904007F000702020102308201753082011D06072A8648CE3D020130820110020101303406072A8648CE3D0101022900D35E472036BC4FB7E13C785ED201E065F98FCFA6F6F40DEF4F92B9EC7893EC28FCD412B1F1B32E27305404283EE30B568FBAB0F883CCEBD46D3F3BB8A2A73513F5EB79DA66190EB085FFA9F492F375A97D860EB40428520883949DFDBC42D3AD198640688A6FE13F41349554B49ACC31DCCD884539816F5EB4AC8FB1F1A604510443BD7E9AFB53D8B85289BCC48EE5BFE6F20137D10A087EB6E7871E2A10A599C710AF8D0D39E2061114FDD05545EC1CC8AB4093247F77275E0743FFED117182EAA9C77877AAAC6AC7D35245D1692E8EE1022900D35E472036BC4FB7E13C785ED201E065F98FCFA5B68F12A32D482EC7EE8658E98691555B44C5931102010103520004710DA6DAB5B770920D3D4D6807B02A13059BEFB4926E2D00CFDE4B4471571473A582934BBE92059800663578C83419E3563FE3E8AF3AE58B521D3741693C9CE19B312392CB00F59AF086863186706396";
                System.out.println("Expected:" + HexEncodedDg14);
            }
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

        var intent = new Intent(getApplicationContext(), TestJobActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        var pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, new String[][]{new String[]{"android.nfc.tech.IsoDep"}});
    }
}