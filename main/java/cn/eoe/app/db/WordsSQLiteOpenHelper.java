package cn.eoe.app.db;

/**单词数据库的帮助类
 * Created by 徐启 on 2019/4/12.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WordsSQLiteOpenHelper extends SQLiteOpenHelper {

    //建表
    private final String CREATE_WORDS = "create table Words(" +
            "id Integer primary key autoincrement," +
            "isChinese text," +
            "key text," +
            "fy text," +
            "psE text," +
            "pronE text," +
            "psA text," +
            "pronA text," +
            "posAcceptation text," +
            "sent text)";

    public WordsSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_WORDS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            switch(oldVersion){
                case 1:
                    db.execSQL("v1版本时数据库升级操作"); //旧版本的数据库神级操作的case语句后都没有break语句，
                    //这样便可以确保每一次升级当前版本到最新版本的更新操作都可以被执行到了
                case 2:
                    db.execSQL("v1——v2版本数据库升级操作");
                case 3:
                    db.execSQL("v2——v3版本数据库升级操作");
                    //v3版本为当前最新数据库版本
                    break;
            }
        }


    }

