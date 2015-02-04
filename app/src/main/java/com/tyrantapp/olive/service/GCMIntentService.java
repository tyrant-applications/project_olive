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

package com.tyrantapp.olive.service;

import com.google.android.gcm.GCMBaseIntentService;
import com.kth.baasio.entity.push.BaasioPayload;
import com.kth.baasio.entity.push.BaasioPush;
import com.kth.baasio.exception.BaasioException;
import com.kth.baasio.utils.JsonUtils;
import com.kth.baasio.utils.ObjectUtils;
import com.kth.common.utils.LogUtils;
import com.tyrantapp.olive.ConversationActivity;
import com.tyrantapp.olive.OliveApplication;
import com.tyrantapp.olive.configuration.BaasioConfig;
import com.tyrantapp.olive.configuration.Constants;
import com.tyrantapp.olive.helper.OliveHelper;
import com.tyrantapp.olive.network.AWSQueryManager;
import com.tyrantapp.olive.network.RESTApiManager;
import com.tyrantapp.olive.type.ChatSpaceInfo;
import com.tyrantapp.olive.util.SharedVariables;

import android.content.Context;
import android.content.Intent;

/**
 * {@link android.app.IntentService} responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

    private static final String TAG = LogUtils.makeLogTag("GCM");
    
    public static final int NOTIFICATION_ID = 10000;

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
            return;
        }
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    // This method do not used * depressed *
    private static void generateNotification(Context context, String message) {
        BaasioPayload msg = JsonUtils.parse(message, BaasioPayload.class);
        if (ObjectUtils.isEmpty(msg)) {
            return;
        }

        String alert = "";
        if (!ObjectUtils.isEmpty(msg.getAlert())) {
            alert = msg.getAlert().replace("\\r\\n", "\n");
        }

        // ROOM ID
        // SENDER
        // MESSAGE

        String pushType = msg.getProperty(RESTApiManager.OLIVE_PUSH_PROPERTY_PUSH_TYPE).asText();
        if (RESTApiManager.OLIVE_PUSH_PROPERTY_PUSH_TYPE_CREATE.equals(pushType)) {
            Intent syncIntent = new Intent(context, SyncNetworkService.class)
                    .setAction(SyncNetworkService.INTENT_ACTION_SYNC_ROOMS_LIST);
            context.startService(syncIntent);
        } else
        if (RESTApiManager.OLIVE_PUSH_PROPERTY_PUSH_TYPE_LEAVE.equals(pushType)) {
            Intent syncIntent = new Intent(context, SyncNetworkService.class)
                    .setAction(SyncNetworkService.INTENT_ACTION_SYNC_ROOMS_LIST);
            context.startService(syncIntent);
        } else
        if (RESTApiManager.OLIVE_PUSH_PROPERTY_PUSH_TYPE_SIGNOUT.equals(pushType)) {
        } else
        if (RESTApiManager.OLIVE_PUSH_PROPERTY_PUSH_TYPE_POST.equals(pushType)) {
            Long idRoom = msg.getProperty(RESTApiManager.OLIVE_PUSH_PROPERTY_ROOM_ID).asLong();
            String sender = msg.getProperty(RESTApiManager.OLIVE_PUSH_PROPERTY_SENDER).asText();

            // Notification?!
            String foregroundActivity = OliveHelper.getForegroundActivityName(context);
            boolean isTopPackage = foregroundActivity.startsWith(OliveApplication.class.getPackage().getName());
            boolean isTopConversation = foregroundActivity.equals(ConversationActivity.class.getName());

            boolean bNotifiy = true;

            Intent syncIntent = null;
            if (isTopPackage) {
                // need to call sync
                syncIntent = new Intent(context, SyncNetworkService.class)
                        .setAction(SyncNetworkService.INTENT_ACTION_SYNC_CONVERSATION)
                        .putExtra(Constants.Intent.EXTRA_ROOM_ID, idRoom);
                context.startService(syncIntent);

                if (isTopConversation) {
                    ChatSpaceInfo info = (ChatSpaceInfo) SharedVariables.get(Constants.Conversation.SHARED_SPACE_INFO);

                    if (info != null && info.mChatroomId == idRoom) {
                        // user in the room.
                        syncIntent = new Intent(context, SyncNetworkService.class)
                                .setAction(SyncNetworkService.INTENT_ACTION_READ_MESSAGES)
                                .putExtra(Constants.Intent.EXTRA_SPACE_ID, info.mId);
                        context.startService(syncIntent);
                        OliveHelper.removeNotification(context);
                        bNotifiy = false;
                    }
                }
            }

            if (bNotifiy)
                OliveHelper.generateMessageNotification(context, idRoom, sender, alert, 1);
        }

//        int icon = R.drawable.ic_launcher;
//        long when = System.currentTimeMillis();
//        NotificationManager notificationManager = (NotificationManager)context
//                .getSystemService(Context.NOTIFICATION_SERVICE);
//
//        Intent notificationIntent = new Intent(context, SplashActivity.class);
//
//        // set intent so it does not start a new activity
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        //notificationIntent.putExtra(ConversationActivity.EXTRA_SPACE_ID, DatabaseHelper.SpaceHelper.getSpaceId(context, idRoom));
//
//        android.util.Log.d(TAG, "Receive by push [" + sender + "] => [ ROOM : " + idRoom + " ]");
//
//        android.util.Log.d(TAG, "GCMIntentService.intent = " + notificationIntent.getExtras().toString());
//
//        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
//
//        Builder builder = new NotificationCompat.Builder(context).setWhen(when)
//                .setSmallIcon(icon).setContentTitle(context.getString(R.string.app_name))
//                .setContentText(sender + " : " + alert).setContentIntent(intent).setTicker(sender + " : " + alert)
//                .setAutoCancel(true);
//
//        if (PreferenceHelper.getBooleanPreferences(context, SettingActivity.OLIVE_PREF_NOTIFICATION, true) &&
//            !OliveHelper.getForegroundPackageName(context).equals(MainActivity.class.getPackage().getName().toString())) {
//        	// Notification ON!
//        	//builder.setVibrate(new long[]{200, 100, 200, 100});
//        	builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
//        }
//
//        Notification notification = builder.getNotification();
//	    notificationManager.notify(GCMIntentService.NOTIFICATION_ID, notification);
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
}
