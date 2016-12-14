package com.example.gio.autostop.Server;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class UploadPosition extends StringRequest {
    private static final String REQUEST_URL="http://autostop1.comxa.com/upload.php";
    private Map<String,String> params;
    public UploadPosition(Positions position,Response.Listener<String> listener){
       super(Method.POST,REQUEST_URL,listener,null);
        params=new HashMap<>();
        params.put("mac",position.getMac());
        params.put("android_id",position.getAndroidId());
        params.put("latitude",position.getLatitude()+"");
        params.put("longitude",position.getLongitude()+"");
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
