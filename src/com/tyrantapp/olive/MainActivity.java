package com.tyrantapp.olive;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.R.id;
import com.tyrantapp.olive.R.layout;
import com.tyrantapp.olive.R.menu;
import com.tyrantapp.olive.adapters.RecipientsListAdapter;
import com.tyrantapp.olive.components.RecipientsListView;
import com.tyrantapp.olive.fragments.ParentsFragment;


public class MainActivity extends FragmentActivity {
	// static variable
	static private final String		TAG = "MainActivity";
			
	
	// for Layout
	private RootFragmentsPagerAdapter	mRootFragmentsAdapter;
	private ViewPager					mRootFragmentsPager;
	
	// for Setting
	private Button						mSettingButton;

		@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
	    //getActionBar().hide();
	    
	    // Initialize
		setContentView(R.layout.activity_main);
		
		// Load main fragments
		mRootFragmentsAdapter = new RootFragmentsPagerAdapter(getSupportFragmentManager());
		mRootFragmentsPager = (ViewPager) findViewById(R.id.root_fragments_pager);
		mRootFragmentsPager.setAdapter(mRootFragmentsAdapter);
				
		// Setting Button
		mSettingButton = (Button) findViewById(R.id.setting_button);		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class RootFragmentsPagerAdapter extends FragmentPagerAdapter {
		public RootFragmentsPagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public int getCount() {
			return 1;
		}

		@Override
		public Fragment getItem(int sectionNumber) {
			return ParentsFragment.newInstance(sectionNumber);
		}
	}
	
	public void onSetting(View v) {		
		Intent intent = new Intent(this, SettingsActivity.class);
		this.startActivity(intent);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
}
