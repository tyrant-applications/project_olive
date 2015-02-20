package com.tyrantapp.olive.adapter;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.helper.DatabaseHelper;
import com.tyrantapp.olive.helper.OliveHelper;
import com.tyrantapp.olive.provider.OliveContentProvider.ChatSpaceColumns;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.CursorAdapter;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
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
            DatabaseHelper.ChatSpaceHelper.getCursor(context)
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
            String title = cursor.getString(cursor.getColumnIndex(ChatSpaceColumns.DISPLAYNAME));
            if (title == null) title = cursor.getString(cursor.getColumnIndex(ChatSpaceColumns.TITLE));
        	nameView.setText(title);
        	
        	long lLastReceived = cursor.getLong(cursor.getColumnIndex(ChatSpaceColumns.LAST_UPDATED));

        	if (lLastReceived > 0) {
        		timeView.setVisibility(View.VISIBLE);
        		String prettyTime = DateUtils.getRelativeTimeSpanString(lLastReceived).toString();
        		timeView.setText(prettyTime);
        	} else {
        		timeView.setVisibility(View.INVISIBLE);
        	}
        	
        	int nUnreadCount = cursor.getInt(cursor.getColumnIndex(ChatSpaceColumns.UNREAD));
        	if (nUnreadCount > 0) {
        		unreadView.setText(String.valueOf(nUnreadCount));
        		unreadView.setVisibility(View.VISIBLE);
        		pltView.setBackgroundColor(mContext.getResources().getColor(R.color.oliveblue));
        	} else {
        		unreadView.setVisibility(View.INVISIBLE);
        		pltView.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        	}

            // Portrait
            String path = cursor.getString(cursor.getColumnIndex(ChatSpaceColumns.PICTURE));
            Bitmap bmpProfile = OliveHelper.getCachedImage(path);
            if (bmpProfile != null) {
                Bitmap bmpCircle = OliveHelper.makeCircleBitmap(bmpProfile);
                picView.setImageBitmap(bmpCircle);
            } else {
                picView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_no_photo_login));
            }
        	
        	// favorite (starred)
        	final long spaceId = cursor.getLong(cursor.getColumnIndex(ChatSpaceColumns._ID));
        	final boolean bStarred = cursor.getInt(cursor.getColumnIndex(ChatSpaceColumns.STARRED)) > 0;
        	
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
        	favView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (!bStarred) {
                        android.util.Log.d(TAG, "Starred to ON");
                        DatabaseHelper.SpaceHelper.setStarred(mContext, spaceId, true);
                    } else {
                        android.util.Log.d(TAG, "Starred to OFF");
                        DatabaseHelper.SpaceHelper.setStarred(mContext, spaceId, false);
                    }
                    return false;
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
