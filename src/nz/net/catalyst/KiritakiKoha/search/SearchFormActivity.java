package nz.net.catalyst.KiritakiKoha.search;

import java.util.ArrayList;

import nz.net.catalyst.KiritakiKoha.EditPreferences;
import nz.net.catalyst.KiritakiKoha.GlobalResources;
import nz.net.catalyst.KiritakiKoha.InfoActivity;
import nz.net.catalyst.KiritakiKoha.log.LogConfig;
import nz.net.catalyst.KiritakiKoha.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.search_form);
        
        // Set up click handlers for the text field and button
        ((Button) this.findViewById(R.id.btnGo)).setOnClickListener(this);
    }
    private void enableSearchButton () {
		// Disable while we process
        ((Button) this.findViewById(R.id.btnGo)).setText(getString(R.string.search_form_go));
        ((Button) this.findViewById(R.id.btnGo)).setEnabled(true);    	
    }
    private void disableSearchButton () {
		// Disable while we process
        ((Button) this.findViewById(R.id.btnGo)).setText(getString(R.string.search_form_searching));
        ((Button) this.findViewById(R.id.btnGo)).setEnabled(false);
    }
    public void onClick(View v) {
		if (v.getId() == R.id.btnGo) {
			disableSearchButton();
	        
	        EditText mText;
			int pos;

			String[] av = getResources().getStringArray(R.array.search_options_arrayValues);
			ArrayList<String> idxValues = new ArrayList<String>();
			ArrayList<String> qValues = new ArrayList<String>();
			
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
			// Start the details dialog and pass in the intent containing item details.
	        
        	if ( ! ( idxValues.size() > 0 && qValues.size() > 0 ) ) {
    			Toast.makeText(this, getString(R.string.search_no_search_terms), Toast.LENGTH_SHORT).show();
        	} else {
        		// Load up the search results intent
		        Intent d = new Intent(this, SearchResultsActivity.class);
				d.putStringArrayListExtra("idx", idxValues);
				d.putStringArrayListExtra("q", qValues);
				startActivity(d);
        	}
        	
			enableSearchButton();
			v.invalidate();
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		menu.add(0, GlobalResources.PREFERENCES, 1, R.string.menu_preferences).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0, GlobalResources.INFO, 2, R.string.menu_info).setIcon(android.R.drawable.ic_menu_info_details);
		return result;
	}

	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case GlobalResources.PREFERENCES:
				startActivity(new Intent(this, EditPreferences.class));
				break;
			case GlobalResources.INFO:
				startActivity(new Intent(this, InfoActivity.class));
				break;
		}
		return true;
	}
}