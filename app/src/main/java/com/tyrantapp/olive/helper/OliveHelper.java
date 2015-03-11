package com.tyrantapp.olive.helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import com.tyrantapp.olive.R;
import com.tyrantapp.olive.SettingActivity;
import com.tyrantapp.olive.SplashActivity;
import com.tyrantapp.olive.configuration.Constants;
import com.tyrantapp.olive.network.RESTApiManager;
import com.tyrantapp.olive.service.GCMIntentService;
import com.tyrantapp.olive.service.SyncNetworkService;
import com.tyrantapp.olive.util.SharedVariables;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

public class OliveHelper {
    private final static String TAG = "OliveHelper";

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public static final String MIMETYPE_TEXT = "text/plain";
    public static final String MIMETYPE_IMAGE = "image/png";
    public static final String MIMETYPE_VIDEO = "video/mpeg";
    public static final String MIMETYPE_AUDIO = "audio/ogg";
    public static final String MIMETYPE_GEOLOCATE = "application/x-geolocation";
    public static final String MIMETYPE_EMOJI = "image/x-emoji";


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
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

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
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
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

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
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
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
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
                pszRet = MIMETYPE_EMOJI;
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
        else if (MIMETYPE_EMOJI.equals(mimetype))
            nRet = 5;

        return nRet;
    }


    public static boolean isNetworkAvailable(Context context) {
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

    public static void showSoftInputMethod(Context context, EditText focusView) {
        if (focusView != null) {
            focusView.requestFocus();
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(focusView, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static void hideSoftInputMethod(Context context, EditText focusView) {
        if (focusView != null) {
            focusView.setText(null);
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
        }
    }

    public static Bitmap makeCircleBitmap(Bitmap bitmap) {
        int cropSize = (bitmap.getWidth() > bitmap.getHeight()) ? bitmap.getHeight() : bitmap.getWidth();
        Bitmap output = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        int offsetX = (int) ((bitmap.getWidth() - cropSize) * .5f);
        int offsetY = (int) ((bitmap.getHeight() - cropSize) * .5f);
        final Rect inRect = new Rect(offsetX, offsetY, cropSize + offsetX, cropSize + offsetY);
        final Rect outRect = new Rect(0, 0, cropSize, cropSize);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(cropSize / 2, cropSize / 2, cropSize / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, inRect, outRect, paint);
        return output;
    }

    private static File getExternalFilesDir(Context context) {
        File file = new File("/storage/emulated/legacy/Android/data/" + context.getPackageName() + "/files"); // can not get valid-storage path. (like galaxy)
        if (!file.exists()) file = context.getExternalFilesDir(null);
        if (!file.exists()) file = context.getFilesDir(); // if no have external storage
        return file;
    }

    public static String getProfileImageDir(Context context) {
        return getExternalFilesDir(context) + "/media/profile/";
    }

    public static String getProfileImagePath(Context context) {
        return getProfileImageDir(context) + "profile.jpg";
    }

    public static String getMessageMediaDir(Context context) {
        return getExternalFilesDir(context) + "/media/messages/";
    }

    public static String getPathLastSegment(String filePath) {
        if (filePath != null) {
            String[] separatedPath = filePath.split("\\/");
            return separatedPath[separatedPath.length - 1];
        }
        return null;
    }

    public static String getUpperPath(String filePath) {
        return filePath.replace(getPathLastSegment(filePath), "").replace("//", "/").replace("\\\\", "\\");
    }

    public static String getSmallThumbFilename(String filePath) {
        // 128 x 128
        if (filePath != null) {
            int dotPos = filePath.lastIndexOf(".");
            String filename = filePath.substring(0, dotPos);
            String extension = filePath.substring(dotPos);
            return filename + "_s" + extension;
        }
        return null;
    }

    public static String getMediumThumbFilename(String filePath) {
        // 256 x 256
        if (filePath != null) {
            int dotPos = filePath.lastIndexOf(".");
            String filename = filePath.substring(0, dotPos);
            String extension = filePath.substring(dotPos);
            return filename + "_m" + extension;
        }
        return null;
    }

    public static String getLargeThumbFilename(String filePath) {
        // 512 x 512
        if (filePath != null) {
            int dotPos = filePath.lastIndexOf(".");
            String filename = filePath.substring(0, dotPos);
            String extension = filePath.substring(dotPos);
            return filename + "_l" + extension;
        }
        return null;
    }

    public static String downloadCachedMedia(String URL, String basePath) {
        if (URL != null) {
            String filename = OliveHelper.getPathLastSegment(URL);
            String filePath = basePath + filename;
            if (!(new File(filePath).exists())) {
                // thumbs
                boolean bDownloaded = false;
                if (downloadMedia(getSmallThumbFilename(URL), basePath) != null) bDownloaded = true;
                if (downloadMedia(getMediumThumbFilename(URL), basePath) != null)
                    bDownloaded = true;
                if (downloadMedia(getLargeThumbFilename(URL), basePath) != null) bDownloaded = true;
                if (!bDownloaded) {
                    filePath = downloadMedia(URL, basePath);
                }
            }
            return filePath;
        }
        return null;
    }

    public static String downloadMedia(String URL, String basePath) {
        if (URL != null) {
            try {
                String filename = OliveHelper.getPathLastSegment(URL);
                String filePath = basePath + filename;
                if (!(new File(filePath).exists())) {
                    InputStream is = (InputStream) new java.net.URL(URL).getContent();
                    Bitmap bmpInfo = BitmapFactory.decodeStream(is);
                    OliveHelper.saveImage(bmpInfo, Bitmap.CompressFormat.JPEG, 70, filePath);
                }
                return filePath;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String downloadUserProfile(Context context, String URL) {
        if (URL != null) {
            try {
                String filePath = getProfileImagePath(context);
                if (!(new File(filePath).exists())) {
                    InputStream is = (InputStream) new java.net.URL(URL).getContent();
                    Bitmap bmpInfo = BitmapFactory.decodeStream(is);
                    OliveHelper.saveImage(bmpInfo, Bitmap.CompressFormat.JPEG, 70, filePath);
                }
                return filePath;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Bitmap getCachedImage(String path) {
        return getCachedImage(path, 0);
    }

    public static Bitmap getCachedImage(String path, int minLevel) {
        String smallPath = getSmallThumbFilename(path);
        String mediumPath = getMediumThumbFilename(path);
        String largePath = getLargeThumbFilename(path);

        Bitmap bmpRet = null;
        if (path != null) {
            switch (minLevel) {
                case 0: // Small > Medium > Large > Original
                    if (bmpRet == null && new File(smallPath).exists())
                        bmpRet = BitmapFactory.decodeFile(smallPath);
                case 1:
                    if (bmpRet == null && new File(mediumPath).exists())
                        bmpRet = BitmapFactory.decodeFile(mediumPath);
                case 2:
                    if (bmpRet == null && new File(largePath).exists())
                        bmpRet = BitmapFactory.decodeFile(largePath);
                case 3:
                    if (bmpRet == null && new File(path).exists())
                        bmpRet = BitmapFactory.decodeFile(path);
                    break;
                default:
                    return getCachedImage(path);
            }
        }
        return bmpRet;
    }

    public static boolean saveFile(InputStream is, String outFilePath) {
        boolean bRet = false;

        try {
            File file = new File(outFilePath);
            if (!file.exists()) {
                String outDir = getUpperPath(outFilePath);
                new File(outDir).mkdirs();
                file.createNewFile();
            }

            OutputStream os = new FileOutputStream(file);

            final byte[] buffer = new byte[1024];
            int read;

            while ((read = is.read(buffer)) != -1) os.write(buffer, 0, read);
            os.flush();
            os.close();

            bRet = true;
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        return bRet;
    }

    public static boolean saveImage(Bitmap bitmap, Bitmap.CompressFormat format, int quality, String outPath) {
        boolean bRet = false;
        OutputStream out = null;

        try {
            File outFile = new File(outPath);
            if (!outFile.exists()) {
                String outDir = getUpperPath(outPath);
                new File(outDir).mkdirs();
                outFile.createNewFile();
            }

            out = new FileOutputStream(outFile);

            if (out != null) {
                // copy
                bitmap.compress(format, quality, out);
                bRet = true;

                out.close();
                out = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bRet;
    }

    /**
     * 받은 JSON 객체를 파싱하는 메소드
     * @param page
     * @return
     */
    public static HashMap<String, String> JSONParser(String jsonString) {
        HashMap<String, String> mapParsed = new HashMap<String, String>();

        android.util.Log.i("서버에서 받은 전체 내용 : ", jsonString);

        ArrayList<String> dequeKeys = new ArrayList<String>();
        ArrayList<String> stackName = new ArrayList<String>();
        ArrayList<JSONObject> stackObj = new ArrayList<JSONObject>();
        ArrayList<Integer> stackCount = new ArrayList<Integer>();
        try {
            JSONObject obj = new JSONObject(jsonString);
            stackObj.add(obj);
            stackCount.add(obj.length());

            Iterator<String> keys = obj.keys();
            while (keys.hasNext()) dequeKeys.add(keys.next());

            while (!dequeKeys.isEmpty()) {
                int idxLast = dequeKeys.size() - 1;
                String key = dequeKeys.get(idxLast);

                if (stackCount.get(stackCount.size() - 1) > 0) {
                    dequeKeys.remove(idxLast);
                    stackCount.set(stackCount.size() - 1, stackCount.get(stackCount.size() - 1) - 1);

                    obj = stackObj.get(stackObj.size() - 1);
                    JSONObject subObj = obj.optJSONObject(key);

                    if (subObj != null) {
                        stackName.add(key);
                        stackObj.add(subObj);
                        stackCount.add(subObj.length());

                        Iterator<String> subKeys = subObj.keys();
                        while (subKeys.hasNext()) dequeKeys.add(subKeys.next());
                    } else {
                        String fullname = new String();
                        for (String name : stackName) fullname += name + "::";
                        mapParsed.put(fullname + key, obj.getString(key));
                    }
                } else {
                    stackName.remove(stackName.size() - 1);
                    stackObj.remove(stackObj.size() - 1);
                    stackCount.remove(stackCount.size() - 1);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mapParsed;
    }

    public static ArrayList<HashMap<String, String>> JSONArrayParser(String jsonString) {
        if (jsonString.startsWith("[") && jsonString.endsWith("]")) {
            ArrayList<HashMap<String, String>> arrayParsed = new ArrayList<HashMap<String, String>>();
            boolean bFind = true;
            int start = 0;
            do {
                int begin = jsonString.indexOf("{", start);
                if (begin > 0) {
                    int stackCount = 1;
                    int end = begin + 1;
                    while(stackCount > 0) {
                        int candidate01 = jsonString.indexOf("{", end);
                        int candidate02 = jsonString.indexOf("}", end);
                        if ((candidate01 >= 0) && (candidate01 < candidate02)) {
                            stackCount++;
                            end = candidate01 + 1;
                        } else {
                            stackCount--;
                            end = candidate02 + 1;
                        }
                    }
                    String separate = jsonString.substring(begin, end);
                    arrayParsed.add(JSONParser(separate));
                    //jsonString = jsonString.replace(separate, "");
                    start = end;
                } else bFind = false;
            } while (bFind);
            return arrayParsed;
        } else return null;
    }

    public static boolean copyAssets(Context context, String directory, boolean internal) {
        boolean bRet = false;
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list("presets");
        } catch (IOException e) {
            android.util.Log.e(TAG, "Failed to get asset file list.", e);
        }
        for(String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(directory + "/" + filename);
                File outFile = null;
                if (internal) {
                    new File(context.getFilesDir() + "/" + directory).mkdirs();
                    outFile = new File(context.getFilesDir(), directory + "/" + filename);
                } else {
                    new File(context.getExternalFilesDir(null) + "/" + directory).mkdirs();
                    outFile = new File(context.getExternalFilesDir(null), directory + "/" + filename);
                }
                if (!outFile.exists()) {
                    out = new FileOutputStream(outFile);
                    copyFile(in, out);
                    bRet = true;
                }
            } catch(IOException e) {
                android.util.Log.e(TAG, "Failed to copy asset file: " + directory + "/" + filename, e);
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
        return bRet;
    }

    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}
