package com.example.gio.autostop;


import android.app.Activity;
import android.content.Context;
import android.location.Location;

import com.example.gio.autostop.server.Positions;
import com.example.gio.autostop.interfaces.MapRequestRequestCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class MVP_Interfaces {
    /**
     * Required View methods available to Presenter.
     * A passive layer, responsible to show data
     * and receive user interactions
     * Presenter to View
     */
    public interface RequiredViewOps {
        void notifyDeletedMarkers();

        void dropMarkerOnMap(LatLng newLatLng);

        Location notifyGetLastKnownLocation(Context context);

        void gpsManagerStart(LatLng destinationPosition, final Boolean chosenMode);

        void checkInReminderAlertMessage(final Context context);

        void showDestinationAlertDialog();

        void drawPath(String result, Marker marker);

        void setUpMapIfNeeded();

        void enableMyLocation();
    }

    /**
     * Operations offered to View to communicate with Presenter.
     * Process user interaction, sends data requests to Model, etc.
     * View to Presenter
     */
    public interface ProvidedPresenterOps {
        void notifyDropMarker(LatLng newLatLng);

        void setView(RequiredViewOps view);

        void onDestroy(boolean isChangingConfiguration);

        void notifyToDeleteMarkers(Marker markerForDeletion, final Activity activity);

        LatLng checkInCurrentPosition(Context context);

        void setUpMapDemand(Activity activity, MapRequestRequestCallback callback);

        Positions searchList(Double latitude, Double longitude);

        String getWifiAddress();

        void uploadingPosition(Activity activity, LatLng destinationPosition, final Boolean chosenMode);
    }

    /**
     * Required Presenter methods available to Model.
     * Model to Presenter
     */
    public interface RequiredPresenterOps {
        void notifyDeleteDMarkers();

        Location getLastKnownLocation(Context context);

        void uploadingPosition(LatLng destinationPosition, final Boolean chosenMode);

        void gpsManagerStart(LatLng destinationPosition, final Boolean chosenMode);

        void checkInReminderAlertMessage(final Context context);

        void showDestinationAlertDialog();

//        void drawPath(String result, Marker marker);

        void setUpMapIfNeeded();

        void enableMyLocation();
    }

    /**
     * Operations offered to Model to communicate with Presenter
     * Handles all data business logic.
     * Presenter to Model
     */
    public interface ProvidedModelOps {
        LatLng checkInCurrentPosition(Context context);

        String getWifiMacAddress();

        void setUpMap(final Activity activity, final MapRequestRequestCallback callback);

        Positions searchList(Double latitude, Double longitude);

        void onDestroy(boolean isChangingConfiguration);

        void deleteMarkers(Marker markerForDeletion, final Activity activity);

        void uploadingPosition(Activity activity, LatLng destinationPosition, final Boolean chosenMode);
    }
}
