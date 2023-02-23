package nl.rijksoverheid.rdw.rde.client.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import nl.rijksoverheid.rdw.rde.client.AppSharedPreferences;
import nl.rijksoverheid.rdw.rde.client.MenuItemHandler;
import nl.rijksoverheid.rdw.rde.client.R;

public class EnterMrzActivity extends AppCompatActivity
{
    ActivityResultLauncher<Intent> nfcSettingsLauncher;
    private EditText editTextDocumentId;
    private EditText editTextDob;
    private EditText editTextDocDoe;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        nfcSettingsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->
        {
            // nothing to do
        });

        var message = getIntent().getLongExtra(DecryptMessageActivity.DECRYPT_MESSAGE_ID, -1);

        getSupportActionBar().setTitle(R.string.appbar_title);
        getSupportActionBar().setSubtitle("Decrypting - Enter MRZ");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_enter_mrz);

        final var clickButton = (Button) findViewById(R.id.buttonEnroll);

        editTextDocumentId = findViewById(R.id.editTextDocumentId);
        editTextDob = findViewById(R.id.editTextDob);
        editTextDocDoe = findViewById(R.id.editTextDocDoe);

        final var storedBacKey = new AppSharedPreferences(this).readBacKey();

        editTextDocumentId.setText(storedBacKey.DocId);
        editTextDob.setText(storedBacKey.Dob);
        editTextDocDoe.setText(storedBacKey.Expiry);

        clickButton.setOnClickListener(v ->
        {
            storedBacKey.DocId = editTextDocumentId.getText().toString();
            storedBacKey.Dob = editTextDob.getText().toString();
            storedBacKey.Expiry = editTextDocDoe.getText().toString();

            if (!storedBacKey.isComplete()) {
                System.out.println("BAC Key is not complete");
                return;
            }

            final var sp = new AppSharedPreferences(this);
            sp.write(storedBacKey);


            final var intent = new Intent(getApplicationContext(), DecryptMessageActivity.class);
            intent.putExtra(DecryptMessageActivity.DECRYPT_MESSAGE_ID, message);
            startActivity(intent);
        });
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



}