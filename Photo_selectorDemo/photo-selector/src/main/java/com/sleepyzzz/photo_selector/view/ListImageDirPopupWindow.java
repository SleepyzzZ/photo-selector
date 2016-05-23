package com.sleepyzzz.photo_selector.view;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sleepyzzz.photo_selector.R;
import com.sleepyzzz.photo_selector.adapter.CommonAdapter;
import com.sleepyzzz.photo_selector.entity.ImageFolder;
import com.sleepyzzz.photo_selector.event.OnPhotoDirSelected;
import com.sleepyzzz.photo_selector.util.ViewHolder;

import java.util.List;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * Date: 2016-04-11
 * Time: 10:46
 * FIXME
 */
public class ListImageDirPopupWindow extends BasePopupWindowForListView<ImageFolder> {

    private ListView mListDir;

    private int lastSelectIndex = 0;

    private CommonAdapter<ImageFolder> folderAdapter;

    public ListImageDirPopupWindow(int width, int height,
                                   List<ImageFolder> datas, View contentView) {
        super(contentView, width, height, true, datas);
    }

    //回调接口
    private OnPhotoDirSelected mOnPhotoDirSelected = null;

    @Override
    protected void initViews() {

        folderAdapter = new CommonAdapter<ImageFolder>(mContext, mDatas,
                R.layout.list_folder_item) {
            @Override
            public void convert(ViewHolder helper, ImageFolder item) {
                helper.setText(R.id.tv_dir_name, item.getName());
                helper.setImageByUrl(R.id.iv_cover, item.getFirstImagePath());
                helper.setText(R.id.tv_count, item.getCount() + "张");

                if(lastSelectIndex == helper.getPosition())
                    helper.setImageVisibility(R.id.iv_indicator, View.VISIBLE);
                else
                    helper.setImageVisibility(R.id.iv_indicator, View.INVISIBLE);
            }

            @Override
            public void setSelectIndex(int i) {
                if(lastSelectIndex == i)
                    return;

                lastSelectIndex = i;
                notifyDataSetChanged();
            }

            @Override
            public int getSetlectIndex() {

                return lastSelectIndex;
            }
        };
        mListDir = (ListView) findViewById(R.id.lv_list_dir);
        mListDir.setAdapter(folderAdapter);
    }

    @Override
    protected void init() {

    }

    public void setOnPhotoDirSelected(OnPhotoDirSelected photoDirSelected) {
        this.mOnPhotoDirSelected = photoDirSelected;
    }

    @Override
    protected void initEvents() {
        mListDir.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mOnPhotoDirSelected != null) {
                    mOnPhotoDirSelected.onSelected(mDatas.get(position));
                    //更新文件夹列表标记
                    folderAdapter.setSelectIndex(position);
                }
            }
        });
    }

    @Override
    protected void beforeInitWeNeedSomeParams(Object... params) {

    }
}
