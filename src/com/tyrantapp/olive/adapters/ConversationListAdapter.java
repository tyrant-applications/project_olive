package com.tyrantapp.olive.adapters;

import java.util.ArrayList;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.components.ConversationItem;
import com.tyrantapp.olive.components.ConversationListView;
import com.tyrantapp.olive.components.ConversationItem;
import com.tyrantapp.olive.components.RecipientItem;
import com.tyrantapp.olive.providers.OliveContentProvider.ConversationColumns;
import com.tyrantapp.olive.providers.OliveContentProvider.RecipientColumns;

import android.app.Activity;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ConversationListAdapter extends CursorAdapter {
	final static private String 		TAG	= "ConversationListAdapter";

	// member variables
    private Context 	mContext;
    private String		mRecipientName;
    private String		mNickName;
    
    public ConversationListAdapter(Context context, long recipient_id) {
        super(
        	context, 
        	context.getContentResolver().query(
				ConversationColumns.CONTENT_URI,
				ConversationColumns.PROJECTIONS,
				ConversationColumns.RECIPIENT_ID + "=?", 
				new String[] { String.valueOf(recipient_id), }, 
				null
			)
		);
        
        // for update recipient name
		Cursor cursor = context.getContentResolver().query(
				RecipientColumns.CONTENT_URI, 
				new String[] { RecipientColumns.USERNAME, RecipientColumns.NICKNAME, },
				RecipientColumns._ID + "=?", 
				new String[] { String.valueOf(recipient_id), },
				null);
		
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			mRecipientName = cursor.getString(cursor.getColumnIndex(RecipientColumns.USERNAME));
			mNickName = cursor.getString(cursor.getColumnIndex(RecipientColumns.NICKNAME));
		} else {
			mRecipientName = "Not Found";
			mNickName = "Unknown User";
		}
        
        mContext = context;
    }
    
    public ConversationListAdapter(Context context, Cursor cursor) {
    	super(context, cursor);
    	
		mContext = context;
	}

	@Override
    public void bindView(View view, Context context, Cursor cursor) {
		Button btnOlive = (Button) view.findViewById(R.id.olive_button);
        
		if (btnOlive != null) {
			btnOlive.setText(cursor.getString(cursor.getColumnIndex(ConversationColumns.CTX_DETAIL)));
        }
    }
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//abandon current focus
	    View currentFocus = ((Activity)mContext).getCurrentFocus();
	    if (currentFocus != null) {
	        currentFocus.clearFocus();
	    }
	    
		return super.getView(position, convertView, parent);
	}

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.conversation_item, parent, false);
        return view;
    }

    public String getRecipientName() {
    	return mRecipientName;
    }
    
    public String getNickName() {
    	return mNickName;
    }
}
