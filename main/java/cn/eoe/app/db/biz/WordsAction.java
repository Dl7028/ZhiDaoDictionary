package cn.eoe.app.db.biz;

/** 查词的工具类，内部有方法：
 * 1.保存words到数据库
 * 2.获取address地址
 * 3.向数据库中查找words
 * 4.保存发音mp3文件
 * 5.播放发音MP3
 * Created by 徐启 on 2019/4/12.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import cn.eoe.app.utils.FileUtil;
import cn.eoe.app.https.HttpCallBackListener;
import cn.eoe.app.https.HttpUtil;
import cn.eoe.app.db.WordsSQLiteOpenHelper;
import cn.eoe.app.model.Words;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * 单例类
 * 在整个应用生命周期中，要保证只有一个单例类的实例被创建
 */
public class WordsAction {

    private static WordsAction wordsAction;                                                       //本类的实例
    private final String TABLE_WORDS = "Words";                                                 //表名

    public static SQLiteDatabase db;                                                               //数据库实例
    private MediaPlayer player = null;                                                            //MediaPlayer实例

    //私有的构造器， 将构造方法私有化,不允许外部直接创建对象
    private WordsAction(Context context) {
        WordsSQLiteOpenHelper helper = new WordsSQLiteOpenHelper(context, TABLE_WORDS, null,1);
        db = helper.getWritableDatabase();                                                          //创建数据库
    }

    /**
     * 单例类WordsAction获取实例方法
     * 双重效验锁，提高性能
     * 1.检查变量是否被初始化(不去获得锁)，如果已被初始化立即返回这个变量。
     * 2.获取锁
     * 3.第二次检查变量是否已经被初始化：如果其他线程曾获取过锁，那么变量已被初始化，返回初始化的变量。
     * 4.否则，初始化并返回变量。
     *
     * @param context 上下文
     */
    public static WordsAction getInstance(Context context) {
        if (wordsAction == null) {
            //线程开始执行同步代码块之前，必须先获得对同步代码块的锁定。
            // 任何时刻只能有一个线程可以获得对(WordsAction.class)同步监视器的锁定，当同步代码块执行完成后，该线程会释放对该同步监视器的锁定
            synchronized (WordsAction.class) {                                                   //同步代码块
                if (wordsAction == null) {                                                        // 可能会有多个线程一起进入同步块外的 if，如果在同步块内不进行二次检验的话就会生成多个实例了。
                    wordsAction = new WordsAction(context);
                }
            }
        }
        return wordsAction;
    }

    /**
     * 向数据库中保存新的Words对象
     * 会先对word进行判断，为有效值时才会保存
     *
     * @param words 单词类的实例
     */
    public boolean saveWords(Words words) {
        //判断是否是有效对象，即有数据
        if (words.getSent().length() > 0) {
            ContentValues values = new ContentValues();                                             //创建ContentValues对象存储键值对
            values.put("isChinese", "" + words.getIsChinese());
            values.put("key", words.getKey());
            values.put("fy", words.getFy());
            values.put("psE", words.getPsE());
            values.put("pronE", words.getPronE());
            values.put("psA", words.getPsA());
            values.put("pronA", words.getPronA());
            values.put("posAcceptation", words.getPosAcceptation());
            values.put("sent", words.getSent());
            db.insert(TABLE_WORDS, null, values);                                                 //在表中添加数据
            values.clear();                                                                         //删除values中所有的值
            return true;
        }
        return false;
    }

