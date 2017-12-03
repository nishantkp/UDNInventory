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
    public static final int DATABASE_VERSION = 1;

    // Name of database file
    public static final String DATABASE_NAME = "inventory.db";

    // String containing SQL statement to create table
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ItemEntry.TABLE_NAME + "("
                    + ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + ","
                    + ItemEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL" + ","
                    + ItemEntry.COLUMN_ITEM_PRICE + " REAL NOT NULL" + ","
                    + ItemEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL DEFAULT 0" + ","
                    + ItemEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL" + ","
                    + ItemEntry.COLUMN_SUPPLIER_PHONE + " TEXT" + ","
                    + ItemEntry.COLUMN_SUPPLIER_EMAIL + " TEXT" + ","
                    + ItemEntry.COLUMN_ITEM_THUMBNAIL + " BLOB" + ")";

    // String containing SQL statement to delete table
    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ItemEntry.TABLE_NAME;

    public ItemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // If database does not exists create a new database or
    // if it does exists, continue with the existing one
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }
}
