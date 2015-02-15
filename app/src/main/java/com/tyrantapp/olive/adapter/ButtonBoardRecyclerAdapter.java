package com.tyrantapp.olive.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.helper.DatabaseHelper;
import com.tyrantapp.olive.helper.OliveHelper;
import com.tyrantapp.olive.provider.OliveContentProvider;
import com.tyrantapp.olive.type.UserProfile;

public class ButtonBoardRecyclerAdapter extends CursorRecyclerAdapter<ButtonBoardRecyclerAdapter.ButtonBoardItemViewHolder> {
	final static private String 		TAG	= ButtonBoardRecyclerAdapter.class.getSimpleName();

	// member variables
	private Context 		mContext;

    public ButtonBoardRecyclerAdapter(Context context, int sectionNumber) {
    	super(DatabaseHelper.PresetButtonHelper.getSectionCursor(context, sectionNumber));
    	mContext = context;
    }

    @Override
    public ButtonBoardItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(
        		viewGroup.getContext()).
                inflate(R.layout.boardbutton_item,
                        viewGroup,
                        false);
        return new ButtonBoardItemViewHolder(itemView);
    }


	@Override
	public void onBindViewHolderCursor(ButtonBoardItemViewHolder holder, Cursor cursor) {
		if (cursor != null) {
            String author = cursor.getString(cursor.getColumnIndex(OliveContentProvider.PresetButtonColumns.AUTHOR));
            String mimetype = cursor.getString(cursor.getColumnIndex(OliveContentProvider.PresetButtonColumns.MIMETYPE));
            Long idExtra = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.PresetButtonColumns.EXTRA_ID));
            String context = cursor.getString(cursor.getColumnIndex(OliveContentProvider.PresetButtonColumns.CONTEXT));

            boolean bShowImage = true;
            if (OliveHelper.MIMETYPE_TEXT.equals(mimetype)) {
                holder.mButtonText.setText(context);
                bShowImage = false;
            } else
            if (OliveHelper.MIMETYPE_IMAGE.equals(mimetype)) {
            } else
            if (OliveHelper.MIMETYPE_VIDEO.equals(mimetype)) {
            } else
            if (OliveHelper.MIMETYPE_AUDIO.equals(mimetype)) {
            } else
            if (OliveHelper.MIMETYPE_GEOLOCATE.equals(mimetype)) {
            } else
            if (OliveHelper.MIMETYPE_EMOJI.equals(mimetype)) {
                //holder.mButtonImage.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), ));
            }

            if (bShowImage) {
                holder.mButtonText.setVisibility(View.GONE);
                holder.mButtonImage.setVisibility(View.VISIBLE);
            } else {
                holder.mButtonText.setVisibility(View.VISIBLE);
                holder.mButtonImage.setVisibility(View.GONE);
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

	public final static class ButtonBoardItemViewHolder extends RecyclerView.ViewHolder {
		TextView mButtonText;
        ImageView mButtonImage;
		
		public ButtonBoardItemViewHolder(View itemView) {
			super(itemView);
			mButtonText = (TextView) itemView.findViewById(R.id.button_text);
            mButtonImage = (ImageView) itemView.findViewById(R.id.button_image);
		}
    }
}
