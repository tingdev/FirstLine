package kevin.com.firstline;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;

import org.litepal.LitePal;

public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        context = getApplicationContext();
        LitePal.initialize(context);

        if (!LeakCanary.isInAnalyzerProcess(this)) {
            LeakCanary.install(this);
        }
    }

    public static Context getContext() {
        return context;
    }


}
