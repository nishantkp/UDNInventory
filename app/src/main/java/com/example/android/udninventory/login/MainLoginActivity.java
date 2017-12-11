package com.example.android.udninventory.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.udninventory.R;

public class MainLoginActivity extends AppCompatActivity {

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
                    InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
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

        mLoginButton.setEnabled(false);

        // Show progress dialog for better user experience
        final ProgressDialog progressDialog = new ProgressDialog(MainLoginActivity.this,
                R.style.LoginTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();
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
}
