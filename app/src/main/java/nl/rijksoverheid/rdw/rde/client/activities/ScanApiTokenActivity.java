package nl.rijksoverheid.rdw.rde.client.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;

import nl.rijksoverheid.rdw.rde.client.AppSharedPreferences;
import nl.rijksoverheid.rdw.rde.client.R;
import nl.rijksoverheid.rdw.rde.client.ScanApiTokenActivityOnClickListener;


public class ScanApiTokenActivity extends AppCompatActivity {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_scan_token);
            //final TextView textView = findViewById(R.id.textView);
            final ImageButton qrButton = findViewById(R.id.qr_button);
            qrButton.setOnClickListener(new ScanApiTokenActivityOnClickListener(this));
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            super.onActivityResult(requestCode, resultCode, data);
            var result = IntentIntegrator.parseActivityResult(resultCode, data);
            if (result != null)
            {
                final var token = result.getContents();
                final var sp = new AppSharedPreferences(this);
                sp.writeApiToken(token);

                final var intent = new Intent(getApplicationContext(), EnrollmentActivity.class);
                startActivity(intent);
            }
    }

//    public static String parseResult(final String scanResult)
//    {
//        var lines = scanResult.split("$");
//
//        for (var line:lines)
//        {
//            if (line.startsWith("Contents:"))
//                return line.substring("Contents:".length());
//        }
//
//        return "No token";
//    }
}