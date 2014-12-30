package com.tyrantapp.olive;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.tyrantapp.olive.adapters.RecipientsListAdapter;
import com.tyrantapp.olive.components.RecipientsListView;
import com.tyrantapp.olive.services.SyncNetworkService;


public class MainActivity extends BaseActivity {
	// static variable
	static private final String		TAG = "MainActivity";
	
	// View
	private RecipientsListView		mRecipientsListView;
	private RecipientsListAdapter 	mRecipientsAdapter;
	
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	    // Initialize
		setContentView(R.layout.activity_main);
		
		mRecipientsListView = (RecipientsListView)findViewById(R.id.recipients_list_view);
		mRecipientsAdapter = new RecipientsListAdapter(this);
		mRecipientsListView.setAdapter(mRecipientsAdapter);

		mRecipientsListView.setOnItemClickListener(mOnItemClickListener);

        // Update User info
        Intent intent = new Intent(this, SyncNetworkService.class).setAction(SyncNetworkService.INTENT_ACTION_SYNC_USER_INFO);
        startService(intent);

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
		
		android.util.Log.d(TAG, "onStart");
        Intent intent = new Intent(this, SyncNetworkService.class)
        	.setAction(SyncNetworkService.INTENT_ACTION_SYNC_RECIPIENT_INFO);
        startService(intent);	
	}
	
	@Override
	public void onPause() {
		super.onPause();
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
        Intent intent = new Intent(this, AddRecipientActivity.class);
        startActivityForPasscode(intent);
	}

	public void onSetting(View v) {		
		Intent intent = new Intent(this, SettingActivity.class);
		startActivityForPasscode(intent);
	}
}
