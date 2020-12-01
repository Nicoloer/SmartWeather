package com.example.smartweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.Cursor.*;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;



public class MainActivity extends AppCompatActivity {
    private DBhelper dbhelper;
    private Weather w;
    private TextView textView;
    private SQLiteDatabase dbWeather;
    String city,adcode,weather,reporttime,temperature,winddirection,humidity,windpower;
    String province="xxxx";
    String responsedata="";
    String staradcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbhelper = new DBhelper(this, "weather.db", null, 1);
        dbWeather = dbhelper.getWritableDatabase();

        Button query = findViewById(R.id.query);
        Button starcity=findViewById(R.id.starcity);
        Button choosecity=findViewById(R.id.city);
        final EditText adcodeview = findViewById(R.id.editText);

//        Log.i("查询城市的adcode", staradcode);
        query.setOnClickListener(new View.OnClickListener() {
//                    先判断数据库中是否存在该adcode 若存在则读出不进行访问查询

            @Override
            public void onClick(View v) {
                staradcode = adcodeview.getText().toString();
                Log.i("查询城市的adcode", staradcode);
//                先判断数据库中是否存在该adcode若存在则读出不进行访问查询
               if(!isexisted(staradcode)) {
                   Log.e("判断adcode", "该城市不是星标城市，在线查询" );
//      获取数据（先初始化为null）
                   getDataAsync(staradcode, new VolleyCallback() {
                       @Override
                       public void onSuccess(String result) {
                           try {
                               parseJSONWithJSONObject(responsedata);
                               Log.e("json数据处理", "处理完成");

                           } catch (JSONException e) {
                               e.printStackTrace();
                           }
                       }

                   });

               }
               else
                   intent();



            }
        });

        starcity.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,StartActivity.class);
                startActivity(intent);
            }
        });

        choosecity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,chooseprovince.class);
                startActivity(intent);
            }
        });

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
                    Log.e("MainActivity", "onSucceed: 获取数据成功");
                    responsedata = response.body().string();
                    Log.e("获取到在线信息", "onResponse: "+responsedata );
                    callback.onSuccess(responsedata);
                }
            }
        });
    }
    interface VolleyCallback {
        void onSuccess(String result);
    }
//不运行主线程
    private void showResponse(final String response) {
//        更新UI操作在主线程
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                textView.setText(response);
//            }
//        });
    }

    private void parseJSONWithJSONObject(String jsondata) throws JSONException {
        try {
            JSONObject jsonObject = new JSONObject(jsondata);
            JSONArray jsonArray = jsonObject.getJSONArray("lives");
//            每一次只会查询一条信息
                JSONObject j = jsonArray.getJSONObject(Integer.parseInt("0"));
                if(j.getString("province").equals(""))
                    Toast.makeText(MainActivity.this, "该城市id不存在", Toast.LENGTH_SHORT).show();
//                    //返回界面
                    else{
                    province = j.getString("province");
                    city = j.getString("city");
                    adcode = j.getString("adcode");
                    weather = j.getString("weather");
                    temperature = j.getString("temperature");
                    winddirection = j.getString("winddirection");
                    windpower = j.getString("windpower");
                    humidity = j.getString("humidity");
                    reporttime = j.getString("reporttime");
                    Log.e("parseJSON", "parseJSONWithJSONObject: " + city + " adcode:" + adcode);
                    intent();
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void intent(){

        Intent intent = new Intent(MainActivity.this, weather_info.class);
        intent.putExtra("province", province);
        intent.putExtra("city", city);
        intent.putExtra("adcode", adcode);
        intent.putExtra("weather", weather);
        intent.putExtra("temperature", temperature);
        intent.putExtra("winddirection", winddirection);
        intent.putExtra("windpower", windpower);
        intent.putExtra("reporttime", reporttime);
        intent.putExtra("humidity", humidity);
        startActivity(intent);
    }

//    判断是否存在于数据库
    private boolean isexisted(String stradcode){
        dbWeather = dbhelper.getWritableDatabase();
        Cursor cursor=dbWeather.rawQuery("select * from weather where adcode=?",new String[]{stradcode});
        if(cursor.getCount()!=0){
            Log.e("isexisted", "记录条数"+cursor.getCount() );
            Log.e("isexisted", " 数据库存有该城市");
            if (cursor.moveToFirst()) {
                do {
                    province=cursor.getString(cursor.getColumnIndex("province"));
                    city=cursor.getString(cursor.getColumnIndex("city"));
                    adcode=cursor.getString(cursor.getColumnIndex("adcode"));
                    weather=cursor.getString(cursor.getColumnIndex("weather"));
                    temperature=cursor.getString(cursor.getColumnIndex("temperature"));
                    winddirection=cursor.getString(cursor.getColumnIndex("winddirection"));
                    windpower=cursor.getString(cursor.getColumnIndex("windpower"));
                    humidity=cursor.getString(cursor.getColumnIndex("humidity"));
                    reporttime=cursor.getString(cursor.getColumnIndex("reporttime"));
//
                    Log.e("isexisted()", "是否存于数据库" +"city"+city+ " adcode:" + adcode);
//
                     } while (cursor.moveToNext());
            }
            cursor.close();
            return true;
        }
        else {
            Log.e("isexisted", " 数据库没有该城市");
            return false;
        }
    }
}

