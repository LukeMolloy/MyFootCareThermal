package com.example.footcare.diabeticcarethermal;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.RequiresApi;
import android.support.annotation.Size;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class Reward extends Activity {
    DatabaseHelper myDB;
    Button btnDone;
    TextView percent, message1, message2;
    ImageView imageView2;
    double woundFootPercent, firstFootPercent, woundPercent;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        myDB = new DatabaseHelper(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward);

        btnDone = (Button) findViewById(R.id.btnDone);
        percent = (TextView) findViewById(R.id.percent);
        message1 = (TextView) findViewById(R.id.message1);
        message2 = (TextView) findViewById(R.id.message2);
        imageView2 = (ImageView) findViewById(R.id.imageView2);

        woundFootPercent = getIntent().getDoubleExtra("woundFootPercent", 0.0);
        firstFootPercent = getIntent().getDoubleExtra("firstFootPercent", 0.0);
        woundPercent = getIntent().getDoubleExtra("woundPercent", 0.0);

        assignRewardText();
        DoneButton();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void assignRewardText(){
        int displayPercent = (int) woundFootPercent;
        String displayPercentString = new Integer(displayPercent).toString();

        String specialCondition = findSpecialCondition();

        int numEntriesinWeek = numberOfEntriesInWeek();

        boolean completeMonth = healthCheckup();

        if(Objects.equals("FirstEntry", specialCondition)) {
            message1.setText("Great Work!");
            message2.setText("You have completed your first foot check!");
            percent.setText("Your wound tracking started today, we will measure your wound size against it's original size. So your wound starts at 100%.");
            percent.setTextSize(20);
            imageView2.setImageResource(R.drawable.reward0);
        }else if(completeMonth == true){
            message1.setText("It's been 4 weeks!");
            message2.setText("Remember to check in regularly with a health professional.");
            percent.setText("Your wound is " + displayPercentString + "% of its original size");
            imageView2.setImageResource(R.drawable.reward4);
        }else if(Objects.equals("GoalReached", specialCondition)){
            message1.setText("You've Reached Your Goal!");
            message2.setText("Your wound has now shrunk by 50% or more");
            percent.setText("Your wound is " + displayPercentString + "% of its original size");
            imageView2.setImageResource(R.drawable.reward3);
        }else if(numEntriesinWeek != 0){
            percent.setText("Your wound is " + displayPercentString + "% of its original size");
            if(numEntriesinWeek == 1){
                message1.setText("Its the start of a new week");
                message2.setText("Remember to try and do three foot checks per week.");
                imageView2.setImageResource(R.drawable.oneentry);
                imageView2.setPadding(70,200,70,200);
            }else if(numEntriesinWeek == 2){
                message1.setText("You're on a roll!");
                message2.setText("You have completed 2 checks this week... One to go!");
                imageView2.setImageResource(R.drawable.twoentries);
                imageView2.setPadding(70,200,70,200);
            }else if(numEntriesinWeek == 3){
                message1.setText("Week Complete!");
                message2.setText("You have done the three recommended checks for this week... Great work!");
                imageView2.setImageResource(R.drawable.threeentries);
                imageView2.setPadding(70,200,70,200);
            }
        }else if(woundPercent > firstFootPercent){
            message1.setText("Keep it up!");
            message2.setText("Remember to change your dressing regularly!");
            percent.setText("Your wound is " + displayPercentString + "% of its original size");
            imageView2.setImageResource(R.drawable.reward1);
        } else {
            message1.setText("Great Work!");
            message2.setText("Your wound is getting smaller. Keep it up!");
            percent.setText("Your wound is " + displayPercentString + "% of its original size");
            imageView2.setImageResource(R.drawable.reward2);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String findSpecialCondition(){

        String condition;
        boolean GoalReached = false;

        DatabaseHelper myDB = new DatabaseHelper(this);
        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_ANALYSIS);
        final ArrayList<String> Size = new ArrayList<String>();
        int i = 0;
        while (res.moveToNext()) {
            if (!Objects.equals(res.getString(1), null)) {
                if (!Objects.equals(res.getString(1), "")) {
                    Size.add(String.valueOf(i));
                    if(Double.valueOf(res.getString(1)) < 50.0){
                        GoalReached = true;
                    }else{
                        GoalReached = false;
                    }
                    i++;
                }
            }
        }

        if(Size.size() == 1){
            condition = "FirstEntry";
        }else if(GoalReached == true) {
            condition = "GoalReached";
        }else{
            condition = "";
        }

        return condition;
    }

    public void DoneButton() {
        btnDone.setOnClickListener(
                new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(View view) {
                        finish();
                        Intent intent;
                        intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("popupMessage", findSpecialCondition());
                        startActivity(intent);

                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onBackPressed() {
        Intent intent;
        intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("popupMessage", findSpecialCondition());
        startActivity(intent);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public int numberOfEntriesInWeek(){
        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_ANALYSIS);
        Calendar c = Calendar.getInstance();
        int numberOfEntries = 0;

        int currentYear = c.get(Calendar.YEAR);
        int currentWeek = c.get(Calendar.WEEK_OF_YEAR);

        while (res.moveToNext()) {
            if (!Objects.equals(res.getString(1), null)) {
                if (!Objects.equals(res.getString(1), "")) {
                    try {
                        Date date = new java.text.SimpleDateFormat("dd/MM/yy").parse(res.getString(4));
                        c.setTime(date);
                        int entryYear = c.get(Calendar.YEAR);
                        int entryWeek = c.get(Calendar.WEEK_OF_YEAR);

                        if(entryYear == currentYear){
                            if(entryWeek == currentWeek){
                                numberOfEntries+=1;
                            }
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }

        return numberOfEntries;
    }

//    @RequiresApi(api = Build.VERSION_CODES.N)
//    public boolean completeMonth(){
//        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_ANALYSIS);
//        Calendar c = Calendar.getInstance();
//        boolean completeMonth = false;
//
//        int currentYear = c.get(Calendar.YEAR);
//        int month = c.get(Calendar.MONTH);
//        int currentMonth = month;
//        int day = 1;
//        int[] weeks = new int[4];
//        int[] entriesInWeek = new int[4];
//        int numWeeks = 0;
//
//        while (month == currentMonth){
//            c.set(currentYear,currentMonth, day);
//            weeks[c.get(Calendar.WEEK_OF_MONTH)-1] = c.get(Calendar.WEEK_OF_YEAR);
//            day +=1;
//            if (weeks[3] != 0){
//                break;
//            }
//        }
//
//        int x = 0;
//
//        while (res.moveToNext()) {
//            if (!Objects.equals(res.getString(1), null)) {
//                if (!Objects.equals(res.getString(1), "")) {
//                    try {
//                        for (int i = 0; i < weeks.length;) {
//                            int currentWeek = weeks[i];
//                            Date date = new java.text.SimpleDateFormat("dd/MM/yy").parse(res.getString(4));
//                            c.setTime(date);
//                            int entryWeek = c.get(Calendar.WEEK_OF_YEAR);
//
//                            if (entryWeek == currentWeek) {
//                                entriesInWeek[x]+=1;
//                                if(entriesInWeek[x] >= 3){
//                                    x++;
//                                }
//                            }
//                            i++;
//                        }
//                    } catch(Exception e){
//
//                    }
//                }
//            }
//        }
//        return completeMonth;
//    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean healthCheckup(){
        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_ANALYSIS);
        Calendar c = Calendar.getInstance();
        int numberOfWeeks = 0;
        boolean completeMonth = false;

        int currentYear = c.get(Calendar.YEAR);
        int currentWeek = 0;
        int weekCount = 0;
        int numEntriesInWeek = numberOfEntriesInWeek();

        while (res.moveToNext()) {
            if (!Objects.equals(res.getString(1), null)) {
                if (!Objects.equals(res.getString(1), "")) {
                    try {
                        Date date = new java.text.SimpleDateFormat("dd/MM/yy").parse(res.getString(4));
                        c.setTime(date);
                        int week = c.get(Calendar.WEEK_OF_YEAR);
                        if(week != currentWeek){
                            currentWeek = week;
                            weekCount+=1;
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }

        if(numEntriesInWeek <=1){
            if(weekCount%4 == 0){
                completeMonth = true;
            }
        }
        return completeMonth;
    }
}
