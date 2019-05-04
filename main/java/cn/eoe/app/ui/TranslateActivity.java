package cn.eoe.app.ui;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.dictionary13.R;
import cn.eoe.app.https.HttpCallBackListener;
import cn.eoe.app.https.HttpUtil;
import cn.eoe.app.db.biz.NoteWordsAction;
import cn.eoe.app.analytic.ParseJSON;
import cn.eoe.app.analytic.ParseXML;
import cn.eoe.app.analytic.WordsHandler;
import cn.eoe.app.model.Words;
import java.io.InputStream;

import static cn.eoe.app.ui.HistoryRecordsActivity.wordsAction;
import static cn.eoe.app.db.biz.NoteWordsAction.db;

/**
 * 翻译活动
 * 1.单词与句子的翻译，可以获取单词发音
 *
 */

public class TranslateActivity extends Activity {
    private TextView searchWords_key, searchWords_psE, searchWords_psA, searchWords_posAcceptation, searchWords_sent;  //单词翻译，英式发音，美式发音，基本释义，例句
    private ImageButton searchWords_voiceE, searchWords_voiceA,button_collect;                                //发音按钮
    private LinearLayout searchWords_posA_layout,searchWords_posE_layout, searchWords_linerLayout, searchWords_fatherLayout; //发音线性排列，翻译线性排列，父局线性排列，考虑到可见和不可见
    public  static String kWord =null;                                                            //kWord：搜索的单词的临时变量
    private  int flag = 0;                                                                        //收藏变色功能的状态量
    public Words words = new Words();                                                             //Words实例

