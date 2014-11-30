package com.tyrantapp.olive.fragments;

import java.util.HashMap;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.interfaces.OnOliveKeypadListener;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment
 * must implement the {@link KeypadFragment.OnFragmentInteractionListener}
 * interface to handle interaction events. Use the
 * {@link KeypadFragment#newInstance} factory method to create an instance of
 * this fragment.
 * 
 */
@SuppressLint("UseSparseArrays")
public class KeypadFragment extends Fragment {
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private static final String 				ARG_SECTION_NUMBER = "section_number";
	private static final String 				ARG_SECTION_TYPE = "section_type";
	
	public static final int						TYPE_KEYPAD_12 = 0;
	public static final int						TYPE_KEYPAD_2  = 1;
	
	
	private static HashMap<Integer, Fragment>	mFragmentsMap = new HashMap<Integer, Fragment>();
	private static OnOliveKeypadListener		mOliveKeypadListener;
		
	
	// member variables.
	private int				mSectionNumber;
	private int				mSectionType;
	private Button[]		mOliveButtons = new Button[12];
			
	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static KeypadFragment getInstance(int sectionNumber, int type) {
		KeypadFragment fragment = null;
		
		if (mFragmentsMap.containsKey(sectionNumber)) {
			fragment = (KeypadFragment) mFragmentsMap.get(sectionNumber);
		} else {
			fragment = new KeypadFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			args.putInt(ARG_SECTION_TYPE, type);
			fragment.setArguments(args);			
			
			mFragmentsMap.put(sectionNumber, fragment);
		}
		
		return fragment;
	}		

	public static Fragment getFragment(int sectionNumber) {
		return mFragmentsMap.get(sectionNumber);			
	}		

	public static void setOnOliveKeypadListener(OnOliveKeypadListener listener) {
		mOliveKeypadListener = listener;
	}

	public KeypadFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = null;
		
		mSectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
		mSectionType = getArguments().getInt(ARG_SECTION_TYPE);
		
		if (mSectionType == TYPE_KEYPAD_2) {
			rootView = inflater.inflate(R.layout.fragment_keypad_2, container, false);
			
			mOliveButtons[0]  = (Button)rootView.findViewById(R.id.olive_1a);
			mOliveButtons[1]  = (Button)rootView.findViewById(R.id.olive_1b);
		} else {
			rootView = inflater.inflate(R.layout.fragment_keypad_12, container, false);
			
			mOliveButtons[0]  = (Button)rootView.findViewById(R.id.olive_1a);
			mOliveButtons[1]  = (Button)rootView.findViewById(R.id.olive_1b);
			mOliveButtons[2]  = (Button)rootView.findViewById(R.id.olive_1c);
			mOliveButtons[3]  = (Button)rootView.findViewById(R.id.olive_1d);
			mOliveButtons[4]  = (Button)rootView.findViewById(R.id.olive_2a);
			mOliveButtons[5]  = (Button)rootView.findViewById(R.id.olive_2b);
			mOliveButtons[6]  = (Button)rootView.findViewById(R.id.olive_2c);
			mOliveButtons[7]  = (Button)rootView.findViewById(R.id.olive_2d);
			mOliveButtons[8]  = (Button)rootView.findViewById(R.id.olive_3a);
			mOliveButtons[9]  = (Button)rootView.findViewById(R.id.olive_3b);
			mOliveButtons[10] = (Button)rootView.findViewById(R.id.olive_3c);
			mOliveButtons[11] = (Button)rootView.findViewById(R.id.olive_3d);
		}
			
					
		if (mOliveKeypadListener != null) {
			for (int idx = 0; idx < 12; idx++) {
				if (mOliveButtons[idx] != null) mOliveButtons[idx].setOnClickListener(new OnKeypadClickListener(mOliveKeypadListener, mSectionNumber, idx));
			}
			
			mOliveKeypadListener.onKeypadCreate(mSectionNumber);
		}			
		
		return rootView;
	}
	
	public View getOliveButton(int idx) {
		return (View)mOliveButtons[idx];
	}
	
	class OnKeypadClickListener implements OnClickListener {
		private OnOliveKeypadListener 	mKeypadListener;
		
		private int						mSectionNumber;
		private int 					mIndexNumber;
		
		public OnKeypadClickListener(OnOliveKeypadListener listener, int sectionNumber, int index) {
			mKeypadListener = listener;
			mSectionNumber = sectionNumber;
			mIndexNumber = index;
		}

		@Override
		public void onClick(View view) {
			if (mKeypadListener != null) {
				mKeypadListener.onKeypadClick(mSectionNumber, mIndexNumber);
			}
		}			
	}
}
