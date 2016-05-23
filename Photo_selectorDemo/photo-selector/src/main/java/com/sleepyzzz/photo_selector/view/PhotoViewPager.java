package com.sleepyzzz.photo_selector.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * Date: 2016-04-13
 * Time: 15:23
 * FIXME
 */
public class PhotoViewPager extends ViewPager {

    private float mTrans;
    private float mScale;
    /**
     * 最大的缩小比例
     */
    private static final float SCALE_MAX = 0.5f;
    /**
     * 保存position与对应的View
     */
    private HashMap<Integer, View> mChildrenViews = new LinkedHashMap<Integer, View>();
    /**
     * 滑动时左边的元素
     */
    private View mLeft;
    /**
     * 滑动时右边的元素
     */
    private View mRight;

    public PhotoViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //滑动特别小的距离时，认为没有动
        float effectOffset = isSmall(positionOffset) ? 0 : positionOffset;
        //获取左边的View
        mLeft = findViewFromObject(position);
        //获取右边的View
        mRight = findViewFromObject(position + 1);
        //添加切换动画效果
        animateStack(mLeft, mRight, effectOffset, positionOffsetPixels);
        super.onPageScrolled(position, positionOffset, positionOffsetPixels);
    }

    private void animateStack(View left, View right, float effectOffset,
                              int positioOffsetPixels) {

        if(right != null) {
            /**
             * 缩小比例 如果手指从右到左滑动(切换到后一个):0.0~1.0,即从一半到最大
             * 如果手指从左到右滑动(切换到前一个):1.0~0,即从最大到一半
             */
            mScale = (1 - SCALE_MAX) * effectOffset + SCALE_MAX;
            /**
             * x偏移量:如果手指从右到左滑动(切换到后一个):0-720如果手指从左到右的滑动(切换到前一个):720-0
             */
            mTrans = -getWidth() - getPageMargin() + positioOffsetPixels;
            right.setScaleX(mScale);
            right.setScaleY(mScale);
            right.setTranslationX(mTrans);
        }
        if(left != null) {
            left.bringToFront();
        }
    }


    private View findViewFromObject(int position) {

        return mChildrenViews.get(position);
    }

    private boolean isSmall(float positionOffset) {

        return Math.abs(positionOffset) < 0.0001;
    }

    /*@Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }*/

}
