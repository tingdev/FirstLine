package kevin.com.firstline;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlayAudioFragment extends Fragment {

    private static final String TAG = "PlayAudioFragment";
    private static WeakReference<View> mainViewWeakRef;
    private Button btnPlayAudio;
    private ProgressBar pgb;
    private TextView pgl;
    private static WeakReference<Context> contextWeakReference;

    public PlayAudioFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        android.support.v7.app.ActionBar actionBar = ((MainActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            //actionBar.setTitle("Play Audio");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_play_audio, container, false);
        btnPlayAudio = v.findViewById(R.id.play_audio);
        mainViewWeakRef = new WeakReference<>(v);
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach: ");
        contextWeakReference = new WeakReference<>(context);
    }

    private static class AudioPlayerListener implements AudioPlayer.Listener {
        private WeakReference<Button> btnPlayAudioWeakRef;
        private WeakReference<ProgressBar> pgb;
        private WeakReference<TextView> pgl;

        public AudioPlayerListener(View v) {
            btnPlayAudioWeakRef = new WeakReference<>((Button)v.findViewById(R.id.play_audio));
            pgb = new WeakReference<>((ProgressBar)v.findViewById(R.id.progressBar));
            pgl = new WeakReference<>((TextView)v.findViewById(R.id.progressLabel));
        }

        @Override
        public void onStart(int duration) {
            pgb.get().setMax(duration);
            btnPlayAudioWeakRef.get().setText("STOP");
        }

        @Override
        public void onProgress(int current, int max) {
            pgb.get().setProgress(current);
            pgl.get().setText(current + "/" + max);
        }

        @Override
        public void onStop() {
            btnPlayAudioWeakRef.get().setText("PLAY");
        }

        @Override
        public void onComplete(int lastPos, int max) {
            btnPlayAudioWeakRef.get().setText("PLAY");
            pgl.get().setText(String.format("%d/%d", lastPos, max));
            pgb.get().setProgress(lastPos);
        }
    }

    private static class BtnPlayListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            AudioPlayer.getInstance(contextWeakReference.get()).setListener(new AudioPlayerListener(mainViewWeakRef.get()));
            AudioPlayer.getInstance(contextWeakReference.get()).start();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        btnPlayAudio.setOnClickListener(new BtnPlayListener());
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView: ");
        super.onDestroyView();
        //mainView = null;
        AudioPlayer.getInstance(getContext()).release();
        if (btnPlayAudio != null) {
            btnPlayAudio.setOnClickListener(null);
            btnPlayAudio = null;
        }
        AudioPlayer.getInstance(getContext()).setListener(null);
    }

}
