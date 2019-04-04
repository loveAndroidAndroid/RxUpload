package com.wen.network.http;

import com.alibaba.fastjson.JSON;
import com.wen.network.bean.ErrResponse;
import com.wen.network.bean.ResultException;
import com.wen.network.bean.ResultResponse;
import com.wen.network.common.ConstantNet;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Created by wen on 2018/5/14.
 */

public class JsonArraResponseBodyConverter<T> implements Converter<ResponseBody, T> {

    private Type type;

    JsonArraResponseBodyConverter(Type type) {
        this.type = type;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        String response = value.string();
        try {
            //ResultResponse 只解析status字段
            ResultResponse resultResponse = JSON.parseObject(response, ResultResponse.class);
            if (Integer.parseInt(resultResponse.getCode()) == ConstantNet.CODE_SUCCESS) {
                //result==1表示成功返回，继续用本来的Model类解析
                return JSON.parseObject(response, type);
            } else {
                //ErrResponse 将msg解析为异常消息文本
                ErrResponse errResponse = JSON.parseObject(response, ErrResponse.class);
                throw new ResultException(resultResponse.getCode(), errResponse.getMsg());
            }
        } finally {
        }
    }
}
