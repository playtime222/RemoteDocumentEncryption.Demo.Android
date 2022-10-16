package nl.rijksoverheid.rdw.rde.client.activities.Errors;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import nl.rijksoverheid.rdw.rde.client.MenuItemHandler;
import nl.rijksoverheid.rdw.rde.client.R;
import nl.rijksoverheid.rdw.rde.client.activities.MainActivity;

public class ShowErrorActivity extends AppCompatActivity
{
    public static final String ExtraTag = "ERROR_MESSAGE";

    public static void show(String message, Activity currentActivity) {
        var intent = new Intent(currentActivity.getApplicationContext(), ShowErrorActivity.class);
        intent.putExtra(ShowErrorActivity.ExtraTag, message);
        currentActivity.startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (new MenuItemHandler().onOptionsItemSelected(item, this))
            return true;

        return super.onOptionsItemSelected(item);
    }

    /** Called when the activity is first created. */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(final Bundle savedInstanceState) {

        getSupportActionBar().setTitle(R.string.appbar_title);
        getSupportActionBar().setSubtitle("ERROR!");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_nfc_connection_error);

        final var message = getIntent().getStringExtra(ShowErrorActivity.ExtraTag);
        final TextView errorMessageTextView = findViewById(R.id.error_message);
        errorMessageTextView.setText(message);

        final var refreshButton = (Button)findViewById(R.id.button);
        refreshButton.setOnClickListener(v -> {
                //Just do it again
                var intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
        });
    }
}


