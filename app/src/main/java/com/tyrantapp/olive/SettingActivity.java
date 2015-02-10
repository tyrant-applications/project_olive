package com.tyrantapp.olive;

import com.tyrantapp.olive.helper.DatabaseHelper;
import com.tyrantapp.olive.helper.OliveHelper;
import com.tyrantapp.olive.helper.PreferenceHelper;
import com.tyrantapp.olive.type.UserProfile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

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
//		RESTApiManager helper = RESTApiManager.getInstance();
//		HashMap<String, String> mapProfile = helper.getUserProfile();
//      mapProfile.get(RESTApiManager.OLIVE_PROPERTY_PROFILE_BASE.OLIVE_PROPERTY_USERNAME)

        UserProfile profile = DatabaseHelper.UserHelper.getUserProfile(SettingActivity.this);
		
		ev.setText(profile.mUsername);
		ev.setSelected(true);

        pv.setText(OliveHelper.formatNumber(OliveHelper.getLineNumber(this)));

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
        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(SettingActivity.this);
        alert_confirm.setMessage(R.string.alert_logout).setCancelable(false).setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 'YES'
                        // db 삭제
                        //DatabaseHelper.RecipientHelper.removeRecipients(SettingActivity.this);
                        //DatabaseHelper.SpaceHelper.removeSpaces(SettingActivity.this);
                        //DatabaseHelper.ConversationHelper.removeConversations(SettingActivity.this);
                        //DatabaseHelper.ButtonBoardHelper.removeButtonBoards(SettingActivity.this);
                        // user정보 삭제
                        if (DatabaseHelper.UserHelper.removeUserProfile(SettingActivity.this)) {
                            Intent intent = new Intent(getApplicationContext(), SignInActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }
                }).setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 'No'
                        return;
                    }
                });
        AlertDialog alert = alert_confirm.create();
        alert.show();
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
