package com.example.gio.autostop.view;


import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.gio.autostop.R;
import com.example.gio.autostop.helper.Constants;
import com.example.gio.autostop.presenter.MapPresenter;


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
        MapFragment mapFragment = new MapFragment();
        Bundle args = new Bundle();
        args.putBoolean(Constants.chosenMode, chosenMode);
        mapFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container_main, mapFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}