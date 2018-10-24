package kevin.com.firstline;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class MyService extends Service {
    private static String TAG = "MyService";
    private static boolean isfg = false;

    private MockBinder mBinder = new MockBinder();
    public class MockBinder extends Binder {
        public void start() {
            Log.i(TAG, "start: downloading...");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SystemClock.sleep(2000);
                }
            }).start();
        }

        public int getProgress() {
            Log.i(TAG, "getProgress:");
            return 0;
        }
    }

    public static void setForground(boolean isfg) {
        MyService.isfg = isfg;
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    SystemClock.sleep(1000);
                    //stopSelf();       // can stop the service automatically after task processed.  another solution is using IntentService
                    Log.i(TAG, "run: inside onStartCommand");
                }
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: MyService");
        super.onCreate();

        if (!isfg) {
            return;
        }

        Intent intent = new Intent(this, NotificationContentDetailActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);

        String channelId = "1321";
        // if build version >= 26, we MUST create a NotificationChannel to show the notifications!
        // if build version < 26, NotificationChannel is NOT supported, will cause crash, thus we determine the version here!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel nc = new NotificationChannel(channelId, "ForgServiceChannelId", NotificationManager.IMPORTANCE_HIGH);
            nc.enableLights(true);
            nc.setLightColor(Color.BLUE);
            NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            nm.createNotificationChannel(nc);
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("content tile")
                .setContentText("content text")
                .setContentInfo("content info")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.apple)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.apple))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();
        startForeground(21, notification);
    }
}
