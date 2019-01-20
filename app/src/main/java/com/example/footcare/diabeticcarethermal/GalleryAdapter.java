package com.example.footcare.diabeticcarethermal;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Admin on 23/04/2017.
 * Based on the tutorial
 * http://stacktips.com/tutorials/android/android-gridview-example-building-image-gallery-in-android
 */

public class GalleryAdapter extends ArrayAdapter {
    private Context theContext;
    private int rid;
    ArrayList theImages = new ArrayList();

    public GalleryAdapter(Context theContext, int rid, ArrayList theImages){
        super(theContext, rid, theImages);
        this.rid = rid; // resource id
        this.theContext = theContext;
        this.theImages = theImages;
    }

    @Override
    public View getView(int pos, View theItem, ViewGroup theGrid){
        View item = theItem;
        ViewHolder recycle = null;

        if(item == null){ // There are no items in grid
            LayoutInflater inf = ((Activity) theContext).getLayoutInflater();
            item = inf.inflate(rid, theGrid, false);
            recycle = new ViewHolder();
            recycle.title = (TextView) item.findViewById(R.id.description);
            recycle.imgSrc = (ImageView) item.findViewById(R.id.gridImage);
            item.setTag(recycle);
        } else { // There are grid items
            recycle = (ViewHolder) item.getTag();
        }
        ImageClass imgObj = (ImageClass) theImages.get(pos);
        recycle.title.setText(imgObj.getTitle());
        recycle.imgSrc.setImageBitmap(imgObj.getImage());
        return item;
    }

    static class ViewHolder {
        TextView title;
        ImageView imgSrc;
    }
}
