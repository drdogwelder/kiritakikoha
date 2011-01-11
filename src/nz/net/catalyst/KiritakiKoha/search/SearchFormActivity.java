package nz.net.catalyst.KiritakiKoha.search;

import java.util.ArrayList;

import nz.net.catalyst.KiritakiKoha.EditPreferences;
import nz.net.catalyst.KiritakiKoha.Constants;
import nz.net.catalyst.KiritakiKoha.log.LogConfig;
import nz.net.catalyst.KiritakiKoha.R;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class SearchFormActivity extends Activity implements OnClickListener  {
	static final String TAG = LogConfig.getLogTag(SearchFormActivity.class);
	// whether DEBUG level logging is enabled (whether globally, or explicitly
	// for this log tag)
	static final boolean DEBUG = LogConfig.isDebug(TAG);
	// whether VERBOSE level logging is enabled
	static final boolean VERBOSE = LogConfig.VERBOSE;
	
	private void initiateScan (){
		try {
			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			intent.putExtra("SCAN_MODE", Constants.SEARCH_SCAN_MODE);
			startActivityForResult(intent, 0);
	    } catch (ActivityNotFoundException e) {
        	Toast.makeText(this, getResources().getString(R.string.scan_not_available), Toast.LENGTH_SHORT).show();
	    }
	}

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.search_form);
        
        // Set up click handlers for the text field and button
        ((Button) this.findViewById(R.id.btnSearchGo)).setOnClickListener(this);
    }
    public void onClick(View v) {
		if (v.getId() == R.id.btnSearchGo) {			
	        EditText mText;
			int pos;

	    	SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

			String[] av = getResources().getStringArray(R.array.search_options_arrayValues);
			ArrayList<String> idxValues = new ArrayList<String>();
			ArrayList<String> qValues = new ArrayList<String>();
			String pub_date_range;
			
			// allow for 3 fields - maybe make the form dynamic (auto new one if entering in one)
	        //TODO - maybe clean - improve the form element processing (bit cut-n-paste)
			
	        mText = (EditText) this.findViewById(R.id.searchTerms1);
	        if ( mText.getText().toString().trim().length() > 0 ) {
				pos = ((Spinner) this.findViewById(R.id.spinner1)).getSelectedItemPosition();				
				idxValues.add(av[pos]);
				qValues.add(mText.getText().toString().trim());
	        }
	        mText = (EditText) this.findViewById(R.id.searchTerms2);
	        if ( mText.getText().toString().trim().length() > 0 ) {
				pos = ((Spinner) this.findViewById(R.id.spinner2)).getSelectedItemPosition();
				idxValues.add(av[pos]);
				qValues.add(mText.getText().toString().trim());
	        }
	        mText = (EditText) this.findViewById(R.id.searchTerms3);
	        if ( mText.getText().toString().trim().length() > 0 ) {
				pos = ((Spinner) this.findViewById(R.id.spinner3)).getSelectedItemPosition();
				idxValues.add(av[pos]);
				qValues.add(mText.getText().toString().trim());
	        }
	        //limit-yr=1999-2000
	        mText = (EditText) this.findViewById(R.id.pub_date_range);
        	pub_date_range = mText.getText().toString().trim();
	        
			// Start the details dialog and pass in the intent containing item details.
	        
        	if ( ! ( idxValues.size() > 0 && qValues.size() > 0 ) ) {
    			Toast.makeText(this, getString(R.string.search_no_search_terms), Toast.LENGTH_SHORT).show();
        	} else {
            	
        		// Load up the search results intent
		        Intent d = new Intent(this, SearchResultsActivity.class);
				d.putStringArrayListExtra("idx", idxValues);
				d.putStringArrayListExtra("q", qValues);
				d.putExtra(Constants.SEARCH_PUB_DATE_RANGE_PARAM, pub_date_range);
				
				if ( mPrefs.getBoolean(getResources().getString(R.string.pref_limit_available_key).toString(), false) ) 
					d.putExtra(Constants.LIMIT_AVAILABLE,	"something-non-empty");
				startActivity(d);
        	}
		}
	}

	public boolean onSearchRequested() {
		initiateScan();
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		menu.add(Menu.NONE, Constants.SCAN, 1, R.string.menu_scan).setIcon(R.drawable.ic_menu_scan);
		menu.add(Menu.NONE, Constants.PREFERENCES, 2, R.string.menu_preferences).setIcon(android.R.drawable.ic_menu_preferences);
		return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
			case Constants.SCAN:
				initiateScan();
				break;				
			case Constants.PREFERENCES:
				startActivity(new Intent(this, EditPreferences.class));
				break;				
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}
	public void onActivityResult(int requestCode, int resultCode, Intent intent) { 
		
        if (resultCode == Activity.RESULT_OK) {
        	String contents = intent.getStringExtra("SCAN_RESULT");
        	String formatName = intent.getStringExtra("SCAN_RESULT_FORMAT");
        	
        	if ( DEBUG ) Log.d(TAG, "scanResult: " + contents + " (" + formatName + ")");
        	
        	EditText mText = (EditText) this.findViewById(R.id.searchTerms1);
        	mText.setText(contents);
        	
        	Spinner mSpinner = (Spinner) this.findViewById(R.id.spinner1);
			String[] ai = getResources().getStringArray(R.array.search_options_array);
			for ( int i=0 ; i < ai.length; i++ ) {
				if ( ai[i].equals(Constants.ISBN) ) 
		        	mSpinner.setSelection(i);
			}
		} 
	}
}