package nz.net.catalyst.KiritakiKoha.issuelist;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import nz.net.catalyst.KiritakiKoha.Constants;
import nz.net.catalyst.KiritakiKoha.R;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Activity that handles displaying of currently issued books
 * Creates an Expandable display list etc
 * TODO Fix Odd Bug where XML returns as invalid IP, dont know what causes this :P
 */
public class IssueListActivity extends Activity implements OnGroupExpandListener
{
	//Unique Tag for this class
	static final String TAG = LogConfig.getLogTag(IssueListActivity.class);
	//Whether Debug is enabled for this class
	static final boolean DEBUG = LogConfig.isDebug(TAG);
	
	//Unique ID of the logged in patron, for db actions
	static int patron_id = -1;
	private SharedPreferences prefs = null;
	private String respString = "";
	private static int id = -1;
	
	//List containing data of all the books currently on issue
	private static ArrayList<Loan> onloan = null;
	
	ExpandListAdapter adapter = new ExpandListAdapter(this);
	ExpandableListView listview = null;
	
	/**
     * {@inheritDoc}
     */
	@Override
	public void onCreate(Bundle b)
	{
		super.onCreate(b);

		setContentView(R.layout.loan_list_results);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		if(onloan == null)retrieveData();
		
		if(onloan == null)
		{
			Toast.makeText(this, "Error Loading irsdl.pl", Toast.LENGTH_LONG);
			finish();
			return;
		}
		else
		{
			listview = (ExpandableListView) findViewById(R.id.listView);
	       	for(int i = 0;i < onloan.size();i ++)
	       	{
	       		adapter.addLoanItem(onloan.get(i));
	       	}
	       	listview.setOnGroupExpandListener(this);
	        listview.setAdapter(adapter);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case Constants.REFRESH:
				retrieveData();
		        finish();
		        startActivity(new Intent(this, IssueListActivity.class));
				break;
			case Constants.LOGOUT:
				AccountManager accMana = AccountManager.get(this);
				Account[] accs = accMana.getAccounts();
				Log.d(TAG, "ACCS: "+accs.length);
				for(int i = 0;i < accs.length;i ++)
				{
					accMana.removeAccount(accs[i], null, null);
				}
				onloan = null;
				id = -1;
				finish();
				break;
		}
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		menu.add(Menu.NONE, Constants.REFRESH, 1, R.string.menu_refresh).setIcon(android.R.drawable.arrow_up_float);
		menu.add(Menu.NONE, Constants.LOGOUT, 2, R.string.menu_logout).setIcon(android.R.drawable.ic_delete);
		return result;
	}
	
	/*
	 * Retrieves Data from the Specified Opac Server, and displays appropriate error messages
	 */
	
	public int retrieveData()
	{
		AccountManager accManager = AccountManager.get(this);
		Account[] accounts        = accManager.getAccountsByType(Constants.ACCOUNT_TYPE);
		String sessionKey = "";
		for(int i = 0;i < accounts.length;i ++)
		{
			Account acct = accounts[i];
			sessionKey = accManager.getUserData(acct, Constants.AUTH_SESSION_KEY);
			/*
			 * Gets the Database ID from Opac. For use when getting userdata
			 */
			getID(accManager, acct, sessionKey);
			if(sessionKey.length() > 0)
			{
				//TRY GET STUFF
				switch(tryGetPatronData(sessionKey))
				{
				case Constants.RESP_FAILED:
					Toast.makeText(this, "Error Retrieving User Data", Toast.LENGTH_SHORT).show();
					return Constants.RESP_FAILED;
				case Constants.RESP_SUCCESS:
					//If ILS-DI is Disabled
					if(respString.toLowerCase().contains("ils-di is disabled"))
					{
						Toast.makeText(this, R.string.ils_disabled_error, Toast.LENGTH_SHORT).show();
						finish();
						return Constants.RESP_SUCCESS;
					}
				}
				return Constants.RESP_FAILED;
			}
		}
		//Prompts for login.
		startActivity(new Intent(this, AuthenticatorActivity.class));
		return 0;
	}
	
