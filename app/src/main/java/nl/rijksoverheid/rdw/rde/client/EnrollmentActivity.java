package nl.rijksoverheid.rdw.rde.client;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

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

        clickButton.setOnClickListener(v ->
        {
            final var intent = new Intent(getApplicationContext(), EnrollmentReadDocumentActivity.class);
            final var bacKeyStorage = new BacKeyStorage();
            bacKeyStorage.setValue(
                editTextDocumentId.getText().toString(),
                editTextDob.getText().toString(),
                editTextDocDoe.getText().toString()
            );
            bacKeyStorage.write(intent);

            intent.putExtra(EnrollmentReadDocumentActivity.DISPLAY_NAME_EXTRA_TAG, editTextDisplayName.getText().toString());

            startActivity(intent);
        });
    }
}