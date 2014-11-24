package com.tyrantapp.olive.services;

import java.util.ArrayList;

import com.kth.baasio.Baas;
import com.kth.baasio.entity.BaasioBaseEntity;
import com.kth.baasio.entity.entity.BaasioEntity;
import com.kth.baasio.entity.user.BaasioUser;
import com.kth.baasio.exception.BaasioException;
import com.kth.baasio.query.BaasioQuery;
import com.kth.baasio.query.BaasioQuery.ORDER_BY;
import com.kth.baasio.response.BaasioResponse;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.tyrantapp.olive.ConversationActivity;
import com.tyrantapp.olive.R;
import com.tyrantapp.olive.helper.RESTHelper;
import com.tyrantapp.olive.providers.OliveContentProvider.ConversationColumns;
import com.tyrantapp.olive.providers.OliveContentProvider.RecipientColumns;
import com.tyrantapp.olive.types.OliveMessage;
import com.tyrantapp.olive.types.UserInfo;

public class SyncNetworkService extends Service {
	public static final String TAG = "OliveService";
	
	public static final String INTENT_ACTION = "intent.action.tyrantapp.olive.service";
	
	public static final String INTENT_ACTION_SYNC_CONVERSATION		= INTENT_ACTION + ".sync_conversation";
	// sync unread count acquired by sync conversation
	//public static final String INTENT_ACTION_SYNC_UNREAD_COUNT		= INTENT_ACTION + ".sync_unread_count";

	public static final String INTENT_ACTION_SYNC_RECIPIENT_INFO	= INTENT_ACTION + ".sync_recipient_info";
	public static final String INTENT_ACTION_SYNC_USER_INFO			= INTENT_ACTION + ".sync_user_info";
	
	public static final String INTENT_ACTION_POST_OLIVE				= INTENT_ACTION + ".post_olive";
	public static final String INTENT_ACTION_MARK_TO_READ			= INTENT_ACTION + ".mark_to_read";
	
	
	public static final String EXTRA_FROM							= "from";
	public static final String EXTRA_RECIPIENTNAME					= "recipient_name";
	public static final String EXTRA_MESSAGE						= "message";
		
	private	SyncNetworkService mService = null;
	
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
		
		mService = this;

		super.onCreate();
		
