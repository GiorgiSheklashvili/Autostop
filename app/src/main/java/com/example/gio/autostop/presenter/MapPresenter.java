package com.example.gio.autostop.presenter;

import android.app.Activity;
import android.content.Context;
import android.location.Location;

import com.example.gio.autostop.MVP_Interfaces;
import com.example.gio.autostop.server.Positions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.example.gio.autostop.interfaces.MapRequestRequestCallback;
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
        if ( !isChangingConfiguration ) {
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
    public void notifyDropMarker(LatLng newLatLng) {
        getView().dropMarkerOnMap(newLatLng);
    }

    @Override
    public void setView(MVP_Interfaces.RequiredViewOps view) {
        mView = new WeakReference<>(view);

    }

    @Override
    public void notifyToDeleteMarkers(Marker markerForDeletion, Activity activity) {
        mModel.deleteMarkers(markerForDeletion, activity);
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
    public Positions searchList(Double latitude, Double longitude) {
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
    public void uploadingPosition(LatLng destinationPosition, Boolean chosenMode) {

    }

    @Override
    public void uploadingPosition(Activity activity, LatLng destinationPosition, Boolean chosenMode) {
        mModel.uploadingPosition(activity, destinationPosition, chosenMode);
    }

    @Override
    public void gpsManagerStart(LatLng destinationPosition, Boolean chosenMode) {
        getView().gpsManagerStart(destinationPosition, chosenMode);
    }

    @Override
    public void checkInReminderAlertMessage(Context context) {

    }

    @Override
    public void showDestinationAlertDialog() {

    }


    @Override
    public void setUpMapIfNeeded() {

    }

    @Override
    public void enableMyLocation() {

    }
}
