package com.tyrantapp.olive.adapter;

import java.util.ArrayList;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.components.ConversationItem;
import com.tyrantapp.olive.components.ConversationListView;
import com.tyrantapp.olive.components.RecipientItem;
import com.tyrantapp.olive.provider.OliveContentProvider.ConversationColumns;
import com.tyrantapp.olive.provider.OliveContentProvider.RecipientColumns;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;

public class RecipientsListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private Cursor mCursor;
    
    
    private ContentObserver mObserver = null;
    
    private class UpdateHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			notifyDataSetChanged();
		}
    };
    
    private UpdateHandler mUpdateHandler = new UpdateHandler();
    
    public RecipientsListAdapter(Context context, ArrayList<RecipientItem> groups) {
        mContext = context;
        mCursor = context.getContentResolver().query(RecipientColumns.CONTENT_URI, new String[] {RecipientColumns._ID, RecipientColumns.USERNAME, RecipientColumns.UNREAD}, null, null, null);
        
        mObserver = new ContentObserver(null) {
        	@Override public void onChange(boolean self){
        		mCursor.requery();
        		mUpdateHandler.sendEmptyMessage(0);
        		android.util.Log.d("Olive", "Recipient Changed! = " + self);
        	}
        };
        
        context.getContentResolver().registerContentObserver(RecipientColumns.CONTENT_URI, true, mObserver);
    }    

    @Override
    public Object getChild(int groupPosition, int childPosition) {
    	if (mCursor == null) return null;
    	mCursor.moveToPosition(groupPosition);
    	int nRecipientID = mCursor.getInt(mCursor.getColumnIndex(RecipientColumns._ID));
    	return mContext.getContentResolver().query(
    			ConversationColumns.CONTENT_URI, 
    			new String[] {ConversationColumns._ID, ConversationColumns.RECIPIENT, ConversationColumns.CTX_DETAIL, ConversationColumns.DATE}, 
    			ConversationColumns.RECIPIENT + "=" + String.valueOf(nRecipientID), 
    			null, 
    			null);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.conversation_item, null);
        }
        ConversationListView lv = (ConversationListView) convertView.findViewById(R.id.conversation_list);
        
//        android.view.ViewGroup.LayoutParams childParams = lv.getLayoutParams();
//        childParams.height = LayoutParams.MATCH_PARENT;
//        lv.setLayoutParams(childParams);

        lv.setAdapter(new ConversationListAdapter(mContext, (Cursor)getChild(groupPosition, childPosition)));
        
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
    	return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
    	if (mCursor == null) return null;
    	
    	if (mCursor.getCount() <= groupPosition) {
    		return null;
    	} else {
    		mCursor.moveToPosition(groupPosition);
        	return mCursor;
    	}
    }

    @Override
    public int getGroupCount() {
    	if (mCursor == null) return 1;
    	
    	return mCursor.getCount() + 1;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.recipient_item, null);
        }
        TextView tv = (TextView) convertView.findViewById(R.id.recipient_name);

    	//RecipientItem group = (RecipientItem) getGroup(groupPosition);
    	Cursor cursor = (Cursor)getGroup(groupPosition);
    	if (cursor != null) {
	    	String recipientName = cursor.getString(cursor.getColumnIndex(RecipientColumns.USERNAME));
	        tv.setText(recipientName);
    	} else {
    		tv.setText("+");
    	}
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
    
}
