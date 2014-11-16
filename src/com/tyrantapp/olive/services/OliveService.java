package com.tyrantapp.olive.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kth.baasio.Baas;
import com.kth.baasio.callback.BaasioCallback;
import com.kth.baasio.callback.BaasioSignUpCallback;
import com.kth.baasio.entity.BaasioBaseEntity;
import com.kth.baasio.entity.entity.BaasioEntity;
import com.kth.baasio.entity.user.BaasioUser;
import com.kth.baasio.exception.BaasioException;
import com.kth.baasio.query.BaasioQuery;
import com.kth.baasio.query.BaasioQuery.ORDER_BY;
import com.kth.baasio.response.BaasioResponse;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tyrantapp.olive.MainActivity;
import com.tyrantapp.olive.R;
import com.tyrantapp.olive.services.*;
import com.tyrantapp.olive.types.UserInfo;

public class OliveService extends Service {
	public static final String TAG = "OliveService";
	
	public static final String INTENT_ACTION = "intent.action.tyrantapp.olive.service";
		
	// User custom
	public static final int OLIVE_SUCCESS					=  1;
	public static final int OLIVE_FAIL_UNKNOWN				=  0;
	public static final int OLIVE_FAIL_NO_SIGNIN			= -1;
	public static final int OLIVE_FAIL_NO_EXIST 			= -2;
	public static final int OLIVE_FAIL_NO_PARAMETER			= -3;
	public static final int OLIVE_FAIL_ALREADY_EXIST		= -4;	
	public static final int OLIVE_FAIL_BAD_NETWORK			= -5;
	public static final int OLIVE_FAIL_TIMEOUT				= -6;
	public static final int OLIVE_FAIL_INVALID_ID			= -7;
	public static final int OLIVE_FAIL_BAD_PASSWORD			= -8;
	public static final int OLIVE_FAIL_INVALID_PASSWORD		= -9;
	
	public static final String OLIVE_RETURN_FAILED			= "__FAILED__";
	
	
	private static final String SERVER_URL = "http://0.0.0.0:8080/MyServer/JSONServer.jsp";
	private	OliveService mService = null;
	private boolean	mOnlined = false;
	
	
	private final IOliveService.Stub mBinder = new IOliveService.Stub() {
		
		@Override
		public int signUp(String username, String password) throws RemoteException {
			return mService.signUp(username, password);
		}
		
		@Override
		public int signIn(String username, String password) throws RemoteException {
			return mService.signIn(username, password);
		}
		
		@Override
		public int signOut() throws RemoteException {
			return mService.signOut();
		}
		
		@Override
		public boolean isSignedIn() throws RemoteException {
			return mService.isSignedIn();
		}
		
		@Override
		public UserInfo getUserProfile() throws RemoteException {
			return mService.getUserProfile();
		}
		
		@Override
		public UserInfo getRecipientProfile(String username) throws RemoteException {
			return mService.getRecipientProfile(username);
		}
		
	};
		
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		
		if(android.os.Build.VERSION.SDK_INT > 9) {
		    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	        StrictMode.setThreadPolicy(policy);
		}
		
		mService = this;
		
