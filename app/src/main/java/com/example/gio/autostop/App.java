package com.example.gio.autostop;

import android.app.Application;
import android.content.Context;

import com.example.gio.autostop.helper.AutostopSettings;


public class App extends Application{
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        AutostopSettings.initialize(this);
        App.context = getApplicationContext();
    }
    public static Context getAppContext() {
        return App.context;
    }
}
