package com.wen.network.bean;

import java.io.Serializable;

/**
 * Created by wen on 2018/5/14.
 */
public class TopResponse<Data> implements Serializable{

    private static final long serialVersionUID = -3414482088783616034L;
    private String code;
    private String msg;
    private Data data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getInfo() {
        return msg;
    }

    public void setInfo(String info) {
        this.msg = info;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
