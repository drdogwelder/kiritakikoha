package nz.net.catalyst.KiritakiKoha.search;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nz.net.catalyst.KiritakiKoha.EditPreferences;
import nz.net.catalyst.KiritakiKoha.GlobalResources;
import nz.net.catalyst.KiritakiKoha.InfoActivity;
import nz.net.catalyst.KiritakiKoha.log.LogConfig;
import nz.net.catalyst.KiritakiKoha.R;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SearchResultsActivity extends ListActivity {
	static final String TAG = LogConfig.getLogTag(SearchResultsActivity.class);
	// whether DEBUG level logging is enabled (whether globally, or explicitly
	// for this log tag)
	static final boolean DEBUG = LogConfig.isDebug(TAG);
	// whether VERBOSE level logging is enabled
	static final boolean VERBOSE = LogConfig.VERBOSE;
	
	// application preferences
	private SharedPreferences mPrefs;

    ArrayList<Article> items = new ArrayList<Article>();
	private Bundle m_extras;

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        setContentView(R.layout.search_results);
        
        m_extras = getIntent().getExtras();
        if (m_extras == null) {
			Toast.makeText(this, getString(R.string.search_bad_request), Toast.LENGTH_SHORT).show();
        	finish();
        	return;
        }
        	
    	if ( ! ( m_extras.containsKey("idx") && m_extras.containsKey("q") ) ) {
			Toast.makeText(this, getString(R.string.search_bad_request), Toast.LENGTH_SHORT).show();
        	finish();
        	return;
    	}

		ArrayList<String> idxValues = m_extras.getStringArrayList("idx");
		ArrayList<String> qValues = m_extras.getStringArrayList("q");
		
    	if ( ! ( idxValues.size() > 0 && qValues.size() > 0 ) ) {
			Toast.makeText(this, getString(R.string.search_no_search_terms), Toast.LENGTH_SHORT).show();
        	finish();
        	return;
    	}

		String mURL = mPrefs.getString(getResources().getString(R.string.pref_default_url_key).toString(),
										getResources().getString(R.string.default_url).toString());

		String qStr = "";
		Iterator<String> idxItr = idxValues.iterator(); 
		Iterator<String> qItr = qValues.iterator(); 
		while ( idxItr.hasNext() && qItr.hasNext() ) { 
			String q = qItr.next();
			String idx = idxItr.next();
			qStr = qStr + "&idx=" + idx + "&q=" + Uri.encode(q);
		}

			// Finally add the query string
        mURL = mURL + qStr;
        
		try {
			RSSHandler rh = new RSSHandler();
			Log.d(TAG, "URL = " + mURL);
			items = rh.getItems(this, new URL(mURL));
		} catch (MalformedURLException e) {
			Log.e(TAG, "Malfomed URL: " + e.getMessage());
			Toast.makeText(this, getString(R.string.search_url_invalid), Toast.LENGTH_SHORT).show();
        	finish();
        	return;
		} catch (IOException e) {
			Log.e(TAG, "Connection error: " + e.getMessage());
			Toast.makeText(this, getString(R.string.search_connection_failure), Toast.LENGTH_SHORT).show();
        	finish();
        	return;
		}

		if ( items.isEmpty()) {
			Toast.makeText(this, getString(R.string.search_no_results), Toast.LENGTH_SHORT).show();
        	finish();
        	return;
		}
		
		List<String> results = new ArrayList<String>();
		if ( ! items.isEmpty()) {

			for (Iterator<Article> it = items.iterator(); it.hasNext(); ) { 
				Article a = it.next();
				if ( a.title != null ) 
					results.add(a.title);
			}
		}

		if ( results.isEmpty()) {
			Toast.makeText(this, getString(R.string.search_no_results), Toast.LENGTH_SHORT).show();
        	finish();
        	return;
		}
		
		ArrayAdapter<String> x = 
            new ArrayAdapter<String>(this, R.layout.search_results_row, results);
        setListAdapter(x);
        
		if ( items.size() == 1)
			loadItem(0);
	}
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if ( items.isEmpty())
			return;
		loadItem(position);
	}
	
	private void loadItem(int position) {
		
		Intent d = new Intent(this, ItemDetailsActivity.class);
		
		//TODO must be a better way, maybe an ArrayList
		Article a = items.get(position);
		d.putExtra("title", a.title );
		//if ( a.url.toString().trim().length() > 0 )
		//	d.putExtra("link", a.url.toString() );

		if ( a.description.trim().length() > 0 ) 
			d.putExtra("description", a.description );
		
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
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case GlobalResources.PREFERENCES:
				startActivity(new Intent(this, EditPreferences.class));
				break;
			case GlobalResources.SEARCH:
				startActivity(new Intent(this, SearchFormActivity.class));
				break;
			case GlobalResources.INFO:
				startActivity(new Intent(this, InfoActivity.class));
				break;
		}
		return true;
	}
	public boolean onSearchRequested() {
		startActivity(new Intent(this, SearchFormActivity.class));
		finish();
		return true;
	}
}