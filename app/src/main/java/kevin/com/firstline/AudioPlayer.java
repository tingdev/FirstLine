package kevin.com.firstline;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class AudioPlayer {
    private static final String TAG = "AudioPlayer";
    private MediaPlayer audioPlayer;
    private int audioDuration;
    private int currentBeforeStop;

    private static AudioPlayer instance;
    private Listener listener;
    private Context context;

    public interface Listener {
        public void onStart(int duration);
        public void onProgress(int current, int max);
        public void onStop();
        public void onComplete(int lastPos, int max);
    }

    private AudioPlayer(Context context) {
        this.context = context;
        if (audioPlayer == null) {
            audioPlayer = new MediaPlayer();
        }
    }

    public static AudioPlayer getInstance(Context context) {
        if (instance == null) {
            instance = new AudioPlayer(context);
        }
        return instance;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private static class PlayAudioProgressTask extends AsyncTask<Integer, Integer, Boolean> {

        private WeakReference<Listener> listenerWeakReference;
        private WeakReference<MediaPlayer> audioPlayerWeakReference;

        public PlayAudioProgressTask(MediaPlayer player, Listener listener) {
            listenerWeakReference = new WeakReference<>(listener);
            audioPlayerWeakReference = new WeakReference<>(player);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.i(TAG, "onProgressUpdate: " + values[0]);
            Listener listener = listenerWeakReference.get();
            if (listener != null) {
                listener.onProgress(values[0], values[1]);
            }
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            int max = params[0];
//            pgb.setMax(max);
            //SystemClock.sleep(100);
            int save = 0;
            while (!isCancelled() && audioPlayerWeakReference.get() != null && audioPlayerWeakReference.get().isPlaying()) {
                int current = audioPlayerWeakReference.get().getCurrentPosition();
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

    public void start() {
        audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                listener.onComplete(currentBeforeStop, audioDuration);
            }
        });

        if (ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((MainActivity)context, new String[]{WRITE_EXTERNAL_STORAGE}, MainActivity.REQUEST_PERMISSION_CODE_WRITE_EXTERNAL_STORAGE_FOR_OPEN_AUDIO);
        } else {
            togglePlayAudio();
        }
    }

    private AsyncTask<Integer, Integer, Boolean> progressTask;

    public void togglePlayAudio() {
        if (audioPlayer.isPlaying()) {
            currentBeforeStop = audioPlayer.getCurrentPosition();
            audioPlayer.reset();
            listener.onStop();
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
            progressTask = new PlayAudioProgressTask(audioPlayer, listener);
            progressTask.execute(audioDuration);
            listener.onStart(audioDuration);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void release() {
        if (progressTask != null) {
            progressTask.cancel(true);
        }
        if (audioPlayer != null) {
            audioPlayer.release();
        }
        // audioPlayer = null;
        context = null;       // unreference activity here!
        listener = null;
        instance = null;
    }
}
