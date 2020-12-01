package com.example.smartweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class StartActivity extends AppCompatActivity {
    private DBhelper dbhelper;
//    private Weather w;
    private SQLiteDatabase dbWeather;
    private Adapter adapter;
    private List<Weather> weathers=new ArrayList<>();
    String province;
    String city;
    String adcode;
    String weather;
    String reporttime;
    String temperature;
    String winddirection;
    String humidity;
    String windpower;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        dbhelper = new DBhelper(this, "weather.db", null, 1);
        dbWeather = dbhelper.getWritableDatabase();
//       加载布局
        Button buttonAdd = (Button) findViewById(R.id.add);
        ListView listView=findViewById(R.id.listView);
//        初始化DiaryList
        initDiarys();
        Log.d("weathers", weathers.toString());
//        配置Adapter
        adapter=new Adapter(StartActivity.this,R.layout.weather_item,weathers);

        listView.setAdapter(adapter);

//       listview 点击事件到UPdateweather
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("点击事件position", String.valueOf(position));
//       显示单篇日记的内容
                Intent intent=new Intent(StartActivity.this, weather_info.class);
//为下一个活动传递点击的diary的数据
                Weather w1= new Weather();
                w1=weathers.get(position);
                intent.putExtra("province",province);
                intent.putExtra("city",city);
                intent.putExtra("adcode", adcode);
                intent.putExtra("weather",weather);
                intent.putExtra("temperature", temperature);
                intent.putExtra("reporttime",reporttime);
                startActivity(intent);
            }
        });

    }
    public void initDiarys(){
//        先寻找到最新的天气情况更新到数据库中
        Cursor cursor=dbWeather.rawQuery("select * from weather where star=?",new String[]{"1"});
        weathers.clear();
        if (cursor.moveToFirst()) {
            do {
                province=cursor.getString(cursor.getColumnIndex("province"));
                city=cursor.getString(cursor.getColumnIndex("city"));
                adcode=cursor.getString(cursor.getColumnIndex("adcode"));
                weather=cursor.getString(cursor.getColumnIndex("weather"));
                temperature=cursor.getString(cursor.getColumnIndex("temperature"));
                reporttime=cursor.getString(cursor.getColumnIndex("reporttime"));
                Weather w=new Weather();
                w.setProvince(province);w.setCity(city);w.setAdcode(adcode);w.setWeather(weather);w.setTemperature(temperature);w.setReporttime(reporttime);
                weathers.add(w);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }
}