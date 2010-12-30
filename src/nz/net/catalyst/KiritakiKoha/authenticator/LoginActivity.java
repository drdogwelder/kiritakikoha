package nz.net.catalyst.KiritakiKoha.authenticator;

import nz.net.catalyst.KiritakiKoha.log.LogConfig;
import nz.net.catalyst.KiritakiKoha.R;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * This activity handles the initial log-in process when creating
 * a new account. It displays a simple log-in UI to allow the user to
 * enter their credentials while displaying appropriate error messages
 * when they occur.
 * 
 * @author David X Wang
 *
 */

public class LoginActivity extends AccountAuthenticatorActivity {
	
	private EditText mUsernameField;
	private EditText mPasswordField;
	private TextView mMessage;
	private String mUsername = "username";
	private String mPassword = "password";
	private Thread mAuthThread = null;
	private Handler mHandler = new Handler();
	
	private static final int PROGRESS_DIALOG = 0;
	private static final int SERVER_PREFERENCES = 1;
	
	private static final String TAG = "LoginActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		if(LogConfig.VERBOSE) Log.v(TAG, "LoginActivity created.");
		
		//fetch components
		mMessage = (TextView)findViewById(R.id.login_message);
		mUsernameField = (EditText)findViewById(R.id.username_field);
		mPasswordField = (EditText)findViewById(R.id.password_field);
	}
	
	@Override
    protected Dialog onCreateDialog(int id) {
		if(id == PROGRESS_DIALOG){
			//create progress dialog
			final ProgressDialog dialog = new ProgressDialog(this);
	        dialog.setMessage(getText(R.string.login_activity_authenticating));
	        dialog.setIndeterminate(true);
	        dialog.setCancelable(true);
	        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
	            public void onCancel(DialogInterface dialog) {
	                if (mAuthThread != null) {
	                    mAuthThread.interrupt();
	                    if(LogConfig.VERBOSE) Log.v(TAG, "Login cancelled.");
	                    finish();
	                }
	            }
	        });
	        return dialog;
		}
		return null;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, SERVER_PREFERENCES, Menu.NONE, R.string.server_preferences_activity_label);
		return result;
	}
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case SERVER_PREFERENCES:
                Intent i = new Intent(this, ServerPreferences.class);
                startActivity(i);
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

	/**
	 * Handles the onClick event of the login submit button.
	 * @param view
	 */
	public void onSubmit(View view){
		mUsername = mUsernameField.getText().toString();
		mPassword = mPasswordField.getText().toString();
		if(LogConfig.VERBOSE) Log.v(TAG, "onSubmit() ... Attempting to validate account.");
		//make sure fields are filled in
		if(validate(mUsername, mPassword)){
			showProgress();
			//TODO attempt to authenticate account with Koha
			//mAuthThread = LDAPHelper.createAuthenticationThread(mUsername, mPassword, mHandler, this);
			//mAuthThread.start();
		}
	}
	
	public void onAuthenticationResult(boolean result, String message){
		hideProgress();
		if(LogConfig.VERBOSE) Log.v(TAG, "Authentication result received: "+result+" with message: "+message);
		if(result){
			finishAuthentication();
		} else{
			mMessage.setText(message);
		}
	}
	
	private void finishAuthentication(){
		//create new account for user credentials
		String accountType = AccountAuthenticator.ACCOUNT_TYPE;
		Account account = new Account(mUsername, accountType);
		AccountManager am = AccountManager.get(this);

		if(LogConfig.VERBOSE) Log.v(TAG, "Creating account explicitly - username: "+mUsername+"| -accountType:"+accountType);
		boolean accountCreated = am.addAccountExplicitly(account, mPassword, null);
		
		//pass account information back to the caller
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			 if (accountCreated){
				  if(LogConfig.VERBOSE) Log.v(TAG, "finishAuthentication() ... Putting account name and type into response.");
				  AccountAuthenticatorResponse response = extras.getParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
				  Bundle result = new Bundle();
				  result.putString(AccountManager.KEY_ACCOUNT_NAME, mUsername);
				  result.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
				  response.onResult(result);
			 }
			 finish();
		}
	}
	
	private void showProgress(){
		showDialog(PROGRESS_DIALOG);
	}
	
	private void hideProgress(){
		try {
			dismissDialog(PROGRESS_DIALOG);
		} catch (IllegalArgumentException e) {
			Log.d(TAG, "hideProgress() ... progress dialog already cancelled: "+e);
		}
	}
	
	private boolean validate(String u, String p){
		if(TextUtils.isEmpty(u)){
			mMessage.setText(getString(R.string.login_no_name));
			return false;
		} else if(TextUtils.isEmpty(p)){
			mMessage.setText(getString(R.string.login_no_password));
			return false;
		} else{
			return true;
		}
	}
}
