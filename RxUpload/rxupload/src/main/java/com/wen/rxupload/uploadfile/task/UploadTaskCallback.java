package com.wen.rxupload.uploadfile.task;

/**
 * Created by zhangxiaowen on 2018/11/7.
 * 上传进度回调给单个任务
 */
public interface UploadTaskCallback<T> {

    void start();

    void next(UploadStatus status);

    void error(Throwable throwable);

    void complete(T t);
}
