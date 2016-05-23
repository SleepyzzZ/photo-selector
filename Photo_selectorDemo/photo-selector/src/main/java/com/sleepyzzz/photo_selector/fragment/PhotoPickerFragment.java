package com.sleepyzzz.photo_selector.fragment;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sleepyzzz.photo_selector.R;
import com.sleepyzzz.photo_selector.activity.PhotoPickerActivity;
import com.sleepyzzz.photo_selector.adapter.PhotoPickerAdapter;
import com.sleepyzzz.photo_selector.entity.ImageFolder;
import com.sleepyzzz.photo_selector.event.OnItemClickListerner;
import com.sleepyzzz.photo_selector.event.OnPhotoDirSelected;
import com.sleepyzzz.photo_selector.view.ListImageDirPopupWindow;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * Date: 2016-04-14
 * Time: 15:24
 * FIXME
 */
public class PhotoPickerFragment extends Fragment {

    private final static String TAG = PhotoPickerFragment.class.getSimpleName();
    /**
     * 最大图片选择数量
     */
    public static final String EXTRA_SELECT_COUNT = "max_select_count";
    /**
     * 默认选择集
     */
    public static final String EXTRA_DEFAULT_SELECTED_LIST = "default_list";
    /**
     * 屏幕高度
     */
    public static final String EXTRA_SCREEN_HEIGHT = "screen_height";
    /**
     * 控件
     */
    private GridView mGridView;
    private Button mPreviewBtn;
    private Button mPopWindowBtn;
    private RelativeLayout mBottonLy;
    private Button mCommitBtn;
    private TextView mTopTv;
    /**
     * 当前选择的图片文件夹
     */
    private File mImgDir;
    /**
     * 第一个扫描到的文件夹
     */
    private String firstDirName;
    /**
     * 当前文件夹下图片名列表
     */
    private List<String> mImgNames;
    /**
     * 临时辅助类，用于防止同一个文件夹的多次扫描
     */
    private HashSet<String> mDirPaths = new HashSet<String>();
    /**
     * 扫描拿到所有的图片文件夹
     */
    private List<ImageFolder> mImageFolders = new ArrayList<ImageFolder>();
    /**
     * 文件夹选择popupwindow
     */
    private ListImageDirPopupWindow mDirPopupWindow;
    /**
     * 文件选择适配器
     */
    private PhotoPickerAdapter mPickerAdapter;

    private ProgressDialog mProgressDialog;

    private int mDesireSelectCount;
    private int mScreenHeight;
    private String mDesireSearchPath;

    private View rootView;

