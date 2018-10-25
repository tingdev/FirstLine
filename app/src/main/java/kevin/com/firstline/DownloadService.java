package kevin.com.firstline;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

public class DownloadService extends Service implements DownloadInterface {

    private static String TAG = "DownloadService";
    private Binder mBinder = new DownloadBinder();

    private NotificationManager nm;
    private String channelId = "DownloadChannelId";
    private int NOT_ID = 3;

    public static String ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE";
    public static String ACTION_CANCEL = "ACTION_CANCEL";
    public static int CURRENT_STATUS_PLAY = 1;
    public static int CURRENT_STATUS_PAUSE = 2;

    private int lastPercent;
    private String lastUrl = null;

    //public static int currentStatus = CURRENT_STATUS_PAUSE;
    private Object lock = new Object();

    private boolean isDownloading() {
        synchronized (lock) {
            return (downloadTask != null);
        }
    }

    BroadcastReceiver downloadBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: broadcast" + intent.getAction());
            if (intent.getAction() == DownloadService.ACTION_PLAY_PAUSE) {
                boolean isDownloading = isDownloading();
                Log.i(TAG, "onReceive: " + isDownloading);
                if (isDownloading) {
                    paushDownloadTask();
                } else {
                    startDownloadTask(lastUrl);
                }
            } else if (intent.getAction() == DownloadService.ACTION_CANCEL) {
                cancelDownloadTask();
            }
        }
    };

    @Override
    public void onStart() {
        Log.i(TAG, "onStart: ");
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        synchronized (lock) {
            downloadTask = null;
        }
        Notification n = getNotification("Update paused", lastPercent, CURRENT_STATUS_PAUSE);
        nm.notify(NOT_ID, n);
    }

    @Override
    public void onProgress(int percent) {
        Log.i(TAG, "onProgress: " + percent);
        lastPercent = percent;
        //builder.setProgress(100, percent, false)
        //        .setContentText(percent + "%");
        Notification n = getNotification("Updating...", percent, CURRENT_STATUS_PLAY);
        nm.notify(NOT_ID, n);
    }

    @Override
    public void onSuccess() {
        Log.i(TAG, "onSuccess: " +  " thread id: " + Thread.currentThread().getId());
        synchronized (lock) {
            downloadTask = null;
        }
        Notification n = getNotification("Update success!", 100, CURRENT_STATUS_PAUSE);
        nm.notify(NOT_ID, n);
    }

    @Override
    public void onFail() {
        Log.i(TAG, "onFail: ");
        synchronized (lock) {
            downloadTask = null;
        }
        Notification n = getNotification("Update failed!", -1, CURRENT_STATUS_PAUSE);
        nm.notify(NOT_ID, n);
    }

    @Override
    public void onCancel() {
        Log.i(TAG, "onCancel: ");
        synchronized (lock) {
            downloadTask = null;
        }
        lastPercent = 0;
        Notification n = getNotification("Update cancelled!", 0, CURRENT_STATUS_PAUSE);
        nm.notify(NOT_ID, n);
    }

    private void prepare() {
        nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel nc = new NotificationChannel(channelId, "DownloadNotificationChannel", NotificationManager.IMPORTANCE_DEFAULT);
            nm.createNotificationChannel(nc);
        }

        IntentFilter intf = new IntentFilter(DownloadService.ACTION_PLAY_PAUSE);
        intf.addAction(DownloadService.ACTION_CANCEL);
        registerReceiver(downloadBroadcastReceiver, intf);

    }
    private RemoteViews rv;
    private RemoteViews getRemoteViews() {
        if (rv == null) {
            rv = new RemoteViews(this.getPackageName(), R.layout.download_notification);

            Intent playPauseIntent = new Intent(ACTION_PLAY_PAUSE);
            PendingIntent pausePlayPi = PendingIntent.getBroadcast(this, 1, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.pause_play, pausePlayPi);

            Intent cancelIntent = new Intent(ACTION_CANCEL);
            PendingIntent cancelPi = PendingIntent.getBroadcast(this, 1, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.cancel, cancelPi);
        }

        return rv;
    }

    private Notification getNotification(String title, int percent, int current_status) {
        Intent intent = new Intent(DownloadService.this, NotificationContentDetailActivity.class);
        PendingIntent pi = PendingIntent.getActivity(DownloadService.this, 0, intent, 0);

        RemoteViews remoteViews = getRemoteViews();
        remoteViews.setTextViewText(R.id.title, title);
        if (percent != -1) {
            remoteViews.setTextViewText(R.id.progress, percent + "%");
            remoteViews.setProgressBar(R.id.progressbar, 100, percent, false);
        }

        if (current_status == CURRENT_STATUS_PAUSE) {
            remoteViews.setImageViewResource(R.id.pause_play, R.drawable.pause);
        } else if (current_status == CURRENT_STATUS_PLAY) {
            remoteViews.setImageViewResource(R.id.pause_play, R.drawable.play);
        } else {
            remoteViews.setImageViewResource(R.id.pause_play, R.drawable.kiwi);  //Ooops!
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(DownloadService.this, channelId);
        Notification n = builder
                .setSmallIcon(R.drawable.grape)     /* no use but IMPORTANT, otherwise the whole notification will NOT show the custom view */
                .setOnlyAlertOnce(true)
                .setContentIntent(pi)
                /*
                  .setContentTitle("Update")
                */
                .setCustomBigContentView(remoteViews)       // big content view respects the wrap_content setting
                .setCustomContentView(remoteViews)
                .build();
        return n;
    }

    private DownloadTask downloadTask = null;

    private void startDownloadTask(String url) {
        synchronized (lock) {
            if (downloadTask == null) {
                downloadTask = new DownloadTask(DownloadService.this);
                downloadTask.execute(url);
                Notification n = getNotification("Updating...", 0, CURRENT_STATUS_PLAY);
                startForeground(NOT_ID, n);
                lastUrl = url;
            }
        }
    }

    private void cancelDownloadTask() {
        synchronized (lock) {
            if (downloadTask != null) {
                downloadTask.cancel();
            }
        }
        DownloadTask.deleteFile();

        Notification n = getNotification("Update cancelled!", -1, CURRENT_STATUS_PLAY);
        nm.notify(NOT_ID, n);
    }

    private void paushDownloadTask() {
        synchronized (lock) {
            if (downloadTask != null) {
                downloadTask.pause();
            }
        }
    }

    public class DownloadBinder extends Binder {
        void start(String url) {
            startDownloadTask(url);
        }

        void pause() {
            paushDownloadTask();
        }

        void cancel() {
            cancelDownloadTask();
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        prepare();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
        unregisterReceiver(downloadBroadcastReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
