package kevin.com.firstline;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.PersistableBundle;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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
import org.w3c.dom.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity  implements  ContactsFragment.OnFragmentInteractionListener{
    private static final String TAG = "MainActivity";
    private FruitRecyclerView frv;

    private static final int REQUEST_PERMISSION_CODE_CALL_PHONE = 1;
    private static final int REQUEST_PERMISSION_CODE_READ_CONTACTS = 2;
    private static final int REQUEST_PERMISSION_CODE_WRITE_EXTERNAL_STORAGE = 3;

    private static final int TAKE_PHOTO = 1;
    private static final int CHOOSE_PHOTO = 2;
    private Uri imageUri;
    private boolean onSaveInstanceState;
    private boolean showPhotoLater;

    private Bitmap photoBitmap = null;

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

        // the one with the higher priority would receive the broadcast first.
        IntentFilter inf = new IntentFilter("kevin.com.action.ADD_FRUIT");
        inf.setPriority(200);
        registerReceiver(broadcastReceiver, inf);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, inf);

        IntentFilter anotherInf = new IntentFilter("kevin.com.action.ADD_FRUIT");
        anotherInf.setPriority(100);
        registerReceiver(anotherReceiver, anotherInf);
        LocalBroadcastManager.getInstance(this).registerReceiver(anotherReceiver, inf);

        LitePal.getDatabase();

        Button callBtn = findViewById(R.id.call_phone);
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PERMISSION_CODE_CALL_PHONE);
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
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_PERMISSION_CODE_READ_CONTACTS);
                } else {
                    showContacts(readContacts());
                }
            }
        });

        Button btnTakePhoto = findViewById(R.id.take_photo);
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });

        Button btnChooseFromAlbum = findViewById(R.id.choose_from_album);
        btnChooseFromAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE_WRITE_EXTERNAL_STORAGE);
                } else {
                    openAlbum();
                }
            }
        });

        Log.i("MainActivity", "my task id " + getTaskId());
        Log.i(TAG, "trace onCreate:  end");
    }

    private void takePhoto() {
        File imageFile = new File(getExternalCacheDir(), "photo.jpg");
        try {
            if (imageFile.exists()) {
                imageFile.delete();
            }
            imageFile.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= 24) {
            imageUri = FileProvider.getUriForFile(this, "kevin.com.firstline.fileprovider", imageFile);
        } else {
            imageUri = Uri.fromFile(imageFile);
        }

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);

    }

    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        if (onSaveInstanceState) {

                            // This will cause 'Can not perform this action after onSaveInstanceState' error!
                            /*
                            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                            getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, new PhotoFragment().setPhoto(bitmap)).addToBackStack(null).commit();
                            */

                            showPhotoLater = true;
                            photoBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                String imagePath = null;
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        imagePath = getImagePathOnOrAfterKitKat(data);
                    } else {
                        imagePath = getImagePathBeforeKitKat(data);
                    }

                    if (imagePath != null) {
                        if (onSaveInstanceState) {
                            showPhotoLater = true;
                            photoBitmap = BitmapFactory.decodeFile(imagePath);
                        }
                    }
                }
                break;
        }
    }

    private String getImagePathOnOrAfterKitKat(Intent data) {
        // data example: "content://com.android.providers.media.documents/document/image:1867767 flag=0x1", NOT a String, it's the inside information!!!
        String imagePath = null;
        Uri uri = data.getData();   // uri:  "content://com.android.providers.media.documents/document/image%3A1867767"
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);        //example:  "image:186776"
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;      // "_id=186776"
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);  // "/storage/emulated/0/Pictures/Screenshots/Screenshot_20181007-131113.jpg"
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath();
        }
        return imagePath;
    }

    private String getImagePathBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        return getImagePath(uri, null);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
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
            case REQUEST_PERMISSION_CODE_CALL_PHONE: {
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
            case REQUEST_PERMISSION_CODE_READ_CONTACTS: {
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
            case REQUEST_PERMISSION_CODE_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
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
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart: ");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(TAG, "onRestoreInstanceState: ");
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        Log.i(TAG, "onRestoreInstanceState: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (showPhotoLater) {
            try {
                showPhoto(photoBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
            onSaveInstanceState = false;
            showPhotoLater = false;
        }
        Log.i(TAG, "onResume: ");
    }

    private void showPhoto(Bitmap bitmap) {
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_container, new PhotoFragment().setPhoto(bitmap)).addToBackStack(null).commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        onSaveInstanceState = true;
        Log.i(TAG, "onSaveInstanceState: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");
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
