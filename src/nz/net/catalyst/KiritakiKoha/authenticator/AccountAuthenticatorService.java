package nz.net.catalyst.KiritakiKoha.authenticator;

import nz.net.catalyst.KiritakiKoha.log.LogConfig;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AccountAuthenticatorService extends Service {
	private AccountAuthenticator mAccountAuthenticator;
	private static final String TAG = "AccountAuthenticatorService";

	@Override
	public void onCreate() {
		if(LogConfig.VERBOSE) Log.v(TAG, "Service started ...");
		mAccountAuthenticator = new AccountAuthenticator(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		if(LogConfig.VERBOSE) Log.v(TAG, "IBinder returned for AccountAuthenticator implementation.");
		return mAccountAuthenticator.getIBinder();
	}
}
