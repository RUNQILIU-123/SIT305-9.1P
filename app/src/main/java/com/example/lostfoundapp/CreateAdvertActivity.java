package com.example.lostfoundapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 修复了上传图片闪退问题的版本
 */
public class CreateAdvertActivity extends AppCompatActivity {

    private static final String TAG = "CreateAdvertFix";
    private RadioGroup rgType;
    private EditText etName, etContactName, etPhone, etDescription, etLocation;
    private Spinner spCategory;
    private ImageView ivPreview;
    private Uri savedImageUri = null;
    private DatabaseHelper dbHelper;

    private final String[] categories = {"Electronics", "Pets", "Wallets", "Keys", "Bags", "Other"};

    // 图片选择回调
    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        // 重要：立即打开输入流，防止权限失效
                        InputStream is = getContentResolver().openInputStream(uri);
                        if (is != null) {
                            // 1. 创建本地私有文件
                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                            File file = new File(getFilesDir(), "IMG_" + timeStamp + ".jpg");
                            
                            // 2. 将图片流拷贝到本地，彻底解决权限问题
                            FileOutputStream fos = new FileOutputStream(file);
                            byte[] buffer = new byte[8192];
                            int read;
                            while ((read = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, read);
                            }
                            fos.close();
                            is.close();

                            // 3. 记录本地路径
                            savedImageUri = Uri.fromFile(file);
                            
                            // 4. 显示预览
                            showImagePreview(file.getAbsolutePath());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Critical error picking image", e);
                        Toast.makeText(this, "Error processing image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_advert);

        dbHelper = new DatabaseHelper(this);
        
        // 初始化控件
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

        // 设置下拉列表
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        // 绑定按钮
        btnUploadImage.setOnClickListener(v -> mGetContent.launch("image/*"));
        btnSave.setOnClickListener(v -> saveAdvert());
    }

    private void showImagePreview(String path) {
        try {
            // 采用采样率压缩，防止大图 OOM 闪退
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4; // 缩小 4 倍显示预览
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

        int selectedId = rgType.getCheckedRadioButtonId();
        RadioButton rb = findViewById(selectedId);
        String type = rb.getText().toString();
        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        LostFoundItem item = new LostFoundItem(0, type, name, contact, phone, desc, 
                                            spCategory.getSelectedItem().toString(), loc, 
                                            savedImageUri.toString(), ts);

        if (dbHelper.insertItem(item) != -1) {
            Toast.makeText(this, "Advert saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show();
        }
    }
}
