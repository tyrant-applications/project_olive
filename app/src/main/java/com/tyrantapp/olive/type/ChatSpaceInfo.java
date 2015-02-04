package com.tyrantapp.olive.type;

import android.os.Parcel;

public class ChatSpaceInfo extends SpaceInfo {
    public long     mRecipientId;
    public String   mDisplayname;
    public String   mPhonenumber;
    //public Bitmap   mPicture;

    public static final Creator<ChatSpaceInfo> CREATOR = new Creator<ChatSpaceInfo>() {
        public ChatSpaceInfo createFromParcel(Parcel in) {
        	ChatSpaceInfo oRet = new ChatSpaceInfo();

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

            oRet.mRecipientId = in.readLong();
            oRet.mDisplayname = in.readString();
            oRet.mPhonenumber = in.readString();
            //oRet.mPicture = in.readBundle();

        	return oRet;
        }
        
        public ChatSpaceInfo[] newArray( int size ) {
            return new ChatSpaceInfo[size];
        }
    };
    
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(mRecipientId);
        dest.writeString(mDisplayname);
        dest.writeString(mPhonenumber);
        //dest.writeBundle(mPicture);
    }
}
