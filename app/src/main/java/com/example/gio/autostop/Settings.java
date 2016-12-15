package com.example.gio.autostop;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.Marker;


public class Settings {
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    public static void initialize(Context context){
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        editor=sharedPreferences.edit();
    }
    private static void commit(){
        editor.commit();
    }
    public static void saveBoolean(String key,Boolean value){
        editor.putBoolean(key,value);
        commit();
    }
    public static boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }
    public static void saveLong(String key,Long value){
        editor.putLong(key,value);
        commit();
    }
    public static String getString(String key) {
        return sharedPreferences.getString(key,"error");
    }
    public static void saveString(String key,String value){
        editor.putString(key,value);
        commit();
    }

    public static Long getLong(String key){
        return sharedPreferences.getLong(key,0);
    }

}
