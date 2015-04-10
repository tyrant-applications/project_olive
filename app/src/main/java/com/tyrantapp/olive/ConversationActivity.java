package com.tyrantapp.olive;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.tyrantapp.olive.network.RESTApiManager;
import com.tyrantapp.olive.provider.OliveContentProvider;
import com.tyrantapp.olive.provider.OliveContentProvider.ConversationColumns;
import com.tyrantapp.olive.service.SyncNetworkService;
import com.tyrantapp.olive.type.ButtonInfo;
import com.tyrantapp.olive.type.ChatSpaceInfo;
import com.tyrantapp.olive.type.ConversationMessage;
import com.tyrantapp.olive.type.UserProfile;
import com.tyrantapp.olive.util.SharedVariables;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.database.DataSetObserver;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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
    private ImageView                   mLastOliveImage;
    private GoogleMap                   mLastOliveMap;
    //private Fragment                    mLastOliveMapWrapper;
    private RelativeLayout              mLastOliveMapWrapper;
	private View						mLastOliveExpander;
	
	private ViewFlipper					mInputMethodFlipper;
	private View						mInputMethodKeypad;
    private View						mInputMethodText;
    private View                        mOliveKeypad;
    private boolean						mTypingMode;
    private boolean                     mExpandingMode;

	private KeypadPagerAdapter			mKeypadPagerAdapter;
	private ViewPager					mKeypadPager;
	
	private View						mOliveText;
	private View						mOliveGallery;
	private View						mOliveCamera;
	private View						mOliveVoice;
	private View						mOliveLocation;
	
	private EditText					mTextEditor;
	private Button						mTextSender;

    private Cursor                      mCursorForUpdate;
	
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
            Uri outputUri = Uri.fromFile(new File(OliveHelper.getMessageMediaDir(ConversationActivity.this), "temporary.jpg"));
			Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
			startActivityForResult(intent, RESULT_TAKE_PICTURE);
		}
	};
	
	private OnClickListener	mOnVoiceClickListener = new OnClickListener() {
		public void onClick(View view) {
		}
	};
	
	private OnClickListener	mOnLocationClickListener = new OnClickListener() {
		public void onClick(View view) {
            Intent intent = new Intent(ConversationActivity.this, MapsActivity.class);
            startActivityForResult(intent, RESULT_GET_LOCATION);
		}
	};
	
	private OnClickListener	mOnSendClickListener = new OnClickListener() {
		public void onClick(View view) {
            // send text
			android.util.Log.d(TAG, "onSendText [" + mSpaceId + "] : " + mTextEditor.getText());

            if (mTextEditor.getText().toString() != null && !mTextEditor.getText().toString().isEmpty()) {
                ConversationMessage message = new ConversationMessage();
                message.mMessageId = -1;
                message.mAuthor = "user";
                message.mMimetype = OliveHelper.MIMETYPE_TEXT;
                message.mContext = mTextEditor.getText().toString();
                message.mSpaceId = mSpaceId;
                message.mSender = DatabaseHelper.UserHelper.getUserProfile(getApplicationContext()).mUsername;
                message.mStatus = ConversationColumns.STATUS_PENDING;
                message.mCreated = System.currentTimeMillis();
                DatabaseHelper.ConversationHelper.addMessage(getApplicationContext(), message);

                Intent intent = new Intent(getApplicationContext(), SyncNetworkService.class)
                        .setAction(SyncNetworkService.INTENT_ACTION_SEND_MESSAGE);
                startService(intent);

                changeNormalMode();
            }
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_conversation);
		
		// Initialize
        long idRoom = getIntent().getLongExtra(Constants.Intent.EXTRA_ROOM_ID, -1);
        getIntent().removeExtra(Constants.Intent.EXTRA_ROOM_ID);

        mSpaceId = AWSQueryManager.obtainSpaceIdByRoomId(this, idRoom);
		if (mSpaceId >= 0) {
            mUserProfile = DatabaseHelper.UserHelper.getUserProfile(this);
            mSpaceInfo = DatabaseHelper.ChatSpaceHelper.getChatSpaceInfo(this, mSpaceId);
            SharedVariables.put(Constants.Conversation.SHARED_SPACE_INFO, mSpaceInfo);
        }

        if (mSpaceInfo == null) {
            throw new IllegalArgumentException();
        } else
        if (mSpaceInfo.mPhonenumber == null || mSpaceInfo.mPhonenumber.equals("null")) {
            findViewById(R.id.call_button).setVisibility(View.INVISIBLE);
        }

        if (mSpaceInfo.mDisplayname == null || mSpaceInfo.mDisplayname.equals("null")) {
            findViewById(R.id.add_button).setVisibility(View.VISIBLE);
        }

        mConversationAdapter = new ConversationRecyclerAdapter(this, mSpaceId);
		mConversationView = (ConversationRecyclerView) findViewById(R.id.conversations_list_view);
		mConversationView.setAdapter(mConversationAdapter);
			
		mConversationView.setOnItemClickListener(mOnConversationClickListener);
		mConversationAdapter.registerAdapterDataObserver(mConversationObserver);

		// Last Olive (interaction mode)
        mLastOliveText = (TextView) findViewById(R.id.conversation_last_olive_text);
        mLastOliveImage = (ImageView) findViewById(R.id.conversation_last_olive_image);
        mLastOliveMapWrapper = (RelativeLayout) findViewById(R.id.conversation_last_olive_googlemap_wrapper);
        //mLastOliveMapWrapper = getSupportFragmentManager().findFragmentById(R.id.conversation_last_olive_googlemap);
        mLastOliveMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.conversation_last_olive_googlemap)).getMap();
		mLastOliveExpander = (View) findViewById(R.id.conversation_last_olive_expander);

        // default setting for map
        // Zoom in the Google Map
        //mLastOliveMap.getUiSettings().setMapToolbarEnabled(false);
        if (mLastOliveMap != null) mLastOliveMap.getUiSettings().setAllGesturesEnabled(false);

        // IME mode
		mTypingMode = false;
        mExpandingMode = false;
		mInputMethodFlipper = (ViewFlipper) findViewById(R.id.input_method_flipper);
		mInputMethodKeypad = (View) findViewById(R.id.input_method_keypad);
		mInputMethodText = (View) findViewById(R.id.input_method_text);
        mOliveKeypad = (View) findViewById(R.id.olive_keypad);

		// Fragment for Olive Keyboard
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mKeypadPagerAdapter = new KeypadPagerAdapter(getSupportFragmentManager(), this, this);
		
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

