package com.tyrantapp.olive.type;

import android.os.Parcel;
import android.os.Parcelable;

public class RecipientInfo implements Parcelable {
    public long     mId;
    public String	mUsername;
	public String   mDisplayname;
	public String	mPhoneNumber;
	//public Bitmap	mPicture;
	public long		mModified;	

    public static final Parcelable.Creator<RecipientInfo> CREATOR = new Parcelable.Creator<RecipientInfo>() {
        public RecipientInfo createFromParcel(Parcel in) {
        	RecipientInfo oRet = new RecipientInfo();

            oRet.mId = in.readLong();
            oRet.mUsername = in.readString();
        	oRet.mDisplayname = in.readString();
        	oRet.mPhoneNumber = in.readString();
            //oRet.mPicture = in.readBundle();
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
        //dest.writeBundle(mPicture);
        dest.writeLong(mModified);
    }
}
