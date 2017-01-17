package com.example.gio.autostop.user_interface.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

import com.example.gio.autostop.AutostopSettings;
import com.example.gio.autostop.R;


public class DriverFragment extends Fragment {
    public static Button unCheckDriver;
    private Spinner spinner;
    public static boolean createdDriverFragment = false;

    public DriverFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_driver, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unCheckDriver = (Button) view.findViewById(R.id.button4);
        unCheckDriver.setClickable(AutostopSettings.getBoolean("mCheckOutForDriverButton"));
        unCheckDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AutostopSettings.saveBoolean("mCheckOutForDriverButton", false);
                AutostopSettings.saveBoolean("mCheckOutButton", false);
                unCheckDriver.setClickable(false);
                MapFunctionsFragment.deleteMarkers();
                AutostopSettings.saveBoolean("carIconAlreadyCreated",false);
            }
        });

//
//        spinner = (Spinner) view.findViewById(R.id.spinner);
//        String[] items = new String[]{"1", "2", "3", "4", "5+"};
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, items);
//        spinner.setAdapter(adapter);
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//
//            @Override
//            public void onItemSelected(AdapterView<?> adapter, View v,
//                                       int position, long id) {
//                // On selecting a spinner item
//                String item = adapter.getItemAtPosition(position).toString();
//
//                // Showing selected spinner item
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });
        createdDriverFragment=true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MapFunctionsFragment.chosenMode1=false;
    }

    @Override
    public void onResume() {
        super.onResume();
        unCheckDriver.setClickable(AutostopSettings.getBoolean("mCheckOutForDriverButton"));
    }
}
