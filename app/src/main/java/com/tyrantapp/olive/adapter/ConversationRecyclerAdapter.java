package com.tyrantapp.olive.adapter;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.helper.DatabaseHelper;
import com.tyrantapp.olive.helper.OliveHelper;
import com.tyrantapp.olive.provider.OliveContentProvider.ConversationColumns;
import com.tyrantapp.olive.type.UserProfile;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
				holder.mButtonHolder.setBackground(res.getDrawable(R.drawable.bg_olive_you));
			} else {
				holder.mButtonHolder.setBackground(res.getDrawable(R.drawable.bg_olive_me));
			}
            String mimetype = cursor.getString(cursor.getColumnIndex(ConversationColumns.MIMETYPE));
            String content = cursor.getString(cursor.getColumnIndex(ConversationColumns.CONTEXT));
            Bitmap bmpImage = null;
            switch (OliveHelper.convertMimetype(mimetype)) {
                case 0: //MIMETYPE_TEXT:
                    holder.mButtonText.setText(content);
                    holder.mButtonText.setVisibility(View.VISIBLE);
                    holder.mButtonImage.setVisibility(View.GONE);
                    break;
                case 1: //MIMETYPE_IMAGE:
                    bmpImage = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_image_filter_black_48dp);//OliveHelper.getCachedImage(content, 1);
                    holder.mButtonImage.setImageBitmap(bmpImage);
                    holder.mButtonText.setVisibility(View.GONE);
                    holder.mButtonImage.setVisibility(View.VISIBLE);
                    break;
                case 2: //MIMETYPE_VIDEO:
                    bmpImage = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_video_black_48dp);
                    holder.mButtonImage.setImageBitmap(bmpImage);
                    holder.mButtonText.setVisibility(View.GONE);
                    holder.mButtonImage.setVisibility(View.VISIBLE);
                    break;
                case 3: //MIMETYPE_AUDIO:
                    bmpImage = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_microphone_outline_black_48dp);
                    holder.mButtonImage.setImageBitmap(bmpImage);
                    holder.mButtonText.setVisibility(View.GONE);
                    holder.mButtonImage.setVisibility(View.VISIBLE);
                    break;
                case 4: //MIMETYPE_GEOLOCATE:
                    bmpImage = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_map_marker_black_48dp);
                    holder.mButtonImage.setImageBitmap(bmpImage);
                    holder.mButtonText.setVisibility(View.GONE);
                    holder.mButtonImage.setVisibility(View.VISIBLE);
                    break;
                case 5: //MIMETYPE_EMOJI:
                    bmpImage = OliveHelper.getCachedImage(content, 1);
                    holder.mButtonImage.setImageBitmap(bmpImage);
                    holder.mButtonText.setVisibility(View.GONE);
                    holder.mButtonImage.setVisibility(View.VISIBLE);
                    break;
            }
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
        RelativeLayout mButtonHolder;
        TextView mButtonText;
        ImageView mButtonImage;
		
		public ConversationItemViewHolder(View itemView) {
			super(itemView);
            mButtonHolder = (RelativeLayout) itemView.findViewById(R.id.conversation_interface);
            mButtonText = (TextView) itemView.findViewById(R.id.conversation_text);
            mButtonImage = (ImageView) itemView.findViewById(R.id.conversation_image);
            mButtonImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		}
	}
}
