package com.tyrantapp.olive.util;


import java.util.HashMap;
import java.util.Set;

public class SharedVariables {
    private final static String TAG = SharedVariables.class.getSimpleName();

    private static HashMap<Object, Object> mMap = new HashMap<Object, Object>();

    public static int size() {
        return mMap.size();
    }

    public static boolean isEmpty() {
        return mMap.isEmpty();
    }

    public static boolean containsKey(Object key) {
        return mMap.containsKey(key);
    }

    public static Set<Object> keySet() {
        return mMap.keySet();
    }

    public static void clear() {
        mMap.clear();
    }

    public static void remove(String key) {
        mMap.remove(key);
    }

    public static void put(Object key, int value) {
        mMap.put(key, value);
    }

    public static void put(Object key, float value) {
        mMap.put(key, value);
    }

    public static void put(Object key, boolean value) {
        mMap.put(key, value);
    }

    public static void put(Object key, long value) {
        mMap.put(key, value);
    }

    public static void put(Object key, double value) {
        mMap.put(key, value);
    }

    public static void put(Object key, byte value) {
        mMap.put(key, value);
    }

    public static void put(Object key, String value) {
        mMap.put(key, value);
    }

    public static void put(Object key, Object value) {
        mMap.put(key, value);
    }

    public static void put(Object key, int[] values) {
        mMap.put(key, values);
    }

    public static void put(Object key, float[] values) {
        mMap.put(key, values);
    }

    public static void put(Object key, boolean[] values) {
        mMap.put(key, values);
    }

    public static void put(Object key, long[] values) {
        mMap.put(key, values);
    }

    public static void put(Object key, double[] values) {
        mMap.put(key, values);
    }

    public static void put(Object key, byte[] values) {
        mMap.put(key, values);
    }

    public static void put(Object key, String[] values) {
        mMap.put(key, values);
    }

    public static void put(Object key, Object[] values) {
        mMap.put(key, values);
    }

    public static int getInt(Object key) {
        Integer value = (Integer)mMap.get(key);
        return (value != null) ? value : -1;
    }

    public static float getFloat(Object key) {
        Float value = (Float)mMap.get(key);
        return (value != null) ? value : -1;
    }

    public static boolean getBoolean(Object key) {
        Boolean value = (Boolean)mMap.get(key);
        return (value != null) ? value : false;
    }

    public static long getLong(Object key) {
        Long value = (Long)mMap.get(key);
        return (value != null) ? value : -1;
    }

    public static double getDouble(Object key) {
        Double value = (Double)mMap.get(key);
        return (value != null) ? value : -1;
    }

    public static byte getByte(Object key) {
        Byte value = (Byte)mMap.get(key);
        return (value != null) ? value : -1;
    }

    public static String getString(Object key) {
        return (String)mMap.get(key);
    }

    public static Object get(Object key) {
        return mMap.get(key);
    }

    public static int[] getIntArray(Object key) {
        return (int[])mMap.get(key);
    }

    public static float[] getFloatArray(Object key) {
        return (float[])mMap.get(key);
    }

    public static boolean[] getBooleanArray(Object key) {
        return (boolean[])mMap.get(key);
    }

    public static long[] getLongArray(Object key) {
        return (long[])mMap.get(key);
    }

    public static double[] getDoubleArray(Object key) {
        return (double[])mMap.get(key);
    }

    public static byte[] getByteArray(Object key) {
        return (byte[])mMap.get(key);
    }

    public static String[] getStringArray(Object key) {
        return (String[])mMap.get(key);
    }

    public static Object[] getObjectArray(Object key) {
        return (Object[])mMap.get(key);
    }
}
