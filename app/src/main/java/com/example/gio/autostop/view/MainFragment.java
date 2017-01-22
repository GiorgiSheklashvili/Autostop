package com.example.gio.autostop.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gio.autostop.helper.AutostopSettings;
import com.example.gio.autostop.R;
import com.example.gio.autostop.helper.OnSwipeTouchListener;
import com.example.gio.autostop.presenter.MapPresenter;


public class MainFragment extends Fragment {
    private TextView driver;
    private TextView passenger;
    private View slideView;
    private LinearLayout chooseButtonLayout;
    private Boolean choose;
    private ImageView goImageView;
    private onChooseModeListener mCallback;

    public interface onChooseModeListener{
        void notifyChosenMode(boolean chosen);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback=(onChooseModeListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        choose=false;
        slideView = view.findViewById(R.id.slide_view);
        passenger = (TextView) view.findViewById(R.id.passenger);
        driver = (TextView) view.findViewById(R.id.driver);
        chooseButtonLayout = (LinearLayout) view.findViewById(R.id.chooseType);
        goImageView = (ImageView) view.findViewById(R.id.imageGo);
        goImageView.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               if (AutostopSettings.checkIfInternetIsAvailable(v.getContext())) {
                                                   mCallback.notifyChosenMode(choose);
                                               } else {
                                                   Toast.makeText(v.getContext(), "No Internet", Toast.LENGTH_LONG).show();
                                               }
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

}
