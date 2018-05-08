package com.hbweather_2.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 浩比 on 2018/5/1.
 */

public class Forecast {

    public String date;//预报日期

    @SerializedName("cond_code_d")
    public String cond;//天气状况(改变SerializedName可改变返回值，如天气状况代码)

    public String tmp_max;//当日最高温度

    public String tmp_min;//当日最低温度

    //----------------------------------

    public String sr;//日出时间

    public String ss;//日落时间

    public String pop;//降水概率

    public String uv_index;//紫外线指数

}
