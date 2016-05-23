package com.sleepyzzz.photo_selector.fragment;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.sleepyzzz.photo_selector.R;
import com.sleepyzzz.photo_selector.adapter.PhotoPagerAdapter;
import com.sleepyzzz.photo_selector.adapter.PhotoPickerAdapter;
import com.sleepyzzz.photo_selector.event.OnPagerCLickListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * Date: 2016-04-15
 * Time: 20:47
 * FIXME
 */
public class PhotoPagerFragment extends Fragment {

    public final static String ARG_PATH = "PATHS";
    public final static String ARG_POSITION = "CURRENT_POSITION";
    public final static String ARG_MAXCOUNT = "MAX_SELECT_COUNT";

    private ArrayList<String> mPaths;
    private ViewPager mViewPager;
    private PhotoPagerAdapter mPagerAdapter;

    private RelativeLayout mBottombar;
    private ImageView mSelectMark;
    private Button mSelectBtn;
    private Button mCommitBtn;
    private TextView mToptv;

    private boolean isShowBottomBar = true;

    public final static long ANIM_DURATION = 200L;

    public final static String ARG_THUMBNAIL_TOP = "THUMBNAIL_TOP";
    public final static String ARG_THUMBNAIL_LEFT = "THUMBNAIL_LEFT";
    public final static String ARG_THUMBNAIL_WIDTH = "THUMBNAIL_WIDTH";
    public final static String ARG_THUMBNAIL_HEIGHT = "THUMBNAIL_HEIGHT";
    public final static String ARG_HAS_ANIM = "HAS_ANIM";

    private int thumbnailTop = 0;
    private int thumbnailLeft = 0;
    private int thumbnailWidth = 0;
    private int thumbnailHeight = 0;

    private boolean hasAnim = false;

    private final ColorMatrix colorizerMatrix = new ColorMatrix();

    private int currentPosition = 0;

    private int mDesireSelectCount;

    public static PhotoPagerFragment newInstance(List<String> paths, int position, int maxSelectCount) {

        PhotoPagerFragment fragment = new PhotoPagerFragment();

        Bundle args = new Bundle();
        args.putStringArray(ARG_PATH, paths.toArray(new String[paths.size()]));
        args.putInt(ARG_POSITION, position);
        args.putBoolean(ARG_HAS_ANIM, false);
        args.putInt(ARG_MAXCOUNT, maxSelectCount);
        fragment.setArguments(args);

        return fragment;
    }

    public static PhotoPagerFragment newInstance(List<String> paths, int position, int maxSelectCount, int[] screenLocation, int thumbnailWidth, int thumbnailHeight) {

        PhotoPagerFragment fragment = newInstance(paths, position, maxSelectCount);

        fragment.getArguments().putInt(ARG_THUMBNAIL_LEFT, screenLocation[0]);
        fragment.getArguments().putInt(ARG_THUMBNAIL_TOP, screenLocation[1]);
        fragment.getArguments().putInt(ARG_THUMBNAIL_WIDTH, thumbnailWidth);
        fragment.getArguments().putInt(ARG_THUMBNAIL_HEIGHT, thumbnailHeight);
        fragment.getArguments().putBoolean(ARG_HAS_ANIM, true);

        return fragment;
    }

    public void setPhotos(List<String> paths, int position) {

        this.mPaths.clear();
        this.mPaths.addAll(paths);
        this.currentPosition = position;

        mViewPager.setCurrentItem(position);
        mViewPager.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPaths = new ArrayList<>();

        Bundle bundle = getArguments();
        if (bundle != null) {
            String[] pathArr = bundle.getStringArray(ARG_PATH);
            mPaths.clear();
            if (pathArr != null) {
                mPaths = new ArrayList<>(Arrays.asList(pathArr));
            }

            hasAnim = bundle.getBoolean(ARG_HAS_ANIM);
            currentPosition = bundle.getInt(ARG_POSITION);
            mDesireSelectCount = bundle.getInt(ARG_MAXCOUNT);
            thumbnailTop = bundle.getInt(ARG_THUMBNAIL_TOP);
            thumbnailLeft = bundle.getInt(ARG_THUMBNAIL_LEFT);
            thumbnailWidth = bundle.getInt(ARG_THUMBNAIL_WIDTH);
            thumbnailHeight = bundle.getInt(ARG_THUMBNAIL_HEIGHT);
        }

        mPagerAdapter = new PhotoPagerAdapter(getActivity(), mPaths);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_photo_pager, container, false);

        mViewPager = (ViewPager) rootView.findViewById(R.id.vp_photos);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(currentPosition);
        mViewPager.setOffscreenPageLimit(5);

        mBottombar = (RelativeLayout) rootView.findViewById(R.id.ly_bottom_bar);
        mSelectMark = (ImageView) rootView.findViewById(R.id.iv_select_box);
        mSelectBtn = (Button) rootView.findViewById(R.id.btn_selectbox);
        mCommitBtn = (Button) getActivity().findViewById(R.id.btn_commit);
        mToptv = (TextView) getActivity().findViewById(R.id.tv_back);

        //更新第一张预览图片的selectbox
        if (PhotoPickerAdapter.mSelectedImage.contains(
                mPaths.get(currentPosition)))
        {
            mSelectMark.setImageResource(R.drawable.selectbox_marked);
        }
        //进入pager显示Top TextView(eg:2/19)
        mToptv.setText(String.format("%d/%d", currentPosition+1,
                mPaths.size()));

