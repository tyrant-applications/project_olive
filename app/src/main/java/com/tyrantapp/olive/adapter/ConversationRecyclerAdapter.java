package com.tyrantapp.olive.adapter;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.helper.DatabaseHelper;
import com.tyrantapp.olive.provider.OliveContentProvider.ConversationColumns;
import com.tyrantapp.olive.type.UserProfile;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ConversationRecyclerAdapter extends CursorRecyclerAdapter<ConversationRecyclerAdapter.ConversationItemViewHolder> {
	final static private String 		TAG	= "ConversationListAdapter";

	// member variables
	private Context 		mContext;
	
    public ConversationRecyclerAdapter(Context context, long spaceId) {
    	super(DatabaseHelper.ConversationHelper.getCursor(context, spaceId));
    	mContext = context;
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
            UserProfile profile = DatabaseHelper.UserHelper.getUserProfile(mContext);
			boolean isRecv = !profile.mUsername.equals(cursor.getString(cursor.getColumnIndex(ConversationColumns.SENDER)));
			Resources res = mContext.getResources();
			if (isRecv) {
				holder.mButton.setBackground(res.getDrawable(R.drawable.bg_olive_you));
			} else {
				holder.mButton.setBackground(res.getDrawable(R.drawable.bg_olive_me));
			}
			holder.mButton.setText(cursor.getString(cursor.getColumnIndex(ConversationColumns.CONTEXT)));
    	}
		
	}
	
	public Cursor getItem(int position) {
    	Cursor cursor = getCursor();
    	if (cursor != null && cursor.moveToPosition(position))
    		return cursor;
    	else 
    		return null;
    }
    

	public final static class ConversationItemViewHolder extends RecyclerView.ViewHolder {
		TextView mButton;
		
		public ConversationItemViewHolder(View itemView) {
			super(itemView);
			mButton = (TextView) itemView.findViewById(R.id.olive_button);
		}
	}
}
