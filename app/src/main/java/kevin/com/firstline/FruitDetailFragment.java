package kevin.com.firstline;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class FruitDetailFragment extends Fragment {

    private Fruit f;

    public FruitDetailFragment(Fruit f) {
        this.f = f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fruit_detail_frag, container, false);
        TextView tv = view.findViewById(R.id.f_title);
        tv.setText(f.getName());

        ImageView iv = view.findViewById(R.id.f_image);
        iv.setImageResource(f.getImageId());

        tv = view.findViewById(R.id.f_detail);
        tv.setText(f.getFid() + "");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
