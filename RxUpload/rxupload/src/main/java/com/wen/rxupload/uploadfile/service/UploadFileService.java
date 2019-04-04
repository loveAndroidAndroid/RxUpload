package com.wen.rxupload.uploadfile.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.wen.rxupload.uploadfile.task.UploadEvent;
import com.wen.rxupload.uploadfile.task.UploadTask;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zhangxiaowen on 2018/11/1.
 * startService开启服务
 * bindService 用户访问service中的方法等
 */

public class UploadFileService extends Service {

    //用于传递最大并发线程参数
    public static final String INTENT_KEY = "max_upload_number";
    //自定义UploadBinder 返回service实例
    private UploadBinder mBinder;
    //此Map用于返回进度的消息 用urlFile为唯一指定key
    private Map<String, FlowableProcessor<UploadEvent>> processorMap;
    //用于存储任务执行的队列 默认大小无限制
    private BlockingQueue<UploadTask> uploadQueue;
    //java并发包下的类  用于控制线程执行的个数
    private Semaphore semaphore;
    //上传task的列表 用urlFile为唯一指定key
    private Map<String, UploadTask> taskMap;
    private Disposable disposable;

    @Override
    public void onCreate() {
        super.onCreate();
        //执行初始化操作
        mBinder = new UploadBinder();
        uploadQueue = new LinkedBlockingQueue<>();
        processorMap = new ConcurrentHashMap<>();
        taskMap = new ConcurrentHashMap<>();
    }

    /**
     * 返回内部类binder对象
     *
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //启动队列
        startDispatch();
        return mBinder;
    }

    /**
     * 在onStartCommand中初始化最大并发线程数
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int maxUploadNumber = intent.getIntExtra(INTENT_KEY, 3);
            semaphore = new Semaphore(maxUploadNumber);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 在队列里添加任务
     *
     * @param uploadTask
     * @throws InterruptedException
     */
    public void addUploadTask(UploadTask uploadTask) throws InterruptedException {
        //如果此任务已经在taskMap中存在  则不添加到队列中
        boolean init = uploadTask.init(taskMap, processorMap);
        if (init) {
            uploadQueue.put(uploadTask);
        }
    }

    /**
     * 开始任务的调度
     * ObservableEmitter 类似发射器的东西 用于发射事件
     * Consumer  最小粒度的观察者
     */
    private void startDispatch() {
        disposable = Observable
                .create(new ObservableOnSubscribe<UploadTask>() {
                    @Override
                    public void subscribe(ObservableEmitter<UploadTask> emitter) throws Exception {
                        UploadTask uploadTask;
                        while (!emitter.isDisposed()) {
                            try {
                                uploadTask = uploadQueue.take();
                            } catch (InterruptedException e) {
                                continue;
                            }
                            emitter.onNext(uploadTask);
                        }
                        emitter.onComplete();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Consumer<UploadTask>() {
                    @Override
                    public void accept(UploadTask uploadTask) throws Exception {
                        uploadTask.startUploadFile(semaphore);
                    }
                });
    }

    /**
     * FlowableProcessor 系统级别的处理器 线程安全
     * 用来处理进度相应的消息
     * Processor和Subject的作用是相同的。
     * 关于Subject部分，RxJava1.x与RxJava2.x在用法上没有显著区别。其中Processor是RxJava2.x新增的，继承自Flowable
     * 所以其本身就是一个被观察者
     *
     * @return
     * @aram url
     * <p>
     * 此方法用于创建urlFile对应的FlowableProcessor
     */
    public FlowableProcessor<UploadEvent> receiveUploadEvent(String urlFile) {
        FlowableProcessor<UploadEvent> processor = createProcessor(urlFile);
        return processor;
    }

    private FlowableProcessor<UploadEvent> createProcessor(String urlFile) {
        if (processorMap.get(urlFile) == null) {
            FlowableProcessor<UploadEvent> processor =
                    BehaviorProcessor.<UploadEvent>create().toSerialized();
            processorMap.put(urlFile, processor);
        }
        return processorMap.get(urlFile);
    }

    /**
     * 再退出时 执行清空操作
     */
    public void clearAll() {
        uploadQueue.clear();
        taskMap.clear();
        processorMap.clear();
    }

    /**
     * 退出请求
     */
    public void cancelUpload(String urlFile) {
        UploadTask uploadTask = taskMap.get(urlFile);
        if (uploadTask != null) {
            uploadTask.cancel();
        }
    }

    /**
     * 用于对外暴露UploadFileService实例
     * Binder extent Ibinder
     */
    public class UploadBinder extends Binder {
        public UploadFileService getService() {
            return UploadFileService.this;
        }
    }
}
