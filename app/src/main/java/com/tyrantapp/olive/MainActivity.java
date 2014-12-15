package com.tyrantapp.olive;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;

import com.tyrantapp.olive.adapters.RecipientsListAdapter;
import com.tyrantapp.olive.components.RecipientsListView;
import com.tyrantapp.olive.helper.OliveHelper;
import com.tyrantapp.olive.providers.OliveContentProvider.RecipientColumns;
import com.tyrantapp.olive.services.SyncNetworkService;
import com.tyrantapp.olive.types.UserInfo;


public class MainActivity extends BaseActivity {
	// static variable
	static private final String		TAG = "MainActivity";
	
	// View
	private RecipientsListView		mRecipientsListView;
	private RecipientsListAdapter 	mRecipientsAdapter;
	private ViewFlipper				mFooterFlipper;
	private EditText				mRecipientEdit;
	

	// for common
	private UserInfo				mUserInfo;
	private String					mRecipientName;
	private boolean 				mFooterFlipped;

	
	// listener
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// Find current item's recipient ID and start conversation activity
			android.util.Log.d(TAG, "id = " + id);
			
			Intent intent = new Intent(getApplicationContext(), ConversationActivity.class)
				.putExtra(ConversationActivity.EXTRA_RECIPIENT_ID, id);
			startActivityForPasscode(intent);
		}
	};
	
	private OnEditorActionListener mOnEditorActionListener = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				onAddRecipient(null);
			}
			return false;
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	    // Initialize
		setContentView(R.layout.activity_main);
		
		mFooterFlipped = false;
		
		mRecipientsListView = (RecipientsListView)findViewById(R.id.recipients_list_view);
		mRecipientsAdapter = new RecipientsListAdapter(this);
		mRecipientsListView.setAdapter(mRecipientsAdapter);

		mRecipientsListView.setOnItemClickListener(mOnItemClickListener);
		

		// prepare components
		mFooterFlipper = (ViewFlipper) findViewById(R.id.footer_flipper);
		mRecipientEdit = (EditText) findViewById(R.id.recipient_edit);
		
		// register event
		mRecipientEdit.setOnEditorActionListener(mOnEditorActionListener);
		
		// Access to conversation activity directly
		PassToConversationActivity(getIntent());
		
		setEnablePasscode(true);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		PassToConversationActivity(intent);
	}
	
	private void PassToConversationActivity(Intent intent) {
		// Access to conversation activity directly
		long recipientId = intent.getLongExtra(ConversationActivity.EXTRA_RECIPIENT_ID, -1);
		if (recipientId >= 0) {
			Intent newIntent = new Intent(getApplicationContext(), ConversationActivity.class).putExtra(ConversationActivity.EXTRA_RECIPIENT_ID, recipientId);
			startActivityForPasscode(newIntent);
        }
	}

	@Override
	public void onStart() {
		super.onStart();
		
		mUserInfo = mRESTHelper.getUserProfile();

		android.util.Log.d(TAG, "onStart");
        Intent intent = new Intent(this, SyncNetworkService.class)
        	.setAction(SyncNetworkService.INTENT_ACTION_SYNC_RECIPIENT_INFO);
        startService(intent);	
	}
	
	@Override
	public void onPause() {
		super.onPause();
		flipFooter(false);
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

	public void onVoting(View v) {		
		Toast.makeText(this,  getResources().getString(R.string.error_not_supported_yet), Toast.LENGTH_SHORT).show();
	}
	
	public void onAdding(View v) {		
		flipFooter(true);
	}
	
	public void onAddRecipient(View v) {
		String pszTag = mRecipientEdit.getText().toString();
		
		if (!pszTag.isEmpty()) {
			UserInfo info =  mRESTHelper.getRecipientProfile(pszTag);
			
			if (info != null) {
				ContentValues values = new ContentValues();
				values.put(RecipientColumns.USERNAME, pszTag);
				values.put(RecipientColumns.NICKNAME, pszTag);
				values.put(RecipientColumns.UNREAD, false);
				
				getContentResolver().insert(RecipientColumns.CONTENT_URI, values);	
				android.util.Log.d("Olive", "Insert recipient!");
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_succeed_add_recipient), Toast.LENGTH_SHORT).show();
			} else {
				android.util.Log.d("Olive", "Failed Insert recipient!");
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_failed_add_recipient), Toast.LENGTH_SHORT).show();
			}
		} else {
			android.util.Log.d("Olive", "Failed Insert recipient!");
		}

		flipFooter(false);
	}
	
	public void onSetting(View v) {		
		Intent intent = new Intent(this, SettingActivity.class);
		startActivityForPasscode(intent);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		boolean isActionBack = event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP;
		
		if (isFooterFlipped() && isActionBack) {
			flipFooter(false);
			return true;
		} else {
			return super.dispatchKeyEvent(event);
		}
	}
	
	public boolean isFooterFlipped() {
		return mFooterFlipped;
	}
	
	public void flipFooter(boolean bFlip) {
		if (bFlip) {
			if (!isFooterFlipped()) {
				mFooterFlipped = true;
				mFooterFlipper.showNext();
				showSoftImputMethod(mRecipientEdit);
			}
		} else {
			if (isFooterFlipped()) {
				mFooterFlipped = false;
				mFooterFlipper.showPrevious();					
				hideSoftInputMethod(mRecipientEdit);
			}
		}
	}
	
	public void showSoftImputMethod(final EditText focusView) {
		if (focusView != null) {
			focusView.requestFocus();
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(focusView, InputMethodManager.SHOW_IMPLICIT);
		}
	}
	
	public void hideSoftInputMethod(final EditText focusView) {
		if (focusView != null) {
			focusView.setText(null);
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
		}
	}
}
