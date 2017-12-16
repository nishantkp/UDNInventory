package com.example.android.udninventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.udninventory.data.ItemContract.ItemEntry;

/**
 * Custom ContentProvider class to deal with database CRUD operations
 * C    -   Create
 * R    -   Read
 * U    -   Update
 * D    -   Delete
 * <p>
 * Created by Nishant on 12/3/2017.
 */

public class ItemProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = ItemProvider.class.getName();
    /**
     * URI matcher code for whole item table
     */
    private static final int ITEM = 100;
    /**
     * URI matched code for a single item from the table
     */
    private static final int ITEM_ID = 101;
    private static final int CREDENTIALS = 102;
    private static final int CREDENTIALS_ID = 103;
    /**
     * UriMatcher object to match the context URI to corresponding code
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer, this runs the first time anything is called from this class
    static {
        // The content uri of form "content://com.example.android.inventory/items" will
        // maps to integer value of {@link ITEM} 100. This uri gives access to multiple
        // rows of items table
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.ITEM_PATH, ITEM);

        // The content uri of form "content://com.example.android.inventory/items/#" will
        // maps to integer value of {@link ITEM_ID} 101. This uri gives access to single item
        // form the table

        // In this case # is a wild card, "#" represents to any integer value
        // i.e content://com.example.android.inventory/items/5 matches but
        // content://com.example.android.inventory/items doesn't match
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.ITEM_PATH + "/#", ITEM_ID);

        // The content uri of form "content://com.example.android.inventory/credentials" will
        // maps to integer value of {@link CREDENTIALS} 102. This uri gives access to multiple
        // rows of credentials table
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.CREDENTIALS_PATH, CREDENTIALS);

        // The content uri of form "content://com.example.android.inventory/credentials/#" will
        // maps to integer value of {@link CREDENTIALS_ID} 103. This uri gives access to particular
        // row of credentials table
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.CREDENTIALS_PATH + "/#", CREDENTIALS_ID);
    }

    // Database helper object
    private ItemDbHelper mItemDbHelper;

    // Initialize the provider and helper object
    @Override
    public boolean onCreate() {
        mItemDbHelper = new ItemDbHelper(getContext());
        return true;
    }

    /**
     * Performs the query on given URI
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {

        // Get readable database inorder to query data
        SQLiteDatabase database = mItemDbHelper.getReadableDatabase();

        // This will hold the result of query
        Cursor cursor = null;

        // figure out if URI matcher can match the uri and get the matching code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM:
                // Query the items table directly with given projection, selection, selectionArgs,
                // and sort order
                // The cursor could contain multiple rows of items table
                cursor = database.query(ItemDbHelper.getNewTableName(), projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ITEM_ID:
                // Extract the ID from the URI
                // For uri content://com.example.android.inventory/items/3
                // selection will be "_ID=?" and
                // selectionArgs will be string array containing actual id of 3

                // For every "?" in selection, we need to have an element in electionArgs
                // Since we hve only one "?" in our case, the selectionArgs string array
                // only contains one element
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ItemDbHelper.getNewTableName(), projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case CREDENTIALS:
                // Query the items table directly with given projection, selection, selectionArgs,
                // and sort order
                // The cursor could contain multiple rows of items table
                cursor = database.query(ItemEntry.CREDENTIALS_TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Can not query unknown URI " + uri);
        }

        // Set the notification uri on Cursor
        // So we know what content URI the Cursor was created for
        // If the data at this URI changes, so we know we need to update the Cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * MIME Type : describes which type of data our app is handling
     * <p>
     * Returns the MIME type of data for content URI
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM:
                return ItemEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return ItemEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Insert a new item into provider with given ContentValues
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM:
                return insertItem(uri, values);
            case CREDENTIALS:
                return insertCredentials(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not support for " + uri);
        }
    }

    /**
     * Delete the data as given selection and selectionArg
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        // Get the writable database
        SQLiteDatabase database = mItemDbHelper.getWritableDatabase();
        // Track the number of rows deleted
        int rowsDeleted = 0;
        switch (match) {
            case ITEM:
                // Delete all the rows that matches selection and selectionArgs
                rowsDeleted = database.delete(ItemDbHelper.getNewTableName(), selection, selectionArgs);
                break;
            case ITEM_ID:
                // Delete the single row given by id in URI
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ItemDbHelper.getNewTableName(), selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    /**
     * Update the data at given selection, selection arguments and content values
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM:
                return updateItem(uri, values, selection, selectionArgs);
            case ITEM_ID:
                // For ITEM_ID extract the id from URI so we know which row to update.
                // selection will be "_ID=" and selectionArgs will be actual id of item
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Insert an item into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertItem(Uri uri, ContentValues contentValues) {

        String itemName = contentValues.getAsString(ItemEntry.COLUMN_ITEM_NAME);
        // Check that the item name value is not null
        if (itemName == null) {
            throw new IllegalArgumentException("Item name is required");
        }

        Integer itemPrice = contentValues.getAsInteger(ItemEntry.COLUMN_ITEM_PRICE);
        // Check the item price is not null and it's positive value
        if (itemPrice != null && itemPrice < 0) {
            throw new IllegalArgumentException("Item requires a valid price");
        }

        Integer itemQuantity = contentValues.getAsInteger(ItemEntry.COLUMN_ITEM_QUANTITY);
        // Check the item quantity is not null and it's a positive value
        if (itemQuantity != null && itemQuantity < 0) {
            throw new IllegalArgumentException("Item requires a valid quantity");
        }

        String supplierName = contentValues.getAsString(ItemEntry.COLUMN_SUPPLIER_NAME);
        // Check the supplier name is not a null value
        if (supplierName == null) {
            throw new IllegalArgumentException("Item requires a supplier name");
        }

        // Get the writable database in order to insert a new item
        SQLiteDatabase database = mItemDbHelper.getWritableDatabase();
        // Insert a new item with given values
        long id = database.insert(ItemDbHelper.getNewTableName(), null, contentValues);
        if (id == -1) {
            database.close();
            return null;
        }
        database.close();
        // Notify all the listener that data has changed for the item content uri
        // content://com.example.android.inventory/items
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the uri with ID ( ID of newly inserted row) appended at the end of URI
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Insert a new account details into table
     *
     * @param uri           Uri of credentials table to insert new data
     * @param contentValues details about new account
     * @return Uri of newly inserted row
     */
    private Uri insertCredentials(Uri uri, ContentValues contentValues) {
        // Get the writable database in order to insert a new credentials
        SQLiteDatabase database = mItemDbHelper.getWritableDatabase();
        // Insert new credentials into table
        long id = database.insert(ItemEntry.CREDENTIALS_TABLE_NAME, null, contentValues);
        if (id == -1) {
            database.close();
            return null;
        }
        database.close();
        // Return the uri with ID (ID of newly inserted row) appended at the end of URI
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Update the database with given content values. Apply the changes to row specified in
     * selection and selectionArgs
     * return the number of rows that were successfully updated
     */
    private int updateItem(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        // If the {@link ItemEntry#COLUMN_ITEM_NAME} key is present,
        // check that the name value is not null
        if (contentValues.containsKey(ItemEntry.COLUMN_ITEM_NAME)) {
            String itemName = contentValues.getAsString(ItemEntry.COLUMN_ITEM_NAME);
            if (itemName == null) {
                throw new IllegalArgumentException("Item name is required");
            }
        }

        // If the {@link ItemEntry#COLUMN_ITEM_PRICE} key is present,
        // Check the price is not null and it's positive value
        if (contentValues.containsKey(ItemEntry.COLUMN_ITEM_PRICE)) {
            Integer itemPrice = contentValues.getAsInteger(ItemEntry.COLUMN_ITEM_PRICE);
            if (itemPrice != null && itemPrice < 0) {
                throw new IllegalArgumentException("Item requires a valid price");
            }
        }

        // If the {@link ItemEntry#COLUMN_ITEM_QUANTITY} key is present,
        // Check the quantity is not null and it's a positive value
        if (contentValues.containsKey(ItemEntry.COLUMN_ITEM_QUANTITY)) {
            Integer itemQuantity = contentValues.getAsInteger(ItemEntry.COLUMN_ITEM_QUANTITY);
            if (itemQuantity != null && itemQuantity < 0) {
                throw new IllegalArgumentException("Item requires a valid quantity");
            }
        }

        // If the{@link ItemEntry#COLUMN_SUPPLIER_NAME} key is present,
        // Check the name is not a null value
        if (contentValues.containsKey(ItemEntry.COLUMN_SUPPLIER_NAME)) {
            String supplierName = contentValues.getAsString(ItemEntry.COLUMN_SUPPLIER_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("Item requires a supplier name");
            }
        }

        // Get the writable database
        SQLiteDatabase database = mItemDbHelper.getWritableDatabase();
        // Update the database and get the number of row affected
        int rowsUpdated = database.update(ItemDbHelper.getNewTableName(), contentValues, selection, selectionArgs);
        database.close();
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}