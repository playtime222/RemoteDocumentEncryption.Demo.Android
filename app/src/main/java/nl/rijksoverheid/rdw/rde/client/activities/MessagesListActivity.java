package nl.rijksoverheid.rdw.rde.client.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import nl.rijksoverheid.rdw.rde.client.AppSharedPreferences;
import nl.rijksoverheid.rdw.rde.client.Mapper;
import nl.rijksoverheid.rdw.rde.client.MessageMetadata;
import nl.rijksoverheid.rdw.rde.client.R;
import nl.rijksoverheid.rdw.rde.client.SimpleArrayAdapter;
import nl.rijksoverheid.rdw.rde.client.lib.HttpResponse;
import nl.rijksoverheid.rdw.rde.client.lib.RdeServerProxy;
import nl.rijksoverheid.rdw.rde.client.lib.data.ReceivedMessageList;

public class MessagesListActivity extends Activity implements AdapterView.OnItemClickListener
{
    /** Called when the activity is first created. */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_messages);

        final HttpResponse<ReceivedMessageList> messageListResult;
        try {
            final var sp = new AppSharedPreferences(this);
            final var servicesToken = sp.readApiToken();
            messageListResult = new RdeServerProxy().getMessages(servicesToken);
        } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
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


