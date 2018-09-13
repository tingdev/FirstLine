package kevin.com.firstline;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.Manifest.permission.CALL_PHONE;

public class MainActivity extends AppCompatActivity  implements  ContactsFragment.OnFragmentInteractionListener{
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

                LitePal.deleteDatabase("fruits");

                Intent i = new Intent("kevin.com.action.RESET_FRUIT");
                sendBroadcast(i, "com.kevin.permission.broadcast_send_from_specified_source");

            }
        });

        Button addFruit = (Button) findViewById(R.id.add_fruit);
        addFruit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int rand = new Random().nextInt(Fruits.getFruits().size());
                Fruit selected = Fruits.getFruits().get(rand);

                Fruit n = new Fruit(Fruits.getFruits().size(), selected.getImageId(), selected.getName(), selected.getDetail(), selected.getPrice());
                Fruits.getFruits().add(n);

                frv.notifyItemInserted(Fruits.getFruits().size() - 1);

                // broadcast and ordered broadcast, local broadcast tests.
                Intent i = new Intent("kevin.com.action.ADD_FRUIT");
                sendBroadcast(i, "com.kevin.permission.broadcast_send_to_specified_target");
                //sendBroadcast(i);

                //i.putExtra("isOrdered", true);
                //sendOrderedBroadcast(i, null);      // the receiver with the intentfield of higher priority would receive the broadcast first.

                //LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(MainActivity.this);
                //i.putExtra("isLocal", true);
                //lbm.sendBroadcast(i);
            }
        });
/*
        // the one with the higher priority would receive the broadcast first.
        IntentFilter inf = new IntentFilter("kevin.com.action.ADD_FRUIT");
        inf.setPriority(200);
        registerReceiver(broadcastReceiver, inf);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, inf);

        IntentFilter anotherInf = new IntentFilter("kevin.com.action.ADD_FRUIT");
        anotherInf.setPriority(100);
        registerReceiver(anotherReceiver, anotherInf);
        LocalBroadcastManager.getInstance(this).registerReceiver(anotherReceiver, inf);
*/
        LitePal.getDatabase();

        Button callBtn = findViewById(R.id.call_phone);
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                } else {
                    call(Uri.parse("tel:13819193687"));
                }
            }
        });

        Button viewContacts = findViewById(R.id.view_contacts);
        viewContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 2);
                } else {
                    showContacts(readContacts());
                }
            }
        });

        Log.i(TAG, "trace onCreate:  end");
    }

    private List<Contact> readContacts() {
        List<Contact> ls = new ArrayList<Contact>();
        Cursor cs = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        if (cs != null) {
            while (cs.moveToNext()) {
                String name = cs.getString(cs.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number = cs.getString(cs.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                ls.add(new Contact(name, number));
            }
            cs.close();
        }
        return ls;
    }

    private void showContacts(List<Contact> list) {
        ContactsFragment f = new ContactsFragment(list);
        getSupportFragmentManager().beginTransaction().add(R.id.main_layout, f).addToBackStack(null).commit();
    }

    private void call(Uri uri) {
        Intent i = new Intent(Intent.ACTION_CALL);
        i.setData(uri);
        startActivity(i);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                boolean called = false;
                for (int i = 0; i < grantResults.length; i++) {
                    if ((permissions[i].equals(Manifest.permission.CALL_PHONE)) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        call(Uri.parse("tel:13819193687"));
                        called = true;
                        break;
                    }
                }

                if (!called) {
                    Toast.makeText(MainActivity.this, "You're Not permitted to call!", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case 2: {
                boolean viewed = false;
                for (int i = 0; i < grantResults.length; i++) {
                    if ((permissions[i].equals(Manifest.permission.READ_CONTACTS)) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        showContacts(readContacts());
                        viewed = true;
                        break;
                    }
                }

                if (!viewed) {
                    Toast.makeText(MainActivity.this, "You're Not permitted to view contacts!", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default:
                break;
        }

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

    @Override
    public void onFragmentInteraction(Uri uri) {
        call(uri);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

}