    public static PhotoPickerFragment newInstance(int screenHeight, int selectCount, String searchPath) {

        Bundle args = new Bundle();
        args.putInt(EXTRA_SCREEN_HEIGHT, screenHeight);
        args.putInt(EXTRA_SELECT_COUNT, selectCount);
        args.putString(EXTRA_DEFAULT_SELECTED_LIST, searchPath);
        PhotoPickerFragment fragment = new PhotoPickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mProgressDialog.dismiss();
            //为View绑定数据
            data2View();
            //初始化展示文件夹的PopupWindow
            initListDirPopupWindow();
        }
    };

    private void initListDirPopupWindow() {

        mDirPopupWindow = new ListImageDirPopupWindow(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (0.7 * mScreenHeight), mImageFolders, LayoutInflater.from(getActivity())
                .inflate(R.layout.list_folder, null));
        mDirPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //设置背景颜色变暗
                WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
                lp.alpha = 1.0f;
                getActivity().getWindow().setAttributes(lp);
            }
        });
        //设置选择文件夹的回调
        mDirPopupWindow.setOnPhotoDirSelected(new OnPhotoDirSelected() {
            @Override
            public void onSelected(ImageFolder folder) {
                mImgDir = new File(folder.getDir());
                mImgNames = Arrays.asList(mImgDir.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        if(filename.endsWith(".jpg") || filename.endsWith(".png")
                                || filename.endsWith(".jpeg"))
                            return true;
                        return false;
                    }
                }));
                mPickerAdapter = new PhotoPickerAdapter(getActivity(), mImgNames,
                        R.layout.grid_item_image, mImgDir.getAbsolutePath(), mDesireSelectCount);
                mGridView.setAdapter(mPickerAdapter);
                //item回调
                mPickerAdapter.setOnItemClickListerner(new OnItemClickListerner() {
                    @Override
                    public void onPhotoClick(View view, int position) {

                        List<String> mImgUrls = new ArrayList<String>();
                        for (int i = 0;i < mImgNames.size(); i++) {
                            mImgUrls.add(mImgDir + "/" + mImgNames.get(i));
                        }
                        int[] screenLocation = new int[2];
                        view.getLocationOnScreen(screenLocation);
                        PhotoPagerFragment fragment =
                                PhotoPagerFragment.newInstance(mImgUrls, position, mDesireSelectCount, screenLocation,
                                        view.getWidth(), view.getHeight());
                        ((PhotoPickerActivity) getActivity()).addPhotoPagerFragment(fragment);
                    }

                    @Override
                    public void onMarkClick(String path) {

                        refreshUI(path);
                    }
                });

                mPopWindowBtn.setText(folder.getName());
                mDirPopupWindow.dismiss();
            }
        });
    }

    private void refreshUI(String path) {
        if(PhotoPickerAdapter.mSelectedImage.contains(path)) {
            if(PhotoPickerAdapter.mSelectedImage.size() != 0) {
                mPreviewBtn.setEnabled(true);
                mPreviewBtn.setText(getResources().getString(R.string.preview)
                        + "(" + PhotoPickerAdapter.mSelectedImage.size() + ")");
                mCommitBtn.setEnabled(true);
                mCommitBtn.setText(String.format("%s(%d/%d)", getString(R.string.action_done)
                        , PhotoPickerAdapter.mSelectedImage.size(), mDesireSelectCount));
            } else {
                mPreviewBtn.setEnabled(false);
                mPreviewBtn.setText(R.string.preview);
                mCommitBtn.setEnabled(false);
                mCommitBtn.setText(R.string.action_done);
            }
        } else {
            if(PhotoPickerAdapter.mSelectedImage.size() == 0) {
                mPreviewBtn.setEnabled(false);
                mPreviewBtn.setText(R.string.preview);
                mCommitBtn.setEnabled(false);
                mCommitBtn.setText(R.string.action_done);
            } else {
                mPreviewBtn.setEnabled(true);
                mPreviewBtn.setText(getResources().getString(R.string.preview)
                        + "(" + PhotoPickerAdapter.mSelectedImage.size() + ")");
                mCommitBtn.setEnabled(true);
                mCommitBtn.setText(String.format("%s(%d/%d)",
                        getString(R.string.action_done), PhotoPickerAdapter.mSelectedImage.size(), mDesireSelectCount));
            }
        }
    }

    /**
     * 绑定数据
     */
    private void data2View() {

        if(mImgDir == null) {
            Toast.makeText(getActivity(), "no scanned images",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mPopWindowBtn.setText(firstDirName);
        mImgNames = Arrays.asList(mImgDir.list());
        /**
         * 文件夹路径个图片路径分开保存，减少了内存的消耗
         */
        mPickerAdapter = new PhotoPickerAdapter(getActivity(), mImgNames,
                R.layout.grid_item_image, mImgDir.getAbsolutePath(), mDesireSelectCount);
        mGridView.setAdapter(mPickerAdapter);
        //item选择回调
        mPickerAdapter.setOnItemClickListerner(new OnItemClickListerner() {
            @Override
            public void onPhotoClick(View view, int position) {

                List<String> mImgUrls = new ArrayList<String>();
                for (int i = 0;i < mImgNames.size(); i++) {
                    mImgUrls.add(mImgDir + "/" + mImgNames.get(i));
                }
                int[] screenLocation = new int[2];
                view.getLocationOnScreen(screenLocation);
                PhotoPagerFragment fragment =
                        PhotoPagerFragment.newInstance(mImgUrls, position, mDesireSelectCount, screenLocation,
                                view.getWidth(), view.getHeight());
                ((PhotoPickerActivity) getActivity()).addPhotoPagerFragment(fragment);
            }

            @Override
            public void onMarkClick(String path) {

                refreshUI(path);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //选择图片数量
        mDesireSelectCount = getArguments().getInt(EXTRA_SELECT_COUNT);
        //屏幕高度
        mScreenHeight = getArguments().getInt(EXTRA_SCREEN_HEIGHT);
        //默认选择
        mDesireSearchPath = getArguments().getString(EXTRA_DEFAULT_SELECTED_LIST);
        //扫描图片
        getImages();
    }

    private void initView(View view) {

        mGridView = (GridView) view.findViewById(R.id.gd_image);
        mPopWindowBtn = (Button) view.findViewById(R.id.btn_choose_dir);
        mPreviewBtn = (Button) view.findViewById(R.id.btn_preview);
        mBottonLy = (RelativeLayout) view.findViewById(R.id.layout_bottom_actionbar);
        mCommitBtn = (Button) getActivity().findViewById(R.id.btn_commit);
        mTopTv = (TextView) getActivity().findViewById(R.id.tv_back);

        mTopTv.setText(R.string.images);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //保存fragment视图状态
        rootView = getPersistentView(inflater, container, savedInstanceState, R.layout.fragment_grid_image);

        initView(rootView);
        initEvent();

        return rootView;
    }

    /**
     * 保存fragment视图状态
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @param layoutId
     * @return
     */
    private View getPersistentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState, int layoutId) {

        if(rootView == null) {

            rootView = inflater.inflate(layoutId, container, false);
        } else {

            ViewGroup parent = (ViewGroup) rootView.getParent();
            if(parent != null) {
                parent.removeView(rootView);
            }
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void initEvent() {
        mPopWindowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDirPopupWindow.setAnimationStyle(R.style.anim_popup_dir);
                mDirPopupWindow.showAsDropDown(mBottonLy, 0, 0);
                //设置背景颜色变暗
                WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
                lp.alpha = .3f;
                getActivity().getWindow().setAttributes(lp);
            }
        });
        //初始化按钮
        if(PhotoPickerAdapter.mSelectedImage == null ||
                PhotoPickerAdapter.mSelectedImage.size() <= 0) {
            mPreviewBtn.setText(R.string.preview);
            mPreviewBtn.setEnabled(false);
            mCommitBtn.setText(R.string.action_done);
            mCommitBtn.setEnabled(false);
        } else {
            mPreviewBtn.setText(getResources().getString(R.string.preview)
                    + "(" + PhotoPickerAdapter.mSelectedImage.size() + ")");
            mPreviewBtn.setEnabled(true);
            mCommitBtn.setText(String.format("%s(%d/%d)", getString(R.string.action_done),
                    PhotoPickerAdapter.mSelectedImage.size(), mDesireSelectCount));
            mCommitBtn.setEnabled(true);
        }
        //监听预览按钮
        mPreviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int[] screenLocation = new int[2];
                v.getLocationOnScreen(screenLocation);
                PhotoPagerFragment fragment =
                        PhotoPagerFragment.newInstance(PhotoPickerAdapter.mSelectedImage, 0, mDesireSelectCount, screenLocation,
                                v.getWidth(), v.getHeight());
                ((PhotoPickerActivity) getActivity()).addPhotoPagerFragment(fragment);
            }
        });
    }

    private String[] IMAGE_PROJECTION = {
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media._ID
    };

    /**
     * 扫描图片
     */
    private void getImages() {
        if(!Environment.getExternalStorageState().
                equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(getActivity(), "no external storage", Toast.LENGTH_SHORT).show();
            return;
        }
        //显示进度条
        mProgressDialog = ProgressDialog.show(getActivity(), null, "loading...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                String firstImage = null;
                Uri mImgUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = getActivity().getContentResolver();

                Cursor mCursor = null;
                //只查询jpeg和png图片
                if (mDesireSearchPath == null)
                {
                    mCursor = mContentResolver.query(mImgUri, IMAGE_PROJECTION,
                            IMAGE_PROJECTION[3] + "=? or "
                                    + IMAGE_PROJECTION[3] + "=?",
                            new String[]{"image/jpeg", "image/png"},
                            IMAGE_PROJECTION[2] + " DESC");
                } else
                {
                    mCursor = mContentResolver.query(mImgUri, IMAGE_PROJECTION,
                            IMAGE_PROJECTION[4] + ">0 AND " + IMAGE_PROJECTION[0]
                                    + " like '%" + mDesireSearchPath + "%'", null,
                            IMAGE_PROJECTION[2] + " DESC");
                }

                while(mCursor.moveToNext()) {
                    //获得图片路径
                    String path = mCursor.getString(mCursor.
                            getColumnIndex(MediaStore.Images.Media.DATA));
                    //拿到第一张图片的路径
                    if(firstImage == null) {
                        firstImage = path;
                    }

                    //获取该图片的父路径名
                    File parentFile = new File(path).getParentFile();
                    if(parentFile == null)
                        continue;
                    String dirPath = parentFile.getAbsolutePath();
                    ImageFolder imageFolder = null;
                    //利用一个HashSet防止多次扫描同一个文件夹
                    if(mDirPaths.contains(dirPath)) {
                        continue;
                    } else {
                        mDirPaths.add(dirPath);
                        //初始化ImageLoader
                        imageFolder = new ImageFolder();
                        imageFolder.setDir(dirPath);
                        imageFolder.setFirstImagePath(path);
                        //获取优先显示的图片父路径名
                        if(mCursor.getPosition() == 0) {
                            mImgDir = parentFile;
                            firstDirName = imageFolder.getName();
                        }
                    }

                    int picSize = parentFile.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String filename) {
                            if(filename.endsWith(".jpg")
                                    || filename.endsWith(".png")
                                    || filename.endsWith("jpeg"))
                                return true;
                            return false;
                        }
                    }).length;
                    //需要显示所有图片，修改此处
                    imageFolder.setCount(picSize);
                    mImageFolders.add(imageFolder);
                }
                mCursor.close();
                //扫描完成，辅助的HashSet释放内存
                mDirPaths = null;
                mHandler.sendEmptyMessage(0x110);
            }
        }).start();
    }

    public PhotoPickerAdapter getPhotoPickerAdapter() {

        return mPickerAdapter;
    }
}
