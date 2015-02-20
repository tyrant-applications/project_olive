package com.tyrantapp.olive.type;

import android.os.Parcel;
import android.os.Parcelable;

public class RecipientInfo implements Parcelable {
    public long     mId;
    public String	mUsername;
	public String   mDisplayname;
	public String	mPhoneNumber;
	public String   mPicture;
    public String   mMediaURL;
	public long		mModified;	

    public static final Parcelable.Creator<RecipientInfo> CREATOR = new Parcelable.Creator<RecipientInfo>() {
        public RecipientInfo createFromParcel(Parcel in) {
        	RecipientInfo oRet = new RecipientInfo();

            oRet.mId = in.readLong();
            oRet.mUsername = in.readString();
        	oRet.mDisplayname = in.readString();
        	oRet.mPhoneNumber = in.readString();
            oRet.mPicture = in.readString();
            oRet.mMediaURL = in.readString();
        	oRet.mModified = in.readLong();
            
        	return oRet;
        }
        
        public RecipientInfo[] newArray( int size ) {
            return new RecipientInfo[size];
        }
    };
    
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeString(mUsername);
        dest.writeString(mDisplayname);
        dest.writeString(mPhoneNumber);
        dest.writeString(mPicture);
        dest.writeString(mMediaURL);
        dest.writeLong(mModified);
    }
}
