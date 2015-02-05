package com.tyrantapp.olive.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import com.tyrantapp.olive.OliveApplication;
import com.tyrantapp.olive.PasscodeActivity;
import com.tyrantapp.olive.R;
import com.tyrantapp.olive.SettingActivity;
import com.tyrantapp.olive.SplashActivity;
import com.tyrantapp.olive.configuration.Constants;
import com.tyrantapp.olive.service.GCMIntentService;
import com.tyrantapp.olive.util.SharedVariables;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;

public class OliveHelper {
	private final static String TAG = "OliveHelper";

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public static final String MIMETYPE_TEXT       = "text/plain";
    public static final String MIMETYPE_IMAGE      = "image/png";
    public static final String MIMETYPE_VIDEO      = "video/mpeg";
    public static final String MIMETYPE_AUDIO      = "audio/ogg";
    public static final String MIMETYPE_GEOLOCATE  = "application/x-geolocation";
    public static final String MIMETYPE_EMOTICON   = "image/x-emoticon";


    public static String getForegroundActivityName(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> taskInfo = am.getRunningTasks(1);
		ComponentName componentInfo = taskInfo.get(0).topActivity;
		return componentInfo.getClassName();		
	}
	
	public static String getForegroundPackageName(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> taskInfo = am.getRunningTasks(1);
		ComponentName componentInfo = taskInfo.get(0).topActivity;
		return componentInfo.getPackageName();		
	}

	public static void generateMessageNotification(Context context, long idRoom, String sender, String msg, int unread) {
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(context, SplashActivity.class);

        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra(Constants.Intent.EXTRA_ROOM_ID, idRoom);

        android.util.Log.d(TAG, "Received message is " + msg + " (" + idRoom + ")");
        android.util.Log.d(TAG, "GCMIntentService.intent = " + notificationIntent.getExtras().toString());

        // Constructs the Builder object.
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT/*PendingIntent.FLAG_CANCEL_CURRENT*/);
        
        Builder builder = new NotificationCompat.Builder(context).setWhen(when)
                .setSmallIcon(icon)
                .setContentTitle(context.getString(R.string.notification_title_message))
                .setContentText(sender)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(intent)
                //.setTicker(message)
                .setAutoCancel(true);
        
        if (!PreferenceHelper.getBooleanPreferences(context, SettingActivity.OLIVE_PREF_NOTIFICATION, true)) {
        	// Notification OFF!
            builder.setDefaults(Notification.DEFAULT_LIGHTS);
            //builder.setVibrate(new long[]{200, 100, 200, 100});
            //builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }
        
        Notification notification = builder.getNotification();
	    notificationManager.notify(GCMIntentService.NOTIFICATION_ID, notification);

        SharedVariables.put(Constants.Notification.SHARED_NOTIFICATION_ROOM_ID, idRoom);
    }

	public static void removeNotification(Context context) {
		// clear Notification
        SharedVariables.remove(Constants.Notification.SHARED_NOTIFICATION_ROOM_ID);

		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(GCMIntentService.NOTIFICATION_ID);
	}

    public static boolean isEmailAddress(Context context, String email) {
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        if (!pattern.matcher(email).matches()) {
            return false;
        }
        return true;
    }

    public static boolean isPhoneNumber(String phonenumber) {
        return PhoneNumberUtils.isGlobalPhoneNumber(phonenumber);
    }

    // Sync phone number
    public static String getLineNumber(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getLine1Number();
    }

    public static String formatNumber(String number) {
        return PhoneNumberUtils.formatNumberToE164(number, Locale.getDefault().getCountry());
    }

    public static String getIMEINumber(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static long dateToLong(String date) {
        long lRet = 0;

        // reduce microsec to compatible of timestamp
        int indexOfusec = date.indexOf(".");
        if (indexOfusec >= 0) {
            date = date.replace(date.substring(indexOfusec, indexOfusec + 7), "");
        }

        try {
            lRet = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZZZZZ").parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return lRet;
    }

    public static String convertMimetype(int type) {
        String pszRet = null;
        switch (type) {
            case 0:
                pszRet = MIMETYPE_TEXT;
                break;
            case 1:
                pszRet = MIMETYPE_IMAGE;
                break;
            case 2:
                pszRet = MIMETYPE_VIDEO;
                break;
            case 3:
                pszRet = MIMETYPE_AUDIO;
                break;
            case 4:
                pszRet = MIMETYPE_GEOLOCATE;
                break;
            case 5:
                pszRet = MIMETYPE_EMOTICON;
                break;
        }
        return pszRet;
    }

    public static int convertMimetype(String mimetype) {
        int nRet = -1;
        if (MIMETYPE_TEXT.equals(mimetype))
            nRet = 0;
        else if (MIMETYPE_IMAGE.equals(mimetype))
            nRet = 1;
        else if (MIMETYPE_VIDEO.equals(mimetype))
            nRet = 2;
        else if (MIMETYPE_AUDIO.equals(mimetype))
            nRet = 3;
        else if (MIMETYPE_GEOLOCATE.equals(mimetype))
            nRet = 4;
        else if (MIMETYPE_EMOTICON.equals(mimetype))
            nRet = 5;

        return nRet;
    }


    public static boolean isConnectedNetwork(Context context) {
        boolean bNetwork = false;
        try {
            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo.State wifi = conMan.getNetworkInfo(1).getState(); // wifi
            if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
                bNetwork = true;
            }

            NetworkInfo.State mobile = conMan.getNetworkInfo(0).getState(); // mobile ConnectivityManager.TYPE_MOBILE
            if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
                bNetwork = true;
            }
        } catch (NullPointerException e) {
        }

        return bNetwork;
    }

}
