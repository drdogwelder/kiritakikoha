package nz.net.catalyst.KiritakiKoha.search;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nz.net.catalyst.KiritakiKoha.Constants;
import nz.net.catalyst.KiritakiKoha.EditPreferences;
import nz.net.catalyst.KiritakiKoha.R;
import nz.net.catalyst.KiritakiKoha.Record;
import nz.net.catalyst.KiritakiKoha.hold.PlaceHoldFormActivity;
import nz.net.catalyst.KiritakiKoha.log.LogConfig;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;

import android.preference.PreferenceManager;

import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchResultsActivity extends Activity implements OnChildClickListener, OnGroupExpandListener {
	static final String TAG = LogConfig.getLogTag(SearchResultsActivity.class);
	// whether DEBUG level logging is enabled (whether globally, or explicitly
	// for this log tag)
	static final boolean DEBUG = LogConfig.isDebug(TAG);
	// whether VERBOSE level logging is enabled
	static final boolean VERBOSE = LogConfig.VERBOSE;
	
	// application preferences
	private SharedPreferences mPrefs;

	ArrayList<Record> items = new ArrayList<Record>();
	private Bundle m_extras;
	private List<String> searchTerms;

	private Thread mSearchThread;
    private final Handler mHandler = new Handler();
    
    ExpandableListView listview;
	ExpandableListAdapter adapter = new ExpandableListAdapter(this, new ArrayList<String>(), 
			new ArrayList<ArrayList<Record>>());
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        setContentView(R.layout.search_results);
        listview = (ExpandableListView) findViewById(R.id.listView);
        listview.setOnChildClickListener(this);
        listview.setOnGroupExpandListener(this);
        
        m_extras = getIntent().getExtras();
        if (m_extras == null) {
			Toast.makeText(this, getString(R.string.search_bad_request), Toast.LENGTH_SHORT).show();
        	finish();
        	return;
        }
        	
    	if ( ! ( m_extras.containsKey("idx") && m_extras.containsKey("q") ) ) {
			Toast.makeText(this, getString(R.string.search_bad_request), Toast.LENGTH_SHORT).show();
        	finish();
        	return;
    	}

		ArrayList<String> idxValues = m_extras.getStringArrayList("idx");
		ArrayList<String> opValues = m_extras.getStringArrayList("op");
		ArrayList<String> qValues = m_extras.getStringArrayList("q");
		String pub_date_range = m_extras.getString(Constants.SEARCH_PUB_DATE_RANGE_PARAM);
		
		//Save search terms for later use.
		searchTerms = qValues; 
    	
		if ( ! ( idxValues.size() > 0 && qValues.size() > 0 ) ) {
			Toast.makeText(this, getString(R.string.search_no_search_terms), Toast.LENGTH_SHORT).show();
        	finish();
        	return;
    	}

		String mURL = mPrefs.getString(getResources().getString(R.string.pref_base_url_key).toString(),
										getResources().getString(R.string.base_url).toString());
		//Log.v("mURL", mURL);
		
		//Checking for Malformed Base URL(Adds / if non exitant)
		if(mURL.charAt(mURL.length()-1) != '/' || mURL.charAt(mURL.length()-1) != '\\'){
			mURL += '/';
		}
		
		mURL = mURL + mPrefs.getString(getResources().getString(R.string.pref_search_url_key).toString(),
				getResources().getString(R.string.search_url).toString());

		String qStr = "";
		Iterator<String> idxItr = idxValues.iterator();
		Iterator<String> opItr = opValues.iterator();
		Iterator<String> qItr = qValues.iterator(); 
		while ( idxItr.hasNext() && qItr.hasNext()) { 
			String q = qItr.next();

			String idx = idxItr.next();
			qStr = qStr + "&idx=" + idx + "&q=" + Uri.encode(q);
			if (opItr.hasNext()) {
				String op = opItr.next();
				qStr = qStr + "&op=" + op;
			}
		}
		if ( pub_date_range.trim().length() > 0 )
			qStr = qStr + "&" + Constants.SEARCH_PUB_DATE_RANGE_PARAM + "=" 
									+ Uri.encode(pub_date_range);
		if ( m_extras.containsKey(Constants.LIMIT_AVAILABLE) ) 
			qStr = qStr + "&" + Constants.LIMIT_AVAILABLE;

		// Finally add the query string
		// Needs to have an interface for sorting by ""
		// Currently is on A=Z by Default
        mURL = mURL + qStr + "&sort_by=title_az";
        
        showProgress();
        // Start search
        mSearchThread =	runSearch(mURL, listview, mHandler, this);
	}
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
     public static Thread runSearch(final String u, final ExpandableListView elv, final Handler handler, final Context context) {
        final Runnable runnable = new Runnable() {
            public void run() {
                search(u, elv, handler, context);
            }
        };
        // run on background thread.
        return SearchResultsActivity.performOnBackgroundThread(runnable);
    }
	protected static void search(String u, ExpandableListView elv, Handler handler, Context context) {
		ArrayList<Record> results = null;
		try {
			RSSHandler rh = new RSSHandler();
			Log.d(TAG, "URL = " + u);
			results = rh.getItems(context, new URL(u));
		} catch (MalformedURLException e) {
			Log.e(TAG, "Malfomed URL: " + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "Connection error: " + e.getMessage());
		}

        sendResult(results, elv, handler, context);
	}
    private static void sendResult(final ArrayList<Record> result, final ExpandableListView listview, final Handler handler, final Context context) {
        if (handler == null || context == null) {
            return;
        }
        handler.post(new Runnable() {
            public void run() {
                ((SearchResultsActivity) context).onSearchResult(result, listview);
            }
        });
    }
    /*
     * {@inheritDoc}
     */
	private void onSearchResult(ArrayList<Record> results, ExpandableListView listview) {
		hideProgress();
        // Initialize the adapter with blank groups and children
        // We will be adding children on a thread, and then update the ListView
		
		if(results == null)
		{
			Toast.makeText(this, getResources().getString(R.string.search_connection_failure), 
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		if ( results.isEmpty() ) {
			Toast.makeText(this, getResources().getString(R.string.search_no_results), 
					Toast.LENGTH_SHORT).show();
			finish();
		}
		
		for (Iterator<Record> it = results.iterator(); it.hasNext(); ) { 
				Record a = it.next();
				adapter.addItem(a);
		}

        // Set this blank adapter to the list view
		listview.setAdapter(adapter);
	}
    /*
     * {@inheritDoc}
     */
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		menu.add(0, Constants.SEARCH, 1, R.string.menu_search).setIcon(android.R.drawable.ic_menu_search);
		menu.add(0, Constants.PREFERENCES, 2, R.string.menu_preferences).setIcon(android.R.drawable.ic_menu_preferences);
		return result;
	}
    /*
     * {@inheritDoc}
     */
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case Constants.SEARCH:
				finish();
				break;
			case Constants.PREFERENCES:
				startActivity(new Intent(this, EditPreferences.class));
				break;
			default:
				return super.onOptionsItemSelected(item);			
		}
		return true;
	}
    /*
     * {@inheritDoc}
     */
	public boolean onSearchRequested() {
		finish();
		return true;
	}
    /*
     * {@inheritDoc}
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getText(R.string.search_inprogress));
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Log.i(TAG, "dialog cancel has been invoked");
                if (mSearchThread != null) {
                    mSearchThread.interrupt();
                }
            }
        });
        return dialog;
    }
    /**
     * Shows the progress UI for a lengthy operation.
     */
    protected void showProgress() {
        showDialog(0);
    }
    /**
     * Hides the progress UI for a lengthy operation.
     */
    protected void hideProgress() {
    	try {
    		dismissDialog(0);
    	} catch ( IllegalArgumentException e ) {
    		// do nothing .. must have gone by itself.
    	}
    }

	public class ExpandableListAdapter extends BaseExpandableListAdapter implements OnChildClickListener {

	    @Override
	    public boolean areAllItemsEnabled()
	    {
	        return true;
	    }

	    private Context context;

	    private ArrayList<String> groups;

	    private ArrayList<ArrayList<Record>> children;

	    public ExpandableListAdapter(Context context, ArrayList<String> groups,
	            ArrayList<ArrayList<Record>> children) {
	        this.context = context;
	        this.groups = groups;
	        this.children = children;
	    }

	    public void addItem(Record rec) {
	        if (!groups.contains(rec.getGroup())) {
	            groups.add(rec.getGroup());
	        }
	        int index = groups.indexOf(rec.getGroup());
	        if (children.size() < index + 1) {
	            children.add(new ArrayList<Record>());
	        }
	        children.get(index).add(rec);
	    }

	    @Override
	    public Object getChild(int groupPosition, int childPosition) {
	        return children.get(groupPosition).get(childPosition);
	    }

	    @Override
	    public long getChildId(int groupPosition, int childPosition) {
	        return childPosition;
	    }
	    
	    // Return a child view. You can load your custom layout here.
	    @Override
	    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
	            View convertView, ViewGroup parent) {
	    	Record rec = (Record) getChild(groupPosition, childPosition);
	        if (convertView == null) {
	            LayoutInflater infalInflater = (LayoutInflater) context
	                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            convertView = infalInflater.inflate(R.layout.search_results_row_child, null);
	        }
	        TextView tv;
	        tv = (TextView) convertView.findViewById(R.id.title);
	        SpannableString text = new SpannableString(rec.getTitle());
	        highlightSearchTerms(text);
	        tv.setText(text);
	        
	        tv = (TextView) convertView.findViewById(R.id.description);
	        tv.setText(Html.fromHtml(rec.getDescription()));
	        Log.d(TAG, "Decription= "+rec.getDescription());
	        
	        boolean showImages = mPrefs.getBoolean("show.image", true);
	        if(showImages){
	        	ImageView iv = (ImageView) convertView.findViewById(R.id.thumbnail);
	        	InputStream imageInput = BookThumbnailService.checkGoogle(rec.getISBN());
		        if(imageInput == null){
		        	imageInput = BookThumbnailService.getThumbnail(rec.getISBN());
		        }
		        iv.setImageBitmap(BitmapFactory.decodeStream(imageInput));
	        }
	        
			Boolean useWeb = mPrefs.getBoolean(getResources().getString(R.string.pref_hold_via_web_key), false);
			if ( useWeb ) {
		        tv.setMovementMethod(LinkMovementMethod.getInstance());
		        tv.setLinksClickable(true);
			}
	        return convertView;
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
	    public long getGroupId(int groupPosition) {
	        return groupPosition;
	    }

	    // Return a group view. You can load your custom layout here.
	    @Override
	    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
	            ViewGroup parent) {
	        String group = (String) getGroup(groupPosition);
	        if (convertView == null) {
	            LayoutInflater infalInflater = (LayoutInflater) context
	                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            convertView = infalInflater.inflate(R.layout.search_results_row, null);
	        }
	        TextView tv = (TextView) convertView.findViewById(R.id.title);
	        SpannableString text = new SpannableString(group);
	        highlightSearchTerms(text);
	        tv.setText(text);

	        return convertView;
	    }
	    

	    @Override
	    public boolean hasStableIds() {
	        return true;
	    }

	    @Override
	    public boolean isChildSelectable(int arg0, int arg1) {
	        return true;
	    }

		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			// TODO make this do the right thing
			Record selectedRecord = (Record) getChild(groupPosition, childPosition);
			Intent intent = new Intent(this.context, PlaceHoldFormActivity.class);
			intent.putExtra("bib", (Parcelable) selectedRecord);
			startActivity(intent);
			
			return true;
		}
		
		private void highlightSearchTerms(SpannableString text) {
			
			for (String term : searchTerms) {
				int start = text.toString().toLowerCase().indexOf(term.toLowerCase());
				if (start >= 0) {
				   text.setSpan(new ForegroundColorSpan(Color.BLACK), start, start+term.length(), 0);
				   text.setSpan(new BackgroundColorSpan(Color.WHITE), start, start+term.length(), 0);
				}
				
			}
		}
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		adapter.onChildClick(parent, v, groupPosition, childPosition, id);
		parent.expandGroup(childPosition);
		return false;
	}
	@Override
	public void onGroupExpand(int groupPosition) {
		if(!mPrefs.getBoolean("limit.items", true))return;
		for(int i = 0;i < listview.getCount();i++){
			if (i != groupPosition)listview.collapseGroup(i);
		}
	}
}
