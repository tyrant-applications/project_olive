
package com.tyrantapp.olive;

import com.google.android.gcm.GCMRegistrar;
import com.kth.baasio.Baas;
import com.kth.baasio.callback.BaasioDeviceCallback;
import com.kth.baasio.entity.push.BaasioDevice;
import com.kth.baasio.exception.BaasioException;
import com.kth.common.utils.LogUtils;
import com.tyrantapp.olive.configuration.BaasioConfig;
import com.tyrantapp.olive.configuration.Constants;
import com.tyrantapp.olive.helper.DatabaseHelper;
import com.tyrantapp.olive.helper.OliveHelper;
import com.tyrantapp.olive.network.AWSQueryManager;
import com.tyrantapp.olive.provider.OliveContentProvider;

import android.app.Application;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.widget.Toast;

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

        AWSQueryManager.initialize(getApplicationContext());
        //BaasioHelper.initialize(getApplicationContext());

//        Baas.io().init(this, BaasioConfig.BAASIO_URL, BaasioConfig.BAASIO_ID,
//                BaasioConfig.APPLICATION_ID);
//
//        mGCMRegisterTask = Baas.io().setGcmEnabled(this, null, new BaasioDeviceCallback() {
//
//            @Override
//            public void onException(BaasioException e) {
//                LogUtils.LOGE(TAG, "init onException:" + e.toString());
//            }
//
//            @Override
//            public void onResponse(BaasioDevice response) {
//                LogUtils.LOGD(TAG, "init onResponse:" + response.toString());
//            }
//        }, BaasioConfig.GCM_SENDER_ID);

        GCMRegistrar.setRegisteredOnServer(getApplicationContext(), true);

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

                // add to database
                for (HashMap<String, String> valueSet : arrDefault) {
                    String author = valueSet.get("author");

                    File subFile = new File(getExternalFilesDir(null), "presets/" + author + ".json");
                    try {
                        BufferedReader subReader = new BufferedReader(new FileReader(subFile));
                        StringBuilder subBuilder = new StringBuilder();
                        while (true) {
                            String line = subReader.readLine();
                            if (line != null) {
                                subBuilder.append(line);
                            } else {
                                break;
                            }
                        }
                        HashMap<String, String> mapSub = OliveHelper.JSONParser(subBuilder.toString());
                        String displayName = mapSub.get("displayname");
                        int version = Integer.parseInt(mapSub.get("version"));
                        ArrayList<HashMap<String, String>> arrKeys = OliveHelper.JSONArrayParser(mapSub.get("keys"));

                        if (DatabaseHelper.DownloadSetHelper.addDownloadSet(this, displayName, author, version) >= 0) {
                            for (HashMap<String, String> key : arrKeys) {
                                String mimetype = key.get("mimetype");
                                String contexts = key.get("context");
                                long idButton = Long.parseLong(key.get("id"));

                                DatabaseHelper.DownloadButtonHelper.addButton(this, mimetype, contexts, idButton, author, version);
                            }
                        }
                    } catch (FileNotFoundException se1) {
                        se1.printStackTrace();
                    } catch (IOException se2) {
                        se2.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }

    }

    @Override
    public void onTerminate() {
        GCMRegistrar.setRegisteredOnServer(this, false);

        if (mGCMRegisterTask != null) {
            mGCMRegisterTask.cancel(true);
        }

        Baas.io().uninit(this);
        super.onTerminate();
    }
}
