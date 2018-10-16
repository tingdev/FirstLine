package kevin.com.firstline;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class MyService extends Service {
    private static String TAG = "MyService";

    private DownloadBinder mBinder = new DownloadBinder();
    public class DownloadBinder extends Binder {
        public void start() {
            Log.i(TAG, "start: downloading...");
            SystemClock.sleep(2000);
        }

        public int getProgress() {
            Log.i(TAG, "getProgress:");
            return 0;
        }
    }

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: MyService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: MyService");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: MyService");
        super.onCreate();
    }
}
