package com.tyrantapp.olive.fragments;

import com.tyrantapp.olive.ConversationActivity;
import com.tyrantapp.olive.R;
import com.tyrantapp.olive.MainActivity.ParentFragment;
import com.tyrantapp.olive.R.layout;
import com.tyrantapp.olive.adapter.RecipientsListAdapter;
import com.tyrantapp.olive.components.RecipientsListView;
import com.tyrantapp.olive.providers.OliveContentProvider.ConversationColumns;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment
 * must implement the {@link RootFragment.OnFragmentInteractionListener}
 * interface to handle interaction events. Use the
 * {@link RootFragment#newInstance} factory method to create an instance
 * of this fragment.
 * 
 */
public class RootFragment extends Fragment {
	final static private String			TAG = "RecipientsFragment";
	
	// for Recipient Fragments
	private RecipientsListView			mRecipientsListView;
	private RecipientsListAdapter 		mRecipientsAdapter;
	
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static RootFragment newInstance(int sectionNumber) {
		RootFragment fragment = new RootFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		
		return fragment;
	}

	public RootFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = null;
		
		switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
		// Recipients Fragment
		case 0:
			rootView = inflater.inflate(R.layout.fragment_recipients, container, false);

			if (rootView != null) {
				mRecipientsListView = (RecipientsListView)rootView.findViewById(R.id.recipients_list_view);
				PrepareFragment();
			}
			break;
			
		default:
		}
		
		return rootView;
	}
	
	public void PrepareFragment() {
		// Set up our adapter
		final Activity activity = getActivity();
		
		mRecipientsAdapter = new RecipientsListAdapter(activity);
		mRecipientsListView.setAdapter(mRecipientsAdapter);

		mRecipientsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// Find current item's recipient ID and start conversation activity
				
				android.util.Log.d(TAG, "id = " + id);
				
				if (id >= 0) {
					Intent intent = new Intent(getActivity(), ConversationActivity.class)
						.putExtra(ConversationColumns.RECIPIENT, id);
					startActivity(intent);
				}
			}

		});
	}
}
