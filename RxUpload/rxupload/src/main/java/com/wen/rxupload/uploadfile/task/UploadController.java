package com.wen.rxupload.uploadfile.task;

/**
 * UploadController 上传回调的Controller
 */
public class UploadController {

    private Callback callback;

    public void handleClick(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void startUpload();

        void startNowUpload();
        
        void failUpload();

        void Complete(UploadEvent event);

    }

    public void setEvent(UploadEvent event) {
        int flag = event.getFlag();
        switch (flag) {
            case UploadFlag.START:
                callback.startUpload();
                break;
            case UploadFlag.STARTED:
                callback.startNowUpload();
                break;
            case UploadFlag.FAIL:
                callback.failUpload();
                break;
            case UploadFlag.COMPLETE:
                callback.Complete(event);
                break;
        }
    }
}
