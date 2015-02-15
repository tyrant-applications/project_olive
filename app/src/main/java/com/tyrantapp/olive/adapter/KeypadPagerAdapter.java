package com.tyrantapp.olive.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.tyrantapp.olive.fragment.KeypadFragment;
import com.tyrantapp.olive.helper.DatabaseHelper;
import com.tyrantapp.olive.listener.OnOliveKeypadListener;

public class KeypadPagerAdapter extends FragmentPagerAdapter {
    private final static String TAG = KeypadPagerAdapter.class.getSimpleName();

	private OnOliveKeypadListener mKeypadListener;
    private Context mContext;

	public KeypadPagerAdapter(FragmentManager fm, Context context, OnOliveKeypadListener listener) {
		super(fm);

        mContext = context;
		mKeypadListener = listener;
		KeypadFragment.setOnOliveKeypadListener(mKeypadListener);
	}

	@Override
	public Fragment getItem(int position) {
		// getItem is called to instantiate the fragment for the given page.
		// Return a PlaceholderFragment (defined as a static inner class
		// below)

		return KeypadFragment.newInstance(mContext, position);
	}

	@Override
	public int getCount() {
		return DatabaseHelper.PresetButtonHelper.getNumberOfSections(mContext);
	}
}
