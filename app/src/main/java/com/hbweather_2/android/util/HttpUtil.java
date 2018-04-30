package com.hbweather_2.android.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by 浩比 on 2018/4/30.
 */

public class HttpUtil {

    /**
     * 和服务器进行交互,从服务器端获取全国所有省市的数据
     * @param address 请求地址
     * @param callback 回调（处理服务器响应）
     */
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
