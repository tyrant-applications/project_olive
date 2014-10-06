package com.tyrantapp.olive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.R.id;
import com.tyrantapp.olive.adapter.ConversationListAdapter;
import com.tyrantapp.olive.adapter.RecipientsListAdapter;
import com.tyrantapp.olive.components.ConversationItem;
import com.tyrantapp.olive.components.RecipientItem;
import com.tyrantapp.olive.components.RecipientsListView;
import com.tyrantapp.olive.provider.OliveContentProvider;
import com.tyrantapp.olive.provider.OliveContentProvider.ConversationColumns;
import com.tyrantapp.olive.provider.OliveContentProvider.RecipientColumns;


public class MainActivity extends ActionBarActivity {
	private RecipientsListView		mRecipientsListView;
	
	private SectionsPagerAdapter 	mOliveKeyboardAdapter;
	private ViewPager				mOliveKeyboardPager;
	
	private Button					mSettingButton;
	
	private int						mLastExpandedGroupItem;
	
	private static int				sSelectedRecipientID;
	
	private boolean					mInputNewRecipient;
	private View					mRecipientRegisterView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
	    getActionBar().hide();
	    
	    // Initialize	   
		setContentView(R.layout.activity_main); 
		mRecipientsListView = (RecipientsListView)findViewById(R.id.recipients_list);
		
		LoadOliveListAdapter();
				
		// Fragment for Olive Keyboard
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mOliveKeyboardAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mOliveKeyboardPager = (ViewPager) findViewById(R.id.olive_keyboard_pager);
		mOliveKeyboardPager.setAdapter(mOliveKeyboardAdapter);
		mOliveKeyboardPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) {}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				if (arg0 == 4) {
					mOliveKeyboardPager.setCurrentItem(1, false);
				}
			}
			
			@Override
			public void onPageSelected(int arg0) {}
		});
		
		// Initialize Last Expanded Item
		mLastExpandedGroupItem = -1;
		mInputNewRecipient = false;
		
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
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below)
			
			return PlaceholderFragment.newInstance(position + 1);
		}

