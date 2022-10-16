package nl.rijksoverheid.rdw.rde.client.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import nl.rijksoverheid.rdw.rde.client.AppSharedPreferences;
import nl.rijksoverheid.rdw.rde.client.Mapper;
import nl.rijksoverheid.rdw.rde.client.MenuItemHandler;
import nl.rijksoverheid.rdw.rde.client.MessageMetadata;
import nl.rijksoverheid.rdw.rde.client.R;
import nl.rijksoverheid.rdw.rde.client.SimpleArrayAdapter;
import nl.rijksoverheid.rdw.rde.client.activities.Errors.ShowErrorActivity;
import nl.rijksoverheid.rdw.rde.client.lib.HttpResponse;
import nl.rijksoverheid.rdw.rde.client.lib.RdeServerProxy;
import nl.rijksoverheid.rdw.rde.client.lib.data.ReceivedMessageList;

public class MessagesListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener
{
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
        getSupportActionBar().setSubtitle("Received Messages");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_messages);

        HttpResponse<ReceivedMessageList> messageListResult = null;
        try {
            final var sp = new AppSharedPreferences(this);
            final var servicesToken = sp.readApiToken();
            messageListResult = new RdeServerProxy().getMessages(servicesToken);
        } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();

            if (messageListResult == null)
                ShowErrorActivity.show("Could not contact server or obtain message list.", this);
            else
                ShowErrorActivity.show("Could not contact server or obtain message list: " + messageListResult.getCode() + "/" + messageListResult.getMessage(), this);

            return;
        }

        final var refreshButton = (Button)findViewById(R.id.buttonRefresh);
        refreshButton.setOnClickListener(v -> {
                //Just do it again
                var intent = new Intent(getApplicationContext(), MessagesListActivity.class);
                startActivity(intent);
        });

        final ListView listView = findViewById(R.id.messageItems);
        listView.setOnItemClickListener(this);
        final var items = Arrays.stream(messageListResult.getData().getItems()).map(Mapper::map).toArray(MessageMetadata[]::new);

        System.out.println("Messages found: " + items.length);

        final var adaptor = new SimpleArrayAdapter(listView.getContext(), items);
        listView.setAdapter(adaptor);
    }

    /*
     * Parameters:
		adapter - The AdapterView where the click happened.
		view - The view within the AdapterView that was clicked
		position - The position of the view in the adapter.
		id - The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id)
    {
//        Toast.makeText(getApplicationContext(),
//                ((TextView) view).getText(),
//                Toast.LENGTH_SHORT).show();

        if (adapter == null)
            throw new IllegalArgumentException();

        if (view == null)
            throw new IllegalArgumentException();

        final var itemAtPosition = adapter.getItemAtPosition(position);
        final var intent = new Intent(getApplicationContext(), DecryptMessageActivity.class);
        intent.putExtra(DecryptMessageActivity.DECRYPT_MESSAGE_ID, ((MessageMetadata)itemAtPosition).getId());
        MessagesListActivity.this.startActivity(intent);
    }
}


