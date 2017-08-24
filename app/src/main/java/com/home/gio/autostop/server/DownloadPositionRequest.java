package com.home.gio.autostop.server;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class DownloadPositionRequest extends StringRequest{
    private Map<String, String> params;
    private static final String REQUEST_URL="http://autostop1.000webhostapp.com/download.php";
    public DownloadPositionRequest(Response.Listener<String> listener){
        super(Request.Method.POST,REQUEST_URL,listener,null);
        params = new HashMap<>();
        params.put("user", "user1");
        params.put("password", "password1");
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
//
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
