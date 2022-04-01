package nl.rijksoverheid.rdw.rde.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.RequiresApi;

import java.util.Arrays;

import nl.rijksoverheid.rdw.rde.client.lib.RdeServerProxy;
import nl.rijksoverheid.rdw.rde.apdusimulator.*;
import nl.rijksoverheid.rdw.rde.crypto.*;
import nl.rijksoverheid.rdw.rde.documents.*;
import nl.rijksoverheid.rdw.rde.remoteapi.*;
import nl.rijksoverheid.rdw.rde.messaging.*;
import nl.rijksoverheid.rdw.rde.messaging.zipV2.*;
import nl.rijksoverheid.rdw.rde.mrtdfiles.*;

public class MessagesListActivity extends Activity implements AdapterView.OnItemClickListener
{
    /** Called when the activity is first created. */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_messages);

        final var bacKeyStorage = new BacKeyStorage();
        bacKeyStorage.read(getIntent());

        final var messageListResult = new RdeServerProxy().GetMessages();

//        var messageListResult = new MessageListResult();
//        messageListResult.setItems(new MessageInfoDto[]{
//                new MessageInfoDto(123, "2021-10-05 15:15", "Someone", "someone else", "unremarkable", "http://1"),
//                new MessageInfoDto(124, "2021-10-06 15:15", "Someone else", "don't care", "notable", "http://2"),
//        });

        final var refreshButton = (Button)findViewById(R.id.buttonRefresh);
        refreshButton.setOnClickListener(v -> {
            //Just do it again
                var intent = new Intent(getApplicationContext(), MessagesListActivity.class);
                startActivity(intent);
        });

        final ListView listView = findViewById(R.id.messageItems);
        listView.setOnItemClickListener(this);

        final var items = Arrays.stream(messageListResult.getItems()).map(Mapper::map).toArray(MessageMetadata[]::new);

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
        final var url = ((MessageMetadata)itemAtPosition).getUrl();
        final var intent = new Intent(getApplicationContext(), DecryptMessageActivity.class);
        intent.putExtra(DecryptMessageActivity.ExtraTag, url);
        MessagesListActivity.this.startActivity(intent);
    }
}


