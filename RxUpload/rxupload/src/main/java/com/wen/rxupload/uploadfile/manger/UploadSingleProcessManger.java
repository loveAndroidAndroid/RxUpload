package com.wen.rxupload.uploadfile.manger;

import android.text.TextUtils;

import com.wen.network.bean.TopResponse;
import com.wen.network.http.RetrofitUtil;
import com.wen.rxupload.uploadfile.netHelper.UploadSubscriberCallBack;
import com.wen.rxupload.uploadfile.requestBoy.UploadFileRequestBody;
import com.wen.rxupload.uploadfile.task.UploadSingleItem;

import org.reactivestreams.Publisher;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;

/**
 * Created by zhangxiaowen on 2018/10/31.
 * 单多文件单进度上传管理者
 */

public class UploadSingleProcessManger<T> {

    /**
     * 同步方法 单上传文件的封装
     *
     * @param file               需要上传的文件
     * @param fileUploadObserver 上传回调
     */
    public Disposable syncUpLoadFile(UploadSingleItem uploadSingleItem, UploadSubscriberCallBack<T> uploadSubscriberCallBack) {
        return baseUploadFiles(false, uploadSingleItem, uploadSubscriberCallBack);
    }

    /**
     * 单上传文件的封装
     *
     * @param file               需要上传的文件
     * @param fileUploadObserver 上传回调
     */
    public Disposable upLoadFile(UploadSingleItem uploadSingleItem, UploadSubscriberCallBack<T> uploadSubscriberCallBack) {
        return baseUploadFiles(true, uploadSingleItem, uploadSubscriberCallBack);
    }

    private Disposable baseUploadFiles(boolean isAsync, UploadSingleItem uploadSingleItem, UploadSubscriberCallBack<T> uploadSubscriberCallBack) {
        MultipartBody multipartBody = fileToMultipartBody(uploadSingleItem.getStringKey(), uploadSingleItem.getParamMap(), uploadSingleItem.getFile(), uploadSubscriberCallBack);
        Object o = RetrofitUtil
                .getInstance()
                .getRetrofit()
                .create(uploadSingleItem.getzClass());

        Disposable disposable = null;

        try {
            Method declaredMethod = o.getClass().getDeclaredMethod(uploadSingleItem.getStringMethod(), MultipartBody.class);
            Flowable<TopResponse<T>> invoke = (Flowable<TopResponse<T>>) declaredMethod.invoke(o, multipartBody);
            Flowable<TopResponse<T>> tmpInvoke = null;
            if (isAsync) {
                tmpInvoke = invoke
                        .subscribeOn(Schedulers.io());
            } else {
                tmpInvoke = invoke;
            }
            disposable = tmpInvoke
                    .observeOn(AndroidSchedulers.mainThread())
                    .onBackpressureBuffer()
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

    /**
     * 多上传文件的封装
     *
     * @param files              需要上传的多个文件
     * @param fileUploadObserver 上传回调
     */
    public Disposable upLoadFiles(UploadSingleItem uploadSingleItem, UploadSubscriberCallBack<T> uploadSubscriberCallBack) {
        MultipartBody multipartBody = filesToMultipartBody(uploadSingleItem.getStringKeys(), uploadSingleItem.getParamMap(), uploadSingleItem.getFiles(), uploadSubscriberCallBack);
        Object o = RetrofitUtil
                .getInstance()
                .getRetrofit()
                .create(uploadSingleItem.getzClass());

        Disposable disposable = null;

        try {
            Method declaredMethod = o.getClass().getDeclaredMethod(uploadSingleItem.getStringMethod(), MultipartBody.class);
            Flowable<TopResponse<T>> invoke = (Flowable<TopResponse<T>>) declaredMethod.invoke(o, multipartBody);
            disposable = invoke.subscribeOn(Schedulers.io())
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

    /**
     * 得到单文件上传的MultipartBody
     *
     * @param file
     * @param uploadSubscriberCallBack
     *
     * @return
     */
    public MultipartBody fileToMultipartBody(String name, Map<String, String> paramsMap, File file,
                                             UploadSubscriberCallBack<T> uploadSubscriberCallBack) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        if (file == null) {
            return builder.build();
        }
        UploadFileRequestBody uploadFileRequestBody =
                new UploadFileRequestBody<T>(file, uploadSubscriberCallBack);
        builder.addFormDataPart(name, file.getName(), uploadFileRequestBody);
        if (paramsMap != null) {
            Set<String> params = paramsMap.keySet();
            for (String paramKey : params) {
                if (paramKey != null) {
                    String paramValue = paramsMap.get(paramKey);
                    if (!TextUtils.isEmpty(paramValue)) {
                        builder.addFormDataPart(paramKey, paramValue);
                    }
                }
            }
        }
        builder.setType(MultipartBody.FORM);
        return builder.build();
    }

    /**
     * 得到多文件上传的MultipartBody
     *
     * @param files
     * @param uploadSubscriberCallBack
     *
     * @return
     */
    public MultipartBody filesToMultipartBody(List<String> name, Map<String, String> paramsMap, List<File> files,
                                              UploadSubscriberCallBack<T> uploadSubscriberCallBack) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        if (files == null || files.size() == 0) {
            return builder.build();
        }
        long content = 0;
        for (File file : files) {
            content += file.length();
        }
        for (int i = 0; i < files.size(); i++) {
            UploadFileRequestBody uploadFileRequestBody =
                    new UploadFileRequestBody<T>(content, files.get(i), uploadSubscriberCallBack);
            builder.addFormDataPart(name.get(i), files.get(i).getName(), uploadFileRequestBody);
        }
        if (paramsMap != null) {
            Set<String> params = paramsMap.keySet();
            for (String paramKey : params) {
                if (paramKey != null) {
                    String paramValue = paramsMap.get(paramKey);
                    builder.addFormDataPart(paramKey, paramValue);
                }
            }
        }
        builder.setType(MultipartBody.FORM);
        return builder.build();
    }
}
