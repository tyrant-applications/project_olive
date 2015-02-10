package com.tyrantapp.olive.type;

import android.os.Parcel;
import android.os.Parcelable;

public class ButtonInfo implements Parcelable {
    public long     mId;
    public long     mSectionId;
    public int   	mSectionIndex;
    public String	mAuthor;
    public String   mMimetype;
    public int      mIndex;
    public String   mContext;

    public static final Creator<ButtonInfo> CREATOR = new Creator<ButtonInfo>() {
        public ButtonInfo createFromParcel(Parcel in) {
        	ButtonInfo oRet = new ButtonInfo();

            oRet.mId = in.readLong();
            oRet.mSectionId = in.readLong();
            oRet.mSectionIndex = in.readInt();
            oRet.mAuthor = in.readString();
            oRet.mMimetype = in.readString();
            oRet.mIndex = in.readInt();
            oRet.mContext = in.readString();

        	return oRet;
        }
        
        public ButtonInfo[] newArray( int size ) {
            return new ButtonInfo[size];
        }
    };
    
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeLong(mSectionId);
        dest.writeInt(mSectionIndex);
        dest.writeString(mAuthor);
        dest.writeString(mMimetype);
        dest.writeInt(mIndex);
        dest.writeString(mContext);
    }
}
