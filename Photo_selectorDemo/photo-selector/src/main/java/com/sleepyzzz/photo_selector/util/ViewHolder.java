package com.sleepyzzz.photo_selector.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sleepyzzz.photo_selector.R;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * Date: 2016-04-11
 * Time: 10:13
 * FIXME
 */
public class ViewHolder {

    private final SparseArray<View> mViews;
    private int mPosition;
    private View mConvertView;
    private Context mContext;

    private ViewHolder(Context context, ViewGroup parent, int layoutId,
                       int position)
    {
        this.mContext = context;
        this.mPosition = position;
        this.mViews = new SparseArray<View>();
        mConvertView = LayoutInflater.from(context).inflate(layoutId, parent,
                false);
        //set Tag
        mConvertView.setTag(this);
    }

    /**
     * 拿到一个ViewHolder对象
     * @param context
     * @param convertView
     * @param parent
     * @param layoutId
     * @param position
     * @return
     */
    public static ViewHolder get(Context context, View convertView,
                                 ViewGroup parent, int layoutId, int position)
    {
        ViewHolder holder = null;
        if(convertView == null) {
            holder = new ViewHolder(context, parent, layoutId, position);
        } else {
            holder = (ViewHolder) convertView.getTag();
            holder.mPosition = position;
        }
        return holder;
    }

    public View getConvertView() {
        return mConvertView;
    }

    /**
     * 通过空间ID获取对应的控件,如果没有则加入views
     * @param viewId
     * @param <T>
     * @return
     */
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if(view == null) {
            view = mConvertView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    /**
     * 为TextView设置字符串
     * @param viewId
     * @param text
     * @return
     */
    public ViewHolder setText(int viewId, String text) {
        TextView view = getView(viewId);
        view.setText(text);
        return this;
    }

    /**
     * 为ImageView设置图片
     *
     * @param viewId
     * @param drawableId
     * @return
     */
    public ViewHolder setImageResource(int viewId, int drawableId)
    {
        ImageView view = (ImageView) getView(viewId);
        view.setImageResource(drawableId);

        return this;
    }



    /**
     * 为ImageView设置图片
     *
     * @param viewId
     * @param bm
     * @return
     */
    public ViewHolder setImageBitmap(int viewId, Bitmap bm)
    {
        ImageView view = (ImageView) getView(viewId);
        view.setImageBitmap(bm);
        return this;
    }

    /**
     * 为ImageView设置图片
     *
     * @param viewId
     * @param url
     * @return
     */
    public ViewHolder setImageByUrl(int viewId, String url)
    {
        //自定义图片异步加载类
        //ImageLoader.getInstance(3, ImageLoader.Type.LIFO).loadImage(url, (ImageView) getView(viewId));
        Glide.with(mContext)
                .load(url)
                .centerCrop()
                .dontAnimate()
                .thumbnail(0.5f)
                .placeholder(R.drawable.ic_photo_black_48dp)
                .error(R.drawable.ic_broken_image_black_48dp)
                .into((ImageView)getView(viewId));
        return this;
    }

    /**
     * 为ImageView设置是否可见状态
     * @param viewId
     * @param state
     * @return
     */
    public ViewHolder setImageVisibility(int viewId, int state)
    {
        ImageView view = (ImageView) getView(viewId);
        view.setVisibility(state);
        return this;
    }

    public int getPosition()
    {
        return mPosition;
    }
}
