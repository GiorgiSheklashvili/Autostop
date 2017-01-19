package com.example.gio.autostop.user_interface.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gio.autostop.helper.Constants;
import com.example.gio.autostop.R;
import com.example.gio.autostop.helper.OnSwipeTouchListener;


public class MainFragment extends Fragment {
    public ImageView goImageView;
    public TextView driver;
    public TextView passenger;
    View slideView;
    LinearLayout chooseButtonLayout;
    Boolean choose = false;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_main,container,false);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        slideView = view.findViewById(R.id.slide_view);
        passenger = (TextView) view.findViewById(R.id.passenger);
        driver = (TextView) view.findViewById(R.id.driver);
        chooseButtonLayout = (LinearLayout) view.findViewById(R.id.chooseType);
        goImageView = (ImageView) view.findViewById(R.id.imageGo);
        goImageView.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               if (checkIfInternetIsAvailable(v.getContext()))
                                               {
                                                   MapViewFragment mapViewFragment=new MapViewFragment();
                                                   Bundle args=new Bundle();
                                                   args.putBoolean(Constants.chosenMode,choose);
                                                   mapViewFragment.setArguments(args);
                                                   FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                                   transaction.replace(R.id.fragment_container_main, mapViewFragment);
                                                   transaction.addToBackStack(null);
                                                   transaction.commit();
                                               }
                                               else
                                                   Toast.makeText(v.getContext(), "No Internet", Toast.LENGTH_LONG).show();
                                           }
                                       }
        );

        passenger.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewGroup.LayoutParams params = slideView.getLayoutParams();
                params.height = passenger.getHeight();
                params.width = passenger.getWidth();
                slideView.setLayoutParams(params);
                passenger.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
        passenger.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                slideView.animate().x(0);
                choose = false;
                return false;
            }
        });
        driver.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                slideView.animate().x(slideView.getWidth());
                choose = true;
                return false;
            }
        });
        chooseButtonLayout.setOnTouchListener(new OnSwipeTouchListener(view.getContext()) {
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                slideView.animate().x(slideView.getWidth());
                choose = true;
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                slideView.animate().x(0);
                choose = false;
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }
    public void displayMap(boolean choose) {
//        Intent intent = new Intent(this, MapsActivity.class);
//        intent.putExtra(Constants.chosenMode, choose);
//        startActivity(intent);
    }

    public boolean checkIfInternetIsAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

}
