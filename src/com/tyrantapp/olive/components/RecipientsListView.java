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

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ExpandableListView;


/**
 * Demonstrates expandable lists backed by a Simple Map-based adapter
 */
public class RecipientsListView extends ExpandableListView {
	
	private boolean	mScrollable = true;
    private float	mPositionY;

	public RecipientsListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void SetScrollable(boolean scrollable) {
		mScrollable = scrollable;
	}
	
	public boolean IsScrollable() {
		return mScrollable;
	}
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
    	if (!mScrollable) {
    		/*
	        final int actionMasked = ev.getActionMasked() & MotionEvent.ACTION_MASK;
	 
	        if (actionMasked == MotionEvent.ACTION_DOWN) {
	            // Record the position the list the touch landed on
	            mPosition = pointToPosition((int) ev.getX(), (int) ev.getY());
	            return super.dispatchTouchEvent(ev);
	        }
	 
	        if (actionMasked == MotionEvent.ACTION_MOVE) {
	            // Ignore move events
	            return true;
	        }
	 
	        if (actionMasked == MotionEvent.ACTION_UP) {
	            // Check if we are still within the same view
	            if (pointToPosition((int) ev.getX(), (int) ev.getY()) == mPosition) {
	                super.dispatchTouchEvent(ev);
	            } else {
	                // Clear pressed state, cancel the action
	                setPressed(false);
	                invalidate();
	                return true;
	            }
	        }
	        */
	        final int actionMasked = ev.getActionMasked() & MotionEvent.ACTION_MASK;
	   	 
	        if (actionMasked == MotionEvent.ACTION_DOWN) {
	            // Record the position the list the touch landed on
	        	mPositionY = ev.getY();
	            return super.dispatchTouchEvent(ev);
	        }
	 
	        if (actionMasked == MotionEvent.ACTION_MOVE) {
	            ev.setLocation(ev.getX(), mPositionY);
	            return super.dispatchTouchEvent(ev);
	        }
	 
	        if (actionMasked == MotionEvent.ACTION_UP) {
	        	ev.setLocation(ev.getX(), mPositionY);
	            return super.dispatchTouchEvent(ev);
	        }
    	}
	    return super.dispatchTouchEvent(ev);
    }
}
