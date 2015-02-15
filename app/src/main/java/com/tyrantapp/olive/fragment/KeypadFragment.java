package com.tyrantapp.olive.fragment;

import java.util.HashMap;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.adapter.ButtonBoardRecyclerAdapter;
import com.tyrantapp.olive.component.ButtonBoardRecyclerView;
import com.tyrantapp.olive.component.ButtonBoardRecyclerView.*;
import com.tyrantapp.olive.helper.DatabaseHelper;
import com.tyrantapp.olive.listener.OnOliveKeypadListener;
import com.tyrantapp.olive.type.ButtonInfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
	private static final String					TAG = "KeypadFragment";
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private static final String 				ARG_SECTION_NUMBER = "section_number";

	private static HashMap<Integer, Fragment>	mFragmentsMap = new HashMap<Integer, Fragment>();
	private static OnOliveKeypadListener		mOliveKeypadListener;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static KeypadFragment newInstance(Context context, int sectionNumber) {
		KeypadFragment fragment = new KeypadFragment(context, sectionNumber);
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		
		mFragmentsMap.put(sectionNumber, fragment);		
		return fragment;
	}		

	public static Fragment getFragment(int sectionNumber) {
		return mFragmentsMap.get(sectionNumber);			
	}		

	public static void setOnOliveKeypadListener(OnOliveKeypadListener listener) {
		mOliveKeypadListener = listener;
	}


    // member variables.
    private Context                     mContext;
    private int				            mSectionNumber;
    private ButtonBoardRecyclerAdapter  mButtonBoardAdapter;

    public KeypadFragment(Context context, int sectionNumber) {
        mContext = context;
        mButtonBoardAdapter = new ButtonBoardRecyclerAdapter(context, sectionNumber);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = null;
		
		mSectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);

        rootView = inflater.inflate(R.layout.fragment_buttonboard, container, false);

        ButtonBoardRecyclerView view = (ButtonBoardRecyclerView)rootView.findViewById(R.id.buttonboard_view);
        view.setAdapter(mButtonBoardAdapter);
        view.setOnItemClickListener(new OnKeypadClickListener(mOliveKeypadListener, mSectionNumber));

		if (mOliveKeypadListener != null) {
			mOliveKeypadListener.onKeypadCreate(mSectionNumber);
		}			
		
		return rootView;
	}

    class OnKeypadClickListener implements OnItemClickListener {
        private OnOliveKeypadListener 	mKeypadListener;

        private int						mSectionNumber;

        public OnKeypadClickListener(OnOliveKeypadListener listener, int sectionNumber) {
            mKeypadListener = listener;
            mSectionNumber = sectionNumber;
        }

        @Override
        public void onItemClick(View view, int position) {
            if (mKeypadListener != null) {
                mKeypadListener.onKeypadClick(mSectionNumber, position);
            }
        }

        @Override
        public void onItemLongClick(View view, int position) {
            if (mKeypadListener != null) {
                mKeypadListener.onKeypadLongClick(mSectionNumber, position);
            }
            ((Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(100);
        }
    }

	public ButtonInfo getOliveButton(int sectionNumber, int index) {
        int nIndex = sectionNumber * DatabaseHelper.PresetButtonHelper.BUTTON_PER_SECTION + index;
        long id = DatabaseHelper.PresetButtonHelper.getIdByIndex(mContext, nIndex);
		return DatabaseHelper.PresetButtonHelper.getButtonInfo(mContext, id);
	}
}
