package nl.rijksoverheid.rdw.rde.client;

import android.content.Intent;
import android.content.SharedPreferences;

import org.jmrtd.BACKey;

public class BacKeyStorage
{
    public static final String MRZ_ID_EXTRA_TAG = "MRZ.DocId";
    public static final String MRZ_EXPIRY_EXTRA_TAG = "MRZ.Expiry";
    public static final String MRZ_DOB_EXTRA_TAG = "MRZ.DOB";

    public final String DOCUMENT_NUMBER = "SPECI2014";
    public final String DATE_OF_BIRTH = "650310";
    public final String DATE_OF_EXPIRY = "240309";

    private String id;
    private String dob;
    private String expiry;

    public BACKey getValue()
    {
        return new BACKey(
                DOCUMENT_NUMBER,
                DATE_OF_BIRTH,
                DATE_OF_EXPIRY);
    }

    //TODO Validate
    public void setValue(final String id, final String dob, final String expiry)
    {
        if (id == null)
            throw new IllegalArgumentException();

        if (dob == null)
            throw new IllegalArgumentException();

        if (expiry == null)
            throw new IllegalArgumentException();


        this.id = id;
        this.dob = dob;
        this.expiry = expiry;
    }

    public void write(final Intent intent)
    {
        if (intent == null)
            throw new IllegalArgumentException();

        intent.putExtra(MRZ_ID_EXTRA_TAG, id);
        intent.putExtra(MRZ_DOB_EXTRA_TAG, dob);
        intent.putExtra(MRZ_EXPIRY_EXTRA_TAG, expiry);
    }

    public void read(final Intent intent)
    {
        if (intent == null)
            throw new IllegalArgumentException();

        id = intent.getStringExtra(MRZ_ID_EXTRA_TAG);
        dob = intent.getStringExtra(MRZ_DOB_EXTRA_TAG);
        expiry = intent.getStringExtra(MRZ_EXPIRY_EXTRA_TAG);
    }

    public void write(final SharedPreferences sharedPrefs)
    {
        if (sharedPrefs == null)
            throw new IllegalArgumentException();

        final var writer = sharedPrefs.edit();
        writer.putString(MRZ_ID_EXTRA_TAG, id);
        writer.putString(MRZ_DOB_EXTRA_TAG, dob);
        writer.putString(MRZ_EXPIRY_EXTRA_TAG, expiry);
        writer.apply();
    }

    public void read(final SharedPreferences sharedPrefs)
    {
        if (sharedPrefs == null)
            throw new IllegalArgumentException();

        id = sharedPrefs.getString(MRZ_ID_EXTRA_TAG, null);
        dob = sharedPrefs.getString(MRZ_DOB_EXTRA_TAG, null);
        expiry = sharedPrefs.getString(MRZ_EXPIRY_EXTRA_TAG, null);
    }

}
