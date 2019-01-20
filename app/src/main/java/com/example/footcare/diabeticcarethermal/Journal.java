package com.example.footcare.diabeticcarethermal;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.icu.text.DateFormat;
import android.support.annotation.DrawableRes;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.Space;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.AlarmManager.INTERVAL_DAY;
import static com.example.footcare.diabeticcarethermal.R.id.center;
import static com.example.footcare.diabeticcarethermal.R.id.center_horizontal;
import static com.example.footcare.diabeticcarethermal.R.id.center_vertical;
import static com.example.footcare.diabeticcarethermal.R.id.invisible;

//public class Journal extends AppCompatActivity {
//    DatabaseHelper myDB;
//    ImageButton excellent;
//    ImageButton good;
//    ImageButton fair;
//    ImageButton poor;
//    ImageButton terrible;
////    Button showAll;
//    Button submit;
//    EditText activity;
//    int rating = 0;
//    LinearLayout linearlayout;
//    CardView cards;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_journal);
//        myDB = new DatabaseHelper(this);
////        showAll = (Button)findViewById(R.id.showall);
//        submit = (Button)findViewById(R.id.submit);
//        activity = (EditText)findViewById(R.id.activity);
//        excellent = (ImageButton)findViewById(R.id.excellent);
//        good = (ImageButton)findViewById(R.id.good);
//        fair = (ImageButton)findViewById(R.id.fair);
//        poor = (ImageButton)findViewById(R.id.poor);
//        terrible = (ImageButton)findViewById(R.id.terrible);
//        linearlayout = (LinearLayout)findViewById(R.id.entries);
////        viewAll();
//        listeners();
//        AddData();
//        displayEntries();
//
//
//    }
//
//    private void displayEntries() {
//        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_JOURNAL_ENTRY);
//        if (res.getCount()!=0){
//            for (res.moveToLast(); !res.isBeforeFirst(); res.moveToPrevious()) {
//                TextView activity = new TextView(this);
//                TextView date = new TextView(this);
//                ImageView rating = new ImageView(this);
//                TextView line = new TextView(this);
//                TextView line2 = new TextView(this);
//
//                activity.setText(res.getString(3));
//                activity.setTextSize(20);
//
//                date.setText(res.getString(1));
//                date.setTextSize(15);
//
//                if (res.getInt(2) == 1){
//                    rating.setImageResource(R.drawable.excellentactive);
//                } if (res.getInt(2) == 2){
//                    rating.setImageResource(R.drawable.goodactive);
//                }if (res.getInt(2) == 3){
//                    rating.setImageResource(R.drawable.fairactive);
//                } if (res.getInt(2) == 4){
//                    rating.setImageResource(R.drawable.pooractive);
//                }if (res.getInt(2) == 5){
//                    rating.setImageResource(R.drawable.terribleactive);
//                }
//
//                line.setWidth(DrawerLayout.LayoutParams.MATCH_PARENT);
//                line.setHeight(10);
//                line.setBackgroundColor(Color.BLACK);
//
//                line2.setWidth(DrawerLayout.LayoutParams.MATCH_PARENT);
//                line2.setHeight(5);
//                line2.setBackgroundColor(Color.WHITE);
//
//                linearlayout.addView(rating);
//                linearlayout.addView(date);
//                linearlayout.addView(activity);
//                linearlayout.addView(line);
//                linearlayout.addView(line2);
//            }
//        }
//    }
//
//    public void AddData(){
//        submit.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        String Activity = activity.getText().toString();
//                        if (rating == 0) {
//                            showMessage("Please rate your mood","");
//                        }if (Activity.equals("")){
//                            showMessage("Please fill out your activities for the day","");
//                        }if (rating != 0 && !Activity.equals("")) {
//                            String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
//                            boolean isInserted = myDB.insertJournal(date, rating, Activity);
//                            if (isInserted = true) {
//                                Toast.makeText(Journal.this, "Entry Posted", Toast.LENGTH_LONG).show();
//                            } else {
//                                Toast.makeText(Journal.this, "There was an error with posting entry", Toast.LENGTH_LONG).show();
//                            }
//                            linearlayout.removeAllViews();
//                            displayEntries();
//
//                            //Reset journal entry fields
//                            excellent.setImageResource(R.drawable.excellentinactive);
//                            good.setImageResource(R.drawable.goodinactive);
//                            fair.setImageResource(R.drawable.fairinactive);
//                            poor.setImageResource(R.drawable.poorinactive);
//                            terrible.setImageResource(R.drawable.terribleinactive);
//                            activity.setText("");
//                        }
//                    }
//                }
//        );
//    }
//
//    public void listeners() {
//        excellent.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view){
//                        rating = 1;
//                        excellent.setImageResource(R.drawable.excellentactive);
//                        good.setImageResource(R.drawable.goodinactive);
//                        fair.setImageResource(R.drawable.fairinactive);
//                        poor.setImageResource(R.drawable.poorinactive);
//                        terrible.setImageResource(R.drawable.terribleinactive);
//                    }
//                }
//        );
//        good.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view){
//                        rating = 2;
//                        excellent.setImageResource(R.drawable.excellentinactive);
//                        good.setImageResource(R.drawable.goodactive);
//                        fair.setImageResource(R.drawable.fairinactive);
//                        poor.setImageResource(R.drawable.poorinactive);
//                        terrible.setImageResource(R.drawable.terribleinactive);
//                    }
//                }
//        );
//        fair.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view){
//                        rating = 3;
//                        excellent.setImageResource(R.drawable.excellentinactive);
//                        good.setImageResource(R.drawable.goodinactive);
//                        fair.setImageResource(R.drawable.fairactive);
//                        poor.setImageResource(R.drawable.poorinactive);
//                        terrible.setImageResource(R.drawable.terribleinactive);
//                    }
//                }
//        );
//        poor.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view){
//                        rating = 4;
//                        excellent.setImageResource(R.drawable.excellentinactive);
//                        good.setImageResource(R.drawable.goodinactive);
//                        fair.setImageResource(R.drawable.fairinactive);
//                        poor.setImageResource(R.drawable.pooractive);
//                        terrible.setImageResource(R.drawable.terribleinactive);
//                    }
//                }
//        );
//        terrible.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view){
//                        rating = 5;
//                        excellent.setImageResource(R.drawable.excellentinactive);
//                        good.setImageResource(R.drawable.goodinactive);
//                        fair.setImageResource(R.drawable.fairinactive);
//                        poor.setImageResource(R.drawable.poorinactive);
//                        terrible.setImageResource(R.drawable.terribleactive);
//                    }
//                }
//        );
//    }
//
////    public void viewAll() {
////        showAll.setOnClickListener(
////                new View.OnClickListener() {
////                    @Override
////                    public void onClick(View view) {
////                        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_JOURNAL_ENTRY);
////                        if(res.getCount() == 0){
////                            showMessage("Error:","Nothing Found");
////                            return;
////                        }
////                        StringBuffer buffer = new StringBuffer();
////                        while (res.moveToNext()) {
////                            buffer.append("ID:"+ res.getInt(0)+"\n");
////                            buffer.append("Entry Date:"+ res.getString(1)+"\n");
////                            buffer.append("Rating: "+ res.getInt(2)+"\n");
////                            buffer.append("Activity: "+ res.getString(3)+"\n");
////                        }
////                        //show data
////                        showMessage("Data",buffer.toString());
////                    }
////                }
////        );
////    }
//    public void showMessage(String title, String message) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setCancelable(true);
//        builder.setTitle(title);
//        builder.setMessage(message);
//        builder.show();
//    }
//}
