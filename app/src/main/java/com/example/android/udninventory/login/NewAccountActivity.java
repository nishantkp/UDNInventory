package com.example.android.udninventory.login;

import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.udninventory.R;
import com.example.android.udninventory.data.ItemContract.ItemEntry;

public class NewAccountActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /* TAG for log messages*/
    private static final String LOG_TAG = NewAccountActivity.class.getName();
    /* Loader id for creating a new account */
    private static final int CREATE_NEW_ACCOUNT_LOADER_ID = 1;
    /* TextInput wrappers */
    private TextInputLayout mUserEmailWrapper;
    private TextInputLayout mUserNameWrapper;
    private TextInputLayout mUserPasswordWrapper;
    /* EditText fields for user information */
    private EditText mUserEmail;
    private EditText mUserName;
    private EditText mUserPassword;
    private Button mCreateAccountButton;
    /* Flag to check email address provided by user exists in the database or not */
    private boolean mEmailExistsFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);

        /* Find all the views with respective Ids */
        mUserEmailWrapper = findViewById(R.id.new_user_email_edit_text_wrapper);
        mUserNameWrapper = findViewById(R.id.new_user_name_edit_text_wrapper);
        mUserPasswordWrapper = findViewById(R.id.new_user_password_edit_text_wrapper);
        mUserEmail = findViewById(R.id.new_user_email_edit_text);
        mUserName = findViewById(R.id.new_user_name_edit_text);
        mUserPassword = findViewById(R.id.new_user_password_edit_text);

        mCreateAccountButton = findViewById(R.id.new_user_create_account_button);
        mCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Hide the keyboard as soon as user hit the 'CREATE ACCOUNT' button
                if (view != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    assert inputMethodManager != null;
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                // When user clicks on 'CREATE ACCOUNT', take the input provided by user,
                // validate it and create a new account
                createNewAccount();
            }
        });

        TextView loginExistingAccount = findViewById(R.id.new_user_login_link_existing_user);
        loginExistingAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginExistingAccountIntent = new Intent(NewAccountActivity.this, MainLoginActivity.class);
                startActivity(loginExistingAccountIntent);
            }
        });
    }

    /**
     * Disable going back to {@link MainLoginActivity}
     */
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    /**
     * Create new account when user clicks on `CREATE ACCOUNT` button
     */
    private void createNewAccount() {

        // if user has not provided valid data, do not create an account and return from method
        if (!validateData()) {
            return;
        }

        // If flag value is false, that means email provided by user doesn't exists in database
        // so initialize the loader to create a new account
        if (!mEmailExistsFlag) {
            getLoaderManager().initLoader(CREATE_NEW_ACCOUNT_LOADER_ID, null, this);
        } else {
            // If flag is true, that means email provided by user already exists in database
            // so restart the loader and allow user to enter new email address for account
            getLoaderManager().restartLoader(CREATE_NEW_ACCOUNT_LOADER_ID, null, this);
        }

    }

    /**
     * This is method is called to valid the data inserted by user
     *
     * @return true if data inserted by user is valid, false if it's invalid
     */
    private boolean validateData() {
        boolean validData = true;

        String email = mUserEmail.getText().toString().trim();
        String name = mUserName.getText().toString().trim();
        String password = mUserPassword.getText().toString();

        // If the Name text field is empty or Name is less then 3 characters,
        // give user a error message
        if (TextUtils.isEmpty(name) || name.length() < 3) {
            mUserNameWrapper.setError(getString(R.string.text_layout_invalid_name));
            validData = false;
        } else {
            mUserNameWrapper.setErrorEnabled(false);
        }

        // If the email text field is empty or email address is not per EMAIL_ADDRESS pattern,
        // give user a error message
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mUserEmailWrapper.setError(getString(R.string.text_layout_invalid_email));
            validData = false;
        } else {
            mUserEmailWrapper.setErrorEnabled(false);
        }

        // If the password text field is emory or password is not between 5 and 8 digits,
        // give user a error message
        if (TextUtils.isEmpty(password) || password.length() < 5 || password.length() > 8) {
            mUserPasswordWrapper.setError(getString(R.string.text_layout_invalid_password));
            validData = false;
        } else {
            mUserEmailWrapper.setErrorEnabled(false);
        }
        return validData;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since we have to make sure that user can not enter the same email address twice
        // while creating a new account, so set the projection for email address column
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.CREDENTIALS_TABLE_COLUMN_EMAIL};

        // where clause for email address column, because we want to check if the email provided by
        // user is present in the email column or not
        String selection = ItemEntry.CREDENTIALS_TABLE_COLUMN_EMAIL + "=?";

        // Email provided by user when creating a new account
        String email = mUserEmail.getText().toString().trim();
        String[] selectionArgs = new String[]{email};

        return new CursorLoader(
                this,                       // Context of the app
                ItemEntry.CREDENTIALS_CONTENT_URI,  // Uri to query Credentials table
                projection,                         // Projection of which column of table we are interested in
                selection,                          // Where clause
                selectionArgs,                       // Selection args
                null);                     // Sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // If email address is present in the table, set the error message about email address
        // already exists in database
        if (cursor.moveToFirst()) {
            mUserEmailWrapper.setError("Email address already exists");
            mEmailExistsFlag = true;
        } else {
            mUserEmailWrapper.setErrorEnabled(false);
            insertUserDetails();
            mEmailExistsFlag = false;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    /**
     * This method is called to create a new account by
     * inserting details provided by used into database
     */
    private void insertUserDetails() {
        // Progress dialog for better user experience
        final ProgressDialog progressDialog = new ProgressDialog(
                NewAccountActivity.this,
                R.style.LoginTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();
        // Prevent Progress dialog from dismissing when user click on rest of the screen
        progressDialog.setCancelable(false);

        // Get the text provided by user
        String name = mUserName.getText().toString().trim();
        String email = mUserEmail.getText().toString().trim();
        String password = mUserPassword.getText().toString().trim();
        String tableName = email.replace(".", "_");
        tableName = tableName.replace("@", "_");
        tableName = tableName.replace("-", "_");

        // Content value object to insert data entered by user into database
        final ContentValues contentValues = new ContentValues();
        contentValues.put(ItemEntry.CREDENTIALS_TABLE_COLUMN_USER_NAME, name);
        contentValues.put(ItemEntry.CREDENTIALS_TABLE_COLUMN_EMAIL, email);
        contentValues.put(ItemEntry.CREDENTIALS_TABLE_COLUMN_PASSWORD, password);
        contentValues.put(ItemEntry.CREDENTIALS_TABLE_USER_INVENTORY_TABLE, tableName);

        // Run AsyncTask to add data into database in background thread,
        // without disturbing UI thread
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Insert data into Credentials database
                Uri uri = getContentResolver().insert(ItemEntry.CREDENTIALS_CONTENT_URI, contentValues);
                try {
                    // Put thread to sleep for 2seconds to show user a progress dialog
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                // If inserting data was successful, finish the activity
                if (uri != null) {
                    finish();
                }
            }
        });
    }
}
