package com.sleepyzzz.photo_selector.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sleepyzzz.photo_selector.view.SquaredImageView;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * Date: 2016-04-08
 * Time: 16:14
 * FIXME
 */
public class ImageLoader {

    private static final String TAG = ImageLoader.class.getSimpleName();

    private static ImageLoader mInstance;
    /**
     * 线程池的线程数量
     */
    private int mThreadCount = 1;
    /**
     * 队列调度方式
     */
    private Type mType = Type.LIFO;
    /**
     * 轮询线程
     */
    private Thread mPoolThread;
    private Handler mPoolThreadHandler;
    /**
     * 线程池
     */
    private ExecutorService mThreadPool;
    /**
     * 由于线程池内部也有一个阻塞线程，防止加入任务的速度过快，使LIFO效果不明显
     */
    private volatile Semaphore mPoolSemaphore;
    /**
     * 防止取任务时mPoolThreadHandler未初始化完成
     */
    private volatile Semaphore mSemaphore = new Semaphore(0);
    /**
     * 图片缓存核心类
     */
    private LruCache<String, Bitmap> mLruCache;
    /**
     * 任务队列
     */
    private LinkedList<Runnable> mTasks;
    /**
     * 运行在UI线程的handler,用于给ImageIVew设置图图片
     */
    private Handler mHandler;
    /**
     * 单例模式双重校验锁
     * @return
     */
    public static ImageLoader getInstance() {

        if(mInstance == null) {
            synchronized (ImageLoader.class) {
                if(mInstance == null) {
                    mInstance = new ImageLoader(1, Type.LIFO);
                }
            }
        }
        return mInstance;
    }

    public static ImageLoader getInstance(int threadCount, Type type) {

        if(mInstance == null) {
            synchronized (ImageLoader.class) {
                if(mInstance == null) {
                    mInstance = new ImageLoader(threadCount, type);
                }
            }
        }
        return mInstance;
    }

