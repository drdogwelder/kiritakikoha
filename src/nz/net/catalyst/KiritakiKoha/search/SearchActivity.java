package nz.net.catalyst.KiritakiKoha.search;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nz.net.catalyst.KiritakiKoha.EditPreferences;
import nz.net.catalyst.KiritakiKoha.GlobalResources;
import nz.net.catalyst.KiritakiKoha.log.LogConfig;
import nz.net.catalyst.KiritakiKoha.R;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class SearchActivity extends ListActivity implements OnClickListener  {
	static final String TAG = LogConfig.getLogTag(SearchActivity.class);
	// whether DEBUG level logging is enabled (whether globally, or explicitly
	// for this log tag)
	static final boolean DEBUG = LogConfig.isDebug(TAG);
	// whether VERBOSE level logging is enabled
	static final boolean VERBOSE = LogConfig.VERBOSE;
	
	// application preferences
	private SharedPreferences mPrefs;

    List<Article> items = new ArrayList<Article>();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        setContentView(R.layout.search);
        
        // Set up click handlers for the text field and button
        ((Button) this.findViewById(R.id.btnGo)).setOnClickListener(this);
        ((Button) this.findViewById(R.id.btnCancel)).setOnClickListener(this);
    }
    
	public void onClick(View v) {
		if (v.getId() == R.id.btnGo) {
			String mURL = mPrefs.getString(getResources().getString(R.string.pref_default_url_key).toString(),
											getResources().getString(R.string.default_url).toString());
			EditText mText;
			String qStr = "";
			int pos;

			String[] av = getResources().getStringArray(R.array.search_options_arrayValues);

			// allow for 3 fields 
			
	        mText = (EditText) this.findViewById(R.id.searchTerms1);
	        if ( mText.getText().toString().trim().length() > 0 ) {
				pos = ((Spinner) this.findViewById(R.id.spinner1)).getSelectedItemPosition();
				qStr = qStr + "&idx=" + av[pos] + "&q=" + Uri.encode(mText.getText().toString().trim());
	        }
	        mText = (EditText) this.findViewById(R.id.searchTerms2);
	        if ( mText.getText().toString().trim().length() > 0 ) {
				pos = ((Spinner) this.findViewById(R.id.spinner2)).getSelectedItemPosition();
				qStr = qStr + "&idx=" + av[pos] + "&q=" + Uri.encode(mText.getText().toString().trim());
	        }
	        mText = (EditText) this.findViewById(R.id.searchTerms3);
	        if ( mText.getText().toString().trim().length() > 0 ) {
				pos = ((Spinner) this.findViewById(R.id.spinner3)).getSelectedItemPosition();
				qStr = qStr + "&idx=" + av[pos] + "&q=" + Uri.encode(mText.getText().toString().trim());
	        }
	        // Finally add the querystring
	        mURL = mURL + qStr;
	        
			try {
				RSSHandler rh = new RSSHandler();
				Log.d(TAG, "URL = " + mURL);
				items = rh.getItems(this, new URL(mURL));
			} catch (MalformedURLException e) {
				Toast.makeText(this, "The URL you have entered is invalid.", Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Toast.makeText(this, "Failed to run search.", Toast.LENGTH_SHORT).show();
			}
			
			List<String> results = new ArrayList<String>();
			if ( ! items.isEmpty()) {

				for (Iterator<Article> it = items.iterator(); it.hasNext(); ) { 
					Article a = it.next();
					if ( a.title != null ) {
						results.add(a.title);
						//Log.d(TAG, "Adding result: " + a.title + " (" + a.description + ")");
					}
				}
			}

			if ( results.isEmpty()) {
				Log.d(TAG, "No items returned");
				results.add("Sorry, your search returned no results");
				//return; // nothing to do
			}
			
	        //Hide the soft keyboard
	        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mText.getWindowToken(), 0);

			ArrayAdapter<String> x = 
                new ArrayAdapter<String>(this, R.layout.search_row, results);
            setListAdapter(x);
            
		}
        // Hide the search form...
        this.findViewById(R.id.search_form).setVisibility(View.GONE);
		//Hide the info details
		this.findViewById(R.id.info).setVisibility(View.GONE);
        
	}
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if ( items.isEmpty())
			return;
		
		Intent d = new Intent(this, ItemDetailsActivity.class);
		
		//TODO gotta be a better way
		Article a = items.get(position);
		d.putExtra("title", a.title );
		if ( a.url.toString().trim().length() > 0 ) {
			d.putExtra("link", a.url.toString() );
			//Log.d(TAG, "- URL added to intent (" + a.url.toString() + ")");
		}

		if ( a.description.trim().length() > 0 ) {
			d.putExtra("description", a.description );
			//Log.d(TAG, "- description added to intent (" + a.description + ")");
		}
		
		// Start the details dialog and pass in the intent containing item details.
		startActivity(d);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		menu.add(0, GlobalResources.SEARCH, 1, R.string.menu_search).setIcon(android.R.drawable.ic_menu_search);
		menu.add(0, GlobalResources.PREFERENCES, 2, R.string.menu_preferences).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0, GlobalResources.INFO, 3, R.string.menu_info).setIcon(android.R.drawable.ic_menu_info_details);
		return result;
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
        if ( this.findViewById(R.id.search_form).getVisibility() == View.GONE ) {
        	menu.findItem(GlobalResources.SEARCH).setEnabled(true);
        }
		return super.onPrepareOptionsMenu(menu);
	}
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case GlobalResources.PREFERENCES:
				Intent i = new Intent(this, EditPreferences.class);
				startActivity(i);
				break;
			case GlobalResources.SEARCH:
				this.findViewById(R.id.search_form).setVisibility(View.VISIBLE);
				//Hide the info details
				this.findViewById(R.id.info).setVisibility(View.GONE);
				break;
			case GlobalResources.INFO:
				this.findViewById(R.id.search_form).setVisibility(View.GONE);
				//Hide the info details
				this.findViewById(R.id.info).setVisibility(View.VISIBLE);
				break;
		}
		return true;
	}
}