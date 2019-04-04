package com.wen.rxupload.uploadfile.task;

import java.io.Serializable;

/**
 * Created by zhangxiaowen on 2018/11/1.
 * 上传回调的事件
 */

public class UploadEvent implements Serializable {

    private static final long serialVersionUID = 3398585473606266145L;
    private int flag = UploadFlag.START;
    private UploadStatus uploadStatus = new UploadStatus();
    private Throwable mError;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public UploadStatus getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(UploadStatus uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public Throwable getError() {
        return mError;
    }

    public void setError(Throwable error) {
        mError = error;
    }
}
