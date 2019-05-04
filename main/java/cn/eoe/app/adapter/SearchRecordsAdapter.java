package cn.eoe.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.dictionary13.R;
import cn.eoe.app.db.biz.WordsAction;

import java.util.List;


import static cn.eoe.app.ui.HistoryRecordsActivity.listView;
import static cn.eoe.app.ui.HistoryRecordsActivity.searchRecordsAdapter;

/**
 *
 * 搜索记录的listView的适配器
 * Created by 徐启 on 2019/4/20.
 */

    public class SearchRecordsAdapter extends BaseAdapter {
    private Context context;
    private List<String> searchRecordsList;
    private LayoutInflater inflater;

    public SearchRecordsAdapter(Context context, List<String> searchRecordsList) {
        this.context = context;
        this.searchRecordsList = searchRecordsList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return searchRecordsList.size() == 0 ? 0 : searchRecordsList.size();
    }

    @Override
    public Object getItem(int position) {
        return searchRecordsList.size() == 0 ? null : searchRecordsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //普通式适配器
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.records_item, null);                         //convertView为空时，重新加载item
        }
            TextView recordTv = (TextView) convertView.findViewById(R.id.records_words);          //加载布局
            ImageButton recordIb = (ImageButton) convertView.findViewById(R.id.item_close);
            recordIb.setFocusable(false);                                                          //不自动获取焦点

           //item中删除键点击事件
            recordIb.setOnClickListener(new View.OnClickListener() {
                String key = searchRecordsList.get(position);                                    //key为当前点击的单词

                @Override
                public void onClick(View v) {
                    WordsAction.db.delete("Words", "key=?", new String[]{key});                  //数据库中删除
                    searchRecordsList.remove(position);                                          //arrayList2中删除位置为position的值
                    searchRecordsAdapter = new SearchRecordsAdapter(v.getContext(), searchRecordsList);  //创建一个WordAdapter适配器类的对象，并传入参数进行初始化
                    listView.setAdapter(searchRecordsAdapter);
                    notifyDataSetChanged();

                }
            });
            String content = searchRecordsList.get(position);
            recordTv.setText(content);
            return convertView;
        }

    }


