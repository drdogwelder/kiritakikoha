package nz.net.catalyst.KiritakiKoha;

import nz.net.catalyst.KiritakiKoha.log.LogConfig;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


public class EditPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	static final String TAG = LogConfig.getLogTag(EditPreferences.class);
	// whether DEBUG level logging is enabled (whether globally, or explicitly for this log tag)
	static final boolean DEBUG = LogConfig.isDebug(TAG);
	// whether VERBOSE level logging is enabled
	static final boolean VERBOSE = LogConfig.VERBOSE;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		prefs.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onDestroy() {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.unregisterOnSharedPreferenceChangeListener(this);
		
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.preferences, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.reset:
			resetToDefaults();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	}
	
	private void resetToDefaults() {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		// clear the preferences
		prefs.edit().clear().commit();
		// reset defaults
		PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
		
		// refresh displayed values by restarting activity (a hack, but apparently there
		// isn't a nicer way)
		finish();
		startActivity(getIntent());
	}
	
}
