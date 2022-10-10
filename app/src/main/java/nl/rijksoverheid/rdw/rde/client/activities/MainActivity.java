package nl.rijksoverheid.rdw.rde.client.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import nl.rijksoverheid.rdw.rde.client.AppSharedPreferences;

//Routes to the correct starting activity
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final var sp = new AppSharedPreferences(this);
        var apiToken = sp.readApiToken();
        if (apiToken == null || apiToken.getAuthToken() == null || apiToken.getAuthToken().isEmpty()) {
            final var intent = new Intent(getApplicationContext(), ScanApiTokenActivity.class);
            startActivity(intent);
            return;
        }

        final var intent = new Intent(getApplicationContext(), MessagesListActivity.class);
        startActivity(intent);
        return;
    }
}