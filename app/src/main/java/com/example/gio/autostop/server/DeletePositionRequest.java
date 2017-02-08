package com.example.gio.autostop.server;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class DeletePositionRequest extends StringRequest {
    private static final String REQUEST_URL="http://autostop1.comxa.com/delete.php";
    private Map<String,String> params;
    public DeletePositionRequest(String mac, String android_id, Response.Listener<String> listener) {
        super(Method.POST, REQUEST_URL, listener, null);
        params=new HashMap<>();
        params.put("mac",mac);
        params.put("android_id",android_id);
    }

    @Override
    protected Map<String, String> getParams(){
        return params;
    }
}
