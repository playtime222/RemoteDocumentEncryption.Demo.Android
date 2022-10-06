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

import net.sf.scuba.smartcards.CardServiceException;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.jmrtd.BACKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

import nl.rijksoverheid.rdw.rde.client.R;
import nl.rijksoverheid.rdw.rde.client.lib.AndroidRdeDocument;
import nl.rijksoverheid.rdw.rde.documents.*;
import nl.rijksoverheid.rdw.rde.messaging.*;
import nl.rijksoverheid.rdw.rde.mrtdfiles.Dg14Reader;

//todo RENAME to TestDecryptingRbResponse
public class TestRdeDecryptionActivity extends AppCompatActivity
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

        //From Java test dumps
        //Case 4
        final var Case4_PcdPublicKey = "308201753082011d06072a8648ce3d020130820110020101303406072a8648ce3d0101022900d35e472036bc4fb7e13c785ed201e065f98fcfa6f6f40def4f92b9ec7893ec28fcd412b1f1b32e27305404283ee30b568fbab0f883ccebd46d3f3bb8a2a73513f5eb79da66190eb085ffa9f492f375a97d860eb40428520883949dfdbc42d3ad198640688a6fe13f41349554b49acc31dccd884539816f5eb4ac8fb1f1a604510443bd7e9afb53d8b85289bcc48ee5bfe6f20137d10a087eb6e7871e2a10a599c710af8d0d39e2061114fdd05545ec1cc8ab4093247f77275e0743ffed117182eaa9c77877aaac6ac7d35245d1692e8ee1022900d35e472036bc4fb7e13c785ed201e065f98fcfa5b68f12a32d482ec7ee8658e98691555b44c59311020101035200045236a266f0d1cfef2d5f36aaf3f0a2f9b2782628ecbc858dee2447418e07a9aa33a3be67a3c4c06679200f67195860f8a6358fda19bb971aaae2442dc3f482d50135f3517a173c178620237c84db6d55";
        //final var Case4_PcdPrivateKey = "308202c70201003082011d06072a8648ce3d020130820110020101303406072a8648ce3d0101022900d35e472036bc4fb7e13c785ed201e065f98fcfa6f6f40def4f92b9ec7893ec28fcd412b1f1b32e27305404283ee30b568fbab0f883ccebd46d3f3bb8a2a73513f5eb79da66190eb085ffa9f492f375a97d860eb40428520883949dfdbc42d3ad198640688a6fe13f41349554b49acc31dccd884539816f5eb4ac8fb1f1a604510443bd7e9afb53d8b85289bcc48ee5bfe6f20137d10a087eb6e7871e2a10a599c710af8d0d39e2061114fdd05545ec1cc8ab4093247f77275e0743ffed117182eaa9c77877aaac6ac7d35245d1692e8ee1022900d35e472036bc4fb7e13c785ed201e065f98fcfa5b68f12a32d482ec7ee8658e98691555b44c593110201010482019f3082019b020101042890226dfe8294a79fcbed8202b402e033a59c404c5dd6c883fccae159f624bd0f72c5e1fdd8edfd9aa082011430820110020101303406072a8648ce3d0101022900d35e472036bc4fb7e13c785ed201e065f98fcfa6f6f40def4f92b9ec7893ec28fcd412b1f1b32e27305404283ee30b568fbab0f883ccebd46d3f3bb8a2a73513f5eb79da66190eb085ffa9f492f375a97d860eb40428520883949dfdbc42d3ad198640688a6fe13f41349554b49acc31dccd884539816f5eb4ac8fb1f1a604510443bd7e9afb53d8b85289bcc48ee5bfe6f20137d10a087eb6e7871e2a10a599c710af8d0d39e2061114fdd05545ec1cc8ab4093247f77275e0743ffed117182eaa9c77877aaac6ac7d35245d1692e8ee1022900d35e472036bc4fb7e13c785ed201e065f98fcfa5b68f12a32d482ec7ee8658e98691555b44c59311020101a154035200045236a266f0d1cfef2d5f36aaf3f0a2f9b2782628ecbc858dee2447418e07a9aa33a3be67a3c4c06679200f67195860f8a6358fda19bb971aaae2442dc3f482d50135f3517a173c178620237c84db6d55";
        //final var Case4_SharedSecret = "4365c884bcea345510f09bfa9ba02b26ebacd06a6cc3dc418434793127ed92f6d1a0a74d6ad3f3b3";
        //KsEnc: 5254f4a4d092358ad880b2b15159b1a5e2e106fa1d465334e76d5392c3cee16b
        //KsMac: 16db862f7e879c41edd6ccf8390d79785fdca2c83ec8a4b1c55ecbf32bc21e29
        //Command APDU: 0cb08e000d97010a8e087def53cd91b9202000
        //IV: 545363C372825B6E2C1FE39B27DB8726
        //Response:6E8201D9318201D5300D
        //MAC this: 87110127CF892A7B99E61F68701C0B60FF95FD99029000
        //RB Result: 87110127cf892a7b99e61f68701c0b60ff95fd990290008e0886febcffb8fd5cc19000

        final String Case4_Length10 = "8711011342ceb25180b830b9d709ce38f73321990290008e0870f137bd85dcca6b9000";

        final var hexPublicKey = Case4_PcdPublicKey;
        final var hexExpectedCommand = "0cb08e000d97010a8e087def53cd91b9202000";
        final var hexExpectedWrappedRbResult = "87110127cf892a7b99e61f68701c0b60ff95fd990290008e0886febcffb8fd5cc19000";

        try
        {
            //Setup MessageCipherInfo
            final var mca = new MessageCipherInfo();
            //Dont need iv
            final var rdeInfo = new RdeMessageDecryptionInfo();
            rdeInfo.setCommand(hexExpectedCommand);
            rdeInfo.setDocumentDisplayName("SPEC2014");
            rdeInfo.setPcdPublicKey(hexPublicKey);
            mca.setRdeInfo(rdeInfo);

            final var response = getApduResponseForDecryption(SPEC2014BacKey, tag, mca);

            var hexActualRbWrappedResult = Hex.toHexString(response);

            System.out.println("EXPECTED Response  :" + hexExpectedWrappedRbResult);
            System.out.println("ACTUAL   Response  :" + hexActualRbWrappedResult);

            if (hexExpectedWrappedRbResult.equals(hexActualRbWrappedResult))
                System.out.println(">>>> PASS");
            else
                System.out.println("FAIL!");
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

    private static byte[] getApduResponseForDecryption(final BACKey bacKey, final Tag tag, final MessageCipherInfo mca) throws IOException, CardServiceException, GeneralSecurityException {
        byte[] dg14content;
        try (final var doc = new AndroidRdeDocument()) {
            doc.open(tag, bacKey);
            dg14content = doc.getFileContent(14);
        }
        try (final var doc = new AndroidRdeDocument())
        {
            doc.open(tag, bacKey);
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

        var intent = new Intent(getApplicationContext(), TestRdeDecryptionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        var pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, new String[][]{new String[]{"android.nfc.tech.IsoDep"}});
    }
}