package com.hbweather_2.android;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.hbweather_2.android.gson.Forecast;
import com.hbweather_2.android.gson.Hourly;
import com.hbweather_2.android.gson.Lifestyle;
import com.hbweather_2.android.gson.Weather;
import com.hbweather_2.android.util.HttpUtil;
import com.hbweather_2.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;//滚动视图对象

    private TextView basicLocation;//basic--城市名称

    private TextView nowCond;//now--实况天气

    private TextView nowTmp;//now--温度

    private TextView nowHum;//now--相对湿度

    private TextView nowWindDir;//now--风向

    private TextView nowWindSpd;//now--风速

    private TextView nowFl;//now--体感温度

    private TextView nowPcpn;//now--降水量

    private TextView nowPres;//now--大气压强

    private TextView nowVis;//now--能见度

    private LinearLayout hourlyLayout;//逐小时预报

    private LinearLayout forecastLayout;//天气预报

    private LinearLayout lifestyleLayout;//生活指数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        //初始化各控件
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        basicLocation = (TextView) findViewById(R.id.basic_location);
        nowCond = (TextView) findViewById(R.id.now_cond);
        nowTmp = (TextView) findViewById(R.id.now_tmp);
        nowHum = (TextView) findViewById(R.id.now_hum);
        nowWindDir = (TextView) findViewById(R.id.now_wind_dir);
        nowWindSpd = (TextView) findViewById(R.id.now_wind_spd);
        nowFl = (TextView) findViewById(R.id.now_fl);
        nowPcpn = (TextView) findViewById(R.id.now_pcpn);
        nowPres = (TextView) findViewById(R.id.now_pres);
        nowVis = (TextView) findViewById(R.id.now_vis);
        hourlyLayout = (LinearLayout) findViewById(R.id.hourly_layout);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        lifestyleLayout = (LinearLayout) findViewById(R.id.lifestyle_layout);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null){
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);//将返回的JSON数据解析成Weather实体类
            showWeatherInfo(weather);//处理并展示Weather实体类中的数据
        }else{
            //无缓存时去服务器查询天气
            String weatherId = getIntent().getStringExtra("weather_id");//根据天气id并调用requestWeather()方法来从服务器请求天气数据
            weatherLayout.setVisibility(View.INVISIBLE);//请求数据时会隐藏ScrollView
            requestWeather(weatherId);//根据天气id请求城市天气信息
        }
    }

    /**
     * 根据天气id请求城市天气信息
     * @param weatherId
     */
    public void requestWeather(final String weatherId){

        String weatherUrl = "https://free-api.heweather.com/s6/weather?location=" + weatherId + "&key=8518f3bef50144e39994370699b08d5e";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据
     * @param weather
     */
    public void showWeatherInfo(Weather weather){
        String cityName = weather.basic.cityName;//城市名称
        basicLocation.setText(cityName);
        String weatherInfo = weather.now.cond;//实时天气信息
        nowCond.setText(weatherInfo);
        String degree = weather.now.temperature;//实时温度
        nowTmp.setText(degree);
        String hum = weather.now.hum;//相对湿度
        nowHum.setText(hum);
        String wind_dir = weather.now.wind_dir;//风向
        nowWindDir.setText(wind_dir);
        String wind_spd = weather.now.wind_spd;//风速(公里/小时)
        nowWindSpd.setText(wind_spd);
        String fl = weather.now.fl;//体感温度
        nowFl.setText(fl);
        String pcpn = weather.now.pcpn;//降水量
        nowPcpn.setText(pcpn);
        String pres = weather.now.pres;//大气压强
        nowPres.setText(pres);
        String vis = weather.now.vis;//能见度
        nowVis.setText(vis);
/*
        hourlyLayout.removeAllViews();
        for (Hourly hourly : weather.hourlyList){
            View view = LayoutInflater.from(this).inflate(R.layout.test_hourly, hourlyLayout, false);//逐小时汇报子项布局
            TextView hourly_time = (TextView) view.findViewById(R.id.hourly_time);
            TextView hourly_cond = (TextView) view.findViewById(R.id.hourly_cond_text);
            TextView hourly_tmp = (TextView) view.findViewById(R.id.hourly_tmp);
            hourly_time.setText(hourly.time);
            hourly_cond.setText(hourly.cond_txt);
            hourly_tmp.setText(hourly.tmp);
            hourlyLayout.addView(view);
        }
*/
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.test_forecast, forecastLayout, false);
            TextView forecast_date = (TextView) view.findViewById(R.id.forecast_date);
            TextView forecast_cond = (TextView) view.findViewById(R.id.forecast_cond);
            TextView forecast_max = (TextView) view.findViewById(R.id.forecast_max);
            TextView forecast_min = (TextView) view.findViewById(R.id.forecast_min);
            forecast_date.setText(forecast.date);
            forecast_cond.setText(forecast.cond);
            forecast_max.setText(forecast.tmp_max);
            forecast_min.setText(forecast.tmp_min);
            forecastLayout.addView(view);
        }
/*
        lifestyleLayout.removeAllViews();
        for (Lifestyle lifestyle : weather.lifestyleList){
            View view = LayoutInflater.from(this).inflate(R.layout.test_lifestyle, lifestyleLayout, false);
            TextView lifestyle_type = (TextView) view.findViewById(R.id.lifestyle_type);
            TextView lifestyle_brf = (TextView) view.findViewById(R.id.lifestyle_brf);
            TextView lifestyle_txt = (TextView) view.findViewById(R.id.lifestyle_txt);
            lifestyle_type.setText(lifestyle.type);
            lifestyle_brf.setText(lifestyle.brf);
            lifestyle_txt.setText(lifestyle.txt);
            lifestyleLayout.addView(view);
        }
*/
        weatherLayout.setVisibility(View.VISIBLE);

    }
}
