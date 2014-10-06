package com.tyrantapp.olive.components;

public class ConversationItem {
	
    private int		mAuthor;
    
    private int		mCategory;
    private int		mOliveId;
    
    private long	mTimestamp;

    
    public int getAuthor() {
        return mAuthor;
    }

    public void setAuthor(int author) {
        mAuthor = author;
    }

    public int getCategory() {
        return mCategory;
    }
    
    public void setCategory(int category) {
        mCategory = category;
    }

    public int getOliveId() {
        return mOliveId;
    }
    
    public void setOliveId(int oliveId) {
    	mOliveId = oliveId;
    }
    
    public long getTimestamp() {
    	return mTimestamp;
    }
    
    public void setTimestamp(long timestamp) {
    	mTimestamp = timestamp;
    }
    
}
