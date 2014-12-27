package com.tyrantapp.olive;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.tyrantapp.olive.helper.RESTHelper;
import com.tyrantapp.olive.providers.OliveContentProvider;
import com.tyrantapp.olive.types.UserInfo;

import java.util.ArrayList;
import java.util.HashMap;


public class AddRecipientActivity extends BaseActivity {
    private final static String TAG = "AddRecipientActivity";

    //private UserInfo mFoundInfo = null;
    private ListView mListView;
    private MySimpleArrayAdapter mAdapter;
    private ArrayList<UserInfo> mFoundInfoList = new ArrayList<UserInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipient);

        mAdapter = new MySimpleArrayAdapter(this, R.layout.found_friend_item, mFoundInfoList);
        mListView = (ListView) findViewById(R.id.found_listview);
        mListView.setAdapter(mAdapter);
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

        mFoundInfoList.clear();
        updateRecipientInfo(info);
    }

    public void onSearchID(View view) {
        String searchId = ((EditText)findViewById(R.id.edit_searchId)).getText().toString();
        UserInfo info = mRESTHelper.getRecipientProfile(searchId, null);

        mFoundInfoList.clear();
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
                }
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
            mFoundInfoList.add(info);
            mAdapter.notifyDataSetChanged();
        } else {
            mFoundInfoList.clear();
            // Need to add UI for notifying "not found"
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
                holder.mChecked = (CheckBox)v.findViewById(R.id.check_should_add);
                v.setTag(holder);
            } else {
                holder = (MySimpleViewHolder)v.getTag();
            }

            UserInfo info = mItems.get(position);
            if (info != null) {
                holder.mUsername.setText(info.mUsername);
                holder.mChecked.setTag(info.mUsername);
                holder.mChecked.setOnCheckedChangeListener(this);
            }
            return v;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mMapSelected.put((String)buttonView.getTag(), isChecked);
        }

        public boolean isSelected(String username) {
            return mMapSelected.get(username);
        }
    }

    private static class MySimpleViewHolder {
        public ImageView mPhoto;
        public TextView mUsername;
        public CheckBox mChecked;
    }
}
