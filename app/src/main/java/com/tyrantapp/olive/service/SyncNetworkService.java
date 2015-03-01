package com.tyrantapp.olive.service;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.configuration.Constants;
import com.tyrantapp.olive.helper.DatabaseHelper;
import com.tyrantapp.olive.helper.OliveHelper;
import com.tyrantapp.olive.network.AWSQueryManager;
import com.tyrantapp.olive.network.RESTApiManager;

import com.tyrantapp.olive.provider.OliveContentProvider;
import com.tyrantapp.olive.type.ConversationMessage;
import com.tyrantapp.olive.type.RecipientInfo;
import com.tyrantapp.olive.type.SpaceInfo;
import com.tyrantapp.olive.type.UserProfile;

public class SyncNetworkService extends Service {
	public static final String TAG = "OliveService";
	
	public static final String INTENT_ACTION = "intent.action.tyrantapp.olive.service";

    // ROOM UPDATE -> CONVERSATION UPDATE -> FRIEND UPDATE
    public static final String INTENT_ACTION_SYNC_ALL               = INTENT_ACTION + ".sync_all";
    public static final String INTENT_ACTION_SYNC_USER_PROFILE      = INTENT_ACTION + ".sync_user_profile";
    public static final String INTENT_ACTION_SYNC_FRIENDS_LIST      = INTENT_ACTION + ".sync_friends_list";
    public static final String INTENT_ACTION_SYNC_ROOMS_LIST        = INTENT_ACTION + ".sync_rooms_list";
    public static final String INTENT_ACTION_SYNC_CONVERSATION	    = INTENT_ACTION + ".sync_conversation";

    public static final String INTENT_ACTION_SEND_MESSAGE           = INTENT_ACTION + ".send_message";
    public static final String INTENT_ACTION_READ_MESSAGES          = INTENT_ACTION + ".read_messages";

	private final ISyncNetworkService.Stub mBinder = new ISyncNetworkService.Stub() {
	};
		
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");

		if(android.os.Build.VERSION.SDK_INT > 9) {
			android.util.Log.d(TAG, "Network Strict Mode On!");
			
		    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	        StrictMode.setThreadPolicy(policy);
		}

		super.onCreate();

        // prepare default keypad
        DatabaseHelper.PresetButtonHelper.initialize(this);

