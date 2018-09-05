package kevin.com.firstline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent s = new Intent(context, MainActivity.class);
            s.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(s);
        }
    }
}
