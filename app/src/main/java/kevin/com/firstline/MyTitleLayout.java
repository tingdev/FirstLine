package kevin.com.firstline;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class MyTitleLayout extends LinearLayout {
    static final String TAG = "MyTitleLayout";
    Context context;
    public MyTitleLayout(final Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(TAG, "MyTitleLayout: ");
        this.context = context;
        boolean flag = attrs.getAttributeBooleanValue("http://schemas.android.com/apk/res-auto", "kevin_flag", false);
        Log.i(TAG, "MyTitleLayout: kevin flag " + flag);
        LayoutInflater.from(context).inflate(R.layout.custom_title, this);
        ImageButton back = (ImageButton)findViewById(R.id.btn_back);
        ImageButton search = (ImageButton)findViewById(R.id.btn_search);
        Button bdmap = (Button)findViewById(R.id.btn_bdmap);
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //((Activity)context).finish();

                Intent intent = new Intent(context, NotificationContentDetailActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// this flag is set dynamically, co-operate with 'taskAffinity', this activity would be in a new task stack
                context.startActivity(intent);

            }
        });

        bdmap.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)context).openBaiduMapPage();
            }
        });

        search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                Intent intent = new Intent(context, NotificationContentDetailActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);     // this flag is set dynamically, co-operate with 'taskAffinity', this activity would be in a new task stack
                //PS: use 'adb shell dumpsys activity activities' to show the tasks and stacks
                //NOTE: for PendingIntent, this flag FLAG_ACTIVITY_NEW_TASK is automatically added internally !!!!!!
                //   by tracking the log, you can find it!!!!
                PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                String channelId = "1234";

                // if build version >= 26, we MUST create a NotificationChannel to show the notifications!
                // if build version < 26, NotificationChannel is NOT supported, will cause crash, thus we determine the version here!
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel nc = new NotificationChannel(channelId, "fruitschannel", NotificationManager.IMPORTANCE_MAX);
                    nc.enableLights(true);
                    nc.setLightColor(Color.BLUE);
                    nm.createNotificationChannel(nc);
                }

                NotificationCompat.Builder nb = new NotificationCompat.Builder(context, channelId)
                        .setContentTitle("NotificationTitle")
                        .setContentText("NotificationText")
                        .setContentInfo("NotificationInfo")
                        .setSmallIcon(R.drawable.banana_alpha)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.banana))
                        .setContentIntent(pi)
                        .setAutoCancel(true)
                        .setNumber(3)
                        .setBadgeIconType(Notification.BADGE_ICON_LARGE)
                        .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(BitmapFactory.decodeResource(getResources(),R.drawable.banana)));
                nm.notify(1, nb.build());
            }
        });
    }

    public MyTitleLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.i(TAG, "MyTitleLayout: 2");
    }
}
