package com.tyrantapp.olive;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.tyrantapp.olive.adapter.RecipientsListAdapter;
import com.tyrantapp.olive.component.RecipientsListView;
import com.tyrantapp.olive.configuration.Constants;
import com.tyrantapp.olive.helper.DatabaseHelper;


public class MainActivity extends BaseActivity {
	// static variable
	static private final String		TAG = "MainActivity";
	
	// View
	private RecipientsListView		mRecipientsListView;
	private RecipientsListAdapter 	mRecipientsAdapter;

    // Intent
    private Intent                  mPassIntent;
	
	// listener
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// Find current item's recipient ID and start conversation activity
			android.util.Log.d(TAG, "id = " + id);

            long idRoom = DatabaseHelper.SpaceHelper.getRoomId(MainActivity.this, id);
			Intent intent = new Intent(getApplicationContext(), ConversationActivity.class).putExtra(Constants.Intent.EXTRA_ROOM_ID, idRoom);
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

        Cursor cRecipient = DatabaseHelper.RecipientHelper.getCursor(this);
        Cursor cSpace = DatabaseHelper.SpaceHelper.getCursor(this);
        Cursor cChat = DatabaseHelper.ChatSpaceHelper.getCursor(this);

        if (cRecipient != null && cRecipient.getCount() > 0) {
            cRecipient.moveToFirst();
            android.util.Log.d(TAG, "Recipient Data");
            do {
                for (int i = 0; i < cRecipient.getColumnCount(); i++) {
                    android.util.Log.d(TAG, "        " + cRecipient.getColumnName(i) + " : " + cRecipient.getString(i));
                }
            } while (cRecipient.moveToNext());
        }

        if (cSpace != null && cSpace.getCount() > 0) {
            cSpace.moveToFirst();
            android.util.Log.d(TAG, "Space Data");
            do {
                for (int i = 0; i < cSpace.getColumnCount(); i++) {
                    android.util.Log.d(TAG, "        " + cSpace.getColumnName(i) + " : " + cSpace.getString(i));
                }
            } while (cSpace.moveToNext());
        }

        if (cChat != null && cChat.getCount() > 0) {
            cChat.moveToFirst();
            android.util.Log.d(TAG, "ChatSpace Data");
            do {
                for (int i = 0; i < cChat.getColumnCount(); i++) {
                    android.util.Log.d(TAG, "        " + cChat.getColumnName(i) + " : " + cChat.getString(i));
                }
            } while (cChat.moveToNext());
        }

        setEnablePasscode(true);
    }

// Intent가 Main에서 Splash로 옮겨지게 됨에 따라 필요 없어짐.
//	@Override
//	protected void onNewIntent(Intent intent) {
//		super.onNewIntent(intent);
//        passToConversationActivity(intent);
//        setIntent(null);
//	}

	@Override
	public void onStart() {
		super.onStart();
		
		android.util.Log.d(TAG, "onStart");

        // Access to conversation activity directly
        passToConversationActivity();
    }
	
	@Override
	public void onPause() {
		super.onPause();
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
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

    private boolean passToConversationActivity() {
        boolean bRet = false;
        // Access to conversation activity directly
        if (isAuthenticated()) {
            Intent intent = getIntent();
            long idRoom = intent.getLongExtra(Constants.Intent.EXTRA_ROOM_ID, -1);
            if (idRoom >= 0) {
                Intent newIntent = new Intent(getApplicationContext(), ConversationActivity.class).putExtra(Constants.Intent.EXTRA_ROOM_ID, idRoom);
                startActivityForPasscode(newIntent);
                bRet = true;
            }
            getIntent().removeExtra(Constants.Intent.EXTRA_ROOM_ID);
        }
        return bRet;
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
