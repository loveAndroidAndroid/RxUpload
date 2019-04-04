package com.wen.rxupload.uploadfile.task;

import java.io.Serializable;
import java.util.Map;

import io.reactivex.disposables.Disposable;

/**
 * Created by zhangxiaowen on 2018/11/1.
 * 上传操作所需构造的bean类
 */

public class UploadMutiItem implements Serializable {

    private static final long serialVersionUID = 6594398131889838048L;

    private Class zClass;//retrofit 所需的接口对象
    private String stringMethod;//上传文件的方法
    private String stringKey;//上传文件所需的key 字符串
    private Map<String, String> paramMap;//上传文件所需的其他参数的字符串
    private Disposable disposable;//监听文件上传进度的Disposable
    private String urlFile;//上传文件的url
    public int progress = 0;//上传文件的进度记录 用于列表更新进度

    public Class getzClass() {
        return zClass;
    }

    public void setzClass(Class zClass) {
        this.zClass = zClass;
    }

    public String getStringMethod() {
        return stringMethod;
    }

    public void setStringMethod(String stringMethod) {
        this.stringMethod = stringMethod;
    }

    public String getStringKey() {
        return stringKey;
    }

    public void setStringKey(String stringKey) {
        this.stringKey = stringKey;
    }

    public Map<String, String> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, String> paramMap) {
        this.paramMap = paramMap;
    }

    public Disposable getDisposable() {
        return disposable;
    }

    public void setDisposable(Disposable disposable) {
        this.disposable = disposable;
    }

    public String getUrlFile() {
        return urlFile;
    }

    public void setUrlFile(String urlFile) {
        this.urlFile = urlFile;
    }
}
