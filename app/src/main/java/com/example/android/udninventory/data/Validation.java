package com.example.android.udninventory.data;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validation class to validate user input
 * Created by Nishant on 12/4/2017.
 */

public class Validation {


    /* Email address validation patterns */
    private static final String EMAIL_PATTERN = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

    public Validation() {
        // Empty constructor so no one can accidentally initialize it
    }

    /**
     * This method is called to check whether the email address provided by user is valid or not
     *
     * @param email email string inputted by user
     * @return true is it's valid email address, false if it's not valid email address
     */
    public static boolean validateEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * This method is called to check whether the input data entered by user is valid or not
     * i.e it't not consists of blank spaces only
     * So we don't enter blank data into database
     *
     * @param inputData input by user
     * @return true if the data is present, false if the data is not present
     */
    public static boolean isInputDataPresent(String inputData) {
        return !TextUtils.isEmpty(inputData);
    }
}
