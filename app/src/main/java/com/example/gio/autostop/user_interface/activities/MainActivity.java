package com.example.gio.autostop.user_interface.activities;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.gio.autostop.MVP_Interfaces;
import com.example.gio.autostop.R;
import com.example.gio.autostop.helper.Constants;
import com.example.gio.autostop.user_interface.fragments.MainFragment;
import com.example.gio.autostop.user_interface.fragments.MapViewFragment;


public class MainActivity extends AppCompatActivity implements MainFragment.onChooseModeListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null){
            return;
        }
        MainFragment mainFragment = new MainFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_main, mainFragment).commit();
    }
    @Override
    public void notifyChosenMode(boolean chosenMode) {
        MapViewFragment mapViewFragment = new MapViewFragment();
        Bundle args = new Bundle();
        args.putBoolean(Constants.chosenMode, chosenMode);
        mapViewFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container_main, mapViewFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}