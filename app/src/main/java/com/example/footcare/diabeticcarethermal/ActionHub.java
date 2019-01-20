package com.example.footcare.diabeticcarethermal;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ActionHub extends AppCompatActivity {

    private ImageButton mImageLeftFoot, mImageRightFoot, mImageLeft, mImageRight;
    private Button checkButton, checkTempButton;
    private TextView tempReadout, tempReadoutCaption;
    static final int GET_PHOTO_REQUEST = 1;
    DatabaseHelper myDB;
    Uri leftImagePathURI, rightImagePathURI;
    String leftImagePath, rightImagePath, leftOrRight;
    boolean leftFoot, rightFoot;
    boolean tempChecked = false;
    double leftTemp, rightTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_hub);

        myDB = new DatabaseHelper(getApplicationContext());
        mImageLeftFoot=(ImageButton)findViewById(R.id.imageButtonLeft);
        mImageRightFoot=(ImageButton)findViewById(R.id.imageButtonRight);
        mImageLeft=(ImageButton)findViewById(R.id.imageLeft);
        mImageRight=(ImageButton)findViewById(R.id.imageRight);
        checkButton=(Button)findViewById(R.id.checkButton);
        checkTempButton=(Button)findViewById(R.id.checkTempButton);
        tempReadout=(TextView)findViewById(R.id.tempReadoutTitle);
        tempReadoutCaption=(TextView)findViewById(R.id.tempReadoutCaption);
        leftTemp = 0.0;
        rightTemp = 0.0;

        checkButton.setClickable(false);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formatedDate = sdf.format(new Date());

        leftImagePath = "/storage/emulated/0/ThermalFootImages/" + "FLIROne-Visibleleft" + formatedDate + ".jpg";
        rightImagePath = "/storage/emulated/0/ThermalFootImages/" + "FLIROne-Visibleright" + formatedDate + ".jpg";

        File leftImgFile = new  File(leftImagePath);
        if(leftImgFile.exists()){
            leftImagePathURI = Uri.parse(leftImagePath);
            mImageLeft.setImageURI(leftImagePathURI);
            mImageLeft.setScaleType(ImageView.ScaleType.FIT_XY);
            mImageLeft.setPadding(0,0,0,0);
            leftTemp = retrieveTemp("left",formatedDate);
            leftFoot = true;
        }else{
            leftFoot = false;
        }

        File rightImgFile = new  File(rightImagePath);
        if(rightImgFile.exists()){
            rightImagePathURI = Uri.parse(rightImagePath);
            mImageRight.setImageURI(rightImagePathURI);
            mImageRight.setScaleType(ImageView.ScaleType.FIT_XY);
            mImageRight.setPadding(0,0,0,0);
            rightTemp = retrieveTemp("right",formatedDate);
            rightFoot = true;
        }else{
            rightFoot = false;
        }

        if(leftFoot && rightFoot){
            checkTempButton.setClickable(true);
            checkTempButton.setBackgroundResource(R.drawable.button_rounded_redesign);
        }else{
            checkTempButton.setClickable(false);
        }

        leftOrRight = findChosenFoot();
        int a = 0;
    }

    public void leftFootPhoto(View v)
    {
        Intent intent = new Intent(this, CameraViewThermal.class);
        intent.putExtra("LeftOrRight", "left");
        startActivityForResult(intent, GET_PHOTO_REQUEST);
    }

    public String leftOrRightFoot(){
        String result = "";
        Cursor data = myDB.getAllData(myDB.TABLE_ANALYSIS);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
        String dateNow = dateFormat.format(new Date());
        Boolean ImageFound = false;
        while (data.moveToNext()) {
            String ImageDate = data.getString(data.getColumnIndex("DATE"));
            String footType = data.getString(3);
            result = footType;
        }
        data.close();

        return result;
    }

    public double retrieveTemp(String leftright, String currentDate){
        double temp = 0.0;
        Cursor data = myDB.getAllData(myDB.TABLE_TEMP);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");

        while (data.moveToNext()) {
            String TempDate = data.getString(data.getColumnIndex("DATE"));
            String footType = data.getString(data.getColumnIndex("FOOTTYPE"));
            String tempVal = data.getString(data.getColumnIndex("TEMPVAL"));

            if(Objects.equals(TempDate, currentDate)){
                if(Objects.equals(leftright, footType)){
                    temp = Double.valueOf(tempVal);
                }
            }
        }
        data.close();

        return temp;
    }

    public void rightFootPhoto(View v)
    {
        Intent intent = new Intent(this, CameraViewThermal.class);
        intent.putExtra("LeftOrRight", "right");
        startActivityForResult(intent, GET_PHOTO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formatedDate = sdf.format(new Date());
        if (requestCode == GET_PHOTO_REQUEST) {
            if (resultCode == RESULT_OK) {
                String imagePath = data.getStringExtra("IMAGE_PATH");
                double footTemp = data.getDoubleExtra("FOOT_TEMP", 0.0);
                String leftRight = data.getStringExtra("LeftOrRight");
                Uri imagePathURI = Uri.parse(imagePath);
                tempReadout.setVisibility(View.GONE);
                checkTempButton.setVisibility(View.VISIBLE);

                if(Objects.equals(leftRight, "left")){
                    leftImagePath = imagePath;
                    mImageLeft.setImageDrawable(getResources().getDrawable(R.drawable.actionhub_left));
                    mImageLeft.setImageURI(imagePathURI);
                    mImageLeft.setScaleType(ImageView.ScaleType.FIT_XY);
                    mImageLeft.setPadding(0,0,0,0);
                    leftTemp = footTemp;
                    if(!leftFoot){
                        myDB.insertTemp(String.valueOf(footTemp), "left", formatedDate);
                    }else{
                        myDB.updateTemp(String.valueOf(footTemp), "left", formatedDate);
                    }
                    leftFoot = true;
                }else{
                    rightImagePath = imagePath;
                    mImageRight.setImageDrawable(getResources().getDrawable(R.drawable.actionhub_right));
                    mImageRight.setImageURI(imagePathURI);
                    mImageRight.setScaleType(ImageView.ScaleType.FIT_XY);
                    mImageRight.setPadding(0,0,0,0);
                    rightTemp = footTemp;
                    if(!rightFoot){
                        myDB.insertTemp(String.valueOf(footTemp), "right", formatedDate);
                    }else{
                        myDB.updateTemp(String.valueOf(footTemp), "right", formatedDate);
                    }
                    rightFoot = true;
                }

                if(leftFoot && rightFoot){
                    checkTempButton.setClickable(true);
                    checkTempButton.setBackgroundResource(R.drawable.button_rounded_redesign);
                }else{
                    checkTempButton.setClickable(false);
                }
            }else{

            }
        }
    }

    public void checkFootTemp(View v) {
        if (leftFoot && rightFoot) {
            tempChecked = true;
            checkButton.setClickable(true);
            checkButton.setBackgroundResource(R.drawable.button_rounded_redesign);
            tempReadout.setVisibility(View.VISIBLE);
            checkTempButton.setVisibility(View.GONE);
            Double tempDifference = leftTemp-rightTemp;
            if(tempDifference < 0){
                tempDifference = tempDifference*-1;
            }

            if(tempDifference > 3.0){
                tempReadout.setText("Risk of Infection");
                tempReadout.setTextColor(getResources().getColor(R.color.Red));
                tempReadoutCaption.setText("Check your feet again in 24 hours. Your my foot care app will remind you.");
                Intent intent = new Intent(getApplicationContext(), ThermalFeedback.class);
                intent.putExtra("Temperature", tempDifference);
                startActivity(intent);
            }else {
                tempReadout.setText("No Infection Risk");
                tempReadout.setTextColor(getResources().getColor(R.color.colorPrimary));
                tempReadoutCaption.setVisibility(View.GONE);
            }
//            String formattedDouble = String.format("%.2f", tempDifference) + " degrees difference";
        }
    }

    public void checkFoot(View v) {
        if (leftFoot && rightFoot && tempChecked) {
            String entryFound;
            entryFound = leftOrRightFoot();
            if(!Objects.equals(entryFound, "")){
                if(Objects.equals(leftOrRight, "Right")){
                    Intent intent = new Intent(getApplicationContext(), ProcessFootImage_v2.class);
                    intent.putExtra("IMAGE_PATH", rightImagePath);
                    intent.putExtra("LOOP_COUNT", 0);
                    intent.putExtra("LeftOrRight", leftOrRight);
                    startActivity(intent);
                }else if(Objects.equals(leftOrRight, "Left")) {
                    Intent intent = new Intent(getApplicationContext(), ProcessFootImage_v2.class);
                    intent.putExtra("IMAGE_PATH", leftImagePath);
                    intent.putExtra("LOOP_COUNT", 0);
                    intent.putExtra("LeftOrRight", leftOrRight);
                    startActivity(intent);
                }
            }else{
                LayoutInflater inflater = this.getLayoutInflater();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View dialogView = inflater.inflate(R.layout.dialog_actionhub, null);
                builder.setView(dialogView);

                ImageButton buttonLeft = (ImageButton) dialogView.findViewById(R.id.imageButtonLeftChoice);
                ImageButton buttonRight = (ImageButton) dialogView.findViewById(R.id.imageButtonRightChoice);

                final AlertDialog dialog = builder.create();

                buttonLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), ProcessFootImage_v2.class);
                        intent.putExtra("IMAGE_PATH", leftImagePath);
                        intent.putExtra("LOOP_COUNT", 0);
                        intent.putExtra("LeftOrRight", "left");
                        myDB.updateContact("Left", "",0);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });

                buttonRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), ProcessFootImage_v2.class);
                        intent.putExtra("IMAGE_PATH", rightImagePath);
                        intent.putExtra("LOOP_COUNT", 0);
                        intent.putExtra("LeftOrRight", "right");
                        myDB.updateContact("Right", "",0);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        }
    }

    public String findChosenFoot() {
        String result = "";
        int count = 0;
        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_SETTINGS);
        while (res.moveToNext()) {
            if (count == 0) {
                result = res.getString(1);
                count++;
            }
        }
        return result;
    }
}
