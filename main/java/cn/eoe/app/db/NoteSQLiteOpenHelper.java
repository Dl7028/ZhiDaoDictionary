package cn.eoe.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 单词本数据库帮助类
 * Created by 徐启 on 2019/4/21.
 */

public class NoteSQLiteOpenHelper extends SQLiteOpenHelper {


    private final String CREATE_NOTE = "create table Note(" +
            "id Integer primary key autoincrement," +
            "key text,"+
            "posAcceptation text)";

    public NoteSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_NOTE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
