package cn.eoe.app.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dictionary13.R;
import cn.eoe.app.adapter.NoteAdapter;
import cn.eoe.app.db.biz.NoteWordsAction;
import cn.eoe.app.db.biz.WordsAction;
import cn.eoe.app.model.Words;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import static cn.eoe.app.ui.HistoryRecordsActivity.wordsAction;
import static cn.eoe.app.db.biz.NoteWordsAction.db;

/**
 * 单词本活动类
 * 1.显示收藏的单词
 * 2.可以按字母排序和默认排序
 * 3.多选删除功能
 * 4.离线状态下可获得发音
 */

public class NoteActivity extends Activity implements View.OnClickListener {

    private static  ArrayList<Words> arrayList, arrayList2, arrayList3;
    public static ListView nListView;                                                             //用于单词本的ListView

    private NoteWordsAction noteWordsAction;
    private ImageButton button_initial;                                                          //排序按钮
    private boolean b = false;                                                                   //用于判断点击排序次数的状态量

    public static Button bt_cancel,bt_delete;                                                     //多选删除的取消键和删除键

    public static TextView tv_sum;                                                                //显示有多少个单词被选中
    public static LinearLayout linearLayout;                                                      //多选的操作布局，包括删除取消选择按钮和显示个数文本
    public static NoteAdapter noteAdapter;
    public static final int NOSELECT_STATE = -1;                                                //定义一个常量
    public static boolean isMultiSelect = false;                                                 //是否为多选状态，初始化为否
    public static List<Words> listDelete = new ArrayList<>();                                     //一个容器用于存放需要删除的数据
    TranslateActivity ma = new TranslateActivity();


   //返回上一个活动时同时设置CheckBox为不可见
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {                                       //setVisibility()设置CheckBox 的启动状态
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            cancelClick();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        wordsAction = WordsAction.getInstance(this);                                               //获取WordsAction对象
        button_initial = (ImageButton) findViewById(R.id.bt_initial);

        bt_cancel = (Button) findViewById(R.id.bt_cancel);
        bt_delete = (Button) findViewById(R.id.bt_delete);
        tv_sum = (TextView) findViewById(R.id.tv_sum);
        linearLayout = (LinearLayout) findViewById(R.id.delete_linearLayout);
        bt_cancel.setOnClickListener(this);
        bt_delete.setOnClickListener(this);

        bt_cancel.setFocusable(false);                                                             //解决按钮与item点击冲突事件，不自动获取焦点
        bt_delete.setFocusable(false);
        nListView = (ListView) findViewById(R.id.listview);

        button_initial.setOnClickListener(this);
        noteWordsAction = new NoteWordsAction(this);
        setNoteAdapter();                                                                           //初始化加载页面用默认排序

    }

    //初始化页面，在数据库中获取数据并且显示出来
    public void setNoteAdapter() {
        arrayList = new ArrayList<Words>();
        arrayList2 = new ArrayList<Words>();
        arrayList = noteWordsAction.getWordsFromSQLiteToList();                                   //获取数据存放在arrayList中
        for (int i = arrayList.size(); i > 0; i--) {
            arrayList2.add(arrayList.get(i - 1));                                                   //arrayList2增加arrayList索引降序的数据
        }
        noteAdapter = new NoteAdapter(NoteActivity.this, arrayList2,NOSELECT_STATE);             //创建一个NoteAdapter适配器类的对象，并传入参数进行初始化，未点击item，默认为-1
        nListView.setAdapter(noteAdapter);                                                          //利用setAdapter建立ListView与数据之间的关联
        noteAdapter.notifyDataSetChanged();
    }

    //按钮点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
          case R.id.bt_initial:
                b = !b;                                                                             //第一次点击时b=true，第二次点击时b=false，如此循环下去
               //按单词首字母排序
                if (b) {
                    compareToWord();                                                                //按单词首字母排序
                    noteAdapter = new NoteAdapter(NoteActivity.this, arrayList3,NOSELECT_STATE); //更新界面
                    nListView.setAdapter(noteAdapter);
                    Toast.makeText(NoteActivity.this, "按首字母排序", Toast.LENGTH_SHORT).show();
                //默认排序
                } else {
                    setNoteAdapter();
                    Toast.makeText(NoteActivity.this, "默认排序", Toast.LENGTH_SHORT).show();
                }
                break;
           case R.id.bt_cancel:
               //取消选择
                cancelClick();
                break;
            case R.id.bt_delete:
                dialogBox();              //弹出对话框，确认删除
                break;
        }
    }

    //首字母排序方法
    public void compareToWord() {
        arrayList3 = new ArrayList<Words>();
        Collections.sort(arrayList2, new Comparator<Words>() {
            @Override
            public int compare(Words words1, Words words2) {                                       //在Words类中重写这个方法，可以比较两个单词首字母的大小
                return words1.getKey().compareToIgnoreCase(words2.getKey());
            }
        });
        for (int i = 0; i < arrayList2.size(); i++)
            arrayList3.add(arrayList2.get(i));
    }


    //取消选择方法
    public void cancelClick(){
        isMultiSelect = false;                                                                     // 退出多选模式
        listDelete.clear();                                                                         // 清除listDelete中的数据
        noteAdapter = new NoteAdapter(NoteActivity.this, arrayList2,NOSELECT_STATE);             //更新界面
        nListView.setAdapter(noteAdapter);
        linearLayout.setVisibility(View.GONE);                                                     //隐藏linearLayout
    }

    //删除方法
    public void deleteClick(){
        isMultiSelect = false;                                                                      //多选状态设置为否
        for (int i = 0; i < arrayList2.size(); i++) {
            for (int j = 0; j < listDelete.size(); j++) {
                if (arrayList2.get(i).getKey().equals( listDelete.get(j).getKey())) {               //arrayList2中的元素与listDelete中的元素一个一个比较
                    db.delete("Note","key=?",new String[]{arrayList2.get(i).getKey()});           //数据库删除与listDelete中元素相同的数据
                    arrayList2.remove(arrayList2.get(i));                                           //arrayList2也移除这个数据
                }
            }
        }

        listDelete.clear();                                                                         //清除 listDelete
        noteAdapter = new NoteAdapter(NoteActivity.this, arrayList2,NOSELECT_STATE);             //更新界面
        nListView.setAdapter(noteAdapter);
        linearLayout.setVisibility(View.GONE);                                                     //隐藏linearLayout
    }

    //对话框提示事件
    private void dialogBox() {

        AlertDialog.Builder bb = new AlertDialog.Builder(this);
        bb.setMessage("确认删除所选单词吗？");
        bb.setTitle("提示");
        bb.setCancelable(true);
        bb.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteClick();
            }
        });
        bb.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        bb.show();
    }

}







