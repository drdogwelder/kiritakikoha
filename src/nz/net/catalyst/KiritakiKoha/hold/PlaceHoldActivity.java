package nz.net.catalyst.KiritakiKoha.hold;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import nz.net.catalyst.KiritakiKoha.EditPreferences;
import nz.net.catalyst.KiritakiKoha.GlobalResources;
import nz.net.catalyst.KiritakiKoha.InfoActivity;
import nz.net.catalyst.KiritakiKoha.R;
import nz.net.catalyst.KiritakiKoha.authenticator.AuthenticatorActivity;
import nz.net.catalyst.KiritakiKoha.log.LogConfig;
/*
 * HoldTitle
	Creates, for a patron, a title-level hold request on a given bibliographic record in Koha.

	Parameters
		patron_id (Required) - the ILS identifier for the patron for whom the request is placed
		bib_id (Required)    - the ILS identifier for the bibliographic record on which the request is placed
		request_location (Required) - IP address where the end user request is being placed
		pickup_location (Optional) - an identifier indicating the location to which to deliver the item for pickup
		needed_before_date (Optional) - date after which hold request is no longer needed
		pickup_expiry_date (Optional) - date after which item returned to shelf if item is not picked up

Example Call

ilsdi.pl?service=HoldTitle&patron_id=1&bib_id=1&request_location=127.0.0.1
Example Response

<?xml version="1.0" encoding="ISO-8859-1" ?>
<HoldTitle>
  <title>(les) galères de l'Orfèvre</title>
  <date_available>2009-05-11</date_available>
  <pickup_location>Bibliothèque Jean-Prunier</pickup_location>
</HoldTitle>
 */
import nz.net.catalyst.KiritakiKoha.search.SearchFormActivity;

public class PlaceHoldActivity extends Activity{
	static final String TAG = LogConfig.getLogTag(PlaceHoldActivity.class);
	// whether DEBUG level logging is enabled (whether globally, or explicitly for this log tag)
	static final boolean DEBUG = LogConfig.isDebug(TAG);
	// whether VERBOSE level logging is enabled
	static final boolean VERBOSE = LogConfig.VERBOSE;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.place_hold);
    }
    
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		menu.add(0, GlobalResources.SEARCH, 1, R.string.menu_search).setIcon(android.R.drawable.ic_menu_search);
		menu.add(0, GlobalResources.PREFERENCES, 2, R.string.menu_preferences).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0, GlobalResources.INFO, 3, R.string.menu_info).setIcon(android.R.drawable.ic_menu_info_details);
		return result;
	}
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case GlobalResources.PREFERENCES:
				startActivity(new Intent(this, EditPreferences.class));
				break;
			case GlobalResources.SEARCH:
				finish();
				break;
			case GlobalResources.INFO:
				startActivity(new Intent(this, InfoActivity.class));
				break;
		}
		return true;
	}
	public boolean onSearchRequested() {
		finish();
		return true;
	}

}
