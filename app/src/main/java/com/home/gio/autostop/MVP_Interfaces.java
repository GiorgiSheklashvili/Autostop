package com.home.gio.autostop;


import android.app.Activity;
import android.content.Context;
import android.location.Location;

import com.home.gio.autostop.model.Position;
import com.home.gio.autostop.interfaces.MapRequestRequestCallback;
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

        Location notifyGetLastKnownLocation(Context context);

        void gpsManagerStart(LatLng destinationPosition, final Boolean chosenMode);

    }

    /**
     * Operations offered to View to communicate with Presenter.
     * Process user interaction, sends data requests to Model, etc.
     * View to Presenter
     */
    public interface ProvidedPresenterOps {

        void setView(RequiredViewOps view);

        void onDestroy(boolean isChangingConfiguration);

        void notifyToDeleteMarkers(Marker markerForDeletion, final Context context);

        LatLng checkInCurrentPosition(Context context);

        void setUpMapDemand(Activity activity, MapRequestRequestCallback callback);

        Position searchList(Double latitude, Double longitude);

        String getWifiAddress();

        void uploadingPosition(Context context, LatLng destinationPosition, final Boolean chosenMode);
    }

    /**
     * Required Presenter methods available to Model.
     * Model to Presenter
     */
    public interface RequiredPresenterOps {
        void notifyDeleteDMarkers();

        Location getLastKnownLocation(Context context);

        void gpsManagerStart(LatLng destinationPosition, final Boolean chosenMode);

    }

    /**
     * Operations offered to Model to communicate with Presenter
     * Handles all data business logic.
     * Presenter to Model
     */
    public interface ProvidedModelOps {
        LatLng checkInCurrentPosition(Context context);

        String getWifiMacAddress();

        void setUpMap(final Context context, final MapRequestRequestCallback callback);

        Position searchList(Double latitude, Double longitude);

        void onDestroy(boolean isChangingConfiguration);

        void deleteMarkers(Marker markerForDeletion, final Context context);

        void uploadingPosition(Context context, LatLng destinationPosition, final Boolean chosenMode);
    }
}
