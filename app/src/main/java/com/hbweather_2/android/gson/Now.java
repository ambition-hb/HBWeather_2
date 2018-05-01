package com.hbweather_2.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 浩比 on 2018/5/1.
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;//实况温度

    @SerializedName("cond_txt")
    public String cond;//实况天气

    public String hum;//湿度

    public String wind_dir;//风向(风向+风速)

    public String wind_spd;//风速(风向+风速)

    public String fl;//体感温度

    public String pcpn;//降水量

    public String pres;//大气压强

    public String vis;//能见度

}
