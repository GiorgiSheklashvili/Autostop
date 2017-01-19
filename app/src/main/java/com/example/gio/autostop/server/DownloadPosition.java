package com.example.gio.autostop.server;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

public class DownloadPosition extends StringRequest{
    private static final String REQUEST_URL="http://autostop1.comxa.com/download.php";
    public DownloadPosition(Response.Listener<String> listener){
        super(Request.Method.POST,REQUEST_URL,listener,null);
    }
}
