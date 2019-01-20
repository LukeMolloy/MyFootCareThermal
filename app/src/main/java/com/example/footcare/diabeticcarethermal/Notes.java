package com.example.footcare.diabeticcarethermal;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2RGB;

public class Notes extends AppCompatActivity {
    DatabaseHelper myDB;
    String imageUri, dateNow, dateLong, leftOrRight;
    double woundFootPercent, woundPercent, firstFootPercent;
    boolean foundWoundSize, foundImage;
    TextView Notes;
    Button btnSkip, btnDone;
    Bitmap jpg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Notes = (TextView) findViewById(R.id.Notes);
        btnSkip = (Button) findViewById(R.id.btnSkip);
        btnDone = (Button) findViewById(R.id.btnDone);

        imageUri = getIntent().getStringExtra("imageUri");
        dateNow = getIntent().getStringExtra("dateNow");
        dateLong = getIntent().getStringExtra("dateLong");
        woundFootPercent = getIntent().getDoubleExtra("woundFootPercent", 0.0);
        firstFootPercent = getIntent().getDoubleExtra("firstFootPercent", 0.0);
        woundPercent = getIntent().getDoubleExtra("woundPercent", 0.0);
        foundWoundSize = getIntent().getBooleanExtra("foundWoundSize", false);
        foundImage = getIntent().getBooleanExtra("foundImage", false);
        leftOrRight = getIntent().getStringExtra("LeftOrRight");

        jpg = BitmapFactory.decodeFile(imageUri);
//        setPicture();
        SkipButton();
        DoneButton();
    }

    private void setPicture() {
        ImageView imageView = (ImageView) this.findViewById(R.id.imageView);
        imageView.setImageBitmap(null);
        imageView.setImageBitmap(jpg);
    }

    /*insertIntoDB(String dateNow, double woundsize, boolean foundWoundsize, boolean foundImage)
        params: dateNow - A string of the current date (dd/mm/yyyy) to be entered into DB
                woundsize - The size of the wound found to be entered into DB
                foundWoundsize - if it has already found a recorded wound size for that day
                foundImage - if it has already found an image saved for that day
        Description: Used to instead results into database. If the user has already added an entry
                      for that day, then it updates the results.
        returns: None
    */
    public void insertIntoDB(String dateNow, double woundsize, double woundfootpercent, boolean foundWoundsize, boolean foundImage, String notes, String leftOrRight){
        myDB = new DatabaseHelper(getApplicationContext());
        String woundStr = String.valueOf(woundsize);
        String woundfootStr = String.valueOf(woundfootpercent);
        if (woundsize > 0)  {
            if (foundWoundsize) {
                myDB.updateAnalysis(woundStr, woundfootStr, leftOrRight, dateNow);
            }else{
                myDB.insertData(woundStr, woundfootStr, leftOrRight, dateNow);
            }
        }

        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ThermalFootImages/");
        if (!folder.exists()) {
            folder.mkdir();
        }

        Mat OrgMat = new Mat();
        Utils.bitmapToMat(jpg, OrgMat);

        Mat finalmat = new Mat(OrgMat.size(),COLOR_BGR2GRAY);
        Imgproc.cvtColor( OrgMat, finalmat, COLOR_BGR2RGB );

        String mOrgFile = folder.toString() + "/Org_" + dateLong + ".jpg";
//        if (!foundImage){
        myDB.insertImage("file://" + mOrgFile, leftOrRight, notes, dateNow);
//        myDB.insertImage("file://" + mProcessedFile, LeftOrRight, dateNow);s
//        }
        Imgcodecs.imwrite(mOrgFile, finalmat);
//        Imgcodecs.imwrite(mProcessedFile, dst);

    }

    public void DoneButton() {
        btnDone.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                        String notes = Notes.getText().toString();
                        insertIntoDB(dateNow,woundFootPercent,woundPercent,foundWoundSize,foundImage,notes,leftOrRight);
                        Intent intent;
                        intent = new Intent(getApplicationContext(), Reward.class);
                        intent.putExtra("woundPercent", woundPercent);
                        intent.putExtra("woundFootPercent", woundFootPercent);
                        intent.putExtra("firstFootPercent", firstFootPercent);
                        startActivity(intent);
                    }
                });

    }

    public void SkipButton() {
        btnSkip.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                        String notes = "";
                        insertIntoDB(dateNow,woundFootPercent,woundPercent,foundWoundSize,foundImage,notes,leftOrRight);
                        Intent intent;
                        intent = new Intent(getApplicationContext(), Reward.class);
                        intent.putExtra("woundPercent", woundPercent);
                        intent.putExtra("woundFootPercent", woundFootPercent);
                        intent.putExtra("firstFootPercent", firstFootPercent);
                        startActivity(intent);
                    }
                });

    }

    public void onBackPressed() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Discard entry")
                .setMessage("Are you sure you want to discard this entry?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getApplicationContext(), ActionHub.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}
