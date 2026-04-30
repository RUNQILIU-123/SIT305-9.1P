# SIT305 Task 7.1P Lost and Found App

## Description
The Lost and Found App is a mobile application developed for Android that allows users to post advertisements for items they have lost or found. It provides a platform to browse through items, filter them by category, and contact the person who posted the advert.

## Main Features
- **Create Advert**: Users can post a new lost or found item with details including name, contact info, description, category, and location.
- **Image Upload**: Users can select an image from their device gallery to accompany the post.
- **SQLite Database**: All data, including item details and image URIs, is stored locally in a SQLite database.
- **Show All Items**: A list view of all posted adverts with thumbnails and key details.
- **Category Filtering**: Users can filter the list of items by categories (Electronics, Pets, Wallets, Keys, Bags, Other).
- **Item Details**: Tapping on an item shows its full details and a large image.
- **Remove Advert**: Users can delete an advert once the item has been returned or the post is no longer needed.
- **Timestamp**: Each post is automatically timestamped when created.

## Technologies Used
- **Java**: Primary programming language.
- **Android Studio**: Development environment.
- **SQLite**: Local database management.
- **Activity Result API**: For handling image selection from the gallery.

## How to Run the App
1. Open the project in **Android Studio**.
2. Sync the project with Gradle files.
3. Connect an Android device or start an emulator (API 31 or higher recommended).
4. Click the **Run** button in Android Studio.

## Implementation Notes
- **Image Handling**: The app uses `getContentResolver().takePersistableUriPermission` to ensure that image previews remain visible even after the app is restarted.
- **Data Persistence**: Uses a custom `DatabaseHelper` class extending `SQLiteOpenHelper`.
- **UI**: Built using standard XML layouts, `RecyclerView`, and Material Components where available.
