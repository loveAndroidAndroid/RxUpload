package com.wen.rxupload.uploadfile.task;

import java.io.Serializable;

/**
 * Created by zhangxiaowen on 2018/11/1.
 * UploadStatus 上传进度信息
 * 目前只有进度，提供一个类方便以后添加（如实时size）
 */

public class UploadStatus<T> implements Serializable {
    private static final long serialVersionUID = 3006498952627626456L;
    public int progress;
    public T t;
}
