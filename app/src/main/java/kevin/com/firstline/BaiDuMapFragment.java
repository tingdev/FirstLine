package kevin.com.firstline;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;


/**
 * A simple {@link Fragment} subclass.
 */
public class BaiDuMapFragment extends Fragment {

    public BaiDuMapFragment(Context context) {
        // Required empty public constructor
        SDKInitializer.initialize(context.getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_bai_du_map, container, false);
        MapView mv = v.findViewById(R.id.bdmapView);
        ((MainActivity)getContext()).onBaiDuMapViewCreated(mv);
        return v;
    }

}
