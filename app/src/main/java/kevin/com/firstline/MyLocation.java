package kevin.com.firstline;

import android.content.Context;
import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

public class MyLocation {

    private static final String TAG = "MyLocation";
    private static MyLocation m;
    private Context context;
    private static LocationClient mClient;
    private BaiduMap map;
    private boolean isFirstTime;

    private MyLocation(Context context) {
        this.context = context;
    }

    public synchronized static MyLocation getInstance(Context context) {
        if (m == null) {
            m = new MyLocation(context);
            mClient = new LocationClient(context.getApplicationContext());
            LocationClientOption option = new LocationClientOption();
            option.setScanSpan(5000);
            option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
            option.setCoorType("bd09ll");       // this is vary important! to coordinated MUST be transformed.
            mClient.setLocOption(option);
        }
        return m;
    }

    private void navigateTo(BaiduMap map, BDLocation location) {
        if (isFirstTime) {
            map.animateMapStatus(MapStatusUpdateFactory.zoomTo(19f));
            isFirstTime = false;
        }

        if (location.getLocType() == BDLocation.TypeGpsLocation
                || location.getLocType() == BDLocation.TypeNetWorkLocation) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            map.animateMapStatus(MapStatusUpdateFactory.newLatLng(latLng));

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
        }
        if (mClient != null) {
            mClient.stop();
        }
        Log.i(TAG, "stop: ");
    }

}