//        syncIntent = new Intent(this, SyncNetworkService.class)
//                .setAction(SyncNetworkService.INTENT_ACTION_SYNC_CONVERSATION)
//                .putExtra(Constants.Intent.EXTRA_ROOM_ID, mSpaceInfo.mChatroomId);
//        startService(syncIntent);

        syncIntent = new Intent(this, SyncNetworkService.class)
                .setAction(SyncNetworkService.INTENT_ACTION_READ_MESSAGES)
                .putExtra(Constants.Intent.EXTRA_SPACE_ID, mSpaceId);
        startService(syncIntent);

        if (mSpaceInfo.mChatroomId == SharedVariables.getLong(Constants.Notification.SHARED_NOTIFICATION_ROOM_ID)) {
            OliveHelper.removeNotification(this);
        }

        Uri uri = Uri.withAppendedPath(OliveContentProvider.ChatSpaceColumns.CONTENT_URI, String.valueOf(mSpaceId));
        mCursorForUpdate = getContentResolver().query(uri, OliveContentProvider.ChatSpaceColumns.PROJECTIONS, null, null, null);
        mCursorForUpdate.registerContentObserver(new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                android.util.Log.d(TAG, "Chatroom Info Updated");
                mSpaceInfo = DatabaseHelper.ChatSpaceHelper.getChatSpaceInfo(ConversationActivity.this, mSpaceId);

                // title
                TextView tv = (TextView) findViewById(R.id.title_name);
                String title = mSpaceInfo.mDisplayname;
                if (title == null) title = mSpaceInfo.mTitle;
                tv.setText(title);

                // call icon
                if (mSpaceInfo == null) {
                    throw new IllegalArgumentException();
                } else
                if (mSpaceInfo.mPhonenumber == null || mSpaceInfo.mPhonenumber.equals("null")) {
                    findViewById(R.id.call_button).setVisibility(View.INVISIBLE);
                } else {
                    findViewById(R.id.call_button).setVisibility(View.VISIBLE);
                }

                if (mSpaceInfo.mDisplayname == null || mSpaceInfo.mDisplayname.equals("null")) {
                    findViewById(R.id.add_button).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.add_button).setVisibility(View.GONE);
                }
            }
        });
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

        // Mark to read one more time.
        Intent syncIntent = null;

        syncIntent = new Intent(this, SyncNetworkService.class)
                .setAction(SyncNetworkService.INTENT_ACTION_READ_MESSAGES)
                .putExtra(Constants.Intent.EXTRA_SPACE_ID, mSpaceId);
        startService(syncIntent);

        mCursorForUpdate.close();
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.conversation, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}

    public void onCall(View view) {
        if (mSpaceInfo.mPhonenumber != null && !mSpaceInfo.mPhonenumber.equals("null")) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mSpaceInfo.mPhonenumber));
            startActivityIgnoreResult(intent);
        }
    }

    public void onAdd(View view) {
        new AlertDialog.Builder(ConversationActivity.this)
                .setTitle("Add Friend")
                .setMessage("Add as friends?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        for (String participant : mSpaceInfo.mParticipants.split(",")) {
                            if (!participant.equals(mUserProfile.mUsername)) {
                                if (mRESTApiManager.addFriends(new String[]{participant,}) == RESTApiManager.OLIVE_SUCCESS) {
                                    // Sync room and friends
                                    Intent syncIntent = null;

                                    syncIntent = new Intent(ConversationActivity.this, SyncNetworkService.class)
                                            .setAction(SyncNetworkService.INTENT_ACTION_SYNC_FRIENDS_LIST);
                                    startService(syncIntent);
                                }
                            }
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_input_add)
                .show();
    }

	@Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
	    super.onActivityResult(requestCode, resultCode, intent);

        String selectedImage = null;
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != intent) {
            Uri selectedImageUri = intent.getData();

            // String picturePath contains the path of selected Image
            // first, copy file to data folder as profile.png? jpg?
            // second, upload picture.
            InputStream in = null;
            try {
                in = getContentResolver().openInputStream(selectedImageUri);

                if (OliveHelper.saveFile(in, OliveHelper.getMessageMediaDir(this) + "temporary.jpg")) {
                    selectedImage = OliveHelper.getMessageMediaDir(this) + "temporary.jpg";
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in != null) in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else
        if (requestCode == RESULT_TAKE_PICTURE && resultCode == RESULT_OK) {
            // String picturePath contains the path of selected Image
            // first, copy file to data folder as profile.png? jpg?
            // second, upload picture.

            selectedImage = OliveHelper.getMessageMediaDir(this) + "temporary.jpg";
        } else
        if (requestCode == RESULT_GET_LOCATION && resultCode == MapsActivity.RESULT_SUCCESS) {
            sendGeoLocation(intent.getDoubleExtra("latitude", 0), intent.getDoubleExtra("longitude", 0));
        }

        if (selectedImage != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = 0;

            int nSize = 0;
            do {
                options.inSampleSize++;
                BitmapFactory.decodeFile(selectedImage, options);
            } while ((options.outWidth * options.outHeight) > Constants.Configuration.MAX_IMAGE_RESOLUTION * Constants.Configuration.MAX_IMAGE_RESOLUTION);

            options.inJustDecodeBounds = false;
            sendImage(BitmapFactory.decodeFile(selectedImage, options));

            ignorePasscodeOnce();
        }
    }

    private boolean sendGeoLocation(double latitude, double longitude) {
        ConversationMessage message = new ConversationMessage();
        message.mMessageId = -1;
        message.mAuthor = "user";
        message.mSpaceId = mSpaceId;
        message.mSender = DatabaseHelper.UserHelper.getUserProfile(this).mUsername;
        message.mMimetype = OliveHelper.MIMETYPE_GEOLOCATE;
        message.mStatus = ConversationColumns.STATUS_PENDING;
        message.mCreated = System.currentTimeMillis();
        message.mContext = latitude + "," + longitude;

        DatabaseHelper.ConversationHelper.addMessage(this, message);

        Intent sendIntent = new Intent(this, SyncNetworkService.class)
                .setAction(SyncNetworkService.INTENT_ACTION_SEND_MESSAGE);
        startService(sendIntent);

        return true;
    }

    private boolean sendImage(Bitmap bitmap) {
        boolean bRet = false;

        ConversationMessage message = new ConversationMessage();
        message.mMessageId = -1;
        message.mAuthor = "user";
        message.mSpaceId = mSpaceId;
        message.mSender = DatabaseHelper.UserHelper.getUserProfile(this).mUsername;
        message.mMimetype = OliveHelper.MIMETYPE_IMAGE;
        message.mStatus = ConversationColumns.STATUS_PENDING;
        message.mCreated = System.currentTimeMillis();

        String filename = mSpaceInfo.mChatroomId + "_" + message.mCreated + ".jpg";
        String filePath = OliveHelper.getMessageMediaDir(this) + filename;
        if (OliveHelper.saveImage(bitmap, Bitmap.CompressFormat.JPEG, 70, filePath)) {
            message.mContext = filePath;
            DatabaseHelper.ConversationHelper.addMessage(this, message);

            Intent sendIntent = new Intent(this, SyncNetworkService.class)
                    .setAction(SyncNetworkService.INTENT_ACTION_SEND_MESSAGE);
            startService(sendIntent);
            bRet = true;
        }

        return bRet;
    }

	@Override
	public void onBackPressed() {
		if (mTypingMode || mExpandingMode) {
			changeNormalMode();
		} else {
			super.onBackPressed();
		}
	}
	
	@Override
	public void onKeypadCreate(int sectionNumber) {
		KeypadFragment fragment = (KeypadFragment)KeypadFragment.getFragment(sectionNumber);
//
//        for (int i=0; i<12; i++) {
//            long idButton = DatabaseHelper.PresetButtonHelper.getIdByIndex(this, sectionNumber * 12 + i);
//            ButtonInfo info = DatabaseHelper.PresetButtonHelper.getButtonInfo(this, idButton);
//            //((Button)fragment.getOliveButton(sectionNumber, i)).setText(info.mContext);
//        }
//		switch (sectionNumber) {
//		/*  // first resource
//		 * ((Button)fragment.getOliveButton(0)).setText("Eat");
//			((Button)fragment.getOliveButton(1)).setText("Can We Meet?");
//			((Button)fragment.getOliveButton(2)).setText("Yes");
//			((Button)fragment.getOliveButton(3)).setText("Where?");
//			((Button)fragment.getOliveButton(4)).setText("Coffee");
//			((Button)fragment.getOliveButton(5)).setText("Can We Talk?");
//			((Button)fragment.getOliveButton(6)).setText("No");
//			((Button)fragment.getOliveButton(7)).setText("When?");
//			((Button)fragment.getOliveButton(8)).setText("Pub");
//			((Button)fragment.getOliveButton(9)).setText("Wanna Do Something?");
//			((Button)fragment.getOliveButton(10)).setText("Busy");
//			((Button)fragment.getOliveButton(11)).setText("With?");
//		 */
//		case 0:
//			((Button)fragment.getOliveButton(0)).setText("(Food?)");
//			((Button)fragment.getOliveButton(1)).setText("(Yes)");
//			((Button)fragment.getOliveButton(2)).setText("Where?");
//			((Button)fragment.getOliveButton(4)).setText("(Coffee?)");
//			((Button)fragment.getOliveButton(5)).setText("(No)");
//			((Button)fragment.getOliveButton(6)).setText("When?");
//			((Button)fragment.getOliveButton(8)).setText("(Drink?)");
//			((Button)fragment.getOliveButton(9)).setText("(Maybe)");
//			((Button)fragment.getOliveButton(10)).setText("(Busy)");
//			break;
//		case 1:
//			((Button)fragment.getOliveButton(0)).setText("BEER NOW");
//			((Button)fragment.getOliveButton(1)).setText("BEER LATER");
//			break;
//		case 2:
//			((Button)fragment.getOliveButton(0)).setText("Happy Hour");
//			((Button)fragment.getOliveButton(1)).setText("Uris");
//			((Button)fragment.getOliveButton(2)).setText("(Mel's)");
//			((Button)fragment.getOliveButton(3)).setText("In class");
//			((Button)fragment.getOliveButton(4)).setText("Rugby HH");
//			((Button)fragment.getOliveButton(5)).setText("Watson");
//			((Button)fragment.getOliveButton(6)).setText("(Pourhouse)");
//			((Button)fragment.getOliveButton(7)).setText("CBS Matters");
//			((Button)fragment.getOliveButton(8)).setText("Afterparty");
//			((Button)fragment.getOliveButton(9)).setText("Warren");
//			((Button)fragment.getOliveButton(10)).setText("(Parlour)");
//			break;
//		default:
//		}
	}

	@Override
	public void onKeypadClick(int sectionNumber, int index) {
		KeypadFragment fragment = (KeypadFragment)KeypadFragment.getFragment(sectionNumber);
		ButtonInfo info = (ButtonInfo)fragment.getOliveButton(sectionNumber, index);

        if (info.mContext != null && !info.mContext.isEmpty()) {
            ConversationMessage message = new ConversationMessage();
            message.mMessageId = -1;
            message.mAuthor = info.mAuthor;
            message.mMimetype = info.mMimetype;
            message.mContext = info.mContext;
            message.mSpaceId = mSpaceId;
            message.mSender = DatabaseHelper.UserHelper.getUserProfile(this).mUsername;
            message.mStatus = ConversationColumns.STATUS_PENDING;
            message.mCreated = System.currentTimeMillis();
            DatabaseHelper.ConversationHelper.addMessage(this, message);

            Intent intent = new Intent(this, SyncNetworkService.class)
                    .setAction(SyncNetworkService.INTENT_ACTION_SEND_MESSAGE);
            startService(intent);
        }
	}

    @Override
    public void onKeypadLongClick(int sectionNumber, int index) {
        int position = sectionNumber * DatabaseHelper.PresetButtonHelper.BUTTON_PER_SECTION + index;
        long idButton = DatabaseHelper.PresetButtonHelper.getIdByIndex(this, position);
        Intent intent = new Intent(this, KeyCustomizeActivity.class)
                .putExtra(Constants.Intent.EXTRA_BUTTON_ID, idButton);
        startActivityForPasscode(intent);
    }
	
	public void onExpand(View view) {
		android.util.Log.d(TAG, "Not supported yet.");
		Toast.makeText(this, R.string.error_not_supported_yet, Toast.LENGTH_SHORT).show();
	}

    public void onLastMessage(View view) {
        changeExpandingMode();
    }

	public void updateLastOlive(Cursor cursor) {
		if (cursor != null && cursor.getCount() > 0) {
            //android.support.v4.app.FragmentManager fm = getSupportFragmentManager();//getFragmentManager();
            //fm.beginTransaction().hide(mLastOliveMapWrapper).commit();

            String mimetype = cursor.getString(cursor.getColumnIndex(ConversationColumns.MIMETYPE));
            String mediaURL = cursor.getString(cursor.getColumnIndex(ConversationColumns.MEDIAURL));
            String content = cursor.getString(cursor.getColumnIndex(ConversationColumns.CONTEXT));
            Bitmap bmpImage = null;
            double latitude = 0;
            double longitude = 0;
            switch (OliveHelper.convertMimetype(mimetype)) {
                case 0: //MIMETYPE_TEXT:
                    mLastOliveText.setText(content);
                    mLastOliveText.setVisibility(View.VISIBLE);
                    mLastOliveImage.setVisibility(View.GONE);
                    mLastOliveMapWrapper.setVisibility(View.GONE);
                    break;
                case 1: //MIMETYPE_IMAGE:
                    OliveHelper.downloadMedia(mediaURL, OliveHelper.getMessageMediaDir(this));
                    bmpImage = OliveHelper.getCachedImage(content, 3);
                    mLastOliveImage.setImageBitmap(bmpImage);
                    mLastOliveText.setVisibility(View.GONE);
                    mLastOliveImage.setVisibility(View.VISIBLE);
                    mLastOliveMapWrapper.setVisibility(View.GONE);
                    break;
                case 2: //MIMETYPE_VIDEO:
                    mLastOliveText.setText(content);
                    mLastOliveText.setVisibility(View.GONE);
                    mLastOliveImage.setVisibility(View.VISIBLE);
                    mLastOliveMapWrapper.setVisibility(View.GONE);
                    break;
                case 3: //MIMETYPE_AUDIO:
                    mLastOliveText.setText(content);
                    mLastOliveText.setVisibility(View.GONE);
                    mLastOliveImage.setVisibility(View.VISIBLE);
                    mLastOliveMapWrapper.setVisibility(View.GONE);
                    break;
                case 4: //MIMETYPE_GEOLOCATE:
                    latitude = Double.parseDouble(content.substring(0, content.indexOf(",")));
                    longitude = Double.parseDouble(content.substring(content.indexOf(",") + 1));

                    try {
                        String addressName = "HERE";
                        Geocoder geocoder = new Geocoder(this);
                        for (Address address : geocoder.getFromLocation(latitude, longitude, 1)) {
                            addressName = address.getAddressLine(0);
                        }
                        if (mLastOliveMap != null)
                            mLastOliveMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(addressName)).showInfoWindow();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // Showing the current location in Google Map
                    if (mLastOliveMap != null)
                        mLastOliveMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude + 0.0015, longitude), 15));

                    mLastOliveText.setVisibility(View.GONE);
                    mLastOliveImage.setVisibility(View.GONE);
                    mLastOliveMapWrapper.setVisibility(View.VISIBLE);
                    break;
                case 5: //MIMETYPE_EMOJI:
                    mLastOliveText.setText(content);
                    mLastOliveText.setVisibility(View.GONE);
                    mLastOliveImage.setVisibility(View.VISIBLE);
                    mLastOliveMapWrapper.setVisibility(View.GONE);
                    break;
            }
		} else {
			mLastOliveText.setText("No Conversation.");
		}
	}
	
	public boolean isTypingMode() {
		return mTypingMode;
	}

    public boolean isExpandingMode() { return mExpandingMode; }

    public void changeExpandingMode() {
        if (!mExpandingMode) {
            mExpandingMode = true;
            //mInputMethodFlipper.showNext();
            mOliveKeypad.setVisibility(View.GONE);
            hideSoftInputMethod(mTextEditor);
        }
    }

	public void changeTypingMode() {
		if (!mTypingMode) {
			mTypingMode = true;
			//mInputMethodFlipper.showNext();
			mInputMethodKeypad.setVisibility(View.GONE);
			mInputMethodText.setVisibility(View.VISIBLE);
			showSoftInputMethod(mTextEditor);
		}
	}
	
	public void changeNormalMode() {
		if (mTypingMode || mExpandingMode) {
			mTypingMode = false;
            mExpandingMode = false;
			//mInputMethodFlipper.showPrevious();
			mInputMethodKeypad.setVisibility(View.VISIBLE);
            mOliveKeypad.setVisibility(View.VISIBLE);
            mInputMethodText.setVisibility(View.GONE);
			hideSoftInputMethod(mTextEditor);
		}
	}
	
	public void showSoftInputMethod(final EditText focusView) {
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
