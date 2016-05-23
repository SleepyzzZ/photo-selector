package com.sleepyzzz.photo_selector.event;

import android.view.View;

/**
 * @ClassName：
 * @Description：TODO
 * @author：Administrator on 2016/4/15 10:31
 * @
 * @
 * @update：Administrator on 2016/4/15 10:31
 * @modify：
 */
public interface OnItemClickListerner {

    void onPhotoClick(View view, int position);

    void onMarkClick(String path);
}
