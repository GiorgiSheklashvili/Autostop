package com.home.gio.autostop.server;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class DeletePositionRequest extends StringRequest {
    private static final String REQUEST_URL="http://autostop1.000webhostapp.com/delete.php";
    private Map<String,String> params;
    public DeletePositionRequest(String mac, String android_id, Response.Listener<String> listener) {
        super(Method.POST, REQUEST_URL, listener, null);
        params=new HashMap<>();
        params.put("mac",mac);
        params.put("android_id",android_id);
        params.put("user", "user1");
        params.put("password", "password1");
    }

    @Override
    protected Map<String, String> getParams(){
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
