package com.home.gio.autostop.server;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.home.gio.autostop.model.Position;

import java.util.HashMap;
import java.util.Map;

public class UploadPositionRequest extends StringRequest {
    private static final String REQUEST_URL="http://autostop1.comxa.com/upload.php";
    private Map<String,String> params;
    public UploadPositionRequest(Position position, Response.Listener<String> listener){
       super(Method.POST,REQUEST_URL,listener,null);
        params=new HashMap<>();
        params.put("mac",position.getMac());
        params.put("android_id",position.getAndroidId());
        params.put("latitude",position.getLatitude()+"");
        params.put("longitude",position.getLongitude()+"");
        params.put("latitudeDestination",position.getLatitudeDestination()+"");
        params.put("longitudeDestination",position.getLongitudeDestination()+"");
        params.put("kindOfUser",position.getIsKindOfUser().toString()+"");
    }
    @Override
    public Map<String, String> getParams() {
        return params;
    }


}
