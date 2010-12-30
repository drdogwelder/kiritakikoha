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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class SearchKoha extends ListActivity implements OnClickListener  {
	static final String TAG = LogConfig.getLogTag(SearchKoha.class);
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
        ((Button) this.findViewById(R.id.runSearchKoha)).setOnClickListener(this);
        
    }
    
	public void onClick(View v) {
		if (v.getId() == R.id.runSearchKoha) {
	        EditText mText = (EditText) this.findViewById(R.id.searchKeywords);
	        
			String mURL = mPrefs.getString(getResources().getString(R.string.pref_default_url_key).toString(),
											getResources().getString(R.string.default_url).toString());
			mURL = mURL + mText.getText().toString(); // Simple concatenation
			
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
			
			if ( items.isEmpty()) {
				Log.d(TAG, "No items returned");
				return; // nothing to do
			}
			
			List<String> results = new ArrayList<String>();

			for (Iterator<Article> it = items.iterator(); it.hasNext(); ) { 
				Article a = it.next();
				if ( a.title != null ) {
					results.add(a.title);
					Log.d(TAG, "Adding result: " + a.title + " (" + a.description + ")");
				}
			} 

			if ( results.isEmpty()) {
				Log.d(TAG, "No items returned");
				return; // nothing to do
			}
            ArrayAdapter<String> x = 
                new ArrayAdapter<String>(this, R.layout.search_row, results);
            setListAdapter(x);
		}
	}
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent d = new Intent(this, ItemDetailsActivity.class);
		
		Article a = items.get(position);
		d.putExtra("title", a.title );
		d.putExtra("link", a.url.toString() );
		d.putExtra("description", a.description );
		d.putExtra("isbn", a.isbn );
		
		// Start the details dialog and pass in the intent containing item details.
		startActivity(d);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		menu.add(0, GlobalResources.PREFERENCES, 1, R.string.prefs_app_name).setIcon(android.R.drawable.ic_menu_preferences);
		return result;
	}
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case GlobalResources.PREFERENCES:
				Intent i = new Intent(this, EditPreferences.class);
				startActivity(i);
				return true;
		}
		return false;
	}
}