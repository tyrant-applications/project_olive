package com.tyrantapp.olive;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.R.id;
import com.tyrantapp.olive.R.layout;
import com.tyrantapp.olive.R.menu;
import com.tyrantapp.olive.adapters.ConversationListAdapter;
import com.tyrantapp.olive.adapters.KeypadPagerAdapter;
import com.tyrantapp.olive.components.ConversationListView;
import com.tyrantapp.olive.fragments.KeypadFragment;
import com.tyrantapp.olive.interfaces.OnOliveKeypadListener;
import com.tyrantapp.olive.providers.OliveContentProvider.ConversationColumns;
import com.tyrantapp.olive.providers.OliveContentProvider.RecipientColumns;
import com.tyrantapp.olive.services.SyncNetworkService;
import com.tyrantapp.olive.types.OliveMessage;
import com.tyrantapp.olive.types.UserInfo;
import com.tyrantapp.olive.BaseActivity.OnConnectServiceListener;

import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class ConversationActivity extends BaseActivity implements OnOliveKeypadListener {
	final static private String			TAG = "ConversationActivity";

	final static private int			RESULT_LOAD_IMAGE	= 1;
	final static private int			RESULT_TAKE_PICTURE	= 2;
	final static private int			RESULT_RECORD_VIDEO	= 3;
	final static private int			RESULT_RECORD_VOICE	= 4;
	final static private int			RESULT_GET_LOCATION	= 5;
	
	final static public String			EXTRA_FROM	= "from";
	final static public String			EXTRA_TO	= "to";
	
	
	private long						mRecipientId;
	private String						mUsername;
	private String						mRecipientName;
	
	private ConversationListAdapter		mConversationAdapter;
	private ConversationListView		mConversationListView;
	
	private TextView					mLastOliveText;
	private Button						mLastOliveExpander;
	
	private ViewFlipper					mInputMethodFlipper;
	private View						mInputMethodKeypad;
	private View						mInputMethodText;
	private boolean						mTypingMode;
	
	private KeypadPagerAdapter			mKeypadPagerAdapter;
	private ViewPager					mKeypadPager;
	
	private View						mOliveText;
	private View						mOliveGallery;
	private View						mOliveCamera;
	private View						mOliveVoice;
	private View						mOliveLocation;
	
	private EditText					mTextEditor;
	private Button						mTextSender;
	
	private DataSetObserver	mConversationObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			Cursor cursor = (Cursor) mConversationAdapter.getCursor();
			cursor.moveToLast();
			
			updateLastOlive(cursor);
			
			super.onChanged();
		}
	};
	
	private OnItemClickListener	mOnConversationClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Cursor cursor = (Cursor) mConversationAdapter.getItem(position);
			
			updateLastOlive(cursor);
		}		
	};
	
	private OnClickListener	mOnTextClickListener = new OnClickListener() {
		public void onClick(View view) {
			if (!mTypingMode) {
				changeTypingMode();
			}			
		}
	};
	
	private OnClickListener	mOnGalleryClickListener = new OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(intent, RESULT_LOAD_IMAGE);
		}
	};
	
	private OnClickListener	mOnCameraClickListener = new OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(intent, RESULT_TAKE_PICTURE);
		}
	};
	
	private OnClickListener	mOnVoiceClickListener = new OnClickListener() {
		public void onClick(View view) {
		}
	};
	
	private OnClickListener	mOnLocationClickListener = new OnClickListener() {
		public void onClick(View view) {
		}
	};
	
	private OnClickListener	mOnSendClickListener = new OnClickListener() {
		public void onClick(View view) {
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_conversation);
		
		// Initialize
		Intent intent = getIntent();
		
		if (intent != null) {
			mUsername = intent.getStringExtra(EXTRA_FROM);
			mRecipientName = intent.getStringExtra(EXTRA_TO);
			
			getIntent().removeExtra(EXTRA_FROM);
			getIntent().removeExtra(EXTRA_TO);
			getIntent().removeExtra("TEST");
			
			android.util.Log.d(TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~> " + mRecipientName + " / " + mUsername + " / " + intent.getStringExtra("TEST"));
			
			mRecipientId = intent.getLongExtra(ConversationColumns.RECIPIENT_ID, -1);
			if (mRecipientId < 0) {
				Cursor cursor = getContentResolver().query(
						RecipientColumns.CONTENT_URI, 
						new String[] { RecipientColumns._ID, },
						RecipientColumns.USERNAME + "=?",
						new String[] { mUsername, },
						null);
				
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					mRecipientId = cursor.getLong(cursor.getColumnIndex(RecipientColumns._ID));
				} else {
					Toast.makeText(this, R.string.toast_error_no_registered_recipient, Toast.LENGTH_SHORT).show();
					finish();
					return;
				}
			}
		}		
		
		if (mRecipientId >= 0) {
			mConversationAdapter = new ConversationListAdapter(this, mRecipientId);
			mConversationListView = (ConversationListView) findViewById(R.id.conversations_list_view);
			mConversationListView.setAdapter(mConversationAdapter);
			
			mConversationListView.setOnItemClickListener(mOnConversationClickListener);
			mConversationAdapter.registerDataSetObserver(mConversationObserver);
		} else {
			Toast.makeText(this,  "Invalid recipient id.", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		// Last Olive (interaction mode)
		mLastOliveText = (TextView) findViewById(R.id.conversation_last_olive_text);
		mLastOliveExpander = (Button) findViewById(R.id.conversation_last_olive_expander);
		
		// IME mode
		mTypingMode = false;
		mInputMethodFlipper = (ViewFlipper) findViewById(R.id.input_method_flipper);
		mInputMethodKeypad = (View) findViewById(R.id.input_method_keypad);
		mInputMethodText = (View) findViewById(R.id.input_method_text);

		// Fragment for Olive Keyboard
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mKeypadPagerAdapter = new KeypadPagerAdapter(getSupportFragmentManager(), this);
		
		// Set up the ViewPager with the sections adapter.
		mKeypadPager = (ViewPager) findViewById(R.id.olive_keypad_pager);
		mKeypadPager.setAdapter(mKeypadPagerAdapter);		
		
		// footer
		mOliveText = findViewById(R.id.olive_extend_text);
		mOliveGallery = findViewById(R.id.olive_extend_gallery);
		mOliveCamera = findViewById(R.id.olive_extend_camera);
		mOliveVoice = findViewById(R.id.olive_extend_voice);
		mOliveLocation = findViewById(R.id.olive_extend_location);
		
		mOliveText.setOnClickListener(mOnTextClickListener);
		mOliveGallery.setOnClickListener(mOnGalleryClickListener);
		mOliveCamera.setOnClickListener(mOnCameraClickListener);
		mOliveVoice.setOnClickListener(mOnVoiceClickListener);
		mOliveLocation.setOnClickListener(mOnLocationClickListener);	
		
		// Text mode
		mTextEditor = (EditText) findViewById(R.id.olive_extend_text_editor);
		mTextSender = (Button) findViewById(R.id.olive_extend_send_button);
		
		mTextSender.setOnClickListener(mOnSendClickListener);
		
		// Update UI
		if (mConversationAdapter != null) {
			TextView tv = (TextView) findViewById(R.id.title_name);
			tv.setText(mConversationAdapter.getNickName());
			
			Cursor cursor = mConversationAdapter.getCursor();
			if (cursor != null) cursor.moveToLast();
			updateLastOlive(cursor);
		
		}
		
		android.util.Log.d(TAG, "onCreate Finished");
	}

	@Override
	public void onStart() {
		android.util.Log.d(TAG, "onStart Begin");
		super.onStart();

        Intent intent = new Intent(this, SyncNetworkService.class)
        	.setAction(SyncNetworkService.INTENT_ACTION_MARK_TO_READ)
        	.putExtra(SyncNetworkService.EXTRA_RECIPIENTNAME, mRecipientName);
        startService(intent);
        
        android.util.Log.d(TAG, "onStart Finished");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		ContentValues values = new ContentValues();
		values.put(ConversationColumns.IS_READ, "true");
		
		getContentResolver().update(
				ConversationColumns.CONTENT_URI, 
				values, 
				ConversationColumns.RECIPIENT_ID + "=" + mRecipientId, 
				null);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.conversation, menu);
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
	
	@Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	     super.onActivityResult(requestCode, resultCode, data);
	      
	     if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
	         Uri selectedImage = data.getData();
	         String[] filePathColumn = { MediaStore.Images.Media.DATA };
	 
	         Cursor cursor = getContentResolver().query(selectedImage,
	                 filePathColumn, null, null, null);
	         cursor.moveToFirst();
	 
	         int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	         String picturePath = cursor.getString(columnIndex);
	         cursor.close();
	                      
	         // String picturePath contains the path of selected Image
	     } else
    	 if (requestCode == RESULT_TAKE_PICTURE && resultCode == RESULT_OK) {  
             Bitmap bmpPicture = (Bitmap) data.getExtras().get("data"); 
             //imageView.setImageBitmap(bmpPicture);
         }
	}

	@Override
	public void onBackPressed() {
		if (mTypingMode) {
			changeNormalMode();
		} else {
			super.onBackPressed();
		}
	}
	
	@Override
	public void onKeypadCreate(int sectionNumber) {
		KeypadFragment fragment = (KeypadFragment)KeypadFragment.getFragment(sectionNumber);
		
		switch (sectionNumber) {
		case 0:
			((Button)fragment.getOliveButton(0)).setText("Eat");
			((Button)fragment.getOliveButton(1)).setText("Can We Meet?");
			((Button)fragment.getOliveButton(2)).setText("Yes");
			((Button)fragment.getOliveButton(3)).setText("Where?");
			((Button)fragment.getOliveButton(4)).setText("Coffee");
			((Button)fragment.getOliveButton(5)).setText("Can We Talk?");
			((Button)fragment.getOliveButton(6)).setText("No");
			((Button)fragment.getOliveButton(7)).setText("When?");
			((Button)fragment.getOliveButton(8)).setText("Pub");
			((Button)fragment.getOliveButton(9)).setText("Wanna Do Something?");
			((Button)fragment.getOliveButton(10)).setText("Busy");
			((Button)fragment.getOliveButton(11)).setText("With?");
			break;
		default:
		}		
	}

	@Override
	public void onKeypadClick(int sectionNumber, int index) {
		KeypadFragment fragment = (KeypadFragment)KeypadFragment.getFragment(sectionNumber);
		Button view = (Button)fragment.getOliveButton(index);
		
        Intent intent = new Intent(this, SyncNetworkService.class)
        	.setAction(SyncNetworkService.INTENT_ACTION_POST_OLIVE)
        	.putExtra(SyncNetworkService.EXTRA_RECIPIENTNAME, mRecipientName)
        	.putExtra(SyncNetworkService.EXTRA_MESSAGE, String.valueOf(view.getText()));
        startService(intent);	

//		ContentValues values = new ContentValues();
//		OliveMessage msg = null; 
//		
//		if (mRecipientId >= 0) {
//			// Send Message to Server
//			msg = mRESTHelper.postOlive(mRecipientName, String.valueOf(view.getText()));
//			if (msg != null) {
//				values.put(ConversationColumns.RECIPIENT_ID, mRecipientId);
//				values.put(ConversationColumns.CTX_DETAIL, msg.mContext);
//				values.put(ConversationColumns.IS_RECV, false);
//				values.put(ConversationColumns.IS_PENDING, false);
//				values.put(ConversationColumns.IS_READ, false);
//				values.put(ConversationColumns.MODIFIED, msg.mModified);
//				
//				getContentResolver().insert(ConversationColumns.CONTENT_URI, values);
//			} else {
//				Toast.makeText(this, R.string.toast_failed_post_olive, Toast.LENGTH_SHORT).show();
//			}				
//		}
	}
	
	public void updateLastOlive(Cursor cursor) {
		if (cursor != null && cursor.getCount() > 0) {
			String content = cursor.getString(cursor.getColumnIndex(ConversationColumns.CTX_DETAIL));
			mLastOliveText.setText(content);
		} else {
			mLastOliveText.setText("Hello?");
		}
	}
	
	public boolean isTypingMode() {
		return mTypingMode;
	}
	
	public void changeTypingMode() {
		if (!mTypingMode) {
			mTypingMode = true;
			//mInputMethodFlipper.showNext();
			mInputMethodKeypad.setVisibility(View.GONE);
			mInputMethodText.setVisibility(View.VISIBLE);
			showSoftImputMethod(mTextEditor);
		}
	}
	
	public void changeNormalMode() {
		if (mTypingMode) {
			mTypingMode = false;
			//mInputMethodFlipper.showPrevious();
			mInputMethodKeypad.setVisibility(View.VISIBLE);
			mInputMethodText.setVisibility(View.GONE);
			hideSoftInputMethod(mTextEditor);
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
