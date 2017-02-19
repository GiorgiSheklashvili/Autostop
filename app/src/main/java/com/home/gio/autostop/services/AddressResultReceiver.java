package com.home.gio.autostop.services;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.widget.TextView;

import com.home.gio.autostop.helper.Constants;

public class AddressResultReceiver extends ResultReceiver {
    private int CREATOR;
    private String mAddressOutput;
    private TextView mLocationAddressTextView;

    public TextView getmLocationAddressTextView() {
        return mLocationAddressTextView;
    }

    public void setmLocationAddressTextView(TextView mLocationAddressTextView) {
        this.mLocationAddressTextView = mLocationAddressTextView;
    }

    public String getmAddressOutput() {
        return mAddressOutput;
    }

    public void setmAddressOutput(String mAddressOutput) {
        this.mAddressOutput = mAddressOutput;
    }

    public AddressResultReceiver(Handler handler) {
        super(handler);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);
        mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
        mLocationAddressTextView.setText(mAddressOutput);
    }
}