package com.example.lostfoundapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

/**
 * Detailed view of an item. Includes memory-safe image loading to prevent crashes.
 */
public class ItemDetailActivity extends AppCompatActivity {

    private static final String TAG = "ItemDetailActivity";
    private DatabaseHelper dbHelper;
    private LostFoundItem currentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        dbHelper = new DatabaseHelper(this);

        // Retrieve the item passed via Intent
        currentItem = (LostFoundItem) getIntent().getSerializableExtra("selected_item");

        if (currentItem != null) {
            setupUI();
        } else {
            Toast.makeText(this, "Error: Item not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupUI() {
        ImageView ivFullImage = findViewById(R.id.ivFullImage);
        TextView tvType = findViewById(R.id.tvDetailType);
        TextView tvName = findViewById(R.id.tvDetailName);
        TextView tvCategory = findViewById(R.id.tvDetailCategory);
        TextView tvLocation = findViewById(R.id.tvDetailLocation);
        TextView tvDescription = findViewById(R.id.tvDetailDescription);
        TextView tvContact = findViewById(R.id.tvDetailContact);
        TextView tvPhone = findViewById(R.id.tvDetailPhone);
        TextView tvTimestamp = findViewById(R.id.tvDetailTimestamp);
        Button btnRemove = findViewById(R.id.btnRemove);

        // Set text data
        tvType.setText(currentItem.getType());
        tvName.setText(currentItem.getName());
        tvCategory.setText("Category: " + currentItem.getCategory());
        tvLocation.setText("Location: " + currentItem.getLocation());
        tvDescription.setText(currentItem.getDescription());
        tvContact.setText("Contact: " + currentItem.getContactName());
        tvPhone.setText("Phone: " + currentItem.getPhoneNumber());
        tvTimestamp.setText("Posted on: " + currentItem.getTimestamp());

        // Memory-safe image loading for the detail screen
        if (currentItem.getImageUri() != null) {
            try {
                Uri uri = Uri.parse(currentItem.getImageUri());
                String path = uri.getPath();
                if (path != null && new File(path).exists()) {
                    // Load a large but safe version of the image (max 1024x1024)
                    Bitmap bitmap = decodeSampledBitmap(path, 1024, 1024);
                    if (bitmap != null) {
                        ivFullImage.setImageBitmap(bitmap);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading detail image", e);
            }
        }

        // Remove button click listener
        btnRemove.setOnClickListener(v -> showDeleteConfirmation());
    }

    /**
     * Helper to decode an image into a smaller size to avoid OutOfMemory errors.
     */
    private Bitmap decodeSampledBitmap(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate sample size (power of 2)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Advert")
                .setMessage("Are you sure you want to remove this advert?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteItem(currentItem.getId());
                    Toast.makeText(ItemDetailActivity.this, "Advert removed", Toast.LENGTH_SHORT).show();
                    finish(); // Return to the list
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
