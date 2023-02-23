package nl.rijksoverheid.rdw.rde.client.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;

import nl.rijksoverheid.rdw.rde.client.AppSharedPreferences;
import nl.rijksoverheid.rdw.rde.client.MenuItemHandler;
import nl.rijksoverheid.rdw.rde.client.R;
import nl.rijksoverheid.rdw.rde.client.ScanApiTokenActivityOnClickListener;


public class ScanApiTokenActivity extends AppCompatActivity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.appbar_title);
        getSupportActionBar().setSubtitle("Scan API Token");

        setContentView(R.layout.activity_scan_token);
        final Button qrButton = findViewById(R.id.qr_button);
        qrButton.setOnClickListener(v -> {
            new ScanApiTokenActivityOnClickListener(this).onClick(v);
        });

//        final ViewGroup getViewGroup = (ViewGroup) ((ViewGroup)
//                this.findViewById(android.R.id.content)).getChildAt(0);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (new MenuItemHandler().onOptionsItemSelected(item, this))
            return true;

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        var result = IntentIntegrator.parseActivityResult(resultCode, data);
        if (result != null) {
            final var token = result.getContents();
            final var sp = new AppSharedPreferences(this);
            sp.writeApiToken(token);

            final var intent = new Intent(getApplicationContext(), MessagesListActivity.class);
            startActivity(intent);
        }
    }
}