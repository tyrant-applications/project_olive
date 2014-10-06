package com.tyrantapp.olive.adapter;

import java.util.ArrayList;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.components.ConversationItem;
import com.tyrantapp.olive.components.ConversationListView;
import com.tyrantapp.olive.components.ConversationItem;
import com.tyrantapp.olive.components.RecipientItem;
import com.tyrantapp.olive.provider.OliveContentProvider.ConversationColumns;
import com.tyrantapp.olive.provider.OliveContentProvider.RecipientColumns;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ConversationListAdapter extends BaseAdapter {

    private Context 						mContext;
    private Cursor							mCursor;

    
    private ContentObserver mObserver = null;
    
    private class UpdateHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			notifyDataSetChanged();
		}
    };
    
    private UpdateHandler mUpdateHandler = new UpdateHandler();

    public ConversationListAdapter(Context context, ArrayList<ConversationItem> groups) {
        mContext = context;
    }
    
    public ConversationListAdapter(Context context, Cursor cursor) {
		mContext = context;
		mCursor = cursor;
		
        mObserver = new ContentObserver(null) {
        	@Override public void onChange(boolean self){
        		mCursor.requery();
        		mUpdateHandler.sendEmptyMessage(0);
        		
        		//android.util.Log.d("Olive", "Conversation Changed! = " + self);
        	}
        };
        
        context.getContentResolver().registerContentObserver(ConversationColumns.CONTENT_URI, true, mObserver);
        
		mCursor.moveToFirst();
		for (int j=0; j<mCursor.getCount(); j++) {
			int nChildID = mCursor.getInt(mCursor.getColumnIndex(ConversationColumns._ID));
			int nParentID = mCursor.getInt(mCursor.getColumnIndex(ConversationColumns.RECIPIENT));
			String pszText = mCursor.getString(mCursor.getColumnIndex(ConversationColumns.CTX_DETAIL));
			
			//android.util.Log.d("Olive", ">>> [" + nParentID + " : " + nChildID + "] " + pszText);
			
			mCursor.moveToNext();
		}
	}

	@Override
    public int getCount() {
		//android.util.Log.d("Olive", "Conversation getCount = " + mCursor.getCount());
		return mCursor.getCount();
        //return mItems.size();
    }

    @Override
    public Object getItem(int position) {
    	mCursor.moveToPosition(position);
    	return mCursor;
    }

    @Override
    public long getItemId(int position) {
        return (long)mCursor.getInt(mCursor.getColumnIndex(ConversationColumns._ID));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	//android.util.Log.d("Olive", "getView::Coversation = " + position);
    	
    	//ConversationItem item = mItems.get(position);
    	if (convertView == null) {
        	LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);// LayoutInflater.from(parent.getContext());//
            convertView = inflater.inflate(R.layout.olive_item, null);
        }
        
        if (mCursor.moveToPosition(position)) {
        	Button btn = (Button) convertView.findViewById(R.id.olive_button);
			//android.util.Log.d("Olive", "View Item Text = " + mCursor.getString(mCursor.getColumnIndex(ConversationColumns.CTX_DETAIL)));
	        btn.setText(mCursor.getString(mCursor.getColumnIndex(ConversationColumns.CTX_DETAIL)));
        }
        return convertView;
    }

}