        if (savedInstanceState == null && hasAnim) {

            ViewTreeObserver observer = mViewPager.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {

                    mViewPager.getViewTreeObserver().removeOnPreDrawListener(this);

                    int[] screenLocation = new int[2];
                    mViewPager.getLocationOnScreen(screenLocation);
                    thumbnailLeft = thumbnailLeft - screenLocation[0];
                    thumbnailTop = thumbnailTop - screenLocation[1];

                    runEnterAnimation();

                    return true;
                }
            });
        }

        mPagerAdapter.setOnPagerCLickListener(new OnPagerCLickListener() {
            @Override
            public void onPagerClick() {

                if(isShowBottomBar)
                {
                    mBottombar.setVisibility(View.GONE);
                    isShowBottomBar = !isShowBottomBar;
                } else
                {
                    mBottombar.setVisibility(View.VISIBLE);
                    isShowBottomBar = !isShowBottomBar;
                }
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                hasAnim = currentPosition == position;
                if (PhotoPickerAdapter.mSelectedImage.
                        contains(mPaths.get(position)))
                {
                    mSelectMark.setImageResource(R.drawable.selectbox_marked);
                } else
                {
                    mSelectMark.setImageResource(R.drawable.selectbox_n);
                }
                mToptv.setText(String.format("%d/%d", position+1,
                        mPaths.size()));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mSelectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int pos = mViewPager.getCurrentItem();
                if (PhotoPickerAdapter.mSelectedImage.
                        contains(mPaths.get(pos)))
                {
                    mSelectMark.setImageResource(R.drawable.selectbox_n);
                    PhotoPickerAdapter.mSelectedImage.remove(mPaths.get(pos));
                    if (PhotoPickerAdapter.mSelectedImage.size() == 0)
                    {
                        mCommitBtn.setEnabled(false);
                        mCommitBtn.setText(R.string.action_done);
                    } else
                    {
                        mCommitBtn.setText(String.format("%s(%d/%d)", getString(R.string.action_done)
                                , PhotoPickerAdapter.mSelectedImage.size(), mDesireSelectCount));
                    }
                } else
                {
                    if (PhotoPickerAdapter.mSelectedImage.size() < mDesireSelectCount)
                    {
                        mSelectMark.setImageResource(R.drawable.selectbox_marked);
                        PhotoPickerAdapter.mSelectedImage.add(mPaths.get(pos));
                        mCommitBtn.setEnabled(true);
                        mCommitBtn.setText(String.format("%s(%d/%d)", getString(R.string.action_done)
                                , PhotoPickerAdapter.mSelectedImage.size(), mDesireSelectCount));
                    } else
                    {
                        Toast.makeText(getActivity().getApplicationContext(),
                                R.string.msg_amount_limit, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return rootView;
    }

    private void runEnterAnimation() {

        final long duration = ANIM_DURATION;

        ViewHelper.setPivotX(mViewPager, 0);
        ViewHelper.setPivotY(mViewPager, 0);
        ViewHelper.setScaleX(mViewPager, (float) thumbnailWidth / mViewPager.getWidth());
        ViewHelper.setScaleY(mViewPager, (float) thumbnailHeight / mViewPager.getHeight());
        ViewHelper.setTranslationX(mViewPager, thumbnailLeft);
        ViewHelper.setTranslationY(mViewPager, thumbnailTop);

        ViewPropertyAnimator.animate(mViewPager)
                .setDuration(duration)
                .scaleX(1)
                .scaleY(1)
                .translationX(0)
                .translationY(0)
                .setInterpolator(new DecelerateInterpolator());

        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mViewPager.getBackground(),
                "alpha", 0, 255);
        bgAnim.setDuration(duration);
        bgAnim.start();

        ObjectAnimator colorizer = ObjectAnimator.ofFloat(PhotoPagerFragment.this,
                "saturation", 0, 1);
        colorizer.setDuration(duration);
        colorizer.start();
    }

    public void runExitAnimation(final Runnable endAction) {

        if (!getArguments().getBoolean(ARG_HAS_ANIM, false) || !hasAnim) {
            endAction.run();
            return;
        }

        final long duration = ANIM_DURATION;
        ViewPropertyAnimator.animate(mViewPager)
                .setDuration(duration)
                .setInterpolator(new AccelerateInterpolator())
                .scaleX((float) thumbnailWidth / mViewPager.getWidth())
                .scaleY((float) thumbnailHeight / mViewPager.getHeight())
                .translationX(thumbnailLeft)
                .translationY(thumbnailTop)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        endAction.run();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

        ObjectAnimator bgAnim =
                ObjectAnimator.ofInt(mViewPager.getBackground(), "alpha", 0);
        bgAnim.setDuration(duration);
        bgAnim.start();

        ObjectAnimator colorizer =
                ObjectAnimator.ofFloat(PhotoPagerFragment.this, "saturation", 1, 0);
        colorizer.setDuration(duration);
        colorizer.start();
    }

    public void setSaturation(float value) {

        colorizerMatrix.setSaturation(value);
        ColorMatrixColorFilter colorizerFilter = new ColorMatrixColorFilter(colorizerMatrix);
        mViewPager.getBackground().setColorFilter(colorizerFilter);
    }

    public ViewPager getViewPager() {

        return mViewPager;
    }

    public ArrayList<String> getPaths() {

        return mPaths;
    }

    public int getCurrentPosition() {

        return mViewPager.getCurrentItem();
    }
}
