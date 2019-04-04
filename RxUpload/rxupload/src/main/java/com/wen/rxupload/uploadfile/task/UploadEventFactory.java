package com.wen.rxupload.uploadfile.task;

import static com.wen.rxupload.uploadfile.task.UploadFlag.COMPLETE;
import static com.wen.rxupload.uploadfile.task.UploadFlag.FAIL;
import static com.wen.rxupload.uploadfile.task.UploadFlag.START;
import static com.wen.rxupload.uploadfile.task.UploadFlag.STARTED;

/**
 * Created by zhangxiaowen on 2018/11/2.
 * 采用简单工厂设计模式提供UploadEvent事件实体类
 */

public class UploadEventFactory {

    public static UploadEvent start(UploadStatus status) {
        return createEvent(START, status);
    }

    public static UploadEvent started(UploadStatus status) {
        return createEvent(STARTED, status);
    }

    public static UploadEvent failed(UploadStatus status, Throwable throwable) {
        return createEvent(FAIL, status, throwable);
    }

    public static UploadEvent completed(UploadStatus status) {
        return createEvent(COMPLETE, status);
    }

    private static UploadEvent createEvent(int flag, UploadStatus status, Throwable throwable) {
        UploadEvent event = createEvent(flag, status);
        event.setError(throwable);
        return event;
    }

    public static UploadEvent createEvent(int flag, UploadStatus status) {
        UploadEvent event = new UploadEvent();
        event.setUploadStatus(status == null ? new UploadStatus() : status);
        event.setFlag(flag);
        return event;
    }
}
