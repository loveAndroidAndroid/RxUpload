package com.wen.network.bean;

import java.io.Serializable;

/**
 * Created by wen on 2018/5/14.
 * code不为1 解析msg错误字段
 */
public class ErrResponse implements Serializable {

    private static final long serialVersionUID = -5180361253882702933L;
    private String code;
    private String msg;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
