package com.tyrantapp.olive.types;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class OliveMessage  implements Parcelable {
	public String	mFrom;
	public String	mTo;
	public boolean	mIsRead;
	public boolean	mIsPending;
	
	public int		mAuthor;
	public int		mCategory;
	public String	mContext;

	public long		mCreated;	

    public static final Parcelable.Creator<OliveMessage> CREATOR = new Parcelable.Creator<OliveMessage>() {
        public OliveMessage createFromParcel(Parcel in) {
        	OliveMessage oRet = new OliveMessage();
        	
        	oRet.mFrom = in.readString();
        	oRet.mTo = in.readString();
        	oRet.mIsRead = in.readInt() > 0;
        	oRet.mIsPending = in.readInt() > 0;
        	
        	oRet.mAuthor = in.readInt();
        	oRet.mCategory = in.readInt();
        	oRet.mContext = in.readString();
        	
        	oRet.mCreated = in.readLong();
            
        	return oRet;
        }
        
        public OliveMessage[] newArray( int size ) {
            return new OliveMessage[size];
        }
    };
    
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mFrom);
        dest.writeString(mTo);
        dest.writeInt((mIsRead) ? 1 :0);
        dest.writeInt((mIsPending) ? 1 :0);
        
        dest.writeInt(mAuthor);
        dest.writeInt(mCategory);
        dest.writeString(mContext);
        
        dest.writeLong(mCreated);
    }
}