		// run SyncConversation();
		onSyncConversation(null);
		onSyncUnreadCount(null);
	}
	
	@Override
	public void onDestroy() {
		// TODO HERE
		
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && intent.getAction() != null)
			android.util.Log.d(TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ onStartCommand + " + intent.getAction().toString());
		else
			android.util.Log.d(TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ onStartCommand + ");
		
		if (intent != null && INTENT_ACTION_SYNC_CONVERSATION.equals(intent.getAction())) {
			// sync unread count acquired by sync conversation
			android.util.Log.d(TAG, "SYNC CONVERSATION");
			String from = intent.getStringExtra(EXTRA_FROM);
			onSyncConversation(from);
			onSyncUnreadCount(from);
		} else 
		if (intent != null && INTENT_ACTION_SYNC_RECIPIENT_INFO.equals(intent.getAction())) {
			android.util.Log.d(TAG, "SYNC RECIPIENT INFO");
			onSyncRecipientInfo();
		} else
		if (intent != null && INTENT_ACTION_SYNC_USER_INFO.equals(intent.getAction())) {
			android.util.Log.d(TAG, "SYNC USER INFO");
			onSyncUserInfo();
		} else
		if (intent != null && INTENT_ACTION_POST_OLIVE.equals(intent.getAction())) {
			android.util.Log.d(TAG, "POST OLIVE");
			String recipientName = intent.getStringExtra(EXTRA_RECIPIENTNAME);
			String message = intent.getStringExtra(EXTRA_MESSAGE);
			onPostOlive(recipientName, message);
		} else
		if (intent != null && INTENT_ACTION_MARK_TO_READ.equals(intent.getAction())) {
			android.util.Log.d(TAG, "MARK TO READ");
			String recipientName = intent.getStringExtra(EXTRA_RECIPIENTNAME);
			onMarkToRead(recipientName);
			onSyncUnreadCount(recipientName);
		}
		
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	// Service function
	private void onSyncConversation(String recipientName) {
		RESTHelper helper = RESTHelper.getInstance();
		
		ArrayList<String> listRecipients = new ArrayList<String>();

		if (recipientName == null) {
			// All recipients
			// get recipient list from db
			Cursor cursor = getContentResolver().query(
					RecipientColumns.CONTENT_URI, 
					RecipientColumns.PROJECTIONS, 
					null, null, null);
			
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				
				for (int i=0; i<cursor.getCount(); i++) {
					listRecipients.add(cursor.getString(cursor.getColumnIndex(RecipientColumns.USERNAME)));
					cursor.moveToNext();
				}
			}
		} else {
			// Selected recipients
			listRecipients.add(recipientName);
		}
		
		for (String username : listRecipients) {
			// get pending data list
			OliveMessage[] arrMessages = helper.getPendingOlives(username);
			
			if (arrMessages != null) {
				android.util.Log.d(TAG, "SyncConv " + username + " = " + arrMessages.length);
			
				// send mark to server
				helper.markToRead(username);
				
				// preapre recipient id
				long lRecipientId = -1;
				Cursor cursor = getContentResolver().query(
						RecipientColumns.CONTENT_URI, 
						new String[] { RecipientColumns._ID, },
						RecipientColumns.USERNAME + "=?",
						new String[] { username, },
						null);
					
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					lRecipientId = cursor.getLong(cursor.getColumnIndex(RecipientColumns._ID));
				
					// add to db
					ContentValues values = new ContentValues();
					for (OliveMessage msg : arrMessages) {
						android.util.Log.d(TAG, "Message+++  ");
						android.util.Log.d(TAG, "Message = " + msg.mContext);
						values.put(ConversationColumns.RECIPIENT_ID, lRecipientId);
						values.put(ConversationColumns.CTX_DETAIL, msg.mContext);
						values.put(ConversationColumns.IS_RECV, true);
						values.put(ConversationColumns.IS_PENDING, false);
						values.put(ConversationColumns.IS_READ, false);
						values.put(ConversationColumns.MODIFIED, msg.mModified);
						
						getContentResolver().insert(
								ConversationColumns.CONTENT_URI, 
								values);
						
					}
				}
				
				helper.markToDispend(username);
			}
		}
	}
	
	private void onSyncUnreadCount(String recipientName) {
		android.util.Log.d(TAG, "onSyncUnreadCount = " + recipientName);
		
		ArrayList<String> listRecipients = new ArrayList<String>();

		if (recipientName == null) {
			// All recipients
			// get recipient list from db
			Cursor cursor = getContentResolver().query(
					RecipientColumns.CONTENT_URI, 
					RecipientColumns.PROJECTIONS, 
					null, null, null);
			
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				
				for (int i=0; i<cursor.getCount(); i++) {
					listRecipients.add(cursor.getString(cursor.getColumnIndex(RecipientColumns.USERNAME)));
					cursor.moveToNext();
				}
			}
		} else {
			// Selected recipients
			listRecipients.add(recipientName);
		}
		
		android.util.Log.d(TAG, "onSyncUnreadCount + " + listRecipients.size());
		
		for (String username : listRecipients) {
			// get unread count from server
			//int nUnreadCount = helper.getUnreadCount(username);
			int nUnreadCount = 0;
			
			// preapre recipient id
			long lRecipientId = -1;
			Cursor recpCursor = getContentResolver().query(
					RecipientColumns.CONTENT_URI, 
					new String[] { RecipientColumns._ID, },
					RecipientColumns.USERNAME + "=?",
					new String[] { username, },
					null);
			
			if (recpCursor != null && recpCursor.getCount() > 0) {
				recpCursor.moveToFirst();
				lRecipientId = recpCursor.getLong(recpCursor.getColumnIndex(RecipientColumns._ID));
			}
			
			Cursor countCursor = getContentResolver().query(
					ConversationColumns.CONTENT_URI, 
					new String[] { ConversationColumns._ID, },
					ConversationColumns.RECIPIENT_ID + "=" + lRecipientId + " AND " + ConversationColumns.IS_READ + "=0 AND " + ConversationColumns.IS_RECV + "=1",
					null, null);
			
			if (countCursor != null) nUnreadCount = countCursor.getCount();
			
			android.util.Log.d(TAG, "Unread count [" + lRecipientId + "] = " + nUnreadCount);
			
			// update to db
			ContentValues values = new ContentValues();
			values.put(RecipientColumns.UNREAD, nUnreadCount);
			
			getContentResolver().update(
					RecipientColumns.CONTENT_URI,
					values,
					RecipientColumns.USERNAME + "=?",
					new String[] { username, });
		}
	}

	private void onSyncRecipientInfo() {
		
	}
		
	private void onSyncUserInfo() {
		
	}
	
	private void onPostOlive(String recipientName, String message) {
		RESTHelper helper = RESTHelper.getInstance();
		
		// preapre recipient id
		long lRecipientId = -1;
		Cursor cursor = getContentResolver().query(
				RecipientColumns.CONTENT_URI, 
				new String[] { RecipientColumns._ID, },
				RecipientColumns.USERNAME + "=?",
				new String[] { recipientName, },
				null);
			
		Uri uri = null;
		ContentValues values = new ContentValues();
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			lRecipientId = cursor.getLong(cursor.getColumnIndex(RecipientColumns._ID));
			
			// Send Message to Server
			values.put(ConversationColumns.RECIPIENT_ID, lRecipientId);
			values.put(ConversationColumns.CTX_DETAIL, message);
			values.put(ConversationColumns.IS_RECV, false);
			values.put(ConversationColumns.IS_PENDING, true);
			values.put(ConversationColumns.IS_READ, false);
			values.put(ConversationColumns.MODIFIED, System.currentTimeMillis());
		
			uri = getContentResolver().insert(ConversationColumns.CONTENT_URI, values);
		}
		
		OliveMessage msg = helper.postOlive(recipientName, message);
		if (msg != null) {
			values.put(ConversationColumns.IS_PENDING, false);
			values.put(ConversationColumns.MODIFIED, msg.mModified);
			
			getContentResolver().update(uri, values, null, null);
		} else {
			Toast.makeText(this, R.string.toast_failed_post_olive, Toast.LENGTH_SHORT).show();
		}
	}
	
	private void onMarkToRead(String recipientName) {
		RESTHelper helper = RESTHelper.getInstance();
		
		// preapre recipient id
		long lRecipientId = -1;
		Cursor recpCursor = getContentResolver().query(
				RecipientColumns.CONTENT_URI, 
				new String[] { RecipientColumns._ID, },
				RecipientColumns.USERNAME + "=?",
				new String[] { recipientName, },
				null);
		
		android.util.Log.d(TAG, "____ RECIPIENT = " + recipientName);
		
		if (recpCursor != null && recpCursor.getCount() > 0) {
			android.util.Log.d(TAG, "Cursur Find!!!!!!!!!!!!!!!!11");
			recpCursor.moveToFirst();
			lRecipientId = recpCursor.getLong(recpCursor.getColumnIndex(RecipientColumns._ID));
		}
		
		ContentValues values = new ContentValues();
		values.put(ConversationColumns.IS_READ, 1);
		int nCount = getContentResolver().update(
				ConversationColumns.CONTENT_URI,
				values,
				ConversationColumns.RECIPIENT_ID + "=" + lRecipientId + " AND " + ConversationColumns.IS_READ + "=0 AND " + ConversationColumns.IS_RECV + "=1",
				null);
		
		android.util.Log.d(TAG, "onMarkToRead = " +  lRecipientId + " / " + nCount + " recpCursor = " + recpCursor.toString());
		
		helper.markToRead(recipientName);
	}
	
	/*
	public String queryByHttp(HashMap<String, String> mapMessage) {
		String pszResult = OLIVE_RETURN_FAILED;
		
		if (mapMessage.isEmpty()) {
			mLastError = OLIVE_NO_PARAMETER;
			return pszResult;
		}
		
		// Server preference
		DefaultHttpClient client = new DefaultHttpClient();
		try {
			// check할 id/pw 서버로 전송
			StringBuilder builder = new StringBuilder();
			Set<String> setKey = mapMessage.keySet();
			for (String key : setKey) {
				builder.append(key);
				builder.append("=");
				builder.append(mapMessage.get(key));
				builder.append(",");
			}
			builder.deleteCharAt(builder.length() - 1);
			
			//HttpPost post = new HttpPost(SERVER_URL+"?msg="+msg);
			HttpPost post = new HttpPost(SERVER_URL + "?" + builder.toString());
			
			// Delay time 3000ms
			HttpParams params = client.getParams();
			HttpConnectionParams.setConnectionTimeout(params, 3000);
			HttpConnectionParams.setSoTimeout(params,  3000);
			
			// Process of gathering data from server after sending data.
			HttpResponse response = client.execute(post);
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "utf-8"));
			
			String pszLine = null;
			
			while ((pszLine = reader.readLine()) != null) {
				pszResult += pszLine;
			}
			
			mLastError = OLIVE_SUCCESS;
			
		} catch (Exception e) {
			e.printStackTrace();
			
			// 연결 지연 종료
			client.getConnectionManager().shutdown();
			mLastError = OLIVE_FAIL_TIMEOUT;
		}
		return pszResult;
	}
	
	public String[][] parseFromJSON(String pszRecvServerPage) {
		Log.i(TAG, "All contexts from server : " + pszRecvServerPage);
		
		try {
			JSONObject json = new JSONObject(pszRecvServerPage);
			JSONArray jArr = json.getJSONArray("List");
			
			// 받아온 pRecvServerPage를 분석하는 부분
			String[] jsonName = {"msg1", "msg2", "msg3"};
			String[][] parseredData = new String[jArr.length()][jsonName.length];
			for (int i=0; i<jArr.length(); i++) {
				json = jArr.getJSONObject(i);
				if (json != null) {
					for (int j=0; j<jsonName.length; j++) {
						parseredData[i][j] = json.getString(jsonName[j]);
					}
				}
			}
			
			// 분해된 데이터를 확인하기 위한 부분
			for (int i=0; i<parseredData.length; i++) {
				Log.i(TAG, "Parsed Data [" + i + "] = " + parseredData[i][0]);
				Log.i(TAG, "Parsed Data [" + i + "] = " + parseredData[i][1]);
				Log.i(TAG, "Parsed Data [" + i + "] = " + parseredData[i][2]);
			}
			
			return parseredData;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	*/
}

