package com.hbweather_2.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.hbweather_2.android.gson.Forecast;
import com.hbweather_2.android.gson.Hourly;
import com.hbweather_2.android.gson.Suggestion;
import com.hbweather_2.android.gson.Weather;
import com.hbweather_2.android.service.AutoUpdateService;
import com.hbweather_2.android.util.HttpUtil;
import com.hbweather_2.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;

    private Button button;

    public SwipeRefreshLayout swipeRefresh;

    private String mWeatherId;

    private ScrollView weatherLayout;//滚动视图对象

    private TextView basicLocation;//basic--城市名称

    private TextView nowCond;//now--实况天气

    private TextView nowTmp;//now--温度

    private TextView nowHum;//now--相对湿度

    private TextView nowWindDir;//now--风向 -

    private TextView nowWindSpd;//now--风速 -

    private TextView nowWind;//+

    private TextView nowFl;//now--体感温度

    private TextView nowPcpn;//now--降水量

    private TextView nowPres;//now--大气压强

    private TextView nowVis;//now--能见度

    private LinearLayout hourlyLayout;//逐小时预报

    private LinearLayout forecastLayout;//天气预报

    private LinearLayout suggestionLayout;//生活指数

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
//        nowWindDir = (TextView) findViewById(R.id.now_wind_dir);//-
//        nowWindSpd = (TextView) findViewById(R.id.now_wind_spd);//-
        nowWind = (TextView) findViewById(R.id.now_wind); //+
        nowFl = (TextView) findViewById(R.id.now_fl);
        nowPcpn = (TextView) findViewById(R.id.now_pcpn);
        nowPres = (TextView) findViewById(R.id.now_pres);
        nowVis = (TextView) findViewById(R.id.now_vis);
        hourlyLayout = (LinearLayout) findViewById(R.id.hourly_layout);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        suggestionLayout = (LinearLayout) findViewById(R.id.suggestion_layout);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);//下拉刷新进度条颜色
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        button = (Button) findViewById(R.id.button);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null){
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);//将返回的JSON数据解析成Weather实体类
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);//处理并展示Weather实体类中的数据
        }else{
            //无缓存时去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");//根据天气id并调用requestWeather()方法来从服务器请求天气数据
            weatherLayout.setVisibility(View.INVISIBLE);//请求数据时会隐藏ScrollView
            requestWeather(mWeatherId);//根据天气id请求城市天气信息
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);//触发下拉监听触发器后，调用requestWeather()方法请求天气信息
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    /**
     * 根据天气id请求城市天气信息
     * @param weatherId
     */
    public void requestWeather(final String weatherId){
        //https://free-api.heweather.com/s6/weather?location=CN101010100&key=8518f3bef50144e39994370699b08d5e
        String weatherUrl = "https://free-api.heweather.net/s6/weather?location=" + weatherId + "&key=c5d781e1c8ac422987f33e0eb79adad5";
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
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
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
                        swipeRefresh.setRefreshing(false);
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
        String degree = weather.now.temperature + "℃";//实时温度
        nowTmp.setText(degree);
        String hum = weather.now.hum;//相对湿度
        nowHum.setText(hum);
        String wind_dir = weather.now.wind_dir;//风向
        //nowWindDir.setText(wind_dir);
        String wind_spd = weather.now.wind_spd;//风速(公里/小时)
        //nowWindSpd.setText(wind_spd);
        String wind = wind_dir + wind_spd + "级";//+
        nowWind.setText(wind);//+
        String fl = weather.now.fl + "℃";//体感温度
        nowFl.setText(fl);
        String pcpn = weather.now.pcpn + "mm";//降水量
        nowPcpn.setText(pcpn);
        String pres = weather.now.pres + "Pa";//大气压强
        nowPres.setText(pres);
        String vis = weather.now.vis + "公里";//能见度
        nowVis.setText(vis);

        hourlyLayout.removeAllViews();
        for (Hourly hourly : weather.hourlyList){
            View view = LayoutInflater.from(this).inflate(R.layout.hourly_item, hourlyLayout, false);//逐小时预报子项布局
            TextView hourly_time = (TextView) view.findViewById(R.id.hourly_time);
            ImageView hourly_cond = (ImageView) view.findViewById(R.id.hourly_cond_text);
            TextView hourly_tmp = (TextView) view.findViewById(R.id.hourly_tmp);
            String hour_time = hourly.time.split(" ")[1];//-
            hourly_time.setText(hour_time);//-
//            hourly_time.setText(hourly.time);//+
            String cond_1 = hourly.cond;//+
            switch (cond_1){
                case "100":
                    hourly_cond.setImageResource(R.drawable.w100);
                    return;
                case "101":
                case "102":
                case "104":
                    hourly_cond.setImageResource(R.drawable.w104);
                    break;
                case "103":
                    hourly_cond.setImageResource(R.drawable.w101);
                    break;
                case "200":
                case "201":
                case "202":
                case "203":
                case "204":
                    hourly_cond.setImageResource(R.drawable.w200);
                    break;
                case "205":
                case "206":
                case "207":
                    hourly_cond.setImageResource(R.drawable.w207);
                    break;
                case "208":
                    hourly_cond.setImageResource(R.drawable.w208);
                    break;
                case "209":
                case "210":
                case "211":
                case "212":
                case "213":
                    hourly_cond.setImageResource(R.drawable.w212);
                    break;
                case "302":
                case "303":
                    hourly_cond.setImageResource(R.drawable.w302);
                    break;
                case "304":
                    hourly_cond.setImageResource(R.drawable.w304);
                    break;
                case "300":
                case "305":
                case "309":
                    hourly_cond.setImageResource(R.drawable.w305);
                    break;
                case "301":
                case "306":
                    hourly_cond.setImageResource(R.drawable.w306);
                    break;
                case "307":
                    hourly_cond.setImageResource(R.drawable.w307);
                    break;
                case "308":
                case "310":
                case "311":
                    hourly_cond.setImageResource(R.drawable.w310);
                    break;
                case "312":
                case "313":
                    hourly_cond.setImageResource(R.drawable.w312);
                    break;
                case "400":
                case "405":
                case "406":
                case "407":
                    hourly_cond.setImageResource(R.drawable.w400);
                    break;
                case "401":
                    hourly_cond.setImageResource(R.drawable.w401);
                    break;
                case "402":
                    hourly_cond.setImageResource(R.drawable.w402);
                    break;
                case "403":
                    hourly_cond.setImageResource(R.drawable.w403);
                    break;
                case "404":
                    hourly_cond.setImageResource(R.drawable.w404);
                    break;
                case "500":
                case "501":
                    hourly_cond.setImageResource(R.drawable.w500);
                    break;
                case "502":
                    hourly_cond.setImageResource(R.drawable.w502);
                    break;
                case "503":
                    hourly_cond.setImageResource(R.drawable.w503);
                    break;
                case "504":
                    hourly_cond.setImageResource(R.drawable.w504);
                    break;
                default:
                    hourly_cond.setImageResource(R.drawable.w999);
                    break;
            }
//            hourly_cond.setText(hourly.cond);//-
            hourly_tmp.setText(hourly.tmp);
            hourlyLayout.addView(view);
        }

        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView forecast_date = (TextView) view.findViewById(R.id.forecast_date);
            ImageView forecast_cond = (ImageView) view.findViewById(R.id.forecast_cond);
            TextView forecast_max = (TextView) view.findViewById(R.id.forecast_max);
            TextView forecast_min = (TextView) view.findViewById(R.id.forecast_min);
            forecast_date.setText(forecast.date);
            String cond = forecast.cond;
            switch (cond){
                case "100":
                    forecast_cond.setImageResource(R.drawable.w100);
                    return;
                case "101":
                case "102":
                case "104":
                    forecast_cond.setImageResource(R.drawable.w104);
                    break;
                case "103":
                    forecast_cond.setImageResource(R.drawable.w101);
                    break;
                case "200":
                case "201":
                case "202":
                case "203":
                case "204":
                    forecast_cond.setImageResource(R.drawable.w200);
                    break;
                case "205":
                case "206":
                case "207":
                    forecast_cond.setImageResource(R.drawable.w207);
                    break;
                case "208":
                    forecast_cond.setImageResource(R.drawable.w208);
                    break;
                case "209":
                case "210":
                case "211":
                case "212":
                case "213":
                    forecast_cond.setImageResource(R.drawable.w212);
                    break;
                case "302":
                case "303":
                    forecast_cond.setImageResource(R.drawable.w302);
                    break;
                case "304":
                    forecast_cond.setImageResource(R.drawable.w304);
                    break;
                case "300":
                case "305":
                case "309":
                    forecast_cond.setImageResource(R.drawable.w305);
                    break;
                case "301":
                case "306":
                    forecast_cond.setImageResource(R.drawable.w306);
                    break;
                case "307":
                    forecast_cond.setImageResource(R.drawable.w307);
                    break;
                case "308":
                case "310":
                case "311":
                    forecast_cond.setImageResource(R.drawable.w310);
                    break;
                case "312":
                case "313":
                    forecast_cond.setImageResource(R.drawable.w312);
                    break;
                case "400":
                case "405":
                case "406":
                case "407":
                    forecast_cond.setImageResource(R.drawable.w400);
                    break;
                case "401":
                    forecast_cond.setImageResource(R.drawable.w401);
                    break;
                case "402":
                    forecast_cond.setImageResource(R.drawable.w402);
                    break;
                case "403":
                    forecast_cond.setImageResource(R.drawable.w403);
                    break;
                case "404":
                    forecast_cond.setImageResource(R.drawable.w404);
                    break;
                case "500":
                case "501":
                    forecast_cond.setImageResource(R.drawable.w500);
                    break;
                case "502":
                    forecast_cond.setImageResource(R.drawable.w502);
                    break;
                case "503":
                    forecast_cond.setImageResource(R.drawable.w503);
                    break;
                case "504":
                    forecast_cond.setImageResource(R.drawable.w504);
                    break;
                default:
                    forecast_cond.setImageResource(R.drawable.w999);
                    break;
            }
            //forecast_cond.setText(forecast.cond);
            forecast_max.setText(forecast.tmp_max);
            forecast_min.setText(forecast.tmp_min);
            forecastLayout.addView(view);
        }

        suggestionLayout.removeAllViews();
        for (Suggestion suggestion : weather.suggestionList){
            View view = LayoutInflater.from(this).inflate(R.layout.suggestion_item, suggestionLayout, false);
            TextView typeText = (TextView) view.findViewById(R.id.type);
            TextView brfText = (TextView) view.findViewById(R.id.brf);
            TextView txtText = (TextView) view.findViewById(R.id.txt);
            String type = suggestion.type;
            if(type.equals("comf")){
                type = "舒适度指数:";
            }else if (type.equals("drsg")){
                type = "穿衣指数:";
            }else if (type.equals("flu")){
                type = "流感指数:";
            }else if (type.equals("sport")){
                type = "运动指数:";
            }else if (type.equals("trav")){
                type = "旅游指数:";
            }else if (type.equals("uv")){
                type = "紫外线指数:";
            }else if (type.equals("cw")){
                type = "洗车指数:";
            }else if (type.equals("air")){
                type = "空气污染扩散条件指数:";
            }
            typeText.setText(type);
            brfText.setText(suggestion.brf);
            txtText.setText(suggestion.info);
            suggestionLayout.addView(view);
        }

        weatherLayout.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }
}
