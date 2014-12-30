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
	
	// Member variables.
	private Context 	mContext;
	
	private ListAdapter	mAdapter;
	private View		mHeader;
	private boolean		mShowHeader = false;
	
	public RecipientsListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		
		LayoutInflater inflater = LayoutInflater.from(mContext);
		
		// Add header for empty database.
		mHeader = inflater.inflate(R.layout.recipient_item_header, this, false);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
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
		
		super.setAdapter(adapter);
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
}
