package org.grameenfoundation.cch.activity;


import java.util.Calendar;
import java.util.Locale;


import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.activity.AppActivity;
import org.digitalcampus.oppia.activity.StartUpActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.AboutActivity;
import org.digitalcampus.oppia.activity.HelpActivity;
import org.grameenfoundation.cch.model.WebAppInterface;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract.Events;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.SearchView;


import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;



@SuppressLint("SetJavaScriptEnabled")
public class HomeActivity extends AppActivity implements OnSharedPreferenceChangeListener {
	Intent myIntent;
	public static final String TAG = HomeActivity.class.getSimpleName();
	private SharedPreferences prefs;

	private WebView myWebView;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
		PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
		
		setContentView(R.layout.cch_activity_home1);

		// set preferred lang to the default lang
		if (prefs.getString(getString(R.string.prefs_language), "").equals("")) {
			Editor editor = prefs.edit();
			editor.putString(getString(R.string.prefs_language), Locale.getDefault().getLanguage());
			editor.commit();
		}
				
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        
		myWebView = (WebView) findViewById(R.id.webView1);
		myWebView.getSettings().setJavaScriptEnabled(true);
		myWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
		myWebView.setWebViewClient(new WebViewClient(){
				
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
													
						if (url.equals("file:///android_asset/www/cch/modules/eventplanner/viewcal")) {
							Log.v(TAG, "Launching viewcal");
							   	Intent i = new Intent();
							   	ComponentName cn = new ComponentName("com.android.calendar", "com.android.calendar.LaunchActivity");
								i.setComponent(cn);
								startActivity(i);	 
								
						}
						else if(url.equals("file:///android_asset/www/cch/modules/eventplanner/addevent")){
							
								Log.v(TAG, "Launching addcal");
								  Calendar cal = Calendar.getInstance(); 
								   myIntent = new Intent(myIntent.ACTION_INSERT);
								   	myIntent.setType("vnd.android.cursor.item/event");
								    //myIntent.putExtra("Event type", "CCh" );
								    
								    myIntent.putExtra("beginTime", cal.getTimeInMillis());
								    myIntent.putExtra("allDay", true);	    
								    myIntent.putExtra("rrule", "FREQ=YEARLY");
								    myIntent.putExtra("endTime", cal.getTimeInMillis()+60*60*1000);
								    //myIntent.putExtra("title", pickevent.getSelectedItem().toString());
								    //myIntent.removeCategory("title");
								    myIntent.putExtra("description", "Description of your event");
								    myIntent.putExtra("eventLocation", "Please enter where you are ");
								    myIntent.putExtra(Events.ACCESS_LEVEL, Events.ACCESS_PRIVATE);
								    myIntent.putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);
								    startActivity(myIntent);	 
									
							
						}
						else {
							view.loadUrl(url);
							return true;
						}
						
						return true;
				}
		});
		
		String url = "file:///android_asset/www/cch/index.html";
		myWebView.loadUrl(url);
	}

	@Override
	public void onStart() {
		super.onStart();		
	}

	@Override
	public void onResume(){
		super.onResume();
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}
	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
	        myWebView.goBack();
	        return true;
	    } else if((keyCode == KeyEvent.KEYCODE_BACK) && !myWebView.canGoBack()) {
    		finish();
            return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_home, menu);

		// Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
     	SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
     	searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
     	// searchView.setIconifiedByDefault(false);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		UIUtils.showUserData(menu,this);
	    return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_settings:
				Intent i = new Intent(this, PrefsActivity.class);				
				startActivity(i);
				return true;
			
			case R.id.menu_about:
				startActivity(new Intent(this, AboutActivity.class));
				return true;
			case R.id.menu_help:
				startActivity(new Intent(this, HelpActivity.class));
				return true;
			case R.id.menu_logout:
				logout();
				return true;
		}
		return true;
	}

	private void logout() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setTitle(R.string.logout);
		builder.setMessage(R.string.logout_confirm);
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// wipe activity data
				DbHelper db = new DbHelper(HomeActivity.this);
				db.onLogout();
				db.close();

				// wipe user prefs
				/*Editor editor = prefs.edit();
				editor.putString(getString(R.string.prefs_username), "");
				editor.putString(getString(R.string.prefs_api_key), "");
				editor.putInt(getString(R.string.prefs_badges), 0);
				editor.putInt(getString(R.string.prefs_points), 0);
				editor.commit();*/

				// restart the app
				HomeActivity.this.startActivity(new Intent(HomeActivity.this, StartUpActivity.class));
				HomeActivity.this.finish();

			}
		});
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return; // do nothing
			}
		});
		builder.show();
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.d(TAG, key + " changed");
		if(key.equalsIgnoreCase(getString(R.string.prefs_server))){
			Editor editor = sharedPreferences.edit();
			if(!sharedPreferences.getString(getString(R.string.prefs_server), "").endsWith("/")){
				String newServer = sharedPreferences.getString(getString(R.string.prefs_server), "").trim()+"/";
				editor.putString(getString(R.string.prefs_server), newServer);
		    	editor.commit();
			}
		}
		if(key.equalsIgnoreCase(getString(R.string.prefs_points)) || key.equalsIgnoreCase(getString(R.string.prefs_badges))){
			supportInvalidateOptionsMenu();
		}
	}
}
