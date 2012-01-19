package nz.net.catalyst.KiritakiKoha.hold;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import nz.net.catalyst.KiritakiKoha.Constants;
import nz.net.catalyst.KiritakiKoha.R;
import nz.net.catalyst.KiritakiKoha.Record;
import nz.net.catalyst.KiritakiKoha.authenticator.AuthenticatorActivity;
import nz.net.catalyst.KiritakiKoha.authenticator.KohaAuthHandler;
import nz.net.catalyst.KiritakiKoha.log.LogConfig;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PlaceHoldFormActivity extends Activity implements OnClickListener, TextWatcher {
	static final String TAG = LogConfig.getLogTag(PlaceHoldFormActivity.class);
	// whether DEBUG level logging is enabled (whether globally, or explicitly for this log tag)
	static final boolean DEBUG = LogConfig.isDebug(TAG);
	// whether VERBOSE level logging is enabled
	static final boolean VERBOSE = LogConfig.VERBOSE;
	
	// application preferences
	private SharedPreferences mPrefs;
	
	private Record bib;
	private Bundle m_extras;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.place_hold);
        
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        m_extras = getIntent().getExtras();
        if (m_extras == null) {
			Toast.makeText(this, getString(R.string.place_hold_on_nothing), Toast.LENGTH_SHORT).show();
        	finish();
        	return;
        }
        	
    	if ( ! ( m_extras.containsKey("bib") ) ) {
			Toast.makeText(this, getString(R.string.place_hold_on_nothing), Toast.LENGTH_SHORT).show();
        	finish();
        	return;
    	}
    	bib = m_extras.getParcelable("bib");

        ((Button) this.findViewById(R.id.btnHoldGo)).setOnClickListener(this);
        ((TextView) this.findViewById(R.id.title)).setText(bib.getTitle());
        ((TextView) this.findViewById(R.id.pickup_location)).addTextChangedListener(this);
    }
    
	public boolean onSearchRequested() {
		finish();
		return true;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnHoldGo) {

			Log.d(TAG, "bib id = " + bib.getID());
			
			AccountManager mAccountManager = AccountManager.get(this);
			Account[] mAccounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
			
			String session_key;
			for (int i=0; i < mAccounts.length; i++) {
				Account a = mAccounts[i];
				session_key = mAccountManager.getUserData(a, Constants.AUTH_SESSION_KEY);
				if ( session_key.length() > 0 ) {
					
					switch ( placeHold(bib.getID(), session_key) ) {
					case Constants.RESP_SUCCESS:
						Toast.makeText(this, "Place hold completed", Toast.LENGTH_SHORT).show();
		            	finish();
		            	break;
					case Constants.RESP_NO_ITEMS:
		            	Toast.makeText(this, "Sorry, place hold failed - likely no available items", Toast.LENGTH_SHORT).show();
		            	finish();
						if ( DEBUG ) Log.d(TAG, "Place hold failed");
						break;
					case Constants.RESP_INVALID_SESSION:
						Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show();
		            	finish();

		            	String mAuthtoken = mAccountManager.peekAuthToken(a, Constants.AUTHTOKEN_TYPE);
						mAccountManager.invalidateAuthToken(Constants.ACCOUNT_TYPE, mAuthtoken);
		            	
						if ( DEBUG ) Log.d(TAG, "Place hold failed - session expired");
						break;
					case Constants.RESP_FAILED:
		            	Toast.makeText(this, "Sorry, place hold failed", Toast.LENGTH_SHORT).show();
		            	finish();
						if ( DEBUG ) Log.d(TAG, "Place hold failed");
						break;
					}
					return;
				}
			}
			
			if ( DEBUG ) Log.d(TAG, "Not logged in, can't place a hold");
			startActivity(new Intent(this, AuthenticatorActivity.class));
			return;
		}
	}
	
	private int placeHold (String id, String session_key) {
        HttpResponse resp;

        KohaAuthHandler.maybeCreateHttpClient();

    	// application preferences
		String aURI = mPrefs.getString(getResources().getString(R.string.pref_base_url_key).toString(),
												getResources().getString(R.string.base_url).toString());
		aURI = aURI + mPrefs.getString(getResources().getString(R.string.pref_placehold_url_key).toString(),
				getResources().getString(R.string.placehold_url).toString());
		String branch = mPrefs.getString(getResources().getString(R.string.pref_branch_key).toString(), "");
		
		aURI = aURI + "?biblionumber=" + Uri.encode(bib.getID());
		
		if ( DEBUG ) Log.d(TAG, "Place hold URL: " + aURI);
		if ( DEBUG ) Log.d(TAG, "Place hold Cookie: " + session_key);
		
        final HttpPost post = new HttpPost(aURI);
        post.setHeader("Cookie", session_key);
        // Add your data  			
		
		/*
		place_reserve=1
		&single_bib=6
		&expiration_date_6=
		&reqtype_6=Any
		&branch=WORK
		*/
		
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);  
        nameValuePairs.add(new BasicNameValuePair("place_reserve", "1"));  
        nameValuePairs.add(new BasicNameValuePair("single_bib", bib.getID()));  
        nameValuePairs.add(new BasicNameValuePair("reserve_mode", "single"));  
        nameValuePairs.add(new BasicNameValuePair("reqtype", "Any")); 
        if ( branch.length() > 0 )
        	nameValuePairs.add(new BasicNameValuePair("branch", branch));  
        try {
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Place hold post encoding exception: " + e);
            return Constants.RESP_FAILED;
		}  
        
        try {
        	HttpClient mHttpClient = KohaAuthHandler.getHttpClient();
            resp = mHttpClient.execute(post);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            	
            	HttpEntity resEntity = resp.getEntity();
            	String content = KohaAuthHandler.convertStreamToString(resEntity.getContent());

            	if ( ! session_key.startsWith(KohaAuthHandler.getCookie(resp, "Set-Cookie", "CGISESSID") ) ) 
            		return Constants.RESP_INVALID_SESSION;
            	else if ( content.contains("?biblionumber=" + bib.getID())) 
	            	return Constants.RESP_SUCCESS;
            	else 
	            	return Constants.RESP_NO_ITEMS;
           } else {
        	   if ( VERBOSE ) Log.v(TAG, "Failed to place item on hold: " + resp.getStatusLine());
            }
        } catch (final IOException e) {
        	if ( DEBUG ) Log.d(TAG, "IOException when getting placing hold", e);
        } finally {
        	if ( DEBUG ) Log.d(TAG, "place hold completing");
        }
        return Constants.RESP_FAILED;
    }

	@Override
	public void afterTextChanged(Editable s) {
		Button holdButton = (Button) this.findViewById(R.id.btnHoldGo);
		if (s.length()>0) { 
			holdButton.setEnabled(true); 
		} else { 
			holdButton.setEnabled(false);
			
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		
	}
}
