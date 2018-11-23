package kevin.com.firstline;

import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

public class MyLocation {

    private static final String TAG = "MyLocation";
    private static MyLocation instance;
    private LocationClient mClient;
    private BaiduMap map;
    private boolean isFirstTime = true;

    private MyLocation() {
    }

    public synchronized static MyLocation getInstance() {
        if (instance == null) {
            instance = new MyLocation();
        }
        return instance;
    }

    private void navigateTo(BaiduMap map, BDLocation location) {
        if (isFirstTime) {
            map.setTrafficEnabled(true);
            map.setMapStatus(MapStatusUpdateFactory.zoomTo(18));
            isFirstTime = false;
        }

        if (location.getLocType() == BDLocation.TypeGpsLocation
                || location.getLocType() == BDLocation.TypeNetWorkLocation) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            map.setMapStatus(MapStatusUpdateFactory.newLatLng(latLng));

            MyLocationData.Builder builder = new MyLocationData.Builder();
            MyLocationData data = builder
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build();
            map.setMyLocationEnabled(true);
            map.setMyLocationData(data);
        }
    }

    public void start(final BaiduMap map) {
        this.map = map;
        mClient = new LocationClient(MyApplication.getContext());
        LocationClientOption options = new LocationClientOption();
        options.setOpenGps(true);
        options.setCoorType("bd09ll");
        mClient.setLocOption(options);
        mClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                Log.i(TAG, "onReceiveLocation: " + bdLocation);
                navigateTo(map, bdLocation);
            }
        });
        mClient.start();
        Log.i(TAG, "start: ");
    }

    public void stop() {
        if (map != null) {
            map.setMyLocationEnabled(false);
            map = null;
        }
        if (mClient != null) {
            mClient.stop();
            mClient = null;
        }
        Log.i(TAG, "stop: ");
    }

}
