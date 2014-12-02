package com.tyrantapp.olive;

import java.util.Set;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.adapters.ConversationListAdapter;
import com.tyrantapp.olive.adapters.KeypadPagerAdapter;
import com.tyrantapp.olive.components.ConversationListView;
import com.tyrantapp.olive.fragments.KeypadFragment;
import com.tyrantapp.olive.interfaces.OnOliveKeypadListener;
import com.tyrantapp.olive.providers.OliveContentProvider.ConversationColumns;
import com.tyrantapp.olive.providers.OliveContentProvider.RecipientColumns;
import com.tyrantapp.olive.services.SyncNetworkService;

import android.support.v4.view.ViewPager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
	
	final static public String			EXTRA_RECIPIENTNAME	= "recipient";
	final static public String			EXTRA_RECIPIENT_ID	= "recipient_id";
	
	
	private long						mRecipientId;
	private String						mRecipientName;
	
	private ConversationListAdapter		mConversationAdapter;
	private ConversationListView		mConversationListView;
	
	private TextView					mLastOliveText;
	private View						mLastOliveExpander;
	
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
			android.util.Log.d(TAG, "onSendText :: " + mRecipientName + " / " + mTextEditor.getText());
			
			Intent intent = new Intent(getApplicationContext(), SyncNetworkService.class)
	        	.setAction(SyncNetworkService.INTENT_ACTION_POST_OLIVE)
	        	.putExtra(SyncNetworkService.EXTRA_RECIPIENTNAME, mRecipientName)
	        	.putExtra(SyncNetworkService.EXTRA_MESSAGE, String.valueOf(mTextEditor.getText()));
	        startService(intent);
	        
	        changeNormalMode();
		}
	};
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_conversation);
		
		// Initialize
		mRecipientId = getIntent().getLongExtra(EXTRA_RECIPIENT_ID, -1);
		mRecipientName = getIntent().getStringExtra(EXTRA_RECIPIENTNAME);
		
		if (mRecipientName != null) {
			getIntent().removeExtra(ConversationActivity.EXTRA_RECIPIENTNAME);
			android.util.Log.d(TAG, "remove result = " + getIntent().getStringExtra(EXTRA_RECIPIENTNAME));
			
			android.util.Log.d(TAG, "Message from " + mRecipientName + " (" + mRecipientId + ")");
			
			if (mRecipientName != null) {
				Cursor cursor = getContentResolver().query(
						RecipientColumns.CONTENT_URI, 
						new String[] { RecipientColumns._ID, },
						RecipientColumns.USERNAME + "=?",
						new String[] { mRecipientName, },
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
		mLastOliveExpander = (View) findViewById(R.id.conversation_last_olive_expander);
		
		// IME mode
		mTypingMode = false;
		mInputMethodFlipper = (ViewFlipper) findViewById(R.id.input_method_flipper);
		mInputMethodKeypad = (View) findViewById(R.id.input_method_keypad);
		mInputMethodText = (View) findViewById(R.id.input_method_text);

		// Fragment for Olive Keyboard
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mKeypadPagerAdapter = new KeypadPagerAdapter(getSupportFragmentManager(), this, new int[] { KeypadFragment.TYPE_KEYPAD_12, KeypadFragment.TYPE_KEYPAD_2, KeypadFragment.TYPE_KEYPAD_12 });
		
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
	}

	@Override
	public void onStart() {
		super.onStart();

        Intent intent = new Intent(this, SyncNetworkService.class)
        	.setAction(SyncNetworkService.INTENT_ACTION_MARK_TO_READ)
        	.putExtra(SyncNetworkService.EXTRA_RECIPIENTNAME, mRecipientName);
        startService(intent);
        
        sCurrentRecipientName = mRecipientName;        
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
	public void onStop() {
		super.onStop();
		
		sCurrentRecipientName = "";		
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
		/*  // first resource
		 * ((Button)fragment.getOliveButton(0)).setText("Eat");
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
		 */
		case 0:
			((Button)fragment.getOliveButton(0)).setText("(Food?)");
			((Button)fragment.getOliveButton(1)).setText("(Yes)");
			((Button)fragment.getOliveButton(2)).setText("Where?");
			((Button)fragment.getOliveButton(4)).setText("(Coffee?)");
			((Button)fragment.getOliveButton(5)).setText("(No)");
			((Button)fragment.getOliveButton(6)).setText("When?");
			((Button)fragment.getOliveButton(8)).setText("(Drink?)");
			((Button)fragment.getOliveButton(9)).setText("(Maybe)");
			((Button)fragment.getOliveButton(10)).setText("(Busy)");
			break;
		case 1:
			((Button)fragment.getOliveButton(0)).setText("BEER NOW");
			((Button)fragment.getOliveButton(1)).setText("BEER LATER");
			break;
		case 2:			
			((Button)fragment.getOliveButton(0)).setText("Happy Hour");
			((Button)fragment.getOliveButton(1)).setText("Uris");
			((Button)fragment.getOliveButton(2)).setText("(Mel's)");
			((Button)fragment.getOliveButton(3)).setText("In class");
			((Button)fragment.getOliveButton(4)).setText("Rugby HH");
			((Button)fragment.getOliveButton(5)).setText("Watson");
			((Button)fragment.getOliveButton(6)).setText("(Pourhouse)");
			((Button)fragment.getOliveButton(7)).setText("CBS Matters");
			((Button)fragment.getOliveButton(8)).setText("Afterparty");
			((Button)fragment.getOliveButton(9)).setText("Warren");
			((Button)fragment.getOliveButton(10)).setText("(Parlour)");
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
	}
	
	public void onExpand(View view) {
		android.util.Log.d(TAG, "Not supported yet.");
		Toast.makeText(this, R.string.error_not_supported_yet, Toast.LENGTH_SHORT).show();
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
	
	
	/// static
	private static String sCurrentRecipientName;
	public static String getCurrentRecipientName() {
		return sCurrentRecipientName;
	}
}
