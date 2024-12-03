package com.example.detectapplication2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.engine.SDKNativeEngine;
import com.here.sdk.core.engine.SDKOptions;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapview.MapError;
import com.here.sdk.mapview.MapImage;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapMeasure;
import com.here.sdk.mapview.MapScheme;
import com.here.sdk.mapview.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    private static final String TAG = MapFragment.class.getSimpleName();
    private static final String SEARCH_API_KEY = "aSKVjwqkRsPEW7aN0w5sBq9yf2_KkM8eZV1mACAHrgc";

    private MapView mapView;
    private LocationManager locationManager;
    private MapMarker currentLocationMarker;
    private List<MapMarker> searchMarkers = new ArrayList<>();
    private GeoCoordinates currentLocation = null; // Tọa độ hiện tại

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeHERESDK();
    }

    private void initializeHERESDK() {
        String accessKeyId = "onftyHtr9vqHq4oWyTbpUQ";
        String accessKeySecret = "Zs_127UqiZjCL0kVK90MhUaduDhv8NArb-D7ImMPj-J4csuO0gpsjZMPWskUSzBkURBcsxE6alKNq3fkSaeTxg";

        SDKOptions options = new SDKOptions(accessKeyId, accessKeySecret);

        try {
            SDKNativeEngine.makeSharedInstance(getContext(), options);
            Log.d(TAG, "HERE SDK initialized successfully.");
        } catch (InstantiationErrorException e) {
            Log.e(TAG, "HERE SDK initialization failed: " + e.getMessage());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = view.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        EditText locationSearch = view.findViewById(R.id.location_search);
        Button searchButton = view.findViewById(R.id.search_button);

        searchButton.setOnClickListener(v -> {
            String query = locationSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                performSearch(query);
            } else {
                Toast.makeText(getContext(), "Please enter a location to search.", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý quyền và tải bản đồ
        handlePermissions();
        return view;
    }


    private void performSearch(String query) {
        new Thread(() -> {
            try {
                // Tọa độ mặc định (Hà Nội)

                double Lat = currentLocation.latitude;
                double Lng = currentLocation.longitude;

                // Mã hóa query
                String encodedQuery = URLEncoder.encode(query, "UTF-8");

                // Định dạng URL theo Locale.US để đảm bảo dấu thập phân dùng dấu chấm (.)
                String urlString = String.format(
                        java.util.Locale.US,
                        "https://discover.search.hereapi.com/v1/discover?apikey=%s&q=%s&at=%f,%f",
                        SEARCH_API_KEY, encodedQuery, Lat, Lng
                );

                Log.d(TAG, "Search API Full URL: " + urlString);

                // Kết nối HTTP
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // Kiểm tra mã phản hồi
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    // Đọc dữ liệu trả về
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    parseSearchResults(response.toString());
                } else {
                    // Đọc lỗi từ API
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    errorReader.close();

                    // Log lỗi và hiển thị thông báo
                    Log.e(TAG, "Error Response: " + errorResponse);
                    showToast("Search API error: " + responseCode + " - " + errorResponse);
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception during search: " + e.getMessage());
                showToast("Search failed: " + e.getMessage());
            }
        }).start();
    }


    private void showToast(String message) {
        // Kiểm tra xem Fragment đã được liên kết với Activity chưa
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
        }
    }
    private void parseSearchResults(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray items = jsonObject.getJSONArray("items");

            getActivity().runOnUiThread(this::clearSearchMarkers);

            double minDistance = Double.MAX_VALUE;
            GeoCoordinates bestMatchCoordinates = null;

            if (currentLocation == null) {
                showToast("Current location is not available.");
                return;
            }

            double currentLat = currentLocation.latitude;
            double currentLng = currentLocation.longitude;

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                JSONObject position = item.getJSONObject("position");
                double lat = position.getDouble("lat");
                double lng = position.getDouble("lng");
                String title = item.getString("title");

                GeoCoordinates geoCoordinates = new GeoCoordinates(lat, lng);
                getActivity().runOnUiThread(() -> addSearchMarker(geoCoordinates, title));

                // Tính khoảng cách từ tọa độ hiện tại
                double distance = calculateDistance(currentLat, currentLng, lat, lng);

                if (distance < minDistance) {
                    minDistance = distance;
                    bestMatchCoordinates = geoCoordinates;
                }
            }

            if (bestMatchCoordinates != null) {
                GeoCoordinates finalBestMatchCoordinates = bestMatchCoordinates;
                getActivity().runOnUiThread(() -> {
                    updateMapLocation(finalBestMatchCoordinates);
                    Toast.makeText(getContext(), "Displaying closest match on map.", Toast.LENGTH_SHORT).show();
                });
            }

            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Found " + items.length() + " results.", Toast.LENGTH_SHORT).show());
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing search results: " + e.getMessage());
            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error parsing search results.", Toast.LENGTH_SHORT).show());
        }
    }



    private void clearSearchMarkers() {
        for (MapMarker marker : searchMarkers) {
            mapView.getMapScene().removeMapMarker(marker);
        }
        searchMarkers.clear();
    }

    private void addSearchMarker(GeoCoordinates geoCoordinates, String title) {
        MapImage markerImage = MapImageFactory.fromResource(getResources(), android.R.drawable.ic_menu_mylocation);
        MapMarker mapMarker = new MapMarker(geoCoordinates, markerImage);
        searchMarkers.add(mapMarker);
        mapView.getMapScene().addMapMarker(mapMarker);
    }

    private void handlePermissions() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            loadMapScene();
            requestCurrentLocation(); // Lấy vị trí hiện tại và cập nhật bản đồ
        }
    }
    private void loadMapScene() {
        mapView.getMapScene().loadScene(MapScheme.NORMAL_DAY, mapError -> {
            if (mapError != null) {
                Log.e(TAG, "Failed to load map scene: " + mapError.name());
            } else {
                Log.d(TAG, "Map scene loaded successfully.");
            }
        });
    }
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Bán kính Trái Đất (kilometer)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Khoảng cách theo km
    }

    private void requestCurrentLocation() {
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Lấy vị trí GPS mới nhất
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location != null) {
            // Cập nhật tọa độ hiện tại và bản đồ
            currentLocation = new GeoCoordinates(location.getLatitude(), location.getLongitude());
            updateMapLocation(currentLocation);
        } else {
            // Lắng nghe các thay đổi vị trí trong trường hợp không có vị trí trước đó
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    currentLocation = new GeoCoordinates(location.getLatitude(), location.getLongitude());
                    updateMapLocation(currentLocation);
                    locationManager.removeUpdates(this); // Dừng lắng nghe sau khi nhận được vị trí
                }
            });
        }
    }



    private void updateMapLocation(GeoCoordinates geoCoordinates) {
        MapMeasure mapMeasure = new MapMeasure(MapMeasure.Kind.DISTANCE, 1000);

        // Di chuyển camera bản đồ tới vị trí hiện tại
        mapView.getCamera().lookAt(geoCoordinates, mapMeasure);

        // Xóa marker cũ nếu có
        if (currentLocationMarker != null) {
            mapView.getMapScene().removeMapMarker(currentLocationMarker);
        }

        // Thêm marker tại vị trí hiện tại
        MapImage markerImage = MapImageFactory
                .fromResource(getResources(), android.R.drawable.ic_menu_mylocation);
        currentLocationMarker = new MapMarker(geoCoordinates, markerImage);
        mapView.getMapScene().addMapMarker(currentLocationMarker);
    }
}
