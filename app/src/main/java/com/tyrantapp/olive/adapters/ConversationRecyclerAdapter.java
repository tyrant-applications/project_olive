package com.tyrantapp.olive.adapters;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.components.ConversationRecyclerView;
import com.tyrantapp.olive.providers.OliveContentProvider.ConversationColumns;
import com.tyrantapp.olive.providers.OliveContentProvider.RecipientColumns;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class ConversationRecyclerAdapter extends CursorRecyclerAdapter<ConversationRecyclerAdapter.ConversationItemViewHolder> {
	final static private String 		TAG	= "ConversationListAdapter";

	// member variables
	private Context 		mContext;
	
	private String			mRecipientName;
    private String			mNickName;
    
    public ConversationRecyclerAdapter(Context context, long recipient_id) {
    	super(context.getContentResolver().query(
			ConversationColumns.CONTENT_URI,
			ConversationColumns.PROJECTIONS,
			ConversationColumns.RECIPIENT_ID + "=?", 
			new String[] { String.valueOf(recipient_id), }, 
			null
		));
    	mContext = context;
    	
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
        cursor.close();
    }

    @Override
    public ConversationItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(
        		viewGroup.getContext()).
                inflate(R.layout.conversation_item, 
                        viewGroup,
                        false);
        return new ConversationItemViewHolder(itemView);
    }


	@Override
	public void onBindViewHolderCursor(ConversationItemViewHolder holder, Cursor cursor) {
		if (cursor != null) {
			boolean isRecv = cursor.getInt(cursor.getColumnIndex(ConversationColumns.IS_RECV)) > 0;
			Resources res = mContext.getResources();
			if (isRecv) {
				holder.mButton.setBackground(res.getDrawable(R.drawable.bg_olive_you));
			} else {
				holder.mButton.setBackground(res.getDrawable(R.drawable.bg_olive_me));
			}
			holder.mButton.setText(cursor.getString(cursor.getColumnIndex(ConversationColumns.CTX_DETAIL)));
    	}
		
	}
	
	public Cursor getItem(int position) {
    	Cursor cursor = getCursor();
    	if (cursor != null && cursor.moveToPosition(position))
    		return cursor;
    	else 
    		return null;
    }
    
    public String getRecipientName() {
    	return mRecipientName;
    }
    
    public String getNickName() {
    	return mNickName;
    }
    

	public final static class ConversationItemViewHolder extends RecyclerView.ViewHolder {
		TextView mButton;
		
		public ConversationItemViewHolder(View itemView) {
			super(itemView);
			mButton = (TextView) itemView.findViewById(R.id.olive_button);
		}
	}
}
