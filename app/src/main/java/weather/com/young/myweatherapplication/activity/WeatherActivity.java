package weather.com.young.myweatherapplication.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import weather.com.young.myweatherapplication.R;
import weather.com.young.myweatherapplication.service.AutoUpdateService;
import weather.com.young.myweatherapplication.util.HttpCallbackListener;
import weather.com.young.myweatherapplication.util.HttpUtil;
import weather.com.young.myweatherapplication.util.Ultity;

/**
 * Created by young on 16/1/2.
 */
public class WeatherActivity extends Activity implements View.OnClickListener {


    private LinearLayout weatherInfoLayout;
    private TextView cityNameText;
    /**
     * 用于显示发布时间
     */
    private TextView publishText;
    /**
     * 用于显示天气 述信息
     */
    private TextView weatherDespText;
    /**
     * 用于显示气温1
     */
    private TextView temp1Text;
    /**
     * 用于显示气温2
     */
    private TextView temp2Text;
    /**
     * 用于显示当前日期
     */
    private TextView currentDateText;
    /**
     * 切换城市按钮
     */
    private Button switchCity;
    /**
     * 更新天气按钮
     */
    private Button refreshWeather;


    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);

        // 初始化各控件
        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText = (TextView) findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.publish_text);
        weatherDespText = (TextView) findViewById(R.id.weather_desp);
        temp1Text = (TextView) findViewById(R.id.temp1);
        temp2Text = (TextView) findViewById(R.id.temp2);
        currentDateText = (TextView) findViewById(R.id.current_date);
        switchCity = (Button) findViewById(R.id.switch_city);
        refreshWeather = (Button) findViewById(R.id.refresh_weather);
        String countryCode = getIntent().getStringExtra("country_code");
        if(!TextUtils.isEmpty(countryCode)){
            publishText.setText("同步中....");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countryCode);

        }else{
            showWeather();

        }

        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.switch_city:
                Intent it = new Intent(this,ChooseAreaActivity.class);
                it.putExtra("from_weather_activity", true);
                startActivity(it);
                finish();
                break;
            case R.id.refresh_weather:
                publishText.setText("同步中...");
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = sp.getString("weather_code","");
                if(!TextUtils.isEmpty(weatherCode)){
                    queryWeatherInfo(weatherCode);
                }
                break;
            default:
                break;
        }

    }

    private void queryWeatherCode(String countryCode){
        String address="http://www.weather.com.cn/data/list3/city" +
                countryCode + ".xml";
        queryFromServer(address,"countryCode");

    }


    private void queryWeatherInfo(String weatherCode){
        String address="http://www.weather.com.cn/data/cityinfo/" +
                weatherCode + ".html";
        queryFromServer(address,"weatherCode");

    }

    private void queryFromServer(final String address,final String type){
        HttpUtil.setHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if("countryCode".equals(type)){
                    if(!TextUtils.isEmpty(response)){
                        String[] array = response.split("\\|");
                        if(array!=null&&array.length ==2){
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                }else if("weatherCode".equals(type)){
                    Ultity.saveWeatherInfo(WeatherActivity.this,response);
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
                        publishText.setText("同步失败");
                    }
                });
            }
        });
    }

    private void showWeather(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(sp.getString("city_name",""));
        temp1Text.setText(sp.getString("temp1", ""));
        temp2Text.setText(sp.getString("temp2", ""));
        weatherDespText.setText(sp.getString("weather_desp", ""));
        publishText.setText("今天" + sp.getString("publish_time", "") + "发布");
        currentDateText.setText(sp.getString("current_date", ""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
        Intent it = new Intent(this, AutoUpdateService.class);
        startService(it);
    }
}
