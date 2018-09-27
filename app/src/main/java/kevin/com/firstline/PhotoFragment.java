package kevin.com.firstline;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class PhotoFragment extends Fragment {

    private Bitmap photo;

    public PhotoFragment() {

    }

    PhotoFragment setPhoto(Bitmap photo) {
        this.photo = photo;
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_photo, container, false);
        ImageView ivPhoto = v.findViewById(R.id.iv_photo);
        ivPhoto.setImageBitmap(photo);
        return v;
    }

}
