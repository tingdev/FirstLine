package kevin.com.firstline;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.MapView;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity  implements  ContactsFragment.OnFragmentInteractionListener{
    private static final String TAG = "MainActivity";
    private FruitRecyclerView frv;

    private static final int REQUEST_PERMISSION_CODE_CALL_PHONE = 1;
    private static final int REQUEST_PERMISSION_CODE_READ_CONTACTS = 2;
    private static final int REQUEST_PERMISSION_CODE_WRITE_EXTERNAL_STORAGE_FOR_OPEN_ALBUM = 3;
    public static final int REQUEST_PERMISSION_CODE_WRITE_EXTERNAL_STORAGE_FOR_OPEN_AUDIO = 4;
    private static final int REQUEST_PERMISSION_CODE_WRITE_EXTERNAL_STORAGE_FOR_DOWNLOAD = 5;
    private static final int REQUEST_PERMISSION_CODE_LOCATION = 6;

    private static final int TAKE_PHOTO = 1;
    private static final int CHOOSE_PHOTO = 2;
    private Uri imageUri;
    private boolean onSaveInstanceState;
    private boolean showPhotoLater;

    private Bitmap photoBitmap = null;

    DownloadService.DownloadBinder downloadBinder = null;
    //private String DOWNLOAD_URL = "https://raw.githubusercontent.com/guolindev/eclipse/master/eclipse-inst-win64.exe";
    private String DOWNLOAD_URL = "https://raw.githubusercontent.com/tuqinkui/first/master/wireshark.exe";

    private DrawerLayout drawerLayout;

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
        String s = stringFromJNI();
        Log.i(TAG, "onCreate: stringFromJNI " + s);
        //TextView tv = (TextView) findViewById(R.id.sample_text);
        //tv.setText(s);

        // ONLY for kevinTheme
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.menu);
        }

        drawerLayout = findViewById(R.id.drawer_layout);

        final NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nv_reset_fruit:
                        onResetFruit();
                        break;
                    case R.id.nv_add_fruit:
                        onAddFruit();
                        break;
                    case R.id.nv_delete_db_items:
                        deleteDbItems();
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nv_show_baidu_map:
                        navView.setCheckedItem(R.id.nv_show_baidu_map);
                        openBaiduMapPage();
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nv_make_call:
                        makeCall();
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nv_contacts:
                        viewContacts();
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nv_take_photo:
                        takePhoto();
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nv_from_album:
                        selectFromAlbum();
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nv_play_audio:
                        openPlayAudioPage();
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nv_show_webview:
                        openWebPage();
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nv_service:
                        openServiceTestPage();
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nv_my_title:
                        openMyTitlePage();
                        drawerLayout.closeDrawers();
                        break;
                }
                return false;
            }
        });

        FloatingActionButton fab = findViewById(R.id.floating_action_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Add Fruit?", Snackbar.LENGTH_SHORT)
                        .setAction("ADD", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onAddFruit();
                            }
                        }).show();
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

        tryXmlParser();
        tryGsonParser();
        tryOkHttp();

        openMainPage();

        Log.i("MainActivity", "my task id " + getTaskId());
        Log.i(TAG, "trace onCreate:  end");
    }

    private void tryXmlParser() {
        // imcompleted xml parser code
        try {
            XmlPullParser xpp = XmlPullParserFactory.newInstance().newPullParser();
            SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tryGsonParser() {
        // imcompleted gson parser code
        //Gson gson = new Gson();
        //List<Object> o = gson.fromJson("{'key':'value'}", new TypeToken<List<Object>>(){}.getType());    // REPLACE Object with the real CLASS!!!
    }

    private void tryOkHttp() {

        // simple OkHttpClient sample
        //RequestBody reqBody = new FormBody.Builder().add("name", "kevin").add("id", "1111").build();
        //RequestBody reqBody = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "name=kevin&id=1111"); // same as FormBody above!
        //RequestBody reqBody = RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), "name=kevin&id=1111");   // treat it as plain text

        // Use the imgur image upload API as documented at https://api.imgur.com/endpoints/image
        MultipartBody reqBody = new MultipartBody.Builder("AaB03x")
                .setType(MultipartBody.FORM)             // "Content-Type: multipart/form-data; boundary=AaB03x"
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"title\""),
                        RequestBody.create(null, "Square Logo"))
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"image\""),
                        RequestBody.create(MediaType.parse("image/png"), new File(Environment.getExternalStorageDirectory(), "toupload.png")))
                .build();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://192.168.3.157:4444/src/")
                .post(reqBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG, "onResponse: " + response.body().string());
            }
        });
    }

    private void onResetFruit() {
        Fruits.init();
        frv.notifyDataSetChanged();

        Intent i = new Intent("kevin.com.action.RESET_FRUIT");
        sendBroadcast(i, "com.kevin.permission.broadcast_send_from_specified_source");
    }

    private void onAddFruit() {
        int rand = new Random(System.currentTimeMillis()).nextInt(Fruits.getFruits().size());
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

    private void deleteDbItems() {
        try {
            //LitePal.deleteDatabase("fruits");
            DataSupport.deleteAll("fruit", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshFruits() {
        Fruits.shuffle();
        frv.notifyDataSetChanged();
    }

    private void makeCall() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PERMISSION_CODE_CALL_PHONE);
        } else {
            call(Uri.parse("tel:13819193687"));
        }
    }

    private void viewContacts(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_PERMISSION_CODE_READ_CONTACTS);
        } else {
            showContacts(readContacts());
        }
    }

    private void selectFromAlbum(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE_WRITE_EXTERNAL_STORAGE_FOR_OPEN_ALBUM);
        } else {
            openAlbum();
        }
    }

    private void openMainPage() {
        openFuitsPage();
        //openServiceTestPage();
    }

    private void openFuitsPage() {
        Fruits.init();
        FruitFragment ff = new FruitFragment();
        ff.setContext(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, ff).addToBackStack(null).commit();
    }

    private void openPlayAudioPage() {
        PlayAudioFragment paf = new PlayAudioFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, paf).addToBackStack(null).commit();
    }

    private void openWebPage() {
        WebViewFragment wvf = new WebViewFragment("https://www.sogou.com");
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, wvf).addToBackStack(null).commit();
    }

    private void openServiceTestPage() {
        ServiceTestFragment stf = new ServiceTestFragment();
        stf.setContext(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, stf).addToBackStack(null).commit();
    }

    private void openMyTitlePage() {
        MyTitleFragment mtf = new MyTitleFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mtf).addToBackStack(null).commit();
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
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PhotoFragment().setPhoto(bitmap)).addToBackStack(null).commit();
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
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, f).addToBackStack(null).commit();
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
            case REQUEST_PERMISSION_CODE_WRITE_EXTERNAL_STORAGE_FOR_OPEN_ALBUM: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case REQUEST_PERMISSION_CODE_WRITE_EXTERNAL_STORAGE_FOR_OPEN_AUDIO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AudioPlayer.getInstance(MainActivity.this).togglePlayAudio();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case REQUEST_PERMISSION_CODE_WRITE_EXTERNAL_STORAGE_FOR_DOWNLOAD: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchDownloadService();
                }
                break;
            }
            case REQUEST_PERMISSION_CODE_LOCATION: {
                if (grantResults.length > 0) {
                    boolean allGranted = true;
                    for (int result: grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            allGranted = false;
                            break;
                        }
                    }
                    if (allGranted) {
                        startLocation();
                    }
                } else {
                    Log.w(TAG, "onRequestPermissionsResult: " + grantResults.length );
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

    private boolean requestLocationPermissions() {
        List<String> locationPermissions = new ArrayList<String>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            locationPermissions.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            locationPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!locationPermissions.isEmpty()) {
            String[] permissions = locationPermissions.toArray(new String[locationPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CODE_LOCATION);
            return true;
        }
        return false;
    }

    private void startLocation() {
        MyLocation.getInstance(this).start(mapview.getMap());
    }

    private void stopLocation() {
        MyLocation.getInstance(this).stop();
    }

    public void openBaiduMapPage() {
        BaiDuMapFragment bdmf = new BaiDuMapFragment(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, bdmf).addToBackStack(null).commit();
    }

    private MapView mapview;
    public void onBaiDuMapViewCreated(MapView mv) {
        this.mapview = mv;
        if (!requestLocationPermissions()) {
            startLocation();
        }
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
            case R.id.update:
                startDownload();
                break;
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
        }
        return true;

    }

    private void startDownload() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE_WRITE_EXTERNAL_STORAGE_FOR_DOWNLOAD);
        } else {
            launchDownloadService();
        }
    }

    private ServiceConnection downloadSc = null;
    Intent downloadIntent;
    private void launchDownloadService() {

        downloadIntent = new Intent(this, DownloadService.class);
        downloadSc = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                downloadBinder = (DownloadService.DownloadBinder)service;
                downloadBinder.start(DOWNLOAD_URL);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        startService(downloadIntent);       // this is very IMPORTANT!!! this ensures the service always run even if the main activity exits!!!!!
        bindService(downloadIntent, downloadSc, BIND_AUTO_CREATE);
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

        if (mapview != null) {
            mapview.onResume();
        }

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
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PhotoFragment().setPhoto(bitmap)).addToBackStack(null).commit();
    }

    private long lastBackKeyTime = 0;

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        int stackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (stackEntryCount > 1) {
            super.onBackPressed();
            lastBackKeyTime = 0;
        } else {
            Log.i(TAG, "onBackPressed: ignored!" + stackEntryCount);
            if ((lastBackKeyTime != 0) && (System.currentTimeMillis() - lastBackKeyTime < 500)) {
                Log.w(TAG, "onBackPressed: Bye!");
                finish();
            }
            lastBackKeyTime = System.currentTimeMillis();
        }
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
        if (mapview != null) {
            mapview.onPause();
        }
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
        if (mapview != null) {
            mapview.onDestroy();
        }
        stopLocation();

        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(anotherReceiver);
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(broadcastReceiver);
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(anotherReceiver);

        if (downloadSc != null) {
            //stopService(downloadIntent);      //do NOT stop download service here!  we should keep service running!!!
            unbindService(downloadSc);
            downloadSc = null;
        }

        AudioPlayer.getInstance(MainActivity.this).release();
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
