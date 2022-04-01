package nl.rijksoverheid.rdw.rde.client;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SimpleArrayAdapter extends ArrayAdapter<MessageMetadata>
{
    private final Context context;
    private final MessageMetadata[] values;

    public SimpleArrayAdapter(final Context context, final MessageMetadata[] values)
    {
        super(context, -1, values);

        if (context == null)
            throw new IllegalArgumentException();

        if (values == null)
            throw new IllegalArgumentException();

        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent)
    {
        //Unused parameters not checked.

        final var inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final var rowView = inflater.inflate(R.layout.message_list_item_view, parent, false);
        final var textViewId = (TextView) rowView.findViewById(R.id.messageId);
        final var textViewWhenSent = (TextView) rowView.findViewById(R.id.whenSent);
        final var textViewWhoFrom = (TextView) rowView.findViewById(R.id.whoFrom);
        final var textViewShortNote = (TextView) rowView.findViewById(R.id.shortNote);

        textViewId.setText(String.valueOf(values[position].getId()));
        textViewWhenSent.setText(String.valueOf(values[position].getWhenSent()));
        textViewWhoFrom.setText(String.valueOf(values[position].getWhoFrom()));
        textViewShortNote.setText(String.valueOf(values[position].getShortNote()));

        return rowView;
    }
}
