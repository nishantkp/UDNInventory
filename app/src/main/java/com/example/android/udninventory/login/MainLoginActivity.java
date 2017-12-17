package com.example.android.udninventory.login;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.udninventory.Constants.PublicKeys;
import com.example.android.udninventory.InventoryListActivity;
import com.example.android.udninventory.R;
import com.example.android.udninventory.data.ItemContract.ItemEntry;
import com.example.android.udninventory.data.ItemDbHelper;

import java.lang.ref.WeakReference;

public class MainLoginActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /* Loader id for starting a loader when user hits login button */
    private static final int LOGIN_LOADER_ID = 1;
    /* TextInput Wrappers */
    private TextInputLayout mUserEmailWrapper;
    private TextInputLayout mUserPasswordWrapper;
    /* EditText fields */
    private EditText mUserEmail;
    private EditText mUserPassword;
    /* Login button */
    private Button mLoginButton;
    /* Coordinator layout to display snack bar */
    private CoordinatorLayout mCoordinatorLayout;
    /* Flag to check log is failed ot not */
    private boolean LOGIN_FAILED_FLAG = false;
    /* Cursor to store credential information received from querying credentials
    table with user entered email */
    private Cursor mCredentialData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_login);

        // Find all views with respective IDs
        mUserEmailWrapper = findViewById(R.id.login_email_edit_text_wrapper);
        mUserPasswordWrapper = findViewById(R.id.login_password_edit_text_wrapper);
        mUserEmail = findViewById(R.id.login_email_edit_text);
        mUserPassword = findViewById(R.id.login_password_edit_text);
        mCoordinatorLayout = findViewById(R.id.login_coordinator_layout);

        mLoginButton = findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Hide the keyboard as soon as user hit the 'LOGIN' button
                if (view != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    assert inputMethodManager != null;
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                // When user clicks on 'LOGIN', take the input provided by user,
                // validate it and log into an existing account
                login();
            }
        });

        TextView createNewAccount = findViewById(R.id.login_link_create_new_account);
        createNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createNewAccountIntent = new Intent(MainLoginActivity.this, NewAccountActivity.class);
                startActivity(createNewAccountIntent);
            }
        });
    }

    /**
     * Disable going back to {@link NewAccountActivity}
     */
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    /**
     * When user clicks the login button, get the inputs provided by user and validate them,
     * and login them
     */
    private void login() {
        // If data entered by user is not valid, exit the method
        if (!validateData()) {
            return;
        }

        // Start the loader
        getSupportLoaderManager().initLoader(LOGIN_LOADER_ID, null, this);
        // If LOGIN_FAILED_FLAG is true, that means user has inserted incorrect user name/id/email or password
        // So when user again enters the credentials and hit enter, restart the loader
        if (LOGIN_FAILED_FLAG) {
            getSupportLoaderManager().restartLoader(LOGIN_LOADER_ID, null, this);
        }
    }

    /**
     * This method is used to validate data provided by user
     *
     * @return true and false depending upon valid data
     */
    private boolean validateData() {
        boolean validData = true;

        String email = mUserEmail.getText().toString().trim();
        String password = mUserPassword.getText().toString();

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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since we need to match User name and password, define a projection that contains
        // all the parameters that we required
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.CREDENTIALS_TABLE_COLUMN_USER_NAME,
                ItemEntry.CREDENTIALS_TABLE_COLUMN_EMAIL,
                ItemEntry.CREDENTIALS_TABLE_COLUMN_PASSWORD,
                ItemEntry.CREDENTIALS_TABLE_USER_INVENTORY_TABLE};

        // Where clause, because we are interested in row from table
        // which contains an email provided by user
        String selection = ItemEntry.CREDENTIALS_TABLE_COLUMN_EMAIL + "=?";

        // Get the email provided by user and pass that email as selectionArd
        String email = mUserEmail.getText().toString().trim();
        String[] selectionArgs = new String[]{email};

        return new CursorLoader(
                this,                       // Context of app
                ItemEntry.CREDENTIALS_CONTENT_URI,  // URI for whole credential table
                projection,                         // Projection of which column from table that we are interested in
                selection,                          // Where clause
                selectionArgs,                      // Selection arguments
                null);                     // Sort order to null
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
        // checkCredentialsAgainstDatabaseAndLogin(data);
        mCredentialData = data;
        // Start AsyncTask to validate used credentials
        new CheckCredentialsAgainstDatabaseAndLogin(this).execute();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    /**
     * This method is called to check credentials such as user email and password against the values
     * received by performing query on database
     * And finally upon successful credential verification, log user in to their table by starting
     * {@link InventoryListActivity}
     *
     * @param data cursor object received from LoaderCallbacks
     */
    @SuppressLint("StaticFieldLeak")
    private void checkCredentialsAgainstDatabaseAndLogin(final Cursor data) {
        // Perform cursor operations on background thread
        new AsyncTask<Void, Void, ContentValues>() {
            @Override
            protected ContentValues doInBackground(Void... voids) {
                ContentValues contentValues = new ContentValues();
                // If count is <= 0, than means there isn't any row in the database which contains
                // the email address provided by user
                if (data.getCount() <= 0) {
                    contentValues.put(PublicKeys.LOGIN_FAILED_KEY, true);
                    // close the cursor after its usage
                    data.close();
                    return contentValues;
                }
                // If there is a next row present then move to that row, because that row contains the
                // email, password and table name for particular user
                if (data.moveToNext()) {
                    if (data.isFirst()) {
                        data.moveToFirst();
                        contentValues.put(PublicKeys.LOGIN_FAILED_KEY, false);
                        int passwordIndex = data.getColumnIndex(ItemEntry.CREDENTIALS_TABLE_COLUMN_PASSWORD);
                        int tableNameIndex = data.getColumnIndex(ItemEntry.CREDENTIALS_TABLE_USER_INVENTORY_TABLE);
                        String password = data.getString(passwordIndex);
                        contentValues.put(PublicKeys.USER_PASSWORD_KEY, password);
                        // Get the table name, which we need to send to main activity to create new table with that name
                        // to create inventory database for user
                        String tableName = data.getString(tableNameIndex);
                        contentValues.put(PublicKeys.USER_TABLE_NAME_KEY, tableName);
                    }
                }
                // close the cursor after its use
                data.close();
                return contentValues;
            }

            @Override
            protected void onPostExecute(ContentValues contentValues) {
                if (contentValues.containsKey(PublicKeys.LOGIN_FAILED_KEY)) {
                    if (contentValues.getAsBoolean(PublicKeys.LOGIN_FAILED_KEY)) {
                        Snackbar.make(mCoordinatorLayout, getString(R.string.login_user_name_password_incorrect), Snackbar.LENGTH_SHORT).show();
                        // Set the LOGIN_FAILED_FLAG to true
                        LOGIN_FAILED_FLAG = true;
                    } else {
                        String userEnteredPassword = mUserPassword.getText().toString().trim();
                        if (contentValues.getAsString(PublicKeys.USER_PASSWORD_KEY).equals(userEnteredPassword)) {
                            // Remove the email id and password from their respective fields
                            mUserEmail.setText(null);
                            mUserPassword.setText(null);
                            // Set the LOGIN_FAILED_FLAG to false
                            LOGIN_FAILED_FLAG = false;
                            // Show progress dialog for better user experience
                            final ProgressDialog progressDialog = new ProgressDialog(
                                    MainLoginActivity.this,
                                    R.style.LoginTheme_Dark_Dialog);
                            progressDialog.setIndeterminate(true);
                            progressDialog.setMessage(getString(R.string.login_authenticating));
                            progressDialog.show();
                            // Prevent Progress dialog from dismissing when user click on rest of the screen
                            progressDialog.setCancelable(false);
                            // Show dialog for 2.5s and dismiss it
                            new android.os.Handler().postDelayed(
                                    new Runnable() {
                                        public void run() {
                                            progressDialog.dismiss();
                                        }
                                    }, 2500);
                            // Set the table name in ItemDbHelper for user who just successfully login
                            // into account which will be user to create a database table or performing CRUD
                            // operations
                            ItemDbHelper.setNewTableName(contentValues.getAsString(PublicKeys.USER_TABLE_NAME_KEY));
                            Intent loginToUserSpecificDatabase = new Intent(MainLoginActivity.this, InventoryListActivity.class);
                            startActivity(loginToUserSpecificDatabase);
                        } else {
                            // Set the LOGIN_FAILED_FLAG to true
                            LOGIN_FAILED_FLAG = true;
                            Snackbar.make(mCoordinatorLayout, getString(R.string.login_user_name_password_incorrect), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                }
            }
        }.execute();
    }

    /**
     * Private class to check credentials such as user email ans password against the values
     * received by performing query on database
     * And finally upon successful credential verification, log user in to their table by starting
     * {@link InventoryListActivity}
     */
    private static class CheckCredentialsAgainstDatabaseAndLogin extends AsyncTask<Void, Void, ContentValues> {

        /* Stores the week reference to activity */
        private WeakReference<MainLoginActivity> activityReference;

        // Only retain a weak reference to the activity
        CheckCredentialsAgainstDatabaseAndLogin(MainLoginActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected ContentValues doInBackground(Void... voids) {

            // get a reference to the activity if it is still there
            MainLoginActivity activity = activityReference.get();

            ContentValues contentValues = new ContentValues();
            // If count is <= 0, than means there isn't any row in the database which contains
            // the email address provided by user
            if (activity.mCredentialData.getCount() <= 0) {
                contentValues.put(PublicKeys.LOGIN_FAILED_KEY, true);
                // close the cursor after its usage
                activity.mCredentialData.close();
                return contentValues;
            }
            // If there is a next row present then move to that row, because that row contains the
            // email, password and table name for particular user
            if (activity.mCredentialData.moveToNext()) {
                if (activity.mCredentialData.isFirst()) {
                    activity.mCredentialData.moveToFirst();
                    contentValues.put(PublicKeys.LOGIN_FAILED_KEY, false);
                    int passwordIndex = activity.mCredentialData.getColumnIndex(ItemEntry.CREDENTIALS_TABLE_COLUMN_PASSWORD);
                    int tableNameIndex = activity.mCredentialData.getColumnIndex(ItemEntry.CREDENTIALS_TABLE_USER_INVENTORY_TABLE);
                    String password = activity.mCredentialData.getString(passwordIndex);
                    contentValues.put(PublicKeys.USER_PASSWORD_KEY, password);
                    // Get the table name, which we need to send to main activity to create new table with that name
                    // to create inventory database for user
                    String tableName = activity.mCredentialData.getString(tableNameIndex);
                    contentValues.put(PublicKeys.USER_TABLE_NAME_KEY, tableName);
                }
            }
            // close the cursor after its use
            activity.mCredentialData.close();
            return contentValues;
        }

        @Override
        protected void onPostExecute(ContentValues contentValues) {
            // get a reference to the activity if it is still there
            MainLoginActivity activity = activityReference.get();
            if (contentValues.containsKey(PublicKeys.LOGIN_FAILED_KEY)) {
                if (contentValues.getAsBoolean(PublicKeys.LOGIN_FAILED_KEY)) {
                    Snackbar.make(activity.mCoordinatorLayout, activity.getString(R.string.login_user_name_password_incorrect), Snackbar.LENGTH_SHORT).show();
                    // Set the LOGIN_FAILED_FLAG to true
                    activity.LOGIN_FAILED_FLAG = true;
                } else {
                    String userEnteredPassword = activity.mUserPassword.getText().toString().trim();
                    if (contentValues.getAsString(PublicKeys.USER_PASSWORD_KEY).equals(userEnteredPassword)) {
                        // Remove the email id and password from their respective fields
                        activity.mUserEmail.setText(null);
                        activity.mUserPassword.setText(null);
                        // Set the LOGIN_FAILED_FLAG to false
                        activity.LOGIN_FAILED_FLAG = false;
                        // Show progress dialog for better user experience
                        final ProgressDialog progressDialog = new ProgressDialog(
                                activity,
                                R.style.LoginTheme_Dark_Dialog);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage(activity.getString(R.string.login_authenticating));
                        progressDialog.show();
                        // Prevent Progress dialog from dismissing when user click on rest of the screen
                        progressDialog.setCancelable(false);
                        // Show dialog for 2.5s and dismiss it
                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        progressDialog.dismiss();
                                    }
                                }, 2500);
                        // Set the table name in ItemDbHelper for user who just successfully login
                        // into account which will be user to create a database table or performing CRUD
                        // operations
                        ItemDbHelper.setNewTableName(contentValues.getAsString(PublicKeys.USER_TABLE_NAME_KEY));
                        Intent loginToUserSpecificDatabase = new Intent(activity, InventoryListActivity.class);
                        activity.startActivity(loginToUserSpecificDatabase);
                    } else {
                        // Set the LOGIN_FAILED_FLAG to true
                        activity.LOGIN_FAILED_FLAG = true;
                        Snackbar.make(activity.mCoordinatorLayout, activity.getString(R.string.login_user_name_password_incorrect), Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}
