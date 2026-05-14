package com.example.lostfoundapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";
    private GoogleMap mMap;
    private DatabaseHelper dbHelper;
    private Spinner spRadius;
    private List<LostFoundItem> allItems;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentUserLocation;
    private final Map<Marker, Integer> markerItemIds = new HashMap<>();

    private final String[] radiusOptions = {"All", "1 km", "5 km", "10 km", "20 km"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        dbHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        spRadius = findViewById(R.id.spRadius);
        Button btnSearch = findViewById(R.id.btnSearch);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, radiusOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRadius.setAdapter(adapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnSearch.setOnClickListener(v -> searchWithRadius());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnInfoWindowClickListener(marker -> {
            Integer itemId = markerItemIds.get(marker);
            if (itemId != null) {
                Intent intent = new Intent(MapActivity.this, ItemDetailActivity.class);
                for (LostFoundItem item : allItems) {
                    if (item.getId() == itemId) {
                        intent.putExtra("selected_item", item);
                        startActivity(intent);
                        break;
                    }
                }
            }
        });

        loadItemsAndShowMarkers(0);
    }

    private void loadItemsAndShowMarkers(double radiusKm) {
        mMap.clear();
        markerItemIds.clear();
        allItems = dbHelper.getAllItems("All");
        
        Log.d(TAG, "Number of items loaded from SQLite: " + allItems.size());

        List<Marker> markers = new ArrayList<>();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        for (LostFoundItem item : allItems) {
            double lat = item.getLatitude();
            double lon = item.getLongitude();

            if (lat == 0 && lon == 0 && item.getLocation() != null && !item.getLocation().isEmpty()) {
                try {
                    List<Address> addresses = geocoder.getFromLocationName(item.getLocation(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        lat = addresses.get(0).getLatitude();
                        lon = addresses.get(0).getLongitude();
                        item.setLatitude(lat);
                        item.setLongitude(lon);
                        dbHelper.updateItem(item);
                        Log.d(TAG, "Geocoded old item ID: " + item.getId() + " to " + lat + ", " + lon);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Geocoding failed for item ID: " + item.getId(), e);
                }
            }

            if (lat != 0 || lon != 0) {
                Log.d(TAG, String.format("Item ID: %d, Title: %s, Location: %s, Lat: %f, Lon: %f", 
                        item.getId(), item.getName(), item.getLocation(), lat, lon));

                boolean shouldShow = true;
                if (radiusKm > 0 && currentUserLocation != null) {
                    float[] results = new float[1];
                    Location.distanceBetween(currentUserLocation.getLatitude(), currentUserLocation.getLongitude(),
                            lat, lon, results);
                    float distanceKm = results[0] / 1000f;
                    if (distanceKm > radiusKm) {
                        shouldShow = false;
                    }
                    Log.d(TAG, "Item ID: " + item.getId() + " distance from current user: " + distanceKm + " km");
                }

                if (shouldShow) {
                    LatLng pos = new LatLng(lat, lon);
                    float color = item.getType().equalsIgnoreCase("Lost") ? 
                            BitmapDescriptorFactory.HUE_RED : BitmapDescriptorFactory.HUE_AZURE;

                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(pos)
                            .title(item.getType() + " - " + item.getName())
                            .snippet("Category: " + item.getCategory() + " | Posted: " + item.getTimestamp())
                            .icon(BitmapDescriptorFactory.defaultMarker(color)));
                    
                    if (marker != null) {
                        markerItemIds.put(marker, item.getId());
                        markers.add(marker);
                    }
                }
            }
        }

        Log.d(TAG, "Number of markers added to map: " + markers.size());

        if (markers.isEmpty()) {
            Toast.makeText(this, "No items found for the selected radius.", Toast.LENGTH_SHORT).show();
        } else {
            if (markers.size() == 1) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markers.get(0).getPosition(), 12));
            } else {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker m : markers) {
                    builder.include(m.getPosition());
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
            }
        }
    }

    private void searchWithRadius() {
        String selected = spRadius.getSelectedItem().toString();
        Log.d(TAG, "SEARCH clicked. Selected radius: " + selected);

        if (selected.equals("All")) {
            currentUserLocation = null; // Radius All doesn't need current location
            loadItemsAndShowMarkers(0);
        } else {
            double radius = Double.parseDouble(selected.replace(" km", ""));
            getCurrentLocationAndFilter(radius);
        }
    }

    private void getCurrentLocationAndFilter(double radiusKm) {
        boolean hasPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        Log.d(TAG, "Location permission granted: " + hasPermission);

        if (!hasPermission) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
            return;
        }

        Log.d(TAG, "Attempting to get location for radius: " + radiusKm + " km");

        // Try getting last location first (fast)
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                Log.d(TAG, "getLastLocation success: " + location.getLatitude() + ", " + location.getLongitude());
                currentUserLocation = location;
                loadItemsAndShowMarkers(radiusKm);
            } else {
                Log.d(TAG, "getLastLocation returned null. Attempting getCurrentLocation...");
                // If last location is null, request current location (more accurate but slower)
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener(this, new LocationSuccessListener(radiusKm));
            }
        });
    }

    // Helper class to handle the async location result
    private class LocationSuccessListener implements OnSuccessListener<Location> {
        private final double radiusKm;

        public LocationSuccessListener(double radiusKm) {
            this.radiusKm = radiusKm;
        }

        @Override
        public void onSuccess(Location location) {
            if (location != null) {
                Log.d(TAG, "getCurrentLocation success: " + location.getLatitude() + ", " + location.getLongitude());
                currentUserLocation = location;
                loadItemsAndShowMarkers(radiusKm);
            } else {
                Log.d(TAG, "Current location is still null.");
                Toast.makeText(MapActivity.this, "Current location is unavailable. Please enable location in the emulator or choose All.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission granted by user.");
            searchWithRadius();
        } else {
            Log.d(TAG, "Permission denied by user.");
            Toast.makeText(this, "Location permission is required for radius search.", Toast.LENGTH_SHORT).show();
        }
    }
}
