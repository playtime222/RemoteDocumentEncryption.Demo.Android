package nl.rijksoverheid.rdw.rde.client;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

import nl.rijksoverheid.rdw.rde.client.activities.EnrollmentActivity;
import nl.rijksoverheid.rdw.rde.client.activities.MainActivity;
import nl.rijksoverheid.rdw.rde.client.activities.MessagesListActivity;
import nl.rijksoverheid.rdw.rde.client.activities.ScanApiTokenActivity;

public class MenuItemHandler {
    public boolean onOptionsItemSelected(MenuItem item, Activity currentActivity) {
        switch (item.getItemId()) {
            case R.id.action_link_to_server: {
                final var intent = new Intent(currentActivity.getApplicationContext(), ScanApiTokenActivity.class);
                currentActivity.startActivity(intent);
                return true;
            }
            case R.id.action_enrol_mrtd: {
                final var intent = new Intent(currentActivity.getApplicationContext(), EnrollmentActivity.class);
                currentActivity.startActivity(intent);
                return true;
            }
            case R.id.action_show_message_list: {
                final var intent = new Intent(currentActivity.getApplicationContext(), MessagesListActivity.class);
                currentActivity.startActivity(intent);
                return true;
            }
            case R.id.action_use_speci2014_mrz: {
                final var sp = new AppSharedPreferences(currentActivity);
                sp.useSpec2014();
               return true;
            }
            case R.id.action_clear_stored_mrz: {
                final var sp = new AppSharedPreferences(currentActivity);
                sp.clearMrz();
                return true;
            }
            case R.id.action_clear_api_token: {
                final var sp = new AppSharedPreferences(currentActivity);
                sp.clearApiToken();
                final var intent = new Intent(currentActivity.getApplicationContext(), MainActivity.class);
                currentActivity.startActivity(intent);
                return true;
            }
            default:
                return false;
        }

    }
}
