package com.tyrantapp.olive.helper;

import android.content.Context;

import com.kth.baasio.Baas;
import com.kth.baasio.entity.BaasioBaseEntity;
import com.kth.baasio.entity.entity.BaasioEntity;
import com.kth.baasio.entity.push.BaasioMessage;
import com.kth.baasio.entity.push.BaasioPayload;
import com.kth.baasio.entity.push.BaasioPush;
import com.kth.baasio.entity.user.BaasioUser;
import com.kth.baasio.exception.BaasioException;
import com.kth.baasio.query.BaasioQuery;
import com.kth.baasio.query.BaasioQuery.ORDER_BY;
import com.kth.baasio.response.BaasioResponse;
import com.tyrantapp.olive.types.OliveMessage;
import com.tyrantapp.olive.types.UserInfo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class NetworkHelper extends RESTHelper {
	private static final String TAG	= "NetworkHelper";

    private static final String SERVER_URL = "http://ec2-54-64-210-165.ap-northeast-1.compute.amazonaws.com";
    private static final String SERVER_PORT = "8080";
    private static final String SERVER_PATH_SIGNUP = "/user/signup";
    private static final String SERVER_PATH_SIGNIN = "/user/signin";
    private static final String SERVER_PATH_LEAVE = "/user/leave";

    private static final String SERVER_URL_SIGNUP = SERVER_URL + ":" + SERVER_PORT + SERVER_PATH_SIGNUP;
    private static final String SERVER_URL_SIGNIN = SERVER_URL + ":" + SERVER_PORT + SERVER_PATH_SIGNIN;
    private static final String SERVER_URL_LEAVE = SERVER_URL + ":" + SERVER_PORT + SERVER_PATH_LEAVE;

    private static final String		COLLECTION 			= "conversation";
	
	private static final String		PROPERTY_USERNAME 	= BaasioUser.PROPERTY_USERNAME;
	private static final String		PROPERTY_NICKNAME 	= BaasioUser.PROPERTY_NAME;
	private static final String		PROPERTY_EMAIL		= BaasioUser.PROPERTY_EMAIL;
	private static final String		PROPERTY_PHONE		= "phonenumber";
	
	private static final String		PROPERTY_FROM 		= "from";
	private static final String		PROPERTY_TO 		= "to";
	private static final String		PROPERTY_CONTEXT 	= "context";
	private static final String		PROPERTY_PENDING	= "pending";
	private static final String		PROPERTY_UNREAD 	= "unread";
	private static final String		PROPERTY_CREATED	= "created";

	public static void initialize(Context context) {
		RESTHelper.setContext(context);
		setInstance(new NetworkHelper());
	}
	
	public int signUp(String username, String password) {
		int eRet = OLIVE_FAIL_UNKNOWN;
		
		if (isEmailAddress(username)) {
            RestClient restClient = new RestClient(SERVER_URL_SIGNUP);
            restClient.AddParam("username", username);
            restClient.AddParam("password", password);

            try {
                restClient.Execute(RestClient.POST);
                if (restClient.getResponseCode() == 200) {
                    HashMap<String, String> mapRecv = JSONParser(restClient.getResponse());
                    if (mapRecv.get("message").equals("Success") && mapRecv.get("sucess").equals("true")) {
                        android.util.Log.d(TAG, "Response = " + restClient.getErrorMessage() + " / " + restClient.getResponseCode() + " / " + mapRecv.get("data"));
                        eRet = OLIVE_SUCCESS;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                android.util.Log.d(TAG, "Error!" + restClient.getErrorMessage() + " / " + restClient.getResponseCode());
            }
		} else {
			eRet = this.OLIVE_FAIL_INVALID_ID_PW;
		}
		
		return eRet;
	}
	
	public int signIn(String username, String password) {
		int eRet = OLIVE_SUCCESS;
		
		BaasioUser user = null;
		try {
			user = BaasioUser.signIn(getContext(), username, password);
			
			android.util.Log.d(TAG, "Success to sign in [" + user.getUsername() + "]");
			
			eRet = OLIVE_SUCCESS;
		} catch (BaasioException e) {
			e.printStackTrace();
			
			if (e.getErrorCode() == 201) {
				// ID/PW 잘못됨
				eRet = OLIVE_FAIL_INVALID_ID_PW;
			} else {
				eRet = OLIVE_FAIL_UNKNOWN;
			}
		}
		
		return eRet;
	}
	
	public int signOut() {
		int eRet = OLIVE_SUCCESS;
		if (isSignedIn()) {
			BaasioUser.signOut(getContext());
		} else {
			eRet = OLIVE_FAIL_NO_SIGNIN;
		}
		return eRet;
	}
	
	public boolean isSignedIn() {
		String token = Baas.io().getAccessToken();
		boolean bRet = token != null;
		android.util.Log.d(TAG, "Logged in = " + bRet + " / " + token);

		return bRet;
	}
	
	public UserInfo getUserProfile() {
		UserInfo oRet = null;		
		
		BaasioUser user = Baas.io().getSignedInUser();
		
		if (user != null) {
			oRet = new UserInfo();
			oRet.mUsername = user.getUsername();			
			oRet.mNickname = user.getName();
			oRet.mPhoneNumber = user.getMiddlename();
			oRet.mModified = user.getModified();
		}
		
		return oRet;
	}
	
	public UserInfo getRecipientProfile(String email, String phonenumber) {
		UserInfo oRet = null;		
		
		BaasioQuery mQuery = new BaasioQuery();
        mQuery.setType(BaasioUser.ENTITY_TYPE + "/" + email);
        mQuery.setOrderBy(BaasioBaseEntity.PROPERTY_MODIFIED, ORDER_BY.DESCENDING);
    	
        try {
			BaasioResponse reponse = mQuery.query();
			BaasioBaseEntity entity = reponse.getFirstEntity();
			BaasioUser user = BaasioBaseEntity.toType(entity, BaasioUser.class);
			
			oRet = new UserInfo();
			oRet.mUsername = user.getUsername();
			oRet.mNickname = user.getName();
		} catch (BaasioException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
				
		return oRet;
	}
	
	public	int	updateUserProfile(UserInfo info) {
		int nRet = OLIVE_SUCCESS;
		
		if (isSignedIn()) {
			BaasioUser user = Baas.io().getSignedInUser();
			user.setProperty(PROPERTY_NICKNAME, info.mNickname);
			user.setProperty(PROPERTY_PHONE, info.mPhoneNumber);
			
			try {
				user.update(getContext());
			} catch (BaasioException e) {
				nRet = OLIVE_FAIL_UNKNOWN;
				e.printStackTrace();
			}
		} else {
			nRet = OLIVE_FAIL_NO_SIGNIN;
		}
		
		return nRet;
	}
	
//	public int	getUnreadCount(String username) {
//		int nRet = 0;	
//		
//		if (isSignedIn()) {
//			UserInfo info = getUserProfile();
//			
//			BaasioQuery mQuery = new BaasioQuery();
//	        mQuery.setType(COLLECTION);
//	        mQuery.setWheres(PROPERTY_FROM + " = '" + username + "' AND " + PROPERTY_TO + " = '" + info.mUsername + "' AND " + PROPERTY_UNREAD + " = true" );
//	        //mQuery.setProjectionIn(PROPERTY_FROM);
//	        mQuery.setLimit(999);
//	        mQuery.setOrderBy(BaasioBaseEntity.PROPERTY_MODIFIED, ORDER_BY.DESCENDING);
//	    	
//	        try {
//				BaasioResponse response = mQuery.query();
//				nRet = response.getEntities().size();
//			} catch (BaasioException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//		return nRet;
//	}
		
	public	OliveMessage postOlive(String username, String contents) {
		OliveMessage msg = null;
		
		if (isSignedIn()) {
			UserInfo info = getUserProfile();
			msg = new OliveMessage();
			
			msg.mFrom = info.mUsername;
			msg.mTo = username;
			msg.mIsRead = true;
			msg.mAuthor = -1;
			msg.mCategory = -1;
			msg.mContext = contents;
			msg.mCreated = System.currentTimeMillis();
			
			BaasioEntity entity = new BaasioEntity(COLLECTION);
			entity.setProperty(PROPERTY_FROM, msg.mFrom);
			entity.setProperty(PROPERTY_TO, msg.mTo);
			entity.setProperty(PROPERTY_CONTEXT, msg.mContext);
			entity.setProperty(PROPERTY_PENDING, true);
			entity.setProperty(PROPERTY_UNREAD, true);
			
			try {
				msg.mCreated = entity.save().getCreated();
				msg.mIsPending = false;
				
				// Push!
				BaasioQuery mQuery = new BaasioQuery();
		        mQuery.setType(BaasioUser.ENTITY_TYPE + "/" + username);
		        mQuery.setOrderBy(BaasioBaseEntity.PROPERTY_MODIFIED, ORDER_BY.DESCENDING);
		        
		        BaasioResponse reponse = mQuery.query();
				BaasioUser recipient = BaasioBaseEntity.toType(reponse.getFirstEntity(), BaasioUser.class);
				
				BaasioPayload payload = new BaasioPayload();
				payload.setAlert(msg.mContext);      // 전송할 메시지
				payload.setProperty(OLIVE_PUSH_PROPERTY_FROM, msg.mFrom);
				payload.setProperty(OLIVE_PUSH_PROPERTY_TO, msg.mTo);
				payload.setSound("homerun.caf");    // iOS APNS의 sound
				payload.setBadge(1);                // iOS APNS badge 갯수
				
				BaasioMessage message = new BaasioMessage();
				message.setPayload(payload);
				message.setTarget(BaasioMessage.TARGET_TYPE_USER);  // 회원 개별 발송
				message.setTo(recipient.getUuid().toString());
				
				BaasioPush.sendPush(message);

				android.util.Log.d(TAG, "Pushed to " + msg.mTo + " from " + msg.mFrom);
			} catch (BaasioException e) {
				e.printStackTrace();
				msg.mIsPending = true;
				
				//BaasioUser.signOut(getContext());
			} finally {
			}
			
		} else {
			msg = null;
		}
		
		return msg;		
	}
	
	public OliveMessage[] getPendingOlives(String recipientName) {
		OliveMessage[] arrRet = null;
		
		if (isSignedIn()) {
			UserInfo info = getUserProfile();
			
			BaasioQuery mQuery = new BaasioQuery();
	        mQuery.setType(COLLECTION);
	        mQuery.setWheres(PROPERTY_FROM + "='" + recipientName + "' AND " + PROPERTY_TO + "='" + info.mUsername + "' AND " + PROPERTY_PENDING + "=true" );
	        mQuery.setOrderBy(BaasioBaseEntity.PROPERTY_MODIFIED, ORDER_BY.DESCENDING);
	        mQuery.setLimit(999);
	        
	        android.util.Log.d(TAG, "getPendingOlives FROM " + recipientName);
	    	
	        try {
				BaasioResponse reponse = mQuery.query();
				List<BaasioBaseEntity> entities = reponse.getEntities();
				
				arrRet = new OliveMessage[entities.size()];
				int nIndex = 0;
				for (BaasioBaseEntity entity : entities) {
					arrRet[nIndex] = new OliveMessage();
					arrRet[nIndex].mFrom = entity.getProperty(PROPERTY_FROM).asText();
					arrRet[nIndex].mTo = entity.getProperty(PROPERTY_TO).asText();
					arrRet[nIndex].mAuthor = -1;
					arrRet[nIndex].mCategory = -1;
					arrRet[nIndex].mContext = entity.getProperty(PROPERTY_CONTEXT).asText();
					arrRet[nIndex].mIsRead = entity.getProperty(PROPERTY_UNREAD).asBoolean();
					arrRet[nIndex].mIsPending = false;
					arrRet[nIndex].mCreated = entity.getCreated();
					nIndex++;
				}
			} catch (BaasioException e) {
				e.printStackTrace();
			}
		}
		
		return arrRet;
	}
	
	public boolean markToDispend(String username) {
		boolean bRet = false;
		
		if (isSignedIn()) {
			UserInfo info = getUserProfile();
			
			BaasioQuery mQuery = new BaasioQuery();
	        mQuery.setType(COLLECTION);
	        mQuery.setWheres(PROPERTY_FROM + "='" + username + "' AND " + PROPERTY_TO + "='" + info.mUsername + "' AND " + PROPERTY_PENDING + "=true" );
	        mQuery.setOrderBy(BaasioBaseEntity.PROPERTY_MODIFIED, ORDER_BY.DESCENDING);
	        mQuery.setLimit(999);
	    	
	        try {
				BaasioResponse reponse = mQuery.query();
				List<BaasioBaseEntity> entities = reponse.getEntities();
				BaasioEntity updateEntity = new BaasioEntity(COLLECTION);
				
				for (BaasioBaseEntity entity : entities) {
					updateEntity.setUuid(entity.getUuid());
					updateEntity.setProperty(PROPERTY_PENDING, false);
					updateEntity.update();
				}
				bRet = true;
			} catch (BaasioException e) {
				e.printStackTrace();
			}
		}
		
		return bRet;
	}
	
	public boolean markToRead(String username) {
		boolean bRet = false;
		
		if (isSignedIn()) {
			UserInfo info = getUserProfile();
			
			BaasioQuery mQuery = new BaasioQuery();
	        mQuery.setType(COLLECTION);
	        mQuery.setWheres(PROPERTY_FROM + "='" + username + "' AND " + PROPERTY_TO + "='" + info.mUsername + "' AND " + PROPERTY_UNREAD + "=true" );
	        mQuery.setOrderBy(BaasioBaseEntity.PROPERTY_MODIFIED, ORDER_BY.DESCENDING);
	        mQuery.setLimit(999);
	    	
	        try {
				BaasioResponse reponse = mQuery.query();
				List<BaasioBaseEntity> entities = reponse.getEntities();
				BaasioEntity updateEntity = new BaasioEntity(COLLECTION);
				
				for (BaasioBaseEntity entity : entities) {
					updateEntity.setUuid(entity.getUuid());
					updateEntity.setProperty(PROPERTY_UNREAD, false);
					updateEntity.update();
				}
				bRet = true;
			} catch (BaasioException e) {
				e.printStackTrace();
			}
		}
		
		return bRet;
	}
}
