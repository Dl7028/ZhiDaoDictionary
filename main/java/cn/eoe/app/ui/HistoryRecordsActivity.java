package cn.eoe.app.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.dictionary13.R;
import cn.eoe.app.adapter.SearchRecordsAdapter;

import cn.eoe.app.https.HttpCallBackListener;
import cn.eoe.app.https.HttpUtil;
import cn.eoe.app.db.biz.WordsAction;
import cn.eoe.app.model.Words;

import java.io.InputStream;
import java.util.ArrayList;


import static cn.eoe.app.analytic.ParseSentenceJSON.getJsonResult;
import static cn.eoe.app.analytic.ParseSentenceJSON.pictureAddress;
import static cn.eoe.app.db.biz.WordsAction.db;
import static cn.eoe.app.ui.TranslateActivity.kWord;

/**
 * 搜索历史记录的活动
 * 1.显示搜索记录
 * 2.一键删除历史记录
 * 3.一个一个删除
 * 4.访问每日一句API，提前获得图片地址，解决先显示文字后显示图片的问题，实现同时显示
 *
 */


public class HistoryRecordsActivity extends FragmentActivity implements View.OnClickListener {
    private SearchView searchView;
    public static ArrayList<String> arrayList ,arrayList2 ;                                       //存放历史记录内容的数组
    public static SearchRecordsAdapter searchRecordsAdapter;                                      //自定义适配器对象
    public  static ListView listView;                                                             //显示历史记录内容的listView
    public static  WordsAction wordsAction;
    private Button button_empty_records;                                                        //清空历史记录按钮
    private  long exitTime;
    public static String pt2Address;//每日一句图片的地址
    public NotificationManager notificationManager;
    private ConnectivityManager mConnectivity;
//    private TelephonyManager mTelephony;
//
    private DrawerLayout mDrawer;
//    private View mLeft;
    private float mFirstX  = 0;                                    //手指按下时x的坐标
    private int mSensity = 30 ;                                    //菜单打开关闭的手势范围

    private boolean aBoolean = false;





    private ImageButton mNoteBookBtn;
    private ImageButton mSentenceBtn;
    private ImageButton mNotificationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        searchView = (SearchView) findViewById(R.id.Records_searchView);
        button_empty_records = (Button) findViewById(R.id.bt_empty_history);
        listView = (ListView) findViewById(R.id.history_list_view);
        mConnectivity= (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);      //监控网络连接
//        mTelephony = (TelephonyManager)this.getSystemService(TELEPHONY_SERVICE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer);

        mNoteBookBtn = (ImageButton)findViewById(R.id.note_book);
        mNotificationBtn=(ImageButton)findViewById(R.id.open_notification);
        mSentenceBtn = (ImageButton) findViewById(R.id.day_sentence);
        mSentenceBtn.setOnClickListener(this);
        mNotificationBtn.setOnClickListener(this);
        mNoteBookBtn.setOnClickListener(this);
        button_empty_records.setOnClickListener(this);


      //  mLeft = mDrawer.getChildAt(1);
       initEvent();


        searchView.setSubmitButtonEnabled(true);                                                  //设置显示搜索按钮
        searchView.setIconifiedByDefault(false);//设置不自动缩小为图标，点搜索框就出现软键盘
        searchView.setFocusable(false);                                                            //设置进入活动不自动显示软键盘
        loadSentence();                                                                             //为每日一句提前获取图片地址
        wordsAction = WordsAction.getInstance(this);                                               //获得WordsAction对象


