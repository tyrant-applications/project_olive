
package com.tyrantapp.olive;

import com.kth.baasio.Baas;
import com.kth.baasio.callback.BaasioDeviceCallback;
import com.kth.baasio.entity.push.BaasioDevice;
import com.kth.baasio.exception.BaasioException;
import com.kth.common.utils.LogUtils;
import com.tyrantapp.olive.configuration.BaasioConfig;
import com.tyrantapp.olive.helper.OliveHelper;
import com.tyrantapp.olive.network.AWSQueryManager;

import android.app.Application;
import android.os.AsyncTask;
import android.os.StrictMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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

        // copy assets to internal memory
        if (OliveHelper.copyAssets(this, "presets", false)) {
            File file = new File(getExternalFilesDir(null), "presets/default.json");
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                StringBuilder builder = new StringBuilder();
                while (true) {
                    String line = reader.readLine();
                    if (line != null) {
                        builder.append(line);
                    } else {
                        break;
                    }
                }
                HashMap<String, String> mapDefault = OliveHelper.JSONParser(builder.toString());
                ArrayList<HashMap<String, String>> arrDefault = OliveHelper.JSONArrayParser(mapDefault.get("default"));
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
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
