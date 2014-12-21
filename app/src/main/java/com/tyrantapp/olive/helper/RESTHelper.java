package com.tyrantapp.olive.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.types.OliveMessage;
import com.tyrantapp.olive.types.UserInfo;

import android.content.Context;
import android.widget.Toast;

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
import org.json.JSONException;
import org.json.JSONObject;

public abstract class RESTHelper {
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
	
	public static final String OLIVE_PUSH_PROPERTY_FROM		= "from";
	public static final String OLIVE_PUSH_PROPERTY_TO		= "to";
	
	public static final String OLIVE_RETURN_FAILED			= "__FAILED__";

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	private static Context sContext = null;
	private static RESTHelper sInstance = null;
	
	public static RESTHelper getInstance() {
		android.util.Log.d(TAG, "getInstance = " + sInstance);
		return sInstance;
	}
	
	protected static void setInstance(RESTHelper helper) {
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
	
	protected static boolean isEmailAddress(String email) {
		Pattern pattern = Pattern.compile(EMAIL_PATTERN);
		if (!pattern.matcher(email).matches()) {
			Toast.makeText(
					getContext(), 
					getContext().getResources().getString(R.string.toast_error_invalid_email), 
					Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
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
                        request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                    }

                    executeRequest(request, url);
                    break;
                }
            }
        }

        private void executeRequest(HttpUriRequest request, String url)
        {
            HttpClient client = new DefaultHttpClient();

            HttpResponse httpResponse;

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

    /**
     * 받은 JSON 객체를 파싱하는 메소드
     * @param page
     * @return
     */
    protected HashMap<String, String> JSONParser(String jsonString) {
        HashMap<String, String> mapParsed = new HashMap<String, String>();

        android.util.Log.i("서버에서 받은 전체 내용 : ", jsonString);

        ArrayList<String> dequeKeys = new ArrayList<String>();
        ArrayList<String> stackName = new ArrayList<String>();
        ArrayList<JSONObject> stackObj = new ArrayList<JSONObject>();
        ArrayList<Integer> stackCount = new ArrayList<Integer>();
        try {
            JSONObject obj = new JSONObject(jsonString);
            stackObj.add(obj);
            stackCount.add(obj.length());

            Iterator<String> keys = obj.keys();
            while (keys.hasNext()) dequeKeys.add(keys.next());

            while (!dequeKeys.isEmpty()) {
                int idxLast = dequeKeys.size() - 1;
                String key = dequeKeys.get(idxLast);

                if (stackCount.get(stackCount.size() - 1) > 0) {
                    dequeKeys.remove(idxLast);
                    stackCount.set(stackCount.size() - 1, stackCount.get(stackCount.size() - 1) - 1);

                    obj = stackObj.get(stackObj.size() - 1);
                    JSONObject subObj = obj.optJSONObject(key);

                    if (subObj != null) {
                        stackName.add(key);
                        stackObj.add(subObj);
                        stackCount.add(subObj.length());

                        Iterator<String> subKeys = subObj.keys();
                        while (subKeys.hasNext()) dequeKeys.add(subKeys.next());
                    } else {
                        String fullname = new String();
                        for (String name : stackName) fullname += name + "::";
                        mapParsed.put(fullname + key, obj.getString(key));
                    }
                } else {
                    stackName.remove(stackName.size() - 1);
                    stackObj.remove(stackObj.size() - 1);
                    stackCount.remove(stackCount.size() - 1);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mapParsed;
    }
	
	
	// interface
	public abstract int signUp(String username, String password);
	public abstract int signIn(String username, String password);
	public abstract int signOut();
	public abstract boolean isSignedIn();
	public abstract UserInfo getUserProfile();
	public abstract UserInfo getRecipientProfile(String email, String phonenumber);
	public abstract int	updateUserProfile(UserInfo info);
	public abstract OliveMessage postOlive(String recipientName, String contents);
	public abstract OliveMessage[] getPendingOlives(String recipientName);
	public abstract boolean markToDispend(String recipientName);
	public abstract boolean markToRead(String recipientName);
}
