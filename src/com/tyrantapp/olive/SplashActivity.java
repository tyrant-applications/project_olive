package com.tyrantapp.olive;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.services.SyncNetworkService;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class SplashActivity extends BaseActivity {
	final static private String TAG = "SplashActivity";
	
	final private int LONG_SHOWING_TIME = 2000;
	final private int SHORT_SHOWING_TIME = 500;
	
	private String mRecipientName;
	
	class FinishHandler extends Handler {
		static final public int SPLASH_TO_MAIN 		= 0;
		static final public int SPLASH_TO_TUTORIAL 	= 1;
		static final public int SPLASH_TO_LOGIN		= 2;
		
		private Intent mIntent = null;
		
		public void handleMessage(Message msg) {			
			switch(msg.what) {
			case SPLASH_TO_MAIN:
				finish();
				mIntent = new Intent(getApplicationContext(), MainActivity.class).putExtra(ConversationActivity.EXTRA_RECIPIENTNAME, mRecipientName);
				startActivity(mIntent);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
		
		Intent intent = new Intent(this, SyncNetworkService.class);
		startService(intent);
		
		// Access to conversation acitivity directly
		mRecipientName = getIntent().getStringExtra(ConversationActivity.EXTRA_RECIPIENTNAME);
		if (mRecipientName != null) getIntent().removeExtra(ConversationActivity.EXTRA_RECIPIENTNAME);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		if (mRESTHelper.isSignedIn()) {
            mFinishHandler.removeMessages(FinishHandler.SPLASH_TO_LOGIN);
            mFinishHandler.sendEmptyMessageDelayed(FinishHandler.SPLASH_TO_MAIN, SHORT_SHOWING_TIME);
        } else {
        	mFinishHandler.sendEmptyMessageDelayed(FinishHandler.SPLASH_TO_LOGIN, LONG_SHOWING_TIME);
        }
	}
}
