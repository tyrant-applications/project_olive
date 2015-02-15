package com.tyrantapp.olive.component;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.tyrantapp.olive.component.ButtonBoardRecyclerView.OnItemClickListener;

public class ButtonBoardRecyclerView extends RecyclerView {
	private final static String TAG = ButtonBoardRecyclerView.class.getSimpleName();

    private Context             mContext;

	public ButtonBoardRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);

        mContext = context;

		initializeView(context);
	}

	public void finalize() {
	}

	public void initializeView(Context context) {
		GridLayoutManager layoutManager = new GridLayoutManager(context, 4);
		layoutManager.setOrientation(GridLayoutManager.VERTICAL);
        layoutManager.setSpanCount(4);
		setLayoutManager(layoutManager);
		setHasFixedSize(true);
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
        addOnItemTouchListener(new RecyclerItemClickListener(mContext, listener));
	}

	@Override
	public void setAdapter(Adapter adapter) {
		super.setAdapter(adapter);
	}

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
        public void onItemLongClick(View view, int position);
    }

	public static class RecyclerItemClickListener implements
            android.support.v7.widget.RecyclerView.OnItemTouchListener {

        private OnItemClickListener mListener;

		GestureDetector mGestureDetector;
        View            mChildView;
        int             mPosition;

		public RecyclerItemClickListener(Context context, OnItemClickListener listener) {
			mListener = listener;
            mChildView = null;
            mPosition = -1;
			mGestureDetector = new GestureDetector(context,
					new GestureDetector.SimpleOnGestureListener() {

                        @Override
						public boolean onSingleTapUp(MotionEvent e) {
                            mListener.onItemClick(mChildView, mPosition);
                            return super.onSingleTapUp(e);
						}

                        @Override
                        public void onLongPress(MotionEvent e) {
                            mListener.onItemLongClick(mChildView, mPosition);
                            super.onLongPress(e);
                        }
                    }
            );
		}

		@Override
		public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
			View childView = view.findChildViewUnder(e.getX(), e.getY());
			if (childView != null && mListener != null) {
                mChildView = childView;
                mPosition = view.getChildPosition(childView);
                mGestureDetector.onTouchEvent(e);
            }
			return false;
		}

		@Override
		public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
		}
	}
}