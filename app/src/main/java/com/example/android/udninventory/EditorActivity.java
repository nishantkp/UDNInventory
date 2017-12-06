package com.example.android.udninventory;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.udninventory.data.ItemBitmapUtils;
import com.example.android.udninventory.data.ItemContract.ItemEntry;
import com.example.android.udninventory.data.Validation;

public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    /* TAG for log messages */
    private static final String LOG_TAG = EditorActivity.class.getName();
    /* Loader id for an existing loader */
    private static final int EXISTING_ITEM_LOADER = 0;
    /* This code will be return to onActivityResult() when activity exists */
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    /* TextInputLayout for item name */
    TextInputLayout itemNameWrapper;
    /* TextInputLayout for item quantity */
    TextInputLayout itemQuantityWrapper;
    /* TextInputLayout for item price */
    TextInputLayout itemPriceWrapper;
    /* EditText view to enter item name */
    private EditText mItemName;
    /* EditText view to enter item quantity */
    private EditText mItemQuantity;
    /* EditText view to enter Item price */
    private EditText mItemPrice;
    /* EditText view to enter item category*/
    private EditText mItemCategory;
    /* EditText view to enter supplier name */
    private EditText mSupplierName;
    /* EditText view to enter supplier contact information i.e Phone number */
    private EditText mSupplierPhone;
    /* EditText view to enter supplier email */
    private EditText mSupplierEmail;
    /* EditText view to enter new order quantity */
    private EditText mNewOrderQuantity;
    /* Button for decreasing the quantity of item by 1 */
    private Button mDecreaseQuantity;
    /* Button for increasing the quantity for item by 1*/
    private Button mIncreaseQuantity;
    /* Button for placing new order*/
    private Button mPlaceOrder;
    /* Stores the Uri received from MainActivity */
    private Uri mCurrentItemUri;
    /* Will be true if user updates the part of form */
    private boolean mItemHasChanged = false;
    /* ImageView for displaying photo of an item */
    private ImageView mItemThumbnail;
    /* Data flag to be set depending upon valid data */
    private boolean mValidDataFlag = true;
    /* Touch listener */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mItemHasChanged = true;
            return false;
        }
    };
    private Uri mNewlyInsertedRowUri = null;
    /* TextInputLayout for supplier email */
    private TextInputLayout supplierEmailWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_editor);

        // Examine the intent that was used to launch this activity
        // in order to figuring out we are creating a new item or editing an existing one
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        // If intent does not contain an item Uri, we know that we are creating a new item
        if (mCurrentItemUri == null) {
            // Set the app bar title of EditorActivity to "Add item"
            setTitle(getString(R.string.title_editor_activity_new_item));

            // Invalidate the options menu to hide the delete option
            // It doesn't make sense to delete item that hasn't been created yet
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing item, so change the app bar title to "Edit Item"
            setTitle(getString(R.string.title_editor_activity_edit_item));
            getSupportLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        // Find all the relevant views we need to take data from user
        mItemName = findViewById(R.id.item_editor_item_name);
        mItemQuantity = findViewById(R.id.item_editor_item_quantity);
        mItemPrice = findViewById(R.id.item_editor_item_price);
        mItemCategory = findViewById(R.id.item_editor_item_category);
        mSupplierName = findViewById(R.id.item_editor_supplier_name);
        mSupplierPhone = findViewById(R.id.item_editor_supplier_contact);
        mSupplierEmail = findViewById(R.id.item_editor_supplier_email);
        mIncreaseQuantity = findViewById(R.id.item_editor_increase_quantity);
        mDecreaseQuantity = findViewById(R.id.item_editor_decrease_quantity);

        // Find the wrapper for TextInputLayout with respective Ids
        itemNameWrapper = findViewById(R.id.item_editor_item_name_wrapper);
        itemQuantityWrapper = findViewById(R.id.item_editor_item_quantity_wrapper);
        itemPriceWrapper = findViewById(R.id.item_editor_item_price_wrapper);
        TextInputLayout itemCategoryWrapper = findViewById(R.id.item_editor_item_category_wrapper);
        TextInputLayout supplierNameWrapper = findViewById(R.id.item_editor_supplier_name_wrapper);
        TextInputLayout supplierContactWrapper = findViewById(R.id.item_editor_supplier_contact_wrapper);
        supplierEmailWrapper = findViewById(R.id.item_editor_supplier_email_wrapper);

        // Set the hint for all TextInputLayouts
        itemNameWrapper.setHint(getString(R.string.editor_text_input_layout_name_hint));
        itemQuantityWrapper.setHint(getString(R.string.editor_text_input_layout_quantity_hint));
        itemPriceWrapper.setHint(getString(R.string.editor_text_input_layout_price_hint));
        itemCategoryWrapper.setHint(getString(R.string.editor_text_input_layout_category_hint));
        supplierNameWrapper.setHint(getString(R.string.editor_text_input_layout_name_hint));
        supplierContactWrapper.setHint(getString(R.string.editor_text_input_layout_phone_hint));
        supplierEmailWrapper.setHint(getString(R.string.editor_text_input_layout_email_hint));

        mPlaceOrder = findViewById(R.id.item_editor_place_order);
        mNewOrderQuantity = findViewById(R.id.item_editor_new_order_quantity);

        // Button and ImageView for taking photo and displaying thumbnail
        mItemThumbnail = findViewById(R.id.item_editor_item_thumbnail);
        Button captureImage = findViewById(R.id.item_editor_take_photo);
        // Set the opacity of button background to make it transparent
        captureImage.getBackground().setAlpha(180);

        /* Attach a listener to all the EditText views of the form
        * so if the user touched the EditText view then the value of mItemHasChanged will be true
        * and we know that the details for item had changed and it can be helped in showing
        * AlertDialog */
        mItemName.setOnTouchListener(mTouchListener);
        mItemQuantity.setOnTouchListener(mTouchListener);
        mItemPrice.setOnTouchListener(mTouchListener);
        mItemCategory.setOnTouchListener(mTouchListener);
        mSupplierName.setOnTouchListener(mTouchListener);
        mSupplierPhone.setOnTouchListener(mTouchListener);
        mSupplierEmail.setOnTouchListener(mTouchListener);
        mIncreaseQuantity.setOnTouchListener(mTouchListener);
        mDecreaseQuantity.setOnTouchListener(mTouchListener);
        captureImage.setOnTouchListener(mTouchListener);

        /* Set on click listener on button for increasing quantity by 1 */
        mIncreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the quantity from EditText view and trim the leading and trailing white space
                String itemQuantityString = mItemQuantity.getText().toString().trim();
                // If user has already provided some quantity value,
                // increase the quantity by 1
                if (!TextUtils.isEmpty(itemQuantityString)) {
                    // Convert the string value into integer
                    Integer itemQuantity = Integer.parseInt(itemQuantityString);
                    itemQuantity += 1;
                    mItemQuantity.setText(itemQuantity + "");
                }
            }
        });

        /* Set on click listener on button for decreasing quantity by 1 */
        mDecreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the quantity from EditText view and trim the leading and trailing white space
                String itemQuantityString = mItemQuantity.getText().toString().trim();
                // If user has already provided some quantity value,
                // decrease the value by 1
                if (!TextUtils.isEmpty(itemQuantityString)) {
                    // Convert the string value into integer
                    Integer itemQuantity = Integer.parseInt(itemQuantityString);
                    // Decrease the quantity by 1 only if there is a value greater than 0
                    if (itemQuantity > 0) {
                        itemQuantity = itemQuantity - 1;
                    } else {
                        itemQuantity = 0;
                    }
                    mItemQuantity.setText(itemQuantity + "");
                }
            }
        });

        // Find the TextView for deleting item
        final TextView deleteItemTextView = (TextView) findViewById(R.id.item_editor_delete_item);
        FrameLayout deleteFrame = findViewById(R.id.item_editor_delete_item_frame);
        if (mCurrentItemUri == null) {
            // If it's a new Item then hide the while frame of Delete button
            deleteFrame.setVisibility(View.GONE);
        } else {
            // Otherwise warn for deleting item
            deleteItemTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmationDialog();
                }
            });
        }

        // Set on click listener on button for taking photo of item
        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the camera intent and take the photo
                takePicture();
            }
        });

        // Set listener on new order place button
        mPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = mNewOrderQuantity.getText().toString().trim();
                String supplierEmail = mSupplierEmail.getText().toString().trim();
                String itemName = mItemName.getText().toString().trim();
                // If we have a valid quantity and supplier email then place a new order for
                // given quantity
                if (!TextUtils.isEmpty(quantity) && !TextUtils.isEmpty(supplierEmail)) {
                    // Check if the supplier email is valid or not, then place order
                    // otherwise show error message indicating not valid email address
                    if (Validation.validateEmail(supplierEmail)) {
                        supplierEmailWrapper.setErrorEnabled(false);
                        placeNewOrder(quantity, supplierEmail, itemName);
                    } else {
                        supplierEmailWrapper.setError("Not valid email");
                    }

                } else {
                    if (TextUtils.isEmpty(quantity)
                            && TextUtils.isEmpty(supplierEmail)) {
                        // If user has not provided supplier mail and quantity, make a snakeBar
                        // to provide supplier email and quantity, and item name
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.myCoordinatorLayout)
                                , getResources().getString(R.string.editor_snackBar_no_supplier_email_no_quantity)
                                , Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    } else if (TextUtils.isEmpty(supplierEmail)) {
                        // If user has not provided supplier mail, make a snakeBar
                        // to provide supplier email
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.myCoordinatorLayout)
                                , getResources().getString(R.string.editor_snackBar_no_supplier_email)
                                , Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    } else {
                        // If user has not provided item quantity to order, make a snackBar
                        // to provide item quantity
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.myCoordinatorLayout)
                                , getResources().getString(R.string.editor_snackBar_no_item_quantity)
                                , Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.editor_save_item:
                saveItem();
                // If valid data flag is false, return without saving item
                if (!mValidDataFlag) {
                    return true;
                }
                /* If we are editing an existing item,and user clicks on save item button
                * start the intent to {@link OverviewActivity} with Uri of current
                * item to display the updated item on {@link OverviewActivity} */
                if (mCurrentItemUri != null) {
                    // Go to {@link OverviewActivity}
                    backToOverviewActivityIntent(mCurrentItemUri);
                } else {
                    // OTHERWISE we are creating a new item
                    if (mNewlyInsertedRowUri != null) {
                        // If user clicks the save button after entering details about item,
                        // send an intent with Uri of newly created row to {@link OverviewActivity}
                        backToOverviewActivityIntent(mNewlyInsertedRowUri);
                    } else {
                        // If user clicks save button, without inserting any details for item
                        // Go directly to parent activity {@link MainActivity}
                        finish();
                    }
                }
                return true;

            // Respond to the click on up arrow button on the app bar
            case android.R.id.home:
                // If item hasn't changed send current item Uri through
                // intent to {@link OverviewActivity}
                if (!mItemHasChanged && mCurrentItemUri != null) {
                    //NavUtils.navigateUpFromSameTask(this);
                    // Go to {@link OverviewActivity}
                    backToOverviewActivityIntent(mCurrentItemUri);
                    return true;
                }

                // If user is adding a new item, finish the activity and return to
                // {@link MainActivity}
                if (mCurrentItemUri == null) {
                    finish();
                    return true;
                }

                // Otherwise there are unsaved changes, setup dialog to warn users about it
                // Create a click listener to handle the user confirming that changes should be
                // discarded
                DialogInterface.OnClickListener discardButtonClickListener
                        = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked in discard button navigate up to parent activity
                        //NavUtils.navigateUpFromSameTask(EditorActivity.this);
                        // If we are creating a new item,
                        // so when user clicks on "Discard" button go to {@link MainActivity}
                        if (mCurrentItemUri == null) {
                            finish();
                        } else {
                            // OTHERWISE we are updating an item,
                            // so send Uri of current item through intent to {@link OverviewActivity}
                            backToOverviewActivityIntent(mCurrentItemUri);
                        }
                    }
                };
                // Show the dialog that notifies user about unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    //when user click on check option, insert the item into database
    private void saveItem() {
        /* Read the input string and trim the leading and trailing white spaces */
        /* Get the name of item from EditText view */
        String itemNameString = mItemName.getText().toString().trim();

        /* Get the quantity of item from EditText view */
        String itemQuantityString = mItemQuantity.getText().toString().trim();

        /* Get the price of item from EditText view */
        String itemPriceString = mItemPrice.getText().toString().trim();

        /* Get the item category from EditText view */
        String itemCategoryString = mItemCategory.getText().toString().trim();

        /* Get the name of supplier from EditText view */
        String supplierNameString = mSupplierName.getText().toString().trim();

        /* Get the phone number of supplier from EditText view */
        String supplierPhoneString = mSupplierPhone.getText().toString().trim();

        /* Get the email address of supplier from EditText view */
        String supplierEmailString = mSupplierEmail.getText().toString().trim();

        /* Get the bitmap from Image view */
        Bitmap bitmapThumbnail = ((BitmapDrawable) mItemThumbnail.getDrawable()).getBitmap();

        /* If user hit save button by accident without actually giving any data for the item,
        * do not add empty item into the database and return */
        if (mCurrentItemUri == null
                && TextUtils.isEmpty(itemNameString)
                && TextUtils.isEmpty(itemQuantityString)
                && TextUtils.isEmpty(itemPriceString)) {
            return;
        }

        /* If we are creating a new item and entered data is not valid, show error messages
        * to appropriate EditText to avoid updating database with incorrect values */
        if (mCurrentItemUri == null) {
            if (!validateData()) {
                // Set the valid data flag to false
                mValidDataFlag = false;
                return;
            }
        }

        // If user has provided email address and if it's not valid then show the error message
        if (!Validation.validateEmail(supplierEmailString) && Validation.isInputDataPresent(supplierEmailString)) {
            supplierEmailWrapper.setError(getString(R.string.editor_supplier_email_not_valid));
            mValidDataFlag = false;
            return;
        } else {
            supplierEmailWrapper.setErrorEnabled(false);
        }
        // Set the valid data flag to true
        mValidDataFlag = true;

        int itemQuantity;
        // If user has not provided the quantity, do not parse the quantity string into integer
        // use 0 instead
        if (!TextUtils.isEmpty(itemQuantityString)) {
            // Parse the string into integer
            itemQuantity = Integer.parseInt(itemQuantityString);
        } else {
            itemQuantity = 0;
        }

        double itemPrice;
        // If user has not provided the price, do not parse the price string into double
        // use 0.00 instead
        if (!TextUtils.isEmpty(itemPriceString)) {
            itemPrice = Double.parseDouble(itemPriceString);
        } else {
            itemPrice = 0.00;
        }

        // Convert the bitmap image into bytes in order to store into database
        byte[] mByteImage = ItemBitmapUtils.getBytes(bitmapThumbnail);

        /* ContentValue object to add data into inventory.db */
        ContentValues contentValues = new ContentValues();
        contentValues.put(ItemEntry.COLUMN_ITEM_NAME, itemNameString);
        contentValues.put(ItemEntry.COLUMN_ITEM_PRICE, itemPrice);
        contentValues.put(ItemEntry.COLUMN_ITEM_QUANTITY, itemQuantity);
        contentValues.put(ItemEntry.COLUMN_ITEM_CATEGORY, itemCategoryString);
        contentValues.put(ItemEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        contentValues.put(ItemEntry.COLUMN_SUPPLIER_PHONE, supplierPhoneString);
        contentValues.put(ItemEntry.COLUMN_SUPPLIER_EMAIL, supplierEmailString);
        contentValues.put(ItemEntry.COLUMN_ITEM_THUMBNAIL, mByteImage);

        if (mCurrentItemUri == null) {
            // Insert a new item into provider, and it returns the uri of newly inserted item
            mNewlyInsertedRowUri = getContentResolver().insert(ItemEntry.CONTENT_URI, contentValues);
            Log.i(LOG_TAG, "Data inserted with uri : " + mNewlyInsertedRowUri);

            // Show Toast message whether the insertion was successful or not
            if (mNewlyInsertedRowUri == null) {
                // If uri is null, then there is an error with insertion
                Toast.makeText(this, R.string.error_saving_item, Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise insertion is successful
                Toast.makeText(this, R.string.successful_saving_item, Toast.LENGTH_SHORT).show();
            }
        } else {
            // OTHERWISE this is an existing pet so update the item with uri : mCurrentItemUri
            // and pass in new ContentValues. Pass null for the selection and selection arguments
            // as mCurrentItemUri will already identity which row in database we want to update
            int rowsUpdated = getContentResolver().update(mCurrentItemUri, contentValues, null, null);
            // Show the toast message depending on whether or not update was successful
            if (rowsUpdated == 0) {
                // If no rows are updated then there is an error with update
                Toast.makeText(this, getString(R.string.error_updating_item),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise saving an item is successful
                Toast.makeText(this, getString(R.string.successful_updating_item),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * This method is called to verify data inputted by user
     *
     * @return true if data is valid, false if data is not valid
     */
    private boolean validateData() {
        /* Read the input string and trim the leading and trailing white spaces */
        /* Get the name of item from EditText view */
        String itemNameString = mItemName.getText().toString().trim();

        /* Get the quantity of item from EditText view */
        String itemQuantityString = mItemQuantity.getText().toString().trim();

        /* Get the price of item from EditText view */
        String itemPriceString = mItemPrice.getText().toString().trim();

        /* Get the email address of supplier from EditText view */
        String supplierEmailString = mSupplierEmail.getText().toString().trim();

        // If user has not provided any name, then give error message
        if (!Validation.isInputDataPresent(itemNameString)) {
            itemNameWrapper.setError(getString(R.string.editor_item_name_required));
            return false;
        } else {
            itemNameWrapper.setErrorEnabled(false);
        }

        // If user has not provided item price, then give error message
        if (!Validation.isInputDataPresent(itemPriceString)) {
            itemPriceWrapper.setError(getString(R.string.editor_item_price_required));
            return false;
        } else {
            itemPriceWrapper.setErrorEnabled(false);
        }

        // If user has not provided quantity, then give error message
        if (!Validation.isInputDataPresent(itemQuantityString)) {
            itemQuantityWrapper.setError(getString(R.string.editor_item_quantity_required));
            return false;
        } else {
            itemQuantityWrapper.setErrorEnabled(false);
        }

        // If user has not given the valid email address, set the error message and exit the method,
        // without updating database
        if (!Validation.validateEmail(supplierEmailString) && Validation.isInputDataPresent(supplierEmailString)) {
            supplierEmailWrapper.setError(getString(R.string.editor_supplier_email_not_valid));
            return false;
        } else {
            supplierEmailWrapper.setErrorEnabled(false);
        }

        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all the attributes, define projection that
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
            byte[] itemImageBytes = data.getBlob(itemThumbnailIndex);

            // Set the text to appropriate EditText views with values in database
            mItemName.setText(itemNameString);
            mItemQuantity.setText(itemQuantityString);
            mItemPrice.setText(itemPriceString);
            mItemCategory.setText(itemCategoryString);
            mSupplierName.setText(supplierNameString);
            mSupplierPhone.setText(supplierPhoneString);
            mSupplierEmail.setText(supplierEmailString);

            Bitmap bitmapImage;
            // If image bytes are not null, the convert image bytes into Bitmap Image
            // and set the image into ImageView
            if (itemImageBytes != null) {
                bitmapImage = ItemBitmapUtils.getImage(itemImageBytes);
                mItemThumbnail.setImageBitmap(bitmapImage);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mItemName.setText(null);
        mItemPrice.setText(null);
        mItemQuantity.setText(null);
        mItemCategory.setText(null);
        mSupplierName.setText(null);
        mSupplierPhone.setText(null);
        mSupplierEmail.setText(null);
    }

    private void showDeleteConfirmationDialog() {
        // Create the AlertDialog.Builder and set the message and attach listeners to
        // positive and negative buttons on dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.editor_alert_dialog_delete_item_message);
        builder.setPositiveButton(R.string.editor_alert_dialog_delete_positive_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked on "Delete" button so delete the item
                        deleteItem();
                    }
                });

        builder.setNegativeButton(R.string.editor_alert_dialog_delete_negative_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked in "Cancel" button so hide the dialog without doing anything
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

        // Create and show alert dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform deletion of pet in database
     */
    private void deleteItem() {
        if (mCurrentItemUri != null) {
            // Get the content resolver and delete the Item at given content uri
            // Pass in null for both selection and selectionArgs because content uri knows which
            // item to delete
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, "Error deleting item", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
            }
            NavUtils.navigateUpFromSameTask(EditorActivity.this);
        }
    }

    @Override
    public void onBackPressed() {
        // We are viewing current item and the item hasn't changed,
        // send the Uri of current item through intent to
        // {@link OverviewAActivity} to display overview of an item
        if (!mItemHasChanged && mCurrentItemUri != null) {
            backToOverviewActivityIntent(mCurrentItemUri);
            return;
        }

        // If we are creating a new item and user clicks back button,
        // go directly to parent activity {@link MainActivity}
        if (mCurrentItemUri == null) {
            finish();
            return;
        }
        // Otherwise there are unsaved changes, so setup a dialog to warn user
        // Create a listener to handle the user confirming that changes should be discarded
        AlertDialog.OnClickListener discardChangesListener
                = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // If we are creating a new item,
                // so when user clicks on "Discard" button then go to {@link MainActivity}
                if (mCurrentItemUri == null) {
                    finish();
                } else {
                    // OTHERWISE we are updating an item,
                    // so when user clicks on "Discard" button go to {@link OverviewActivity}
                    backToOverviewActivityIntent(mCurrentItemUri);
                }
            }
        };

        // Show dialog that, there are unsaved changes
        showUnsavedChangesDialog(discardChangesListener);
    }

    private void showUnsavedChangesDialog(AlertDialog.OnClickListener discardChangesListener) {
        // Create a dialog, set message and attach listeners to positive and negative buttons
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.editor_alert_dialog_unsaved_changes_message);
        builder.setPositiveButton(R.string.editor_alert_dialog_unsaved_changes_positive_button,
                discardChangesListener);
        builder.setNegativeButton(R.string.editor_alert_dialog_unsaved_changes_negative_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Is user clicks on "Keep editing" option, dismiss the dialog and
                        // continue editing item
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Start a new intent to take photo
    // resolverActivity() returns the first activity component that can handle the intent
    // This check is necessary, if you call startActivityForResult() using an intent that no app
    // can handle, the app will crash
    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    // The android camera application encodes the photo, in the return Intent delivered to
    // onActivityResult() as a small Bitmap in the extras, under key "data"
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap bitmapImage = (Bitmap) extras.get("data");
            // Display the photo in ImageView
            mItemThumbnail.setImageBitmap(bitmapImage);
        }
    }

    // Starts a new intent to place order
    private void placeNewOrder(String quantity, String email, String itemName) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        // Supplier email
        emailIntent.setData(Uri.parse("mailto:" + email));

        // Email subject line
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "New order for " + itemName);

        // Email body
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Good day,\nI would like to place an order for "
                + itemName + " for total quantity of "
                + quantity + "." + "\n\nThanks and have a great day!");

        // Check there is an email app available on device to handle intent,
        // if not then don't start the activity
        // This prevent the app from crashing
        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(emailIntent);
        }
    }

    /* Send Uri of current item through intent to {@link OverviewActivity}
    * to display overview of an item */
    private void backToOverviewActivityIntent(Uri uri) {
        // Create a new intent to go to {@link OverviewActivity} to display overview
        Intent intent = new Intent(EditorActivity.this, OverviewActivity.class);
        // Set uri as data field for Intent
        intent.setData(uri);
        // Launch the {@link OverviewActivity} to display the current pet
        startActivity(intent);
    }
}