        // run SyncConversation();
        RESTApiManager restManager = RESTApiManager.getInstance();
        if (restManager.isAutoSignIn() && restManager.verifyDevice()) {
            new ServiceTask(ServiceTask.SYNC_ALL).execute();
        }
	}
	
	@Override
	public void onDestroy() {
		// TODO HERE
		
		super.onDestroy();
	}

	@Override
	public synchronized int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null)
            android.util.Log.d(TAG, "onStartCommand(" + intent.getAction().toString() + ")");
        else
            android.util.Log.d(TAG, "onStartCommand()");

        if (intent != null && INTENT_ACTION_SYNC_ALL.equals(intent.getAction())) {
            new ServiceTask(ServiceTask.SYNC_ALL).execute();
        } else if (intent != null && INTENT_ACTION_SYNC_CONVERSATION.equals(intent.getAction())) {
            // sync unread count acquired by sync conversation
            android.util.Log.d(TAG, "SYNC CONVERSATION");
            long idRoom = intent.getLongExtra(Constants.Intent.EXTRA_ROOM_ID, -1);
            long idSpace = intent.getLongExtra(Constants.Intent.EXTRA_SPACE_ID, -1);
            if (idRoom < 0 && idSpace >= 0)
                idRoom = DatabaseHelper.SpaceHelper.getRoomId(this, idSpace);
            new ServiceTask(ServiceTask.SYNC_CONVERSATION).execute(String.valueOf(idRoom));
        } else if (intent != null && INTENT_ACTION_SYNC_FRIENDS_LIST.equals(intent.getAction())) {
            android.util.Log.d(TAG, "SYNC FRIENDS LIST");
            new ServiceTask(ServiceTask.SYNC_FRIENDS_LIST).execute();
        } else if (intent != null && INTENT_ACTION_SYNC_ROOMS_LIST.equals(intent.getAction())) {
            android.util.Log.d(TAG, "SYNC ROOMS LIST");
            new ServiceTask(ServiceTask.SYNC_ROOMS_LIST).execute();
        } else if (intent != null && INTENT_ACTION_SYNC_USER_PROFILE.equals(intent.getAction())) {
            android.util.Log.d(TAG, "SYNC USER INFO");
            new ServiceTask(ServiceTask.SYNC_USER_PROFILE).execute();
        } else if (intent != null && INTENT_ACTION_SEND_MESSAGE.equals(intent.getAction())) {
            android.util.Log.d(TAG, "POST OLIVE");
            Long idSpace = intent.getLongExtra(Constants.Intent.EXTRA_SPACE_ID, -1);
            String author = intent.getStringExtra(Constants.Intent.EXTRA_AUTHOR);
            String mimetype = intent.getStringExtra(Constants.Intent.EXTRA_MIMETYPE);
            String context = intent.getStringExtra(Constants.Intent.EXTRA_CONTEXT);
            new ServiceTask(ServiceTask.SEND_MESSAGE).execute(idSpace.toString(), author, mimetype, context);
        } else if (intent != null && INTENT_ACTION_READ_MESSAGES.equals(intent.getAction())) {
            android.util.Log.d(TAG, "MARK TO READ");
            long idSpace = intent.getLongExtra(Constants.Intent.EXTRA_SPACE_ID, -1);
            new ServiceTask(ServiceTask.MARK_TO_READ).execute(String.valueOf(idSpace));
        }

		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	// Service function
    // 대화 가져오기
    private void onSyncConversation() {
        onSyncConversation(-1);
    }

	private void onSyncConversation(long idRoom) {
		RESTApiManager helper = RESTApiManager.getInstance();

        // 서버 대화 가져오기
        ArrayList<HashMap<String, String>> listMessages = helper.receiveMessages(idRoom);

        for (HashMap<String, String> message : listMessages) {
            long idMessage = Long.parseLong(message.get(RESTApiManager.OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_MESSAGE_ID));
            if (DatabaseHelper.ConversationHelper.getConversationId(this, idMessage) < 0) {
                // 메시지 없으면 추가.
                idRoom = Long.parseLong(message.get(RESTApiManager.OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_ROOM_ID));
                // 모르는 사람이 메시지 보내면 한 번도 챗룸에 대응되는 스페이스를 만든적이 없으므로 ... 스페이스 추가 필요!
                long idSpace = AWSQueryManager.obtainSpaceIdByRoomId(this, idRoom);

                ConversationMessage newMessage = new ConversationMessage();
                newMessage.mMessageId = idMessage;
                newMessage.mSpaceId = idSpace;
                newMessage.mSender = message.get(RESTApiManager.OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_AUTHOR);
                newMessage.mAuthor = "user";
                newMessage.mMimetype = OliveHelper.convertMimetype(Integer.parseInt(message.get(RESTApiManager.OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_MSG_TYPE)));
                newMessage.mContext = message.get(RESTApiManager.OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_CONTENTS);
                newMessage.mMediaURL = message.get(RESTApiManager.OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_MEDIAURL);
                newMessage.mStatus = OliveContentProvider.ConversationColumns.STATUS_UNREAD;
                newMessage.mCreated = OliveHelper.dateToLong(message.get(RESTApiManager.OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_REG_DATE));

                DatabaseHelper.ConversationHelper.addMessage(this, newMessage);
            }
        }
	}

	private void onSyncFriendsList() {
        // 서버에서 친구 목록 가져오기
        // 로컬에 없으면 친구 추가.
        // 있으면 전화번호 및 사진 업데이트
        // 로컬에 업데이트 되지 않은 친구 있으면 친구는 서버에 없는 것임(삭제)
        // 친구 삭제시 방도 삭제

        // Sync phone number
        HashSet<String> setSynchronized = new HashSet<String>();
        RESTApiManager helper = RESTApiManager.getInstance();
        ArrayList<HashMap<String, String>> listFriends = helper.getFriendsList();
        for (HashMap<String, String> friend : listFriends) {
            String username = friend.get(RESTApiManager.OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_USERNAME);
            String phone = friend.get(RESTApiManager.OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_PHONE);
            String picture = friend.get(RESTApiManager.OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_PICTURE);
            picture = OliveHelper.getProfileImageDir(this) + OliveHelper.getPathLastSegment(picture);
            String mediaURL = friend.get(RESTApiManager.OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_MEDIAURL);
            long modified = OliveHelper.dateToLong(friend.get(RESTApiManager.OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_MODIFIED));

            long idRecipient = DatabaseHelper.RecipientHelper.getRecipientId(this, username);
            if (idRecipient < 0) {
                // New Friend
                RecipientInfo info = new RecipientInfo();
                info.mUsername = username;
                info.mPhoneNumber = phone;
                info.mDisplayname = DatabaseHelper.ContactProviderHelper.getDisplayname(this, username, phone);
                info.mPicture = picture;
                info.mMediaURL = mediaURL;
                info.mModified = modified;

                DatabaseHelper.RecipientHelper.addRecipient(this, info);
            } else {
                // Update Friend
                RecipientInfo info = DatabaseHelper.RecipientHelper.getRecipientInfo(this, idRecipient);
                info.mPhoneNumber = phone;
                info.mDisplayname = DatabaseHelper.ContactProviderHelper.getDisplayname(this, username, phone);
                info.mPicture = picture;
                info.mMediaURL = mediaURL;
                info.mModified = modified;

                DatabaseHelper.RecipientHelper.updateRecipient(this, info);
            }

            setSynchronized.add(username);
        }

        Cursor c = DatabaseHelper.RecipientHelper.getCursor(this);
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();

            HashSet<Long> setRemoved = new HashSet<Long>();
            do {
                String username = c.getString(c.getColumnIndex(OliveContentProvider.RecipientColumns.USERNAME));
                if (!setSynchronized.contains(username)) {
                    setRemoved.add(c.getLong(c.getColumnIndex(OliveContentProvider.RecipientColumns._ID)));
                }
            } while (c.moveToNext());
            c.close();

            for (long idRemove : setRemoved) {
                DatabaseHelper.RecipientHelper.removeRecipient(this, idRemove);
            }
        }
	}

    private void onSyncRoomsList() {
        HashSet<Long> setSynchronized = new HashSet<Long>();
        RESTApiManager helper = RESTApiManager.getInstance();
        ArrayList<HashMap<String, String>> listRooms = helper.getRoomsList();
        if (listRooms != null) {
            for (HashMap<String, String> room : listRooms) {
                String idRoomStr = room.get(RESTApiManager.OLIVE_PROPERTY_ROOM_LIST_ITEM.OLIVE_PROPERTY_ROOM_ID);
                if (idRoomStr == null) continue;
                long idRoom = Long.parseLong(idRoomStr);
                long idSpace = AWSQueryManager.obtainSpaceIdByRoomId(this, idRoom);

                SpaceInfo info = DatabaseHelper.SpaceHelper.getSpaceInfo(this, idSpace);
                String[] participants = info.mParticipants.split(",");

                if (participants.length > 1) {
                    setSynchronized.add(idSpace);
                } else {
                    helper.leaveRoom(idRoom);
                }
            }

            Cursor c = DatabaseHelper.SpaceHelper.getCursor(this);
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();

                HashSet<Long> setRemoved = new HashSet<Long>();
                do {
                    long idSpace = c.getLong(c.getColumnIndex(OliveContentProvider.SpaceColumns._ID));
                    if (!setSynchronized.contains(idSpace)) {
                        setRemoved.add(idSpace);
                    }
                } while (c.moveToNext());
                c.close();

                for (long idRemove : setRemoved) {
                    DatabaseHelper.SpaceHelper.removeSpace(this, idRemove);
                }
            }
        }
    }
		
	private void onSyncUserProfile() {
        // Sync phone number
        RESTApiManager helper = RESTApiManager.getInstance();
        String phoneNumber = OliveHelper.formatNumber(OliveHelper.getLineNumber(this));
        if (phoneNumber != null) helper.updateUserPhonenumber(phoneNumber);
    }

    private void onSendMessage() {
        onSendMessage(-1);
    }

	private void onSendMessage(long idSpace) {
        // 해당 방의 pending message를 서버로 전달
		RESTApiManager helper = RESTApiManager.getInstance();

        ArrayList<ConversationMessage> listPendingMessages = DatabaseHelper.ConversationHelper.getPendingMessages(this, idSpace);
        for (ConversationMessage message : listPendingMessages) {
            long idRoom = DatabaseHelper.SpaceHelper.getRoomId(this, message.mSpaceId);
            if (idRoom >= 0) {
                HashMap<String, String> mapRet = helper.sendMessage(idRoom, message.mAuthor, message.mMimetype, message.mContext);
                if (mapRet != null) {
                    String idMessageStr = mapRet.get(RESTApiManager.OLIVE_PROPERTY_MESSAGE_INFO.OLIVE_PROPERTY_MESSAGE_ID);
                    if (idMessageStr != null) {
                        String regDate = mapRet.get(RESTApiManager.OLIVE_PROPERTY_MESSAGE_INFO.OLIVE_PROPERTY_REG_DATE);
                        long idMessage = Long.parseLong(idMessageStr);
                        long regTime = OliveHelper.dateToLong(regDate);
                        DatabaseHelper.ConversationHelper.setDispendToMyMessage(this, message.mId, idMessage, regTime);
                    } else {
                        DatabaseHelper.SpaceHelper.removeSpace(this, message.mSpaceId);
                    }
                } else {
                    Toast.makeText(this, R.string.toast_failed_post_olive, Toast.LENGTH_SHORT).show();
                }
            } else {
                // Invalid message... should be deleted.
                DatabaseHelper.ConversationHelper.removeMessage(this, message.mId);
            }
        }
	}
	
	private void onMarkToRead(long idSpace) {
		RESTApiManager helper = RESTApiManager.getInstance();

        long idRoom = DatabaseHelper.SpaceHelper.getRoomId(this, idSpace);
        if (helper.readMessages(idRoom) == RESTApiManager.OLIVE_SUCCESS) {
            DatabaseHelper.ConversationHelper.setReadToOtherMessages(this, idSpace);
        }
	}
	
	public class ServiceTask extends AsyncTask<String, Void, Boolean> {
        public static final int SYNC_ALL                = 0;
		public static final int	SYNC_CONVERSATION_ALL	= 1;
		public static final int	SYNC_CONVERSATION		= 2;
		public static final int SYNC_FRIENDS_LIST       = 3;
        public static final int SYNC_ROOMS_LIST         = 4;
		public static final int SYNC_USER_PROFILE       = 5;
		public static final int SEND_MESSAGE            = 6;
		public static final int	MARK_TO_READ			= 7;
		
		private final int mFunctionId;

		ServiceTask(int functionId) {
			mFunctionId = functionId;
		}

		@Override
		protected Boolean doInBackground(String... params) {
            RESTApiManager restManager = RESTApiManager.getInstance();
            if (OliveHelper.isNetworkAvailable(getApplicationContext())) {
                switch (mFunctionId) {
                    case SYNC_ALL:
                        onSyncRoomsList();
                        onSyncConversation();
                        onSendMessage();
                        onSyncFriendsList();
                        onSyncUserProfile();
                        break;
                    case SYNC_CONVERSATION_ALL:
                        onSyncConversation();
                        break;
                    case SYNC_CONVERSATION:
                        onSyncConversation(Long.parseLong(params[0]));
                        break;
                    case SYNC_FRIENDS_LIST:
                        onSyncFriendsList();
                        break;
                    case SYNC_ROOMS_LIST:
                        onSyncRoomsList();
                        break;
                    case SYNC_USER_PROFILE:
                        onSyncUserProfile();
                        break;
                    case SEND_MESSAGE:
                        onSendMessage(Long.parseLong(params[0]));
                        break;
                    case MARK_TO_READ:
                        onMarkToRead(Long.parseLong(params[0]));
                        break;
                }
            }
			
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
		}
	}
}
