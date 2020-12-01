package com.example.smartweather;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class DBhelper extends SQLiteOpenHelper {

    public static final String CREATE_TABLE_Weather=
            "create table weather(" +
                    "id integer primary key autoincrement, "
                    +"star boolean default 'false',"
                    +"province text,"
                    +"city text,"
                    +"adcode text,"
                    +"weather text,"
                    +"temperature text,"
                    +"humidity text,"
                    +"windpower text,"
                    +"winddirection text,"
                    +"reporttime text)" ;

    private Context mContext;
    public DBhelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_Weather);
//        Toast.makeText(mContext,"create succeeded!",Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists weather");
        db.execSQL(CREATE_TABLE_Weather);

    }
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

}
