package com.tyrantapp.olive;

import com.tyrantapp.olive.adapter.ConversationRecyclerAdapter;
import com.tyrantapp.olive.adapter.KeypadPagerAdapter;
import com.tyrantapp.olive.component.ConversationRecyclerView;
import com.tyrantapp.olive.component.ConversationRecyclerView.RecyclerItemClickListener;
import com.tyrantapp.olive.configuration.Constants;
import com.tyrantapp.olive.fragment.KeypadFragment;
import com.tyrantapp.olive.helper.DatabaseHelper;
import com.tyrantapp.olive.helper.OliveHelper;
import com.tyrantapp.olive.listener.OnOliveKeypadListener;
import com.tyrantapp.olive.network.AWSQueryManager;
import com.tyrantapp.olive.provider.OliveContentProvider.ConversationColumns;
import com.tyrantapp.olive.service.SyncNetworkService;
import com.tyrantapp.olive.type.ChatSpaceInfo;
import com.tyrantapp.olive.type.ConversationMessage;
import com.tyrantapp.olive.type.UserProfile;
import com.tyrantapp.olive.util.SharedVariables;

import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
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

    private UserProfile                 mUserProfile;
    private long                        mSpaceId;
    public  ChatSpaceInfo               mSpaceInfo;

	private ConversationRecyclerView	mConversationView;
	private ConversationRecyclerAdapter	mConversationAdapter;
	
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
	
	//private DataSetObserver	mConversationObserver = new DataSetObserver() {
	private AdapterDataObserver	mConversationObserver = new AdapterDataObserver() {
		@Override
		public void onChanged() {
			super.onChanged();
			
			Cursor cursor = (Cursor) mConversationAdapter.getCursor();
			cursor.moveToLast();
			
			updateLastOlive(cursor);
		}
	};
	
	private RecyclerItemClickListener.OnItemClickListener mOnConversationClickListener = new RecyclerItemClickListener.OnItemClickListener() {
		@Override
		public void onItemClick(View view, int position) {
			updateLastOlive(mConversationAdapter.getItem(position));
			android.util.Log.d(TAG, "Item Count = " + mConversationAdapter.getItemCount());
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
            // send text
			android.util.Log.d(TAG, "onSendText [" + mSpaceId + "] : " + mTextEditor.getText());

            ConversationMessage message = new ConversationMessage();
            message.mSpaceId = mSpaceId;
            message.mSender = mUserProfile.mUsername;
            message.mAuthor = "user";
            message.mMimetype = OliveHelper.MIMETYPE_TEXT;
            message.mContext = mTextEditor.getText().toString();
            message.mStatus = ConversationColumns.STATUS_PENDING;
            message.mCreated = System.currentTimeMillis();
			
			Intent intent = new Intent(getApplicationContext(), SyncNetworkService.class)
	        	.setAction(SyncNetworkService.INTENT_ACTION_SEND_MESSAGE)
	        	.putExtra(Constants.Intent.EXTRA_SPACE_ID, mSpaceId)
                .putExtra(Constants.Intent.EXTRA_AUTHOR, message.mAuthor)
                .putExtra(Constants.Intent.EXTRA_MIMETYPE, message.mMimetype)
	        	.putExtra(Constants.Intent.EXTRA_CONTEXT, String.valueOf(mTextEditor.getText()));
	        startService(intent);
	        
	        changeNormalMode();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_conversation);
		
		// Initialize
        long idRoom = getIntent().getLongExtra(Constants.Intent.EXTRA_ROOM_ID, -1);
        mSpaceId = AWSQueryManager.obtainSpaceIdByRoomId(this, idRoom);
		if (mSpaceId >= 0) {
            mUserProfile = DatabaseHelper.UserHelper.getUserProfile(this);
            mSpaceInfo = DatabaseHelper.ChatSpaceHelper.getChatSpaceInfo(this, mSpaceId);
            SharedVariables.put(Constants.Conversation.SHARED_SPACE_INFO, mSpaceInfo);
        }

        if (mSpaceInfo == null) {
            throw new IllegalArgumentException();
        }
		
		mConversationAdapter = new ConversationRecyclerAdapter(this, mSpaceId);
		mConversationView = (ConversationRecyclerView) findViewById(R.id.conversations_list_view);
		mConversationView.setAdapter(mConversationAdapter);
			
		mConversationView.setOnItemClickListener(mOnConversationClickListener);
		mConversationAdapter.registerAdapterDataObserver(mConversationObserver);
		//mConversationAdapter.registerDataSetObserver(mConversationObserver);
		
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
            String title = mSpaceInfo.mDisplayname;
            if (title == null) title = mSpaceInfo.mTitle;
			tv.setText(title);
			
			Cursor cursor = mConversationAdapter.getCursor();
			if (cursor != null) cursor.moveToLast();
			updateLastOlive(cursor);
		
		}
		
		setEnablePasscode(true);
	}

    @Override
    protected void onDestroy() {
        SharedVariables.remove(Constants.Conversation.SHARED_SPACE_INFO);
        super.onDestroy();
    }

    @Override
	public void onStart() {
		super.onStart();

        Intent syncIntent = null;

        syncIntent = new Intent(this, SyncNetworkService.class)
                .setAction(SyncNetworkService.INTENT_ACTION_SYNC_CONVERSATION)
                .putExtra(Constants.Intent.EXTRA_ROOM_ID, mSpaceInfo.mChatroomId);
        startService(syncIntent);

        syncIntent = new Intent(this, SyncNetworkService.class)
        	.setAction(SyncNetworkService.INTENT_ACTION_READ_MESSAGES)
        	.putExtra(Constants.Intent.EXTRA_SPACE_ID, mSpaceId);
        startService(syncIntent);

        if (mSpaceInfo.mChatroomId == SharedVariables.getLong(Constants.Notification.SHARED_NOTIFICATION_ROOM_ID)) {
            OliveHelper.removeNotification(this);
        }
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	public void onStop() {
		super.onStop();
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
	                      
	         // String picturePath contains the path of selected Image
	         
	         ignorePasscodeOnce();
	     } else
    	 if (requestCode == RESULT_TAKE_PICTURE && resultCode == RESULT_OK) {  
             Bitmap bmpPicture = (Bitmap) data.getExtras().get("data"); 
             //imageView.setImageBitmap(bmpPicture);
             
             ignorePasscodeOnce();
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

        ConversationMessage message = new ConversationMessage();
        message.mMessageId = -1;
        message.mAuthor = "user";
        message.mMimetype = "text/plain";
        message.mContext = view.getText().toString();
        message.mSpaceId = mSpaceId;
        message.mSender = DatabaseHelper.UserHelper.getUserProfile(this).mUsername;
        message.mStatus = ConversationColumns.STATUS_PENDING;
        message.mCreated = System.currentTimeMillis();
        DatabaseHelper.ConversationHelper.addMessage(this, message);

        Intent intent = new Intent(this, SyncNetworkService.class)
        	.setAction(SyncNetworkService.INTENT_ACTION_SEND_MESSAGE);
        startService(intent);
	}
	
	public void onExpand(View view) {
		android.util.Log.d(TAG, "Not supported yet.");
		Toast.makeText(this, R.string.error_not_supported_yet, Toast.LENGTH_SHORT).show();
	}
	
	public void updateLastOlive(Cursor cursor) {
		if (cursor != null && cursor.getCount() > 0) {
			String content = cursor.getString(cursor.getColumnIndex(ConversationColumns.CONTEXT));
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
