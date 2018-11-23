package kevin.com.firstline;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class NotificationContentDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_content_detail);
        Log.i("Notification", "my task id " + getTaskId());
    }
}
