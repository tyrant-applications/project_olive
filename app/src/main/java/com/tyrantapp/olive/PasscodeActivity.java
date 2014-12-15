package com.tyrantapp.olive;

import com.tyrantapp.olive.helper.PreferenceHelper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class PasscodeActivity extends BaseActivity {
	private final static String TAG = "PasscodeActivity";
	
	public final static String	AUTHENTICATE_KEY	= "authenticate_key";
	public final static int		REQUEST_CODE		= 1000;
	public final static int		RESULT_SUCCESS		= 1001;
	
	private EditText mPasscodeView;
	private String mPasscodeKey;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_passcode);
		
		// Set up the sign in form.
		mPasscodeView = (EditText) findViewById(R.id.passcode);
		mPasscodeView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.passcode || id == EditorInfo.IME_NULL) {
					return true;
				}
				return false;
			}
		});
		
		mPasscodeView.setOnKeyListener(new OnKeyListener() {
		
			@Override
			public boolean onKey(View view, int id, KeyEvent event) {
				TextView tv = (TextView)view;
				if (tv.getText().toString().equals(mPasscodeKey)) {
					setResult(RESULT_SUCCESS);				
					finish();
				}
				return false;
			}
			
		});
		
		mPasscodeKey = PreferenceHelper.getStringPreferences(this, SettingActivity.OLIVE_PREF_PASSCODE_KEY, "0000");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.passcode, menu);
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
	public void onBackPressed() {
		// Block Back button
		//super.onBackPressed();
	}
	
	/*
	 * Authentification Code 
	 * */
	private static final int AUTHENTICATE_TIMEOUT = 500;
	private static String sAuthenticateKey = null;
	
	public static String requestAuthenticateKey() {
		sAuthenticateKey = String.valueOf(System.currentTimeMillis() + AUTHENTICATE_TIMEOUT);
		return sAuthenticateKey;
	}
	
	public static boolean verifyAuthenticateKey(Context context, String key) {
		boolean bRet = false;
		if (PreferenceHelper.getBooleanPreferences(context, SettingActivity.OLIVE_PREF_PASSCODE_LOCK, false)) {
			try {
				//if (System.currentTimeMillis() < Long.valueOf(key)) {
				if (key != null && key.equals(sAuthenticateKey)) {
					bRet = true;
				}
			} catch (NumberFormatException e) {
			}
		} else {
			bRet = true;
		}
		sAuthenticateKey = null;
		return bRet;
	}
}
