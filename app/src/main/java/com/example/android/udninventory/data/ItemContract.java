package com.example.android.udninventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract class for database
 * Created by Nishant on 11/30/2017.
 */

public class ItemContract {
    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.undinventory";

    /**
     * Use the CONTENT_AUTHORITY to create the base of all URI's which the app will use to contact
     * the content provider
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path
     * I.e, content://com.example.android.inventory/items/ is valid path for
     * inventory item data
     * content://com.example.android.inventory/sells/ will fails as the contentProvider has
     * no information on what to do with "sells"
     */
    public static final String ITEM_PATH = "items";

    /**
     * Possible path, i.e, content://com.example.inventory/credentials/ is a valid path for inventory
     * login details
     */
    public static final String CREDENTIALS_PATH = "credentials";

    // To prevent someone from accidentally instantiating contract class,
    // give it an empty constructor
    private ItemContract() {
    }

    public static abstract class ItemEntry implements BaseColumns {

        /**
         * The content Uri to access the item data from the in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, ITEM_PATH);

        /**
         * Content Uri to access the login information
         */
        public static final Uri CREDENTIALS_CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CREDENTIALS_PATH);

        /**
         * MIME type of the {@link #CONTENT_URI} for list of items
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + ITEM_PATH;

        /**
         * MIME type of the {@link #CONTENT_URI} for the single items
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + ITEM_PATH;

        /**
         * Name of database table for inventory
         */
        public static final String TABLE_NAME = "items";

        /**
         * Name of database table for username and passwords
         */
        public static final String CREDENTIALS_TABLE_NAME = "credentials";

        /**
         * Name of the user
         * Type: TEXT
         */
        public static final String CREDENTIALS_TABLE_COLUMN_USER_NAME = "user_name";

        /**
         * Email of user
         * Type: TEXT
         */
        public static final String CREDENTIALS_TABLE_COLUMN_EMAIL = "email";

        /**
         * password set ny user
         * Type: TEXT
         */
        public static final String CREDENTIALS_TABLE_COLUMN_PASSWORD = "password";

        /**
         * Unique ID number for an Item (only for use in the database table).
         * Type: INTEGER
         */
        public static final String _ID = BaseColumns._ID;

        /**
         * Name of an item
         * TYpe: TEXT
         */
        public static final String COLUMN_ITEM_NAME = "item_name";

        /**
         * Quantity of an item
         * Type: INTEGER
         */
        public static final String COLUMN_ITEM_QUANTITY = "item_quantity";

        /**
         * Price of an item
         * Type : INTEGER
         */
        public static final String COLUMN_ITEM_PRICE = "item_price";

        /**
         * Category of item
         * Type: TEXT
         */
        public static final String COLUMN_ITEM_CATEGORY = "item_category";

        /**
         * Name of supplier
         * Type: TEXT
         */
        public static final String COLUMN_SUPPLIER_NAME = "supplier_name";

        /**
         * Phone number of supplier
         * Type: Text
         */
        public static final String COLUMN_SUPPLIER_PHONE = "supplier_phone";

        /**
         * Email of suppler
         * Type : TEXT
         */
        public static final String COLUMN_SUPPLIER_EMAIL = "supplier_email";

        /**
         * Image of item
         */
        public static final String COLUMN_ITEM_THUMBNAIL = "item_image";
    }

}
