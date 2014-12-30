package com.tyrantapp.olive;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.tyrantapp.olive.helper.OliveHelper;
import com.tyrantapp.olive.helper.RESTHelper;
import com.tyrantapp.olive.providers.OliveContentProvider;
import com.tyrantapp.olive.types.UserInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;


public class AddRecipientActivity extends BaseActivity {
    private final static String TAG = "AddRecipientActivity";

    //private UserInfo mFoundInfo = null;
    private ListView mListView;
    private TextView mErrorView;
    private MySimpleArrayAdapter mAdapter;
    private ArrayList<UserInfo> mFoundInfoList = new ArrayList<UserInfo>();

    private EditText mEditTextSearchKey;

    private Handler mConfirmHandler = new Handler() {
        public void handleMessage(Message message) {
            ArrayList<UserInfo> arrayInfo = (ArrayList<UserInfo>)message.obj;
            updateRecipientInfo(arrayInfo);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipient);

        mAdapter = new MySimpleArrayAdapter(this, R.layout.found_friend_item, mFoundInfoList);
        mListView = (ListView) findViewById(R.id.found_listview);
        mListView.setAdapter(mAdapter);

        mErrorView = (TextView) findViewById(R.id.found_error);

        mEditTextSearchKey = ((EditText)findViewById(R.id.edit_search_friend));
        mEditTextSearchKey.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
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

    public void onSearchFriends(View view) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Loading. Please wait...");
        dialog.getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
        //dialog.setCancelable(false);
        dialog.show();

        mFoundInfoList.clear();
        mAdapter.notifyDataSetChanged();

        new Thread(new Runnable() {
            public void run() {
                String displayName="", emailAddress="", phoneNumber="";
                ContentResolver cr =getContentResolver();
                Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                ArrayList<UserInfo> arrayInfo = new ArrayList<UserInfo>();

                while (cursor.moveToNext()) {
                    if (!dialog.isShowing()) {
                        mConfirmHandler.post(
                                new Runnable() {
                                    public void run() {
                                        Toast.makeText(AddRecipientActivity.this, "Cancelled loading from contacts.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
                        break;
                    }

                    displayName = "";
                    emailAddress = "";
                    phoneNumber = "";
                    displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

//                    Cursor emails = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + id, null, null);
//                    while (emails.moveToNext()) {
//                        emailAddress = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
//                        break;
//                    }
//                    emails.close();

                    if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                        while (pCur.moveToNext()) {
                            phoneNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            break;
                        }
                        pCur.close();
                    }

                    android.util.Log.d(TAG, "DisplayName: " + displayName + ", PhoneNumber: " + phoneNumber + ", EmailAddress: " + emailAddress);

                    UserInfo info = null;
                    if (OliveHelper.isEmailAddress(AddRecipientActivity.this, emailAddress)) {
                        info = mRESTHelper.getRecipientProfile(emailAddress, null);
                    }
                    if (info == null && PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
                        phoneNumber = PhoneNumberUtils.formatNumberToE164(phoneNumber, Locale.getDefault().getCountry());
                        if (phoneNumber != null) {
                            info = mRESTHelper.getRecipientProfile(null, phoneNumber);
                        }
                    }
                    if (info != null) {
                        info.mNickname = displayName;
                        arrayInfo.add(info);
                    }
                }

                Message msg = mConfirmHandler.obtainMessage();
                msg.obj = arrayInfo;
                mConfirmHandler.sendMessage(msg);
                cursor.close();

                dialog.dismiss();
            }
        }).start();
    }

    public void onSearchFriend(View view) {
        String keyword = mEditTextSearchKey.getText().toString();
        mFoundInfoList.clear();
        mAdapter.notifyDataSetChanged();
        UserInfo info = null;
        if (PhoneNumberUtils.isGlobalPhoneNumber(keyword)) {
            String phoneNumber = PhoneNumberUtils.formatNumberToE164(keyword, Locale.getDefault().getCountry());
            info = mRESTHelper.getRecipientProfile(null, phoneNumber);
        } else
        if (OliveHelper.isEmailAddress(this, keyword)) {
            info = mRESTHelper.getRecipientProfile(keyword, null);
        }
        updateRecipientInfo(info);
    }

    public void onAddRecipient(View view) {
        if (!mFoundInfoList.isEmpty()) {
            for (UserInfo info : mFoundInfoList) {
                if (mAdapter.isSelected(info.mUsername)) {
                    Cursor cursor = getContentResolver().query(
                            OliveContentProvider.RecipientColumns.CONTENT_URI,
                            new String[]{OliveContentProvider.RecipientColumns._ID,},
                            OliveContentProvider.RecipientColumns.USERNAME + "=?",
                            new String[]{info.mUsername,},
                            null
                    );

                    if (cursor.getCount() > 0) {
                        android.util.Log.d("Olive", "Not insert recipient!");
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_error_already_registered), Toast.LENGTH_SHORT).show();
                    } else {
                        ContentValues values = new ContentValues();
                        values.put(OliveContentProvider.RecipientColumns.USERNAME, info.mUsername);
                        values.put(OliveContentProvider.RecipientColumns.NICKNAME, info.mNickname);
                        values.put(OliveContentProvider.RecipientColumns.PHONENUMBER, info.mPhoneNumber);
                        values.put(OliveContentProvider.RecipientColumns.UNREAD, false);

                        getContentResolver().insert(OliveContentProvider.RecipientColumns.CONTENT_URI, values);
                        android.util.Log.d("Olive", "Insert recipient!");
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_succeed_add_recipient), Toast.LENGTH_SHORT).show();
                    }
                    cursor.close();
                }
            }
        } else {
            android.util.Log.d("Olive", "Failed Insert recipient!");
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_failed_add_recipient), Toast.LENGTH_SHORT).show();
        }

        mFoundInfoList.clear();
        mAdapter.notifyDataSetChanged();
        mEditTextSearchKey.setText("");
    }

    public void onBack(View view) {
        finish();
    }

    private void updateRecipientInfo(UserInfo info) {
        if (info != null) {
            mFoundInfoList.add(info);
            mAdapter.notifyDataSetChanged();

            mErrorView.setVisibility(View.GONE);
        } else {
            mFoundInfoList.clear();
            mAdapter.notifyDataSetChanged();

            // Need to add UI for notifying "not found"
            mErrorView.setVisibility(View.VISIBLE);
        }
    }

    private void updateRecipientInfo(ArrayList<UserInfo> array) {
        if (array != null && !array.isEmpty()) {
            for (UserInfo info : array) mFoundInfoList.add(info);
            mAdapter.notifyDataSetChanged();

            mErrorView.setVisibility(View.GONE);
        } else {
            mFoundInfoList.clear();
            mAdapter.notifyDataSetChanged();

            // Need to add UI for notifying "not found"
            mErrorView.setVisibility(View.VISIBLE);
        }
    }

    public class MySimpleArrayAdapter extends ArrayAdapter<UserInfo> implements CompoundButton.OnCheckedChangeListener {
        private ArrayList<UserInfo> mItems;
        private int mResourceId;
        private HashMap<String, Boolean> mMapSelected = new HashMap<String, Boolean>();

        public MySimpleArrayAdapter(Context context, int textViewResourceId, ArrayList<UserInfo> items) {
            super(context, textViewResourceId, items);
            mResourceId = textViewResourceId;
            mItems = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            MySimpleViewHolder holder = null;

            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(mResourceId, null);
                holder = new MySimpleViewHolder();
                holder.mPhoto = (ImageView)v.findViewById(R.id.img_found_photo);
                holder.mUsername = (TextView)v.findViewById(R.id.text_found_name);
                holder.mChecked = (ToggleButton)v.findViewById(R.id.check_should_add);
                v.setTag(holder);
            } else {
                holder = (MySimpleViewHolder)v.getTag();
            }

            UserInfo info = mItems.get(position);
            if (info != null) {
                boolean bChecked = (mMapSelected.get(info.mUsername) != null) ? mMapSelected.get(info.mUsername).booleanValue() : false;
                holder.mUsername.setText(info.mUsername);
                holder.mUsername.setSelected(true);
                holder.mChecked.setChecked(bChecked);
                holder.mChecked.setTag(info.mUsername);
                holder.mChecked.setOnCheckedChangeListener(this);
            }
            return v;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            
            mMapSelected.clear();
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mMapSelected.put((String)buttonView.getTag(), isChecked);
        }

        public boolean isSelected(String username) {
            return (mMapSelected.get(username) != null) ? mMapSelected.get(username).booleanValue() : false;
        }
    }

    private static class MySimpleViewHolder {
        public ImageView mPhoto;
        public TextView mUsername;
        public ToggleButton mChecked;
    }
}
