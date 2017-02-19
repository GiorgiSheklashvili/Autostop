package com.home.gio.autostop;

import android.app.Application;
import android.content.Context;

import com.home.gio.autostop.helper.AutostopSettings;
import com.google.firebase.FirebaseApp;


public class App extends Application{
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        AutostopSettings.initialize(this);
        App.context = getApplicationContext();
        FirebaseApp.initializeApp(this);
    }
    public static Context getAppContext() {
        return App.context;
    }
}
