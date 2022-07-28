package nl.rijksoverheid.rdw.rde.client;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.jmrtd.BACKey;

public class AppSharedPreferences {

    private static final String Id = "THIS_APP";
    private final SharedPreferences sp;

    private static final String API_TOKEN = "ApiToken";
    private static final String DOC_DISPLAY_NAME = "DocDisplayName";

    public AppSharedPreferences(Context activity)
    {
        sp = activity.getSharedPreferences(Id, Activity.MODE_PRIVATE);
    }

    public String readApiToken()
    {
        return sp.getString(API_TOKEN, "Not a token...");
    }

    public void writeDocumentDisplayName(String value)
    {
        final var editor = sp.edit();
        editor.putString(DOC_DISPLAY_NAME, value);
        editor.commit();
    }

    public String readDocumentDisplayName()
    {
        return sp.getString(DOC_DISPLAY_NAME, "default");
    }

    public void writeApiToken(String value)
    {
        final var editor = sp.edit();
        editor.putString(API_TOKEN, value);
        editor.commit();
    }

    private final String MRZ_ID_EXTRA_TAG = "MRZ.DocId";
    private final String MRZ_EXPIRY_EXTRA_TAG = "MRZ.Expiry";
    private final String MRZ_DOB_EXTRA_TAG = "MRZ.DOB";

    public void write(BACKey value)
    {
        final var editor = sp.edit();
        editor.putString(MRZ_ID_EXTRA_TAG, value.getDocumentNumber());
        editor.putString(MRZ_DOB_EXTRA_TAG, value.getDateOfBirth());
        editor.putString(MRZ_EXPIRY_EXTRA_TAG, value.getDateOfExpiry());
        editor.commit();
    }

    public void write(StoredBacKey value)
    {
        final var editor = sp.edit();
        editor.putString(MRZ_ID_EXTRA_TAG, value.DocId);
        editor.putString(MRZ_DOB_EXTRA_TAG, value.Dob);
        editor.putString(MRZ_EXPIRY_EXTRA_TAG, value.Expiry);
        editor.commit();
    }

    public StoredBacKey readBacKey()
    {
        final var v1 = sp.getString(MRZ_ID_EXTRA_TAG, DOCUMENT_NUMBER);
        final var v2 = sp.getString(MRZ_DOB_EXTRA_TAG, DATE_OF_BIRTH);
        final var v3 = sp.getString(MRZ_EXPIRY_EXTRA_TAG, DATE_OF_EXPIRY);
        return new StoredBacKey(v1,v2,v3);
    }


    public final String DOCUMENT_NUMBER = "SPECI2014";
    public final String DATE_OF_BIRTH = "650310";
    public final String DATE_OF_EXPIRY = "240309";
}


