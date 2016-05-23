package com.sleepyzzz.photo_selector.http.okhttp.builder;


import com.sleepyzzz.photo_selector.http.okhttp.OkHttpUtils;
import com.sleepyzzz.photo_selector.http.okhttp.request.OtherRequest;
import com.sleepyzzz.photo_selector.http.okhttp.request.RequestCall;

/**
 * Created by zhy on 16/3/2.
 */
public class HeadBuilder extends GetBuilder
{
    @Override
    public RequestCall build()
    {
        return new OtherRequest(null, null, OkHttpUtils.METHOD.HEAD, url, tag, params, headers).build();
    }
}
