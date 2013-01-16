package nz.net.catalyst.KiritakiKoha;

import nz.net.catalyst.KiritakiKoha.authenticator.AuthenticatorActivity;
import nz.net.catalyst.KiritakiKoha.issuelist.IssueListActivity;
import nz.net.catalyst.KiritakiKoha.log.LogConfig;
import nz.net.catalyst.KiritakiKoha.search.SearchFormActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class InfoActivity extends Activity {
	static final String TAG = LogConfig.getLogTag(InfoActivity.class);
	// whether DEBUG level logging is enabled (whether globally, or explicitly
	// for this log tag)
	static final boolean DEBUG = LogConfig.isDebug(TAG);
	// whether VERBOSE level logging is enabled
	static final boolean VERBOSE = LogConfig.VERBOSE;

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);
        setUserString();
    
    /** Search button**/
    Button searchbutton =(Button) findViewById(R.id.searchbutton);
    searchbutton.setOnClickListener(new OnClickListener() 
    {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(InfoActivity.this, SearchFormActivity.class);
			InfoActivity.this.startActivity(intent);

		}
	})   ; 
    /** My Books button**/
    Button mybooksbutton =(Button) findViewById(R.id.mybooksbutton);
    mybooksbutton.setOnClickListener(new OnClickListener() 
    {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(InfoActivity.this, IssueListActivity.class);
			InfoActivity.this.startActivity(intent);
    }
    }) ;}

    public void setUserString() {
    	
    	String user = AuthenticatorActivity.getUserName();
    	TextView UserID = (TextView) findViewById(R.id.lUsername);
    	
    	if (user==null){
    		UserID.setText("You are not logged in");
    	}
    	else {
        
        UserID.setText("You are logged in as " + user);
    	}

    
    }

    
    		
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		menu.add(Menu.NONE, Constants.SEARCH, 1, R.string.menu_search).setIcon(android.R.drawable.ic_menu_search);
		menu.add(Menu.NONE, Constants.PREFERENCES, 2, R.string.menu_preferences).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(Menu.NONE, Constants.MY_BOOKS, 3, R.string.my_books).setIcon(android.R.drawable.ic_input_get);
		return result;
	}
	public boolean onSearchRequested() {
		startActivity(new Intent(this, SearchFormActivity.class));
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case Constants.SEARCH:
				startActivity(new Intent(this, SearchFormActivity.class));
				break;
			case Constants.PREFERENCES:
				startActivity(new Intent(this, EditPreferences.class));
				break;
			case Constants.MY_BOOKS:
				startActivity(new Intent(this, IssueListActivity.class));
				break;
		}
		return true;
	}
}
