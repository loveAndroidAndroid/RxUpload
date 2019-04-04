package com.wen.network.http;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * ResponseInfoAPI工具类
 */
public class RetrofitUtil {

    private static volatile RetrofitUtil instance = null;
    private Retrofit retrofit;

    private RetrofitUtil() {
    }

    /**
     * 静态内部类
     */
    public static RetrofitUtil getInstance() {
        if (instance == null) {
            synchronized (RetrofitUtil.class) {
                if (instance == null) {
                    instance = new RetrofitUtil();
                }
            }
        }
        return instance;
    }

    public void init(String baseURL, long readTime, long writeTime, long connectTime, Interceptor... interceptor) {
        retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .client(genericClient(readTime, writeTime, connectTime, interceptor))
                //2.自定义ConverterFactory处理异常情况
                .addConverterFactory(JsonArrayConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public OkHttpClient genericClient(long readTime, long writeTime, long connectTime, Interceptor... interceptors) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(readTime, TimeUnit.SECONDS)
                .writeTimeout(writeTime, TimeUnit.SECONDS)
                .connectTimeout(connectTime, TimeUnit.SECONDS);
        if (interceptors != null && interceptors.length > 0) {
            for (int i = 0; i < interceptors.length; i++) {
                builder.addInterceptor(interceptors[i]);
            }
        }
        return builder.build();
    }

    /**
     * 得到Retrofit的对象
     *
     * @return
     */
    public Retrofit getRetrofit() {
        return retrofit;
    }
}
