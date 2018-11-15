package kevin.com.firstline;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

public class AutoRefreshService extends Service {
    private static final String TAG = "AutoRefreshservice";
    private OnAutoRefreshListener listener;

    public interface OnAutoRefreshListener {
        void onRefresh();
    }

    public void setAutoRefreshListener(OnAutoRefreshListener listener) {
        Log.i(TAG, "setAutoRefreshListener: ");
        this.listener = listener;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind: ");
        return new MyBinder();
    }

    public class MyBinder extends Binder {
        public AutoRefreshService getService() {
            return AutoRefreshService.this;
        }
    }

    private Intent it;
    private PendingIntent pi;
    private void triggerOnStartCommand(int interval) {
        AlarmManager alarmManager = (AlarmManager)MyApplication.getContext().getSystemService(Context.ALARM_SERVICE);
        it = new Intent(AutoRefreshService.this, AutoRefreshService.class);
        pi = PendingIntent.getService(AutoRefreshService.this, 0, it, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, pi);
    }

    private void cancelAlarm() {
        if (pi != null) {
            AlarmManager alarmManager = (AlarmManager)MyApplication.getContext().getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pi);
        }
        listener = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: " + listener);
        if (listener != null) {
            Log.i(TAG, "onStartCommand: callback!");
            listener.onRefresh();
        }
        triggerOnStartCommand(10000);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind: ");
        cancelAlarm();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }
}
