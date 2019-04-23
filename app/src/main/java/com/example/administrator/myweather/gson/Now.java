package com.example.administrator.myweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2019-04-23.
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;
    @SerializedName("cond")
    public More more;

    public class More{
        @SerializedName("txt")
        public String info;
    }
}
