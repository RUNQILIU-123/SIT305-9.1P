package com.example.lostfoundapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display all lost and found items.
 * Supports filtering by category.
 */
public class ItemListActivity extends AppCompatActivity {

    private RecyclerView rvItems;
    private Spinner spFilterCategory;
    private ItemAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<LostFoundItem> itemList;

    // Filter categories including "All"
    private final String[] filterCategories = {"All", "Electronics", "Pets", "Wallets", "Keys", "Bags", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        dbHelper = new DatabaseHelper(this);
        rvItems = findViewById(R.id.rvItems);
        spFilterCategory = findViewById(R.id.spFilterCategory);

        // Initialize RecyclerView
        itemList = new ArrayList<>();
        adapter = new ItemAdapter(this, itemList, item -> {
            // On item click, open detail activity
            Intent intent = new Intent(ItemListActivity.this, ItemDetailActivity.class);
            intent.putExtra("selected_item", item);
            startActivity(intent);
        });
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(adapter);

        // Set up filter spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filterCategories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterCategory.setAdapter(spinnerAdapter);

        // Filter selection listener
        spFilterCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadItems(filterCategories[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list when returning to activity (e.g., after deletion)
        loadItems(spFilterCategory.getSelectedItem().toString());
    }

    private void loadItems(String category) {
        itemList = dbHelper.getAllItems(category);
        adapter.updateList(itemList);
    }
}
