package com.wen.rxupload.uploadfile.task;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangxiaowen on 2018/11/1.
 * 上传操作所需构造的bean类
 */

public class UploadSingleItem implements Serializable {

    private static final long serialVersionUID = 2876093702446386602L;
    private Class zClass;//retrofit 所需的接口对象
    private String stringMethod;//上传文件的方法
    private String stringKey;//上传文件所需的key 字符串
    private Map<String, String> paramMap;//上传文件所需的其他参数的字符串
    private File file;//单张文件只传file
    private List<File> files;//多张文件传files集合
    private List<String> stringKeys;//多张文件传key集合

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public List<String> getStringKeys() {
        return stringKeys;
    }

    public void setStringKeys(List<String> stringKeys) {
        this.stringKeys = stringKeys;
    }

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

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