/*
 
        Button save = (Button)findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                BaasioEntity entity = new BaasioEntity("greeting");
                entity.setProperty("greeting", getString(R.string.success_baasio) + "\n\n생성시간:"
                        + System.currentTimeMillis());

                entity.saveInBackground(new BaasioCallback<BaasioEntity>() {

                    @Override
                    public void onResponse(BaasioEntity response) {
                        if (response != null) {
                            tvBaasio.setText(R.string.success_baasio);
                            savedUuid = response.getUuid();

                            Toast.makeText(MainActivity.this, "성공하였습니다.", Toast.LENGTH_LONG).show();
                        }

                    }

                    @Override
                    public void onException(BaasioException e) {
                        Log.e("baas.io", e.toString());

                        Toast.makeText(MainActivity.this, "실패하였습니다.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        Button get = (Button)findViewById(R.id.get);
        get.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (savedUuid != null) {
                    BaasioEntity entity = new BaasioEntity("greeting");
                    entity.setUuid(savedUuid);

                    entity.getInBackground(new BaasioCallback<BaasioEntity>() {

                        @Override
                        public void onResponse(BaasioEntity response) {
                            if (response != null) {
                                String result = response.getProperty("greeting").getTextValue();
                                tvBaasio.setText(result);

                                Toast.makeText(MainActivity.this, "성공하였습니다.", Toast.LENGTH_LONG)
                                        .show();
                            }
                        }

                        @Override
                        public void onException(BaasioException e) {
                            Log.e("baas.io", e.toString());

                            Toast.makeText(MainActivity.this, "실패하였습니다.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
        
        
	//
	// Represents an asynchronous login/registration task used to authenticate
	// the user.
	//
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

		private final String mEmail;
		private final String mPassword;

		UserLoginTask(String email, String password) {
			mEmail = email;
			mPassword = password;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.

			try {
				// Simulate network access.
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				return false;
			}

			for (String credential : DUMMY_CREDENTIALS) {
				String[] pieces = credential.split(":");
				if (pieces[0].equals(mEmail)) {
					// Account exists, return true if the password matches.
					return pieces[1].equals(mPassword);
				}
			}

			// TODO: register the new account here.
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				finish();
				
				Intent intent = new Intent(getApplicationContext(), MainActivity.class);
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			} else {
				mPasswordView
						.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
 */
