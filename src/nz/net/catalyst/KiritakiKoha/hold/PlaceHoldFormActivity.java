package nz.net.catalyst.KiritakiKoha.hold;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import nz.net.catalyst.KiritakiKoha.Constants;
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
		pickup_location (Required) - an identifier indicating the location to which to deliver the item for pickup
		needed_before_date (Optional) - date after which hold request is no longer needed
		pickup_expiry_date (Optional) - date after which item returned to shelf if item is not picked up


 */

public class PlaceHoldFormActivity extends Activity implements OnClickListener {
	static final String TAG = LogConfig.getLogTag(PlaceHoldFormActivity.class);
	// whether DEBUG level logging is enabled (whether globally, or explicitly for this log tag)
	static final boolean DEBUG = LogConfig.isDebug(TAG);
	// whether VERBOSE level logging is enabled
	static final boolean VERBOSE = LogConfig.VERBOSE;
	
	private static final int BEFORE_DATE_DIALOG_ID=1;
	private static final int EXPIRY_DATE_DIALOG_ID=2;
	
	// application preferences
	private SharedPreferences mPrefs;
	
	private Record bib;
	private Bundle m_extras;
	
    private int mBeforeYear;
    private int mBeforeMonth;
    private int mBeforeDay;
    private int mExpiryYear;
    private int mExpiryMonth;
    private int mExpiryDay;
	
