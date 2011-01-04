package nz.net.catalyst.KiritakiKoha.hold;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import nz.net.catalyst.KiritakiKoha.R;
import nz.net.catalyst.KiritakiKoha.Record;
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


 */

public class PlaceHoldFormActivity extends Activity implements OnClickListener {
	static final String TAG = LogConfig.getLogTag(PlaceHoldFormActivity.class);
	// whether DEBUG level logging is enabled (whether globally, or explicitly for this log tag)
	static final boolean DEBUG = LogConfig.isDebug(TAG);
	// whether VERBOSE level logging is enabled
	static final boolean VERBOSE = LogConfig.VERBOSE;
	
	// application preferences
	private SharedPreferences mPrefs;
	
	private Record bib;
	private Bundle m_extras;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.place_hold);
        
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        m_extras = getIntent().getExtras();
        if (m_extras == null) {
			Toast.makeText(this, getString(R.string.place_hold_on_nothing), Toast.LENGTH_SHORT).show();
        	finish();
        	return;
        }
        	
    	if ( ! ( m_extras.containsKey("bib") ) ) {
			Toast.makeText(this, getString(R.string.place_hold_on_nothing), Toast.LENGTH_SHORT).show();
        	finish();
        	return;
    	}
    	Record bib = m_extras.getParcelable("bib");

        ((Button) this.findViewById(R.id.btnHoldGo)).setOnClickListener(this);
        ((TextView) this.findViewById(R.id.title)).setText(bib.getTitle());
    }
    
	public boolean onSearchRequested() {
		finish();
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.btnHoldGo) {
			
			String patronID = mPrefs.getString(getResources().getString(R.string.pref_patron_id_key).toString(), null);
			// check for a Parton ID .. if none - can't place hold.
			if ( patronID == null ) {
			//mURL = new URL(getString(R.string.base_url) + getString(R.string.placehold_url));
				Log.d(TAG, "No ptron ID - not logged in, can't place a hold");
				Toast.makeText(this, getResources().getString(R.string.place_hold_not_logged_in), 
										Toast.LENGTH_SHORT).show();
				return;
			}
			
			// TODO Else do it
			Toast.makeText(this, "Sorry this functionality does not exist yet", 
					Toast.LENGTH_SHORT).show();
		}
	}
}
