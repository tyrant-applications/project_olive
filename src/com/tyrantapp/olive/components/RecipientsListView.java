/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tyrantapp.olive.components;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.providers.OliveContentProvider.RecipientColumns;

import android.content.ContentValues;
import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug.CapturedViewProperty;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.TextView.OnEditorActionListener;


/**
 * Demonstrates expandable lists backed by a Simple Map-based adapter
 */
public class RecipientsListView extends ListView {
	final static private String TAG = "RecipientsListView";
	
	final static private boolean DEBUG = true;
	
	// Member variables.
	private Context 	mContext;
	
	private ViewFlipper	mFlipper;
	private EditText	mAddUserEdit;
	
	private boolean 	mAddMode = false;
	
	private ListAdapter	mAdapter;
	private View		mHeader;
	private boolean		mShowHeader = false;
	
	private OnClickListener mOnFooterClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			changeAddMode();
		}
	};
	
	private OnEditorActionListener mOnEditorActionListener = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				String pszTag = v.getText().toString();
				
				if (DEBUG) {
					if (!pszTag.isEmpty()) {
						ContentValues values = new ContentValues();
						values.put(RecipientColumns.USERNAME, pszTag);
						values.put(RecipientColumns.UNREAD, false);
						mContext.getContentResolver().insert(RecipientColumns.CONTENT_URI, values);

						android.util.Log.d("Olive", "Insert recipient!");
					} else {
						android.util.Log.d("Olive", "Failed Insert recipient!");
					}
				} else {
					// Check server.
				}
				
				changeNormalMode();
			}
			return false;
		}
	};
	
	public RecipientsListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		
		LayoutInflater inflater = LayoutInflater.from(mContext);
		
		// Add header for empty database.
		mHeader = inflater.inflate(R.layout.recipient_item_header, this, false);
		
		// Add + button for adding new recipient.
		View footer = inflater.inflate(R.layout.recipient_item_footer, this, false);
		addFooterView(footer);
		
		// prepare components
		mFlipper = (ViewFlipper) footer.findViewById(R.id.recipient_add_flipper);
		mAddUserEdit = (EditText) footer.findViewById(R.id.recipient_edit);
		
		// register event
		footer.setOnClickListener(mOnFooterClickListener);
		mAddUserEdit.setOnEditorActionListener(mOnEditorActionListener);	
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		
		mAdapter = adapter;
		if (mAdapter != null) {
			mAdapter.registerDataSetObserver(new DataSetObserver() {
	
				@Override
				public void onChanged() {
					if (mAdapter != null) {
						if (!mAdapter.isEmpty()) {
							hideHeader();
						} else {
							showHeader();
						}
					}
	
					super.onChanged();
				}
				
			});
			
			if (mAdapter.isEmpty()) {
				showHeader();
			}
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		boolean isActionBack = event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP;
		
		if (isAddMode() && isActionBack) {
			changeNormalMode();
			return true;
		} else {
			return super.dispatchKeyEvent(event);
		}
	}
	

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		changeNormalMode();
		return super.dispatchTouchEvent(ev);
	}
	
	public void showHeader() {
		if (!mShowHeader) {
			mShowHeader = true;
			addHeaderView(mHeader);
			
		}
	}
	
	public void hideHeader() {
		if (mShowHeader) {
			mShowHeader = false;
			removeHeaderView(mHeader);
			
		}
	}
	
	public boolean isAddMode() {
		return mAddMode;
	}
	
	public void changeAddMode() {
		if (!mAddMode) {
			mAddMode = true;
			mFlipper.showNext();
			showSoftImputMethod(mAddUserEdit);
		}
	}
	
	public void changeNormalMode() {
		if (mAddMode) {
			mAddMode = false;
			mFlipper.showPrevious();					
			hideSoftInputMethod(mAddUserEdit);
		}
	}

	public void showSoftImputMethod(final EditText focusView) {
		if (focusView != null) {
			focusView.requestFocus();
			InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(focusView, InputMethodManager.SHOW_IMPLICIT);
		}
	}
	
	public void hideSoftInputMethod(final EditText focusView) {
		if (focusView != null) {
			focusView.setText(null);
			InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
		}
	}
}
