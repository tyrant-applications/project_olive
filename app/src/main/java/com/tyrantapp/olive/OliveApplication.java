
package com.tyrantapp.olive;

import com.kth.baasio.Baas;
import com.kth.baasio.callback.BaasioDeviceCallback;
import com.kth.baasio.entity.push.BaasioDevice;
import com.kth.baasio.exception.BaasioException;
import com.kth.common.utils.LogUtils;
import com.tyrantapp.olive.configurations.BaasioConfig;
import com.tyrantapp.olive.helper.BaasioHelper;

import android.app.Application;
import android.os.AsyncTask;

public class OliveApplication extends Application {
    private static final String TAG = LogUtils.makeLogTag(OliveApplication.class);

    AsyncTask mGCMRegisterTask;

    @Override
    public void onCreate() {
        super.onCreate();
        		
        BaasioHelper.initialize(getApplicationContext());

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
