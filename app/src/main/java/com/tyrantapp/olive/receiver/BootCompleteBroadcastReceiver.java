package com.tyrantapp.olive.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tyrantapp.olive.service.SyncNetworkService;

/**
 * Created by onetop21 on 15. 2. 3.
 */
public class BootCompleteBroadcastReceiver extends BroadcastReceiver {
    private final static String TAG = BootCompleteBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            Intent syncIntent = null;
            syncIntent = new Intent(context, SyncNetworkService.class).setAction(SyncNetworkService.INTENT_ACTION_SYNC_ALL);
            context.startService(syncIntent);
            if (syncIntent == null) {
                android.util.Log.e(TAG, "Could not start service. " + syncIntent.toString());
            }
        } else {
            android.util.Log.e(TAG, "Received unexpected intent " + intent.toString());
        }
    }
}
