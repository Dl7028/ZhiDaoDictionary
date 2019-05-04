package cn.eoe.app.adapter;

import android.content.Context;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.widget.CheckBox;


import android.widget.ImageButton;
import android.widget.TextView;

import cn.eoe.app.ui.TranslateActivity;
import com.example.dictionary13.R;

import cn.eoe.app.model.Words;

import java.util.HashMap;
import java.util.List;


import static cn.eoe.app.ui.HistoryRecordsActivity.wordsAction;

import static cn.eoe.app.ui.NoteActivity.isMultiSelect;
import static cn.eoe.app.ui.NoteActivity.linearLayout;
import static cn.eoe.app.ui.NoteActivity.listDelete;
import static cn.eoe.app.ui.NoteActivity.nListView;
//import static cn.eoe.app.ui.NoteActivity.noteAdapter;
import static cn.eoe.app.ui.NoteActivity.tv_sum;

/**生词本listView的自定义适配器
 * 生词本内容的显示
 * 生词本单词发音的播放
 * 复选框CheckBox的操作
 *
 * Created by 徐启 on 2019/4/22.
 */

public class NoteAdapter extends BaseAdapter {
    private List<Words> mList;
    private LayoutInflater inflater;
    private HashMap<Integer, Integer> isCheckBoxVisible;                                         // 用来记录是否显示checkBox
 	private HashMap<Integer, Boolean> isChecked;                                                  // 用来记录是否被选中
    private HashMap<Integer,Boolean> storageArea =new HashMap<>();                               // 存放已被选中的CheckBox

  public NoteAdapter(Context context, List<Words> list, int position) {                            //NoteAdapter构造器
    inflater = LayoutInflater.from(context);
    mList = list;
    isCheckBoxVisible = new HashMap<Integer, Integer>();                                          // 用来记录是否显示checkBox
    isChecked = new HashMap<Integer, Boolean>();                                                   // 用来记录是否被选中

      // 如果处于多选状态，则显示CheckBox，否则不显示
     	if (isMultiSelect) {
            for (int i = 0; i < mList.size(); i++) {
                isCheckBoxVisible.put(i, CheckBox.VISIBLE);                                      //若是多选状态，CheckBox均为可见
                isChecked.put(i, false);                                                          //初始化全未勾选
            }
        } else {
            for (int i = 0; i < mList.size(); i++) {
                isCheckBoxVisible.put(i, CheckBox.INVISIBLE);                                    //若不是多选状态，CheckBox均为隐藏
                isChecked.put(i, false);                                                           //初始化全未勾选
            }
        }
}
    @Override
    public int getCount() {
     	return mList.size();
    }

    @Override
    public Object getItem(int position) {

        	return mList.get(position);
    }
    @Override
    public long getItemId(int position) {

        	return position;
    }

    //文艺式自定义适配器
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.note_item, null);
            viewHolder.textView_words = (TextView) convertView.findViewById(R.id.text_word);      //加载布局
            viewHolder.textView_explains = (TextView)convertView.findViewById(R.id.text_explains);
            viewHolder.cb = (CheckBox) convertView.findViewById(R.id.cb_select);
            viewHolder.iButton_sound = (ImageButton) convertView.findViewById(R.id.item_sound);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.cb.setTag(position);
        final Words words = mList.get(position);                                                   //获得当前点击的Words对象
        viewHolder.textView_words.setText(words.getKey());                                        //显示生词本内容
        viewHolder.textView_explains.setText(words.getPosAcceptation());

        //实现长按隐藏发音键
        if(isMultiSelect){
            viewHolder.iButton_sound.setVisibility(View.INVISIBLE);                              //长按时发音按钮设置为不可见
        }else {
           viewHolder.iButton_sound.setVisibility(View.VISIBLE);                                 //长按时发音按钮设置为不可见

        }
        viewHolder.iButton_sound.setFocusable(false);
        //发音按钮点击事件
        viewHolder.iButton_sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordsAction.playMP3(words.getKey(), "E", v.getContext());                           //v.getContext(),获取上下文
            }
        });


        // 根据position设置CheckBox是否可见，是否选中
        viewHolder.cb.setChecked(isChecked.get(position));                                         //setChecked更改CheckBox的选中状态
        viewHolder.cb.setVisibility(isCheckBoxVisible.get(position));                             //setVisibility()设置CheckBox 的启动状态

        convertView.setOnLongClickListener(new onMyLongClick(position, mList));                     // nListView每一个Item的长按事件
        /*
         * 在nListView中点击每一项的处理
         * 如果CheckBox未选中，则点击后选中CheckBox，并将数据添加到list_delete中
         * 如果CheckBox选中，则点击后取消选中CheckBox，并将数据从list_delete中移除
         */
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             	if (isMultiSelect) {
                    // 处于多选模式
                    if (viewHolder.cb.isChecked()) {                                                //若CheckBox已经是被选中的状态
                        //item点击事件
                        viewHolder.cb.setChecked(false);                                           //点击时变成未选中的状态
                        listDelete.remove(words);                                                   //在要删除的数组的移除点击的单词
                        storageArea.remove(position);                                              //若未选中，则不存放

                    } else {
                       viewHolder.cb.setChecked(true);
                        listDelete.add(words);
                        storageArea.put(position,true);                                           //存放这个复选框的状态,若被选中，则存放

                    }
                    tv_sum.setText("共选择了" + listDelete.size() + "项");
                } else {                                                                            //单击listView跳入对应单词的翻译
                    TranslateActivity.kWord = mList.get(position).getKey();
                    Intent intent2=new Intent(v.getContext(),TranslateActivity.class);             //构建Intent，TranslateActivity.this为上下文,NoteActivity.class为活动目标
                    v.getContext().startActivity(intent2);
                }
            }
        });

        //存储复选框的选中状态，防止勾选混乱
        if(storageArea!=null&&storageArea.containsKey(position)){
            viewHolder.cb.setChecked(true);
        }else {
            viewHolder.cb.setChecked(false);
        }
        return convertView;
    }

  public  static class ViewHolder {
        public TextView textView_words;
        public TextView textView_explains;
        public CheckBox cb;
        public ImageButton  iButton_sound;
    }


    // 自定义长按事件
	class onMyLongClick implements View.OnLongClickListener {
        private int position;

        public onMyLongClick(int position, List<Words> list) {
        this.position = position;
          mList = list;
    }
		@Override
        public boolean onLongClick(View v) {
            isMultiSelect = true;
            listDelete.clear();				                                                        // 添加长按Item到删除数据list中
            linearLayout.setVisibility(View.VISIBLE);

            tv_sum.setText("共选择了" + listDelete.size() + "项");
            for (int i = 0; i < mList.size(); i++) {
                isCheckBoxVisible.put(i, CheckBox.VISIBLE);
            }
            // 根据position，设置nListView中对应的CheckBox为选中状态
           NoteAdapter noteAdapter = new NoteAdapter(v.getContext(), mList,position);
            nListView.setAdapter(noteAdapter);
            noteAdapter.notifyDataSetChanged();
            return true;                                                                           //返回true，不会触发单击事件
        }
    }


    }




