package com.example.gio.autostop.server;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

public class DownloadPositionRequest extends StringRequest{
    private static final String REQUEST_URL="http://autostop1.comxa.com/download.php";
    public DownloadPositionRequest(Response.Listener<String> listener){
        super(Request.Method.POST,REQUEST_URL,listener,null);
    }
}
