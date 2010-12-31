package nz.net.catalyst.KiritakiKoha.search;

import nz.net.catalyst.KiritakiKoha.log.LogConfig;
import nz.net.catalyst.KiritakiKoha.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class ItemDetailsActivity extends Activity {
	static final String TAG = LogConfig.getLogTag(SearchActivity.class);
	// whether DEBUG level logging is enabled (whether globally, or explicitly
	// for this log tag)
	static final boolean DEBUG = LogConfig.isDebug(TAG);
	// whether VERBOSE level logging is enabled
	static final boolean VERBOSE = LogConfig.VERBOSE;
	
	private Bundle m_extras;
	private WebView mWebView;
	
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
        	if ( m_extras.containsKey("description") ) {

        		mWebView = (WebView) findViewById(R.id.webview);
	    		mWebView.setWebViewClient(new WebViewClient());
	    		mWebView.getSettings().setJavaScriptEnabled(false);
	    		
	    		mWebView.loadData(getResources().getString(R.string.details_description_style) + 
	    								m_extras.getString("description"), 
	    								"text/html", "utf-8");
	    		mWebView.setBackgroundColor(0);
        	}
        }
	}
}
