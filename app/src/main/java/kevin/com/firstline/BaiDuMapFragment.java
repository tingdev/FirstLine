package kevin.com.firstline;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;


/**
 * A simple {@link Fragment} subclass.
 */
public class BaiDuMapFragment extends Fragment {

    private MapView mapView;

    public BaiDuMapFragment() {
        SDKInitializer.initialize(MyApplication.getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_bai_du_map, container, false);
        mapView = v.findViewById(R.id.bdmapView);
        ((MainActivity)getContext()).onBaiDuMapViewCreated(mapView);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
        ((MainActivity)getContext()).onBaiDuMapViewDestroy();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
