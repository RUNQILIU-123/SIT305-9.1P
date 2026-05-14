package com.example.lostfoundapp;

import java.io.Serializable;

/**
 * Model class for a Lost or Found item.
 * Implements Serializable to allow passing objects between activities.
 */
public class LostFoundItem implements Serializable {
    private int id;
    private String type; // Lost or Found
    private String name;
    private String contactName;
    private String phoneNumber;
    private String description;
    private String category;
    private String location;
    private String imageUri;
    private String timestamp;
    private double latitude;
    private double longitude;

    public LostFoundItem() {}

    public LostFoundItem(int id, String type, String name, String contactName, String phoneNumber, 
                        String description, String category, String location, String imageUri, String timestamp) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.contactName = contactName;
        this.phoneNumber = phoneNumber;
        this.description = description;
        this.category = category;
        this.location = location;
        this.imageUri = imageUri;
        this.timestamp = timestamp;
    }

    public LostFoundItem(int id, String type, String name, String contactName, String phoneNumber, 
                        String description, String category, String location, String imageUri, String timestamp,
                        double latitude, double longitude) {
        this(id, type, name, contactName, phoneNumber, description, category, location, imageUri, timestamp);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
