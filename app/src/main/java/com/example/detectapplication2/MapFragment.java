package com.example.detectapplication2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.engine.SDKNativeEngine;
import com.here.sdk.core.engine.SDKOptions;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapview.MapError;
import com.here.sdk.mapview.MapImage;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapMeasure;
import com.here.sdk.mapview.MapScene;
import com.here.sdk.mapview.MapScheme;
import com.here.sdk.mapview.MapView;

public class MapFragment extends Fragment {

    private MapView mapView;
    private LocationManager locationManager;
    private static final String TAG = MapFragment.class.getSimpleName();
    private MapMarker currentLocationMarker; // Marker cho vị trí hiện tại

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeHERESDK();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = view.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        handlePermissions();
        return view;
    }

    private void initializeHERESDK() {

        String accessKeyID = "";
        String accessKeySecret = "";

        SDKOptions options = new SDKOptions(accessKeyID, accessKeySecret);

        try {
            // Kiểm tra kết nối và khởi tạo HERE SDK
            Context context = getContext();
            SDKNativeEngine.makeSharedInstance(context, options);
            Log.d(TAG, "HERE SDK Initialized successfully.");
        } catch (InstantiationErrorException e) {
            // Log chi tiết lỗi để giúp phát hiện nguyên nhân
            Log.e(TAG, "Initialization of HERE SDK failed: " + e.error.name() + ", " + e.getMessage());
            e.printStackTrace(); // In ra chi tiết stack trace
        }
    }


    private void handlePermissions() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            requestCurrentLocation();  // yêu cầu vị tri trc
            loadMapScene();
        }
    }

    private void loadMapScene() {
        MapMeasure mapMeasureZoom = new MapMeasure(MapMeasure.Kind.DISTANCE, 1000 * 10);
        mapView.getMapScene().loadScene(MapScheme.NORMAL_DAY, new MapScene.LoadSceneCallback() {
            @Override
            public void onLoadScene(@Nullable MapError mapError) {
                if (mapError != null) {
                    Log.d(TAG, "Loading map failed: mapError: " + mapError.name());
                }
            }
        });
    }

    private void requestCurrentLocation() {
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                updateMapLocation(new GeoCoordinates(location.getLatitude(), location.getLongitude()));
            }
        });
    }

    private void updateMapLocation(GeoCoordinates geoCoordinates) {
        double distanceInMeters = 1000; // Đặt mức zoom
        MapMeasure mapMeasureZoom = new MapMeasure(MapMeasure.Kind.DISTANCE, distanceInMeters);

        // Di chuyển camera đến vị trí được chỉ định
        mapView.getCamera().lookAt(geoCoordinates, mapMeasureZoom);

        // Xóa marker cũ (nếu có)
        if (currentLocationMarker != null) {
            mapView.getMapScene().removeMapMarker(currentLocationMarker);
        }

        // Tạo một marker mới cho vị trí
        MapImage mapImage = MapImageFactory.fromResource(this.getResources(), android.R.drawable.ic_menu_mylocation);
        currentLocationMarker = new MapMarker(geoCoordinates, mapImage);

        // Thêm marker vào bản đồ
        mapView.getMapScene().addMapMarker(currentLocationMarker);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadMapScene();
            requestCurrentLocation();
        } else {
            Log.e(TAG, "Location permissions denied by user.");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    private void disposeHERESDK() {
        SDKNativeEngine sdkNativeEngine = SDKNativeEngine.getSharedInstance();
        if (sdkNativeEngine != null) {
            sdkNativeEngine.dispose();
            SDKNativeEngine.setSharedInstance(null);
        }
    }
}