    /**
     * 从数据库中查找查询的words
     *
     * @param key 查找的值
     * @return words 若返回words的key为空，则说明数据库中没有该词
     */
    public Words getWordsFromSQLite(String key) {
        Words words = new Words();
        Cursor cursor = db.query(TABLE_WORDS, null, "key=?", new String[]{key}, null, null, null);    //数据库中的查找操作，一行一行的查找
        //数据库中有
        if (cursor.getCount() > 0) {
            Log.d("测试", "数据库中有");
            if (cursor.moveToFirst()) {
                do {
                    String isChinese = cursor.getString(cursor.getColumnIndex("isChinese"));
                    if ("true".equals(isChinese)) {
                        words.setIsChinese(true);
                    } else if ("false".equals(isChinese)) {
                        words.setIsChinese(false);
                    }
                    words.setKey(cursor.getString(cursor.getColumnIndex("key")));
                    words.setFy(cursor.getString(cursor.getColumnIndex("fy")));
                    words.setPsE(cursor.getString(cursor.getColumnIndex("psE")));
                    words.setPronE(cursor.getString(cursor.getColumnIndex("pronE")));
                    words.setPsA(cursor.getString(cursor.getColumnIndex("psA")));
                    words.setPronA(cursor.getString(cursor.getColumnIndex("pronA")));
                    words.setPosAcceptation(cursor.getString(cursor.getColumnIndex("posAcceptation")));
                    words.setSent(cursor.getString(cursor.getColumnIndex("sent")));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } else {
            Log.d("测试", "数据库中没有");
            cursor.close();
        }
        return words;
    }

    public ArrayList<String> getWordsFromSQLiteToRecordsList() {
        ArrayList<String> arrayList = null;
        arrayList = new ArrayList<>();

        Cursor cursor = db.query(TABLE_WORDS, new String[]{"key"}, null, null, null, null, null);    //数据库中的查找操作，一行一行的查找
        Log.i("在dao","if前");
        if (cursor.getCount() > 0) {
            Log.i("测试", "行数不为0");
            while (cursor.moveToNext()) {                                                            //光标移到一下行
                Log.i("cursor", "有下一行");

                String key1 = cursor.getString(cursor.getColumnIndex("key"));
                System.out.println("dao到List中的key"+key1);
                arrayList.add(key1);
                System.out.println("未返回的arrayList"+arrayList);
            }
        }
        return arrayList;
    }




    /**
     * 获取网络查找单词的对应地址
     *
     * @param key 要查询的单词
     * @return address 所查单词对应的http地址
     */
    public String getAddressForWords(final String key) {
        String address_p1 = "http://dict-co.iciba.com/api/dictionary.php?w=";
        String address_p2 = "";
        String address_p3 = "&key=E568F04171398072F7EC5D8B4A6CBDB4";
        if (isChinese(key)) {
            try {
                //对中文的key进行重新编码，生成正确的网址
                address_p2 = "_" + URLEncoder.encode(key, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            address_p2 = key;
        }
        return address_p1 + address_p2 + address_p3;

    }

    //翻译句子的地址
   public String getAddressForSentence(final  String key){
       String address1 = "http://fanyi.youdao.com/openapi.do?keyfrom=dictionaryTestqq&key=416582248&type=data&doctype=json&version=1.1&q=";
       String address2 = key;
       return  address1+address2;
   }

    /**
     * 判断是否是中文
     *
     * @param strName String类型的字符串
     */
    public static boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据Unicode编码判断中文汉字和符号
     *
     * @param c char类型的字符串
     */
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    /**
     * 保存words的发音MP3文件到SD卡
     * 先请求Http，成功后保存
     *
     * @param words words实例
     */
    public void saveWordsMP3(Words words) {
        String addressE = words.getPronE();
        String addressA = words.getPronA();
        if (addressE != "") {
            final String filePathE = words.getKey();
            HttpUtil.sentHttpRequest(addressE, new HttpCallBackListener() {
                @Override
                public void onFinish(InputStream inputStream) {
                    FileUtil.getInstance().writeToSD(filePathE, "E.mp3", inputStream);
                }
                @Override
                public void onError() {

                }
            });
        }
        if (addressA != "") {
            final String filePathA = words.getKey();
            HttpUtil.sentHttpRequest(addressA, new HttpCallBackListener() {
                @Override
                public void onFinish(InputStream inputStream) {
                    //保存发音地址到本地，实现单词本离线获取单词发音
                    FileUtil.getInstance().writeToSD(filePathA, "A.mp3", inputStream);
                    System.out.println(filePathA);
                }

                @Override
                public void onError() {

                }
            });
        }
    }

    /**
     * 播放words的发音
     *
     * @param wordsKey 单词的key
     * @param ps       E 代表英式发音
     *                 A 代表美式发音
     * @param context  上下文
     */
    public void playMP3(String wordsKey, String ps, Context context) {

        String fileName = wordsKey + "/" + ps + ".mp3";
        String adrs = FileUtil.getInstance().getPathInSD(fileName);                                 //获取本地MP3地址
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
            player = null;
        }
        if (adrs != "") {                                                                           //有内容则播放
            player = MediaPlayer.create(context, Uri.parse(adrs));
            player.start();
        } else {                                                                                   //没有内容则重新去下载
            Words words = getWordsFromSQLite(wordsKey);
            saveWordsMP3(words);
        }
    }


}