	/*
	 * Connects to Opac Server and retrieves the User ID. 
	 * TODO Move this to the Actual authentification code, to avoid messiness :P
	 * Potentially store in AccountManager, for easy and secure access
	 * Instead of 
	 */
	public int getID(AccountManager am, Account acct, String session)
	{
		Log.d(TAG, "Start: Getting ID");
		KohaAuthHandler.maybeCreateHttpClient();
		
		String aURI = prefs.getString(getResources().getString(R.string.pref_base_url_key), getResources().getString(R.string.base_url));
		aURI += "ilsdi.pl";
		if(DEBUG)Log.d(TAG, "Auth URL: "+aURI);
		if(DEBUG)Log.d(TAG, "Session: "+session);
		
		HttpPost post = new HttpPost(aURI);
		post.setHeader("Cookie", session);
		
		List<NameValuePair> argList = new ArrayList<NameValuePair>(2);
		argList.add(new BasicNameValuePair("service", "AuthenticatePatron"));
		argList.add(new BasicNameValuePair("username", acct.name));
		argList.add(new BasicNameValuePair("password", am.getPassword(acct)));
		//argList.add(new BasicNameValuePair("request_location", "127.0.0.1"));
		try
		{
			post.setEntity(new UrlEncodedFormEntity(argList));
		}
		catch(UnsupportedEncodingException e)
		{
			Log.e(TAG, "Error Encoding Patron Data");
			return Constants.RESP_FAILED;
		}
		
		Log.d(TAG, "Sent Post Data");
		
		try
		{
			HttpClient httpClient = KohaAuthHandler.getHttpClient();
			HttpResponse resp = httpClient.execute(post);
			if(resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
			{
				try
				{
					HttpEntity respEntity = resp.getEntity();
					String cont = KohaAuthHandler.convertStreamToString(respEntity.getContent());
					
					int start = cont.indexOf("<id>");
					int end   = cont.indexOf("</id>");
					
					String idStr = cont.substring(start + 4, end);
					
					id = Integer.parseInt(idStr);
					
					Log.d(TAG, "Retrieved Login Id: " + id);
					
					return Constants.RESP_SUCCESS;
				}
				catch(Exception e)
				{
					Toast.makeText(this, "Error establishing link with irldl.pl", Toast.LENGTH_SHORT).show();
					finish();
					onloan = null;
					id = -1;
					return Constants.RESP_INVALID_SESSION;
				}
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return Constants.RESP_FAILED;
		}
		return Constants.RESP_FAILED;
	}
	
	/*
	 * Connects to Koha server and retrieves Data on books on loan.
	 * Requires a login
	 */
	public int tryGetPatronData(String session)
	{
		Log.d(TAG, "Getting Patron Data "+id);
		KohaAuthHandler.maybeCreateHttpClient();
		
		String aURI = prefs.getString(getResources().getString(R.string.pref_base_url_key), getResources().getString(R.string.base_url));
		aURI += "ilsdi.pl";
		String branch = prefs.getString(getResources().getString(R.string.pref_branch_key).toString(), "");
		
		if(DEBUG)Log.d(TAG, "Auth URL: "+aURI);
		if(DEBUG)Log.d(TAG, "Session: "+session);
		
		HttpPost post = new HttpPost(aURI);
		post.setHeader("Cookie", session);
		
		List<NameValuePair> argList = new ArrayList<NameValuePair>(2);
		argList.add(new BasicNameValuePair("patron_id", ""+id));
		argList.add(new BasicNameValuePair("service", "GetPatronInfo"));
		argList.add(new BasicNameValuePair("show_holds", "1"));
		argList.add(new BasicNameValuePair("show_loans", "1"));
		if(branch.length() > 0)argList.add(new BasicNameValuePair("branch", branch));
		try
		{
			post.setEntity(new UrlEncodedFormEntity(argList));
		}
		catch(UnsupportedEncodingException e)
		{
			Log.e(TAG, "Error Encoding Patron Data");
			Toast.makeText(this, R.string.book_list_load_error, Toast.LENGTH_LONG).show();
			return Constants.RESP_FAILED;
		}
		
		try
		{
			HttpClient httpClient = KohaAuthHandler.getHttpClient();
			HttpResponse resp = httpClient.execute(post);
			if(resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
			{
				HttpEntity respEntity = resp.getEntity();
				
				DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
				DocumentBuilder build = fact.newDocumentBuilder();
				Document doc = build.parse(respEntity.getContent());
				
				NodeList children = doc.getElementsByTagName("loan");
				onloan = new ArrayList<Loan>();
				for(int i = 0;i < children.getLength();i ++)
				{
					//Retrieves Data from the XML Format
					//Adds Loan Data to list
					Element loan = (Element)children.item(i);
					NodeList titles = loan.getElementsByTagName("title");
					Loan l = new Loan(i, titles.item(0).getFirstChild().getNodeValue());
					
					NodeList overdue = loan.getElementsByTagName("overdue");
					if(overdue == null || overdue.item(0) == null || overdue.item(0).getFirstChild() == null || overdue.item(0).getFirstChild().getNodeValue() == null)
					{
						l.setOverdue(false);
					}
					else if((overdue.item(0).getFirstChild().getNodeValue().equals("1")))
					{
						l.setOverdue(true);
					}
					
					NodeList dueDates = loan.getElementsByTagName("date_due");
					l.setDueDate(dueDates.item(0).getFirstChild().getNodeValue());
					
					NodeList author = loan.getElementsByTagName("author");
					l.setAuthor(author.item(0).getFirstChild().getNodeValue());
					
					onloan.add(l);
				}
				
				return Constants.RESP_SUCCESS;
			}
		} 
		catch (IOException e) 
		{
			Toast.makeText(this, R.string.book_list_load_error, Toast.LENGTH_LONG).show();
		}
		catch(Exception e)
		{
			Toast.makeText(this, R.string.book_list_load_error, Toast.LENGTH_LONG).show();
		}
		return 0;
	}
	
	public static class ExpandListAdapter extends BaseExpandableListAdapter
	{
		ArrayList<String> groups = new ArrayList<String>();
		ArrayList<ArrayList<Loan>> children = new ArrayList<ArrayList<Loan>>();
		Context context = null;
		
		public ExpandListAdapter(Context context) 
		{
			this.context = context;
		}
		
		@Override
		public Object getChild(int groupPosition, int childPosition) 
		{
			if(groupPosition >= children.size())return null;
			if(childPosition > children.get(groupPosition).size())return null;
			return children.get(groupPosition).get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition)
		{
			return childPosition;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			Loan loan = (Loan) getChild(groupPosition, childPosition);
	        if (convertView == null)
	        {
	            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            convertView = infalInflater.inflate(R.layout.search_results_row_child, null);
	        }
	        TextView tv;
	        tv = (TextView) convertView.findViewById(R.id.title);
	        tv.setText(loan.getName());

	        tv = (TextView) convertView.findViewById(R.id.description);
	        tv.setText(Html.fromHtml(loan.getDesc()));

	        return convertView;
		}
		
		public void addLoanItem(Loan l)
		{
			if(!groups.contains(l.getGroup()))
			{
				groups.add(l.getGroup());
			}
			int parentIndex = groups.indexOf(l.getGroup());
			if (children.size() < parentIndex + 1) 
			{
	            children.add(new ArrayList<Loan>());
	        }
	        children.get(parentIndex).add(l);
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return children.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return groups.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return groups.size();
		}

		@Override
		public long getGroupId(int groupPosition)
		{
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			String group = (String) getGroup(groupPosition);
	        if (convertView == null) {
	            LayoutInflater infalInflater = (LayoutInflater) context
	                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            convertView = infalInflater.inflate(R.layout.search_results_row, null);
	        }
	        TextView tv = (TextView) convertView.findViewById(R.id.title);
	        if(((Loan)getChild(groupPosition, 0)).isOverdue())
	        {
	        	tv.setTextColor(0xFFFF0000);
	        }
	        else
	        {
	        	tv.setTextColor(0xFFAAAAAA);
	        }
	        tv.setText(group);
	        return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition)
		{
			return true;
		}
		
	}

	@Override
	public void onGroupExpand(int groupPosition)
	{
		for(int i = 0;i < listview.getCount();i ++)
		{
			if(i != groupPosition)listview.collapseGroup(i);
		}
	}
}
