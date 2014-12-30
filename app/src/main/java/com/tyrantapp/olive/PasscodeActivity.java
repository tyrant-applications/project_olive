package com.tyrantapp.olive;

import com.tyrantapp.olive.helper.PreferenceHelper;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class PasscodeActivity extends BaseActivity {
	private final static String TAG = "PasscodeActivity";
	
	public final static String	AUTHENTICATE_KEY	= "authenticate_key";
	public final static int		REQUEST_CODE		= 1000;
	public final static int		RESULT_SUCCESS		= 1001;

    private Handler mHandler = new Handler();

    private boolean mRegister = false;
    private boolean mRetry = false;

    private String mPasscode = "";
    private String mPasscodeKey = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_passcode);

        mRegister = !PreferenceHelper.getBooleanPreferences(this, SettingActivity.OLIVE_PREF_PASSCODE_LOCK, false);
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
        if (mRegister) {
            super.onBackPressed();
        }
	}

    public void onKeyDown(View view) {
        TextView v = (TextView)view;
        updateWildPasscode(mPasscode + v.getText().toString());

        if (mPasscode.length() >= 4) {

            if (mRegister) {
                if (!mRetry) {
                    mPasscodeKey = mPasscode;
                    changeMessage(1);
                    clearWildPasscode();
                } else {
                    if (mPasscode.equals(mPasscodeKey)) {
                        PreferenceHelper.saveStringPreferences(this, SettingActivity.OLIVE_PREF_PASSCODE_KEY, mPasscode);
                        PreferenceHelper.saveBooleanPreferences(this, SettingActivity.OLIVE_PREF_PASSCODE_LOCK, true);
                        setResult(RESULT_SUCCESS);
                        finish();
                    } else {
                        changeMessage(2);
                        clearWildPasscode();
                    }
                }
            } else {
                if (mPasscode.equals(mPasscodeKey)) {
                    setResult(RESULT_SUCCESS);
                    finish();
                } else {
                    changeMessage(2);
                    clearWildPasscode();
                }
            }
        }
    }

    public void onKeyDelete(View view) {
        if (mPasscode.length() > 0) {
            updateWildPasscode(mPasscode.substring(0, mPasscode.length() - 1));
        }
    }

    private void changeMessage(int mode) {
        switch (mode) {
            case 0: // Try
                findViewById(R.id.passcode_message_try).setVisibility(View.VISIBLE);
                findViewById(R.id.passcode_message_retry).setVisibility(View.GONE);
                findViewById(R.id.passcode_message_error).setVisibility(View.GONE);
                mRetry = false;
                break;
            case 1: // Retry
                findViewById(R.id.passcode_message_try).setVisibility(View.GONE);
                findViewById(R.id.passcode_message_retry).setVisibility(View.VISIBLE);
                findViewById(R.id.passcode_message_error).setVisibility(View.GONE);
                mRetry = true;
                break;
            case 2: // Error
                findViewById(R.id.passcode_message_try).setVisibility(View.GONE);
                findViewById(R.id.passcode_message_retry).setVisibility(View.GONE);
                findViewById(R.id.passcode_message_error).setVisibility(View.VISIBLE);
                mRetry = false;
                break;
        }
    }

    private void clearWildPasscode() {
        mHandler.postDelayed(new Runnable() {
            public void run() {
                updateWildPasscode("");
            }
        }, 500);
    }

    private void updateWildPasscode(String passcode) {
        mPasscode = passcode;

        switch (passcode.length()) {
            case 4:
                findViewById(R.id.passcode_wild_1st).setVisibility(View.VISIBLE);
                findViewById(R.id.passcode_wild_2nd).setVisibility(View.VISIBLE);
                findViewById(R.id.passcode_wild_3rd).setVisibility(View.VISIBLE);
                findViewById(R.id.passcode_wild_4th).setVisibility(View.VISIBLE);
                break;
            case 3:
                findViewById(R.id.passcode_wild_1st).setVisibility(View.VISIBLE);
                findViewById(R.id.passcode_wild_2nd).setVisibility(View.VISIBLE);
                findViewById(R.id.passcode_wild_3rd).setVisibility(View.VISIBLE);
                findViewById(R.id.passcode_wild_4th).setVisibility(View.INVISIBLE);
                break;
            case 2:
                findViewById(R.id.passcode_wild_1st).setVisibility(View.VISIBLE);
                findViewById(R.id.passcode_wild_2nd).setVisibility(View.VISIBLE);
                findViewById(R.id.passcode_wild_3rd).setVisibility(View.INVISIBLE);
                findViewById(R.id.passcode_wild_4th).setVisibility(View.INVISIBLE);
                break;
            case 1:
                findViewById(R.id.passcode_wild_1st).setVisibility(View.VISIBLE);
                findViewById(R.id.passcode_wild_2nd).setVisibility(View.INVISIBLE);
                findViewById(R.id.passcode_wild_3rd).setVisibility(View.INVISIBLE);
                findViewById(R.id.passcode_wild_4th).setVisibility(View.INVISIBLE);
                break;
            case 0:
                findViewById(R.id.passcode_wild_1st).setVisibility(View.INVISIBLE);
                findViewById(R.id.passcode_wild_2nd).setVisibility(View.INVISIBLE);
                findViewById(R.id.passcode_wild_3rd).setVisibility(View.INVISIBLE);
                findViewById(R.id.passcode_wild_4th).setVisibility(View.INVISIBLE);
                break;
        }
        android.util.Log.d(TAG, "mPasscode = " + passcode);
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
