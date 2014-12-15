package com.tyrantapp.olive.components;

import com.tyrantapp.olive.adapters.ConversationRecyclerAdapter;
import com.tyrantapp.olive.components.ConversationRecyclerView.RecyclerItemClickListener.OnItemClickListener;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class ConversationRecyclerView extends RecyclerView {
	private final static String TAG = "ConversationRecyclerView";
	
	private Context		mContext;
	
	private AdapterDataObserver	mObserver = new AdapterDataObserver() {
		@Override
		public void onChanged() {
			super.onChanged();
			
			Cursor cursor = ((ConversationRecyclerAdapter)getAdapter()).getCursor();
            if (cursor.getCount() > 0) {
                smoothScrollToPosition(cursor.getCount() - 1);
            }
		}
	};
	
	public ConversationRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mContext = context;

		initializeView(context);
	}
	
	public void finalize() {
		getAdapter().unregisterAdapterDataObserver(mObserver);
	}
	
	public void initializeView(Context context) {
		LinearLayoutManager layoutManager = new LinearLayoutManager(context);
		layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		layoutManager.setStackFromEnd(true);
		setLayoutManager(layoutManager);
		setHasFixedSize(true);
	}	
	
	public void setOnItemClickListener(OnItemClickListener listener) {
		addOnItemTouchListener(
		    new RecyclerItemClickListener(mContext, listener)
		);
	}
	
	@Override
	public void setAdapter(Adapter adapter) {
		super.setAdapter(adapter);

		adapter.registerAdapterDataObserver(mObserver);
	}

	public static class RecyclerItemClickListener implements
			android.support.v7.widget.RecyclerView.OnItemTouchListener {
		private OnItemClickListener mListener;

		public interface OnItemClickListener {
			public void onItemClick(View view, int position);
		}

		GestureDetector mGestureDetector;

		public RecyclerItemClickListener(Context context,
				OnItemClickListener listener) {
			mListener = listener;
			mGestureDetector = new GestureDetector(context,
					new GestureDetector.SimpleOnGestureListener() {
						@Override
						public boolean onSingleTapUp(MotionEvent e) {
							return true;
						}
					});
		}

		@Override
		public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
			View childView = view.findChildViewUnder(e.getX(), e.getY());
			if (childView != null && mListener != null
					&& mGestureDetector.onTouchEvent(e)) {
				mListener.onItemClick(childView,
						view.getChildPosition(childView));
			}
			return false;
		}

		@Override
		public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
		}
	}
}