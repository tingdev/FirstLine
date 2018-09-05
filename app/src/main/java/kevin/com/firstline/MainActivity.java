package kevin.com.firstline;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private FruitRecyclerView frv;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isLocal = intent.getBooleanExtra("isLocal", false);
            boolean isOrdered = intent.getBooleanExtra("isOrdered", false);
            if (intent.getAction().equals("kevin.com.action.ADD_FRUIT")) {
                Toast.makeText(context, "add fruit broadcast received! local? " + isLocal + " ordered? " + isOrdered, Toast.LENGTH_SHORT).show();
                if (!isLocal) {
                    abortBroadcast();       // this will crash for local broadcast!
                }
            }
        }
    };

    private BroadcastReceiver anotherReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isLocal = intent.getBooleanExtra("isLocal", false);
            boolean isOrdered = intent.getBooleanExtra("isOrdered", false);
            if (intent.getAction().equals("kevin.com.action.ADD_FRUIT")) {
                Toast.makeText(context, "add fruit broadcast received in ANOTHER broadcast receiver! local? " + isLocal + " ordered? " + isOrdered, Toast.LENGTH_SHORT).show();
                if (!isLocal) {
                    abortBroadcast();       // this will crash for local broadcast!
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "trace onCreate: start");

        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        if (getActionBar()!=null) {
            getActionBar().hide();
        }

        Button resetFruit = (Button) findViewById(R.id.reset_fruit);
        resetFruit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fruits.init();
                frv.notifyDataSetChanged();
            }
        });

        Button addFruit = (Button) findViewById(R.id.add_fruit);
        addFruit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int rand = new Random().nextInt(Fruits.getFruits().size());
                Fruit selected = Fruits.getFruits().get(rand);

                Fruit n = new Fruit(Fruits.getFruits().size(), selected.getImageId(), selected.getName());
                Fruits.getFruits().add(n);

                frv.notifyItemInserted(Fruits.getFruits().size() - 1);

                // broeadcast and orderred broadcast, local broadcast tests.
                Intent i = new Intent("kevin.com.action.ADD_FRUIT");
                //sendBroadcast(i);

                i.putExtra("isOrdered", true);
                sendOrderedBroadcast(i, null);      // the receiver with the intentfield of higher priority would receive the broadcast first.

                //LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(MainActivity.this);
                //i.putExtra("isLocal", true);
                //lbm.sendBroadcast(i);





            }
        });

        // the one with the higher priority would receive the broadcast first.
        IntentFilter inf = new IntentFilter("kevin.com.action.ADD_FRUIT");
        inf.setPriority(200);
        registerReceiver(broadcastReceiver, inf);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, inf);

        IntentFilter anotherInf = new IntentFilter("kevin.com.action.ADD_FRUIT");
        anotherInf.setPriority(100);
        registerReceiver(anotherReceiver, anotherInf);
        LocalBroadcastManager.getInstance(this).registerReceiver(anotherReceiver, inf);
        Log.i(TAG, "trace onCreate:  end");
    }

    public void onFruitRecyclerViewCreated(FruitRecyclerView v) {
        Log.i(TAG, "onFruitRecyclerViewCreated: " + v);
        frv = v;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_item:
                Toast.makeText(MainActivity.this, "Add Clicked!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.remove_item:
                Toast.makeText(MainActivity.this, "Remove clicked!", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(anotherReceiver);
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(broadcastReceiver);
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(anotherReceiver);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
