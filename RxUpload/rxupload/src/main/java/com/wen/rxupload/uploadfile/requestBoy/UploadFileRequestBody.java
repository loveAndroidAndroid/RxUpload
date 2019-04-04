package com.wen.rxupload.uploadfile.requestBoy;


import com.wen.rxupload.uploadfile.netHelper.UploadSubscriberCallBack;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by zhangxiaowen on 2018/10/31.
 * 扩展OkHttp的请求体，实现上传文件时的进度提示
 */
public class UploadFileRequestBody<T> extends RequestBody {

    private static final String mediaType = "multipart/form-data";

    private RequestBody mRequestBody;
    private UploadSubscriberCallBack<T> uploadSubscriberCallBack;
    private long contentLength;

    public UploadFileRequestBody(File file, UploadSubscriberCallBack<T> uploadSubscriberCallBack) {
        this.mRequestBody = RequestBody.create(MediaType.parse(mediaType), file);
        this.uploadSubscriberCallBack = uploadSubscriberCallBack;
    }

    public UploadFileRequestBody(long contentLength, File file, UploadSubscriberCallBack<T> uploadSubscriberCallBack) {
        this.mRequestBody = RequestBody.create(MediaType.parse(mediaType), file);
        this.uploadSubscriberCallBack = uploadSubscriberCallBack;
        this.contentLength = contentLength;
    }

    @Override
    public MediaType contentType() {
        return mRequestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return mRequestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        CountingSink countingSink = new CountingSink(sink);
        BufferedSink bufferedSink = Okio.buffer(countingSink);
        //写入
        mRequestBody.writeTo(bufferedSink);
        //刷新
        //必须调用flush，否则最后一部分数据可能不会被写入
        bufferedSink.flush();
    }

    /**
     * 回传整个上传流的大小
     */
    protected final class CountingSink extends ForwardingSink {

        public CountingSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            if (uploadSubscriberCallBack != null) {
                if (contentLength > 0) {
                    uploadSubscriberCallBack.onProgressChange(byteCount, contentLength);
                } else {
                    uploadSubscriberCallBack.onProgressChange(byteCount, contentLength());
                }
            }
        }
    }
}
