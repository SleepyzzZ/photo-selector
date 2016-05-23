package com.sleepyzzz.photo_selector.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.sleepyzzz.photo_selector.R;
import com.sleepyzzz.photo_selector.event.OnItemClickListerner;
import com.sleepyzzz.photo_selector.util.ViewHolder;
import com.sleepyzzz.photo_selector.view.SquaredImageView;

import java.util.LinkedList;
import java.util.List;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * Date: 2016-04-11
 * Time: 13:22
 * FIXME
 */
public class PhotoPickerAdapter extends CommonAdapter<String> {


    private static final String TAG = PhotoPickerAdapter.class.getSimpleName();
    /**
     * 用户选择的图片,存储为图片的完整路径
     */
    public static List<String> mSelectedImage = new LinkedList<String>();
    /**
     * 文件夹路径
     */
    private String mDirPath;
    /**
     * 选择图片数量限制
     */
    private int mSelectCount;
    /**
     * 回调接口方法
     */
    private OnItemClickListerner mOnItemClickListerner = null;

    public PhotoPickerAdapter(Context context, List<String> mDatas, int itemLayoutId,
                              String dirPath, int selectCount) {
        super(context, mDatas, itemLayoutId);
        this.mDirPath = dirPath;
        this.mSelectCount = selectCount;
    }

    @Override
    public void convert(final ViewHolder helper, final String item) {

        //设置图片
        helper.setImageResource(R.id.siv_image, R.drawable.default_error);
        helper.setImageResource(R.id.iv_checkmark, R.drawable.btn_unselected);
        helper.setImageByUrl(R.id.siv_image, mDirPath + "/" + item);

        final SquaredImageView mImageView = helper.getView(R.id.siv_image);
        final ImageView mCheckMark = helper.getView(R.id.iv_checkmark);
        final View mask = helper.getView(R.id.mask);

        mask.setVisibility(View.GONE);
        //设置Mark的点击事件
        mCheckMark.setOnClickListener(new View.OnClickListener() {
            //选择，则将图片变暗，反之则反之
            @Override
            public void onClick(View v) {
                //已经选择过该图片
                if (mSelectedImage.contains(mDirPath + "/" + item)) {

                    mSelectedImage.remove(mDirPath + "/" + item);
                    mOnItemClickListerner.onMarkClick(mDirPath + "/" +item);    //回调
                    mCheckMark.setImageResource(R.drawable.btn_unselected);
                    mask.setVisibility(View.GONE);
                } else {
                    //未选择该图片
                    if (mSelectedImage.size() == mSelectCount) {
                        Toast.makeText(mContext, R.string.msg_amount_limit,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mSelectedImage.add(mDirPath + "/" + item);
                    mOnItemClickListerner.onMarkClick(mDirPath + "/" +item);    //回调
                    mCheckMark.setImageResource(R.drawable.btn_selected);
                    mask.setVisibility(View.VISIBLE);
                }
            }
        });

        //已经选择过的图片，显示出选择效果
        if (mSelectedImage.contains(mDirPath + "/" + item)) {

            mCheckMark.setImageResource(R.drawable.btn_selected);
            mask.setVisibility(View.VISIBLE);
        }

        //设置photo的点击事件
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mOnItemClickListerner.onPhotoClick(v, helper.getPosition());
            }
        });
    }

    /**
     * 回调方法
     * @param onItemClickListerner
     */
    public void setOnItemClickListerner(OnItemClickListerner onItemClickListerner) {

        this.mOnItemClickListerner = onItemClickListerner;
    }

    public List<String> getmSelectedImage() {

        return mSelectedImage;
    }

    @Override
    public void setSelectIndex(int i) {

    }

    @Override
    public int getSetlectIndex() {

        return 0;
    }
}
