package com.tyrantapp.olive.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.tyrantapp.olive.fragments.KeypadFragment;
import com.tyrantapp.olive.interfaces.OnOliveKeypadListener;

public class KeypadPagerAdapter extends FragmentPagerAdapter {
	private OnOliveKeypadListener mKeypadListener;
	
	public KeypadPagerAdapter(FragmentManager fm, OnOliveKeypadListener listener) {
		super(fm);

		mKeypadListener = listener;
		KeypadFragment.setOnOliveKeypadListener(mKeypadListener);
	}

	@Override
	public Fragment getItem(int position) {
		// getItem is called to instantiate the fragment for the given page.
		// Return a PlaceholderFragment (defined as a static inner class
		// below)
		
		return KeypadFragment.getInstance(position);
	}

	@Override
	public int getCount() {
		return 1;
	}
}
