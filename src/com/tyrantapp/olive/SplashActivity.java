package com.tyrantapp.olive;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.R.layout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class SplashActivity extends Activity {
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
				finish();
				//mIntent = new Intent(getApplicationContext(), MainActivity.class);
				mIntent = new Intent(getApplicationContext(), LoginActivity.class);
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

		//getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
	    //getActionBar().hide();
	    
		setContentView(R.layout.activity_splash);
		
		mFinishHandler.sendEmptyMessageDelayed(FinishHandler.SPLASH_TO_MAIN, SHORT_SHOWING_TIME);
		//mFinishHandler.sendEmptyMessageDelayed(FinishHandler.SPLASH_TO_TUTORIAL, LONG_SHOWING_TIME);
		//mFinishHandler.sendEmptyMessageDelayed(FinishHandler.SPLASH_TO_LOGIN, LONG_SHOWING_TIME);
	}
}