		super.onCreate();		
		
	}
	
	@Override
	public void onDestroy() {
		// TODO HERE
		
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind..");
		return mBinder;
	}
	
	private int signUp(String username, String password) {
		int eRet = OLIVE_SUCCESS;
		
		BaasioUser user = null;
		try {
			user = BaasioUser.signUp(username, username, username+"@"+username.hashCode(), password);

			android.util.Log.d(TAG, "Success to sign up [" + user.getUsername() + "]");
			
			eRet = OLIVE_SUCCESS;
		} catch (BaasioException e) {
			if (e.getErrorCode() == 913) {
				// 이미 가입된 사용자
				eRet = OLIVE_FAIL_ALREADY_EXIST;
			} else {
				eRet = OLIVE_FAIL_UNKNOWN;
			}
		}
		
		return eRet;
	}
	
	private int signIn(String username, String password) {
		int eRet = OLIVE_SUCCESS;
		
		BaasioUser user = null;
		try {
			user = BaasioUser.signIn(this, username, password);
			
			android.util.Log.d(TAG, "Success to sign up [" + user.getUsername() + "]");
			
			eRet = OLIVE_SUCCESS;
		} catch (BaasioException e) {
			if (e.getErrorCode() == 913) {
				// 이미 가입된 사용자
				eRet = OLIVE_FAIL_ALREADY_EXIST;
			} else {
				eRet = OLIVE_FAIL_UNKNOWN;
			}
		}
		
		return eRet;
	}
	
	private int signOut() {
		int eRet = OLIVE_SUCCESS;
		
		return eRet;
	}
	
	private boolean isSignedIn() {
		String token = Baas.io().getAccessToken();
		return token != null;
	}
	
	private UserInfo getUserProfile() {
		UserInfo oRet = null;		
		
		BaasioUser user = Baas.io().getSignedInUser();
		
		if (user != null) {
			oRet = new UserInfo();
			oRet.mUsername = user.getUsername();			
			oRet.mNickname = user.getName();
			oRet.mPhoneNumber = user.getMiddlename();
			oRet.mCreated = user.getCreated();
			oRet.mModified = user.getModified();
		}
		
		return oRet;
	}
	
	private UserInfo getRecipientProfile(String username) {
		UserInfo oRet = null;		
		
		BaasioQuery mQuery = new BaasioQuery();
		mQuery.setType("user");
		//mQuery.setWheres("username LIKE " + username);
		mQuery.setOrderBy(BaasioBaseEntity.PROPERTY_NAME, ORDER_BY.DESCENDING);
		mQuery.setLimit(10);
		
		BaasioUser user = null;
		try {
			BaasioResponse response = mQuery.query();
			user = response.getUser();
		} catch (BaasioException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (user != null) {
			oRet = new UserInfo();
			oRet.mUsername = user.getUsername();			
			oRet.mNickname = user.getName();
			oRet.mPhoneNumber = user.getMiddlename();
			oRet.mCreated = user.getCreated();
			oRet.mModified = user.getModified();
		}
		
		return oRet;
	}
	
	
	/*
	public boolean signIn(String id, String passwd) {
		boolean bRet = false;
		
		if (requestSignIn(id, passwd)) {
			mLastError = OLIVE_SUCCESS;
			mSignedIn = true;
			bRet = true;
		} else {
			mLastError = OLIVE_BAD_PASSWORD;
		}
		
		return bRet;
	}
	
	public boolean signOut() {
		boolean bRet = false;
		
		if (requestSignOut()) {
			mLastError = OLIVE_SUCCESS;
			mSignedIn = false;
			bRet = true;
		} else {
			mLastError = OLIVE_NO_SIGNIN;
		}
		
		return bRet;
	}
	
	public boolean isSignedIn() {
		return mSignedIn;
	}
	
	public boolean getPendingData() {
		boolean bRet = false;
		
		if (isSignedIn()) {
			long lTimestamp = System.currentTimeMillis();
			requestPendingData();
			
			mLastError = OLIVE_SUCCESS;
			bRet = true;
		} else {
			mLastError = OLIVE_FAIL_NO_SIGNIN;
		}
		
		return bRet;
	}
	
	public boolean postData() {
		boolean bRet = false;
		
		if (isSignedIn()) {
			long lTimestamp = System.currentTimeMillis();
			sendgData();
			
			mLastError = OLIVE_SUCCESS;
			bRet = true;
		} else {
			mLastError = OLIVE_FAIL_NO_SIGNIN;
		}
		
		return bRet;
	}
	
	public List<RecipientInfo> getRecipientList() {
		boolean bRet = false;
		
		if (isSignedIn()) {
			long lTimestamp = System.currentTimeMillis();
			requestPendingData();
			
			mLastError = OLIVE_SUCCESS;
			bRet = true;
		} else {
			mLastError = OLIVE_FAIL_NO_SIGNIN;
		}
		
		return bRet;
	}
	*/
	/*

	 *  9. getRecipientList
	 *  9.1 param : id
	 *  9.2 return : vector< struct { id, nick, phone, timestamp } >
	 *  
	 *  10. addRecipient
	 *  10.1 param : id, recipient
	 *  10.2 return : true / false
			 
			 

			 *  11. removeRecipient
			 *  11.1 param : id, recipient
			 *  11.2 return : true / false
			 *  
			 *  12. getRecipientInfo
			 *  12.1 param : id, recipient
			 *  12.2 return : struct { id, nick, phone, timestamp }
			 *  
			 *  13. getRecipientPicture
			 *  13.1 param : id, recipient
			 *  13.2 return : bitmap
			 *  
			 *  14. syncRecipientList
			 *  14.1 param : id
			 *  14.2 return : true / false
			 *  
			 *  15. getUserProfileInfo
			 *  15.1 param : id
			 *  15.2 return : struct { id, nick, phone, timestamp }
			 *  
			 *  16. getUserProfilePicture
			 *  16.1 param : id
			 *  16.2 return : bitmap
			 *  
			 *  17. updateUserProfileInfo
			 *  17.1 param : id, struct { id, nick, phone, timestamp }
			 *  17.2 return : true / false
			 *  
			 *  18. updateUserProfilePicture
			 *  18.1 param : id, bitmap, timestamp
			 *  18.2 return : true / false
			 *  
			 *  19. syncUserProfile
			 *  19.1 param : id
			 *  19.2 return : true / false
	*/
	
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
