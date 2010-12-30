package nz.net.catalyst.KiritakiKoha.search;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import nz.net.catalyst.KiritakiKoha.GlobalResources;
import nz.net.catalyst.KiritakiKoha.log.LogConfig;
import nz.net.catalyst.KiritakiKoha.R;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SearchKoha extends ListActivity implements OnClickListener, Handler.Callback  {
	static final String TAG = LogConfig.getLogTag(SearchKoha.class);
	// whether DEBUG level logging is enabled (whether globally, or explicitly
	// for this log tag)
	static final boolean DEBUG = LogConfig.isDebug(TAG);
	// whether VERBOSE level logging is enabled
	static final boolean VERBOSE = LogConfig.VERBOSE;
	
	// package scope to allow efficient access by inner classes
	final Handler mHandler = new Handler(this);
	
    List<String> items = new ArrayList<String>();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        
        // Set up click handlers for the text field and button
        ((Button) this.findViewById(R.id.runSearchKoha)).setOnClickListener(this);
    }
    
	public void onClick(View v) {
		if (v.getId() == R.id.runSearchKoha) {
	        EditText mText = (EditText) this.findViewById(R.id.searchKeywords);
			String mURL = getResources().getString(R.string.default_url) + mText.getText().toString();
			items.clear();
			
			try {
				RSSHandler rh = new RSSHandler();
				Log.d(TAG, "URL = " + mURL);
				rh.getItems(this, new URL(mURL), mHandler);
			} catch (MalformedURLException e) {
				Toast.makeText(this, "The URL you have entered is invalid.", Toast.LENGTH_SHORT).show();
			}
			
            ArrayAdapter<String> x = 
                new ArrayAdapter<String>(this, R.layout.search_row, items);
            setListAdapter(x);
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
			case GlobalResources.ITEM_FOUND:
	            items.add((String) msg.obj);
	    		Log.d(TAG, "ITEM_FOUND adding ... ");
	            break;
		}
		return false;
	}
	

}