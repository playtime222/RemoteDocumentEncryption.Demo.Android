package nl.rijksoverheid.rdw.rde.client.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import nl.rijksoverheid.rdw.rde.client.AppSharedPreferences;
import nl.rijksoverheid.rdw.rde.client.R;

public class EnrollmentActivity extends AppCompatActivity
{
    ActivityResultLauncher<Intent> nfcSettingsLauncher;
    private EditText editTextDocumentId;
    private EditText editTextDob;
    private EditText editTextDocDoe;
    private EditText editTextDisplayName;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        nfcSettingsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->
        {
            // nothing to do
        });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_enroll);

        final var clickButton = (Button) findViewById(R.id.buttonEnroll);

        editTextDisplayName = findViewById(R.id.editTextDisplayName);
        editTextDocumentId = findViewById(R.id.editTextDocumentId);
        editTextDob = findViewById(R.id.editTextDob);
        editTextDocDoe = findViewById(R.id.editTextDocDoe);

        final var intent = new Intent(getApplicationContext(), EnrollmentReadDocumentActivity.class);

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
            sp.writeDocumentDisplayName(editTextDisplayName.getText().toString());

            startActivity(intent);
        });
    }
}