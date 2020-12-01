package com.example.smartweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class weather_info extends AppCompatActivity {
    private SQLiteDatabase dbWeather;
    private DBhelper dbhelper;
    private TextView locationview,adcodeview,weatherview,temperatureview,reporttimeview;
    String responsedata="";
    String province,city,adcode,weather,reporttime,temperature,winddirection,humidity,windpower;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_info);
        dbhelper=new DBhelper(this,"weather.db",null,1);
        dbWeather = dbhelper.getWritableDatabase();
//      加载界面元素
        locationview=findViewById(R.id.location);
        adcodeview=findViewById(R.id.adcode);
        weatherview=findViewById(R.id.weather);
        temperatureview=findViewById(R.id.temperature);
        reporttimeview=findViewById(R.id.reporttime);

        Button buttonStar=findViewById(R.id.star);
        Button buttonUpdate=findViewById(R.id.update);
        Intent intent=getIntent();
        province=intent.getStringExtra("province");
        city=intent.getStringExtra("city");
        adcode=intent.getStringExtra("adcode");
        weather=intent.getStringExtra("weather");
        temperature=intent.getStringExtra("temperature");
        reporttime=intent.getStringExtra("reporttime");
        winddirection=intent.getStringExtra("winddirection");
        windpower=intent.getStringExtra("windpower");
        humidity=intent.getStringExtra("humidity");
        locationview.setText(province+"\u3000"+city);
        adcodeview.setText(adcode);
        weatherview.setText(weather);
        temperatureview.setText(temperature);
        reporttimeview.setText(reporttime);
        Cursor cursor=dbWeather.rawQuery("select * from weather where adcode=?",new String[]{adcode});
        if(cursor.getCount()!=0)
            buttonStar.setText("取消星标");

        buttonStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values=insert();
                Cursor cursor=dbWeather.rawQuery("select * from weather where adcode=?",new String[]{adcode});
                if(cursor.getCount()==0)
//                    没有数据
                    dbWeather.insert("weather", null, values);
                else
//                    dbWeather.update("weather",values,"adcode=?",new String[]{adcode});
                    dbWeather.execSQL("delete from weather where adcode="+adcode);
            }
        });
//点击更新重新获取当前城市的天气情况
//再检查是否该城市为星标城市若是应当更新天气情况
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDataAsync(adcode, new VolleyCallback(){
                    @Override
                    public void onSuccess(String result) {
                        try {
                            parseJSONWithJSONObject(responsedata);
                            Log.e("json数据处理", "处理完成");
                            showResponse();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                Cursor cursor=dbWeather.rawQuery("select * from weather where adcode=?",new String[]{adcode});
                if(cursor.getCount()!=0) {
                    ContentValues values = insert();
                    dbWeather.update("weather", values, "adcode=?", new String[]{adcode});
                }
            }
        });
    }
    private ContentValues insert(){
        ContentValues values = new ContentValues();
        values.put("province",province);
        values.put("star",true);
        values.put("city", city);
        values.put("adcode",adcode );
        values.put("weather",weather );
        values.put("winddirection",winddirection);
        values.put("windpower",windpower);
        values.put("humidity",humidity);
        values.put("reporttime",reporttime);
        values.put("temperature", temperature);
        return values;
    }

    private void getDataAsync(String staradcode,final VolleyCallback callback) {
        OkHttpClient client = new OkHttpClient.Builder().
                connectTimeout(300, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .build();
//        发起http请求
        Request request = new Request.Builder()
                .url("https://restapi.amap.com/v3/weather/weatherInfo?city="+staradcode+"&key=e165c6e2f93f79b13b7c8fa7f95a2ebf")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("MainActivity", "onFailure: 获取数据失败");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
//                    Toast.makeText(MainActivity.this, "获取数据成功", Toast.LENGTH_SHORT).show();
//                    //返回界面
//                    Log.e("MainActivity", "onSucceed: 获取数据成功");
                    responsedata = response.body().string();
                    Log.e("更新数据", "onResponse: "+responsedata );
                    callback.onSuccess(responsedata);
                }
            }
        });
    }
    interface VolleyCallback {
        void onSuccess(String result);
    }
    private void showResponse() {
//        更新UI操作在主线程
        Log.e("更新UI操作在主线程", "处理完成");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                locationview.setText(province+"\u3000"+city);
                adcodeview.setText(adcode);
                weatherview.setText(weather);
                temperatureview.setText(temperature);
                reporttimeview.setText(reporttime);
            }
        });
    }

    private void parseJSONWithJSONObject(String jsondata) throws JSONException {
        try {
            JSONObject jsonObject = new JSONObject(jsondata);
            JSONArray jsonArray = jsonObject.getJSONArray("lives");
            JSONObject j = jsonArray.getJSONObject(Integer.parseInt("0"));
            province = j.getString("province");
            city = j.getString("city");
            adcode = j.getString("adcode");
            weather = j.getString("weather");
            temperature = j.getString("temperature");
            winddirection = j.getString("winddirection");
            windpower=j.getString("windpower");
            humidity=j.getString("humidity");
            reporttime=j.getString("reporttime");
            Log.d("parseJSON", "parseJSONWithJSONObject: " + city + " adcode:" + adcode);
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed(){
        Intent intent=new Intent(weather_info.this,MainActivity.class);
        finish();
    }
}