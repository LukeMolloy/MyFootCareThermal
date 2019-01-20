package com.example.footcare.diabeticcarethermal;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.icu.text.DateFormat;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.series.DataPoint;
import com.opencsv.CSVWriter;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static android.app.AlarmManager.INTERVAL_DAY;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2RGB;

public class Notifications extends AppCompatActivity {
    DatabaseHelper myDB;

    Button btnSave, btnAcc1, btnAcc2;
    TextView Title1,Title2,Interval1,Interval2,Hour1,Hour2,Min1,Min2;
    String active1, active2;
    Switch Switch1, Switch2;
    LinearLayout notification1, notification2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Title1 = (TextView) findViewById(R.id.title1);
        Title2 = (TextView) findViewById(R.id.title2);
        Interval1 = (TextView) findViewById(R.id.days1);
        Interval2 = (TextView) findViewById(R.id.days2);
        Hour1 = (TextView) findViewById(R.id.hours1);
        Hour2 = (TextView) findViewById(R.id.hours2);
        Min1 = (TextView) findViewById(R.id.mins1);
        Min2 = (TextView) findViewById(R.id.mins2);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnAcc1 = (Button) findViewById(R.id.button_ac_1);
        btnAcc2 = (Button) findViewById(R.id.button_ac_2);
        notification1 = (LinearLayout) findViewById(R.id.notification1);
        notification2 = (LinearLayout) findViewById(R.id.notification2);

        Switch1 = (Switch) findViewById(R.id.switch1);
        Switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (!isChecked) {
                    active1 = "false";
                } else {
                    active1 = "true";
                }
            }
        });

        Switch2 = (Switch) findViewById(R.id.switch2);
        Switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (!isChecked) {
                    active2 = "false";
                } else {
                    active2 = "true";
                }
            }
        });
        populateNotifications();
        NotificationsButton1();
        NotificationsButton2();
        SaveButton();
    }

    public void insertIntoDB(String id, String active, String title, String text, String interval, String hour, String min, boolean first){
        myDB = new DatabaseHelper(getApplicationContext());

        if (first){
            myDB.insertNotificationData(id, active, title, text, interval, hour, min);
        } else {
            myDB.updateNotificationData(id, active, title, text, interval, hour, min);
        }
    }

    public void SaveButton() {
        btnSave.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String title1 = Title1.getText().toString();
                        String title2 = Title2.getText().toString();
                        String interval1 = Interval1.getText().toString();
                        String interval2 = Interval2.getText().toString();
                        String hour1 = Hour1.getText().toString();
                        String hour2 = Hour2.getText().toString();
                        String min1 = Min1.getText().toString();
                        String min2 = Min2.getText().toString();

                        insertIntoDB("1", active1, title1, "", interval1, hour1, min1, false);
                        insertIntoDB("2", active2, title2, "", interval2, hour2, min2, false);

                        Intent intent;
                        intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    public void NotificationsButton1() {
        btnAcc1.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int visibility = notification1.getVisibility();
                        if (visibility == View.VISIBLE){
                            btnAcc1.setBackgroundResource(R.drawable.button_ac_down);
                            notification1.setVisibility(View.GONE);
                        } else if(visibility == View.GONE){
                            btnAcc1.setBackgroundResource(R.drawable.button_ac_up);
                            notification1.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    public void NotificationsButton2() {
        btnAcc2.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int visibility = notification2.getVisibility();
                        if (visibility == View.VISIBLE){
                            btnAcc2.setBackgroundResource(R.drawable.button_ac_down);
                            notification2.setVisibility(View.GONE);
                        } else if(visibility == View.GONE){
                            btnAcc2.setBackgroundResource(R.drawable.button_ac_up);
                            notification2.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    public void populateNotifications() {
        myDB = new DatabaseHelper(this);

        String check = null;

        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_NOTIFICATIONS);
        while (res.moveToNext()) {
            if (!Objects.equals(res.getString(1), null)) {
                if (!Objects.equals(res.getString(1), "")) {
                    try {
                        check = res.getString(1);
                        if (Objects.equals(res.getString(1), "1")) {
                            String a = res.getString(2);
                            if (Objects.equals(res.getString(2), "false")){
                                Switch1.setChecked(false);
                            } else {
                                Switch1.setChecked(true);
                            }
                            Title1.setText(res.getString(3));
                            Interval1.setText(res.getString(5));
                            Hour1.setText(res.getString(6));
                            Min1.setText(res.getString(7));
                        } else if (Objects.equals(res.getString(1), "2")) {
                            if (Objects.equals(res.getString(2), "false")){
                                Switch2.setChecked(false);
                            } else {
                                Switch2.setChecked(true);
                            }
                            Title2.setText(res.getString(3));
                            Interval2.setText(res.getString(5));
                            Hour2.setText(res.getString(6));
                            Min2.setText(res.getString(7));
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }

        if (check == null) {
            insertIntoDB("1", "false","Take a foot selfie today!", "", "3", "12", "00", true);
            Title1.setText("Take a foot selfie today!");
            Interval1.setText("3");
            Hour1.setText("12");
            Min1.setText("00");
            insertIntoDB("2", "false","", "", "", "", "", true);
        } else {

        }
    }
}