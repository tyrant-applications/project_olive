package com.tyrantapp.olive.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.StrictMode;

import org.apache.http.HttpVersion;
import org.apache.http.entity.ContentType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class RESTApiManager {
	public static final String TAG = "RESTHelper";
	
	// User custom
	public static final int OLIVE_SUCCESS					=  1;
	public static final int OLIVE_FAIL_UNKNOWN				=  0;
	public static final int OLIVE_FAIL_NO_SIGNIN			= -1;
	public static final int OLIVE_FAIL_NO_EXIST 			= -2;
	public static final int OLIVE_FAIL_NO_PARAMETER			= -3;
	public static final int OLIVE_FAIL_ALREADY_EXIST		= -4;	
	public static final int OLIVE_FAIL_BAD_NETWORK			= -5;
	public static final int OLIVE_FAIL_TIMEOUT				= -6;
	public static final int OLIVE_FAIL_INVALID_ID_PW		= -7;
	public static final int OLIVE_FAIL_BAD_PASSWORD			= -8;

    public static final String OLIVE_PUSH_PROPERTY_PUSH_TYPE            = "push_type";
    //나를 친구로 삼는 상대방 리스트를 알 수 가 없어 현재 적용 불가
    //public static final String OLIVE_PUSH_PROPERTY_PUSH_TYPE_PROFILE    = "push_type_profile";
    public static final String OLIVE_PUSH_PROPERTY_PUSH_TYPE_CREATE     = "push_type_create";
    public static final String OLIVE_PUSH_PROPERTY_PUSH_TYPE_LEAVE      = "push_type_leave";
    public static final String OLIVE_PUSH_PROPERTY_PUSH_TYPE_POST       = "push_type_post";
    //읽음에 대한 내용이 서버에 없으므로 적용 불가
    //public static final String OLIVE_PUSH_PROPERTY_PUSH_TYPE_READ       = "push_type_read";
    public static final String OLIVE_PUSH_PROPERTY_PUSH_TYPE_SIGNOUT    = "push_type_signout";
    public static final String OLIVE_PUSH_PROPERTY_ROOM_ID              = "room_id";
	public static final String OLIVE_PUSH_PROPERTY_SENDER               = "sender";

    public interface OLIVE_PROPERTY_SUCCESS {
        public static final String OLIVE_PROPERTY_MESSAGE = "message";
        public static final String OLIVE_PROPERTY_DATA = "data";
        public static final String OLIVE_PROPERTY_SUCCESS = "success";
    }

    public interface OLIVE_PROPERTY_ERROR extends OLIVE_PROPERTY_SUCCESS {
        public static final String OLIVE_PROPERTY_ERROR_CODE = "error_code";
    }

    public interface OLIVE_PROPERTY_PROFILE_LIST_ITEM {
        public static final String OLIVE_PROPERTY_USERNAME = "username";
        public static final String OLIVE_PROPERTY_PHONE = "phone";
        public static final String OLIVE_PROPERTY_PICTURE = "picture";
        public static final String OLIVE_PROPERTY_MEDIAURL = "mediaurl";
        public static final String OLIVE_PROPERTY_MODIFIED = "update_date";
    }

    public interface OLIVE_PROPERTY_MESSAGE_LIST_ITEM {
        public static final String OLIVE_PROPERTY_MESSAGE_ID = "message_id";
        public static final String OLIVE_PROPERTY_ROOM_ID = "room_id";
        public static final String OLIVE_PROPERTY_AUTHOR = "author";
        public static final String OLIVE_PROPERTY_MSG_TYPE = "msg_type";
        public static final String OLIVE_PROPERTY_CONTENTS = "contents";
        public static final String OLIVE_PROPERTY_MEDIAURL = "mediaurl";
        public static final String OLIVE_PROPERTY_REG_DATE = "reg_date";
    }

    public interface OLIVE_PROPERTY_ROOM_LIST_ITEM {
        public static final String OLIVE_PROPERTY_ROOM_ID = "id";
        public static final String OLIVE_PROPERTY_ROOM_CREATE_DATE = "create_date";
//        public static final String OLIVE_PROPERTY_ROOM_LAST_MSG_BASE = "last_msg";
//        public static final String OLIVE_PROPERTY_ROOM_LAST_MSG_ID = appendProperty(OLIVE_PROPERTY_ROOM_LAST_MSG_BASE, OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_MESSAGE_ID);
//        public static final String OLIVE_PROPERTY_ROOM_LAST_MSG_ROOM_ID = appendProperty(OLIVE_PROPERTY_ROOM_LAST_MSG_BASE, OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_ROOM_ID);
//        public static final String OLIVE_PROPERTY_ROOM_LAST_MSG_AUTHOR = appendProperty(OLIVE_PROPERTY_ROOM_LAST_MSG_BASE, OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_AUTHOR);
//        public static final String OLIVE_PROPERTY_ROOM_LAST_MSG_TYPE = appendProperty(OLIVE_PROPERTY_ROOM_LAST_MSG_BASE, OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_MSG_TYPE);
//        public static final String OLIVE_PROPERTY_ROOM_LAST_MSG_CONTENTS = appendProperty(OLIVE_PROPERTY_ROOM_LAST_MSG_BASE, OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_CONTENTS);
//        public static final String OLIVE_PROPERTY_ROOM_LAST_MSG_REG_DATE = appendProperty(OLIVE_PROPERTY_ROOM_LAST_MSG_BASE, OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_REG_DATE);
//        public static final String OLIVE_PROPERTY_ROOM_CREATOR_BASE = "creator";
//        public static final String OLIVE_PROPERTY_ROOM_CREATOR_USERNAME = appendProperty(OLIVE_PROPERTY_ROOM_CREATOR_BASE, OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_USERNAME);
//        public static final String OLIVE_PROPERTY_ROOM_CREATOR_PHONE = appendProperty(OLIVE_PROPERTY_ROOM_CREATOR_BASE, OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_PHONE);
//        public static final String OLIVE_PROPERTY_ROOM_CREATOR_PICTURE = appendProperty(OLIVE_PROPERTY_ROOM_CREATOR_BASE, OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_PICTURE);
//        public static final String OLIVE_PROPERTY_ROOM_CREATOR_MODIFIED = appendProperty(OLIVE_PROPERTY_ROOM_CREATOR_BASE, OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_MODIFIED);
//        public static final String OLIVE_PROPERTY_ROOM_ATTENDANTS = "room_attentdants";
    }

    public interface OLIVE_PROPERTY_ROOM_BUNDLE_ITEM {  // ROOM + ROOM_ATTENDANTS
        public static final String OLIVE_PROPERTY_ROOM_BASE = "room";
        public static final String OLIVE_PROPERTY_ROOM_ID = appendProperty(OLIVE_PROPERTY_ROOM_BASE, OLIVE_PROPERTY_ROOM_LIST_ITEM.OLIVE_PROPERTY_ROOM_ID);
        public static final String OLIVE_PROPERTY_ROOM_CREATE_DATE = appendProperty(OLIVE_PROPERTY_ROOM_BASE, OLIVE_PROPERTY_ROOM_LIST_ITEM.OLIVE_PROPERTY_ROOM_CREATE_DATE);
        //        public static final String OLIVE_PROPERTY_ROOM_LAST_MSG_BASE = appendProperty(OLIVE_PROPERTY_ROOM_BASE, "last_msg");
//        public static final String OLIVE_PROPERTY_ROOM_LAST_MSG_ID = appendProperty(OLIVE_PROPERTY_ROOM_LAST_MSG_BASE, OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_MESSAGE_ID);
//        public static final String OLIVE_PROPERTY_ROOM_LAST_MSG_ROOM_ID = appendProperty(OLIVE_PROPERTY_ROOM_LAST_MSG_BASE, OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_ROOM_ID);
//        public static final String OLIVE_PROPERTY_ROOM_LAST_MSG_AUTHOR = appendProperty(OLIVE_PROPERTY_ROOM_LAST_MSG_BASE, OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_AUTHOR);
//        public static final String OLIVE_PROPERTY_ROOM_LAST_MSG_TYPE = appendProperty(OLIVE_PROPERTY_ROOM_LAST_MSG_BASE, OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_MSG_TYPE);
//        public static final String OLIVE_PROPERTY_ROOM_LAST_MSG_CONTENTS = appendProperty(OLIVE_PROPERTY_ROOM_LAST_MSG_BASE, OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_CONTENTS);
//        public static final String OLIVE_PROPERTY_ROOM_LAST_MSG_REG_DATE = appendProperty(OLIVE_PROPERTY_ROOM_LAST_MSG_BASE, OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_REG_DATE);
//        public static final String OLIVE_PROPERTY_ROOM_CREATOR_BASE = appendProperty(OLIVE_PROPERTY_ROOM_BASE, "creator");
//        public static final String OLIVE_PROPERTY_ROOM_CREATOR_USERNAME = appendProperty(OLIVE_PROPERTY_ROOM_CREATOR_BASE, OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_USERNAME);
//        public static final String OLIVE_PROPERTY_ROOM_CREATOR_PHONE = appendProperty(OLIVE_PROPERTY_ROOM_CREATOR_BASE, OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_PHONE);
//        public static final String OLIVE_PROPERTY_ROOM_CREATOR_PICTURE = appendProperty(OLIVE_PROPERTY_ROOM_CREATOR_BASE, OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_PICTURE);
//        public static final String OLIVE_PROPERTY_ROOM_CREATOR_MODIFIED = appendProperty(OLIVE_PROPERTY_ROOM_CREATOR_BASE, OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_MODIFIED);
        public static final String OLIVE_PROPERTY_ROOM_CREATED = "room_created";
        public static final String OLIVE_PROPERTY_ROOM_ATTENDANTS = "room_attentdants";
    }

    public interface OLIVE_PROPERTY_USER_PROFILE extends OLIVE_PROPERTY_SUCCESS {
        public static final String OLIVE_PROPERTY_PROFILE_LIST = OLIVE_PROPERTY_DATA;
    }

    public interface OLIVE_PROPERTY_FRIENDS_PROFILE extends OLIVE_PROPERTY_USER_PROFILE {}

    public interface OLIVE_PROPERTY_FRIENDS_FIND extends OLIVE_PROPERTY_SUCCESS {
        public static final String OLIVE_PROPERTY_PROFILE_LIST = OLIVE_PROPERTY_DATA;
        public static final String OLIVE_PROPERTY_EMAILS_LIST = appendProperty(OLIVE_PROPERTY_DATA, "emails");
        public static final String OLIVE_PROPERTY_CONTACTS_LIST = appendProperty(OLIVE_PROPERTY_DATA, "contacts");
    }

    public interface OLIVE_PROPERTY_ROOM_INFO extends OLIVE_PROPERTY_SUCCESS {
        public static final String OLIVE_PROPERTY_ROOM_ID = appendProperty(OLIVE_PROPERTY_DATA, OLIVE_PROPERTY_ROOM_BUNDLE_ITEM.OLIVE_PROPERTY_ROOM_ID);
        // ROOM
        public static final String OLIVE_PROPERTY_ROOM_CREATE_DATE = appendProperty(OLIVE_PROPERTY_DATA, OLIVE_PROPERTY_ROOM_BUNDLE_ITEM.OLIVE_PROPERTY_ROOM_CREATE_DATE);
//        public static final String OLIVE_PROPERTY_ROOM_LAST_MSG_BASE = appendProperty(OLIVE_PROPERTY_DATA, OLIVE_PROPERTY_ROOM_BUNDLE_ITEM.OLIVE_PROPERTY_ROOM_LAST_MSG_BASE);
//        public static final String OLIVE_PROPERTY_ROOM_LAST_MSG_ID = appendProperty(OLIVE_PROPERTY_ROOM_LAST_MSG_BASE, OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_MESSAGE_ID);
//        public static final String OLIVE_PROPERTY_ROOM_LAST_MSG_ROOM_ID = appendProperty(OLIVE_PROPERTY_ROOM_LAST_MSG_BASE, OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_ROOM_ID);
//        public static final String OLIVE_PROPERTY_ROOM_LAST_MSG_AUTHOR = appendProperty(OLIVE_PROPERTY_ROOM_LAST_MSG_BASE, OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_AUTHOR);
//        public static final String OLIVE_PROPERTY_ROOM_LAST_MSG_TYPE = appendProperty(OLIVE_PROPERTY_ROOM_LAST_MSG_BASE, OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_MSG_TYPE);
//        public static final String OLIVE_PROPERTY_ROOM_LAST_MSG_CONTENTS = appendProperty(OLIVE_PROPERTY_ROOM_LAST_MSG_BASE, OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_CONTENTS);
//        public static final String OLIVE_PROPERTY_ROOM_LAST_MSG_REG_DATE = appendProperty(OLIVE_PROPERTY_ROOM_LAST_MSG_BASE, OLIVE_PROPERTY_MESSAGE_LIST_ITEM.OLIVE_PROPERTY_REG_DATE);
//        public static final String OLIVE_PROPERTY_ROOM_CREATOR_BASE = appendProperty(OLIVE_PROPERTY_DATA, OLIVE_PROPERTY_ROOM_BUNDLE_ITEM.OLIVE_PROPERTY_ROOM_CREATOR_BASE);
//        public static final String OLIVE_PROPERTY_ROOM_CREATOR_USERNAME = appendProperty(OLIVE_PROPERTY_ROOM_CREATOR_BASE, OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_USERNAME);
//        public static final String OLIVE_PROPERTY_ROOM_CREATOR_PHONE = appendProperty(OLIVE_PROPERTY_ROOM_CREATOR_BASE, OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_PHONE);
//        public static final String OLIVE_PROPERTY_ROOM_CREATOR_PICTURE = appendProperty(OLIVE_PROPERTY_ROOM_CREATOR_BASE, OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_PICTURE);
//        public static final String OLIVE_PROPERTY_ROOM_CREATOR_MODIFIED = appendProperty(OLIVE_PROPERTY_ROOM_CREATOR_BASE, OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_MODIFIED);
        // ROOM_ATTENDANTS
        public static final String OLIVE_PROPERTY_ROOM_ATTENDANTS_LIST = appendProperty(OLIVE_PROPERTY_DATA, OLIVE_PROPERTY_ROOM_BUNDLE_ITEM.OLIVE_PROPERTY_ROOM_ATTENDANTS);
    }

    public interface OLIVE_PROPERTY_ROOMS_LIST extends OLIVE_PROPERTY_SUCCESS {
        public static final String OLIVE_PROPERTY_ROOM_LIST = OLIVE_PROPERTY_DATA;
    }

    public interface OLIVE_PROPERTY_MESSAGE_INFO extends OLIVE_PROPERTY_SUCCESS {
        public static final String OLIVE_PROPERTY_MESSAGE_ID = appendProperty(OLIVE_PROPERTY_DATA, "message_id");
        public static final String OLIVE_PROPERTY_ROOM_ID = appendProperty(OLIVE_PROPERTY_DATA, "room_id");
        public static final String OLIVE_PROPERTY_AUTHOR = appendProperty(OLIVE_PROPERTY_DATA, "author");
        public static final String OLIVE_PROPERTY_MSG_TYPE = appendProperty(OLIVE_PROPERTY_DATA, "msg_type");
        public static final String OLIVE_PROPERTY_CONTENTS = appendProperty(OLIVE_PROPERTY_DATA, "contents");
        public static final String OLIVE_PROPERTY_MEDIAURL = appendProperty(OLIVE_PROPERTY_DATA, "mediaurl");
        public static final String OLIVE_PROPERTY_REG_DATE = appendProperty(OLIVE_PROPERTY_DATA, "reg_date");
    }

    public interface OLIVE_PROPERTY_MESSAGES_LIST extends OLIVE_PROPERTY_SUCCESS {
        public static final String OLIVE_PROPERTY_MESSAGE_LIST = OLIVE_PROPERTY_DATA;
    }


    public static final String OLIVE_RETURN_FAILED			= "__FAILED__";

	private static Context sContext = null;
	private static RESTApiManager sInstance = null;
	
	public static RESTApiManager getInstance() {
		android.util.Log.d(TAG, "getInstance = " + sInstance);
		return sInstance;
	}
	
	protected static void setInstance(RESTApiManager helper) {
		android.util.Log.d(TAG, "setInstance = " + helper);
		sInstance = helper;
	}

	protected static Context getContext() {
		return sContext;
	}
	
	protected static void setContext(Context context) {
		android.util.Log.d(TAG, "setContext = " + context);
		sContext = context;
	}

    public static class RestClient {
        private final static String TAG = "RestClient";

        public final static int GET = 0;
        public final static int POST = 1;

        private ArrayList<NameValuePair> params;
        private ArrayList <NameValuePair> headers;

        private String url;

        private int responseCode;
        private String message;

        private String response;



        public String getResponse() {
            return response;
        }

        public String getErrorMessage() {
            return message;
        }

        public int getResponseCode() {
            return responseCode;
        }

        public RestClient(String url)
        {
            this.url = url;
            params = new ArrayList<NameValuePair>();
            headers = new ArrayList<NameValuePair>();
        }

        public void AddParam(String name, String value)
        {
            params.add(new BasicNameValuePair(name, value));
        }

        public void AddHeader(String name, String value)
        {
            headers.add(new BasicNameValuePair(name, value));
        }

        public void Execute(int method) throws Exception
        {
            switch(method) {
                case GET:
                {
                    //add parameters
                    String combinedParams = "";
                    if(!params.isEmpty()){
                        combinedParams += "?";
                        for(NameValuePair p : params)
                        {
                            String paramString = p.getName() + "="
                                    + URLEncoder.encode(p.getValue(), "UTF-8");
                            if(combinedParams.length() > 1)
                            {
                                combinedParams  +=  "&" + paramString;
                            }
                            else
                            {
                                combinedParams += paramString;
                            }
                        }
                    }

                    HttpGet request = new HttpGet(url + combinedParams);

                    //add headers
                    for(NameValuePair h : headers)
                    {
                        request.addHeader(h.getName(), h.getValue());
                    }

                    executeRequest(request, url);
                    break;
                }
                case POST:
                {
                    HttpPost request = new HttpPost(url);

                    //add headers
                    for(NameValuePair h : headers)
                    {
                        request.addHeader(h.getName(), h.getValue());
                    }

                    if(!params.isEmpty()){
                        //request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

                        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                        builder.setCharset(MIME.UTF8_CHARSET);

                        for(NameValuePair p : params) {
                            if(p.getValue().startsWith("file://")) {
                                String path = p.getValue().substring(7);
                                File file = new File (path);
                                if (file.exists()) {
                                    builder.addBinaryBody(p.getName(), file, ContentType.MULTIPART_FORM_DATA, file.getName());
                                }
                            } else {
                                // Normal string data
                                builder.addTextBody(p.getName(), p.getValue(), ContentType.create("text/plain", MIME.UTF8_CHARSET));
                            }
                        }

                        request.setEntity(builder.build());
                    }

                    executeRequest(request, url);
                    break;
                }
            }
        }

        private void executeRequest(HttpUriRequest request, String url)
        {
            HttpClient client = new DefaultHttpClient();
            client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

            HttpResponse httpResponse;

            if(android.os.Build.VERSION.SDK_INT > 9) {
                android.util.Log.d(TAG, "Network Strict Mode On!");

                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }

            try {
                httpResponse = client.execute(request);
                responseCode = httpResponse.getStatusLine().getStatusCode();
                message = httpResponse.getStatusLine().getReasonPhrase();

                HttpEntity entity = httpResponse.getEntity();

                if (entity != null) {

                    InputStream instream = entity.getContent();
                    response = convertStreamToString(instream);

                    // Closing the input stream will trigger connection release
                    instream.close();
                }

            } catch (ClientProtocolException e)  {
                client.getConnectionManager().shutdown();
                e.printStackTrace();
            } catch (IOException e) {
                client.getConnectionManager().shutdown();
                e.printStackTrace();
            }
        }

        private static String convertStreamToString(InputStream is) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();
        }
    }


    /**
     * 서버에 데이터를 보내는 메소드
     * @param msg
     * @return
     */
    /*
    protected String SendByHttp(String msg) {
        if(msg == null)
            msg = "";

        String URL = "http://###.###.###.###:8080/TeamNote/JSONServer.jsp";

        DefaultHttpClient client = new DefaultHttpClient();
        try {
			// 체크할 id와 pwd값 서버로 전송
            HttpPost post = new HttpPost(URL+"?msg="+msg);

			// 지연시간 최대 5초
            HttpParams params = client.getParams();
            HttpConnectionParams.setConnectionTimeout(params, 3000);
            HttpConnectionParams.setSoTimeout(params, 3000);

			// 데이터 보낸 뒤 서버에서 데이터를 받아오는 과정
            HttpResponse response = client.execute(post);
            BufferedReader bufreader = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent(),
                            "utf-8"));

            String line = null;
            String result = "";

            while ((line = bufreader.readLine()) != null) {
                result += line;
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            client.getConnectionManager().shutdown();	// 연결 지연 종료
            return "";
        }

    }
    */

    private static String appendProperty(String author, String options) {
        return author + "::" + options;
    }

    // interface
	public abstract int signUp(String username, String password);
	public abstract int signIn(String username, String password);
	public abstract int signOut();
    public abstract boolean verifyDevice();
	public abstract boolean isAutoSignIn();

	public abstract HashMap<String, String>             getUserProfile();   // <- 동작 안함
    public abstract int	                                updateUserPhonenumber(String phonenumber);
    public abstract int	                                updateUserPicture(Bitmap bitmap);
    public abstract int	                                changePassword(String oldPassword, String newPassword);

    public abstract int                                 addFriends(String[] usernames);
    public abstract int                                 removeFriends(String[] usernames);
    public abstract ArrayList<HashMap<String, String>>  findFriends(String[] emails, String[] contacts);
    public abstract ArrayList<HashMap<String, String>>  getFriendsList(); // <- 동작 안함 (서버의 친구 리스트 (아이디/휴대전화/최종수정시간 리스트)
	public abstract ArrayList<HashMap<String, String>>  getFriendsProfile(String[] usernames); // <- 동작 안함 (친구의 아이디/ 휴대전화/ 사진/ 최종수정시간)
    public abstract HashMap<String, String>             createRoom(String[] usernames);
    public abstract int                                 leaveRoom(long idRoom);
    public abstract ArrayList<HashMap<String, String>>  getRoomsList();
    public abstract HashMap<String, String>             getRoomInfo(long idRoom, ArrayList<HashMap<String, String>> participants);
	public abstract HashMap<String, String>             sendMessage(long idRoom, String author, String mimetype, String context);
    public abstract ArrayList<HashMap<String, String>>  receiveMessages(long idRoom);
	public abstract int                                 readMessages(long idRoom);
}
