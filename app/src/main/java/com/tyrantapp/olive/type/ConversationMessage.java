package com.tyrantapp.olive.type;

import android.os.Parcel;
import android.os.Parcelable;

public class ConversationMessage implements Parcelable {

    public long     mId;
    public long     mMessageId;
    public long     mSpaceId;
	public String   mSender;
	public String	mAuthor;
	public String   mMimetype;
	public String	mContext;
    public int      mStatus;
    public long		mCreated;

    public static final Parcelable.Creator<ConversationMessage> CREATOR = new Parcelable.Creator<ConversationMessage>() {
        public ConversationMessage createFromParcel(Parcel in) {
        	ConversationMessage oRet = new ConversationMessage();

            oRet.mId = in.readLong();
            oRet.mMessageId = in.readLong();
            oRet.mSpaceId = in.readLong();
            oRet.mSender = in.readString();
        	oRet.mAuthor = in.readString();
            oRet.mMimetype = in.readString();
        	oRet.mContext = in.readString();
            oRet.mStatus = in.readInt();
            oRet.mCreated = in.readLong();
            
        	return oRet;
        }
        
        public ConversationMessage[] newArray( int size ) {
            return new ConversationMessage[size];
        }
    };
    
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeLong(mMessageId);
        dest.writeLong(mSpaceId);
        dest.writeString(mSender);
        dest.writeString(mAuthor);
        dest.writeString(mMimetype);
        dest.writeString(mContext);
        dest.writeInt(mStatus);
        dest.writeLong(mCreated);
    }
}
