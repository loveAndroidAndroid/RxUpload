package com.wen.network.bean;

import java.io.Serializable;

/**
 * Created by wen on 2018/5/14.
 * 首次请求只解析code字段
 */

public class ResultResponse implements Serializable {

    private static final long serialVersionUID = -1489959380425705494L;
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
