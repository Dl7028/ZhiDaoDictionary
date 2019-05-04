package cn.eoe.app.analytic;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 用于获取每日一句的Json格式解析
 *
 * Created by 徐启 on 2019/4/24.
 */

public class ParseSentenceJSON {
    public static String audioAddress;
    public static String pictureAddress ;

    public static String getJsonResult( InputStream inputStream) {

        String trans = "" ;
        String date = "";

        BufferedReader buf = new BufferedReader(new InputStreamReader(inputStream));//包装为字符流
        StringBuilder response = new StringBuilder();
        String line;

        try {
            while ((line = buf.readLine()) != null) {
                response.append(line);
            }

            JSONObject jsonObject = new JSONObject(String.valueOf(response));

            trans += jsonObject.getString("dateline")+"<br>";                                     //时间
            trans+= "<br><h1></h1>";
            trans+= "<br><h1></h1>";
            trans+= "<br><h1></h1>";
            trans+= "<br><h1></h1>";
            trans += jsonObject.getString("content")+"<br>";                                      //每日一句英文
            trans+= "<br><h1></h1>";

            trans += jsonObject.getString("note")+"<br>";                                          //每日一句中文翻译
            trans+= "<br><h1></h1>";
            trans+= "<br><h1></h1>";
            trans+= "<br><h1></h1>";
            trans += jsonObject.getString("translation")+"<br>";                                  //小编的话

            audioAddress = jsonObject.getString("tts");

           pictureAddress = jsonObject.getString("picture2");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

       return trans;
    }
}
