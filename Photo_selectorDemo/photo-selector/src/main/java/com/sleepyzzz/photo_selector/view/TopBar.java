package com.sleepyzzz.photo_selector.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sleepyzzz.photo_selector.R;
import com.sleepyzzz.photo_selector.util.DisplayUtil;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * Date: 2016-05-09
 * Time: 13:19
 * FIXME
 */
public class TopBar extends RelativeLayout {


    //包含TopBar上的元素
    private ImageView mLeftIV;
    private TextView mLeftTV;
    private Button mRightBtn;

    // 布局属性，用来控制组件元素在ViewGroup中的位置
    private LayoutParams mLeftParams1, mLeftParams2, mRightParams;

    //左ImageView的属性值
    private Drawable mLeftDrawable;

    //左TextView的属性值
    private String mLeftText;
    private int mLeftTextColor;
    private float mLeftTextSize;

    //右Button的属性值
    private Drawable mRightBackground;
    private String mRightText;
    private int mRightTextColor;
    private float mRightTextSize;

    //监听回调接口
    private OnTopBarClickListener mListener;

    public TopBar(Context context) {
        super(context);
    }

    //有自定义attrs必须构造此方法
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public TopBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        //设置TopBar的背景
        setBackgroundColor(0xff21282C);
        // 通过这个方法，将你在atts.xml中定义的declare-styleable
        // 的所有属性的值存储到TypedArray中
        TypedArray ta = context.obtainStyledAttributes(attrs,
                R.styleable.TopBar);
        // 从TypedArray中取出对应的值来为要设置的属性赋值
        mLeftDrawable = ta.getDrawable(
                R.styleable.TopBar_csleftImageViewDrawable);
        mLeftText = ta.getString(
                R.styleable.TopBar_csleftText);
        mLeftTextColor = ta.getColor(
                R.styleable.TopBar_csleftTextColor, 0);
        mLeftTextSize = ta.getDimension(
                R.styleable.TopBar_csleftTextSize, 10);

        mRightBackground = ta.getDrawable(
                R.styleable.TopBar_csrightBackground);
        mRightText = ta.getString(
                R.styleable.TopBar_csrightText);
        mRightTextColor = ta.getColor(
                R.styleable.TopBar_csrightTextColor, 0);
        mRightTextSize = ta.getDimension(
                R.styleable.TopBar_csrightTextSize, 10);
        // 获取完TypedArray的值后，一般要调用
        // recyle方法来避免重新创建的时候的错误
        ta.recycle();

        mLeftIV = new ImageView(context);
        mLeftTV = new TextView(context);
        mRightBtn = new Button(context);
        // 为创建的组件元素赋值
        // 值就来源于我们在引用的xml文件中给对应属性的赋值
        mLeftIV.setImageDrawable(mLeftDrawable);
        mLeftIV.setId(1);
        mLeftTV.setText(mLeftText);
        mLeftTV.setTextColor(mLeftTextColor);
        mLeftTV.setTextSize(DisplayUtil.px2sp(context, mLeftTextSize));

        mRightBtn.setBackground(mRightBackground);
        mRightBtn.setText(mRightText);
        mRightBtn.setTextColor(mRightTextColor);
        mRightBtn.setTextSize(DisplayUtil.px2sp(context, mRightTextSize));

        //为组件元素设置相应的布局元素
        mLeftParams1 = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        mLeftParams1.leftMargin = DisplayUtil.dip2px(context, 16);
        mLeftParams1.rightMargin = DisplayUtil.dip2px(context, 16);
        mLeftParams1.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
                TRUE);
        mLeftParams1.addRule(RelativeLayout.CENTER_VERTICAL,
                TRUE);
        // 添加到ViewGroup
        addView(mLeftIV, mLeftParams1);

        mLeftParams2 = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        mLeftParams2.addRule(RelativeLayout.RIGHT_OF, mLeftIV.getId());
        mLeftParams2.addRule(RelativeLayout.CENTER_VERTICAL,
                TRUE);
        addView(mLeftTV, mLeftParams2);

        mRightParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        mRightParams.rightMargin = DisplayUtil.dip2px(context, 16);
        mRightParams.topMargin = DisplayUtil.dip2px(context, 5);
        mRightParams.bottomMargin = DisplayUtil.dip2px(context, 5);
        mRightParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
                TRUE);
        mRightParams.addRule(RelativeLayout.CENTER_VERTICAL,
                TRUE);
        addView(mRightBtn, mRightParams);

        mLeftIV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.leftClick();
            }
        });

        mRightBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.rightClick();
            }
        });
    }

    public TopBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnTopBarListener(OnTopBarClickListener listener) {
        this.mListener = listener;
    }

    public interface OnTopBarClickListener
    {
        //点击左ImageView
        void leftClick();
        //点击右Button
        void rightClick();
    }
}
