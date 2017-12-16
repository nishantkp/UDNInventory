package com.example.android.udninventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.udninventory.data.ItemContract.ItemEntry;

/**
 * Helper class to create a database
 * Created by Nishant on 11/30/2017.
 */

public class ItemDbHelper extends SQLiteOpenHelper {

    // Version of database, if you change the schema
    // you must increment the database version
    public static final int DATABASE_VERSION = 6;

    // Table name generated from user email address
    private static String mTableName;

    // Name of database file
    public static final String DATABASE_NAME = "inventory.db";

    // String containing SQL statement to create a table for login information
    public static final String SQL_CREATE_LOGIN_ENTRIES =
            "CREATE TABLE " + ItemEntry.CREDENTIALS_TABLE_NAME + "(" + ItemEntry._ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT" + ","
                    + ItemEntry.CREDENTIALS_TABLE_COLUMN_USER_NAME + " TEXT NOT NULL" + ","
                    + ItemEntry.CREDENTIALS_TABLE_COLUMN_EMAIL + " TEXT NOT NULL" + ","
                    + ItemEntry.CREDENTIALS_TABLE_COLUMN_PASSWORD + " TEXT NOT NULL" + ","
                    + ItemEntry.CREDENTIALS_TABLE_USER_INVENTORY_TABLE + " TEXT NOT NULL" + ")";

    // String containing SQL statement to delete login table
    public static final String SQL_DELETE_LOGIN_ENTRIES =
            "DROP TABLE IF EXISTS " + ItemEntry.CREDENTIALS_TABLE_NAME;

    public ItemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // If database does not exists create a new database or
    // if it does exists, continue with the existing one
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_LOGIN_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(SQL_DELETE_LOGIN_ENTRIES);
        onCreate(sqLiteDatabase);
    }

    /**
     * This method is called to set the table name
     * @param tableName name of the table
     */
    public static void setNewTableName(String tableName) {
        mTableName = tableName;
    }

    /**
     * This method is called to get the table name
     * @return name of the table
     */
    public static String getNewTableName() {
        return mTableName;
    }

    /**
     * This method is called to create a new table is it does not exists
     * @param tableName name of the database table
     */
    public void createDatabaseTable(String tableName) {
        // SQL statement for creating a new table for individual user to store inventory related data
        String CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + tableName + "("
                        + ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + ","
                        + ItemEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL" + ","
                        + ItemEntry.COLUMN_ITEM_PRICE + " REAL NOT NULL" + ","
                        + ItemEntry.COLUMN_ITEM_CATEGORY + " TEXT" + ","
                        + ItemEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL DEFAULT 0" + ","
                        + ItemEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL" + ","
                        + ItemEntry.COLUMN_SUPPLIER_PHONE + " TEXT" + ","
                        + ItemEntry.COLUMN_SUPPLIER_EMAIL + " TEXT" + ","
                        + ItemEntry.COLUMN_ITEM_THUMBNAIL + " BLOB" + ")";
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL(CREATE_ENTRIES);
    }
}
