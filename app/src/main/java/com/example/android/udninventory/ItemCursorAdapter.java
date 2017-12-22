package com.example.android.udninventory;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.udninventory.data.ItemContract.ItemEntry;

/**
 * Custom cursor adapter to inflate list item view
 * Created by Nishant on 12/3/2017.
 */

public class ItemCursorAdapter extends CursorAdapter {

    /* TAG for log messages */
    private static final String LOG_TAG = ItemCursorAdapter.class.getName();
    /* App context */
    private final Context mContext;

    /**
     * Constructs a new {@link ItemCursorAdapter}
     *
     * @param context The context
     * @param c       Cursor from which we get the data
     */
    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        mContext = context;
    }

    /**
     * Makes a new blank list view. No data is set to the view yet.
     *
     * @param context app context
     * @param cursor  Cursor from which we get the data. The Cursor is already
     *                moved to appropriate position
     * @param parent  The parent to which new view is attached to
     * @return The newly created list view
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder();
        // Find the appropriate views from res/layout/list_item.xml
        viewHolder.itemNameTextView = view.findViewById(R.id.list_item_item_name);
        viewHolder.itemQuantityTextView = view.findViewById(R.id.list_item_item_quantity);
        viewHolder.itemPriceTextView = view.findViewById(R.id.list_item_item_price);
        viewHolder.itemCategoryTextView = view.findViewById(R.id.list_item_item_category);
        viewHolder.decreaseQuantityButton = view.findViewById(R.id.list_item_item_decrease_quantity_button);
        view.setTag(viewHolder);
        return view;
    }

    /**
     * This method binds the item data (the current row pointed by cursor) to the given
     * list item layout.
     *
     * @param view    Existing view returned earlier by the newView
     * @param context app context
     * @param cursor  Cursor from which we get the data. The Cursor is already moved to
     *                correct row
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void bindView(final View view, Context context, final Cursor cursor) {
        // ViewHolder design pattern so we do not have to call findViewById every time
        // we populate the list item
        final ViewHolder listItemView = (ViewHolder) view.getTag();

        // Find the columns of item attributes that we are interested in
        int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
        int idColumnIndex = cursor.getColumnIndex(ItemEntry._ID);
        int categoryColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_CATEGORY);

        // Read the item attributes from cursor for current item
        final String itemName = cursor.getString(nameColumnIndex);
        double itemPrice = cursor.getDouble(priceColumnIndex);
        int itemQuantity = cursor.getInt(quantityColumnIndex);
        long itemId = cursor.getLong(idColumnIndex);
        String itemCategory = cursor.getString(categoryColumnIndex);

        // If item quantity is below specified limit warn user by changing textColor of quantity
        // about low inventory
        if (itemQuantity > 0 && itemQuantity < 20) {
            listItemView.itemQuantityTextView
                    .setTextColor(ContextCompat.getColor(mContext, R.color.quantityValueBelowLimit));
        } else if (itemQuantity == 0) {
            // If item quantity is 0 warn user by changing textColor
            listItemView.itemQuantityTextView
                    .setTextColor(ContextCompat.getColor(mContext, R.color.quantityValueZero));
        } else {
            listItemView.itemQuantityTextView
                    .setTextColor(ContextCompat.getColor(mContext, R.color.primaryTextColor));
        }

        // Update the TextViews with the current item details
        listItemView.itemNameTextView.setText(itemName);
        listItemView.itemPriceTextView.setText("$ " + itemPrice);
        listItemView.itemQuantityTextView.setText(String.valueOf(itemQuantity));

        // Update the item category TextView if the category present
        if (TextUtils.isEmpty(itemCategory)) {
            listItemView.itemCategoryTextView.setVisibility(View.GONE);
        } else {
            listItemView.itemCategoryTextView.setVisibility(View.VISIBLE);
            listItemView.itemCategoryTextView.setText(itemCategory);
        }

        // Append id of a row in table with {@link ItemEntry#CONTENT_URI} to generate a appropriate
        // uri to update a database at given id, when user clicks decrease button
        final Uri currentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, itemId);

        // Find the button for decreasing the quantity in ListView with id : list_item_item_decrease_quantity_button
        // As button doesn't work on ListView, we have to set on click listener on decrease quantity
        // in Adapter view
        // Button decreaseQuantityButton = view.findViewById(R.id.list_item_item_decrease_quantity_button);
        // Attach a listener to button
        listItemView.decreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the item quantity and trim the leading and trailing white spaces
                String itemQuantityString = listItemView.itemQuantityTextView.getText().toString().trim();
                // Convert the string into Integer
                Integer itemQuantity = Integer.parseInt(itemQuantityString);
                // Decrease the value of quantity by 1 as long as it remains greater that 0
                if (itemQuantity > 0) {
                    itemQuantity = itemQuantity - 1;
                }

                // If item quantity is zero, make a snackBar and for ordering an item
                if (itemQuantity == 0) {
                    // Change the color of text to warn user
                    listItemView.itemQuantityTextView.setTextColor(ContextCompat.getColor(mContext, R.color.quantityValueZero));

                    // Make a snackBar to show message for making an order and also show
                    // action button for making an order
                    Snackbar snackbar = Snackbar.make(view.getRootView().findViewById(R.id.catalogCoordinatorLayout),
                            mContext.getString(R.string.catalog_make_an_order_for_item_snackBar) + " " + itemName,
                            Snackbar.LENGTH_SHORT);

                    // Set the snackBar action button string and start an intent to make
                    // an order for particular item
                    snackbar.setAction(R.string.catalog_make_an_order_snackBar_action_button,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(mContext, EditorActivity.class);
                                    intent.setData(currentItemUri);
                                    mContext.startActivity(intent);
                                }
                            });
                    // Display snackBar
                    snackbar.show();
                }

                final Integer finalItemQuantity = itemQuantity;
                // Update database for newly updated quantity in background thread
                // to minimize memory operation on UI thread
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        // Create a new content value object for itemQuantity
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(ItemEntry.COLUMN_ITEM_QUANTITY, finalItemQuantity);
                        // Update the database for just item quantity
                        mContext.getContentResolver()
                                .update(currentItemUri, contentValues, null, null);
                    }
                });

                // Set the text in quantity field
                listItemView.itemQuantityTextView.setText(String.valueOf(itemQuantity));
            }
        });
    }

    /**
     * ViewHolder for list item in res/layout/list_item.xml
     * View holder that cashes the views so we do not need to use findViewById every time
     * So scrolling of list view becomes more smooth
     */
    private static class ViewHolder {
        private TextView itemNameTextView;
        private TextView itemQuantityTextView;
        private TextView itemPriceTextView;
        private TextView itemCategoryTextView;
        private Button decreaseQuantityButton;
    }
}