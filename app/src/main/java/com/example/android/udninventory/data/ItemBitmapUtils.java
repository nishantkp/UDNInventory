package com.example.android.udninventory.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * Helper methods to deal with Bitmaps
 * Created by Nishant on 12/3/2017.
 */

public class ItemBitmapUtils {
    // Converts bitmap to byte array to store into database
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    // Converts byteImage into Bitmap in order to display into imageView in editor activity
    public static Bitmap getImage(byte[] byteImage) {
        return BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
    }
}
