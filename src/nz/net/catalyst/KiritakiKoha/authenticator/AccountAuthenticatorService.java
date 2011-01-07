package nz.net.catalyst.KiritakiKoha.authenticator;

import nz.net.catalyst.KiritakiKoha.log.LogConfig;
import nz.net.catalyst.KiritakiKoha.search.RSSHandler;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AccountAuthenticatorService extends Service {
	private AccountAuthenticator mAccountAuthenticator;
	static final String TAG = LogConfig.getLogTag(RSSHandler.class);
	// whether DEBUG level logging is enabled (whether globally, or explicitly
	// for this log tag)
	static final boolean DEBUG = LogConfig.isDebug(TAG);
	// whether VERBOSE level logging is enabled
	static final boolean VERBOSE = LogConfig.VERBOSE;

	@Override
	public void onCreate() {
		if(VERBOSE) Log.v(TAG, "Service started ...");
		mAccountAuthenticator = new AccountAuthenticator(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		if(VERBOSE) Log.v(TAG, "IBinder returned for AccountAuthenticator implementation.");
		return mAccountAuthenticator.getIBinder();
	}
}
