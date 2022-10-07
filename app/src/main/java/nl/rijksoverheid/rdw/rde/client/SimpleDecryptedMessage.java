package nl.rijksoverheid.rdw.rde.client;

import android.os.Parcel;
import android.os.Parcelable;

public class SimpleDecryptedMessage implements Parcelable
{
    private long id;
    private String whoFrom;
    private String whenSent; //ISO sortable datetime
    private String shortNote;
    private String file1Name;
    private String file1Text;

    public SimpleDecryptedMessage()
    {
    }

    public long getId()
    {
        return id;
    }

    public void setId(final long id)
    {
        this.id = id;
    }

    public String getWhoFrom()
    {
        return whoFrom;
    }

    public void setWhoFrom(final String whoFrom)
    {
        this.whoFrom = whoFrom;
    }

    public String getWhenSent()
    {
        return whenSent;
    }

    public void setWhenSent(final String whenSent)
    {
        this.whenSent = whenSent;
    }

    public String getShortNote()
    {
        return shortNote;
    }

    public void setShortNote(final String shortNote)
    {
        this.shortNote = shortNote;
    }

    public String getFile1Text()
    {
        return file1Text;
    }

    public void setFile1Text(final String file1Text)
    {
        this.file1Text = file1Text;
    }

    // Parcelling part
    public SimpleDecryptedMessage(final Parcel in)
    {
        if (in  == null)
            throw new IllegalArgumentException();

        final var data = in.createStringArray();

        if (data.length != 6)
            return;

        this.id = Integer.parseInt(data[0]);
        this.whenSent = data[1];
        this.whoFrom = data[2];
        this.shortNote = data[3];
        this.file1Name = data[4];
        this.file1Text = data[5];
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {

        if (dest  == null)
            throw new IllegalArgumentException();

        dest.writeStringArray(new String[] {
                ""+id,
                whoFrom,
                whenSent,
                shortNote,
                file1Name,
                file1Text
        });
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public SimpleDecryptedMessage createFromParcel(final Parcel in) {
            if (in == null)
                throw new IllegalArgumentException();

            return new SimpleDecryptedMessage(in);
        }

        public SimpleDecryptedMessage[] newArray(final int size) {
            return new SimpleDecryptedMessage[size];
        }
    };

    public String getFile1Name() {
        return file1Name;
    }

    public void setFile1Name(String file1Name) {
        this.file1Name = file1Name;
    }
}