//		@Override
//		public long getItemId(int position) {
//			return position % 3 + 1;
//		}

		@Override
		public int getCount() {
			return 5;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_sectionNum).toUpperCase(l);
			case 1:
				return getString(R.string.title_sectionOlive00).toUpperCase(l);
			case 2:
				return getString(R.string.title_sectionOlive01).toUpperCase(l);
			case 3:
				return getString(R.string.title_sectionOlive02).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_olive, container, false);
			
			Button[] arrOliveBtn = new Button[12];
			arrOliveBtn[0]  = (Button)rootView.findViewById(R.id.olive_00_button);
			arrOliveBtn[1]  = (Button)rootView.findViewById(R.id.olive_10_button);
			arrOliveBtn[2]  = (Button)rootView.findViewById(R.id.olive_20_button);
			arrOliveBtn[3]  = (Button)rootView.findViewById(R.id.olive_01_button);
			arrOliveBtn[4]  = (Button)rootView.findViewById(R.id.olive_11_button);
			arrOliveBtn[5]  = (Button)rootView.findViewById(R.id.olive_21_button);
			arrOliveBtn[6]  = (Button)rootView.findViewById(R.id.olive_02_button);
			arrOliveBtn[7]  = (Button)rootView.findViewById(R.id.olive_12_button);
			arrOliveBtn[8]  = (Button)rootView.findViewById(R.id.olive_22_button);
			arrOliveBtn[9]  = (Button)rootView.findViewById(R.id.olive_03_button);
			arrOliveBtn[10] = (Button)rootView.findViewById(R.id.olive_13_button);
			arrOliveBtn[11] = (Button)rootView.findViewById(R.id.olive_23_button);
			
			switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
			case 1:
				arrOliveBtn[0].setText("1");
				arrOliveBtn[1].setText("2");
				arrOliveBtn[2].setText("3");
				arrOliveBtn[3].setText("4");
				arrOliveBtn[4].setText("5");
				arrOliveBtn[5].setText("6");
				arrOliveBtn[6].setText("7");
				arrOliveBtn[7].setText("8");
				arrOliveBtn[8].setText("9");
				arrOliveBtn[9].setText("-");
				arrOliveBtn[10].setText("0");
				arrOliveBtn[11].setText("NUM");
				break;
				
			case 2:
				arrOliveBtn[0].setText("Hey");
				arrOliveBtn[1].setText("Lunch?");
				arrOliveBtn[2].setText("Yes");
				arrOliveBtn[3].setText("Wassup?");
				arrOliveBtn[4].setText("Dinner?");
				arrOliveBtn[5].setText("No");
				arrOliveBtn[6].setText("Hi");
				arrOliveBtn[7].setText("Drink?");
				arrOliveBtn[8].setText("Maybe");
				arrOliveBtn[9].setText("Great Day!");
				arrOliveBtn[10].setText("Coffee?");
				arrOliveBtn[11].setText("Wait");
				break;
				
			case 3:
				arrOliveBtn[0].setText("Talk?");
				arrOliveBtn[1].setText("Where?");
				arrOliveBtn[2].setText("Yes");
				arrOliveBtn[3].setText("Meet?");
				arrOliveBtn[4].setText("When?");
				arrOliveBtn[5].setText("No");
				arrOliveBtn[6].setText("");
				arrOliveBtn[7].setText("With?");
				arrOliveBtn[8].setText("Maybe");
				arrOliveBtn[9].setText("");
				arrOliveBtn[10].setText("");
				arrOliveBtn[11].setText("Wait");
				break;
				
			case 4:
				arrOliveBtn[0].setText("Location");
				arrOliveBtn[1].setText("Map");
				arrOliveBtn[2].setText("Yes");
				arrOliveBtn[3].setText("Call");
				arrOliveBtn[4].setText("Time");
				arrOliveBtn[5].setText("No");
				arrOliveBtn[6].setText("Contact");
				arrOliveBtn[7].setText("Keyboard");
				arrOliveBtn[8].setText("Maybe");
				arrOliveBtn[9].setText("");
				arrOliveBtn[10].setText("");
				arrOliveBtn[11].setText("Wait");
				break;
				
			default:
				arrOliveBtn[11].setText("OLIVE");
				break;
			}

			OnClickListener onClickListener = new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Button view = (Button)arg0;
					ContentValues values = new ContentValues();
					
					if (sSelectedRecipientID >= 0) {
						values.put(ConversationColumns.RECIPIENT, sSelectedRecipientID);
						values.put(ConversationColumns.CTX_DETAIL, String.valueOf(view.getText()));
						
						//android.util.Log.d("Olive", "Insert Conversation = " + sSelectedRecipientID + " / " + String.valueOf(view.getText()));
						
						getActivity().getContentResolver().insert(ConversationColumns.CONTENT_URI, values);
					}
				}
			};
			
			arrOliveBtn[0].setOnClickListener(onClickListener);
			arrOliveBtn[1].setOnClickListener(onClickListener);
			arrOliveBtn[2].setOnClickListener(onClickListener);
			arrOliveBtn[3].setOnClickListener(onClickListener);
			arrOliveBtn[4].setOnClickListener(onClickListener);
			arrOliveBtn[5].setOnClickListener(onClickListener);
			arrOliveBtn[6].setOnClickListener(onClickListener);
			arrOliveBtn[7].setOnClickListener(onClickListener);
			arrOliveBtn[8].setOnClickListener(onClickListener);
			arrOliveBtn[9].setOnClickListener(onClickListener);
			arrOliveBtn[10].setOnClickListener(onClickListener);
			arrOliveBtn[11].setOnClickListener(onClickListener);
			
			return rootView;
		}
	}
	
	// Olive : Main(Chat) List
	private RecipientsListAdapter 	mRecipientsAdapter;
	
	public void LoadOliveListAdapter() {     
        // Set up our adapter
        mRecipientsAdapter = new RecipientsListAdapter(this, null);
        mRecipientsListView.setAdapter(mRecipientsAdapter);
        
        mRecipientsListView.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View child, int nParam, long lParam) {
				final TextView view = (TextView)child.findViewById(R.id.recipient_name);
				final EditText edit = (EditText)child.findViewById(R.id.recipient_edit);
				
				
				if (view.getText() == "+") {
					if (!mInputNewRecipient) {
						mRecipientRegisterView = child;
						
						view.setVisibility(View.GONE);
						edit.setVisibility(View.VISIBLE);
						edit.requestFocus();
						
						InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				        inputMethodManager.showSoftInput(edit, InputMethodManager.SHOW_FORCED);
				       
						edit.setOnEditorActionListener(new OnEditorActionListener() {
							@Override
							public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
								if (actionId == EditorInfo.IME_ACTION_DONE) {
						        	String pszTag = edit.getText().toString();
						        	
						        	if (!pszTag.isEmpty()) {										
										ContentValues values = new ContentValues();
										values.put(RecipientColumns.USERNAME, pszTag);
										values.put(RecipientColumns.UNREAD, false);
										getContentResolver().insert(RecipientColumns.CONTENT_URI, values);
										
										android.util.Log.d("Olive", "Insert recipient!");
						        	} else {
										android.util.Log.d("Olive", "Failed Insert recipient!");
						        	}

									view.setVisibility(View.VISIBLE);
									edit.setVisibility(View.GONE);
									
									mInputNewRecipient = false;
									
						        }
								return false;
							}
						});
												
						mInputNewRecipient = true;
						
						android.util.Log.d("Olive", "Prepare new recipient!");
						
						return true;
					}
				} else {
					if (mInputNewRecipient) {
						TextView subName = (TextView)mRecipientRegisterView.findViewById(R.id.recipient_name);
						EditText subEdit = (EditText)mRecipientRegisterView.findViewById(R.id.recipient_edit);

						android.util.Log.d("Olive", "Failed Insert recipient!");

						subName.setVisibility(View.VISIBLE);
						subEdit.setVisibility(View.GONE);
												
						mInputNewRecipient = false;
						
						mRecipientRegisterView = null;
					}
				}
				return false;
			}        	
        });
        
        mRecipientsListView.setOnGroupExpandListener(new OnGroupExpandListener() {
			@Override
			public void onGroupExpand(int arg0) {
				android.util.Log.e("Olive", "Expandable!");				
				if (mLastExpandedGroupItem >= 0) {
					mRecipientsListView.collapseGroup(mLastExpandedGroupItem);			
				}
				mLastExpandedGroupItem = arg0;
				
				Cursor cursor = getContentResolver().query(RecipientColumns.CONTENT_URI, new String[] {RecipientColumns._ID,  }, null, null, null);
				if (cursor.moveToPosition(arg0)) {
					sSelectedRecipientID = cursor.getInt(cursor.getColumnIndex(RecipientColumns._ID));
				} else {
					sSelectedRecipientID = -1;
				}
				

				InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
               
				if(getCurrentFocus() != null) {
			        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
			    }
				
				mOliveKeyboardPager.setVisibility(View.VISIBLE);
				mSettingButton.setVisibility(View.INVISIBLE);
				
				mRecipientsListView.SetScrollable(false);
			}
        });
        
        mRecipientsListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {
			@Override
			public void onGroupCollapse(int arg0) {
				mOliveKeyboardPager.setVisibility(View.GONE);
				mSettingButton.setVisibility(View.VISIBLE);
				mLastExpandedGroupItem = -1;
				
				mRecipientsListView.SetScrollable(true);
			}
        });
    }

	public void onSetting(View v) {
		//TextView view = (TextView)findViewById(R.id.text); //View 포함
		
		if (false) {
			Cursor cursor = getContentResolver().query(RecipientColumns.CONTENT_URI, new String[] { RecipientColumns._ID, RecipientColumns.USERNAME, RecipientColumns.UNREAD}, null, null, null);
			cursor.moveToFirst();
			for (int i=0; i<cursor.getCount(); i++) {
				int nID = cursor.getInt(cursor.getColumnIndex(RecipientColumns._ID));
				String pszUsername = cursor.getString(cursor.getColumnIndex(RecipientColumns.USERNAME));
				boolean bUnread = cursor.getInt(cursor.getColumnIndex(RecipientColumns.UNREAD)) > 0;
				
				Cursor cursorChild = getContentResolver().query(ConversationColumns.CONTENT_URI, new String[] { ConversationColumns._ID, ConversationColumns.RECIPIENT, ConversationColumns.CTX_DETAIL}, "recipient=?", new String[] {String.valueOf(nID), }, null);
				int nChildCount = cursorChild.getCount();
				
				android.util.Log.d("Olive", "* [" + nID + "] " + pszUsername + " (" + bUnread + ") => " + nChildCount);
				
				cursorChild.moveToFirst();
				for (int j=0; j<nChildCount; j++) {
					int nChildID = cursorChild.getInt(cursorChild.getColumnIndex(ConversationColumns._ID));
					int nParentID = cursorChild.getInt(cursorChild.getColumnIndex(ConversationColumns.RECIPIENT));
					String pszText = cursorChild.getString(cursorChild.getColumnIndex(ConversationColumns.CTX_DETAIL));
					
					android.util.Log.d("Olive", "*** [" + nParentID + " : " + nChildID + "] " + pszText);
					
					cursorChild.moveToNext();
				}
				cursor.moveToNext();
			}
			
			android.util.Log.d("Olive", "**********************" );
			Cursor cursorChild2 = getContentResolver().query(ConversationColumns.CONTENT_URI, new String[] { ConversationColumns._ID, ConversationColumns.RECIPIENT, ConversationColumns.CTX_DETAIL}, null, null, null);
			cursorChild2.moveToFirst();
			for (int j=0; j<cursorChild2.getCount(); j++) {
				int nChildID = cursorChild2.getInt(cursorChild2.getColumnIndex(ConversationColumns._ID));
				int nParentID = cursorChild2.getInt(cursorChild2.getColumnIndex(ConversationColumns.RECIPIENT));
				String pszText = cursorChild2.getString(cursorChild2.getColumnIndex(ConversationColumns.CTX_DETAIL));
				
				android.util.Log.d("Olive", "*** [" + nParentID + " : " + nChildID + "] " + pszText);
				
				cursorChild2.moveToNext();
			}
		}
		
		Intent intent = new Intent(this, SettingsActivity.class);
		this.startActivity(intent);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	@Override
	public void onBackPressed() {
		if (mLastExpandedGroupItem >= 0) {
			mRecipientsListView.collapseGroup(mLastExpandedGroupItem);			
		} else {
			super.onBackPressed();
		}
	}
}