     // 网络查词完成后回调handleMessage方法
     private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 111:                                                                           //接收信息111，完成UI更新
                    if (words.getSent().length() > 0) {
                        upDateView();
                    } else {
                        searchWords_linerLayout.setVisibility(View.GONE);                        //判断网络查找不到该词的情况，View.GONE 翻译布局不可见
                        Toast.makeText(TranslateActivity.this, "抱歉！找不到该词！", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 112:
                    //句子翻译排版
                    CharSequence response = Html.fromHtml(msg.obj.toString());
                    String str = (String)msg.obj;
                    String str2 = str.replace(str.substring(0,str.indexOf("<")), "");
                    searchWords_posAcceptation.setText(str.substring(0,str.indexOf("<")));
                    CharSequence response2 = Html.fromHtml(str2.toString());
                    searchWords_sent.setText(response2);
                    upDateView02();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trans);                                                   //加载主布局
        final NoteWordsAction noteWordsAction = new NoteWordsAction(TranslateActivity.this);

        //初始化控件
        searchWords_linerLayout = (LinearLayout) findViewById(R.id.searchWords_linerLayout);
        searchWords_posA_layout = (LinearLayout) findViewById(R.id.searchWords_posA_layout);
        searchWords_posE_layout = (LinearLayout) findViewById(R.id.searchWords_posE_layout);
        searchWords_fatherLayout = (LinearLayout) findViewById(R.id.searchWords_fatherLayout);

        searchWords_fatherLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                                    //重写父布局onClick方法

                //点击输入框外实现软键盘隐藏
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });
        //初始化布局，控件变量
        searchWords_key = (TextView) findViewById(R.id.searchWords_key);
        searchWords_psE = (TextView) findViewById(R.id.searchWords_psE);
        searchWords_psA = (TextView) findViewById(R.id.searchWords_psA);
        searchWords_posAcceptation = (TextView) findViewById(R.id.searchWords_posAcceptation);
        searchWords_sent = (TextView) findViewById(R.id.searchWords_sent);
        //收藏点击事件
        button_collect = (ImageButton)findViewById(R.id.bt_collect);

        //若是翻译句子则隐藏收藏按钮
        if(kWord.trim().contains(" ")){
            button_collect.setVisibility(View.INVISIBLE);
        }

        //收藏点击事件
        button_collect = (ImageButton)findViewById(R.id.bt_collect);
        button_collect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //收藏变色功能
                switch (flag){
                    case 0:
                            button_collect.setActivated(true);
                            noteWordsAction.saveWordsToNote(words);                                    //数据库添加
                            Toast.makeText(TranslateActivity.this, "已收藏！", Toast.LENGTH_SHORT).show();
                            flag = 1;

                        break;
                    case 1:
                            button_collect.setActivated(false);
                            db.delete("Note", "key=?", new String[]{words.getKey()});                   //数据库删除
                            Toast.makeText(TranslateActivity.this, "已取消收藏！", Toast.LENGTH_SHORT).show();
                            flag = 0;
                        break;
                }
            }
        });

        searchWords_voiceE = (ImageButton) findViewById(R.id.searchWords_voiceE);
        searchWords_voiceE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordsAction.playMP3(words.getKey(), "E", TranslateActivity.this);                  //播放英式发音
            }
        });
        searchWords_voiceA = (ImageButton) findViewById(R.id.searchWords_voiceA);
        searchWords_voiceA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordsAction.playMP3(words.getKey(), "A", TranslateActivity.this);                  //播放美式发音
            }
        });

        loadWords(kWord); //查词

    }

    //读取words的方法，优先从数据中搜索，没有在通过网络搜索
    public void loadWords(final String key) {
        words = wordsAction.getWordsFromSQLite(key);
        String address = null;
        if ("" == words.getKey()) {                                                                //数据库中没有words数据
            if (key.contains(" ")) {                                                                //判断输入的是否为句子
                //是句子
                address = wordsAction.getAddressForSentence(key);
                System.out.println(address);
            }
           else {
                //是单词
                address = wordsAction.getAddressForWords(key);                                     //获得单词翻译的网络地址
            }
            final String finalAddress = address;
            HttpUtil.sentHttpRequest(finalAddress, new HttpCallBackListener() {                    //网络访问
                @Override
                public void onFinish(InputStream inputStream) {                                    //访问成功回调onFinish方法
                    WordsHandler wordsHandler = new WordsHandler();

                    if (!key.contains(" ")) {
                        //是单词
                        ParseXML.parse(wordsHandler, inputStream);                                  //解析从网络访问获得的XML格式的翻译
                        words = wordsHandler.getWords();                                           //获取解析后的words
                        wordsAction.saveWords(words);                                              //保存words到数据库
                        wordsAction.saveWordsMP3(words);                                           //保存words发音地址到数据库
                        handler.sendEmptyMessage(111);                                             //传递信息给主线程，以完成UI更新
                    } else {
                        //是句子
                        Message message = new Message();
                        message.what =112;
                        message.obj = ParseJSON.getJsonResult(inputStream);
                        handler.sendMessage(message);
                    }
                }
                @Override
                public void onError() {

                }
            });
        } else {
            upDateView();                                                                           //数据库中有words数据则UI更新显示翻译
        }
    }

   //UI更新显示
    public void upDateView() {
        if (words.getIsChinese()) {                                                                //若输入的是中文，则显示中文翻译，并且发音界面设置为不可见
            searchWords_posAcceptation.setText(words.getFy());
            searchWords_posA_layout.setVisibility(View.GONE);
            searchWords_posE_layout.setVisibility(View.GONE);
        } else {
            searchWords_posAcceptation.setText(words.getPosAcceptation());                      //若是英文，则显示PosAcceptation翻译
            if(words.getPsE()!="") {                                                               //若有发音
                searchWords_psE.setText(String.format(getResources().getString(R.string.psE), words.getPsE()));             //getResources()可以获取存在系统的资源,美[%1$s]括号里放words.getPsE()
                searchWords_posE_layout.setVisibility(View.VISIBLE);                            //发音界面设为可见
            }else {
                searchWords_posE_layout.setVisibility(View.GONE);
            }
            if(words.getPsA()!="") {
                searchWords_psA.setText(String.format(getResources().getString(R.string.psA), words.getPsA()));
                searchWords_posA_layout.setVisibility(View.VISIBLE);
            }else {
                searchWords_posA_layout.setVisibility(View.GONE);                               //没有发音则发音布局设为不可见
            }
        }
        searchWords_key.setText(words.getKey());                                                 //显示单词翻译
        searchWords_sent.setText(words.getSent());                                               //例句翻译
        searchWords_linerLayout.setVisibility(View.VISIBLE);                                    //翻译布局可见
    }

    public void upDateView02(){
        searchWords_key.setText(" ");

        searchWords_posA_layout.setVisibility(View.GONE);
        searchWords_posE_layout.setVisibility(View.GONE);
        searchWords_linerLayout.setVisibility(View.VISIBLE);
    }
    public void onBackPressed() {
        super.onBackPressed();
    }
}