        //搜索框查询事件
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                setRecordsAdapter();                                                                        //打开应用就显示搜索记录页面
                Words word = wordsAction.getWordsFromSQLite(query.trim());
                NetworkInfo info = mConnectivity.getActiveNetworkInfo();
                //判断网络有没有连接
                if (info == null || !mConnectivity.getBackgroundDataSetting()&&word.getKey()==null) {
                    Toast.makeText(HistoryRecordsActivity.this, "网络无连接！", Toast.LENGTH_SHORT).show();
                    return false;
                }else {
                    kWord = query.trim();                                                    //输入的值赋值给kWord ，让翻译活动查询kWord
                    Intent intent = new Intent(HistoryRecordsActivity.this, TranslateActivity.class);  //构建Intent，TranslateActivity.this为上下文,NoteActivity.class为活动目标
                    startActivity(intent);
                    return true;
                }
            }

            //SearchView输入文本改变事件
            @Override
            public boolean onQueryTextChange(String newText) {
                setRecordsAdapter();                                                                //当输入内容改变时，显示搜索历史记录
                return false;
            }
        });


        //点击搜索记录listView的item时会切换到点击的单词的翻译页面
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                kWord =arrayList2.get(position);                                  //把点击的单词赋值给kWord，让翻译活动查询这个单词
                Intent intent = new Intent(HistoryRecordsActivity.this, TranslateActivity.class);  //构建Intent，TranslateActivity.this为上下文,NoteActivity.class为活动目标
                startActivity(intent);
            }
        });
    }

    //数据库中获取数据，显示出来
    public void setRecordsAdapter() {
        arrayList = new ArrayList<String>();
        arrayList2 = new ArrayList<String>();
        arrayList = wordsAction.getWordsFromSQLiteToRecordsList();                                 //创建ArrayList对象
       for (int i = arrayList.size(); i > 0; i--) {                                               //让最近放入数据库中的单词显示在前面
            arrayList2.add(arrayList.get(i - 1));
        }
        searchRecordsAdapter = new SearchRecordsAdapter(HistoryRecordsActivity.this,  arrayList2); //创建一个WordAdapter适配器类的对象，并传入参数进行初始化
        listView.setAdapter(searchRecordsAdapter);                                                  //利用setAdapter建立ListView与数据之间的关联
    }



    //创建菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }


    //双击返回键退出程序
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * 解决每日一句活动中先显示文字再显示图片的问题
     * 在主活动中就获得图片的地址，就可以同时显示文字和图片
     */
    public void loadSentence() {
        String textAddress = "http://open.iciba.com/dsapi/";
        HttpUtil.sentHttpRequest(textAddress, new HttpCallBackListener() {
            @Override
            public void onFinish(InputStream inputStream) {
                String str = getJsonResult(inputStream);                                            //获得图片地址，这样启动每日一句时下载图片不用先等文字解析后获得图片地址，
                pt2Address = pictureAddress;                                                        // 而是直接使用这个地址
            }
            @Override
            public void onError() {

            }
        });
    }

    //对话框提示事件
    private void dialogBox() {


        AlertDialog.Builder bb = new AlertDialog.Builder(this);
        bb.setMessage("确认清空历史记录吗？");
        bb.setTitle("提示");
        bb.setCancelable(true);
        bb.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.delete("Words", null, null);                                                   //数据库中删除点击的单词
                setRecordsAdapter();                                                                //重新向数据库取值，获得arrayList2
                searchRecordsAdapter = new SearchRecordsAdapter(HistoryRecordsActivity.this, arrayList2);  //创建一个WordAdapter适配器类的对象，并传入参数进行初始化
                searchRecordsAdapter.notifyDataSetChanged();                                        //刷新数据视图
                listView.setAdapter(searchRecordsAdapter);                                          //显示被单词被删除后的页面
            }
        });
        bb.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        bb.show();
    }



    //通知栏显示
    public void sendNotification(){
        Intent intent = new Intent(HistoryRecordsActivity.this,HistoryRecordsActivity.class);

        PendingIntent pand = PendingIntent.getActivity(this,0,intent,0);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.search);//设置图标
     //   builder.setTicker("你有一条新消息");//手机状态栏通知
        builder.setWhen(System.currentTimeMillis());//获取系统时间
        builder.setContentTitle("知道词典");//设置标题
        builder.setContentText("快速查词");//通知消息内容
        builder.setContentIntent(pand);//点击之后的意图//
        //    builder.setDefaults(Notification.DEFAULT_LIGHTS);//设置指示灯//
        //    builder.setDefaults(Notification.DEFAULT_SOUND);//设置声音//
        //   builder.setDefaults(Notification.DEFAULT_VIBRATE);//设置震动
        builder.setDefaults(Notification.DEFAULT_ALL); //设置全部效果
       // Notification notification = builder.build();//安卓4.0以上手机使用
        Notification notification = builder.getNotification();
        notificationManager.notify(1,notification);
    }
    private void cancelNotification() {

        notificationManager.cancel(1);

    }

  //drawer操作
    private  void initEvent(){
        mDrawer.setDrawerListener(new DrawerLayout.DrawerListener() {

        //当抽屉滑动状态改变的时候被调用
         @Override
            public void onDrawerStateChanged(int arg0) {
                Log.i("drawer", "drawer的状态：" + arg0);
            }

            // 菜单滑动
            @Override
            public void onDrawerSlide(View arg0, float rate) {
                // rate从0.0 ~ 1.0  菜单的显示率
                // 可以设置菜单出现的效果  缩放、透明度变化  参考HorizontalScroll实现的菜单缩放
            }

            @Override
            public void onDrawerOpened(View arg0) {
                Log.i("drawer", "抽屉被完全打开了！");
            }

            @Override
            public void onDrawerClosed(View arg0) {
                Log.i("drawer", "抽屉被完全关闭了！");
            }
        });
    }

    /**
     * DrawerLayout只支持边缘滑动打开菜单
     * 通过判断手势滑动的距离来增加打开菜单的手势范围 在屏幕范围内滑动均可打开关闭菜单
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                mFirstX=ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                float curX = ev.getX();

               //向右滑动 预期效果：关闭右侧菜单或打开左侧菜单
                if(curX-mFirstX>mSensity) {                                                       //若是向右滑动
                        mDrawer.openDrawer(Gravity.START);               //向右滑动事件则是打开左侧菜单
                    }
                   else {                                          // 向左滑动
                            mDrawer.closeDrawer(Gravity.START);           //关闭左侧菜单
                    }
                    default:
                        break;
                }
                return super.dispatchTouchEvent(ev);
        }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.note_book:
                Intent intent=new Intent(HistoryRecordsActivity.this,NoteActivity.class);         //切换活动
                startActivity(intent);
                break;
            case R.id.day_sentence:
                Intent intent2=new Intent(HistoryRecordsActivity.this,SentenceActivity.class);    //切换活动
                startActivity(intent2);
                break;
            case R.id.open_notification:
                aBoolean = !aBoolean;
                if(aBoolean) {
                    sendNotification();
                    Toast.makeText(HistoryRecordsActivity.this, "已开启通知栏查词", Toast.LENGTH_SHORT).show();
                }else {
                    cancelNotification();
                    Toast.makeText(HistoryRecordsActivity.this, "已关闭通知栏查词", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.bt_empty_history:
                //弹出提示框
                dialogBox();
                break;
            default:
                break;
        }
    }
}

