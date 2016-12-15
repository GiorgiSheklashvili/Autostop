package com.example.gio.autostop.User_Interface.fragments;


import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.gio.autostop.R;
import com.example.gio.autostop.Settings;

public class DestinationFragment extends Fragment {
    public static final String PREFS_NAME = "DoNotAskAgain";
    public CheckBox dontShowAgain;

    public DestinationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_destination, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater DialogInflater = LayoutInflater.from(getActivity());
        View messageLayout = DialogInflater.inflate(R.layout.checkbox, null);
        dontShowAgain = (CheckBox) messageLayout.findViewById(R.id.skip);
        builder.setMessage(R.string.destination)
                .setTitle(R.string.dialog_title)
                .setView(messageLayout);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String checkBoxResult = "NOT checked";
                if (dontShowAgain.isChecked()) checkBoxResult = "checked";
                Settings.saveString("skipMessage", checkBoxResult);
                dialog.cancel();
            }
        });
        String skipMessage = Settings.getString("skipMessage");
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);//prevent to get dismissed with back key
        dialog.setCanceledOnTouchOutside(false);//prevent to get dismissed on outside click
        if (!skipMessage.equals("checked"))
            dialog.show();
    }

}
