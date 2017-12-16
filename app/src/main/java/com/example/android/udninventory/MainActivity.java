package com.example.android.udninventory;

import android.app.ActivityOptions;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.udninventory.Constants.PublicKeys;
import com.example.android.udninventory.data.ItemBitmapUtils;
import com.example.android.udninventory.data.ItemContract.ItemEntry;
import com.example.android.udninventory.data.ItemDbHelper;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /* Tag for log messages */
    private static final String LOG_TAG = MainActivity.class.getName();

    /* Loader ID */
    private static final int LOADER_ID = 0;
    private ItemCursorAdapter mItemCursorAdapter;

    /* Name of the table received from MainLoginActivity when user successfully login */
    private String mTableNameForUserInventory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        // Get the intent sent from MainLoginActivity
        Intent loginToUserAccount = getIntent();
        // Get the table name from intent sent from MainLoginActivity
        mTableNameForUserInventory = loginToUserAccount.getStringExtra(PublicKeys.LOGIN_TABLE_NAME_INTENT_KEY);
        // Set the table name for in ItemDbHelper, which will be used bu ItemProvider to perform CURD operation
        ItemDbHelper.setNewTableName(mTableNameForUserInventory);

        // Create a new table for a user in background thread without blocking UI thread
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ItemDbHelper itemDbHelper = new ItemDbHelper(getApplicationContext());
                itemDbHelper.createDatabaseTable(mTableNameForUserInventory);
            }
        });

        // Find the ListView which will be populated with item data
        ListView itemListView = findViewById(R.id.list);

        // Find the View with id empty_view in res/layout/list.xml
        View emptyView = findViewById(R.id.empty_view);
        // Set the emptyView on ListView, to display the "No content" text
        // when there is no item display on ListView
        itemListView.setEmptyView(emptyView);

        // Set up the adapter to create a new list item for each row
        // There is no data yet(until the loader finishes) so pass the null cursor
        mItemCursorAdapter = new ItemCursorAdapter(this, null);

        // Attach a adapter on ListView
        itemListView.setAdapter(mItemCursorAdapter);


        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // By appending id onto the {@link ItemEntry#CONTENT_URI}, we get the
                // uri of current item clicked on
                // i.e, uri would be content://com.example.android.inventory/items/3
                // if the item with id 3 clicked on
                Uri currentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);

                // Create a new intent to go to {@link OverviewActivity} to display overview
                Intent intent = new Intent(MainActivity.this, OverviewActivity.class);
                // Set uri as data field for Intent
                intent.setData(currentItemUri);
                ActivityOptions options = null;
                // If current SDK build is greater that API 16, then only perform scale up animation
                // to start {@link Overview Activity}
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    options = ActivityOptions.makeScaleUpAnimation(view, 0,
                            0, view.getWidth(), view.getHeight());
                    // Launch the {@link OverviewActivity} to display the current pet
                    startActivity(intent, options.toBundle());
                } else {
                    // Otherwise current SDK build is less than 16, so start activity as usual
                    startActivity(intent);
                }
            }
        });

        // Kick off the loader
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_item_add:
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
                return true;
            case R.id.add_dummy_data:
                // Add dummy data (placeholder data)
                insertDummyItem();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu option from res/menu/catalog_activity.xml
        // This adds menu item to app bar
        getMenuInflater().inflate(R.menu.catalog_activity, menu);
        return true;
    }

    /* Insert a dummy data for verifying functionality of insert method from ItemProvider*/
    private void insertDummyItem() {
        // Converts drawable object into byte[]
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder);
        byte[] bytes = ItemBitmapUtils.getBytes(bitmap);

        // ContentValue object for inserting a dummy data
        ContentValues contentValues = new ContentValues();
        contentValues.put(ItemEntry.COLUMN_ITEM_NAME, "Adidas");
        contentValues.put(ItemEntry.COLUMN_ITEM_PRICE, 99.99);
        contentValues.put(ItemEntry.COLUMN_ITEM_QUANTITY, 236);
        contentValues.put(ItemEntry.COLUMN_SUPPLIER_NAME, "Sports Check");
        contentValues.put(ItemEntry.COLUMN_SUPPLIER_PHONE, "(234)345-4522");
        contentValues.put(ItemEntry.COLUMN_SUPPLIER_EMAIL, "order@sportscheck.ca");
        contentValues.put(ItemEntry.COLUMN_ITEM_THUMBNAIL, bytes);

        // Insert a dummy data into item provider and returning the uri of newly inserted data
        Uri uri = getContentResolver().insert(ItemEntry.CONTENT_URI, contentValues);
        Log.i(LOG_TAG, "Uri for inserting a dummy data : " + uri);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ID:
                // Define a projection, that which column from the database
                // we are interested in
                String[] projection = {
                        ItemEntry._ID,
                        ItemEntry.COLUMN_ITEM_NAME,
                        ItemEntry.COLUMN_ITEM_QUANTITY,
                        ItemEntry.COLUMN_ITEM_PRICE,
                        ItemEntry.COLUMN_ITEM_CATEGORY};

                // This loader will executes content provides query method on background thread
                return new CursorLoader(this,   // Parent activity context
                        ItemEntry.CONTENT_URI,  // Content provider URI to query
                        projection,             // Columns to include in resulting cursor
                        null,                   // No selection clause
                        null,                   // No selection arguments
                        null);                  // Default sort order
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mItemCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mItemCursorAdapter.swapCursor(null);
    }
}