package com.home.gio.autostop.server;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.home.gio.autostop.model.Position;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UploadPositionRequest extends StringRequest {
    private static final String REQUEST_URL = "http://autostop1.000webhostapp.com/upload.php";
    private Map<String, String> params;

    public UploadPositionRequest(Position position, Response.Listener<String> listener) {
        super(Request.Method.POST, REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("mac", position.getMac());
        params.put("android_id", position.getAndroidId());
        params.put("latitude", position.getLatitude() + "");
        params.put("longitude", position.getLongitude() + "");
        params.put("latitudeDestination", position.getLatitudeDestination() + "");
        params.put("longitudeDestination", position.getLongitudeDestination() + "");
        params.put("kindOfUser", position.getIsKindOfUser().toString() + "");
        params.put("user", "user1");
        params.put("password", "password1");
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }


//    @Override
//    public Map<String, String> getHeaders() throws AuthFailureError {
//        Map<String, String> headers = new HashMap<>();
//        String credentials = "giusha9:avtobusi6";
//        String auth = "Basic "
//                + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
//        headers.put("Content-Type", "application/json");
//        headers.put("Authorization", auth);
//        return headers;
//    }

}
