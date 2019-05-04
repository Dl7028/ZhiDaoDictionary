package cn.eoe.app.utils;

/**
 * 文件操作类
 * 1.保存发音地址到本地
 *
 * Created by 徐启 on 2019/4/12.
 */

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//文件存储的工具类
public class FileUtil {

    private String SDPath;                                                                         //SD卡目录
    private String AppPath;                                                                        //app目录
    private static FileUtil fileUtil;                                                              //本类的单例


    private FileUtil() {
        Log.i("测试","有没有SD卡");
        //如果手机已插入SD卡，且应用程序具有读写SD卡的功能，则返回true
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            SDPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
            Log.i("测试","有SD卡");
            File fileV = createSDDir(SDPath, "VocabularyBuilder");
            AppPath = fileV.getAbsolutePath() + "/";
        }
    }

    //双重检测，跟WordsAction获取单例一样
    public static FileUtil getInstance() {                                                        //获取FileUtil的方法
        if (fileUtil == null) {
            synchronized (FileUtil.class) {
                if (fileUtil == null) {
                    fileUtil = new FileUtil();
                }
            }
        }
        return fileUtil;
    }

    /**
     * 创建目录
     *
     * @param path    文件夹的路径
     * @param dirName 文件夹名
     */
    public File createSDDir(String path, String dirName) {
        File dir = new File(path + dirName);
        if (dir.exists() && dir.isDirectory()) {
            return dir;
        }
        dir.mkdir();
        Log.d("测试", "创建目录成功");
        System.out.println(dir);
        return dir;
    }

    /**
     * 创建SD文件
     *
     * @param path     文件的路径
     * @param fileName 文件名
     */
    public File createSDFile(String path, String fileName) {
        File file = new File(path+fileName);
        if (file.exists() && file.isFile()) {
            Log.i("7","7");
            return file;
        }
        try {
            file.createNewFile();
            Log.d("测试", "创建文件成功");
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("8","8");
        }
        return file;
    }

    /**
     * 向SD卡中写入文件
     *
     * @param path        文件夹名
     * @param fileName    文件名
     * @param inputStream 输入流
     */
    public void writeToSD(String path, String fileName, InputStream inputStream) {
        OutputStream outputStream = null;
        try {
            File dir = createSDDir(AppPath, path);
            Log.i("5","5");

            File file = createSDFile(dir.getAbsolutePath() + "/", fileName);
            System.out.println(file);
            Log.i("6","6");
            outputStream = new FileOutputStream(file);
            int length;
            byte[] buffer = new byte[2 * 1024];
            while ((length = inputStream.read(buffer)) != -1) {                                    //利用read返回的实际成功读取的字节数，将buffer写入文件，
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            Log.d("测试", "写入成功");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("测试", "写入失败");
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取文件在SD卡上绝对路径，如无该文件返回""
     * @param fileName 单词对应的文件夹名
     */
    public String getPathInSD(String fileName) {                                                   //查找文件然后返回
        File file = new File(AppPath + fileName);
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        return "";
    }

}


