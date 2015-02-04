package com.tyrantapp.olive.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.tyrantapp.olive.fragment.KeypadFragment;
import com.tyrantapp.olive.listener.OnOliveKeypadListener;

public class KeypadPagerAdapter extends FragmentPagerAdapter {
	private OnOliveKeypadListener mKeypadListener;
	private int[] mPageTypes = new int[] { KeypadFragment.TYPE_KEYPAD_12, };
	
	public KeypadPagerAdapter(FragmentManager fm, OnOliveKeypadListener listener, int[] types) {
		super(fm);

		mKeypadListener = listener;
		KeypadFragment.setOnOliveKeypadListener(mKeypadListener);
		
		mPageTypes = types;
	}

	@Override
	public Fragment getItem(int position) {
		// getItem is called to instantiate the fragment for the given page.
		// Return a PlaceholderFragment (defined as a static inner class
		// below)
		
		return KeypadFragment.newInstance(position, mPageTypes[position]);
	}

	@Override
	public int getCount() {
		return mPageTypes.length;
	}
}
