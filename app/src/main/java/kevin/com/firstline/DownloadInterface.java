package kevin.com.firstline;

public interface DownloadInterface {
    public void onStart();
    public void onPause();
    public void onProgress(int percent);
    public void onSuccess();
    public void onFail();
    public void onCancel();
}
