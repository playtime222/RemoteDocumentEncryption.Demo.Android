package nl.rijksoverheid.rdw.rde.clientlib;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import nl.rijksoverheid.rdw.rde.client.ScanApiTokenActivity;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("nl.rijksoverheid.rdw.rde", appContext.getPackageName());
    }

//    @Test
//    public void ParseQrScanrResult() {
//
//        final var result = "Format: QR_CODE\n" +
//                "Contents: a10d71d35e36e866c3e2e894d8f4f571da2de37b5896e5e2f09355aa1b3a2900\n" +
//                "Raw bytes: (80 bytes)\n" +
//                "Orientation: null\n" +
//                "EC level: L\n" +
//                "Barcode image: null\n" +
//                "Original intent: Intent { act=com.google.zxing.client.android.SCAN flg=0x80000 (has extras) }";
//
//        final var expected = "a10d71d35e36e866c3e2e894d8f4f571da2de37b5896e5e2f09355aa1b3a2900";
//
//        assertEquals(expected, ScanApiTokenActivity.parseResult(result).trim());
//    }
}