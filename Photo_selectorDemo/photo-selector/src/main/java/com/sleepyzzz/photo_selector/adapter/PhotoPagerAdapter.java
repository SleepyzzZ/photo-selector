package com.sleepyzzz.photo_selector.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.sleepyzzz.photo_selector.R;
import com.sleepyzzz.photo_selector.event.OnPagerCLickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * Date: 2016-04-15
 * Time: 20:58
 * FIXME
 */
public class PhotoPagerAdapter extends PagerAdapter {

    private List<String> mPaths = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mInflater;

    private OnPagerCLickListener mOnPagerCLickListener;

    public void setOnPagerCLickListener(OnPagerCLickListener onPagerCLickListener) {
        mOnPagerCLickListener = onPagerCLickListener;
    }

    public PhotoPagerAdapter(Context context, List<String> paths) {
        mPaths = paths;
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        View itemView = mInflater.inflate(R.layout.item_pager, container, false);

        ImageView imageView = (ImageView) itemView.findViewById(R.id.iv_pager);

        //自定义图片异步加载类(大图片存在OOM)
        /*ImageLoader.getInstance(3, ImageLoader.Type.LIFO)
                .loadImage(mPaths.get(position), imageView);*/
        /*Glide.with(mContext)
                .load(mPaths.get(position))
                .thumbnail(0.1f)
                .dontAnimate()
                .dontTransform()
                .override(800, 800)
                .placeholder(R.drawable.ic_photo_black_48dp)
                .error(R.drawable.ic_broken_image_black_48dp)
                .into(imageView);*/
        Glide.with(mContext)
                .load(mPaths.get(position))
                .thumbnail(0.1f)        //先显示.1的缩略图
                .dontAnimate()
                .dontTransform()
                .override(800, 800)     //尺寸
                .placeholder(R.drawable.ic_photo_black_48dp)
                .error(R.drawable.ic_broken_image_black_48dp)
                .into(imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mOnPagerCLickListener.onPagerClick();
            }
        });
        /*imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mContext instanceof PhotoPickerActivity) {
                    if(!((Activity) mContext).isFinishing()) {
                        ((Activity) mContext).onBackPressed();
                    }
                }
            }
        });*/
        container.addView(itemView);

        return itemView;
    }

    @Override
    public int getCount() {
        return mPaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