    public ImageLoader(int threadCount, Type type) {

        //loop thread
        mPoolThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();

                mPoolThreadHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        mThreadPool.execute(getTask());
                        try {
                            mPoolSemaphore.acquire();
                        } catch (InterruptedException e) {
                        }
                    }
                };
                //释放一个信号量
                mSemaphore.release();
                Looper.loop();
            }
        };
        mPoolThread.start();

        //获取应用程序最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        mLruCache = new LruCache<String, Bitmap>(cacheSize) {

            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };

        mThreadPool = Executors.newFixedThreadPool(threadCount);
        mPoolSemaphore = new Semaphore(threadCount);
        mTasks = new LinkedList<Runnable>();
        mType = type == null ? Type.LIFO : type;
    }

    /**
     * 从任务队列取出一个任务
     * @return
     */
    private synchronized Runnable getTask() {
        if(mType == Type.FIFO) {
            return mTasks.removeFirst();
        } else if(mType == Type.LIFO) {
            return mTasks.removeLast();
        }
        return null;
    }

    /**
     * 队列的调度方式
     */
    public enum Type {

        FIFO, LIFO
    }

    /**
     * 加载图片
     * @param path
     * @param imageView
     */
    public void loadImage(final String path, final ImageView imageView) {
        //set tag
        imageView.setTag(path);
        //UI线程
        if(mHandler == null) {
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    ImageBeanHolder holder = (ImageBeanHolder) msg.obj;
                    ImageView imageView = holder.imageView;
                    Bitmap bm = holder.bitmap;
                    String path = holder.path;
                    if(imageView.getTag().toString().equals(path)) {
                        imageView.setImageBitmap(bm);
                    }
                }
            };
        }

        Bitmap bm = getBitmapFromLruCache(path);
        if(bm != null) {
            ImageBeanHolder holder = new ImageBeanHolder();
            holder.bitmap = bm;
            holder.imageView = imageView;
            holder.path = path;
            Message message = Message.obtain();
            message.obj = holder;
            mHandler.sendMessage(message);
        } else {
            addTask(new Runnable() {
                @Override
                public void run() {
                    ImageSize imageSize = getImageViewWidth(imageView);

                    int reqWidth = imageSize.width;
                    int reqHeight = imageSize.height;

                    Bitmap bm = decodeSampledBitmapFromResource(path, reqWidth, reqHeight);
                    addBitmapToLruCache(path, bm);
                    ImageBeanHolder holder = new ImageBeanHolder();
                    holder.bitmap = getBitmapFromLruCache(path);
                    holder.imageView = imageView;
                    holder.path = path;
                    Message message = Message.obtain();
                    message.obj = holder;
                    mHandler.sendMessage(message);
                    mPoolSemaphore.release();
                }
            });
        }
    }

    /**
     * 往LruCache中添加一张图片
     * @param key
     * @param bitmap
     */
    private void addBitmapToLruCache(String key, Bitmap bitmap) {
        if (getBitmapFromLruCache(key) == null)
        {
            if (bitmap != null)
                mLruCache.put(key, bitmap);
        }
    }

    /**
     * 根据计算的inSampleSize，得到压缩后图片
     * @param pathName
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private Bitmap decodeSampledBitmapFromResource(String pathName, int reqWidth, int reqHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(pathName, options);

        return bitmap;
    }

    /**
     * 计算inSampleSize，用于压缩图片
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 源图片的宽度
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;

        if (width > reqWidth && height > reqHeight)
        {
            // 计算出实际宽度和目标宽度的比率
            int widthRatio = Math.round((float) width / (float) reqWidth);
            int heightRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = Math.max(widthRatio, heightRatio);
        }

        return inSampleSize;
    }

    /**
     * 根据ImageView获得适当的压缩的宽和高
     * @param imageView
     * @return
     */
    private ImageSize getImageViewWidth(ImageView imageView) {
        ImageSize imageSize = new ImageSize();
        final DisplayMetrics displayMetrics = imageView.getContext()
                .getResources().getDisplayMetrics();
        final ViewGroup.LayoutParams params = imageView.getLayoutParams();

        int width = params.width == ViewGroup.LayoutParams.WRAP_CONTENT ? 0 : imageView
                .getWidth(); // Get actual image width
        if (width <= 0)
            width = params.width; // Get layout width parameter
        if (width <= 0)
            width = getImageViewFieldValue(imageView, "mMaxWidth"); // Check
        // maxWidth
        // parameter
        if (width <= 0)
            width = displayMetrics.widthPixels;
        int height = params.height == ViewGroup.LayoutParams.WRAP_CONTENT ? 0 : imageView
                .getHeight(); // Get actual image height
        if (height <= 0)
            height = params.height; // Get layout height parameter
        if (height <= 0)
            height = getImageViewFieldValue(imageView, "mMaxHeight"); // Check
        // maxHeight
        // parameter
        if (height <= 0)
            height = displayMetrics.heightPixels;
        imageSize.width = width;
        imageSize.height = height;

        return imageSize;
    }

    /**
     * 反射获得ImageView设置的最大宽度和高度
     * @param object
     * @param fieldName
     * @return
     */
    private int getImageViewFieldValue(Object object, String fieldName) {
        int value = 0;
        try
        {
            Field field = SquaredImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = (Integer) field.get(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE)
            {
                value = fieldValue;
            }
        } catch (Exception e)
        {
        }
        return value;
    }

    /**
     * 添加一个任务
     * @param runnable
     */
    private synchronized void addTask(Runnable runnable) {
        try {
            //请求信号量,防止mPoolThreadHandler为null
            if (mPoolThreadHandler == null) {

                mSemaphore.acquire();
            }
        } catch (InterruptedException e) {
        }
        mTasks.add(runnable);
        mPoolThreadHandler.sendEmptyMessage(0x110);
    }

    private Bitmap getBitmapFromLruCache(String key) {

        return mLruCache.get(key);
    }

    private class ImageBeanHolder {
        Bitmap bitmap;
        ImageView imageView;
        String path;
    }

    private class ImageSize {
        int width;
        int height;
    }
}
