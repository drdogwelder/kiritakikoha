package nz.net.catalyst.KiritakiKoha.authenticator;

import nz.net.catalyst.KiritakiKoha.log.LogConfig;
import nz.net.catalyst.KiritakiKoha.search.SearchKoha;
import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Concrete AbstractAccountAuthenticator implementation for handling
 * authentication on accounts of type "nz.net.catalyst.davidwang.ldap.ldapandroidsync".
 * 
 * @author David X Wang
 *
 */

public class AccountAuthenticator extends AbstractAccountAuthenticator {
	static final String TAG = LogConfig.getLogTag(AccountAuthenticator.class);
	// whether DEBUG level logging is enabled (whether globally, or explicitly
	// for this log tag)
	static final boolean DEBUG = LogConfig.isDebug(TAG);
	// whether VERBOSE level logging is enabled
	static final boolean VERBOSE = LogConfig.VERBOSE;

	private Context mContext;
	public static final String ACCOUNT_TYPE = "nz.net.catalyst.kiritahikoha.account";
	
	public AccountAuthenticator(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response,
			String accountType, String authTokenType,
			String[] requiredFeatures, Bundle options)
			throws NetworkErrorException {
		Bundle reply = new Bundle();
		
		Intent i = new Intent(mContext, LoginActivity.class);
		i.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		
		reply.putParcelable(AccountManager.KEY_INTENT, i);
		
		if(LogConfig.VERBOSE) Log.v(TAG, "addAccount() ... replying with key intent for LoginActivity class");
		return reply;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response,
			Account account, Bundle options) {
		return null;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response,
			String accountType) {
		return null;
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response,
			Account account, String authTokenType, Bundle options)
			throws NetworkErrorException {
		return null;
	}

	@Override
	public String getAuthTokenLabel(String authTokenType) {
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response,
			Account account, String[] features) throws NetworkErrorException {
		return null;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response,
			Account account, String authTokenType, Bundle options) {
		return null;
	}

}
