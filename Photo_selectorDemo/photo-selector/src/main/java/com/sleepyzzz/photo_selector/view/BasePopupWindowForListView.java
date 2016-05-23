package com.sleepyzzz.photo_selector.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;

import java.util.List;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * Date: 2016-04-11
 * Time: 09:56
 * FIXME
 */
public abstract class BasePopupWindowForListView<T> extends PopupWindow {

    /**
     * 布局文件的最外层view
     */
    protected View mContentView;
    protected Context mContext;
    /**
     * listview的数据集
     */
    protected List<T> mDatas;

    public BasePopupWindowForListView(View contentView, int width, int height,
                                      boolean focusable)
    {
        this(contentView, width, height, focusable, null);
    }

    public BasePopupWindowForListView(View contentView, int width, int height,
                                      boolean focusable, List<T> mDatas)
    {
        this(contentView, width, height, focusable, mDatas, new Object[0]);
    }

    public BasePopupWindowForListView(View contentView, int width, int height,
                                      boolean focusable, List<T> mDatas, Object... params)
    {
        super(contentView, width, height, focusable);
        this.mContentView = contentView;
        mContext = contentView.getContext();
        if(mDatas != null) {
            this.mDatas = mDatas;
        }

        if(params != null && params.length > 0) {
            beforeInitWeNeedSomeParams(params);
        }

        setBackgroundDrawable(new BitmapDrawable());
        setTouchable(true);
        setOutsideTouchable(true);
        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    dismiss();
                    return true;
                }
                return  false;
            }
        });
        initViews();
        initEvents();
        init();
    }

    protected abstract void init();

    protected abstract void initEvents();

    protected abstract void initViews();

    public View findViewById(int id) {
        return  mContentView.findViewById(id);
    }

    protected abstract void beforeInitWeNeedSomeParams(Object... params);

    protected static int doToPx(Context context, int dp) {
        return (int) (context.getResources().getDisplayMetrics().density * dp + 0.5f);
    }
}
