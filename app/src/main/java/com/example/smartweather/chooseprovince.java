package com.example.smartweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class chooseprovince extends AppCompatActivity {
//    查询到的数据
String responsedata;
ListView listView;
//     查询到的省份名
String name,adcode;
private ArrayList<String> provincelist=new ArrayList<String>();
private ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooseprovince);
        listView = findViewById(R.id.listView);

//callback调用
        getDataAsync(new VolleyCallback() {
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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("点击事件position", String.valueOf(position));
                String currentprovince;
                currentprovince= (String) provincelist.get(position);
//       显示单篇日记的内容

                Intent intent = new Intent(chooseprovince.this, choosecity.class);
//为下一个活动传递点击的diary的数据
                intent.putExtra("province",currentprovince);
                startActivity(intent);
            }
        });

    }
    private void getDataAsync(final VolleyCallback callback) {
        OkHttpClient client = new OkHttpClient.Builder().
                connectTimeout(300, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .build();
//        发起http请求 请求全部省份的数据
        Request request = new Request.Builder()
                .url("https://restapi.amap.com/v3/config/district?key=e165c6e2f93f79b13b7c8fa7f95a2ebf&keywords=&subdistrict=1&extensions=base")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("", "onFailure: 获取数据失败");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
//                    Toast.makeText(MainActivity.this, "获取数据成功", Toast.LENGTH_SHORT).show();
//                    //返回界面
                    Log.e("查看省份", "onSucceed: 获取数据成功");
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
    private void showResponse() {
//        更新UI操作在主线程
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e("主线程", "run:！！！" );
                adapter = new ArrayAdapter<String>(chooseprovince.this, R.layout.weather_item,R.id.item, provincelist);
                listView.setAdapter(adapter);
            }
        });
    }
    private void parseJSONWithJSONObject(String jsondata) throws JSONException {
        try {
            JSONObject jsonObject = new JSONObject(jsondata);
            JSONArray jsonArray = jsonObject.getJSONArray("districts");
//            每一次只会查询一条信息
            JSONObject jsonObject1 = jsonArray.getJSONObject(0);
            JSONArray jsonArray1 = jsonObject1.getJSONArray("districts");
            for(int i=0;i<jsonArray1.length();i++){
                JSONObject j= jsonArray1.getJSONObject(i);
                name=j.getString("name");
                Log.e("省份名", "parseJSONWithJSONObject: "+name);
                provincelist.add(name);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}