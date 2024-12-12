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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;
import com.here.sdk.core.Color;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolyline;
import com.here.sdk.core.engine.SDKNativeEngine;
import com.here.sdk.core.engine.SDKOptions;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapview.LineCap;
import com.here.sdk.mapview.MapError;
import com.here.sdk.mapview.MapImage;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapMeasure;
import com.here.sdk.mapview.MapMeasureDependentRenderSize;
import com.here.sdk.mapview.MapPolyline;
import com.here.sdk.mapview.MapScheme;
import com.here.sdk.mapview.MapView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView;
import java.util.ArrayList;
import java.util.List;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.here.sdk.mapview.RenderSize;
import com.here.sdk.routing.CarOptions;
import com.here.sdk.routing.DynamicSpeedInfo;
import com.here.sdk.routing.Maneuver;
import com.here.sdk.routing.ManeuverAction;
import com.here.sdk.routing.PaymentMethod;
import com.here.sdk.routing.Route;
import com.here.sdk.routing.RouteOptions;
import com.here.sdk.routing.RouteRailwayCrossing;
import com.here.sdk.routing.RoutingEngine;
import com.here.sdk.routing.Section;
import com.here.sdk.routing.Span;
import com.here.sdk.routing.Toll;
import com.here.sdk.routing.TollFare;
import com.here.sdk.routing.TrafficOptimizationMode;
import com.here.sdk.routing.Waypoint;
import com.here.sdk.transport.TransportMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    private static final String TAG = MapFragment.class.getSimpleName();
    private static final String SEARCH_API_KEY = "aSKVjwqkRsPEW7aN0w5sBq9yf2_KkM8eZV1mACAHrgc";
    private ListView searchResultsList;
    private List<String> searchResultsTitles = new ArrayList<>();
    private List<GeoCoordinates> searchResultsCoordinates = new ArrayList<>();

    private MapView mapView;
    private LocationManager locationManager;
    private MapMarker currentLocationMarker;
    private FusedLocationProviderClient fusedLocationClient;
    private List<MapMarker> searchMarkers = new ArrayList<>();
    private GeoCoordinates currentLocation = null; // Tọa độ hiện tại
    private RoutingEngine routingEngine;
    private GeoCoordinates destinationLocation;
    private boolean trafficDisabled;
    private final List<MapPolyline> mapPolylines = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeHERESDK();
        initializeRoutingEngine();
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        mapView = view.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        EditText locationSearch = view.findViewById(R.id.location_search);
        Button searchButton = view.findViewById(R.id.search_button);
        searchResultsList = view.findViewById(R.id.search_results_list);

        searchButton.setOnClickListener(v -> {
            String query = locationSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                performSearch(query);
            } else {
                Toast.makeText(getContext(), "Please enter a location to search.", Toast.LENGTH_SHORT).show();
            }
        });

        searchResultsList.setOnItemClickListener((parent, view1, position, id) -> {
            GeoCoordinates selectedCoordinates = searchResultsCoordinates.get(position);
            updateMapLocation(selectedCoordinates);
            searchResultsList.setVisibility(View.GONE);
            clearPolylines();
            clearSearchMarkers();
            calculateRoute(currentLocation, selectedCoordinates);
        });

        handlePermissions();
        setupMapGestures();
        return view;
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
                // Chỉ di chuyển camera đến vị trí hiện tại
                requestCurrentLocation();
                // Hiển thị pothole sau khi đã di chuyển camera
                fetchAndDisplayPotholes();
            }
        });
    }



    private void performSearch(String query) {
        new Thread(() -> {
            try {
                // Clear old markers and polylines
                getActivity().runOnUiThread(() -> {
                    clearSearchMarkers();
                    clearPolylines();
                });

                double Lat = currentLocation.latitude;
                double Lng = currentLocation.longitude;
                String encodedQuery = URLEncoder.encode(query, "UTF-8");
                String urlString = String.format(
                        java.util.Locale.US,
                        "https://discover.search.hereapi.com/v1/discover?apikey=%s&q=%s&at=%f,%f",
                        SEARCH_API_KEY, encodedQuery, Lat, Lng
                );

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    parseSearchResults(response.toString());
                } else {
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    errorReader.close();
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

            searchResultsTitles.clear();
            searchResultsCoordinates.clear();

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                JSONObject position = item.getJSONObject("position");
                double lat = position.getDouble("lat");
                double lng = position.getDouble("lng");
                String title = item.getString("title");

                GeoCoordinates geoCoordinates = new GeoCoordinates(lat, lng);
                searchResultsTitles.add(title);
                searchResultsCoordinates.add(geoCoordinates);
                getActivity().runOnUiThread(() -> addSearchMarker(geoCoordinates, title));
            }

            getActivity().runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, searchResultsTitles);
                searchResultsList.setAdapter(adapter);
                searchResultsList.setVisibility(View.VISIBLE);
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing search results: " + e.getMessage());
            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error parsing search results.", Toast.LENGTH_SHORT).show());
        }
    }

    private void clearPolylines() {
        for (MapPolyline polyline : mapPolylines) {
            mapView.getMapScene().removeMapPolyline(polyline);
        }
        mapPolylines.clear();
    }


    private void clearSearchMarkers() {
        for (MapMarker marker : searchMarkers) {
            mapView.getMapScene().removeMapMarker(marker);
        }
        searchMarkers.clear();
    }

    private void addSearchMarker(GeoCoordinates geoCoordinates, String title) {
        MapImage markerImage = MapImageFactory.fromResource(getResources(), R.drawable.ic_current_location);
        MapMarker mapMarker = new MapMarker(geoCoordinates, markerImage);
        searchMarkers.add(mapMarker);
        mapView.getMapScene().addMapMarker(mapMarker);
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

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLocation = new GeoCoordinates(location.getLatitude(), location.getLongitude());
                        moveCameraToCurrentLocation();
                    } else {
                        showToast("Unable to retrieve current location.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching location: " + e.getMessage());
                    showToast("Error fetching location.");
                });

        // Lấy vị trí GPS mới nhất
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location != null) {
            // Di chuyển camera tới vị trí hiện tại
            currentLocation = new GeoCoordinates(location.getLatitude(), location.getLongitude());
            moveCameraToCurrentLocation();
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    // Di chuyển camera tới vị trí hiện tại
                    currentLocation = new GeoCoordinates(location.getLatitude(), location.getLongitude());
                    moveCameraToCurrentLocation();
                    locationManager.removeUpdates(this); // Dừng lắng nghe
                }
            });
        }
    }

    private void moveCameraToCurrentLocation() {
        if (currentLocation != null && mapView != null) {
            MapMeasure mapMeasure = new MapMeasure(MapMeasure.Kind.DISTANCE, 1000); // Zoom level
            mapView.getCamera().lookAt(currentLocation, mapMeasure);

            // Thêm marker cho vị trí hiện tại
            MapImage markerImage = MapImageFactory
                    .fromResource(getResources(), R.drawable.ic_current_location);
            if (currentLocationMarker != null) {
                mapView.getMapScene().removeMapMarker(currentLocationMarker);
            }
            currentLocationMarker = new MapMarker(currentLocation, markerImage);
            mapView.getMapScene().addMapMarker(currentLocationMarker);
        }
    }


    private void updateMapLocation(GeoCoordinates geoCoordinates) {
        if (geoCoordinates == null || mapView == null) {
            return;
        }

        MapMeasure mapMeasure = new MapMeasure(MapMeasure.Kind.DISTANCE, 1000);
        mapView.getCamera().lookAt(geoCoordinates, mapMeasure);

        MapImage markerImage = MapImageFactory.fromResource(getResources(), R.drawable.ic_current_location);
        MapMarker currentLocationMarker = new MapMarker(geoCoordinates, markerImage);
        mapView.getMapScene().addMapMarker(currentLocationMarker);
    }

    private void fetchAndDisplayPotholes() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("potholes");
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    Pothole pothole = data.getValue(Pothole.class);
                    if (pothole != null) {
                        GeoCoordinates coordinates = new GeoCoordinates(pothole.getLatitude(), pothole.getLongitude());
                        addPotholeMarker(coordinates, pothole.getLevel());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Error fetching pothole data.");
            }
        });
    }

    private void addPotholeMarker(GeoCoordinates geoCoordinates, String level) {
        MapImage markerImage = MapImageFactory.fromResource(getResources(), R.drawable.ic_warning);
        MapMarker mapMarker = new MapMarker(geoCoordinates, markerImage);
        mapView.getMapScene().addMapMarker(mapMarker);
    }

    private void initializeRoutingEngine() {
        try {
            routingEngine = new RoutingEngine();
        } catch (InstantiationErrorException e) {
            Log.e(TAG, "Error initializing RoutingEngine: " + e.getMessage());
        }
    }

    private void setupMapGestures() {
        mapView.getGestures().setTapListener(touchPoint -> {
            GeoCoordinates tappedCoordinates = mapView.viewToGeoCoordinates(touchPoint);
            if (tappedCoordinates != null) {
                destinationLocation = tappedCoordinates;
                showToast("Destination set: " + tappedCoordinates.latitude + ", " + tappedCoordinates.longitude);
                addDestinationMarker(tappedCoordinates); // Thêm marker tại điểm đích
            }
            if (currentLocation != null) {
                calculateRoute(currentLocation, destinationLocation);
            } else {
                showToast("Current location is not available.");
            }
        });
    }

    private void addDestinationMarker(GeoCoordinates coordinates) {
        MapImage markerImage = MapImageFactory.fromResource(getResources(), R.drawable.ic_current_location);
        MapMarker destinationMarker = new MapMarker(coordinates, markerImage);
        mapView.getMapScene().addMapMarker(destinationMarker);
    }

    private void logRouteSectionDetails(Route route) {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");

        for (int i = 0; i < route.getSections().size(); i++) {
            Section section = route.getSections().get(i);

            Log.d(TAG, "Route Section : " + (i + 1));
            Log.d(TAG, "Route Section Departure Time : "
                    + dateFormat.format(section.getDepartureLocationTime().localTime));
            Log.d(TAG, "Route Section Arrival Time : "
                    + dateFormat.format(section.getArrivalLocationTime().localTime));
            Log.d(TAG, "Route Section length : " + section.getLengthInMeters() + " m");
            Log.d(TAG, "Route Section duration : " + section.getDuration().getSeconds() + " s");
        }
    }

    private void logRouteRailwayCrossingDetails(Route route) {
        for (RouteRailwayCrossing routeRailwayCrossing : route.getRailwayCrossings()) {
            GeoCoordinates routeOffsetCoordinates = routeRailwayCrossing.coordinates;
            int routeOffsetSectionIndex = routeRailwayCrossing.routeOffset.sectionIndex;
            double routeOffsetInMeters = routeRailwayCrossing.routeOffset.offsetInMeters;

            Log.d(TAG, "A railway crossing of type " + routeRailwayCrossing.type.name() +
                    "is situated " +
                    routeOffsetInMeters + " m away from start of section: " +
                    routeOffsetSectionIndex);
        }
    }

    private void logTollDetails(Route route) {
        for (Section section : route.getSections()) {
            List<Span> spans = section.getSpans();
            List<Toll> tolls = section.getTolls();
            if (!tolls.isEmpty()) {
                Log.d(TAG, "Attention: This route may require tolls to be paid.");
            }
            for (Toll toll : tolls) {
                Log.d(TAG, "Toll information valid for this list of spans:");
                Log.d(TAG, "Toll system: " + toll.tollSystem);
                Log.d(TAG, "Toll country code (ISO-3166-1 alpha-3): " + toll.countryCode);
                Log.d(TAG, "Toll fare information: ");
                for (TollFare tollFare : toll.fares) {
                    Log.d(TAG, "Toll price: " + tollFare.price + " " + tollFare.currency);
                    for (PaymentMethod paymentMethod : tollFare.paymentMethods) {
                        Log.d(TAG, "Accepted payment methods for this price: " + paymentMethod.name());
                    }
                }
            }
        }
    }

    private void showRouteDetails(Route route) {
        long estimatedTravelTimeInSeconds = route.getDuration().getSeconds();
        long estimatedTrafficDelayInSeconds = route.getTrafficDelay().getSeconds();
        int lengthInMeters = route.getLengthInMeters();

    }

    private void showRouteOnMap(Route route) {
        GeoPolyline routeGeoPolyline = route.getGeometry();
        float widthInPixels = 20;
        Color polylineColor = new Color(0, (float) 0.56, (float) 0.54, (float) 0.63);
        MapPolyline routeMapPolyline = null;
        try {
            routeMapPolyline = new MapPolyline(routeGeoPolyline, new MapPolyline.SolidRepresentation(
                    new MapMeasureDependentRenderSize(RenderSize.Unit.PIXELS, widthInPixels),
                    polylineColor,
                    LineCap.ROUND));
        } catch (MapPolyline.Representation.InstantiationException e) {
            Log.e("MapPolyline Representation Exception:", e.error.name());
        } catch (MapMeasureDependentRenderSize.InstantiationException e) {
            Log.e("MapMeasureDependentRenderSize Exception:", e.error.name());
        }
        mapView.getMapScene().addMapPolyline(routeMapPolyline);
        mapPolylines.add(routeMapPolyline);
        List<Section> sections = route.getSections();
        for (Section section : sections) {
            logManeuverInstructions(section);
        }
    }

    private void showWaypointsOnMap(List<Waypoint> waypoints) {
        int n = waypoints.size();
        for (int i = 0; i < n; i++) {
            GeoCoordinates currentGeoCoordinates = waypoints.get(i).coordinates;
            if (i == 0) {
                addSearchMarker(currentGeoCoordinates, String.valueOf(R.drawable.ic_current_location));
            } else {
                if(i == n-1){
                    addSearchMarker(currentGeoCoordinates, String.valueOf(R.drawable.ic_current_location));
                }else{}
            }
        }
    }

    private void logManeuverInstructions(Section section) {
        Log.d(TAG, "Log maneuver instructions per route section:");
        List<Maneuver> maneuverInstructions = section.getManeuvers();
        for (Maneuver maneuverInstruction : maneuverInstructions) {
            ManeuverAction maneuverAction = maneuverInstruction.getAction();
            GeoCoordinates maneuverLocation = maneuverInstruction.getCoordinates();
            String maneuverInfo = maneuverInstruction.getText()
                    + ", Action: " + maneuverAction.name()
                    + ", Location: " + maneuverLocation.toString();
            Log.d(TAG, maneuverInfo);
        }
    }
    private CarOptions getCarOptions() {
        CarOptions carOptions = new CarOptions();
        carOptions.routeOptions.enableTolls = true;
        carOptions.routeOptions.trafficOptimizationMode = trafficDisabled ?
                TrafficOptimizationMode.DISABLED :
                TrafficOptimizationMode.TIME_DEPENDENT;
        return carOptions;
    }

    private void calculateRoute(GeoCoordinates start, GeoCoordinates destination) {
        if (routingEngine == null) {
            showToast("Routing engine is not initialized.");
            return;
        }

        List<Waypoint> waypoints = new ArrayList<>();
        waypoints.add(new Waypoint(start));
        waypoints.add(new Waypoint(destination));

        routingEngine.calculateRoute(
                waypoints,
                getCarOptions(),
                (routingError, routes) -> {
                    if (routingError == null) {
                        Route route = routes.get(0);
                        showRouteDetails(route);
                        showRouteOnMap(route);
                        logRouteRailwayCrossingDetails(route);
                        logRouteSectionDetails(route);
                        logTollDetails(route);
                        showWaypointsOnMap(waypoints);
                    } else {
                        showToast("No route found.");
                    }
                }
        );;
    }


}
