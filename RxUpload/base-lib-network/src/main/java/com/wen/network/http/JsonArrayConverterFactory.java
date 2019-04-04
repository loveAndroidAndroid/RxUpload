package com.wen.network.http;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by wen on 2018/5/14.
 */
public class JsonArrayConverterFactory extends Converter.Factory {


    public static JsonArrayConverterFactory create() {
        return new JsonArrayConverterFactory();

    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
                                                            Retrofit retrofit) {
        return new JsonArraResponseBodyConverter(type);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                          Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        return new JsonArraResponseBodyConverter(type);
    }
}
