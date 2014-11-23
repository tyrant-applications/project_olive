package com.tyrantapp.olive.helper;

import java.util.regex.Pattern;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.types.OliveMessage;
import com.tyrantapp.olive.types.UserInfo;

import android.content.Context;
import android.widget.Toast;

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
	public static final int OLIVE_FAIL_INVALID_ID			= -7;
	public static final int OLIVE_FAIL_BAD_PASSWORD			= -8;
	public static final int OLIVE_FAIL_INVALID_PASSWORD		= -9;
	
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
	
	
	// interface
	public abstract int signUp(String username, String password);
	public abstract int signIn(String username, String password);
	public abstract int signOut();
	public abstract boolean isSignedIn();
	public abstract UserInfo getUserProfile();
	public abstract UserInfo getRecipientProfile(String email);
	public abstract int	updateUserProfile(UserInfo info);
	public abstract int	getUnreadCount(String username);
	public abstract	OliveMessage postOlive(String username, String contents);
	public abstract OliveMessage[] getPendingOlives(String username);
	public abstract boolean markToRead(String username);
}
