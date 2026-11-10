package com.ifsc.app;


import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    LocationManager locationManager;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        textView = findViewById(R.id.textView);
        MapView mapView = findViewById(R.id.mapView);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        org.osmdroid.config.Configuration.getInstance().load(this, getPreferences(Context.MODE_PRIVATE));

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location l = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            textView.setText(l.getLatitude() + " " + l.getLongitude());
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, location -> {
                textView.setText(location.getLatitude() + " " + location.getLongitude());
                mapView.getController().setZoom(18);
                mapView.getController().setCenter(new org.osmdroid.util.GeoPoint(location.getLatitude(), location.getLongitude()));

                mapView.getOverlays().clear();
                Marker marker = new org.osmdroid.views.overlay.Marker(mapView);
                marker.setPosition(new org.osmdroid.util.GeoPoint(location.getLatitude(), location.getLongitude()));

                mapView.invalidate();

            });
        } else {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                }
            }
        }
    }
}