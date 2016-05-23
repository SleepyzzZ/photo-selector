package com.sleepyzzz.photo_selector.http.okhttp.builder;


import com.sleepyzzz.photo_selector.http.okhttp.request.PostFormRequest;
import com.sleepyzzz.photo_selector.http.okhttp.request.RequestCall;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhy on 15/12/14.
 *
 * update：SleepyzzZ on 2016/3/9 10:53
 * modify：加入批量上传文件方法addFiles
 */
public class PostFormBuilder extends OkHttpRequestBuilder implements HasParamsable
{
    private final static String TAG = "OkHttpRequestBuilder";

    private List<FileInput> files = new ArrayList<>();

    private final static String fileKey = "image";

    @Override
    public RequestCall build()
    {
        return new PostFormRequest(url, tag, params, headers, files).build();
    }

    public PostFormBuilder addFile(String name, String filename, File file)
    {
        files.add(new FileInput(name, filename, file));
        return this;
    }

    public PostFormBuilder addFiles(List<String> list)
    {
        File file;
        for(int i=0; i<list.size(); i++) {

            file = new File(list.get(i));
            addFile(fileKey, file.getName(), file);
        }

        return this;
    }

    public static class FileInput
    {
        public String key;
        public String filename;
        public File file;

        public FileInput(String name, String filename, File file)
        {
            this.key = name;
            this.filename = filename;
            this.file = file;
        }

        @Override
        public String toString()
        {
            return "FileInput{" +
                    "key='" + key + '\'' +
                    ", filename='" + filename + '\'' +
                    ", file=" + file +
                    '}';
        }
    }

    //
    @Override
    public PostFormBuilder url(String url)
    {
        this.url = url;
        return this;
    }

    @Override
    public PostFormBuilder tag(Object tag)
    {
        this.tag = tag;
        return this;
    }

    @Override
    public PostFormBuilder params(Map<String, String> params)
    {
        this.params = params;
        return this;
    }

    @Override
    public PostFormBuilder addParams(String key, String val)
    {
        if (this.params == null)
        {
            params = new LinkedHashMap<>();
        }
        params.put(key, val);
        return this;
    }

    @Override
    public PostFormBuilder headers(Map<String, String> headers)
    {
        this.headers = headers;
        return this;
    }


    @Override
    public PostFormBuilder addHeader(String key, String val)
    {
        if (this.headers == null)
        {
            headers = new LinkedHashMap<>();
        }
        headers.put(key, val);
        return this;
    }


}
