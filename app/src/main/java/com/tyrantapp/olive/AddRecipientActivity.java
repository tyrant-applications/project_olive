package com.tyrantapp.olive;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tyrantapp.olive.helper.RESTHelper;
import com.tyrantapp.olive.providers.OliveContentProvider;
import com.tyrantapp.olive.types.UserInfo;


public class AddRecipientActivity extends BaseActivity {
    private final static String TAG = "AddRecipientActivity";

    private UserInfo mFoundInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipient);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_recipient, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onSearchPhoneNumber(View view) {
        String phoneNumber = ((EditText)findViewById(R.id.edit_phonenumber)).getText().toString();
        UserInfo info = mRESTHelper.getRecipientProfile(null, phoneNumber);

        updateRecipientInfo(info);
    }

    public void onSearchID(View view) {
        String searchId = ((EditText)findViewById(R.id.edit_searchId)).getText().toString();
        UserInfo info = mRESTHelper.getRecipientProfile(searchId, null);

        updateRecipientInfo(info);
    }

    public void onAddRecipient(View view) {
        if (mFoundInfo != null) {
            Cursor cursor = getContentResolver().query(
                    OliveContentProvider.RecipientColumns.CONTENT_URI,
                    new String[] {OliveContentProvider.RecipientColumns._ID, },
                    OliveContentProvider.RecipientColumns.USERNAME + "=?",
                    new String[] { mFoundInfo.mUsername, },
                    null
                    );

            if (cursor.getCount() > 0) {
                android.util.Log.d("Olive", "Not insert recipient!");
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_error_already_registered), Toast.LENGTH_SHORT).show();
            } else {
                ContentValues values = new ContentValues();
                values.put(OliveContentProvider.RecipientColumns.USERNAME, mFoundInfo.mUsername);
                values.put(OliveContentProvider.RecipientColumns.NICKNAME, mFoundInfo.mNickname);
                values.put(OliveContentProvider.RecipientColumns.PHONENUMBER, mFoundInfo.mPhoneNumber);
                values.put(OliveContentProvider.RecipientColumns.UNREAD, false);

                getContentResolver().insert(OliveContentProvider.RecipientColumns.CONTENT_URI, values);
                android.util.Log.d("Olive", "Insert recipient!");
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_succeed_add_recipient), Toast.LENGTH_SHORT).show();
            }
        } else {
            android.util.Log.d("Olive", "Failed Insert recipient!");
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_failed_add_recipient), Toast.LENGTH_SHORT).show();
        }
    }

    public void onBack(View view) {
        finish();
    }

    private void updateRecipientInfo(UserInfo info) {
        if (info != null) {
            mFoundInfo = info;
            ((TextView) findViewById(R.id.text_found_id)).setText(info.mUsername);
            ((TextView) findViewById(R.id.text_found_name)).setText(info.mNickname);
            ((TextView) findViewById(R.id.text_found_phonenumber)).setText(info.mPhoneNumber);
        } else {
            mFoundInfo = null;
            ((TextView) findViewById(R.id.text_found_id)).setText(R.string.error_not_found);
            ((TextView) findViewById(R.id.text_found_name)).setText("");
            ((TextView) findViewById(R.id.text_found_phonenumber)).setText("");
        }
    }
}
