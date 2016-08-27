package com.example.gio.autostop;

import android.content.Context;
import android.content.Intent;
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



public class AddressFragment extends Fragment {
    protected TextView mLocationAddressTextView;
    protected String mAddressOutput;
    private AddressResultReceiver mResultReceiver;
    private ProgressBar mProgressBar;
    protected boolean mAddressRequested;
    private  MapsActivity mapsActivity;
    protected static final String ADDRESS_REQUESTED_KEY = "address-request-pending";
    protected static final String LOCATION_ADDRESS_KEY = "location-address";
    public AddressFragment() {
        // Required empty public constructor
    }
//Receiver for data sent from FetchAddressIntentService.
    class AddressResultReceiver extends ResultReceiver{
        private int CREATOR;
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            displayAddressOutput();
            mAddressRequested = false;
            updateUIWidgets();
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Set defaults
        mResultReceiver = new AddressResultReceiver(new Handler());
        mAddressRequested = false;
        mAddressOutput = " ";
        updateValuesFromBundle(savedInstanceState);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_address_fragment, container, false);
    }
    public void setMapsActivity(MapsActivity mapsActivity){
        this.mapsActivity=mapsActivity;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ADDRESS_REQUESTED_KEY, mAddressRequested);
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
    public void displayAddressOutput(){
    mLocationAddressTextView.setText(mAddressOutput);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mLocationAddressTextView=(TextView) view.findViewById(R.id.address);
        updateUIWidgets();
    }

    public void startIntentService() {
        Intent intent = new Intent(getContext(), FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mapsActivity.mCurrentLocation);
        mapsActivity.startService(intent);

    }
    public void fetchAddressHandler() {
        if (mapsActivity.mGoogleApiClient.isConnected() && mapsActivity.mCurrentLocation != null) {
            mAddressRequested = true;
            updateUIWidgets();
            startIntentService();
            }


    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(ADDRESS_REQUESTED_KEY)) {
                mAddressRequested = savedInstanceState.getBoolean(ADDRESS_REQUESTED_KEY);
            }
            if (savedInstanceState.keySet().contains(LOCATION_ADDRESS_KEY)) {
                mAddressOutput = savedInstanceState.getString(LOCATION_ADDRESS_KEY);
                displayAddressOutput();
            }

        }

    }
    private void updateUIWidgets() {

        if (mAddressRequested) {
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
        } else {
            mProgressBar.setVisibility(ProgressBar.GONE);
        }
    }
}
