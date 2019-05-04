package cn.eoe.app.model;

import java.util.Comparator;

/**
 *
 * 定义Words类
 * Created by 徐启 on 2019/4/12.
 */


public class Words implements Comparator<Words> {

    private boolean isChinese;                                                                   //判断输入的是中文还是英文
    private String key;                                                                            //key英文单词
    private String fy;                                                                             //fy中文单词
    private String psE;                                                                            //英式发音
    private String pronE;                                                                          //英式发音的mp3地址
    private String psA;                                                                            //美式发音
    private String pronA;                                                                         //美式发音的mp3地址
    private String posAcceptation;                                                               //单词的基本释义
    private String sent;                                                                           //例句

    public Words() {
        this.key = "";
        this.fy = "";
        this.psE = "";
        this.pronE = "";
        this.psA = "";
        this.pronA = "";
        this.posAcceptation = "";
        this.sent = "";
        this.isChinese = false;

    }
    public Words(String key){
        this.key = key ;
    }

    public Words(String key,String posAcceptation){
        this.key = key ;
        this.posAcceptation = posAcceptation;
    }

    public Words(boolean isChinese, String key, String fy, String psE,
                 String pronE, String psA, String pronA, String posAcceptation, String sent) {
        this.isChinese = isChinese;
        this.key = key;
        this.fy = fy;
        this.psE = psE;
        this.pronE = pronE;
        this.psA = psA;
        this.pronA = pronA;
        this.posAcceptation = posAcceptation;
        this.sent = sent;
    }

    public boolean getIsChinese() {
        return isChinese;
    }

    public void setIsChinese(boolean isChinese) {
        this.isChinese = isChinese;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getFy() {
        return fy;
    }

    public void setFy(String fy) {
        this.fy = fy;
    }

    public String getPsE() {
        return psE;
    }

    public void setPsE(String psE) {
        this.psE = psE;
    }

    public String getPronE() {
        return pronE;
    }

    public void setPronE(String pronE) {
        this.pronE = pronE;
    }

    public String getPsA() {
        return psA;
    }

    public void setPsA(String psA) {
        this.psA = psA;
    }

    public String getPronA() {
        return pronA;
    }

    public void setPronA(String pronA) {
        this.pronA = pronA;
    }

    public String getPosAcceptation() {
        return posAcceptation;
    }

    public void setPosAcceptation(String posAcceptation) {
        this.posAcceptation = posAcceptation;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    @Override
    public int compare(Words o1, Words o2) {                                                       //重写compare(),实现单词首字母排序
        return o1.getKey().compareToIgnoreCase(o2.getKey());
    }

}
