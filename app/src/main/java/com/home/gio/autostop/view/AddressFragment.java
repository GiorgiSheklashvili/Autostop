package com.home.gio.autostop.view;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.home.gio.autostop.helper.Constants;
import com.home.gio.autostop.services.FetchAddressIntentService;
import com.home.gio.autostop.R;
import com.google.android.gms.common.api.GoogleApiClient;

public class AddressFragment extends Fragment {
    public boolean AddressRequested;
    private static final String ADDRESS_REQUESTED_KEY = "address-request-pending";
    private static final String LOCATION_ADDRESS_KEY = "location-address";
    private TextView mLocationAddressTextView;
    private String mAddressOutput;
    private AddressResultReceiver mResultReceiver;
    private ProgressBar mProgressBar;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    public AddressFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Set defaults
        mResultReceiver = new AddressResultReceiver(new Handler());
        AddressRequested = false;
        mAddressOutput = " ";
        updateValuesFromBundle(savedInstanceState);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_address, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ADDRESS_REQUESTED_KEY, AddressRequested);
        outState.putString(LOCATION_ADDRESS_KEY, mAddressOutput);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mLocationAddressTextView = (TextView) view.findViewById(R.id.address1);
        updateUIWidgets();
    }

    public void displayAddressOutput() {
        mLocationAddressTextView.setText(mAddressOutput);
    }

    public void setVariables(Location currentLocation, GoogleApiClient googleApiClient) {
        this.mCurrentLocation =currentLocation;
        this.mGoogleApiClient=googleApiClient;
    }

    public void startIntentService() {
        Intent intent = new Intent(getContext(), FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mCurrentLocation);
        getActivity().startService(intent);

    }

    public void fetchAddressHandler() {
        if (mGoogleApiClient.isConnected() && mCurrentLocation != null) {
            AddressRequested = true;
            updateUIWidgets();
            startIntentService();
        }
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(ADDRESS_REQUESTED_KEY)) {
                AddressRequested = savedInstanceState.getBoolean(ADDRESS_REQUESTED_KEY);
            }
            if (savedInstanceState.keySet().contains(LOCATION_ADDRESS_KEY)) {
                mAddressOutput = savedInstanceState.getString(LOCATION_ADDRESS_KEY);
                displayAddressOutput();
            }

        }

    }

    private void updateUIWidgets() {

        if (AddressRequested) {
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
        } else {
            mProgressBar.setVisibility(ProgressBar.GONE);
        }
    }

    //Receiver for data sent from FetchAddressIntentService.
    class AddressResultReceiver extends ResultReceiver {
        private int CREATOR;

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            displayAddressOutput();
            AddressRequested = false;
            updateUIWidgets();
        }
    }
}
