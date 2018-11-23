package kevin.com.firstline;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;


/**
 * A simple {@link Fragment} subclass.
 */
public class ServiceTestFragment extends Fragment {

    private static final String TAG = "ServiceTestFragment";
    private Context context;
    private ServiceConnection serviceConn;
    private boolean isForgroundService;

    public ServiceTestFragment() {
        // Required empty public constructor
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_service_test, container, false);

        Button btnStartService = v.findViewById(R.id.start_service);
        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartService();
            }
        });

        Button btnStopService = v.findViewById(R.id.stop_service);
        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStopService();
            }
        });

        Button btnBindService = v.findViewById(R.id.bind_service);
        btnBindService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBindService();
            }
        });

        Button btnUnbindService = v.findViewById(R.id.unbind_service);
        btnUnbindService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUnbindService();
            }
        });

        CheckBox isfgcb = v.findViewById(R.id.is_forground_check_box);
        isForgroundService = isfgcb.isChecked();
        isfgcb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isForgroundService = isChecked;
            }
        });

        return v;
    }

    private void onStartService() {
        MyService.setForground(isForgroundService);
        context.startService(new Intent(context, MyService.class));
    }

    private void onStopService() {
        context.stopService(new Intent(context, MyService.class));
    }

    private void onBindService() {
        MyService.setForground(isForgroundService);
        if (serviceConn == null) {
            serviceConn = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.i(TAG, "onServiceConnected: ");
                    MyService.MockBinder db = (MyService.MockBinder) service;
                    db.start();
                    db.getProgress();
                    Log.i(TAG, "onServiceConnected: command end");
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.i(TAG, "onServiceDisconnected: ");
                }
            };
        }

        context.bindService(new Intent(context, MyService.class), serviceConn, context.BIND_AUTO_CREATE);
        Log.i(TAG, "onClick: bindservice");
    }

    private void onUnbindService() {
        if (serviceConn != null) {
            context.unbindService(serviceConn);
            serviceConn = null;
        }
        Log.i(TAG, "onClick: unbindService");
    }

}
