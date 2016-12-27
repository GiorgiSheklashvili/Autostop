package com.example.gio.autostop;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;

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

    public static void checkGps(Context context) {
        final LocationManager manager = (LocationManager) App.getAppContext().getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps(context);
        }
    }

    private static void buildAlertMessageNoGps(Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        App.getAppContext().startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


}
