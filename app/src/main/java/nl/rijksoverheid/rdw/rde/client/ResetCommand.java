package nl.rijksoverheid.rdw.rde.client;

import android.app.Activity;

public class ResetCommand {
    public void execute(Activity activity)
    {
        final var sp = new AppSharedPreferences(activity);
        sp.clear();
    }
}
