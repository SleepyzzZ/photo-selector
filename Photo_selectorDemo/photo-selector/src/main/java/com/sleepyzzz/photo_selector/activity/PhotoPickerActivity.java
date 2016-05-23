package com.sleepyzzz.photo_selector.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.sleepyzzz.photo_selector.R;
import com.sleepyzzz.photo_selector.adapter.PhotoPickerAdapter;
import com.sleepyzzz.photo_selector.fragment.PhotoPagerFragment;
import com.sleepyzzz.photo_selector.fragment.PhotoPickerFragment;
import com.sleepyzzz.photo_selector.http.okhttp.OkHttpUtils;
import com.sleepyzzz.photo_selector.http.okhttp.callback.StringCallback;

import okhttp3.Call;

public class PhotoPickerActivity extends FragmentActivity {

    private final static String TAG = PhotoPickerActivity.class.getSimpleName();

    public static final String HTTP_URL = "http_url";
    /**
     * 默认图片选择数量
     */
    private int mDefaultCount;
    /**
     * 默认搜索路径
     */
    private String mDefaultSearchPath;
    /**
     * 服务器端地址
     */
    private String mServerUrl;
    /**
     * 控件
     */
    private Button mCommitBtn;
    private ImageView mBackBtn;

    private PhotoPickerFragment mPickerFragment;
    private PhotoPagerFragment mPagerFragment;
    /**
     * 图片上传进度框
     */
    private ProgressDialog mProgressDialog;

    /**
     *暴露接口
     * @param context
     * @param maxSelectCount-限制的最大图片选择数量
     * @param searchPath-指定图片搜索路径(若为null-表示搜索所有图片)
     * @param url-服务器地址
     */
    public static void actionStart(Context context, int maxSelectCount, String searchPath, String url)
    {
        Intent intent = new Intent(context, PhotoPickerActivity.class);
        intent.putExtra(PhotoPickerFragment.EXTRA_SELECT_COUNT, maxSelectCount);
        intent.putExtra(PhotoPickerFragment.EXTRA_DEFAULT_SELECTED_LIST, searchPath);
        intent.putExtra(HTTP_URL, url);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_picker);

        mDefaultCount = getIntent().getIntExtra(PhotoPickerFragment.EXTRA_SELECT_COUNT, 9);
        mDefaultSearchPath = getIntent().getStringExtra(PhotoPickerFragment.EXTRA_DEFAULT_SELECTED_LIST);
        mServerUrl = getIntent().getStringExtra(HTTP_URL);
        if(mPickerFragment == null)
        {
            DisplayMetrics outMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(outMetrics);

            mPickerFragment = PhotoPickerFragment.newInstance(outMetrics.heightPixels, mDefaultCount, mDefaultSearchPath);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mPickerFragment)
                    .commit();
        }

        initViews();
        initEvents();
    }

    private void initEvents() {
        mCommitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProgressDialog.show();
                OkHttpUtils.post()
                        .addFiles(PhotoPickerAdapter.mSelectedImage)
                        .url(mServerUrl)
                        .build()
                        .execute(new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e) {
                                mProgressDialog.cancel();
                                Toast.makeText(getApplicationContext(),
                                        e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onResponse(String response) {
                                mProgressDialog.cancel();
                                Toast.makeText(getApplicationContext(),
                                        "onResponse: "+ response, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void inProgress(float progress) {
                                super.inProgress(progress);
                                mProgressDialog.setProgress((int) (100 * progress));
                            }
                        });
            }
        });

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initViews() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle("提示");
        mProgressDialog.setMessage("图片上传中...");
        mCommitBtn = (Button) findViewById(R.id.btn_commit);
        mBackBtn = (ImageView) findViewById(R.id.iv_back);
    }

    @Override
    public void onBackPressed() {
        if(mPagerFragment != null && mPagerFragment.isVisible()) {
            mPagerFragment.runExitAnimation(new Runnable() {

                @Override
                public void run() {
                    if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        getSupportFragmentManager().popBackStack();
                    }
                }
            });
        } else {
            super.onBackPressed();
        }
    }

    public void addPhotoPagerFragment(PhotoPagerFragment photoPagerFragment) {

        this.mPagerFragment = photoPagerFragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, this.mPagerFragment)
                .addToBackStack(null)
                .commit();
    }
}
