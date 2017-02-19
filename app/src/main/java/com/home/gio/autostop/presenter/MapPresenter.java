package com.home.gio.autostop.presenter;

import android.app.Activity;
import android.content.Context;
import android.location.Location;

import com.home.gio.autostop.MVP_Interfaces;
import com.home.gio.autostop.model.Position;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.home.gio.autostop.interfaces.MapRequestRequestCallback;

import java.lang.ref.WeakReference;

public class MapPresenter implements MVP_Interfaces.ProvidedPresenterOps, MVP_Interfaces.RequiredPresenterOps {

    private WeakReference<MVP_Interfaces.RequiredViewOps> mView;
    private MVP_Interfaces.ProvidedModelOps mModel;

    public MapPresenter(MVP_Interfaces.RequiredViewOps view) {
        this.mView = new WeakReference<>(view);
    }

    public void setModel(MVP_Interfaces.ProvidedModelOps model) {
        this.mModel = model;
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        mView = null;
        mModel.onDestroy(isChangingConfiguration);
        if (!isChangingConfiguration) {
            mModel = null;
        }
    }

    private MVP_Interfaces.RequiredViewOps getView() throws NullPointerException {
        if (mView != null)
            return mView.get();
        else
            throw new NullPointerException("View is unavailable");
    }


    @Override
    public void setView(MVP_Interfaces.RequiredViewOps view) {
        mView = new WeakReference<>(view);
    }

    @Override
    public void notifyToDeleteMarkers(Marker markerForDeletion, Context context) {
        mModel.deleteMarkers(markerForDeletion, context);
    }

    @Override
    public LatLng checkInCurrentPosition(Context context) {
        return mModel.checkInCurrentPosition(context);
    }

    @Override
    public void setUpMapDemand(Activity activity, MapRequestRequestCallback callback) {
        mModel.setUpMap(activity, callback);
    }

    @Override
    public Position searchList(Double latitude, Double longitude) {
        return mModel.searchList(latitude, longitude);
    }

    @Override
    public String getWifiAddress() {
        return mModel.getWifiMacAddress();
    }


    @Override
    public void notifyDeleteDMarkers() {
        getView().notifyDeletedMarkers();
    }


    @Override
    public Location getLastKnownLocation(Context context) {
        return getView().notifyGetLastKnownLocation(context);
    }

    @Override
    public void uploadingPosition(Context context, LatLng destinationPosition, Boolean chosenMode) {
        mModel.uploadingPosition(context, destinationPosition, chosenMode);
    }

    @Override
    public void gpsManagerStart(LatLng destinationPosition, Boolean chosenMode) {
        getView().gpsManagerStart(destinationPosition, chosenMode);
    }


}
