package com.example.detectapplication2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Pair;
import android.widget.Toast;
import android.Manifest;


import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AccelerometerListener implements SensorEventListener {
    private Context context;
    private static final double THRESHOLD_LIGHT = 12.0;
    private static final double THRESHOLD_MEDIUM = 20.0;

    public AccelerometerListener(Context context) {
        this.context = context;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];
        double magnitude = Math.sqrt(x * x + y * y + z * z);

        if (magnitude > THRESHOLD_LIGHT) {
            showConfirmationDialog(magnitude);
            Toast.makeText(context, "Pothole detected!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    private void showConfirmationDialog(double magnitude) {
        String level;
        if (magnitude < THRESHOLD_MEDIUM) {
            level = "light";
        } else if (magnitude < 30.0) {
            level = "medium";
        } else {
            level = "heavy";
        }

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            String address = "Unknown Address";
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (!addresses.isEmpty()) {
                    address = addresses.get(0).getAddressLine(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Show dialog for confirmation
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Pothole Detected");
            builder.setMessage("Confirm pothole at:\n" + address);

            String finalAddress = address;
            builder.setPositiveButton("Confirm", (dialog, which) -> savePotholeToDatabase(latitude, longitude, finalAddress, level));
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void savePotholeToDatabase(double latitude, double longitude, String address, String level) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("potholes");

        String userId = auth.getUid();
        if (userId != null) {
            Pothole pothole = new Pothole(latitude, longitude, address, userId, level);
            database.push().setValue(pothole);
        }
    }
}
