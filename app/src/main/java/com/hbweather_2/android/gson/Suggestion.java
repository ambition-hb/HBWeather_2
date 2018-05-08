package com.hbweather_2.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 浩比 on 2018/5/6.
 */

public class Suggestion {

    public String brf;//生活指数

    @SerializedName("txt")
    public String info;//生活指数详细描述

    public String type;//生活指数类型

}
