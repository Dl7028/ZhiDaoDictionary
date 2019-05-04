package cn.eoe.app.db.biz;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import cn.eoe.app.db.NoteSQLiteOpenHelper;

import cn.eoe.app.model.Words;

import java.util.ArrayList;

/**
 * 生词本单词操作类
 * 1.把单词放入Note表中
 * 2.在Note表中取出
 * Created by 徐启 on 2019/4/21.
 */

public class NoteWordsAction {

    private final String TABLE_NOTE = "Note";                                                   //表名
    public static SQLiteDatabase db;                                                                     //数据库实例

    public NoteWordsAction(Context context) {
        NoteSQLiteOpenHelper helper = new NoteSQLiteOpenHelper(context, TABLE_NOTE, null, 2);
        db = helper.getWritableDatabase();                                                          //创建数据库
    }
    /**
     * table:表名，不能为null
     *columns:要查询的列名，可以是多个，可以为null，表示查询所有列
     *selection:查询条件，比如id=? and name=? 可以为null
     *selectionArgs:对查询条件赋值，一个问号对应一个值，按顺序 可以为null
     *having:语法have，可以为null
     *orderBy：语法，按xx排序，可以为null
     * @return
     */
    //保存单词的key，posAcceptation到NOTE
    public void saveWordsToNote(Words words) {
        Cursor cursor = db.query(TABLE_NOTE, null, "key=?", new String[]{words.getKey()}, null, null, null);    //数据库中的查找操作，一行一行的查找
        if(cursor.getCount()>0){
            return;
        }else {
            ContentValues values = new ContentValues();                       //创建ContentValues对象存储键值对
            values.put("key", words.getKey());
            values.put("posAcceptation", words.getPosAcceptation());
            db.insert(TABLE_NOTE, null, values);                          //在表中添加数据
            values.clear();                                                  //删除values中所有的值
        }
    }

    //取出NOTE表中所有的单词
    public ArrayList<Words> getWordsFromSQLiteToList() {
        ArrayList<Words> arrayList = null;
        arrayList = new ArrayList<>();

        Cursor cursor = db.query(TABLE_NOTE, null, null, null, null, null, null);    //数据库中的查找操作，一行一行的查找
        Log.i("在dao","if前");
        if (cursor.getCount() > 0) {
            Log.i("测试", "行数不为0");
            while (cursor.moveToNext()) {                                                            //光标移到一下行
                Log.i("cursor", "有下一行");
                String key1 = cursor.getString(cursor.getColumnIndex("key"));
                System.out.println("dao到List中的key"+key1);
                String posAcceptation = cursor.getString(cursor.getColumnIndex("posAcceptation"));
                System.out.println("dao到List中的posAcceptation"+posAcceptation);
                Words words = new Words(key1, posAcceptation);
                arrayList.add(words);
                System.out.println("未返回的arrayList"+arrayList);
            }
        }
        return arrayList;
    }

}
