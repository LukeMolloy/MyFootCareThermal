package com.example.footcare.diabeticcarethermal;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Created by Kyle on 23/04/2017.
 * Taken from http://stacktips.com/tutorials/android/android-gridview-example-building-image-gallery-in-android
 * Class for storing a single image for the slideshow and gallery
 */

class ImageClass {
    private Bitmap image;
    private String title;
    private Uri uri;

    public ImageClass(Bitmap image, String title, Uri uri) {
        super();
        this.image = image;
        this.title = title;
        this.uri = uri;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public Uri getUri(){
        return uri;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
