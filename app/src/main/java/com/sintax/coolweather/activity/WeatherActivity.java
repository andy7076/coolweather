package com.sintax.coolweather.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sintax.coolweather.R;
import com.sintax.coolweather.util.HttpCallBackListener;
import com.sintax.coolweather.util.HttpUtil;
import com.sintax.coolweather.util.Utility;

import static com.sintax.coolweather.R.id.publish_time;

public class WeatherActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView cityName;
    private TextView publishTime;
    private TextView curDate;
    private TextView curWeather;
    private TextView curTemp1;
    private TextView curTemp2;
    private ImageButton refresh;
    private ImageButton chooseCity;
    private RelativeLayout weatherInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        initComponents();
        String countryCode = getIntent().getStringExtra("country_code");
        if (!TextUtils.isEmpty(countryCode)) {
            //有县级代号时就去查询天气
            publishTime.setText("同步中...");
            weatherInfo.setVisibility(View.INVISIBLE);
            cityName.setVisibility(View.INVISIBLE);
            queryWeatherCode(countryCode);
        } else {
            //没有县级代号时就直接显示本地天气
            showWeather();
        }
    }

    /**
     * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上。
     */
    private void showWeather() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        cityName.setText(sp.getString("city_name", ""));
        curTemp1.setText(sp.getString("temp1", ""));
        curTemp2.setText(sp.getString("temp2", ""));
        curWeather.setText(sp.getString("cur_weather", ""));
        publishTime.setText("今天"+sp.getString("publish_time", "")+"发布");
        curDate.setText(sp.getString("cur_date", ""));
        weatherInfo.setVisibility(View.VISIBLE);
        cityName.setVisibility(View.VISIBLE);
    }

    /**
     * 查询县级代号所对应的天气代号
     *
     * @param countryCode
     */
    private void queryWeatherCode(String countryCode) {
        String address = "http://www.weather.com.cn/data/list3/city" + countryCode + ".xml";
        queryFromServer(address, "countryCode");
    }

    /**
     * 根据传入的地址和类型去向服务器查询天气代号或者天气信息
     *
     * @param address
     * @param type
     */
    private void queryFromServer(final String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallBackListener() {
            @Override
            public void onFinish(final String response) {
                if (type.equals("countryCode")) {
                    if (!TextUtils.isEmpty(response)) {
                        //从服务器返回的数据中解析出天气代号
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                } else if (type.equals("weatherCode")) {
                    //处理从服务器返回的天气信息
                    Utility.handleWeatherResponse(WeatherActivity.this, response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishTime.setText("同步失败");
                    }
                });
            }
        });
    }

    private void initComponents() {
        cityName = (TextView) findViewById(R.id.city_name);
        publishTime = (TextView) findViewById(publish_time);
        curDate = (TextView) findViewById(R.id.cur_date);
        curWeather = (TextView) findViewById(R.id.cur_weather);
        curTemp1 = (TextView) findViewById(R.id.cur_temp1);
        curTemp2 = (TextView) findViewById(R.id.cur_temp2);
        refresh = (ImageButton) findViewById(R.id.refresh);
        refresh.setOnClickListener(this);
        chooseCity = (ImageButton) findViewById(R.id.choose_city);
        chooseCity.setOnClickListener(this);
        weatherInfo = (RelativeLayout) findViewById(R.id.weather_info_layout);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.refresh:
                publishTime.setText("同步中...");
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = sp.getString("weather_code", "");
                if (!TextUtils.isEmpty(weatherCode)) {
                    queryWeatherInfo(weatherCode);
                }
                break;
            case R.id.choose_city:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
        }
    }

    /**
     * 查询天气代号所对应的天气
     *
     * @param weatherCode
     */
    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryFromServer(address, "weatherCode");
    }
}
