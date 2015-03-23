package com.tyrantapp.olive;

import com.tyrantapp.olive.configuration.Constants;
import com.tyrantapp.olive.helper.DatabaseHelper;
import com.tyrantapp.olive.helper.OliveHelper;
import com.tyrantapp.olive.helper.PreferenceHelper;
import com.tyrantapp.olive.network.RESTApiManager;
import com.tyrantapp.olive.type.UserProfile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.PhoneNumberUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class SettingActivity extends BaseActivity {
	private final static String TAG = "SettingActivity";

    private final static int			RESULT_LOAD_IMAGE	= 1;
    private final static int			RESULT_TAKE_PICTURE	= 2;

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

		// Get Userinfo from DB
//		RESTApiManager helper = RESTApiManager.getInstance();
//		HashMap<String, String> mapProfile = helper.getUserProfile();
//      mapProfile.get(RESTApiManager.OLIVE_PROPERTY_PROFILE_BASE.OLIVE_PROPERTY_USERNAME)

		setEnablePasscode(true);
	}

    @Override
    protected void onStart() {
        super.onStart();

        updateProfileInfo();
    }
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.setting, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}

    private void updateProfileInfo() {
        ImageView iv = (ImageView) findViewById(R.id.pref_photo);
        TextView ev = (TextView) findViewById(R.id.pref_email);
        TextView pv = (TextView) findViewById(R.id.pref_phonenumber);

        UserProfile profile = DatabaseHelper.UserHelper.getUserProfile(SettingActivity.this);

        String path = OliveHelper.getProfileImagePath(SettingActivity.this);
        Bitmap bmpProfile = OliveHelper.getCachedImage(path);
        if (bmpProfile != null) {
            Bitmap bmpCircle = OliveHelper.makeCircleBitmap(bmpProfile);
            iv.setImageBitmap(bmpCircle);
        } else {
            iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_no_photo_login));
        }

        ev.setText(profile.mUsername);
        ev.setSelected(true);

        String phoneNumber = PhoneNumberUtils.formatNumber(OliveHelper.getLineNumber(this), Locale.getDefault().getCountry());
        pv.setText(phoneNumber);
    }

    public void onChangePortrait(View view) {
        // in onCreate or any event where your want the user to
        // select a file
        final CharSequence[] chooseType = { "Take Picture", "Album", "Remove"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setTitle("Make your selection");
        builder.setItems(chooseType, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    Uri outputUri = Uri.fromFile(new File(OliveHelper.getProfileImageDir(SettingActivity.this) + "temporary.jpg"));
                    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
                    startActivityForResult(intent, RESULT_TAKE_PICTURE);
                } else
                if (item == 1) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, RESULT_LOAD_IMAGE);
                } else {
                    new File(OliveHelper.getProfileImagePath(SettingActivity.this)).delete();
                    updateProfileInfo();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
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
                        if (mRESTApiManager.signOut() == RESTApiManager.OLIVE_SUCCESS) {
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

        String selectedFile = null;
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != intent) {
            Uri selectedImage = intent.getData();

            // String picturePath contains the path of selected Image
            // first, copy file to data folder as profile.png? jpg?
            // second, upload picture.
            InputStream in = null;
            try {
                in = getContentResolver().openInputStream(selectedImage);

                if (OliveHelper.saveFile(in, OliveHelper.getProfileImageDir(this) + "temporary.jpg")) {
                    selectedFile = OliveHelper.getProfileImageDir(this) + "temporary.jpg";
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in != null) in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else
        if (requestCode == RESULT_TAKE_PICTURE && resultCode == RESULT_OK) {
            // String picturePath contains the path of selected Image
            // first, copy file to data folder as profile.png? jpg?
            // second, upload picture.

            selectedFile = OliveHelper.getProfileImageDir(this) + "temporary.jpg";
        }

        if (selectedFile != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = 0;

            int nSize = 0;
            do {
                options.inSampleSize++;
                BitmapFactory.decodeFile(selectedFile, options);
            } while ((options.outWidth * options.outHeight) > Constants.Configuration.MAX_IMAGE_RESOLUTION * Constants.Configuration.MAX_IMAGE_RESOLUTION);

            options.inJustDecodeBounds = false;
            mRESTApiManager.updateUserPicture(BitmapFactory.decodeFile(selectedFile, options));

            ignorePasscodeOnce();
        }
    }
}
