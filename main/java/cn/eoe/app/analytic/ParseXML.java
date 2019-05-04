package cn.eoe.app.analytic;

/**
 * 用于解析单词翻译的XML解析
 * Created by 徐启 on 2019/4/12.
 *
 */

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.SAXParserFactory;

public class ParseXML {

     // 使用SAX解析XML的方法
    public static void parse(DefaultHandler handler, InputStream inputStream) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");     //得到xml的输出流
            BufferedReader reader = new BufferedReader(inputStreamReader);                          //处理流
            SAXParserFactory factory = SAXParserFactory.newInstance();                              //得到SAX解析工厂
            XMLReader xmlReader = factory.newSAXParser().getXMLReader();                            //由newSAXParser()得到输入流
            xmlReader.setContentHandler(handler);                                                   //传入输入流和handler给解析器
            xmlReader.parse(new InputSource(reader));                                               //解析
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
