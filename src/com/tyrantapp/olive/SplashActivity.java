package com.tyrantapp.olive;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.R.layout;
import com.tyrantapp.olive.services.IOliveService;
import com.tyrantapp.olive.services.OliveService;
import com.tyrantapp.olive.types.UserInfo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class SplashActivity extends Activity {
	final static private String TAG = "SplashActivity";
	
	final private int LONG_SHOWING_TIME = 2000;
	final private int SHORT_SHOWING_TIME = 500;
	
	class FinishHandler extends Handler {
		static final public int SPLASH_TO_MAIN 		= 0;
		static final public int SPLASH_TO_TUTORIAL 	= 1;
		static final public int SPLASH_TO_LOGIN		= 2;
		
		private Intent mIntent = null;
		
		public void handleMessage(Message msg) {			
			switch(msg.what) {
			case SPLASH_TO_MAIN:
				UserInfo info;
				try {
					info = mService.getUserProfile();
					
					finish();
					mIntent = new Intent(getApplicationContext(), MainActivity.class).putExtra("username", info.mUsername);
					startActivity(mIntent);
					overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				// Test
				UserInfo info2 = null;
				try {
					info2 = mService.getRecipientProfile("hyoil");
					
					if (info2 != null) {
						android.util.Log.d(TAG, "Username = " + info2.mUsername);
						android.util.Log.d(TAG, "Nickname = " + info2.mNickname);
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				break;
				
			case SPLASH_TO_LOGIN:
				finish();
				mIntent = new Intent(getApplicationContext(), SignInActivity.class);
				startActivity(mIntent);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				break;
			}
		}
	};
	
	private FinishHandler mFinishHandler = new FinishHandler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_splash);
	}
	
	@Override
	protected void onStart() {
		startServiceBind();
		mFinishHandler.sendEmptyMessageDelayed(FinishHandler.SPLASH_TO_LOGIN, LONG_SHOWING_TIME);
		super.onStart();
	}

	@Override
	protected void onStop() {
		stopServiceBind();
		super.onStop();
	}

	IOliveService mService;
	ServiceConnection mConntection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			
			if(service != null){
				mService = IOliveService.Stub.asInterface(service);
			
				try {
					if (mService.isSignedIn()) {
						mFinishHandler.removeMessages(FinishHandler.SPLASH_TO_LOGIN);
						mFinishHandler.sendEmptyMessageDelayed(FinishHandler.SPLASH_TO_MAIN, SHORT_SHOWING_TIME);
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			
			if(mService != null){
				mService = null;
			}
		}
	};
	
	private void startServiceBind(){
		//startService(new Intent(this, OliveService.class));
		bindService(new Intent(this, OliveService.class), mConntection, Context.BIND_AUTO_CREATE);
	}
	
	private void stopServiceBind(){
		unbindService(mConntection);
		//stopService(new Intent(this, OliveService.class));
	}	
}
