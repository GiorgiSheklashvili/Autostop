package com.example.gio.autostop;


import com.example.gio.autostop.model.MapModel;
import com.example.gio.autostop.presenter.MapPresenter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 25, manifest = "/src/main/AndroidManifest.xml")
public class MapPresenterTest {
    private MapPresenter mPresenter;
    private MapModel mockModel;
    private MVP_Interfaces.RequiredViewOps mockView;

    public void setup() {
        mockView = Mockito.mock(MVP_Interfaces.RequiredViewOps.class);
        mockModel = Mockito.mock(MapModel.class, Mockito.RETURNS_DEEP_STUBS);
        mPresenter = new MapPresenter(mockView);
        mPresenter.setModel(mockModel);
//        Mockito.when(mockModel.setUpMap()).thenReturn(null);
        Mockito.reset(mockView);
    }

    @Test
    public void testCheckInCurrentPosition() {
        mPresenter.checkInCurrentPosition(RuntimeEnvironment.application);

    }
}
