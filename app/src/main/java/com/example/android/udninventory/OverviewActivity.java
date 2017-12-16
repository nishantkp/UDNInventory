package com.example.android.udninventory;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.udninventory.data.ItemBitmapUtils;
import com.example.android.udninventory.data.ItemContract.ItemEntry;

public class OverviewActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /* Id for the loader */
    private static final int LOADER_ID = 0;
    /* Tag for log messages */
    private static final String LOG_TAG = OverviewActivity.class.getName();
    /* Stores the Uri received from InventoryListActivity */
    private Uri mCurrentItemUri;
    /* TextView for name of item */
    private TextView mItemName;
    /* TextView for quantity for the item */
    private TextView mItemQuantity;
    /* TextView for price of item */
    private TextView mItemPrice;
    /* TextView for category of item */
    private TextView mItemCategory;
    /* TextView for name of supplier */
    private TextView mSupplierName;
    /* TextView for phone number of supplier */
    private TextView mSupplierPhone;
    /* TextView for email of supplier */
    private TextView mSupplierEmail;
    /* ImageView for item thumbnail */
    private ImageView mItemThumbnail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_overview);
        setTitle("Overview");

        // Examine the intent that was used to launch this activity
        // in order to figuring out we are creating a new item or editing an existing one
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();
        Log.i(LOG_TAG, "Overview activity null uri value : " + mCurrentItemUri);
        if (mCurrentItemUri == null) {
            NavUtils.navigateUpFromSameTask(OverviewActivity.this);
        }

        // Find all the relevant TextViews to display the overview of an item
        mItemName = findViewById(R.id.item_overview_item_name);
        mItemPrice = findViewById(R.id.item_overview_item_price);
        mItemQuantity = findViewById(R.id.item_overview_item_quantity);
        mItemCategory = findViewById(R.id.item_overview_item_category);
        mSupplierName = findViewById(R.id.item_overview_supplier_name);
        mSupplierPhone = findViewById(R.id.item_overview_supplier_phone);
        mSupplierEmail = findViewById(R.id.item_overview_supplier_email);
        mItemThumbnail = findViewById(R.id.item_overview_item_thumbnail);

        // Kick off the loader
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the overview shows all the attributes, define projection that
        // contains all the attributes of items table
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_CATEGORY,
                ItemEntry.COLUMN_SUPPLIER_NAME,
                ItemEntry.COLUMN_SUPPLIER_PHONE,
                ItemEntry.COLUMN_SUPPLIER_EMAIL,
                ItemEntry.COLUMN_ITEM_THUMBNAIL};
        // This loader will execute the content providers query method on background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentItemUri,        // Query the ContentProvider for current pet
                projection,             // Columns to include in resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Proceed to moving ti the first row of cursor,
        // (as this should be the only row in the cursor)
        if (data.moveToFirst()) {
            // Find the columns of table that we are interested in
            int itemNameIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int itemQuantityIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
            int itemPriceIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
            int itemCategoryIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_CATEGORY);
            int supplierNameIndex = data.getColumnIndex(ItemEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneIndex = data.getColumnIndex(ItemEntry.COLUMN_SUPPLIER_PHONE);
            int supplierEmailIndex = data.getColumnIndex(ItemEntry.COLUMN_SUPPLIER_EMAIL);
            int itemThumbnailIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_THUMBNAIL);

            // Extract the value from cursor at given index
            String itemNameString = data.getString(itemNameIndex);
            String itemQuantityString = data.getString(itemQuantityIndex);
            String itemPriceString = data.getString(itemPriceIndex);
            String itemCategoryString = data.getString(itemCategoryIndex);
            String supplierNameString = data.getString(supplierNameIndex);
            String supplierPhoneString = data.getString(supplierPhoneIndex);
            String supplierEmailString = data.getString(supplierEmailIndex);
            byte[] imageByte = data.getBlob(itemThumbnailIndex);

            // Set the text to appropriate EditText views with values in database
            mItemName.setText(itemNameString);
            mItemQuantity.setText(itemQuantityString);
            mItemPrice.setText("$ " + itemPriceString);

            // If user has not provided item category, display n/a
            if(TextUtils.isEmpty(itemCategoryString)){
                mItemCategory.setText("Category : n/a");
            } else {
                // If user has provided item category, display category
                mItemCategory.setText("Category : " + itemCategoryString);
            }

            // If supplier name is not present, set text to "n/a"
            if (TextUtils.isEmpty(supplierNameString)) {
                mSupplierName.setText("n/a");
            } else {
                mSupplierName.setText(supplierNameString);
            }

            // If supplier phone number is not available, set text to "n/a"
            if (TextUtils.isEmpty(supplierPhoneString)) {
                mSupplierPhone.setText("n/a");
            } else {
                mSupplierPhone.setText(supplierPhoneString);
            }

            // If supplier email is not available, set text to "n/a"
            if (TextUtils.isEmpty(supplierEmailString)) {
                mSupplierEmail.setText("n/a");
            } else {
                mSupplierEmail.setText(supplierEmailString);
            }

            // If we have byte[] for Bitmap thumbnail, then get the Bitmap from the byte[]
            // (use getImage() method from ItemBitmapUtils class in .Data)
            // and set it to the ImageView
            if (imageByte != null) {
                Bitmap bitmap = ItemBitmapUtils.getImage(imageByte);
                mItemThumbnail.setImageBitmap(bitmap);
            }
        }
    }

    /* Finish the activity when back is pressd */
    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(OverviewActivity.this);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mItemName.setText(null);
        mItemPrice.setText(null);
        mItemQuantity.setText(null);
        mSupplierName.setText(null);
        mSupplierPhone.setText(null);
        mSupplierEmail.setText(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overview_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.overview_edit_option:
                // Create a new intent to go to {@link EditorActivity}
                Intent intent = new Intent(OverviewActivity.this, EditorActivity.class);
                // Set uri as data field for Intent
                intent.setData(mCurrentItemUri);
                // Launch the {@link EditorActivity} to display the current pet
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}