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

import org.bouncycastle.util.encoders.Base64;
import org.jmrtd.BACKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

import nl.rijksoverheid.rdw.rde.client.R;
import nl.rijksoverheid.rdw.rde.client.lib.AndroidRdeDocument;
import nl.rijksoverheid.rdw.rde.documents.GeneralRdeException;
import nl.rijksoverheid.rdw.rde.messaging.MessageCipherInfo;
import nl.rijksoverheid.rdw.rde.messaging.RdeMessageDecryptionInfo;
import nl.rijksoverheid.rdw.rde.mrtdfiles.Dg14Reader;

public class TestCrossPlatformDecryptionActivity extends AppCompatActivity {
    ActivityResultLauncher<Intent> nfcSettingsLauncher;

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

        if (!NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()))
            return;

        final Tag tag = intent.getExtras().getParcelable(NfcAdapter.EXTRA_TAG);

        if (tag == null)
            throw new IllegalStateException("Test failed. No NfcAdaptor Tag.");

        final String DOCUMENT_NUMBER = "SPECI2014";
        final String DATE_OF_BIRTH = "650310";
        final String DATE_OF_EXPIRY = "240309";
        final var SPEC2014BacKey = new BACKey(DOCUMENT_NUMBER, DATE_OF_BIRTH, DATE_OF_EXPIRY);

        //From SPEC2014
        //final var HexEncodedDg14 = "6E8201D9318201D5300D060804007F0007020202020101300F060A04007F000702020302040201013012060A04007F0007020204020402010202010E30170606678108010105020101060A04007F0007010104010330820184060904007F000702020102308201753082011D06072A8648CE3D020130820110020101303406072A8648CE3D0101022900D35E472036BC4FB7E13C785ED201E065F98FCFA6F6F40DEF4F92B9EC7893EC28FCD412B1F1B32E27305404283EE30B568FBAB0F883CCEBD46D3F3BB8A2A73513F5EB79DA66190EB085FFA9F492F375A97D860EB40428520883949DFDBC42D3AD198640688A6FE13F41349554B49ACC31DCCD884539816F5EB4AC8FB1F1A604510443BD7E9AFB53D8B85289BCC48EE5BFE6F20137D10A087EB6E7871E2A10A599C710AF8D0D39E2061114FDD05545EC1CC8AB4093247F77275E0743FFED117182EAA9C77877AAAC6AC7D35245D1692E8EE1022900D35E472036BC4FB7E13C785ED201E065F98FCFA5B68F12A32D482EC7EE8658E98691555B44C5931102010103520004710DA6DAB5B770920D3D4D6807B02A13059BEFB4926E2D00CFDE4B4471571473A582934BBE92059800663578C83419E3563FE3E8AF3AE58B521D3741693C9CE19B312392CB00F59AF086863186706396";
        final var CaProtocolOid = "0.4.0.127.0.7.2.2.3.2.4";

        //From C#

