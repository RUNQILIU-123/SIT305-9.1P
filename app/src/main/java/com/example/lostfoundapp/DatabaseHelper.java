package com.example.lostfoundapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite Database Helper for storing Lost and Found items.
 * Extended for Task 9.1P to include latitude and longitude.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "LostFoundDB";
    private static final int DATABASE_VERSION = 2; // Incremented version for Task 9.1P

    // Table and Columns
    private static final String TABLE_ITEMS = "items";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CONTACT = "contact_name";
    private static final String COLUMN_PHONE = "phone_number";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_IMAGE_URI = "image_uri";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_ITEMS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TYPE + " TEXT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_CONTACT + " TEXT,"
                + COLUMN_PHONE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_CATEGORY + " TEXT,"
                + COLUMN_LOCATION + " TEXT,"
                + COLUMN_IMAGE_URI + " TEXT,"
                + COLUMN_TIMESTAMP + " TEXT,"
                + COLUMN_LATITUDE + " REAL DEFAULT 0,"
                + COLUMN_LONGITUDE + " REAL DEFAULT 0" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Safe upgrade: add latitude and longitude columns without deleting existing data
            db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COLUMN_LATITUDE + " REAL DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COLUMN_LONGITUDE + " REAL DEFAULT 0");
        }
    }

    /**
     * Inserts a new item into the database.
     */
    public long insertItem(LostFoundItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TYPE, item.getType());
        values.put(COLUMN_NAME, item.getName());
        values.put(COLUMN_CONTACT, item.getContactName());
        values.put(COLUMN_PHONE, item.getPhoneNumber());
        values.put(COLUMN_DESCRIPTION, item.getDescription());
        values.put(COLUMN_CATEGORY, item.getCategory());
        values.put(COLUMN_LOCATION, item.getLocation());
        values.put(COLUMN_IMAGE_URI, item.getImageUri());
        values.put(COLUMN_TIMESTAMP, item.getTimestamp());
        values.put(COLUMN_LATITUDE, item.getLatitude());
        values.put(COLUMN_LONGITUDE, item.getLongitude());

        long id = db.insert(TABLE_ITEMS, null, values);
        db.close();
        return id;
    }

    /**
     * Updates an existing item (e.g., after geocoding old records).
     */
    public int updateItem(LostFoundItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUDE, item.getLatitude());
        values.put(COLUMN_LONGITUDE, item.getLongitude());

        int result = db.update(TABLE_ITEMS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(item.getId())});
        db.close();
        return result;
    }

    /**
     * Retrieves all items from the database, optionally filtered by category.
     */
    public List<LostFoundItem> getAllItems(String filterCategory) {
        List<LostFoundItem> itemList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selectQuery;
        String[] selectionArgs = null;

        if (filterCategory == null || filterCategory.equals("All")) {
            selectQuery = "SELECT * FROM " + TABLE_ITEMS + " ORDER BY " + COLUMN_ID + " DESC";
        } else {
            selectQuery = "SELECT * FROM " + TABLE_ITEMS + " WHERE " + COLUMN_CATEGORY + " = ? ORDER BY " + COLUMN_ID + " DESC";
            selectionArgs = new String[]{filterCategory};
        }

        Cursor cursor = db.rawQuery(selectQuery, selectionArgs);

        if (cursor.moveToFirst()) {
            do {
                LostFoundItem item = new LostFoundItem();
                item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                item.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
                item.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                item.setContactName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT)));
                item.setPhoneNumber(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)));
                item.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                item.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                item.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)));
                item.setImageUri(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI)));
                item.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));
                item.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)));
                item.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)));
                itemList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return itemList;
    }

    /**
     * Deletes an item by its ID.
     */
    public void deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ITEMS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}
