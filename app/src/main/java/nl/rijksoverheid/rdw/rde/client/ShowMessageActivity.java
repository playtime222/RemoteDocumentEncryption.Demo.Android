package nl.rijksoverheid.rdw.rde.client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ShowMessageActivity extends AppCompatActivity
{
    public static final String ExtraTag = "SIMPLE_MESSAGE";

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_message);

        final var message = (SimpleDecryptedMessage)getIntent().getExtras().getParcelable(ExtraTag);



        final var whenSentTextView = (TextView) findViewById(R.id.textViewWhenSent);
        final var textViewWhoFrom = (TextView) findViewById(R.id.textViewWhoFrom);
        final var textViewShortNote = (TextView) findViewById(R.id.textViewShortNote);
        final var textViewFileContent = (TextView) findViewById(R.id.textViewFileContent);

        whenSentTextView.setText(message.getWhenSent());
        textViewWhoFrom.setText(message.getWhoFrom());
        textViewShortNote.setText(message.getShortNote());
        textViewFileContent.setText(message.getFile1Text());
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
    }
}