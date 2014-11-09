package com.tyrantapp.olive.adapters;

import java.util.ArrayList;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.components.ConversationItem;
import com.tyrantapp.olive.components.ConversationListView;
import com.tyrantapp.olive.components.RecipientItem;
import com.tyrantapp.olive.providers.OliveContentProvider.ConversationColumns;
import com.tyrantapp.olive.providers.OliveContentProvider.RecipientColumns;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;

public class RecipientsListAdapter extends CursorAdapter {
	final static private String TAG	= "RecipientListAdapter";
	
	// member variables
	private Context				mContext;
	
	public RecipientsListAdapter(Context context) {
        super(
        	context, 
        	context.getContentResolver().query(
				RecipientColumns.CONTENT_URI,
				RecipientColumns.PROJECTIONS,
				null, null, 
				RecipientColumns.ORDERBY
			)
		);
        
        mContext = context;
    }
	
    public RecipientsListAdapter(Context context, Cursor cursor) {
    	super(context, cursor);
    	
    	mContext = context;
    }

	@Override
    public void bindView(View view, Context context, Cursor cursor) {
		RelativeLayout favView = (RelativeLayout) view.findViewById(R.id.recipient_favorite);
		ImageView picView = (ImageView) view.findViewById(R.id.recipient_pic);
    	TextView unreadView = (TextView) view.findViewById(R.id.recipient_unread);
        TextView nameView = (TextView) view.findViewById(R.id.recipient_name);
        
        if (nameView != null) {
        	nameView.setText(cursor.getString(cursor.getColumnIndex(RecipientColumns.USERNAME)));
        	unreadView.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(RecipientColumns.UNREAD))));
        	
        	// favorite (starred)
        	final long recipientId = cursor.getLong(cursor.getColumnIndex(RecipientColumns._ID));
        	final boolean bStarred = cursor.getInt(cursor.getColumnIndex(RecipientColumns.STARRED)) > 0;
        	
			if (bStarred) {
				favView.setBackground(mContext.getResources().getDrawable(R.drawable.bg_badge_favorite));
			} else {
				favView.setBackground(null);
			}
        	        	
        	final Cursor finalCursor = cursor;
        	favView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					ContentValues values = new ContentValues();
					
					if (!bStarred) {
						android.util.Log.d(TAG, "Starred to ON");
						values.put(RecipientColumns.STARRED, true);
					} else {
						android.util.Log.d(TAG, "Starred to OFF");
						values.put(RecipientColumns.STARRED, false);
					}
					
					mContext.getContentResolver().update(
							Uri.withAppendedPath(RecipientColumns.CONTENT_URI, String.valueOf(recipientId)),
							values,
							null, null);
				}
        	});
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
        View view = inflater.inflate(R.layout.recipient_item, parent, false);
        return view;
    }
}
