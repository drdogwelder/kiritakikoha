package nz.net.catalyst.KiritakiKoha.hold;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import nz.net.catalyst.KiritakiKoha.GlobalResources;
import nz.net.catalyst.KiritakiKoha.R;
import nz.net.catalyst.KiritakiKoha.Record;
import nz.net.catalyst.KiritakiKoha.log.LogConfig;
import nz.net.catalyst.KiritakiKoha.authenticator.AuthenticatorActivity;
import nz.net.catalyst.KiritakiKoha.authenticator.KohaAuthHandler;
/*
 * HoldTitle
	Creates, for a patron, a title-level hold request on a given bibliographic record in Koha.

	Parameters
		patron_id (Required) - the ILS identifier for the patron for whom the request is placed
		bib_id (Required)    - the ILS identifier for the bibliographic record on which the request is placed
		request_location (Required) - IP address where the end user request is being placed
		pickup_location (Optional) - an identifier indicating the location to which to deliver the item for pickup
		needed_before_date (Optional) - date after which hold request is no longer needed
		pickup_expiry_date (Optional) - date after which item returned to shelf if item is not picked up


 */

public class PlaceHoldFormActivity extends Activity implements OnClickListener {
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
    }
    
	public boolean onSearchRequested() {
		finish();
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.btnHoldGo) {

			Log.d(TAG, "bib id = " + bib.getID());
			
			AccountManager mAccountManager = AccountManager.get(this);
			Account[] mAccounts = mAccountManager.getAccountsByType(GlobalResources.ACCOUNT_TYPE);
			
			String session_key;
			for (int i=0; i < mAccounts.length; i++) {
				Account a = mAccounts[i];
				session_key = mAccountManager.getUserData(a, GlobalResources.AUTH_SESSION_KEY);
				if ( session_key.length() > 0 ) {
					if ( placeHold(bib.getID(), session_key) ) {
						Toast.makeText(this, "Place hold succeeded", Toast.LENGTH_SHORT).show();
		            	finish();
						return;
					} else {
						Log.d(TAG, "Place fold failed - invalidating account from cache");
						String mAuthtoken = mAccountManager.peekAuthToken(a, GlobalResources.AUTHTOKEN_TYPE);
						mAccountManager.invalidateAuthToken(GlobalResources.ACCOUNT_TYPE, mAuthtoken);
					}
				}
			}
			
			Log.d(TAG, "Not logged in, can't place a hold");
			startActivity(new Intent(this, AuthenticatorActivity.class));
			return;
		}
	}
	
	private Boolean placeHold (String id, String session_key) {
        HttpResponse resp;

        KohaAuthHandler.maybeCreateHttpClient();

    	// application preferences
    	SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String aURI = mPrefs.getString(getResources().getString(R.string.pref_base_url_key).toString(),
												getResources().getString(R.string.base_url).toString());
		aURI = aURI + mPrefs.getString(getResources().getString(R.string.pref_placehold_url_key).toString(),
				getResources().getString(R.string.placehold_url).toString());
		
		aURI = aURI + "?biblionumber=" + Uri.encode(bib.getID());
		
		Log.d(TAG, "Place fold URL: " + aURI);
		Log.d(TAG, "Place fold Cookie: " + session_key);
		
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
        //nameValuePairs.add(new BasicNameValuePair("reserve_mode", "single"));  
        //nameValuePairs.add(new BasicNameValuePair("reqtype", "Any"));  
        //nameValuePairs.add(new BasicNameValuePair("branch", "Work"));  
        try {
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Place hold post encoding exception: " + e);
            return false;
		}  
        
        try {
        	HttpClient mHttpClient = KohaAuthHandler.getHttpClient();
            resp = mHttpClient.execute(post);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            	return true;
           } else {
                Log.v(TAG, "Failed to place item on hold: " + resp.getStatusLine());
            }
        } catch (final IOException e) {
            Log.v(TAG, "IOException when getting placing hold", e);
        } finally {
            Log.v(TAG, "place hold completing");
        }
        return false;
    }
}
