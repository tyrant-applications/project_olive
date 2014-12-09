package com.tyrantapp.olive.adapters;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.providers.OliveContentProvider.ConversationColumns;
import com.tyrantapp.olive.providers.OliveContentProvider.RecipientColumns;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
		LinearLayout pltView = (LinearLayout) view.findViewById(R.id.recipient_plate);
		RelativeLayout favView = (RelativeLayout) view.findViewById(R.id.recipient_favorite);
		ImageView picView = (ImageView) view.findViewById(R.id.recipient_pic);
		ImageView ovrView = (ImageView) view.findViewById(R.id.recipient_overlay);
    	TextView unreadView = (TextView) view.findViewById(R.id.recipient_unread);
        TextView nameView = (TextView) view.findViewById(R.id.recipient_name);
        TextView timeView = (TextView) view.findViewById(R.id.recipient_last_recv);
        
        if (nameView != null) {
        	nameView.setText(cursor.getString(cursor.getColumnIndex(RecipientColumns.USERNAME)));
        	
        	long lLastReceived = -1;
        	Cursor convCursor = context.getContentResolver().query(
        			ConversationColumns.CONTENT_URI, 
        			new String[] { ConversationColumns.CREATED, },
        			ConversationColumns.RECIPIENT_ID + "=?",
        			new String[] { String.valueOf(cursor.getLong(cursor.getColumnIndex(RecipientColumns._ID))), },
        			ConversationColumns.CREATED + " DESC Limit 1");
        	if (convCursor != null && convCursor.getCount() > 0) {
        		convCursor.moveToFirst();
        		lLastReceived = convCursor.getLong(0);
        	}
        	
        	if (lLastReceived >= 0) {
        		timeView.setVisibility(View.VISIBLE);
        		String prettyTime = DateUtils.getRelativeTimeSpanString(lLastReceived).toString();
        		timeView.setText(prettyTime);
        	} else {
        		timeView.setVisibility(View.INVISIBLE);
        	}
        	
        	int nUnreadCount = cursor.getInt(cursor.getColumnIndex(RecipientColumns.UNREAD));
        	if (nUnreadCount > 0) {
        		unreadView.setText(String.valueOf(nUnreadCount));
        		unreadView.setVisibility(View.VISIBLE);
        		pltView.setBackgroundColor(mContext.getResources().getColor(R.color.oliveblue));
        	} else {
        		unreadView.setVisibility(View.INVISIBLE);
        		pltView.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        	}
        	
        	// favorite (starred)
        	final long recipientId = cursor.getLong(cursor.getColumnIndex(RecipientColumns._ID));
        	final boolean bStarred = cursor.getInt(cursor.getColumnIndex(RecipientColumns.STARRED)) > 0;
        	
			if (bStarred) {
				ovrView.setBackground(mContext.getResources().getDrawable(R.drawable.bg_badge_favorite));
				if (nUnreadCount > 0) {
					nameView.setTextColor(mContext.getResources().getColor(R.color.white));					
				} else {
					nameView.setTextColor(mContext.getResources().getColor(R.color.oliveblue));
				}				
			} else {
				ovrView.setBackground(mContext.getResources().getDrawable(R.drawable.bg_badge_normal));
				if (nUnreadCount > 0) {
					nameView.setTextColor(mContext.getResources().getColor(R.color.white));					
				} else {
					nameView.setTextColor(mContext.getResources().getColor(R.color.black));
				}
			}
			nameView.setSelected(true);
        	        	
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
