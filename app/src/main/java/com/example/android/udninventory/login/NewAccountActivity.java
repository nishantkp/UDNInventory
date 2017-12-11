package com.example.android.udninventory.login;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.udninventory.R;
import com.example.android.udninventory.data.ItemContract.ItemEntry;

public class NewAccountActivity extends AppCompatActivity {

    /* TAG for log messages*/
    private static final String LOG_TAG = NewAccountActivity.class.getName();

    /* TextInput wrappers */
    private TextInputLayout mUserEmailWrapper;
    private TextInputLayout mUserNameWrapper;
    private TextInputLayout mUserPasswordWrapper;

    /* EditText fields for user information */
    private EditText mUserEmail;
    private EditText mUserName;
    private EditText mUserPassword;

    private Button mCreateAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);

        // Print credentials database on LOGCAT
        checkData();

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

        mCreateAccountButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(
                NewAccountActivity.this,
                R.style.LoginTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        String name = mUserName.getText().toString().trim();
        String email = mUserEmail.getText().toString().trim();
        String password = mUserPassword.getText().toString().trim();

        final ContentValues contentValues = new ContentValues();
        contentValues.put(ItemEntry.CREDENTIALS_TABLE_COLUMN_USER_NAME, name);
        contentValues.put(ItemEntry.CREDENTIALS_TABLE_COLUMN_EMAIL, email);
        contentValues.put(ItemEntry.CREDENTIALS_TABLE_COLUMN_PASSWORD, password);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Insert data into Credentials database in background thread
                Uri uri = getContentResolver().insert(ItemEntry.CREDENTIALS_CONTENT_URI, contentValues);
                try {
                    Thread.sleep(1500);
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
        mCreateAccountButton.setEnabled(true);
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
            mUserNameWrapper.setError("at least 3 letters");
            validData = false;
        } else {
            mUserNameWrapper.setErrorEnabled(false);
        }

        // If the email text field is empty or email address is not per EMAIL_ADDRESS pattern,
        // give user a error message
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mUserEmailWrapper.setError("enter valid email address");
            validData = false;
        } else {
            mUserEmailWrapper.setErrorEnabled(false);
        }

        // If the password text field is emory or password is not between 5 and 8 digits,
        // give user a error message
        if (TextUtils.isEmpty(password) || password.length() < 5 || password.length() > 8) {
            mUserPasswordWrapper.setError("between 5 and 8 digits");
            validData = false;
        } else {
            mUserEmailWrapper.setErrorEnabled(false);
        }
        return validData;
    }

    // This function is called to check the inserted credentials
    private void checkData() {
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.CREDENTIALS_TABLE_COLUMN_USER_NAME,
                ItemEntry.CREDENTIALS_TABLE_COLUMN_EMAIL,
                ItemEntry.CREDENTIALS_TABLE_COLUMN_PASSWORD};

        Cursor cursor = getContentResolver().query(ItemEntry.CREDENTIALS_CONTENT_URI
                , projection
                , null
                , null
                , null);

        assert cursor != null;
        while (cursor.moveToNext()) {
            int idCheck = cursor.getInt(cursor.getColumnIndex(ItemEntry._ID));
            String nameCheck = cursor.getString(cursor.getColumnIndex(ItemEntry.CREDENTIALS_TABLE_COLUMN_USER_NAME));
            String emailCheck = cursor.getString(cursor.getColumnIndex(ItemEntry.CREDENTIALS_TABLE_COLUMN_EMAIL));
            String passwordCheck = cursor.getString(cursor.getColumnIndex(ItemEntry.CREDENTIALS_TABLE_COLUMN_PASSWORD));
            Log.i(LOG_TAG, "\nid:" + idCheck + " Name:" + nameCheck + " Email:" + emailCheck + " Password:" + passwordCheck);
        }
        cursor.close();
    }
}
