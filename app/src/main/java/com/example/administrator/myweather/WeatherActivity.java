package com.example.administrator.myweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
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

import com.bumptech.glide.Glide;
import com.example.administrator.myweather.gson.Forecast;
import com.example.administrator.myweather.gson.Weather;
import com.example.administrator.myweather.util.HttpUtil;
import com.example.administrator.myweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static org.litepal.LitePalApplication.getContext;

public class WeatherActivity extends AppCompatActivity {
    //成员变量定义
     private ScrollView weatherLayout;
     private TextView titleCity;
     private TextView titleUpdateTime;
     private TextView degreeText;
     private TextView weatherInfoText;
     private LinearLayout forecastLayout;
     private TextView aqiText;
     private TextView pm25Text;
     private TextView comfortText;
     private TextView carwashText;
     private TextView sportText;
     private ImageView bingPicImg;
     public SwipeRefreshLayout swipRefresh;
     public DrawerLayout drawerLayout;
     private Button navButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        //实现背景图和状态栏融合在一起
        if(Build.VERSION.SDK_INT>=21){
            //5.0以上的系统才执行该段段代码
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        //初始化各控件
         weatherLayout=(ScrollView)findViewById(R.id.weather_layout);
        titleCity=(TextView)findViewById(R.id.title_city_1);
        titleUpdateTime=(TextView)findViewById(R.id.update_time);
        degreeText=(TextView)findViewById(R.id.degree_text);
        weatherInfoText=(TextView)findViewById(R.id.weather_info_text);
        forecastLayout=(LinearLayout) findViewById(R.id.forecast_layout);
        aqiText=(TextView)findViewById(R.id.aqi_text);
        pm25Text=(TextView)findViewById(R.id.pm25_text);
        comfortText=(TextView)findViewById(R.id.comfort_text);
        carwashText=(TextView)findViewById(R.id.car_wash_text);
        sportText=(TextView)findViewById(R.id.sport_text);
        bingPicImg=(ImageView)findViewById(R.id.bing_pic_img);
        swipRefresh=(SwipeRefreshLayout)findViewById(R.id.swip_refresh);
        swipRefresh.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        navButton=(Button)findViewById(R.id.nav_button);

        //获取缓存信息
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        String bingPic=prefs.getString("bing_pic",null);
        final String weatherId;
        //装载背景图片
        if(bingPic!=null){
            //有缓存图片数据时，直接装载
            Glide.with(this).load(bingPic).into(bingPicImg);
        }
        else {
            loadBingPic();
        }
        //获取天气信息
        if(weatherString!=null){
            //有缓存时直接解析天气数据
            Weather weather= Utility.handleWeatherResponse(weatherString);
            weatherId=weather.basic.weatherId;
            showWeatherInfo(weather);
        }else {
          //无缓存时去服务器查询天气,通过主活动Intent传送过来的weather_id
             weatherId=getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.VISIBLE);
            requestWeather(weatherId);//到服务器请求获取天气情况
        }
        //下拉刷新天气
        swipRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
        //返回主页滑动页面按钮
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);//打开滑动页面
            }
        });
    }

    /**
     * 根据天气ID请求城市天气信息
     */
    public void requestWeather(final String weatherId){

     final String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=9fd505a530574d45ad114527e1139e29";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败!",Toast.LENGTH_SHORT).show();
                        swipRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String responseText=response.body().string();
                final Weather weather=Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //测试
                       // Toast.makeText(getBaseContext(),"获取的数据:"+responseText,Toast.LENGTH_LONG).show();
                        if(weather!=null&&"ok".equals(weather.status)){
                            //将网络获取的数据存入缓存
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                            //装载背景图片
                            loadBingPic();
                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败2!",Toast.LENGTH_SHORT).show();
                        }
                        swipRefresh.setRefreshing(false);
                    }
                });
            }
        });

    }

    /**
     * 处理并展示Weater实体中信息
     */
    private void showWeatherInfo(Weather weather){
       // Toast.makeText(WeatherActivity.this,"获取天气成功"+weather.basic.cityName,Toast.LENGTH_LONG).show();
       String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime.split(" ")[1];
        String degree=weather.now.temperature+"℃";
        String weatherInfo=weather.now.more.info;


        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();

        //天气预测
        for (Forecast forecast:weather.forecastList){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText=(TextView)view.findViewById(R.id.date_text);
            TextView infoText=(TextView)view.findViewById(R.id.info_text);
            TextView maxText=(TextView)view.findViewById(R.id.max_text);
            TextView minText=(TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }

        //空气质量
        if(weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }

        //舒适度
        String comfort="舒适度:"+weather.suggestion.comfort.info;
        String carWash="洗车指数:"+weather.suggestion.carWash.info;
        String sport="运动建议:"+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carwashText.setText(carWash);
        sportText.setText(sport);
        //
        weatherLayout.setVisibility(View.VISIBLE);



    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic(){
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String bingPic=response.body().string();
                //存入缓存
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                //切换到主线程处理图片
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });

            }
        });

    }


}