//        ssc       : 0000000000000000-0000000000000001
//        n         : 0000000000000000-0000000000000001-0cb08e0080000000-0000000000000000-9701ff8000000000-0000000000000000
//        mac       : 95a577346372b3d6-a19edd79ce730c5e
//        d08e      : 8e0895a577346372-b3d6
//        Response       : 6e8201d9318201d5300d060804007f0007020202020101300f060a04007f000702020302040201013012060a04007f0007020204020402010202010e30170606678108010105020101060a04007f0007010104010330820184060904007f000702020102308201753082011d06072a8648ce3d020130820110020101303406072a8648ce3d0101022900d35e472036bc4fb7e13c785ed201e065f98fcfa6f6f40def4f92b9ec7893ec28fcd412b1f1b32e27305404283ee30b568fbab0f883ccebd46d3f3bb8a2a73513f5eb79da66190eb085ffa9f492f375a97d860eb40428520883949dfdbc42d3ad198640688a6fe13f41349554b49acc31dccd884539
//        Padded response: 6e8201d9318201d5300d060804007f0007020202020101300f060a04007f000702020302040201013012060a04007f0007020204020402010202010e30170606678108010105020101060a04007f0007010104010330820184060904007f000702020102308201753082011d06072a8648ce3d020130820110020101303406072a8648ce3d0101022900d35e472036bc4fb7e13c785ed201e065f98fcfa6f6f40def4f92b9ec7893ec28fcd412b1f1b32e27305404283ee30b568fbab0f883ccebd46d3f3bb8a2a73513f5eb79da66190eb085ffa9f492f375a97d860eb40428520883949dfdbc42d3ad198640688a6fe13f41349554b49acc31dccd88453980
//        IV: 6929a2184d4e7efb10ef42829f00c090
//        Response ciphertext: 14e458abd787b05478f35336a596d3dda4d9a260414964eed62b29ecbc483bab5c503b5b2eaaab7161328ba17cca53c43748c522094a98b4e2a1f7f9b3019da8d2fb37ac1f35a75e041d22af61b65950ff115fa39961d237e35dac6d21a45af228bbcc5800311ba24afb79c093d41864584570dcfb26e29eb6254a0ba0c1739118e4952fbc53d799e0bc8a0b707e0f45c69413e6de8a294ab5d81af34ba021d40f5cf7dde7de99233175a258022768b6f8dc66c3020543c64e9964a5983cbb1a77fca55496983161b1e3633d25de287991003d9b612e77427d38adccaf866f76223057b442ab6a3de8307d4f662ef16f4b6d842e6ce8d92532deccd3980be658
//        MAC this: 878201010114e458abd787b05478f35336a596d3dda4d9a260414964eed62b29ecbc483bab5c503b5b2eaaab7161328ba17cca53c43748c522094a98b4e2a1f7f9b3019da8d2fb37ac1f35a75e041d22af61b65950ff115fa39961d237e35dac6d21a45af228bbcc5800311ba24afb79c093d41864584570dcfb26e29eb6254a0ba0c1739118e4952fbc53d799e0bc8a0b707e0f45c69413e6de8a294ab5d81af34ba021d40f5cf7dde7de99233175a258022768b6f8dc66c3020543c64e9964a5983cbb1a77fca55496983161b1e3633d25de287991003d9b612e77427d38adccaf866f76223057b442ab6a3de8307d4f662ef16f4b6d842e6ce8d92532deccd3980be65899029000
//        MAC     : 6771a460dd881c262e66cf4556d34495
//        DUMP RDE Message Parameters >>>>>>>
//        {
//            "EphemeralPublicKey": "MIIBdTCCAR0GByqGSM49AgEwggEQAgEBMDQGByqGSM49AQECKQDTXkcgNrxPt+E8eF7SAeBl+Y/Ppvb0De9PkrnseJPsKPzUErHxsy4nMFQEKD7jC1aPurD4g8zr1G0/O7iipzUT9et52mYZDrCF/6n0kvN1qX2GDrQEKFIIg5Sd/bxC060ZhkBoim/hP0E0lVS0mswx3M2IRTmBb160rI+x8aYEUQRDvX6a+1PYuFKJvMSO5b/m8gE30QoIfrbnhx4qEKWZxxCvjQ054gYRFP3QVUXsHMirQJMkf3cnXgdD/+0RcYLqqcd4d6qsasfTUkXRaS6O4QIpANNeRyA2vE+34Tx4XtIB4GX5j8+lto8Soy1ILsfuhljphpFVW0TFkxECAQEDUgAER/2dW0k77SR3Yr+1ffgrDTohofwuOTmgaekiChYA0MKiNX7QtGKKvTNs7R4EkhVVXlZt+tEIaoJpXE+8c24lE2BUo5k31xPH9CKK0QG4Dsg=",
//                "WrappedCommand": "DLCOAA2XAf+OCJWldzRjcrPWAA==",
//                "WrappedResponse": "h4IBAQEU5Fir14ewVHjzUzalltPdpNmiYEFJZO7WKynsvEg7q1xQO1suqqtxYTKLoXzKU8Q3SMUiCUqYtOKh9/mzAZ2o0vs3rB81p14EHSKvYbZZUP8RX6OZYdI3412sbSGkWvIou8xYADEbokr7ecCT1BhkWEVw3Psm4p62JUoLoMFzkRjklS+8U9eZ4LyKC3B+D0XGlBPm3oopSrXYGvNLoCHUD1z33efemSMxdaJYAidotvjcZsMCBUPGTplkpZg8uxp3/KVUlpgxYbHjYz0l3ih5kQA9m2Eud0J9OK3Mr4ZvdiIwV7RCq2o96DB9T2Yu8W9LbYQubOjZJTLezNOYC+ZYmQKQAI4IZ3GkYN2IHCaQAA==",
//                "DebugInfo": {
//            "SharedSecretHex": "008cfc0ba21c404773f55bed3642f873b5ed5b8c630e243820f70e9ddc39afb33ce54b945e3cf3ef30",
//                    "ReadBinaryResponseHex": "878201010114e458abd787b05478f35336a596d3dda4d9a260414964eed62b29ecbc483bab5c503b5b2eaaab7161328ba17cca53c43748c522094a98b4e2a1f7f9b3019da8d2fb37ac1f35a75e041d22af61b65950ff115fa39961d237e35dac6d21a45af228bbcc5800311ba24afb79c093d41864584570dcfb26e29eb6254a0ba0c1739118e4952fbc53d799e0bc8a0b707e0f45c69413e6de8a294ab5d81af34ba021d40f5cf7dde7de99233175a258022768b6f8dc66c3020543c64e9964a5983cbb1a77fca55496983161b1e3633d25de287991003d9b612e77427d38adccaf866f76223057b442ab6a3de8307d4f662ef16f4b6d842e6ce8d92532deccd3980be658990290008e086771a460dd881c269000",
//                    "CaWrapperDebugInfo": {
//                "Type": "AesSecureMessagingWrapper",
//                        "Cipher": "AES/CBC/NoPadding",
//                        "Mac": "AESCMAC",
//                        "KsEnc": "20a8f83529af9dac11c385b0c22f15f01140b7d357f4eae38764680a93c40053",
//                        "KsMac": "d2cae3b2e407e9601c13ecfc50b0e74c29e873f003f6d136429e6d8c9c472313"
//            },
//            "WrappedResponseHex": "878201010114e458abd787b05478f35336a596d3dda4d9a260414964eed62b29ecbc483bab5c503b5b2eaaab7161328ba17cca53c43748c522094a98b4e2a1f7f9b3019da8d2fb37ac1f35a75e041d22af61b65950ff115fa39961d237e35dac6d21a45af228bbcc5800311ba24afb79c093d41864584570dcfb26e29eb6254a0ba0c1739118e4952fbc53d799e0bc8a0b707e0f45c69413e6de8a294ab5d81af34ba021d40f5cf7dde7de99233175a258022768b6f8dc66c3020543c64e9964a5983cbb1a77fca55496983161b1e3633d25de287991003d9b612e77427d38adccaf866f76223057b442ab6a3de8307d4f662ef16f4b6d842e6ce8d92532deccd3980be658990290008e086771a460dd881c269000"
//        }
//        }
//<<<<<< DUMP RDE Message Parameters
//        SecretKey: 0cee5325485189bffa5d5307be397570840c0402bae22343e62e5507c05d5484
//        Encoded: 504b03041400000808002672fb5436623b130d0000000500000005000000525f315f3132d433d03300000000ffff0300504b03041400000808002672fb549ea93f2b19000000100000000400000041545f315a17b0c440aa53e6ac937446f239a6abd600000000ffff0300504b03041400000808002672fb5414fabdcf0c0000000400000005000000525f335f31cacb2f4905000000ffff0300504b03041400000808002672fb54ec025d1a19000000100000000400000041545f33dafbfb76c41a7e974707986fdee4ffa2b414000000ffff0300504b03041400000808002672fb543cc0160a3e020000c703000005000000525f325f319453cb8e1c370cfc9739070b8aa444d1675f8200b17f81e223586076c770760c1886ff3d9c754eb9058d6ea949aa582c523f2ecfdf2e1f2ea67bb2a8a36bcdd202587b9f4022dfab365f7ebb7c8dfcfdb56e970f3f2e71f3fb4bbebe7d7cfefbcbd5beff692fd9181fb3ec7e7deb50b7cf5f6f6f37bf5d3f3d477be0899fe069a0f4579eb01feaf701fac5e3f3fd5c9ffd8ffcde81041b61c8fcb58e800582b6176f4f0a68dbbf1e78ec1f7fc4ff89692b202a40d04c16045ac7b98ee420973d33fa64c2ea2a7779d9aa550c91c5a578343b46291ddb193cf08c1a873051082603e3a64c8233d7ae63076a6f72cf13bc828aced986263407d5cc231ab6d650c8037b5699162b16c93495d8abedef9813a17194352a9a2d06590ced9a1e6d688acdbd7810eb9c7c58cd9d46b8c7de3c49f7588f646cde9c9aaf2de03980994e48aad59914fb7496adc79d77e63c95ab1e7a4a0c30d89267a56c1989d686a9ead26bed68213511d618832b029ac24c1feedb9abc12b29408ca4c10a6aa8c31646c4cb3c6684831335fe6d21d419e3196623687ffd7a7795af1814618dce02e997bcd9dad928e39e761f6a9d4a3f13e19d0b9005a03a9d068afd269481659785a1089da783ab9e1b0518e494a6ab03415116cac66068e863425030e2fec828368795798d07d7ca49d39d78aea76c15eb671e97c54e1422bb1a760f5d418a9b6cef228ac181ba7a3c7d990be1fb7e5f6f262afeff7c47b4cb269870a8caade6f9d36458817091e8aa6d54722cffdaf5fb7f1f57ebdfefcf90f000000ffff0300504b03041400000808002672fb544b1adf0b18000000100000000400000041545f3232b012e0f0639ada545472cb9129f01e37000000ffff0300504b03041400000808002672fb54f32b0947190000001000000005000000525f355f31bacbf52b2aa9b7bec7afa1f687d9edd53900000000ffff0300504b03041400000808002672fb54b3159210190000001000000003000000415f350abf957847cbefaac40f8b1b769b2a350300000000ffff0300504b03041400000808002672fb54d001b1a82a0000002000000005000000525f345f31526ef32fe728dfdf5f7daee2d7679e2f57f758b09831d41784afdf75f705eb99031f01000000ffff0300504b03041400000808002672fb54147088cf19000000100000000400000041545f3412f3f8d47ba7cab66043777bc331ffbd1900000000ffff0300504b010214001400000808002672fb5436623b130d00000005000000050000000000000000000000000000000000525f315f31504b010214001400000808002672fb549ea93f2b190000001000000004000000000000000000000000003000000041545f31504b010214001400000808002672fb5414fabdcf0c0000000400000005000000000000000000000000006b000000525f335f31504b010214001400000808002672fb54ec025d1a190000001000000004000000000000000000000000009a00000041545f33504b010214001400000808002672fb543cc0160a3e020000c70300000500000000000000000000000000d5000000525f325f31504b010214001400000808002672fb544b1adf0b180000001000000004000000000000000000000000003603000041545f32504b010214001400000808002672fb54f32b09471900000010000000050000000000000000000000000070030000525f355f31504b010214001400000808002672fb54b315921019000000100000000300000000000000000000000000ac030000415f35504b010214001400000808002672fb54d001b1a82a000000200000000500000000000000000000000000e6030000525f345f31504b010214001400000808002672fb54147088cf190000001000000004000000000000000000000000003304000041545f34504b0506000000000a000a00f80100006e0400000000

        final var EphemeralPublicKeyBase64 = "MIIBdTCCAR0GByqGSM49AgEwggEQAgEBMDQGByqGSM49AQECKQDTXkcgNrxPt+E8eF7SAeBl+Y/Ppvb0De9PkrnseJPsKPzUErHxsy4nMFQEKD7jC1aPurD4g8zr1G0/O7iipzUT9et52mYZDrCF/6n0kvN1qX2GDrQEKFIIg5Sd/bxC060ZhkBoim/hP0E0lVS0mswx3M2IRTmBb160rI+x8aYEUQRDvX6a+1PYuFKJvMSO5b/m8gE30QoIfrbnhx4qEKWZxxCvjQ054gYRFP3QVUXsHMirQJMkf3cnXgdD/+0RcYLqqcd4d6qsasfTUkXRaS6O4QIpANNeRyA2vE+34Tx4XtIB4GX5j8+lto8Soy1ILsfuhljphpFVW0TFkxECAQEDUgAER/2dW0k77SR3Yr+1ffgrDTohofwuOTmgaekiChYA0MKiNX7QtGKKvTNs7R4EkhVVXlZt+tEIaoJpXE+8c24lE2BUo5k31xPH9CKK0QG4Dsg=";
        final var WrappedCommandBase64 = "DLCOAA2XAf+OCJWldzRjcrPWAA==";
        final var WrappedResponseBase64 = "h4IBAQEU5Fir14ewVHjzUzalltPdpNmiYEFJZO7WKynsvEg7q1xQO1suqqtxYTKLoXzKU8Q3SMUiCUqYtOKh9/mzAZ2o0vs3rB81p14EHSKvYbZZUP8RX6OZYdI3412sbSGkWvIou8xYADEbokr7ecCT1BhkWEVw3Psm4p62JUoLoMFzkRjklS+8U9eZ4LyKC3B+D0XGlBPm3oopSrXYGvNLoCHUD1z33efemSMxdaJYAidotvjcZsMCBUPGTplkpZg8uxp3/KVUlpgxYbHjYz0l3ih5kQA9m2Eud0J9OK3Mr4ZvdiIwV7RCq2o96DB9T2Yu8W9LbYQubOjZJTLezNOYC+ZYmQKQAI4IZ3GkYN2IHCaQAA==";

        final var hexPublicKey = Hex.toHexString(Base64.decode(EphemeralPublicKeyBase64));
        final var hexExpectedCommand = Hex.toHexString(Base64.decode(WrappedCommandBase64));
        final var hexExpectedWrappedRbResult = Hex.toHexString(Base64.decode(WrappedResponseBase64));

        System.out.println("Z                  :" + hexPublicKey);
        System.out.println("hexExpectedCommand :" + hexExpectedCommand);

        try {
            //Setup MessageCipherInfo
            final var mca = new MessageCipherInfo();
            final var rdeInfo = new RdeMessageDecryptionInfo();
            mca.setRdeInfo(rdeInfo);
            rdeInfo.setPcdPublicKey(hexPublicKey);
            rdeInfo.setCommand(hexExpectedCommand);
            //rdeInfo.setDocumentDisplayName("SPEC2014");

            final var response = getApduResponseForDecryption(SPEC2014BacKey, tag, mca);

            var hexActualRbWrappedResult = Hex.toHexString(response);

            System.out.println("EXPECTED Response  :" + hexExpectedWrappedRbResult);
            System.out.println("ACTUAL   Response  :" + hexActualRbWrappedResult);

            if (hexExpectedWrappedRbResult.equals(hexActualRbWrappedResult))
                System.out.println(">>>> PASS");
            else
                System.out.println("FAIL!");
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CardServiceException e) {
            e.printStackTrace();
        } catch (GeneralRdeException e) {
            e.printStackTrace();
        }
    }

    private static byte[] getApduResponseForDecryption(final BACKey bacKey, final Tag tag, final MessageCipherInfo mca) throws IOException, CardServiceException, GeneralSecurityException, GeneralRdeException {
        byte[] dg14content;
        try (final var doc = new AndroidRdeDocument()) {
            doc.open(tag, bacKey);
            dg14content = doc.getFileContent(14);
        }
        try (final var doc = new AndroidRdeDocument()) {
            doc.open(tag, bacKey);
            return doc.getApduResponseForDecryption(mca, dg14content);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        var nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
        if (nfcAdapter == null)
            // nfc not available on the device
            return;

        if (!nfcAdapter.isEnabled()) {
            nfcSettingsLauncher.launch(new Intent(Settings.ACTION_NFC_SETTINGS));
            return;
        }

        var intent = new Intent(getApplicationContext(), nl.rijksoverheid.rdw.rde.client.activities.TestCrossPlatformDecryptionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        var pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, new String[][]{new String[]{"android.nfc.tech.IsoDep"}});
    }
}
