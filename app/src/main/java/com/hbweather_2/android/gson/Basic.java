package com.hbweather_2.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 浩比 on 2018/5/1.
 */

public class Basic {

    @SerializedName("location")
    public String cityName;//城市名称

    @SerializedName("cid")
    public String weatherId;//城市ID


}
