package kevin.com.firstline;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toolbar;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlayAudioFragment extends Fragment {

    private View v;

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
        v = inflater.inflate(R.layout.fragment_play_audio, container, false);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        final Button btnPlayAudio = v.findViewById(R.id.play_audio);
        final ProgressBar pgb = v.findViewById(R.id.progressBar);
        final TextView pgl = v.findViewById(R.id.progressLabel);
        btnPlayAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioPlayer.getInstance(getContext()).setListener(new AudioPlayer.Listener() {
                    @Override
                    public void onStart(int duration) {
                        pgb.setMax(duration);
                        btnPlayAudio.setText("STOP");
                    }

                    @Override
                    public void onProgress(int current, int max) {
                        pgb.setProgress(current);
                        pgl.setText(current + "/" + max);
                    }

                    @Override
                    public void onStop() {
                        btnPlayAudio.setText("PLAY");
                    }

                    @Override
                    public void onComplete(int lastPos, int max) {
                        btnPlayAudio.setText("PLAY");
                        pgl.setText(String.format("%d/%d", lastPos, max));
                        pgb.setProgress(lastPos);
                    }
                });
                AudioPlayer.getInstance(getContext()).start();
            }
        });
    }
}
