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
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.litepal.LitePal;
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
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity  implements  ContactsFragment.OnFragmentInteractionListener{
    private static final String TAG = "MainActivity";
    private FruitRecyclerView frv;

    private static final int REQUEST_PERMISSION_CODE_CALL_PHONE = 1;
    private static final int REQUEST_PERMISSION_CODE_READ_CONTACTS = 2;
    private static final int REQUEST_PERMISSION_CODE_WRITE_EXTERNAL_STORAGE_FOR_OPEN_ALBUM = 3;
    private static final int REQUEST_PERMISSION_CODE_WRITE_EXTERNAL_STORAGE_FOR_OPEN_AUDIO = 4;

    private static final int TAKE_PHOTO = 1;
    private static final int CHOOSE_PHOTO = 2;
    private Uri imageUri;
    private boolean onSaveInstanceState;
    private boolean showPhotoLater;

    private Bitmap photoBitmap = null;

    private MediaPlayer audioPlayer = new MediaPlayer();
    private int audioDuration;
    private int currentBeforeStop;

    private TextView pgl;
    private ProgressBar pgb;

    private ServiceConnection serviceConn;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    class playAudioProgressTask extends AsyncTask<Integer, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            pgb.setProgress(values[0]);
            pgl.setText(values[0] + "/" + values[1]);
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            int max = params[0];
            pgb.setMax(max);
            //SystemClock.sleep(100);
            while (audioPlayer.isPlaying()) {
                int save = 0;
                int current = audioPlayer.getCurrentPosition();
                if ((current % 100 == 0) && current > save) {
                    onProgressUpdate(current, max);
                    save = current;
                }
            }
            return true;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }
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
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE_WRITE_EXTERNAL_STORAGE_FOR_OPEN_ALBUM);
                } else {
                    openAlbum();
                }
            }
        });

        final Button btnPlayAudio = findViewById(R.id.play_audio);
        audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnPlayAudio.setText("PLAY");
                        pgl.setText(String.format("%d/%d", currentBeforeStop, audioDuration));
                        pgb.setProgress(currentBeforeStop);
                    }
                });
            }
        });

        btnPlayAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE_WRITE_EXTERNAL_STORAGE_FOR_OPEN_AUDIO);
                } else {
                    playAudio();
                }
            }
        });

        Button btnWebView = findViewById(R.id.webview);
        btnWebView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebPage();
            }
        });

        tryXmlParser();
        tryGsonParser();
        tryOkHttp();

        pgb = findViewById(R.id.progressBar);
        pgl = findViewById(R.id.progressLabel);

        Button btnStartService = findViewById(R.id.start_service);
        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(MainActivity.this, MyService.class));
            }
        });

        Button btnStopService = findViewById(R.id.stop_service);
        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, MyService.class));
            }
        });

        Button btnBindService = findViewById(R.id.bind_service);
        btnBindService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceConn = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        Log.i(TAG, "onServiceConnected: ");
                        MyService.DownloadBinder db = (MyService.DownloadBinder)service;
                        db.start();
                        db.getProgress();
                        Log.i(TAG, "onServiceConnected: command end");
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        Log.i(TAG, "onServiceDisconnected: ");
                    }
                };

                bindService(new Intent(MainActivity.this, MyService.class), serviceConn, BIND_AUTO_CREATE);
                Log.i(TAG, "onClick: bindservice");
            }
        });

        Button btnUnbindService = findViewById(R.id.unbind_service);
        btnUnbindService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serviceConn != null) {
                    unbindService(serviceConn);
                    serviceConn = null;
                }
                Log.i(TAG, "onClick: unbindService");
            }
        });

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

    private void openWebPage() {
        WebViewFragment wvf = new WebViewFragment("https://www.sogou.com");
        getSupportFragmentManager().beginTransaction().add(R.id.main_layout, wvf).addToBackStack(null).commit();
    }

    private void playAudio() {
        Button btnPlayAudio = findViewById(R.id.play_audio);
        if (audioPlayer.isPlaying()) {
            currentBeforeStop = audioPlayer.getCurrentPosition();
            audioPlayer.reset();
            btnPlayAudio.setText("PLAY");
            return;
        }

        try {
            File file = new File(Environment.getExternalStorageDirectory(), "music.mp3");
            audioPlayer.reset();            // this is IMPORTANT for the second PLAY!   1st PLAY -- PLAY end -- 2nd PLAY
            audioPlayer.setDataSource(file.getPath());
            audioPlayer.prepare();
            audioPlayer.start();
            audioDuration = audioPlayer.getDuration();
            currentBeforeStop = audioDuration;      // in case user doesn't STOP actively(i.e. STOP due to play end).
            new playAudioProgressTask().execute(audioDuration);

            btnPlayAudio.setText("STOP");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    playAudio();
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

        audioPlayer.release();
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
