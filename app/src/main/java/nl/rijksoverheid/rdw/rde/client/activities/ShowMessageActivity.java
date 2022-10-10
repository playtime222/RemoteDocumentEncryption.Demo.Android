package nl.rijksoverheid.rdw.rde.client.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import nl.rijksoverheid.rdw.rde.client.MenuItemHandler;
import nl.rijksoverheid.rdw.rde.client.R;
import nl.rijksoverheid.rdw.rde.client.SimpleDecryptedMessage;

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
        final var textViewFile1Name = (TextView) findViewById(R.id.textViewFile1Name);
        final var textViewFile1Content = (TextView) findViewById(R.id.textViewFile1Content);

        whenSentTextView.setText(message.getWhenSent());
        textViewWhoFrom.setText(message.getWhoFrom());
        textViewShortNote.setText(message.getShortNote());
        textViewFile1Name.setText(message.getFile1Name());
        textViewFile1Content.setText(message.getFile1Text());
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
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