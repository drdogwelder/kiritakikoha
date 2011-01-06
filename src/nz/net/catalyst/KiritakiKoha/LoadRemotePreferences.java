package nz.net.catalyst.KiritakiKoha;

import nz.net.catalyst.KiritakiKoha.log.LogConfig;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;


public class LoadRemotePreferences extends Activity {
	static final String TAG = LogConfig.getLogTag(LoadRemotePreferences.class);
	// whether DEBUG level logging is enabled (whether globally, or explicitly for this log tag)
	static final boolean DEBUG = LogConfig.isDebug(TAG);
	// whether VERBOSE level logging is enabled
	static final boolean VERBOSE = LogConfig.VERBOSE;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//TODO process content of the request and preset preferences.
		//Intent i = this.getIntent();
		
		Log.d(TAG, "LoadRemotePreferences created ...");

	}
	protected void onStart(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//TODO process content of the request and preset preferences.
		//Intent i = this.getIntent();
		
		Log.d(TAG, "LoadRemotePreferences started ...");

	}
	protected void onResume(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//TODO process content of the request and preset preferences.
		//Intent i = this.getIntent();
		
		Log.d(TAG, "LoadRemotePreferences resumed ...");

	}
	protected void onPause(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//TODO process content of the request and preset preferences.
		//Intent i = this.getIntent();
		
		Log.d(TAG, "LoadRemotePreferences paused ...");

	}

}
