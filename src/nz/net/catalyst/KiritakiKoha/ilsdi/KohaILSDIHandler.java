package nz.net.catalyst.KiritakiKoha.ilsdi;

/*
 * 
 * Example Call

ilsdi.pl?service=HoldTitle&patron_id=1&bib_id=1&request_location=127.0.0.1
Example Response

<?xml version="1.0" encoding="ISO-8859-1" ?>
<HoldTitle>
  <title>(les) galères de l'Orfèvre</title>
  <date_available>2009-05-11</date_available>
  <pickup_location>Bibliothèque Jean-Prunier</pickup_location>
</HoldTitle>

Example Call
ilsdi.pl?service=AuthenticatePatron&username=john9&password=soul
Example Response
<?xml version="1.0" encoding="ISO-8859-1" ?>
<AuthenticatePatron>
  <id>419</id>
</AuthenticatePatron>

 */
import nz.net.catalyst.KiritakiKoha.GlobalResources;
import nz.net.catalyst.KiritakiKoha.R;
import nz.net.catalyst.KiritakiKoha.authenticator.AuthenticatorActivity;
import nz.net.catalyst.KiritakiKoha.log.LogConfig;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class KohaILSDIHandler {
	static final String TAG = LogConfig.getLogTag(KohaILSDIHandler.class);
	// whether DEBUG level logging is enabled (whether globally, or explicitly
	// for this log tag)
	static final boolean DEBUG = LogConfig.isDebug(TAG);
	// whether VERBOSE level logging is enabled
	static final boolean VERBOSE = LogConfig.VERBOSE;

    private static HttpClient mHttpClient;
	
    /**
     * Configures the httpClient to connect to the URL provided.
     */
    public static void maybeCreateHttpClient() {
        if (mHttpClient == null) {
            mHttpClient = new DefaultHttpClient();
            final HttpParams params = mHttpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params,
                GlobalResources.REGISTRATION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, GlobalResources.REGISTRATION_TIMEOUT);
            ConnManagerParams.setTimeout(params, GlobalResources.REGISTRATION_TIMEOUT);
        }
    }

    /**
     * Executes the network requests on a separate thread.
     * 
     * @param runnable The runnable instance containing network mOperations to
     *        be executed.
     */
    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }

    /**
     * Connects to the server, authenticates the provided username and
     * password.
     * 
     * @param username The user's username
     * @param password The user's password
     * @param handler The hander instance from the calling UI thread.
     * @param context The context of the calling Activity.
     * @return boolean The boolean result indicating whether the user was
     *         successfully authenticated.
     */
    public static boolean authenticate(String username, String password, Handler handler, final Context context) {
        final HttpResponse resp;

    	// application preferences
    	SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		String aURI = mPrefs.getString(context.getResources().getString(R.string.pref_base_url_key).toString(),
												context.getResources().getString(R.string.base_url).toString());
		aURI = aURI + mPrefs.getString(context.getResources().getString(R.string.pref_login_url_key).toString(),
												context.getResources().getString(R.string.login_url).toString());
		if (aURI.indexOf("?") > 0)
			aURI = aURI + "&";
		else
			aURI = aURI + "?";
			
		aURI = aURI + GlobalResources.PARAM_USERNAME + "=" + Uri.encode(username) 
					+ "&" + GlobalResources.PARAM_PASSWORD + "=" + Uri.encode(password);
		
		Log.d(TAG, "authenticate: using '" + aURI + "'");		

        final HttpGet get = new HttpGet(aURI);
        maybeCreateHttpClient();

        try {
            resp = mHttpClient.execute(get);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            	// TODO here is where we deal with the XML response and get out the patron ID
            	/*
            	    <?xml version="1.0" encoding="ISO-8859-1" ?>
					<AuthenticatePatron>
						<id>419</id>
					</AuthenticatePatron>
            	 */
    			HttpEntity resEntity = resp.getEntity();
    			
    		    if (resEntity == null) {
                    Log.v(TAG, "Error authenticating" + resp.getStatusLine());
                    sendResult(false, handler, context);
                    return false;
    		    }
    		    
				try {
    		    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    		    	DocumentBuilder db;
					db = dbf.newDocumentBuilder();	
    		    	Document doc;
					doc = db.parse(resEntity.getContent());
					for (int i=0; i < doc.getChildNodes().getLength(); i++) {
						Node node = ((NodeList)doc.getChildNodes()).item(i);
						Log.d(TAG, node.getNodeName() + ": " + node.getNodeValue());
						if (node.getNodeName().toLowerCase().equals("authenticatepatron")) {
							for (int j=0; j < node.getChildNodes().getLength(); j++) {
								Node childNode = ((NodeList)node.getChildNodes()).item(j);
								Log.d(TAG, childNode.getNodeName() + ": " + childNode.getNodeValue());
								if (childNode.getNodeName().toLowerCase().equals("id")) {
									String patronID = doc.getFirstChild().getNodeValue().toString();
				    		    	if ( patronID.trim().length() > 0 ) { 
					    				mPrefs.edit()
					    					.putString("patron_id", patronID)
					    					.commit()
					    				;
					                    
					                    sendResult(true, handler, context);
					                    return true;
				    		    	}
								}
							}
						}
    		    	}
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
                    Log.v(TAG, "Error authenticating (ParserConfigurationException)");
				} catch (SAXException e) {
                    Log.v(TAG, "Error authenticating (SAXException)");
				}
            } else {
                Log.v(TAG, "Error authenticating" + resp.getStatusLine());
            }
        } catch (final IOException e) {
            Log.v(TAG, "IOException when getting authtoken", e);
        } finally {
            Log.v(TAG, "getAuthtoken completing");
        }
        sendResult(false, handler, context);
        return false;
    }

    /**
     * Sends the authentication response from server back to the caller main UI
     * thread through its handler.
     * 
     * @param result The boolean holding authentication result
     * @param handler The main UI thread's handler instance.
     * @param context The caller Activity's context.
     */
    private static void sendResult(final Boolean result, final Handler handler,
        final Context context) {
        if (handler == null || context == null) {
            return;
        }
        handler.post(new Runnable() {
            public void run() {
                ((AuthenticatorActivity) context).onAuthenticationResult(result);
            }
        });
    }

    /**
     * Attempts to authenticate the user credentials on the server.
     * 
     * @param username The user's username
     * @param password The user's password to be authenticated
     * @param handler The main UI thread's handler instance.
     * @param context The caller Activity's context
     * @return Thread The thread on which the network mOperations are executed.
     */
    public static Thread attemptAuth(final String username,
        final String password, final Handler handler, final Context context) {
        final Runnable runnable = new Runnable() {
            public void run() {
                authenticate(username, password, handler, context);
            }
        };
        // run on background thread.
        return KohaILSDIHandler.performOnBackgroundThread(runnable);
    }
}
