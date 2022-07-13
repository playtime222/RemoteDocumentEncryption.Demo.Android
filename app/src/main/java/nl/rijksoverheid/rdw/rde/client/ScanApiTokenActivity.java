package nl.rijksoverheid.rdw.rde.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;

import java.util.ArrayList;
import java.util.List;


public class ScanApiTokenActivity extends AppCompatActivity {
        public static final String API_TOKEN_EXTRA_TAG = "ApiToken";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_scan_token);
            final TextView textView = findViewById(R.id.textView);
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
                //var token = parseResult(result.getContents());
                var token = result.getContents();
                final var intent = new Intent(getApplicationContext(), MessagesListActivity.class);
                intent.putExtra(API_TOKEN_EXTRA_TAG, token);
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