/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tyrantapp.olive.services;

import com.google.android.gcm.GCMBaseIntentService;
import com.kth.baasio.entity.push.BaasioPayload;
import com.kth.baasio.entity.push.BaasioPush;
import com.kth.baasio.exception.BaasioException;
import com.kth.baasio.utils.JsonUtils;
import com.kth.baasio.utils.ObjectUtils;
import com.kth.common.utils.LogUtils;
import com.tyrantapp.olive.ConversationActivity;
import com.tyrantapp.olive.MainActivity;
import com.tyrantapp.olive.R;
import com.tyrantapp.olive.configurations.BaasioConfig;
import com.tyrantapp.olive.helper.RESTHelper;
import com.tyrantapp.olive.providers.OliveContentProvider.ConversationColumns;
import com.tyrantapp.olive.providers.OliveContentProvider.RecipientColumns;
import com.tyrantapp.olive.types.OliveMessage;
import com.tyrantapp.olive.types.UserInfo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;

/**
 * {@link android.app.IntentService} responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

    private static final String TAG = LogUtils.makeLogTag("GCM");

    public GCMIntentService() {
        super(BaasioConfig.GCM_SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String regId) {
        LogUtils.LOGI(TAG, "Device registered: regId=" + regId);
        
        try {
            BaasioPush.register(context, regId);
        } catch (BaasioException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void onUnregistered(Context context, String regId) {
        LogUtils.LOGI(TAG, "Device unregistered");

        try {
            BaasioPush.unregister(context);
        } catch (BaasioException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        String announcement = intent.getStringExtra("message");
        if (announcement != null) {
            generateNotification(context, announcement);

            syncConversation(announcement);            
            return;
        }
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String message) {
        BaasioPayload msg = JsonUtils.parse(message, BaasioPayload.class);
        if (ObjectUtils.isEmpty(msg)) {
            return;
        }

        String alert = "";
        if (!ObjectUtils.isEmpty(msg.getAlert())) {
            alert = msg.getAlert().replace("\\r\\n", "\n");
        }

        String to = msg.getProperty(ConversationActivity.EXTRA_TO).asText();
        String from = msg.getProperty(ConversationActivity.EXTRA_FROM).asText();
        
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(context, ConversationActivity.class)
        		.putExtra(ConversationActivity.EXTRA_FROM, from)
        		.putExtra(ConversationActivity.EXTRA_TO, to)
        		.putExtra("TEST", from);
        
        android.util.Log.d(TAG, "Receive by push [" + from + "] => [" + to + "]");
        
        
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(context).setWhen(when)
                .setSmallIcon(icon).setContentTitle(context.getString(R.string.app_name))
                .setContentText(from + " : " + alert).setContentIntent(intent).setTicker(from + " : " + alert)
                .setAutoCancel(true).setVibrate(new long[]{200, 100, 200, 100}).getNotification();
        
        notificationManager.notify(0, notification);
    }
    
    @Override
    public void onError(Context context, String errorId) {
        LogUtils.LOGE(TAG, "Received error: " + errorId);
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        LogUtils.LOGW(TAG, "Received recoverable error: " + errorId);
        return super.onRecoverableError(context, errorId);
    }
    
    private void syncConversation(String message) {
    	android.util.Log.d(TAG, "GCM::syncConversation + " + message);
        BaasioPayload payload = JsonUtils.parse(message, BaasioPayload.class);
        if (ObjectUtils.isEmpty(payload)) {
            return;
        }

        String from = payload.getProperty(ConversationActivity.EXTRA_FROM).asText();
        
        Intent intent = new Intent(this, SyncNetworkService.class)
        	.setAction(SyncNetworkService.INTENT_ACTION_SYNC_CONVERSATION)
        	.putExtra(SyncNetworkService.EXTRA_FROM, from);
        startService(intent);
    }
}