    // the callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener mBeforeDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, 
                                      int monthOfYear, int dayOfMonth) {

                	mBeforeYear=year;
                	mBeforeMonth=monthOfYear;
                	mBeforeDay=dayOfMonth;
                	
                	//initialise the before date to today
                    Calendar c = Calendar.getInstance();
                    c.set(year, monthOfYear, dayOfMonth);
                    
                	updateBeforeDisplay(c.getTime());
                }
            };
            
            private DatePickerDialog.OnDateSetListener mExpiryDateSetListener =
                    new DatePickerDialog.OnDateSetListener() {

                        public void onDateSet(DatePicker view, int year, 
                                              int monthOfYear, int dayOfMonth) {

                        	mExpiryYear=year;
                        	mExpiryMonth=monthOfYear;
                        	mExpiryDay=dayOfMonth;
                        	
                        	//initialise the before date to today
                            Calendar c = Calendar.getInstance();
                            c.set(year, monthOfYear, dayOfMonth);
                        	
                            updateExpiryDisplay(c.getTime());
                        }
                    };
	
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
    	        
    	//initialise the before date to today
        Calendar c = Calendar.getInstance();        
        mBeforeYear = c.get(Calendar.YEAR);
        mBeforeMonth = c.get(Calendar.MONTH);
        mBeforeDay = c.get(Calendar.DAY_OF_MONTH);
        
        c.set(mBeforeYear, mBeforeMonth, mBeforeDay);    	
        updateBeforeDisplay(c.getTime());
        
        //initialise the expiry date to today
        mExpiryYear = c.get(Calendar.YEAR);
        mExpiryMonth = c.get(Calendar.MONTH);
        mExpiryDay = c.get(Calendar.DAY_OF_MONTH);
        
        c.set(mExpiryYear, mExpiryMonth, mExpiryDay);    	
        updateExpiryDisplay(c.getTime());
              
        
        ((Button) this.findViewById(R.id.btnHoldGo)).setOnClickListener(this);
        ((TextView) this.findViewById(R.id.title)).setText(bib.getTitle());
        
        
        final Button pickBeforeDate = (Button) this.findViewById(R.id.pickBeforeDate);
        pickBeforeDate.setOnClickListener(this);     
        final Button pickExpiryDate = (Button) this.findViewById(R.id.pickExpiryDate);
        pickExpiryDate.setOnClickListener(this);
        
        
    	final CheckBox beforeCheckbox = (CheckBox) findViewById(R.id.beforeCheckbox);
    	beforeCheckbox.setOnClickListener(new OnClickListener() {
    	    public void onClick(View v) {
    	        // Perform action on clicks, depending on whether it's now checked
    	        if (((CheckBox) v).isChecked()) {
    	            pickBeforeDate.setEnabled(true);
    	        } else {
    	        	pickBeforeDate.setEnabled(false);
    	        }
    	    }
    	   
    	});
    	
    	final CheckBox expiryCheckbox = (CheckBox) findViewById(R.id.expiryCheckbox);
    	expiryCheckbox.setOnClickListener(new OnClickListener() {
    	    public void onClick(View v) {
    	        // Perform action on clicks, depending on whether it's now checked
    	        if (((CheckBox) v).isChecked()) {
    	            pickExpiryDate.setEnabled(true);
    	        } else {
    	        	pickExpiryDate.setEnabled(false);
    	        }
    	    }
    	});
     
    	EditText pickupLocation = (EditText) findViewById(R.id.pickupLocation);
    	pickupLocation.setText(getBranch());
    	

        ((Button) findViewById(R.id.buttonmoredetails)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
		        Intent browse = new Intent( Intent.ACTION_VIEW , 
		        		Uri.parse( bib.getURL().toString() ));

		        startActivity( browse );
				
			}
		});
    	
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
			
		}else if (v.getId() == R.id.pickBeforeDate){
			showDialog(BEFORE_DATE_DIALOG_ID);
		}
		
		else if (v.getId() == R.id.pickExpiryDate){
			showDialog(EXPIRY_DATE_DIALOG_ID);
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
	    switch (id) {
	    case BEFORE_DATE_DIALOG_ID:
	        return new DatePickerDialog(this,
	                    mBeforeDateSetListener,
	                    mBeforeYear, mBeforeMonth, mBeforeDay);
	 	
	    case EXPIRY_DATE_DIALOG_ID:
	        return new DatePickerDialog(this,
	                    mExpiryDateSetListener,
	                    mExpiryYear, mExpiryMonth, mExpiryDay);
	        
	    }    
	    return null;
	}
	
	private int placeHold (String id, String session_key) {
        HttpResponse resp;

        KohaAuthHandler.maybeCreateHttpClient();

    	// application preferences
		String aURI = mPrefs.getString(getResources().getString(R.string.pref_base_url_key).toString(),
												getResources().getString(R.string.base_url).toString());
		aURI = aURI + mPrefs.getString(getResources().getString(R.string.pref_placehold_url_key).toString(),
				getResources().getString(R.string.placehold_url).toString());
		String branch = getBranch();
		
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
        
        String reserveDate = ""+mBeforeDay +"/"+mBeforeMonth+"/"+mBeforeYear;
        String expiryDate = ""+mExpiryDay+"/"+mExpiryMonth+"/"+mExpiryYear;
        
        boolean submitBefore = ((CheckBox)findViewById(R.id.beforeCheckbox)).isChecked();
        boolean submitExpiry = ((CheckBox)findViewById(R.id.expiryCheckbox)).isChecked();
        
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);  
        nameValuePairs.add(new BasicNameValuePair("place_reserve", "1"));  
        nameValuePairs.add(new BasicNameValuePair("single_bib", bib.getID()));  
        nameValuePairs.add(new BasicNameValuePair("reserve_mode", "single"));  
        nameValuePairs.add(new BasicNameValuePair("reqtype", "Any")); 
       
        if (submitBefore) {
        	nameValuePairs.add(new BasicNameValuePair("reserve_date_" + bib.getID(), reserveDate)); 
        }
        
        if (submitExpiry) {
        	nameValuePairs.add(new BasicNameValuePair("expiration_date_" + bib.getID(), expiryDate));
        }
        
        
        if ( branch.length() > 0 )
        	nameValuePairs.add(new BasicNameValuePair("branch", branch));
        
        if (DEBUG) Log.d(TAG, "Paremeters: " + nameValuePairs);
        
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

	private String getBranch() {
		return mPrefs.getString(getResources().getString(R.string.pref_branch_key).toString(), "");
			
	}
	
	private void updateBeforeDisplay(Date dateToFormat){		
		((Button) findViewById(R.id.pickBeforeDate)).setText(
            new StringBuilder()
                    // Month is 0 based so add 1
                    .append(DateFormat.format("EE", dateToFormat)).append(", ")
                    .append(mBeforeDay).append("/")
                    .append(mBeforeMonth + 1).append("/")
                    .append(mBeforeYear).append(" "));
	}	
	
	private void updateExpiryDisplay(Date dateToFormat){		
		((Button) findViewById(R.id.pickExpiryDate)).setText(
            new StringBuilder()
                    // Month is 0 based so add 1
            		.append(DateFormat.format("EE", dateToFormat)).append(", ")
                    .append(mExpiryDay).append("/")
                    .append(mExpiryMonth + 1).append("/")
                    .append(mExpiryYear).append(" "));
	}

} 

