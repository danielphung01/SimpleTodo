package com.example.simpletodo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_ITEM_TEXT = "item_text";
    public static final String KEY_ITEM_POSITION = "item_position";
    public boolean isListName = false;
    public static final int EDIT_TEXT_CODE = 20;

    List<String> items;         // List without List Name for RecyclerView to display
    List<String> saveList;      // List with List Name to be saved

    TextView listName;
    Button btnAdd;
    EditText etItem;
    RecyclerView rvItems;
    ItemsAdapter itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listName = findViewById(R.id.listName);
        btnAdd = findViewById(R.id.btnAdd);
        etItem = findViewById(R.id.etItem);
        rvItems = findViewById(R.id.rvItems);

        loadItems();
        listName.setText(saveList.get(0));

        // Edit List name
        listName.setOnClickListener(v -> {
            String name = listName.getText().toString();
            Log.d("MainActivity", "List name pressed");

            Intent i = new Intent(MainActivity.this, EditActivity.class);
            // pass the data being edited
            isListName = true;
            i.putExtra(KEY_ITEM_TEXT, name);
            i.putExtra(KEY_ITEM_POSITION, 0);
            // display the activity
            startActivityForResult(i, EDIT_TEXT_CODE);
        });

        ItemsAdapter.OnLongClickListener onLongClickListener = position -> {
            // Delete the item from the model
            items.remove(position);
            saveList.remove(position + 1);
            // Notify the adapter
            itemsAdapter.notifyItemRemoved(position);
            Toast.makeText(getApplicationContext(), "Item was removed", Toast.LENGTH_SHORT).show();
            saveItems();
        };

        ItemsAdapter.OnClickListener onClickListener = position -> {
            Log.d("MainActivity", "Single click at position " + position);
            // create the new activity
            Intent i = new Intent(MainActivity.this, EditActivity.class);
            // pass the data being edited
            isListName = false;
            i.putExtra(KEY_ITEM_TEXT, items.get(position));
            i.putExtra(KEY_ITEM_POSITION, position);
            // display the activity
            startActivityForResult(i, EDIT_TEXT_CODE);
        };
        itemsAdapter = new ItemsAdapter(items, onLongClickListener, onClickListener);
        rvItems.setAdapter(itemsAdapter);
        rvItems.setLayoutManager(new LinearLayoutManager(this));

        btnAdd.setOnClickListener(v -> {
            String todoItem = etItem.getText().toString();
            if (todoItem.length() < 1) {
                Log.d("MainActivity", "empty object");
                etItem.setText("");
                Toast.makeText(getApplicationContext(), "Can't add an item with no text!", Toast.LENGTH_SHORT).show();
            } else {
                // Add it to the model
                items.add(todoItem);
                saveList.add(todoItem);
                // Notify adapter that an item is inserted
                itemsAdapter.notifyItemInserted(items.size() - 1);
                etItem.setText("");
                Toast.makeText(getApplicationContext(), "Item was added", Toast.LENGTH_SHORT).show();
                saveItems();
            }
        });
    }

    // handle the result of the edit activity
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        //super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == EDIT_TEXT_CODE) {
            // Retrieve the updated text value
            String itemText = data.getStringExtra(KEY_ITEM_TEXT);
            // extract the original position of the edited item from the position key

            if (isListName) {
                listName.setText(itemText);
                saveList.set(0, itemText);
                saveItems();
                Toast.makeText(getApplicationContext(), "List name updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                int position = data.getExtras().getInt(KEY_ITEM_POSITION);

                if (itemText.length() == 0) {
                    // Remove item if item is changed to nothing
                    items.remove(position);
                    saveList.remove(position + 1);
                    itemsAdapter.notifyItemRemoved(position);
                    Toast.makeText(getApplicationContext(), "Item was removed", Toast.LENGTH_SHORT).show();
                    saveItems();
                } else {
                    // update the model at the right position with new item text
                    items.set(position, itemText);
                    saveList.set(position + 1, itemText);
                    // notify the adapter
                    itemsAdapter.notifyItemChanged(position);
                    // persist the changes
                    saveItems();
                    Toast.makeText(getApplicationContext(), "Item updated successfully", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Log.w("MainActivity", "Unknown call to onActivityResult");
        }
    }

    private File getDataFile() {
        return new File(getFilesDir(), "data.txt");
    }

    // This function will load items by reading every line of the data file
    private void loadItems() {
        try {
            saveList = new ArrayList<>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
            items = new ArrayList<>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
            items.remove(0);
        } catch (IOException e) {
            Log.e("MainActivity", "Error reading items", e);
            items = new ArrayList<>();
            saveList = new ArrayList<>();
            saveList.add("");
        }
    }

    // This function saves items by writing them into the data file
    private void saveItems() {
        try {
            FileUtils.writeLines(getDataFile(), saveList);
        } catch (IOException e) {
            Log.e("MainActivity", "Error writing items", e);
        }
    }
}