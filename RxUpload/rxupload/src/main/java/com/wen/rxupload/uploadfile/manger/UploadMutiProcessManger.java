package com.wen.rxupload.uploadfile.manger;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;

import com.wen.network.bean.TopResponse;
import com.wen.network.http.RetrofitUtil;
import com.wen.rxupload.uploadfile.netHelper.UploadSubscriberCallBack;
import com.wen.rxupload.uploadfile.requestBoy.UploadFileRequestBody;
import com.wen.rxupload.uploadfile.service.UploadFileService;
import com.wen.rxupload.uploadfile.task.SingleUploadTask;
import com.wen.rxupload.uploadfile.task.UploadEvent;
import com.wen.rxupload.uploadfile.task.UploadMutiItem;
import com.wen.rxupload.uploadfile.task.UploadStatus;
import com.wen.rxupload.uploadfile.task.UploadTaskCallback;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;

/**
 * Created by zhangxiaowen on 2018/11/1.
 * 多文件多进度一对一上传管理者
 */

public class UploadMutiProcessManger<T> {

    //当做发射器的参数 写所有对象的父类接受
    private static final Object object = new Object();
    @SuppressLint("StaticFieldLeak")
    //服务是否存在
    private static volatile boolean bound = false;
    //用于传递最大并发线程参数 默认为3
    private int maxUploadNumber = 3;
    private Context mContext;
    private Semaphore semaphore;
    //Service实例对象 用于调用Service的
    private UploadFileService uploadFileService;

    public UploadMutiProcessManger() {
    }

    public UploadMutiProcessManger(Context context) {
        this.mContext = context.getApplicationContext();
        semaphore = new Semaphore(1);
    }

    /**
     * 用于外界设置上传的最大线程并发数量
     *
     * @param max
     * @return
     */
    public UploadMutiProcessManger maxUploadNumber(int max) {
        this.maxUploadNumber = max;
        return this;
    }

