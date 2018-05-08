package com.hbweather_2.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 浩比 on 2018/5/1.
 */

public class Hourly {

    public String time;//预报时间

    @SerializedName("cond_code")
    public String cond;//预报天气状况

    public String tmp;//预报温度
}
