package com.tyrantapp.olive;

import android.support.v4.app.FragmentActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.tyrantapp.olive.network.RESTApiManager;
import com.tyrantapp.olive.service.ISyncNetworkService;
import com.tyrantapp.olive.service.SyncNetworkService;

import java.util.ArrayList;


public abstract class BaseActivity extends FragmentActivity {
	private static final String TAG = "BaseActivity";
	
	private OnConnectServiceListener	mConnectServiceListener = null;
	private boolean					    mActivatePasscode = false;
	
	protected RESTApiManager mRESTApiManager = RESTApiManager.getInstance();

    private boolean mHoldStartActivity = true;
    private ArrayList<Intent>   mPendingIntent = new ArrayList<Intent>();
	
	@Override
	protected void onStart() {
		super.onStart();
		
		if (mActivatePasscode) {
            mHoldStartActivity = false;
            new Exception().printStackTrace();
            if (!PasscodeActivity.verifyAuthenticateKey(getApplicationContext(), getIntent().getStringExtra(PasscodeActivity.AUTHENTICATE_KEY))) {
	        	Intent intent = new Intent(this, PasscodeActivity.class);
	        	startActivityForPasscode(intent);
	        } else {
	        	ignorePasscodeOnce();
	        }
		}
    	
		startServiceBind();		
	}

	@Override
	protected void onStop() {
        mHoldStartActivity = true;
		stopServiceBind();
		super.onStop();
	}

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		//new Exception().printStackTrace();
		android.util.Log.d(TAG, "onActivityResult");
		if (requestCode == PasscodeActivity.REQUEST_CODE) {
			if (resultCode == PasscodeActivity.RESULT_SUCCESS) {
                getIntent().putExtra(PasscodeActivity.AUTHENTICATE_KEY, PasscodeActivity.requestAuthenticateKey());

                mHoldStartActivity = false;
                for (Intent newIntent : mPendingIntent) {
                    startActivityForPasscode(newIntent);
                }
                mPendingIntent.clear();
            }
		}
	}
	
	protected void ignorePasscodeOnce() {
    	setResult(PasscodeActivity.RESULT_SUCCESS);
	}
	
	protected void startActivityForPasscode(Intent intent) {
        if (mActivatePasscode && mHoldStartActivity) {
            mPendingIntent.add(intent);
        } else {
            intent.putExtra(PasscodeActivity.AUTHENTICATE_KEY, PasscodeActivity.requestAuthenticateKey());
            startActivityForResult(intent, PasscodeActivity.REQUEST_CODE);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
	}

	protected void setEnablePasscode(boolean enable) {
		mActivatePasscode = enable;
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
