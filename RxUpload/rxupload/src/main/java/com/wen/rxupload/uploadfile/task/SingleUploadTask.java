package com.wen.rxupload.uploadfile.task;


import com.wen.rxupload.uploadfile.manger.UploadMutiProcessManger;

import java.util.Map;
import java.util.concurrent.Semaphore;

import io.reactivex.disposables.Disposable;
import io.reactivex.processors.FlowableProcessor;

import static com.wen.rxupload.uploadfile.task.UploadEventFactory.completed;
import static com.wen.rxupload.uploadfile.task.UploadEventFactory.failed;
import static com.wen.rxupload.uploadfile.task.UploadEventFactory.started;


/**
 * Created by zhangxiaowen on 2018/11/2.
 * 单个任务的task
 */

public class SingleUploadTask<T> extends UploadTask {

    //上传图片的urlFile 也作为此次任务的唯一性ID
    private String urlFile;
    //本次任务的被观察着
    private FlowableProcessor<UploadEvent> processor;
    //上传进度回调对象
    private UploadStatus status;
    private Disposable disposable;

    public SingleUploadTask(UploadMutiProcessManger uploadMutiProcessManger, UploadMutiItem uploadItem) {
        super(uploadMutiProcessManger, uploadItem);
        this.urlFile = uploadItem.getUrlFile();
    }

    @Override
    public boolean init(Map<String, UploadTask> taskMap, Map<String, FlowableProcessor<UploadEvent>> processorMap) {
        //得到此次任务的被观察者
        processor = getProcessor(processorMap, urlFile);
        UploadTask uploadTask = taskMap.get(urlFile);
        if (uploadTask == null) {
            taskMap.put(urlFile, this);
            return true;
        }
        return false;
    }

    @Override
    public void startUploadFile(Semaphore semaphore) {
        disposable = startUploadFile(semaphore, new UploadTaskCallback<T>() {
            @Override
            public void start() {
            }

            @Override
            public void next(UploadStatus value) {
                status = value;
                processor.onNext(started(value));
            }

            @Override
            public void error(Throwable throwable) {
                processor.onNext(failed(status, throwable));
            }

            @Override
            public void complete(T t) {
                status.t = t;
                processor.onNext(completed(status));
            }
        });
    }

    @Override
    public void cancel() {
        dispose(disposable);
    }
}
