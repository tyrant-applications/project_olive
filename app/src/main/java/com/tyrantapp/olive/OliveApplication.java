
package com.tyrantapp.olive;

import com.kth.baasio.Baas;
import com.kth.baasio.callback.BaasioDeviceCallback;
import com.kth.baasio.entity.push.BaasioDevice;
import com.kth.baasio.exception.BaasioException;
import com.kth.common.utils.LogUtils;
import com.tyrantapp.olive.configuration.BaasioConfig;
import com.tyrantapp.olive.network.AWSQueryManager;

import android.app.Application;
import android.os.AsyncTask;
import android.os.StrictMode;

public class OliveApplication extends Application {
    private static final String TAG = LogUtils.makeLogTag(OliveApplication.class);

    AsyncTask mGCMRegisterTask;

    @Override
    public void onCreate() {
        super.onCreate();

        if(android.os.Build.VERSION.SDK_INT > 9) {
            android.util.Log.d(TAG, "Network Strict Mode On!");

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        		
        AWSQueryManager.initialize(getApplicationContext());
        //BaasioHelper.initialize(getApplicationContext());

        Baas.io().init(this, BaasioConfig.BAASIO_URL, BaasioConfig.BAASIO_ID,
                BaasioConfig.APPLICATION_ID);

        mGCMRegisterTask = Baas.io().setGcmEnabled(this, null, new BaasioDeviceCallback() {

            @Override
            public void onException(BaasioException e) {
                LogUtils.LOGE(TAG, "init onException:" + e.toString());
            }

            @Override
            public void onResponse(BaasioDevice response) {
                LogUtils.LOGD(TAG, "init onResponse:" + response.toString());
            }
        }, BaasioConfig.GCM_SENDER_ID);

    }

    @Override
    public void onTerminate() {
        if (mGCMRegisterTask != null) {
            mGCMRegisterTask.cancel(true);
        }

        Baas.io().uninit(this);
        super.onTerminate();
    }
}
