package kevin.com.firstline;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.jar.Attributes;

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
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Activity)context).finish();
            }
        });
        search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                Notification n = new NotificationCompat.Builder(context, null)
                        .setContentTitle("NotificationTitle")
                        .setContentText("NotificationText")
                        .setContentInfo("NotificationInfo")
                        .setSmallIcon(R.drawable.banana_alpha)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.banana))
                        .build();
                nm.notify(1, n);
            }
        });
    }

    public MyTitleLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.i(TAG, "MyTitleLayout: 2");
    }
}
