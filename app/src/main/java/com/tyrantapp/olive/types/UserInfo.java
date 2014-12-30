package com.tyrantapp.olive.types;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class UserInfo  implements Parcelable {
	public String	mUsername;
	public String	mNickname;
	public String	mPhoneNumber;
	//public Bitmap	mPicture;
	public long		mModified;	

    public static final Parcelable.Creator<UserInfo> CREATOR = new Parcelable.Creator<UserInfo>() {
        public UserInfo createFromParcel(Parcel in) {
        	UserInfo oRet = new UserInfo();
        	
        	oRet.mUsername = in.readString();
        	oRet.mNickname = in.readString();
        	oRet.mPhoneNumber = in.readString();
        	oRet.mModified = in.readLong();
            
        	return oRet;
        }
        
        public UserInfo[] newArray( int size ) {
            return new UserInfo[size];
        }
    };
    
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUsername);
        dest.writeString(mNickname);
        dest.writeString(mPhoneNumber);
        dest.writeLong(mModified);
    }
}
