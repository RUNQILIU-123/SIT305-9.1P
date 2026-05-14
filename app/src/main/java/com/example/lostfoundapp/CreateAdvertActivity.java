package com.example.lostfoundapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateAdvertActivity extends AppCompatActivity {

    private static final String TAG = "CreateAdvertFix";
    private RadioGroup rgType;
    private EditText etName, etContactName, etPhone, etDescription, etLocation;
    private Spinner spCategory;
    private ImageView ivPreview;
    private Uri savedImageUri = null;
    private DatabaseHelper dbHelper;
    private double currentLatitude = 0;
    private double currentLongitude = 0;

    private FusedLocationProviderClient fusedLocationClient;

    private final String[] categories = {"Electronics", "Pets", "Wallets", "Keys", "Bags", "Other"};

    // Image picker launcher
    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        InputStream is = getContentResolver().openInputStream(uri);
                        if (is != null) {
                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                            File file = new File(getFilesDir(), "IMG_" + timeStamp + ".jpg");
                            
                            FileOutputStream fos = new FileOutputStream(file);
                            byte[] buffer = new byte[8192];
                            int read;
                            while ((read = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, read);
                            }
                            fos.close();
                            is.close();

                            savedImageUri = Uri.fromFile(file);
                            showImagePreview(file.getAbsolutePath());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Critical error picking image", e);
                        Toast.makeText(this, "Error processing image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

    // Places Autocomplete launcher
    private final ActivityResultLauncher<Intent> autocompleteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    Log.d(TAG, "Autocomplete selected place: " + place.getName() + ", Address: " + place.getAddress());
                    etLocation.setText(place.getAddress());
                    if (place.getLatLng() != null) {
                        currentLatitude = place.getLatLng().latitude;
                        currentLongitude = place.getLatLng().longitude;
                        Log.d(TAG, "Autocomplete coordinates: Lat=" + currentLatitude + ", Lon=" + currentLongitude);
                    }
                } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
                    Status status = Autocomplete.getStatusFromIntent(result.getData());
                    Log.e(TAG, "Autocomplete Error: " + status.getStatusMessage());
                    Toast.makeText(this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_advert);

        dbHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Places SDK
        String apiKey = getString(R.string.google_maps_key);
        Log.d(TAG, "Is API Key empty: " + (apiKey == null || apiKey.isEmpty() || apiKey.contains("YOUR_GOOGLE_MAPS_API_KEY_HERE")));
        
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
            Log.d(TAG, "Places initialized: " + Places.isInitialized());
        }
        
        rgType = findViewById(R.id.rgType);
        etName = findViewById(R.id.etName);
        etContactName = findViewById(R.id.etContactName);
        etPhone = findViewById(R.id.etPhone);
        etDescription = findViewById(R.id.etDescription);
        etLocation = findViewById(R.id.etLocation);
        spCategory = findViewById(R.id.spCategory);
        ivPreview = findViewById(R.id.ivPreview);
        Button btnUploadImage = findViewById(R.id.btnUploadImage);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnGetCurrentLocation = findViewById(R.id.btnGetCurrentLocation);
        Button btnChooseLocation = findViewById(R.id.btnChooseLocation);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        btnUploadImage.setOnClickListener(v -> mGetContent.launch("image/*"));
        btnSave.setOnClickListener(v -> saveAdvert());
        
        btnGetCurrentLocation.setOnClickListener(v -> getCurrentLocation());

        // Launch Autocomplete when "Choose Location" is clicked
        btnChooseLocation.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .build(this);
            autocompleteLauncher.launch(intent);
        });

        // Clear coordinates if user starts typing manually to trigger Geocoder fallback later
        etLocation.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                currentLatitude = 0;
                currentLongitude = 0;
            }
        });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
                updateLocationText(currentLatitude, currentLongitude);
            } else {
                Toast.makeText(this, "Could not get current location. Make sure GPS is on.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLocationText(double lat, double lon) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);
                etLocation.setText(address);
            } else {
                etLocation.setText(String.format(Locale.getDefault(), "Lat: %.4f, Lon: %.4f", lat, lon));
            }
        } catch (Exception e) {
            Log.e(TAG, "Geocoder error", e);
            etLocation.setText(String.format(Locale.getDefault(), "Lat: %.4f, Lon: %.4f", lat, lon));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }

    private void showImagePreview(String path) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            if (bitmap != null) {
                ivPreview.setImageBitmap(bitmap);
                ivPreview.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Preview error", e);
        }
    }

    private void saveAdvert() {
        String name = etName.getText().toString().trim();
        String contact = etContactName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String loc = etLocation.getText().toString().trim();

        if (name.isEmpty() || contact.isEmpty() || phone.isEmpty() || desc.isEmpty() || loc.isEmpty()) {
            Toast.makeText(this, "Please fill in all text fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (savedImageUri == null) {
            Toast.makeText(this, "Please upload an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Geocoder Fallback: If no coordinates from Autocomplete or Current Location, resolve the typed string
        if (currentLatitude == 0 && currentLongitude == 0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocationName(loc, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    currentLatitude = addresses.get(0).getLatitude();
                    currentLongitude = addresses.get(0).getLongitude();
                    Log.d(TAG, "Manual Geocoder result: Lat=" + currentLatitude + ", Lon=" + currentLongitude);
                } else {
                    Toast.makeText(this, "Please enter a valid address or use Get Current Location.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "Geocoding fallback failed", e);
                Toast.makeText(this, "Location error. Please use a clearer address or current location.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        int selectedId = rgType.getCheckedRadioButtonId();
        RadioButton rb = findViewById(selectedId);
        String type = rb.getText().toString();
        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        LostFoundItem item = new LostFoundItem(0, type, name, contact, phone, desc, 
                                            spCategory.getSelectedItem().toString(), loc, 
                                            savedImageUri.toString(), ts, currentLatitude, currentLongitude);

        if (dbHelper.insertItem(item) != -1) {
            Toast.makeText(this, "Advert saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show();
        }
    }
}
