package com.tyrantapp.olive.helper;

import java.util.List;

import com.tyrantapp.olive.ConversationActivity;
import com.tyrantapp.olive.MainActivity;
import com.tyrantapp.olive.R;
import com.tyrantapp.olive.SettingActivity;
import com.tyrantapp.olive.providers.OliveContentProvider.RecipientColumns;
import com.tyrantapp.olive.services.GCMIntentService;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;


public class OliveHelper {
	private final static String TAG = "OliveHelper";
	
	public static String getForegroundActivity(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> taskInfo = am.getRunningTasks(1);
		ComponentName componentInfo = taskInfo.get(0).topActivity;
		return componentInfo.getClassName();		
	}
	
	public static String getForegroundPackage(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> taskInfo = am.getRunningTasks(1);
		ComponentName componentInfo = taskInfo.get(0).topActivity;
		return componentInfo.getPackageName();		
	}
	
	public static void generateNotification(Context context, long lRecipientId, String message) {
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        
        String recipientName = getRecipientName(context, lRecipientId);
                
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra(ConversationActivity.EXTRA_RECIPIENT_ID, lRecipientId);
        
        android.util.Log.d(TAG, "Receive by push from " + recipientName + " (" + lRecipientId + ")");
        
        android.util.Log.d(TAG, "GCMIntentService.intent = " + notificationIntent.getExtras().toString());
        
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        
        Builder builder = new NotificationCompat.Builder(context).setWhen(when)
                .setSmallIcon(icon).setContentTitle(context.getString(R.string.app_name))
                .setContentText(recipientName + " : " + message).setContentIntent(intent).setTicker(recipientName + " : " + message)
                .setAutoCancel(true);
        
        if (PreferenceHelper.getBooleanPreferences(context, SettingActivity.OLIVE_PREF_NOTIFICATION, true)) {
        	// Notification ON!
        	//builder.setVibrate(new long[]{200, 100, 200, 100});
        	builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }
        
        Notification notification = builder.getNotification();
	    notificationManager.notify(GCMIntentService.NOTIFICATION_ID, notification);
	}

	public static void removeNotification(Context context) {
		// clear Notification
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(GCMIntentService.NOTIFICATION_ID);
	}
	
	public static long getRecipientId(Context context, String recipientName) {
		long lRecipientId = -1;
		
		if (recipientName != null) {
			Cursor cursor = context.getContentResolver().query(
					RecipientColumns.CONTENT_URI, 
					new String[] { RecipientColumns._ID, },
					RecipientColumns.USERNAME + "=?",
					new String[] { recipientName, },
					null);
			
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				lRecipientId = cursor.getLong(cursor.getColumnIndex(RecipientColumns._ID));
			}
		}		
		
		return lRecipientId;
	}

	public static String getRecipientName(Context context, long recipientId) {
		String pszRecipientName = null;
		
		if (recipientId >= 0) {
			Cursor cursor = context.getContentResolver().query(
					RecipientColumns.CONTENT_URI, 
					new String[] { RecipientColumns.USERNAME, },
					RecipientColumns._ID + "=?",
					new String[] { String.valueOf(recipientId), },
					null);
			
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				pszRecipientName = cursor.getString(cursor.getColumnIndex(RecipientColumns.USERNAME));
			}
		}		
		
		return pszRecipientName;
	}
}
