package com.example.administrator.myweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2019-04-18.
 */

public class HttpUtil {

    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(address).build();
            client.newCall(request).enqueue(callback);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
