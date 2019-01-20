package com.example.footcare.diabeticcarethermal;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.core.Mat;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class Details extends AppCompatActivity {
    DatabaseHelper myDB = new DatabaseHelper(this);
    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;
    private ImageView mImageView;
    ViewPager viewPager;
    double lefttemp, righttemp, tempDifference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DateFormat df = new SimpleDateFormat("dd MMM yyyy");
        long Date = getIntent().getLongExtra("Date", 0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dateLongFormat = new SimpleDateFormat("yyyyMMdd");
        Date itemDate = new Date(Date);
        String formatedDate = sdf.format(itemDate);
        String dateLong = dateLongFormat.format(itemDate);

        buttonHandler(Date);

        Object[] imageData = fetchImageData(itemDate);
        String notes = imageData[0].toString();
        Uri imageUri = Uri.parse(imageData[1].toString());

        String woundPercent = fetchWoundPercent(itemDate);

        TextView textView= (TextView) this.findViewById(R.id.textView);
        textView.setText(notes);

        TextView dateText= (TextView) this.findViewById(R.id.dateText);
        dateText.setText(df.format(itemDate));

        TextView percent= (TextView) this.findViewById(R.id.percent);
        percent.setText(woundPercent+"%");

        ImageView riskIndicator = (ImageView) this.findViewById(R.id.riskIndicator);

        ProgressBar progressBar = (ProgressBar) this.findViewById(R.id.progressBar);
        if(Integer.parseInt(woundPercent) > 100){
            progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.circle));
        }else{
            progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.circlegreen));
        }
        progressBar.setProgress(Integer.parseInt(woundPercent));

        lefttemp = retrieveTemp("left", formatedDate);
        righttemp = retrieveTemp("right", formatedDate);

        tempDifference = lefttemp - righttemp;
        if(tempDifference < 0){
            tempDifference = tempDifference*-1;
        }

        if(tempDifference > 3.0){
            riskIndicator.setImageDrawable(getResources().getDrawable(R.drawable.warningsymbolorange));
        }else {
            riskIndicator.setImageDrawable(getResources().getDrawable(R.drawable.warningsymbolgreen));
        }

        riskIndicator.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Toast.makeText(getApplicationContext(), "Temperature difference of " + Double.toString(tempDifference) + " detected between feet.", Toast.LENGTH_LONG).show();
             }
         });
//        mImageView=(ImageView)findViewById(R.id.imageView);
//        mImageView.setImageURI(imageUri);

        viewPager = (ViewPager) findViewById(R.id.pager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        viewPagerAdapter.images[0] = Uri.parse("/storage/emulated/0/ThermalFootImages/" + "/Org_" + dateLong + ".jpg");
        viewPagerAdapter.images[1] = Uri.parse("/storage/emulated/0/ThermalFootImages/" + "FLIROne-Visibleleft" + formatedDate + ".jpg");
        viewPagerAdapter.images[2] = Uri.parse("/storage/emulated/0/ThermalFootImages/" + "FLIROne-Thermalleft" + formatedDate + ".jpg");
        viewPagerAdapter.images[3] = Uri.parse("/storage/emulated/0/ThermalFootImages/" + "FLIROne-Visibleright" + formatedDate + ".jpg");
        viewPagerAdapter.images[4] = Uri.parse("/storage/emulated/0/ThermalFootImages/" + "FLIROne-Thermalright" + formatedDate + ".jpg");
        viewPager.setAdapter(viewPagerAdapter);

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

    public Object[] fetchImageData(Date date) {
        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_IMAGES);
        Object[] results = new Object[2];

        while (res.moveToNext()) {
            Uri theUri = Uri.parse(res.getString(2));
            if(theUri != null && theUri.getPath() != ""){
                try {
                    java.util.Date itemDate = new java.text.SimpleDateFormat("dd/MM/yy").parse(res.getString(5));
                    if (Objects.equals(itemDate, date)) {
                        results[0] = res.getString(4);
                        results[1] = theUri;
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return results;
    }

    public String fetchWoundPercent(Date date) {
        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_ANALYSIS);
        String result = "";
        DecimalFormat df = new DecimalFormat("#.##");

        while (res.moveToNext()) {
            if (!Objects.equals(res.getString(1), null)) {
                if (!Objects.equals(res.getString(1), "")) {
                    try {
                        java.util.Date itemDate = new java.text.SimpleDateFormat("dd/MM/yy").parse(res.getString(4));
                        if (Objects.equals(itemDate, date)) {
//                            result = res.getString(1);
                            int resultInt = (int) Double.parseDouble(res.getString(1));
                            result = Integer.toString(resultInt);
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
        return result;
    }

    public int fetchEntriesLength() {
        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_ANALYSIS);
        int i = 0;
        while (res.moveToNext()) {
            if (!Objects.equals(res.getString(1), null)) {
                if (!Objects.equals(res.getString(1), "")) {
                    i++;
                }
            }
        }
        return i;
    }

    public long[] createDateArray(int length) {
        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_ANALYSIS);
        long[] entryDates = new long[length];

        int i = 0;
        while (res.moveToNext()) {
            if (!Objects.equals(res.getString(1), null)) {
                if (!Objects.equals(res.getString(1), "")) {
                    try {
                        java.util.Date itemDate = new java.text.SimpleDateFormat("dd/MM/yy").parse(res.getString(4));
                        long millis = itemDate.getTime();
                        entryDates[i] = millis;
                        i++;

                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
        return entryDates;
    }

    public void buttonHandler(long Date){
        int length = fetchEntriesLength();
        final long[] entryDates = createDateArray(length);
        int currentDateInt = 0;
        for (int i = 0; i < entryDates.length;){
            if(Date == entryDates[i]){
                currentDateInt = i;
            }
            i++;
        }

        Button back = (Button) this.findViewById(R.id.back);
        Button next = (Button) this.findViewById(R.id.next);

        if(currentDateInt == 0){
            back.setVisibility(View.INVISIBLE);
        }

        if(currentDateInt == length-1){
            next.setVisibility(View.INVISIBLE);
        }

        final int finalCurrentDateInt = currentDateInt;
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Details.class);
                intent.putExtra("Date", entryDates[finalCurrentDateInt-1]);
                startActivity(intent);
                overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_right );
                finish();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Details.class);
                intent.putExtra("Date", entryDates[finalCurrentDateInt+1]);
                startActivity(intent);
                overridePendingTransition( R.anim.slide_in_right, R.anim.slide_out_left );
                finish();
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();

    }

    public void onWarningIconClicked(View view){
        Toast.makeText(getApplicationContext(), "Temperature difference of " + Double.toString(tempDifference), Toast.LENGTH_LONG).show();
    }
}

