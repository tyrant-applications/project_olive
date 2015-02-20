package com.tyrantapp.olive;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.tyrantapp.olive.helper.DatabaseHelper;
import com.tyrantapp.olive.helper.OliveHelper;
import com.tyrantapp.olive.network.RESTApiManager;
import com.tyrantapp.olive.provider.OliveContentProvider;
import com.tyrantapp.olive.service.SyncNetworkService;
import com.tyrantapp.olive.type.RecipientInfo;
import com.tyrantapp.olive.type.UserProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class AddRecipientActivity extends BaseActivity {
    private final static String TAG = "AddRecipientActivity";

    //private RecipientInfo mFoundInfo = null;
    private ListView mListView;
    private TextView mErrorView;
    private MySimpleArrayAdapter mAdapter;
    private ArrayList<RecipientInfo> mFoundInfoList = new ArrayList<RecipientInfo>();

    private EditText mEditTextSearchKey;

    private Handler mConfirmHandler = new Handler() {
        public void handleMessage(Message message) {
            ArrayList<RecipientInfo> arrayInfo = (ArrayList<RecipientInfo>)message.obj;
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

        setEnablePasscode(true);
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
                Cursor cursor = DatabaseHelper.ContactProviderHelper.getCursor(AddRecipientActivity.this);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();

                    ArrayList<String> listPhoneNumber = new ArrayList<String>();
                    ArrayList<String> listEmail = new ArrayList<String>();
                    do {
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

                        String value = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ContactProviderHelper.DATA));
                        if (OliveHelper.isEmailAddress(AddRecipientActivity.this, value)) {
                            listEmail.add(value);
                        } else if (OliveHelper.isPhoneNumber(value)) {
                            listPhoneNumber.add(OliveHelper.formatNumber(value));
                        }
                    } while (cursor.moveToNext());

                    String[] arrPhonenumbers = new String[listPhoneNumber.size()];
                    for (int i=0; i<listPhoneNumber.size(); i++) {
                        arrPhonenumbers[i] = listPhoneNumber.get(i);
                    }
                    String[] arrEmails = new String[listEmail.size()];
                    for (int i=0; i<listEmail.size(); i++) {
                        arrEmails[i] = listEmail.get(i);
                    }

                    ArrayList<RecipientInfo> listInfo = null;
                    ArrayList<HashMap<String, String>> listFriends = mRESTApiManager.findFriends(arrEmails, arrPhonenumbers);
                    if (listFriends != null && listFriends.size() > 0) {
                        listInfo = new ArrayList<RecipientInfo>();
                        for (HashMap<String, String> friend : listFriends) {
                            RecipientInfo info = new RecipientInfo();
                            info.mUsername = friend.get(RESTApiManager.OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_USERNAME);
                            info.mPhoneNumber = friend.get(RESTApiManager.OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_PHONE);
                            info.mDisplayname = DatabaseHelper.ContactProviderHelper.getDisplayname(AddRecipientActivity.this, info.mUsername, info.mPhoneNumber);
                            info.mPicture = friend.get(RESTApiManager.OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_PICTURE);
                            info.mMediaURL = friend.get(RESTApiManager.OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_MEDIAURL);
                            listInfo.add(info);
                        }
                    }

                    Message msg = mConfirmHandler.obtainMessage();
                    msg.obj = listInfo;
                    mConfirmHandler.sendMessage(msg);
                    cursor.close();
                }

                dialog.dismiss();
            }
        }).start();
    }

    public void onSearchFriend(View view) {
        String keyword = mEditTextSearchKey.getText().toString();
        ArrayList<RecipientInfo> listInfo = null;
        ArrayList<HashMap<String, String>> listFriends = null;
        if (PhoneNumberUtils.isGlobalPhoneNumber(keyword)) {
            String phoneNumber = OliveHelper.formatNumber(keyword);
            listFriends = mRESTApiManager.findFriends(null, new String[] { phoneNumber, });
        } else
        if (OliveHelper.isEmailAddress(this, keyword)) {
            listFriends = mRESTApiManager.findFriends(new String[] { keyword, }, null);
        }

        if (listFriends != null && listFriends.size() > 0) {
            listInfo = new ArrayList<RecipientInfo>();
            for (HashMap<String, String> friend : listFriends) {
                RecipientInfo info = new RecipientInfo();
                info.mUsername = friend.get(RESTApiManager.OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_USERNAME);
                info.mPhoneNumber = friend.get(RESTApiManager.OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_PHONE);
                info.mDisplayname = DatabaseHelper.ContactProviderHelper.getDisplayname(this, info.mUsername, info.mPhoneNumber);
                info.mPicture = friend.get(RESTApiManager.OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_PICTURE);
                info.mMediaURL = friend.get(RESTApiManager.OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_MEDIAURL);
                listInfo.add(info);
            }
        }
        updateRecipientInfo(listInfo);
    }

    public void onAddRecipient(View view) {
        if (!mFoundInfoList.isEmpty()) {
            UserProfile profile = DatabaseHelper.UserHelper.getUserProfile(this);
            ArrayList<HashMap<String, String>> listFriends = mRESTApiManager.getFriendsList();
            HashSet<String> setFriends = new HashSet<String>();
            for (HashMap<String, String> friend : listFriends) {
                setFriends.add(friend.get(RESTApiManager.OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_USERNAME));
            }
            ArrayList<String> listAddFriends = new ArrayList<String>();
            for (RecipientInfo info : mFoundInfoList) {
                if (mAdapter.isSelected(info.mUsername)) {
                    if (!setFriends.contains(info.mUsername)) {
                        listAddFriends.add(info.mUsername);
                        HashMap<String, String> roomInfo = mRESTApiManager.createRoom(new String[] { info.mUsername, });
                        if (roomInfo != null) {
                            android.util.Log.d(TAG, "Succeed to create new space.");
                        }
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_succeed_add_recipient), Toast.LENGTH_SHORT).show();
                    } else {
                        android.util.Log.d("Olive", "Cannot insert recipient!");
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_error_already_registered), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            String[] arrAddFriends = new String[listAddFriends.size()];
            for (int i=0; i<listAddFriends.size(); i++) {
                arrAddFriends[i] = listAddFriends.get(i);
            }

            mRESTApiManager.addFriends(arrAddFriends);

            // Sync room and friends
            Intent syncIntent = null;

            syncIntent = new Intent(this, SyncNetworkService.class)
                    .setAction(SyncNetworkService.INTENT_ACTION_SYNC_ROOMS_LIST);
            startService(syncIntent);

            syncIntent = new Intent(this, SyncNetworkService.class)
                    .setAction(SyncNetworkService.INTENT_ACTION_SYNC_FRIENDS_LIST);
            startService(syncIntent);
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

    private void updateRecipientInfo(ArrayList<RecipientInfo> array) {
        mFoundInfoList.clear();
        if (array != null && !array.isEmpty()) {
            for (RecipientInfo info : array) mFoundInfoList.add(info);
            mAdapter.notifyDataSetChanged();

            mErrorView.setVisibility(View.GONE);
        } else {
            mAdapter.notifyDataSetChanged();

            // Need to add UI for notifying "not found"
            mErrorView.setVisibility(View.VISIBLE);
        }
    }

    public class MySimpleArrayAdapter extends ArrayAdapter<RecipientInfo> implements CompoundButton.OnCheckedChangeListener {
        private ArrayList<RecipientInfo> mItems;
        private int mResourceId;
        private HashMap<String, Boolean> mMapSelected = new HashMap<String, Boolean>();

        public MySimpleArrayAdapter(Context context, int textViewResourceId, ArrayList<RecipientInfo> items) {
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

            RecipientInfo info = mItems.get(position);
            if (info != null) {
                boolean bChecked = (mMapSelected.get(info.mUsername) != null) ? mMapSelected.get(info.mUsername).booleanValue() : false;

                Bitmap bmpProfile = OliveHelper.getCachedImage(info.mPicture);
                if (bmpProfile != null) {
                    Bitmap bmpCircle = OliveHelper.makeCircleBitmap(bmpProfile);
                    holder.mPhoto.setImageBitmap(bmpCircle);
                } else {
                    holder.mPhoto.setImageDrawable(getResources().getDrawable(R.drawable.ic_no_photo_login));
                }

                holder.mUsername.setText(info.mDisplayname);
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
