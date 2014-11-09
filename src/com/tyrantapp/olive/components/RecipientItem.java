package com.tyrantapp.olive.components;

import java.util.ArrayList;

import com.tyrantapp.olive.adapters.ConversationListAdapter;

public class RecipientItem {

	private String 					mRecipientName;
    private ConversationListAdapter	mAdapter;

    public String getRecipientName() {
        return mRecipientName;
    }

    public void setRecipientName(String name) {
        mRecipientName = name;
    }

    public ConversationListAdapter getAdapter() {
        return mAdapter;
    }

}

