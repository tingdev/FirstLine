package kevin.com.firstline;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadTask extends AsyncTask<String, Integer, Integer> {

    private static final String TAG = "DownloadTask";

    public static final int RET_SUCCESS = 0;
    public static final int RET_FAIL = 1;
    public static final int RET_PAUSE = 2;
    public static final int RET_CANCEL = 3;

    private static final int NOTIFICATION_PERCENT_STEP = 10;        // set STEP to a relative larger value to avoid the notification queue not overflow (appeared notification LOST!)

    private boolean isPause = false;
    private boolean isCancel = false;

    private static String filePath = null;

    private DownloadInterface callback;

    public DownloadTask(DownloadInterface callback) {
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Integer integer) {
        Log.i(TAG, "onPostExecute: " + Thread.currentThread().getId());
        super.onPostExecute(integer);
        switch (integer) {
            case RET_SUCCESS:
                callback.onSuccess();
                break;
            case RET_PAUSE:
                callback.onPause();
                break;
            case RET_CANCEL:
                callback.onCancel();
                break;
            case RET_FAIL:
                callback.onFail();
                break;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        callback.onProgress(values[0]);
    }

    @Override
    protected void onCancelled(Integer integer) {
        super.onCancelled(integer);
    }

    @Override
    protected Integer doInBackground(String... strings) {

        Log.i(TAG, "doInBackground: " + Thread.currentThread().getId());

        String url = strings[0];

        callback.onStart();
        int index = url.lastIndexOf("/");
        String fileName = url.substring(index);
        filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + fileName;
        File file = new File(filePath);
        long downloadedLength = 0;
        if (file.exists()) {
            downloadedLength = file.length();
        }
        long contentLength = getContentLength(url);
        Log.i(TAG, "doInBackground: content len " + contentLength);
        if (contentLength <= 0) {
            return RET_FAIL;
        }
        if (downloadedLength >= contentLength) {
            return RET_SUCCESS;
        }

        OkHttpClient client = new OkHttpClient();
        Request req = new Request.Builder()
                .url(url)
                .addHeader("RANGE", "bytes=" + downloadedLength + "-")
                .build();
        InputStream is = null;
        RandomAccessFile raf = null;
        try {
            Response resp = client.newCall(req).execute();
            is = resp.body().byteStream();
            byte[] buff = new byte[1024];
            raf = new RandomAccessFile(file,"rw");
            raf.seek(downloadedLength);
            int len;
            int prevProgress = 0;
            while ((len = is.read(buff)) != -1) {
                raf.write(buff, 0, len);
                downloadedLength += len;
                int progress = (int)(downloadedLength * 100 / contentLength);
                if (progress >= prevProgress + NOTIFICATION_PERCENT_STEP) {
                    publishProgress(progress);
                    prevProgress = progress;
                }
                if (isPause) {
                    return RET_PAUSE;
                }
                if (isCancel) {
                    return RET_CANCEL;
                }
            }
            resp.body().close();
            return RET_SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (raf != null) {
                    raf.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RET_FAIL;
    }

    private long getContentLength(String url) {
        Request req = new Request.Builder()
                .url(url)
                .build();
        try {
            Response resp = new OkHttpClient().newCall(req).execute();
            long contentLen = 0;
            if (resp != null && resp.isSuccessful()) {
                contentLen = resp.body().contentLength();       // -1 means unknown
                resp.body().close();
                return contentLen;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    void pause() {
        isPause = true;
    }

    void cancel() {
        isCancel = true;
    }

    public static void deleteFile() {
        if (filePath == null) {
            return;
        }
        File file = new File(filePath);
        file.delete();
    }

}
