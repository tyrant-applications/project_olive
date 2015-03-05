package com.tyrantapp.olive.type;

import android.os.Parcel;
import android.os.Parcelable;

public class ButtonInfo implements Parcelable {
    public long     mId;
    public int      mIndex;
    public String   mMimetype;
    public String   mContext;
    public long     mButtonId;
    public String	mAuthor;
    public int      mVersion;

    public static final Creator<ButtonInfo> CREATOR = new Creator<ButtonInfo>() {
        public ButtonInfo createFromParcel(Parcel in) {
        	ButtonInfo oRet = new ButtonInfo();

            oRet.mId = in.readLong();
            oRet.mIndex = in.readInt();
            oRet.mMimetype = in.readString();
            oRet.mContext = in.readString();
            oRet.mButtonId = in.readLong();
            oRet.mAuthor = in.readString();
            oRet.mVersion = in.readInt();

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
        dest.writeInt(mIndex);
        dest.writeString(mMimetype);
        dest.writeString(mContext);
        dest.writeLong(mButtonId);
        dest.writeString(mAuthor);
        dest.writeInt(mVersion);
    }
}
