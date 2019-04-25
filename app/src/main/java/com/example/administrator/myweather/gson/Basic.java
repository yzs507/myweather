package com.example.administrator.myweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2019-04-23.
 */

public class Basic {
    @SerializedName("city")//Json字段和Java字段建立映射关系
    public String cityName;
    @SerializedName("id")
    public String weatherId;
    public Update update;
    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }

}
