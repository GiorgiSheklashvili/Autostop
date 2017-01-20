package com.example.gio.autostop;


import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class MVP_Interfaces {
    /**
     * Required View methods available to Presenter.
     * A passive layer, responsible to show data
     * and receive user interactions
     *      Presenter to View
     */
    public interface RequiredViewOps {
        void checkInCurrentPosition(Context context);
        void deleteMarkers();
        Location getLastKnownLocation(Context context);
        void uploadingPosition(LatLng destinationPosition, final Boolean chosenMode);
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
     *      View to Presenter
     */
    interface ProvidedPresenterOps {
        void checkInCurrentPosition(Context context);
        void deleteMarkers();
        void uploadingPosition(LatLng destinationPosition, final Boolean chosenMode);
    }

    /**
     * Required Presenter methods available to Model.
     *      Model to Presenter
     */
    interface RequiredPresenterOps {
        void checkInCurrentPosition(Context context);
        void deleteMarkers();
        Location getLastKnownLocation(Context context);
        void uploadingPosition(LatLng destinationPosition, final Boolean chosenMode);
        void gpsManagerStart(LatLng destinationPosition, final Boolean chosenMode);
        void checkInReminderAlertMessage(final Context context);
        void showDestinationAlertDialog();
        void drawPath(String result, Marker marker);
        void setUpMapIfNeeded();
        void enableMyLocation();
    }
    /**
     * Operations offered to Model to communicate with Presenter
     * Handles all data business logic.
     *      Presenter to Model
     */
    interface ProvidedModelOps {
        void checkInCurrentPosition(Context context);
        void deleteMarkers();
        void uploadingPosition(LatLng destinationPosition, final Boolean chosenMode);
    }
}
