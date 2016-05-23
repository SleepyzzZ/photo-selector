package com.sleepyzzz.photo_selector.entity;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * Date: 2016-04-11
 * Time: 10:47
 * FIXME
 */
public class ImageFolder {
    /**
     * 图片的文件夹路径
     */
    private String dir;
    /**
     * 第一张图片的路径
     */
    private String firstImagePath;
    /**
     * 文件夹的名称
     */
    private String name;
    /**
     * 图片的数量
     */
    private int count;

    public void setDir(String dir) {
        this.dir = dir;
        int lastIndexOf = this.dir.lastIndexOf("/");
        this.name = this.dir.substring(lastIndexOf+1);
    }

    public void setFirstImagePath(String firstImagePath) {
        this.firstImagePath = firstImagePath;
    }

    public String getFirstImagePath() {
        return firstImagePath;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getDir() {
        return dir;
    }
}
