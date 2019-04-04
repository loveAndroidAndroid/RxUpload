package com.wen.rxupload.uploadfile.netHelper;

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;
import com.wen.network.bean.ResultException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import io.reactivex.subscribers.DisposableSubscriber;

/**
 * Created by wen on 2018/5/14.
 * 自定义DisposableSubscriber(业务相关)
 * 采用适配器模式，去除不必要的接口方法
 * 关于文件上传单独写UploadSubscriberCallBack回调和统一处理区别并去除耦合
 */
public abstract class UploadSubscriberCallBack<T> extends DisposableSubscriber<T> {

    //w网络库
    public static final String NET_ERROR = "网络不好，请确认网络重新连接";
    public static final String NOT_KNOW_ERROR = "未知错误，请重新开启APP";
    private long bytesWrittenTotal = 0;

    @Override
    public void onNext(T t) {
        onSuccess(t);
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
        //在这里做全局的错误处理
        if (e instanceof HttpException ||
                e instanceof ConnectException ||
                e instanceof SocketTimeoutException ||
                e instanceof TimeoutException ||
                e instanceof UnknownHostException) {
            //网络错误
            onFailure(new Throwable(NET_ERROR));
        } else if (e instanceof ResultException) {
            onFailure(e);
        } else {
            //其他错误
            onFailure(new Throwable(NOT_KNOW_ERROR));
        }
    }

    //监听文件进度的改变
    public void onProgressChange(long bytesWritten, long contentLength) {
        bytesWrittenTotal += bytesWritten;
        onProgress((int) (bytesWrittenTotal * 100 / contentLength));
    }

    //上传进度回调
    public abstract void onProgress(int progress);

    @Override
    public void onComplete() {
    }

    public abstract void onSuccess(T t);

    public abstract void onFailure(Throwable t);
}
