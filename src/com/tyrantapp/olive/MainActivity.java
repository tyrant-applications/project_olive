package com.tyrantapp.olive;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.adapter.RecipientsListAdapter;
import com.tyrantapp.olive.components.RecipientsListView;
import com.tyrantapp.olive.fragments.RootFragment;
import com.tyrantapp.olive.providers.OliveContentProvider.ConversationColumns;


public class MainActivity extends ActionBarActivity {
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
		public RootFragmentsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return RootFragment.newInstance(position);
		}

		@Override
		public int getCount() {
			return 1;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class ParentFragment extends Fragment {
		// for Recipient Fragments
		private RecipientsListView			mRecipientsListView;
		private RecipientsListAdapter 		mRecipientsAdapter;
		
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static ParentFragment newInstance(int sectionNumber) {
			ParentFragment fragment = new ParentFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			
			return fragment;
		}

		public ParentFragment() {}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = null;
			
			switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
			// Recipients Fragment
			case 0:
				rootView = inflater.inflate(R.layout.fragment_recipients, container, false);

				if (rootView != null) {
					mRecipientsListView = (RecipientsListView)rootView.findViewById(R.id.recipients_list_view);
					PrepareRecipientsFragment();
				}
				break;
				
			default:
			}
			
			return rootView;
		}
		

		public void PrepareRecipientsFragment() {
			// Set up our adapter
			final Activity activity = getActivity();
			
			mRecipientsAdapter = new RecipientsListAdapter(activity);
			mRecipientsListView.setAdapter(mRecipientsAdapter);

			mRecipientsListView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					// Find current item's recipient ID and start conversation activity
					
					android.util.Log.d(TAG, "id = " + id);
					
					if (id >= 0) {
						Intent intent = new Intent(getActivity(), ConversationActivity.class)
							.putExtra(ConversationColumns.RECIPIENT, id);
						startActivity(intent);
					}
				}
			});

		}
	}
	
	public void onSetting(View v) {		
		Intent intent = new Intent(this, SettingsActivity.class);
		this.startActivity(intent);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
}
