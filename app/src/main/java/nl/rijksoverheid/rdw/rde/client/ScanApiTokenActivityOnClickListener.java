package nl.rijksoverheid.rdw.rde.client;

import android.view.View;

import com.google.zxing.integration.android.IntentIntegrator;

import java.util.ArrayList;

public class ScanApiTokenActivityOnClickListener implements View.OnClickListener {
    ScanApiTokenActivity activity;

    public ScanApiTokenActivityOnClickListener(ScanApiTokenActivity activity)
    {
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {
        final var intentIntegrator = new IntentIntegrator(activity);
        var list = new ArrayList<String>();
        list.add(IntentIntegrator.QR_CODE);
        intentIntegrator.setDesiredBarcodeFormats(list);
        intentIntegrator.initiateScan();
    }
}
