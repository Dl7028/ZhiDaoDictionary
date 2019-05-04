package cn.eoe.app.https;

/**
 * Created by 徐启 on 2019/4/12.
 * Http访问的回调接口
 */

import java.io.InputStream;


public interface HttpCallBackListener {

    void onFinish(InputStream inputStream);  //当Http访问完成时回调onFinish方法

    void onError();                          //当Http访问失败时回调onError方法
}