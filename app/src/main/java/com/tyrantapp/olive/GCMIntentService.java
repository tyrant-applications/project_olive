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

package com.tyrantapp.olive;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.kth.common.utils.LogUtils;
import com.tyrantapp.olive.configuration.Constants;
import com.tyrantapp.olive.helper.DatabaseHelper;
import com.tyrantapp.olive.helper.OliveHelper;
import com.tyrantapp.olive.network.AWSQueryManager;
import com.tyrantapp.olive.network.RESTApiManager;
import com.tyrantapp.olive.service.SyncNetworkService;
import com.tyrantapp.olive.type.ChatSpaceInfo;
import com.tyrantapp.olive.util.SharedVariables;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

/**
 * {@link android.app.IntentService} responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {
    private static final String TAG = GCMIntentService.class.getSimpleName();

    public GCMIntentService() {
        super(Constants.Configuration.GOOGLE_SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String regId) {
        Log.i(TAG, "Device registered: regId = " + regId);

        GCMRegistrar.setRegisteredOnServer(getApplicationContext(), true);
//        try {
//            BaasioPush.register(context, regId);
//        } catch (BaasioException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onUnregistered(Context context, String regId) {
        Log.i(TAG, "evice unregistered");

        GCMRegistrar.setRegisteredOnServer(getApplicationContext(), false);
//        try {
//            BaasioPush.unregister(context);
//        } catch (BaasioException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
//        String announcement = intent.getStringExtra("message");
//        if (announcement != null) {
//            generateNotification(context, announcement);
//            return;
//        }
        Log.i(TAG, "Received message");

        RESTApiManager helper = RESTApiManager.getInstance();
        if (helper.isAutoSignIn()) {
            // notifies user
            generateNotification(context, intent.getExtras());
        }
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    // This method do not used * depressed *
    private static void generateNotification(Context context, Bundle message) {
//        BaasioPayload msg = JsonUtils.parse(message, BaasioPayload.class);
//        if (ObjectUtils.isEmpty(msg)) {
//            return;
//        }
//
//        String alert = "";
//        if (!ObjectUtils.isEmpty(msg.getAlert())) {
//            alert = msg.getAlert().replace("\\r\\n", "\n");
//        }

        int pushType = Integer.parseInt(message.getString("push_type"));
        if (pushType == 1) {
//            1: New Message
//            {"msg_type": "0", "author": "ujlikes@naver.com", "push_type": 1, "reg_date": "2015-03-20 15:14:03.215582+00:00", "room_id": 23, "message_id": 85, "contents": "Yes"}
            Long idRoom = Long.parseLong(message.getString("room_id"));
            String sender = message.getString("author");
            String alert = message.getString("contents");
            int msgType = Integer.parseInt(message.getString("msg_type"));
            if (msgType == 1) {
                alert = "Received image.";
            } else
            if (msgType == 2) {
                alert = "Received video.";
            } else
            if (msgType == 3) {
                alert = "Received audio.";
            } else
            if (msgType == 4) {
                double latitude = Double.parseDouble(alert.substring(0, alert.indexOf(",")));
                double longitude = Double.parseDouble(alert.substring(alert.indexOf(",") + 1));
                try {
                    Geocoder geocoder = new Geocoder(context);
                    for (Address address : geocoder.getFromLocation(latitude, longitude, 1)) {
                        alert = address.getAddressLine(0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else
            if (msgType == 5) {
                alert = "Received emoji.";
            }

            long msgId = Long.parseLong(message.getString("message_id"));
            boolean hasMessage = (DatabaseHelper.ConversationHelper.getMessage(context, DatabaseHelper.ConversationHelper.getMessageId(context, msgId)) != null);

            // Notification?!
            String foregroundActivity = OliveHelper.getForegroundActivityName(context);
            PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = pm.isScreenOn();
            boolean isTopPackage = foregroundActivity.startsWith(OliveApplication.class.getPackage().getName());
            boolean isTopConversation = foregroundActivity.equals(ConversationActivity.class.getName());

            boolean bNotifiy = !hasMessage;

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

            if (bNotifiy || !isScreenOn)
                OliveHelper.generateMessageNotification(context, idRoom, sender, alert, 1);
        } else
        if (pushType == 2) {
//            2: new room created
//            {"create_date": "2015-03-21 15:37:26.414544+00:00", "last_msg": "", "creator": {"username": "ujlikes@naver.com", "phone": "01057106299", "update_date": "2015-03-21 15:36:39+00:00", "picture": "/media/profile/IMG_0329_2_3.jpg"}, "push_type": 2, "room_attendants": "ujlikes5@naver.com,ujlikes@naver.com", "id": 25}
            Intent syncIntent = new Intent(context, SyncNetworkService.class)
                    .setAction(SyncNetworkService.INTENT_ACTION_SYNC_ROOMS_LIST);
            context.startService(syncIntent);
        } else
        if (pushType == 3) {
//            3: someone left room
//            {"push_type": 3, "room_id": 24, "user": {"username": "ujlikes@naver.com", "phone": "01057106299", "update_date": "2015-03-21 07:29:14+00:00", "picture": "/media/profile/IMG_0329_2_3.jpg"}}
            Intent syncIntent = new Intent(context, SyncNetworkService.class)
                    .setAction(SyncNetworkService.INTENT_ACTION_SYNC_ROOMS_LIST);
            context.startService(syncIntent);
        } else
        if (pushType == 4) {
//            4: 중복 로그인
//            -> 아직 미구현
            RESTApiManager restManager = AWSQueryManager.getInstance();
            if (!restManager.verifyDevice()) {
                restManager.signOut();
                Toast.makeText(context, R.string.toast_failed_to_sign_in, Toast.LENGTH_SHORT).show();
                Intent newIntent = new Intent(context, SplashActivity.class);
                newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            }
        } else
        if (pushType == 5) {
//            5: profile update
//            {"username": "ujlikes@naver.com", "phone": "01057106299", "push_type": 5, "update_date": "2015-03-21 15:36:39+00:00", "picture": "/media/profile/IMG_0329_2_3.jpg"}
            Intent syncIntent = new Intent(context, SyncNetworkService.class)
                    .setAction(SyncNetworkService.INTENT_ACTION_SYNC_FRIENDS_LIST);
            context.startService(syncIntent);
        } else {
            throw new IllegalArgumentException("Invalid push messag");
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
