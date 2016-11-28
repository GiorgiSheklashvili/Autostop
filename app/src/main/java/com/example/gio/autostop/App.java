package com.example.gio.autostop;

import android.app.Application;


public class App extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        Settings.initialize(this);
    }
}
