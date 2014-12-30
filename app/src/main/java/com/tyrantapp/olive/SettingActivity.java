package com.tyrantapp.olive;

import com.tyrantapp.olive.helper.PreferenceHelper;
import com.tyrantapp.olive.helper.RESTHelper;
import com.tyrantapp.olive.types.UserInfo;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Locale;

public class SettingActivity extends BaseActivity {
	private final static String TAG = "SettingActivity";
	
	public final static String OLIVE_PREF_NOTIFICATION = "olive_pref_notification";
	public final static String OLIVE_PREF_PASSCODE_LOCK = "olive_pref_passcode_lock";
	public final static String OLIVE_PREF_PASSCODE_KEY = "olive_pref_passcode_key";
	public final static String OLIVE_PREF_LOCATION_SERVICE = "olive_pref_locationservice";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		ToggleButton btnNotification = (ToggleButton) findViewById(R.id.pref_notification_switch);
		ToggleButton btnPasscodeLock = (ToggleButton) findViewById(R.id.pref_passcode_lock_switch);
		ToggleButton btnLocationService = (ToggleButton) findViewById(R.id.pref_location_service_switch);
		
		btnNotification.setChecked(PreferenceHelper.getBooleanPreferences(this, OLIVE_PREF_NOTIFICATION, true));
		btnPasscodeLock.setChecked(PreferenceHelper.getBooleanPreferences(this, OLIVE_PREF_PASSCODE_LOCK, false));
		btnLocationService.setChecked(PreferenceHelper.getBooleanPreferences(this, OLIVE_PREF_LOCATION_SERVICE, false));
		
		TextView ev = (TextView) findViewById(R.id.pref_email);
        TextView pv = (TextView) findViewById(R.id.pref_phonenumber);

		// Get Userinfo from DB
		RESTHelper helper = RESTHelper.getInstance();
		UserInfo info = helper.getUserProfile();
		
		ev.setText(info.mNickname);
		ev.setSelected(true);

        pv.setText(PhoneNumberUtils.formatNumber(info.mPhoneNumber, Locale.getDefault().getCountry()));
		
		setEnablePasscode(true);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.setting, menu);
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

    public void onChangePassword(View view) {
        startActivityForPasscode(new Intent(this, ChangePasswordActivity.class));
    }

    public void onLogout(View view) {

    }

	public void onSwitchNotification(View view) {
		android.util.Log.d(TAG, "onSwitchNotification = " + ((ToggleButton)view).isChecked());
		PreferenceHelper.saveBooleanPreferences(this, OLIVE_PREF_NOTIFICATION, ((ToggleButton)view).isChecked());
	}
	
	public void onSwitchPasscodeLock(View view) {
		android.util.Log.d(TAG, "onSwitchPasscodeLock = " + ((ToggleButton)view).isChecked());
        if (((ToggleButton)view).isChecked())
		    startActivityForPasscode(new Intent(this, PasscodeActivity.class));
        else
            PreferenceHelper.saveBooleanPreferences(this, SettingActivity.OLIVE_PREF_PASSCODE_LOCK, false);
	}
	
	public void onSwitchLocationService(View view) {
		android.util.Log.d(TAG, "onSwitchLocationService = " + ((ToggleButton)view).isChecked());
		PreferenceHelper.saveBooleanPreferences(this, OLIVE_PREF_LOCATION_SERVICE, ((ToggleButton)view).isChecked());
	}

    public void onBack(View view) {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        ToggleButton btnPasscodeLock = (ToggleButton) findViewById(R.id.pref_passcode_lock_switch);
        btnPasscodeLock.setChecked(PreferenceHelper.getBooleanPreferences(this, OLIVE_PREF_PASSCODE_LOCK, false));
    }
}
