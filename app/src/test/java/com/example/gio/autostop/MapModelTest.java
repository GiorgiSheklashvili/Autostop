package com.example.gio.autostop;


import com.example.gio.autostop.model.MapModel;
import com.example.gio.autostop.presenter.MapPresenter;
import com.example.gio.autostop.model.Position;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 25, manifest = "/src/main/AndroidManifest.xml")
public class MapModelTest {
    private MapModel mapModel;

    @Before
    public void setup() {
        MapPresenter mockPresenter = Mockito.mock(MapPresenter.class);
        mapModel = new MapModel(mockPresenter);
        mapModel.positionList = new ArrayList<>();
        Mockito.reset(mockPresenter);
    }

    private Position createPosition(double lat, double longT, double latitudeDestination, double longitudeDestination, boolean kindOfUser, String mac, String androidId) {
        Position position = new Position(lat, longT, latitudeDestination, longitudeDestination, kindOfUser, mac, androidId);
        return position;
    }

    @Test
    public void checkInCurrentPosition() {
        mapModel.checkInCurrentPosition(RuntimeEnvironment.application);
        Assert.assertNotNull(mapModel.newLatLng);
    }

//    @Test
//    public void setUpMap() {
//        mapModel.setUpMap(RuntimeEnvironment.application,);
//        Assert.assertEquals(mapModel.jsonArray.length(), mapModel.positionList.size());
//    }

    @Test
    public void searchList() {

    }

    @Test
    public void deleteMarkers() {

    }

    @Test
    public void uploadingPosition() {

    }

    @Test
    public void getWifiMacAddress() {
        Assert.assertNotNull(mapModel.getWifiMacAddress(),"");


    }

}
