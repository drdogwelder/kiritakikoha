package nz.net.catalyst.KiritakiKoha.search;

import nz.net.catalyst.KiritakiKoha.log.LogConfig;
import nz.net.catalyst.KiritakiKoha.R;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class ItemDetailsActivity extends Activity {
	static final String TAG = LogConfig.getLogTag(SearchKoha.class);
	// whether DEBUG level logging is enabled (whether globally, or explicitly
	// for this log tag)
	static final boolean DEBUG = LogConfig.isDebug(TAG);
	// whether VERBOSE level logging is enabled
	static final boolean VERBOSE = LogConfig.VERBOSE;
	
	private Bundle m_extras;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details);
		if(LogConfig.VERBOSE) Log.v(TAG, "Item details created.");
		
        m_extras = getIntent().getExtras();
        if (m_extras == null) {
        	if ( DEBUG ) Log.d(TAG, "No extras .. nothing to do!");
        	finish();
        }
        else {
        	((TextView)findViewById(R.id.title)).setText(m_extras.getString("title"));
        	((TextView)findViewById(R.id.description)).setText(m_extras.getString("description"));
        	((TextView)findViewById(R.id.isbn)).setText(m_extras.getString("isbn"));
        	((TextView)findViewById(R.id.link)).setText(m_extras.getString("link"));
        }
	}
}
