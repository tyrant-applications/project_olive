package com.tyrantapp.olive;

import android.support.v4.app.FragmentActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.tyrantapp.olive.helper.BaasioHelper;
import com.tyrantapp.olive.helper.RESTHelper;
import com.tyrantapp.olive.services.ISyncNetworkService;
import com.tyrantapp.olive.services.SyncNetworkService;


public abstract class BaseActivity extends FragmentActivity {
	private static final String TAG = "BaseActivity";
	
	private OnConnectServiceListener mConnectServiceListener = null;
	
	protected RESTHelper mRESTHelper = RESTHelper.getInstance();
	
	@Override
	protected void onStart() {
		super.onStart();
		startServiceBind();		
	}

	@Override
	protected void onStop() {
		stopServiceBind();
		super.onStop();
	}
	
	protected void setOnConnectServiceListener(OnConnectServiceListener listener) {
		mConnectServiceListener = listener;
	}
	
	
	
	// for interface
//	IOliveServiceCallback mCallbcak = new IOliveServiceCallback.Stub() {
//		
//		@Override
//		public void valueChanged(long value) throws OliveException {
//			Log.i("BHC_TEST", "Activity Callback value : " + value);
//		}
//	};//stopService(new Intent(this, OliveService.class));
	
	// for connect service
	protected ISyncNetworkService mService;
	ServiceConnection mConntection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			if(service != null){
				mService = ISyncNetworkService.Stub.asInterface(service);
				
				if (mConnectServiceListener != null) mConnectServiceListener.onConnected();

				android.util.Log.d(TAG, "startService!" + name);
			}
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			
			if(mService != null){
				mService = null;
				
				if (mConnectServiceListener != null) mConnectServiceListener.onDisconnected();
				
				android.util.Log.d(TAG, "stopService!" + name);
			}
		}
	};
	
	protected void startServiceBind(){
		android.util.Log.d(TAG, "startServiceBind");
		startService(new Intent(this, SyncNetworkService.class));
		bindService(new Intent(this, SyncNetworkService.class), mConntection, Context.BIND_AUTO_CREATE);
	}
	
	protected void stopServiceBind(){
		unbindService(mConntection);
		//stopService(new Intent(this, OliveService.class));
	}
	
	public interface OnConnectServiceListener {
		public void onConnected();
		public void onDisconnected();
	}
}
