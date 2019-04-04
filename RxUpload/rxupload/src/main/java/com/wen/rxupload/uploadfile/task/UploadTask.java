package com.wen.rxupload.uploadfile.task;


import com.wen.rxupload.uploadfile.manger.UploadMutiProcessManger;

import java.util.Map;
import java.util.concurrent.Semaphore;

import io.reactivex.disposables.Disposable;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;

/**
 * Created by zhangxiaowen on 2018/10/31.
 * task抽象封装类
 */

public abstract class UploadTask {

    private UploadMutiProcessManger mUploadMutiProcessManger;
    public UploadMutiItem uploadItem;

    public UploadTask(UploadMutiProcessManger uploadMutiProcessManger, UploadMutiItem uploadItem) {
        this.mUploadMutiProcessManger = uploadMutiProcessManger;
        this.uploadItem = uploadItem;
    }

    //再单个任务中注入taskMap和processorMap
    public abstract boolean init(Map<String, UploadTask> taskMap,
                                 Map<String, FlowableProcessor<UploadEvent>> processorMap);

    //开始任务 传入Semaphore线程控制
    public abstract void startUploadFile(final Semaphore semaphore);

    //取消网络请求
    public abstract void cancel();

    /**
     * 得到对应的processor
     *
     * @param processorMap
     * @param urlFile
     * @return
     */
    protected FlowableProcessor<UploadEvent> getProcessor(Map<String,
            FlowableProcessor<UploadEvent>> processorMap, String urlFile) {
        if (processorMap.get(urlFile) == null) {
            FlowableProcessor<UploadEvent> processor =
                    BehaviorProcessor.<UploadEvent>create().toSerialized();
            processorMap.put(urlFile, processor);
        }
        return processorMap.get(urlFile);
    }

    /**
     * 任务开始上传的方法
     *
     * @param semaphore
     * @param callback
     * @return
     */

    public Disposable startUploadFile(final Semaphore semaphore, final UploadTaskCallback callback) {
        Disposable disposable = mUploadMutiProcessManger.startUploadFileManger(uploadItem, semaphore, callback);
        return disposable;
    }

    /**
     * 取消此次操作
     *
     * @param disposable
     */
    public void dispose(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
