package com.wen.network.bean;

import java.io.Serializable;

/**
 * Created by wen on 2018/5/14.
 * 自定义错误返回
 */
public class ResultException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 1441864083681083935L;
    private String errCode = "";

    public ResultException(String errCode, String msg) {
        super(msg);
        this.errCode = errCode;
    }

    public String getErrCode() {
        return errCode;
    }
}
