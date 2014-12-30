package com.tyrantapp.olive.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceHelper {
	public final static String OLIVE_PREFERENCE = "olive_preference";
	
    // 값 불러오기
    public static int getIntPreferences(Context context, String key, int defValue){
        SharedPreferences pref = context.getSharedPreferences(OLIVE_PREFERENCE, Context.MODE_PRIVATE);
        return pref.getInt(key, defValue);
    }
    
    public static float getFloatPreferences(Context context, String key, float defValue){
        SharedPreferences pref = context.getSharedPreferences(OLIVE_PREFERENCE, Context.MODE_PRIVATE);
        return pref.getFloat(key, defValue);
    }
    
    public static boolean getBooleanPreferences(Context context, String key, boolean defValue){
        SharedPreferences pref = context.getSharedPreferences(OLIVE_PREFERENCE, Context.MODE_PRIVATE);
        return pref.getBoolean(key, defValue);
    }
     
    public static long getLongPreferences(Context context, String key, long defValue){
        SharedPreferences pref = context.getSharedPreferences(OLIVE_PREFERENCE, Context.MODE_PRIVATE);
        return pref.getLong(key, defValue);
    }
    
    public static String getStringPreferences(Context context, String key, String defValue){
        SharedPreferences pref = context.getSharedPreferences(OLIVE_PREFERENCE, Context.MODE_PRIVATE);
        return pref.getString(key, defValue);
    }
     
    // 값 저장하기
    public static void saveIntPreferences(Context context, String key, int value){
        SharedPreferences pref = context.getSharedPreferences(OLIVE_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, value);
        editor.commit();
    }
    
    public static void saveFloatPreferences(Context context, String key, float value){
        SharedPreferences pref = context.getSharedPreferences(OLIVE_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putFloat(key, value);
        editor.commit();
    }
    
    public static void saveBooleanPreferences(Context context, String key, boolean value){
        SharedPreferences pref = context.getSharedPreferences(OLIVE_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    
    public static void saveLongPreferences(Context context, String key, long value){
        SharedPreferences pref = context.getSharedPreferences(OLIVE_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(key, value);
        editor.commit();
    }
    
    public static void saveStringPreferences(Context context, String key, String value){
        SharedPreferences pref = context.getSharedPreferences(OLIVE_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }
    // 값(Key Data) 삭제하기
    public static void removePreferences(Context context, String key){
        SharedPreferences pref = context.getSharedPreferences(OLIVE_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(key);
        editor.commit();
    }
     
    // 값(ALL Data) 삭제하기
    public static void removeAllPreferences(Context context){
        SharedPreferences pref = context.getSharedPreferences(OLIVE_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }
}
