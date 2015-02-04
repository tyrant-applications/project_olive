package com.tyrantapp.olive.type;

import android.os.Parcel;
import android.os.Parcelable;

public class SpaceInfo implements Parcelable {
    public long     mId;
    public long     mChatroomId;
    public String	mParticipants;
    public String	mTitle;
    public int      mType;
    public boolean  mStarred;
    public int      mUnread;
    public long     mConversationId;
    public String   mSender;
    public String   mSnippet;
	public long     mLastUpdated;

    public static final Creator<SpaceInfo> CREATOR = new Creator<SpaceInfo>() {
        public SpaceInfo createFromParcel(Parcel in) {
        	SpaceInfo oRet = new SpaceInfo();

            oRet.mId = in.readLong();
            oRet.mChatroomId = in.readLong();
            oRet.mParticipants = in.readString();
            oRet.mTitle = in.readString();
            oRet.mType = in.readInt();
            oRet.mStarred = in.readInt() > 0;
            oRet.mUnread = in.readInt();
            oRet.mConversationId = in.readLong();
            oRet.mSender = in.readString();
            oRet.mSnippet = in.readString();
        	oRet.mLastUpdated = in.readLong();
            
        	return oRet;
        }
        
        public SpaceInfo[] newArray( int size ) {
            return new SpaceInfo[size];
        }
    };
    
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeLong(mChatroomId);
        dest.writeString(mParticipants);
        dest.writeString(mTitle);
        dest.writeInt(mType);
        dest.writeInt((mStarred) ? 1 : 0);
        dest.writeInt(mUnread);
        dest.writeLong(mConversationId);
        dest.writeString(mSender);
        dest.writeString(mSnippet);
        dest.writeLong(mLastUpdated);
    }
}
