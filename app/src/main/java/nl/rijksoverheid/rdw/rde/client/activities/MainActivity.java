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
//        Debug.startMethodTracing();
//        Log.println(Log.INFO, App.LogTag, "MainActivity start.");
//        System.out.println("MainActivity start.");
        super.onCreate(savedInstanceState);

        final var sp = new AppSharedPreferences(this);
        var apiToken = sp.readApiToken();
        if (apiToken == null || apiToken.getAuthToken() == null || apiToken.getAuthToken().isEmpty()) {
            final var intent = new Intent(getApplicationContext(), ScanApiTokenActivity.class);
//            Log.println(Log.INFO, App.LogTag, "Navigating to ScanApiTokenActivity.");
            startActivity(intent);
            return;
        }

        final var intent = new Intent(getApplicationContext(), MessagesListActivity.class);
//        Log.println(Log.INFO, App.LogTag, "Navigating to MessagesListActivity.");
        startActivity(intent);
        return;
    }
}