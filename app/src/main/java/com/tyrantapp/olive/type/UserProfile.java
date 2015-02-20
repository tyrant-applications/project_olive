package com.tyrantapp.olive.type;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class UserProfile implements Parcelable {
    public String	mUsername;
    public long		mModified;

    public static final Creator<UserProfile> CREATOR = new Creator<UserProfile>() {
        public UserProfile createFromParcel(Parcel in) {
        	UserProfile oRet = new UserProfile();
        	
        	oRet.mUsername = in.readString();
        	oRet.mModified = in.readLong();
            
        	return oRet;
        }
        
        public UserProfile[] newArray( int size ) {
            return new UserProfile[size];
        }
    };
    
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUsername);
        dest.writeLong(mModified);
    }
}
