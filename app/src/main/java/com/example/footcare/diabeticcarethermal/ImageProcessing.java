package com.example.footcare.diabeticcarethermal;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Kyle on 30/03/2017.
 */

public class ImageProcessing {

    /*
    * Image for My Motivation is too big to load so find the inSampleSize
    * so that memory can be allocated these methods are from:
    * https://developer.android.com/topic/performance/graphics/load-bitmap.html#read-bitmap
    * */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * Taken from: http://stackoverflow.com/questions/30789116/implementing-a-file-picker-in-android-and-copying-the-selected-file-to-another-l
     * also: http://stackoverflow.com/questions/15428975/save-bitmap-into-file-and-return-file-having-bitmap-image
     * Takes the Uri source and saves it locally as a file now redundant
     * */
    public static String copyFile(Uri source, ContentResolver cr) throws IOException {

        Bitmap bitmap = MediaStore.Images.Media.getBitmap(cr, source);

        OutputStream out = null;

        String filename = source.getLastPathSegment();
        filename = filename.substring(filename.lastIndexOf(":") + 1);
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/FootImages/");
        if (!folder.exists()) {
            folder.mkdir();
        }
        File destination = new File(folder, filename + ".jpeg");

        Log.d("Dest Absolute Path: ", destination.getAbsoluteFile().getAbsolutePath());
        Log.d("Dest Name: ", destination.getName());

        try {
            out = new FileOutputStream(destination);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // jpg is your Bitmap instance
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "file://" + destination.toString();
    }
    /**
     * Checks if permission exists to read and manage documents
     * */
    public static boolean checkPermission(Context context){
        String permissions[] = {"Manifest.permission.MANAGE_DOCUMENTS","Manifest.permission.READ_EXTERNAL_STORAGE"};
        int perm = context.checkCallingOrSelfPermission(permissions[0]);
        int perm2 = context.checkCallingOrSelfPermission(permissions[1]);
        Log.d("Permission", "Permission: " + perm + " & " + perm2);
        return (perm == PackageManager.PERMISSION_GRANTED && perm2 == PackageManager.PERMISSION_GRANTED);
    }
}
