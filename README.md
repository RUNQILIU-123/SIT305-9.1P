# SIT305 Task 9.1P - Lost and Found Map Mobile App

## Project Overview

This is an Android-based Lost and Found mobile application developed in Android Studio. The project is extended from the original Task 7.1P Lost and Found App and adds map and location-based features for Task 9.1P.

The app allows users to create lost or found item posts and attach a location to each item. Users can select a location by using address autocomplete or by getting their current location. All lost and found items can be displayed on a map. The app also supports radius-based searching, which only shows items within a selected distance from the user's current location.

## Main Features

- Create Lost or Found item records
- Enter item name, phone number, description, date, and location
- Select locations using autocomplete
- Get the user's current location
- Display lost and found items on Google Maps
- Show item locations using map markers
- Search items within a selected radius
- Only display items within X km of the user's current location
- View and manage saved lost and found records

## New Features for Task 9.1P

This task focuses on adding Android geo features to the existing Lost and Found app, including:

- Google Maps integration
- Location selection
- Address autocomplete
- Current location detection
- Map marker display
- Radius-based item filtering

These features help users view item locations more clearly and improve the chance of finding lost or found items nearby.

## Tech Stack

- Android Studio
- Java / Kotlin
- SQLite / Room Database
- Google Maps API
- Google Places Autocomplete API
- Android Location Services
- Gradle

## Project Structure

```text
SIT305-9.1P/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── ...
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   ├── drawable/
│   │   │   │   └── values/
│   │   │   └── AndroidManifest.xml
│   │
│   └── build.gradle
│
├── build.gradle
├── settings.gradle
└── README.md
