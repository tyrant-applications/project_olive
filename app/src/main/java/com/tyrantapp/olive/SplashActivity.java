package com.tyrantapp.olive;

import com.tyrantapp.olive.configuration.Constants;
import com.tyrantapp.olive.helper.DatabaseHelper;
import com.tyrantapp.olive.helper.OliveHelper;
import com.tyrantapp.olive.service.SyncNetworkService;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class SplashActivity extends BaseActivity {
	private final static String TAG = "SplashActivity";
	
	private final static int LONG_SHOWING_TIME = 2000;
    private final static int SHORT_SHOWING_TIME = 500;
	
	class FinishHandler extends Handler {
		static final public int SPLASH_TO_MAIN 		= 0;
		static final public int SPLASH_TO_TUTORIAL 	= 1;
		static final public int SPLASH_TO_LOGIN		= 2;
		
		private Intent mIntent = null;
        private long mRoomId = -1;
        private boolean mAuthenticated = false;
		
		public void handleMessage(Message msg) {			
			switch(msg.what) {
			case SPLASH_TO_MAIN:
				finish();
				mIntent = new Intent(getApplicationContext(), MainActivity.class);
                if (mRoomId < 0)
                    startActivity(mIntent);
                else
                    startActivity(mIntent.putExtra(Constants.Intent.EXTRA_ROOM_ID, mRoomId));

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

        public void setRoomId(long idRoom) {
            mRoomId = idRoom;
        }
        public void setAuthenticated(boolean authenticated) {
            mAuthenticated = authenticated;
        }
	}
	
	private FinishHandler mFinishHandler = new FinishHandler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
	}
	
	@Override
	protected void onStart() {
		super.onStart();

        if (mRESTApiManager.isAutoSignIn()) {
            if (!OliveHelper.isConnectedNetwork(this) || mRESTApiManager.verifyDevice()) {

                // check intent
                Intent intent = getIntent();
                long idRoom = -1;
                if (intent != null) {
                    idRoom = intent.getLongExtra(Constants.Intent.EXTRA_ROOM_ID, -1);
                }

                if (idRoom >= 0) {
                    mFinishHandler.setRoomId(idRoom);
                    mFinishHandler.removeMessages(FinishHandler.SPLASH_TO_LOGIN);
                    mFinishHandler.sendEmptyMessage(FinishHandler.SPLASH_TO_MAIN);
                } else {
                    mFinishHandler.removeMessages(FinishHandler.SPLASH_TO_LOGIN);
                    mFinishHandler.sendEmptyMessageDelayed(FinishHandler.SPLASH_TO_MAIN, SHORT_SHOWING_TIME);
                }

                // 1. sync all (room -> conversation -> friends -> user)
                Intent syncIntent = null;
                syncIntent = new Intent(this, SyncNetworkService.class).setAction(SyncNetworkService.INTENT_ACTION_SYNC_ALL);
                startService(syncIntent);
            } else {
                // Notification reason and logout
                mRESTApiManager.signOut();
                mFinishHandler.sendEmptyMessageDelayed(FinishHandler.SPLASH_TO_LOGIN, LONG_SHOWING_TIME);
            }
        } else {
        	mFinishHandler.sendEmptyMessageDelayed(FinishHandler.SPLASH_TO_LOGIN, LONG_SHOWING_TIME);
        }
	}
}
