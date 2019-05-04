package cn.eoe.app.analytic;




import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 用于翻译句子的JSON解析
 * Created by 徐启 on 2019/4/13.
 */

public class ParseJSON {

    //解析JSON格式，返回字符串
    public static String getJsonResult( InputStream inputStream) {
        String trans = "";
        String  explains = "";

        BufferedReader buf = new BufferedReader(new InputStreamReader(inputStream));               //包装为字符流
        StringBuilder response = new StringBuilder();
        String line;

        //读取
        try {
            while ((line = buf.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            JSONObject jsonObject = new JSONObject(String.valueOf(response));
            trans += "翻译:" + jsonObject.getString("translation") + "<br>";
            trans += explains;
            trans += "<br><h1><font color='red'>网络释义:</font></h1>";
            JSONArray webObject = jsonObject.getJSONArray("web");

            for (int i = 0; i < webObject.length(); i++) {
                JSONObject oj = (JSONObject) webObject.get(i);
                trans += i + 1 + "." + oj.getString("key") + "<br>";
                trans += oj.getString("value") + "<br>";
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return trans;
    }
}
