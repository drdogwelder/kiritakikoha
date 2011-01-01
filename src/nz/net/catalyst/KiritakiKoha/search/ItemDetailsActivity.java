package nz.net.catalyst.KiritakiKoha.search;

import nz.net.catalyst.KiritakiKoha.log.LogConfig;
import nz.net.catalyst.KiritakiKoha.R;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

public class ItemDetailsActivity extends Activity {
	static final String TAG = LogConfig.getLogTag(SearchResultsActivity.class);
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
		
        m_extras = getIntent().getExtras();
        if (m_extras == null) {
			Toast.makeText(this, getString(R.string.search_bad_request), Toast.LENGTH_SHORT).show();
        	finish();
        	return;
        }
        
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