    /**
     * 此次RxJava调度用于根据urlFile注册进度监听
     * 用于接受每个任务返回的进度 key为urlFile
     *
     * @param urlFile
     * @return
     */
    public Observable<UploadEvent> receiveUploadStatus(final String urlFile) {
        return createCommonObservable(null)
                .flatMap(new Function<Object, ObservableSource<UploadEvent>>() {
                    @Override
                    public ObservableSource<UploadEvent> apply(Object o) throws Exception {
                        return uploadFileService.receiveUploadEvent(urlFile).toObservable();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 此次RxJava调度用于开始文件的上传操作
     *
     * @param urlFile
     * @return
     */
    public Observable<?> startSingleUpload(final UploadMutiItem uploadItem) {
        return createCommonObservable(new GeneralObservableCallback() {
            @Override
            public void call() throws InterruptedException {
                uploadFileService.addUploadTask(new SingleUploadTask<T>(UploadMutiProcessManger.this, uploadItem));

            }
        }).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 提供创建Observable的公共方法并在首次调用时开始上传服务
     * 使用semaphore加锁  防止多线程抢占
     *
     * @param callback
     * @return
     */
    private Observable<?> createCommonObservable(final GeneralObservableCallback callback) {
        return Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(final ObservableEmitter<Object> emitter) throws Exception {
                if (!bound) {
                    semaphore.acquire();
                    if (!bound) {
                        startBindServiceAndCall(new ServiceConnectedCallback() {
                            @Override
                            public void call() {
                                doCall(callback, emitter);
                                //成功后放弃锁
                                semaphore.release();
                            }
                        });
                    } else {
                        doCall(callback, emitter);
                        semaphore.release();
                    }
                } else {
                    doCall(callback, emitter);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    /**
     * @param urlFile
     * @return
     */
    public Observable<?> cancelServiceUpload(final String urlFile) {
        return createCommonObservable(new GeneralObservableCallback() {
            @Override
            public void call() {
                uploadFileService.cancelUpload(urlFile);
            }
        }).observeOn(AndroidSchedulers.mainThread());

    }

    /**
     * 通过startService和bindService同时开启服务
     *
     * @param callback
     */
    private void startBindServiceAndCall(final ServiceConnectedCallback callback) {
        Intent intent = new Intent(mContext, UploadFileService.class);
        intent.putExtra(UploadFileService.INTENT_KEY, maxUploadNumber);
        mContext.startService(intent);
        mContext.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                UploadFileService.UploadBinder uploadBinder
                        = (UploadFileService.UploadBinder) binder;
                uploadFileService = uploadBinder.getService();
                //得到服务后注销此服务 防止内存泄露
                mContext.unbindService(this);
                bound = true;
                callback.call();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                //onServiceDisconnected() 在连接正常关闭的情况下是不会被调用的.
                //注意!!这个方法只会在系统杀掉Service时才会调用!!
                bound = false;
            }
        }, Context.BIND_AUTO_CREATE);
    }

    //执行RxJava后续操作
    //ObservableEmitter,俗称发射器
    private void doCall(GeneralObservableCallback callback, ObservableEmitter<Object> emitter) {
        if (callback != null) {
            try {
                callback.call();
            } catch (Exception e) {
                emitter.onError(e);
            }
        }
        emitter.onNext(object);
        emitter.onComplete();
    }

    public Disposable startUploadFileManger(UploadMutiItem uploadItem, final Semaphore semaphore, final UploadTaskCallback callback) {

        File file = getFile(uploadItem.getUrlFile());

        final UploadStatus uploadStatus = new UploadStatus<T>();

        UploadSubscriberCallBack<T> uploadSubscriberCallBack = new UploadSubscriberCallBack<T>() {

            @Override
            public void onProgress(int progress) {
                uploadStatus.progress = progress;
                callback.next(uploadStatus);
            }

            @Override
            public void onSuccess(T t) {
                callback.complete(t);
            }

            @Override
            public void onFailure(Throwable t) {
                callback.error(t);
            }
        };


        MultipartBody multipartBody = getMultipartBody(uploadItem, file, uploadSubscriberCallBack);

        Object o = RetrofitUtil
                .getInstance()
                .getRetrofit()
                .create(uploadItem.getzClass());

        Disposable disposable = null;
        try {
            Method declaredMethod = o.getClass().getDeclaredMethod(uploadItem.getStringMethod(), MultipartBody.class);
            Flowable<TopResponse<T>> invoke = (Flowable<TopResponse<T>>) declaredMethod.invoke(o, multipartBody);
            disposable = invoke.subscribeOn(Schedulers.io())
                    .doOnSubscribe(new Consumer<Subscription>() {
                        @Override
                        public void accept(Subscription subscription) throws Exception {
                            semaphore.acquire();
                        }
                    })
                    .doFinally(new Action() {
                        @Override
                        public void run() throws Exception {
                            semaphore.release();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(new Function<TopResponse<T>, Publisher<T>>() {
                        @Override
                        public Publisher<T> apply(TopResponse<T> tTopResponse) throws Exception {
                            if (tTopResponse.getCode().equals("1")) {
                                return Flowable.just(tTopResponse.getData());
                            } else {
                                return Flowable.error(new Throwable(tTopResponse.getInfo()));
                            }
                        }
                    })
                    .subscribeWith(uploadSubscriberCallBack);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return disposable;
    }

    @NonNull
    private MultipartBody getMultipartBody(UploadMutiItem uploadItem, File file, UploadSubscriberCallBack<T> uploadSubscriberCallBack) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        if (file == null) {
            return builder.build();
        }
        UploadFileRequestBody<T> uploadFileRequestBody = new UploadFileRequestBody<T>(file, uploadSubscriberCallBack);
        builder.addFormDataPart(uploadItem.getStringKey(), file.getName(), uploadFileRequestBody);
        Map<String, String> paramMap = uploadItem.getParamMap();
        if (paramMap != null) {
            Set<String> params = paramMap.keySet();
            for (String paramKey : params) {
                if (paramKey != null) {
                    String paramValue = paramMap.get(paramKey);
                    builder.addFormDataPart(paramKey, paramValue);
                }
            }
        }
        builder.setType(MultipartBody.FORM);
        return builder.build();
    }

    @NonNull
    private File getFile(String urlFile) {
        if (null == urlFile) {
            urlFile = "";
        }
        return new File(urlFile);
    }

    /**
     * 自定义接口用于流程的执行
     */
    private interface GeneralObservableCallback {
        void call() throws Exception;
    }

    private interface ServiceConnectedCallback {
        void call();
    }

    public void clear() {
        if (uploadFileService != null) {
            uploadFileService.clearAll();
            uploadFileService.stopSelf();
            uploadFileService = null;
        }
        bound = false;
    }
}
