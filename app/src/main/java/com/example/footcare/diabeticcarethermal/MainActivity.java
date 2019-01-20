package com.example.footcare.diabeticcarethermal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
//import android.icu.text.SimpleDateFormat;
import java.text.SimpleDateFormat;

import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.icu.util.Calendar;
import android.icu.util.TimeUnit;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;

import android.widget.TextView;
import android.widget.Toast;


import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.jjoe64.graphview.series.Series;
import com.opencsv.CSVWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import static android.app.AlarmManager.INTERVAL_DAY;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static com.example.footcare.diabeticcarethermal.ImageProcessing.decodeSampledBitmapFromResource;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    DatabaseHelper myDB;
    public static final String MOTIVATION_PATH = "M_Path";
    public static final String USER_TAG = "U_TAG";
    SharedPreferences imageLocation;
    SharedPreferences userTag;
    private int PICK_IMAGE_REQUEST = 1;
    private int DEFAULT_YEAR = 1970;
    private int DEFAULT_MONTH = 0;
    private boolean GOAL_REACHED = false;
    Button btnMenu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ThermalFootImages/");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        myDB = new DatabaseHelper(this);

        imageLocation = getSharedPreferences(MOTIVATION_PATH, MODE_PRIVATE);
        userTag = getSharedPreferences(USER_TAG, MODE_PRIVATE);
        if (userTag.getString("tag", "none") == "none") {
            Random rand = new Random();
            int num = rand.nextInt(10000);
            SharedPreferences.Editor ed = userTag.edit();
            ed.putString("tag", String.valueOf(num));
            ed.commit();
        }
        notifications_handler();
        //Code to insert wound size into DB
//        String date2 = new SimpleDateFormat("dd/MM").format(new Date());
//
//        String woundsize = "21.5";
//        myDB.insertData(woundsize, date2.toString());

        /*Request permission from the user*/
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.MANAGE_DOCUMENTS,
                        Manifest.permission.CAMERA},
                1);


        this.setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT); // force the use of landscape

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Reading from SharedPreferences
        String path = imageLocation.getString("location", "this is not the location");

        Log.d("<Motivation Image Path>", path);
        Uri theUri = Uri.parse(path);
        Bitmap motivation = loadMotivation(theUri);

        ImageView motImage = (ImageView) findViewById(R.id.motivationImage);
        motImage.setImageBitmap(motivation);
        motImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String theDate = getCurrentDateString();
                myDB.insertUserData(theDate, "Choose Motivation");
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);

            }
        });
        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_ANALYSIS);
        final ArrayList<String> Size = new ArrayList<String>();
//        final ArrayList<String> SizedaySRight = new ArrayList<String>();
//        final ArrayList<String> SizedaySLeft = new ArrayList<String>();

        int i = 0;
        int x = 0;
        while (res.moveToNext()) {
            if (!Objects.equals(res.getString(1), null)) {
                if (!Objects.equals(res.getString(1), "")) {
                    Size.add(String.valueOf(i));
//                    String test = res.getString(2);
//                    if (Objects.equals(res.getString(3),"Left")){
//                        SizedaySLeft.add(String.valueOf(i));
//                        i++;
//                    }else if (Objects.equals(res.getString(3),"Right")) {
//                        SizedaySRight.add(String.valueOf(i));
//                        x++;
//                    }
                }
            }
        }

        String specialCondition = getIntent().getStringExtra("popupMessage");

        if(Objects.equals("FirstEntry",specialCondition)){
            showMessage("Track your progress", "You start at 100%. Your Goal is to reach 50% in the first 4 weeks.", "The MyFootCare graph will help you track your progress and the star indicates your 4 week goal.", R.drawable.firstentry);
        }else if(Objects.equals("GoalReached",specialCondition)){
//            showMessage("GoalReached", "yay");
        }

        final int Length = Size.size();
//        final int SizeSizeLeft = SizedaySLeft.size();
//        final int SizeSizeRight = SizedaySRight.size();
        DataPoint[] dataPointsSize = new DataPoint[Length];
        dataPointsSize = fetchGraphData(Length);
        findMonths(dataPointsSize);
        graphDataOneLine_v2(dataPointsSize);
        monthButtonHandler(dataPointsSize);
//        graphDataOneLine(Length);
//        graphData(SizeSizeLeft, SizeSizeRight);
    }

    private Bitmap loadMotivation(Uri uri) {
        Bitmap theImage = null;
        try {
            theImage = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (theImage != null) {
//            theImage = Bitmap.createScaledBitmap(theImage, 600, 600, false);
            theImage = Bitmap.createBitmap(theImage);
        } else {
//            theImage = decodeSampledBitmapFromResource(getResources(), R.drawable.placeholder);
            theImage = decodeSampledBitmapFromResource(getResources(), R.drawable.placeholder,  600, 600);
        }
        return theImage;
    }

    /*
    This function simply manages the choosing of a motivational image for display on the main page.
    I don't really see the point in this feature but here it is.

    @Parameters
     - (int)requestCode :
     - (int)resultCode :
     - (Intent data)requestCode :
    @Returns
    Null
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            /* The selected image URI is coming in here */
            Uri uri = data.getData();
            Log.d("The Uri", "The Uri: " + uri.toString());

            // copy the image to the folder
            String newImage = "";
            try {
                newImage = ImageProcessing.copyFile(uri, getContentResolver());
            } catch (IOException e) {
                e.printStackTrace();
            }

            // save the new image path to preferences
            if (newImage != null && newImage != "") {
                SharedPreferences.Editor editor = imageLocation.edit();
                editor.putString("location", newImage);
                editor.commit();

                String path = imageLocation.getString("location", "this is not the location");

                Log.d("<Motivation Image Path>", path);
                Uri theUri = Uri.parse(newImage);
                Bitmap motivation = loadMotivation(theUri);
                ImageView imageView = (ImageView) findViewById(R.id.motivationImage);
                imageView.setImageBitmap(motivation);
            }
        }
    }

    /*
    This function opens the action hub page. The name openCamera is a remnant of
    earlier iterations of this project where it would open the camera immediately.

    @Parameters
        - (View)view : A parameter used in android development to allow interaction between
        the XML page and java
    @Returns
    Null
    */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void openCamera(View view) {
        myDB.insertUserData(getCurrentDateString(), "Foot Check");
        Intent intent = new Intent(this, ActionHub.class);
        intent.putExtra("LeftOrRight", "");
        startActivity(intent);

    }

    /*
    This function inflates a popup that can show some text and a picture on the main page.
    Currently used for a users first entry and for when they reach the 50% goal.

    @Parameters
        - (String)title : A title to be displayed
        - (String)message1 : Some text to be displayed above the image
        - (String)message2 : Some text to be displayed below the image
        - (int)picture : A resource ID for an image
    @Returns
    Null
    */
    public void showMessage(String title, String message1, String message2, int picture) {
        LayoutInflater inflater = this.getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = inflater.inflate(R.layout.dialog_mainactivity, null);
        builder.setView(dialogView);

        Button button = (Button) dialogView.findViewById(R.id.button);
        TextView messageView1 = (TextView) dialogView.findViewById(R.id.messageView1);
        TextView messageView2 = (TextView) dialogView.findViewById(R.id.messageView2);
        TextView titleView = (TextView) dialogView.findViewById(R.id.titleView);
        ImageView stageView = (ImageView) dialogView.findViewById(R.id.stageView);
        ImageView pictureView = (ImageView) dialogView.findViewById(R.id.pictureView);

        titleView.setText(title);
        messageView1.setText(message1);
        messageView2.setText(message2);
        pictureView.setImageResource(picture);


        final AlertDialog dialog = builder.create();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /*
    Pulls notification data entered by user from database and sends it through the the
    SetAlarm function to be scheduled as a notification.

    @Parameters
    Null
    @Returns
    Null
    */
    public void notifications_handler() {
        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_NOTIFICATIONS);

        while (res.moveToNext()) {
            if (!Objects.equals(res.getString(1), null)) {
                if (!Objects.equals(res.getString(1), "")) {
                    try {
                        String Active = res.getString(2);
                        String Title = res.getString(3);
                        String Interval = res.getString(5);
                        String Hour = res.getString(6);
                        String Min = res.getString(7);

                        setAlarm(Active, Title, "", Interval, Hour, Min);

                    } catch (Exception e) {

                    }
                }
            }
        }
    }

    /*
    Sets an alarm to display the notification entered and enabled by the user. Its
    worth noting that this hasnt been perfect in testing. It seems that the alarm
    function has difficulty scheduling iterative alarms that are multiple days apart.

    @Parameters
        - (String)Active : Indicates whether the the notification is active or not.
        - (String)Title : Title of notification
        - (String)Text : Text of notification
        - (String)Interval : How regularly a notification is triggered (Days)
        - (String)Hour : What time (Hour) it should trigger in a day
        - (String)Min : What time (Minute) it should trigger in a day
    @Returns
    Null
    */
    @TargetApi(Build.VERSION_CODES.N)
    public void setAlarm(String Active, String Title, String Text, String Interval, String Hour, String Min) {
        //Taken from https://www.youtube.com/watch?v=1fV9NmvxXJo&t=461s

        Date date = new Date();
        date.setHours(Integer.valueOf(Hour));
        date.setMinutes(Integer.valueOf(Min));
        date.setSeconds(0);

        Intent alertIntent = new Intent(this, NotifyService.class);
        alertIntent.putExtra("Title", Title);
        alertIntent.putExtra("Text", Text);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 100, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, date.getTime(), INTERVAL_DAY*Integer.valueOf(Interval), pendingIntent);


        if (!Objects.equals(Active, "true")) {
            am.cancel(pendingIntent);
        } else {

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void graphDataOneLine(int Size) {
        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_ANALYSIS);
        LineGraphSeries series;
        PointsGraphSeries series2;
        GraphView graph = (GraphView) findViewById(R.id.graph);

        DataPoint[] dataPointsSize = new DataPoint[Size];
        DataPoint[] dataPointsSize2 = new DataPoint[Size];

        int i = 0;

        while (res.moveToNext()) {
            if (!Objects.equals(res.getString(1), null)) {
                if (!Objects.equals(res.getString(1), "")) {
                    try {
                        Date date = new java.text.SimpleDateFormat("dd/MM/yy").parse(res.getString(4));
                        dataPointsSize[i] = new DataPoint(date, Double.valueOf(res.getString(1)));
//                        dataPointsSize[i] = new DataPoint(i, Double.valueOf(res.getString(1)));
                        i++;
                    } catch (Exception e) {

                    }
                }
            }
        }

//        int[] months = new int[dataPointsSize.length];
//        findMonths(dataPointsSize);
//        long firstDay = firstDayOfMonth(2);
//        Date dateTest=new Date(firstDay);
//        long lastDay = lastDayOfMonth(2);
//        Date dateTest2=new Date(lastDay);

        if (dataPointsSize.length != 0) {
            if (dataPointsSize[0] != null) {
                series = new LineGraphSeries<DataPoint>(dataPointsSize);
                series.setColor(Color.rgb(12, 110, 72));
                series.setThickness(16);

                series2 = new PointsGraphSeries<DataPoint>(dataPointsSize);
                series2.setShape(PointsGraphSeries.Shape.POINT);
                series2.setColor(Color.rgb(12, 110, 72));

                graph.addSeries(series);
                graph.addSeries(series2);

                graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
                graph.getGridLabelRenderer().setNumHorizontalLabels(4);

//                graph.getViewport().setScrollable(true); // enables horizontal scrolling
//                graph.getViewport().setScrollableY(true); // enables vertical scrolling
//                graph.getViewport().setScalable(true); // enables horizontal zooming and scrolling
//                graph.getViewport().setScalableY(true);
                graph.getViewport().setXAxisBoundsManual(true);

                graph.getGridLabelRenderer().setTextSize(30);
                graph.getGridLabelRenderer().reloadStyles();

                graph.getViewport().setYAxisBoundsManual(true);
                graph.getViewport().setMinY(0);
//                graph.getViewport().setMaxY(100);

                graph.getViewport().setXAxisBoundsManual(true);
                double x1 = dataPointsSize[0].getX();
                long date1 = (long) x1;
                graph.getViewport().setMinX(date1);
                double x2 = dataPointsSize[3].getX();
                long date2 = (long) x2;
                graph.getViewport().setMaxX(date2);


                series2.setOnDataPointTapListener(new OnDataPointTapListener() {
                    @Override
                    public void onTap(Series series2, DataPointInterface dataPoint) {
                        Toast.makeText(MainActivity.this, "Series1: On Data Point clicked: " + dataPoint, Toast.LENGTH_SHORT).show();
                        double x = dataPoint.getX();
                        long date = (long) x;
                        Intent intent = new Intent(MainActivity.this, Details.class);
                        intent.putExtra("Date", date);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }
    }

//    public DataPoint[] fetchDataInMonth(DataPoint[] Data, int month){
//
//
//    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void findMonths(DataPoint[] Data) {
        int[] months = new int[Data.length];
        Calendar c = Calendar.getInstance();
        int maxMonth = 0;
        boolean monthfound = false;

        for (int i = 0; i < Data.length; ) {
            double x = Data[i].getX();
            long date = (long) x;
            c.setTimeInMillis(date);
            months[i] = c.get(Calendar.MONTH);
            i++;
        }

        for (int i = 0; i < 12; ) {
            for (int x = 0; x < Data.length; ) {
                if (months[x] == i) {
                    Button monthBtn = (Button) this.findViewById(getResources().getIdentifier("month" + Integer.toString(i), "id", getPackageName()));
                    monthBtn.setVisibility(View.VISIBLE);
                    monthfound = true;
//                    if(isArrayHomogenous(months)){
//                        Button monthBtn2 = (Button) this.findViewById(getResources().getIdentifier("month" + Integer.toString(i+1), "id", getPackageName()));
//                        monthBtn2.setVisibility(View.VISIBLE);
//                    }
                }
                if (months[x] > maxMonth){
                    maxMonth = months[x];
                }
                x++;
            }
            i++;
        }

        if (monthfound == false){
            Calendar cal = Calendar.getInstance();
            int month = cal.get(Calendar.MONTH);
            Button monthBtn = (Button) this.findViewById(getResources().getIdentifier("month" + Integer.toString(month), "id", getPackageName()));
            monthBtn.setVisibility(View.VISIBLE);
            monthBtn.setBackgroundResource(R.drawable.button_border_pressed);
        }else{
            Button monthBtn = (Button) this.findViewById(getResources().getIdentifier("month" + Integer.toString(maxMonth), "id", getPackageName()));
            monthBtn.setBackgroundResource(R.drawable.button_border_pressed);
        }
//        return months;
    }

    public boolean isArrayHomogenous(int[] array){
        boolean isHomogenous = true;
        for(int x = 0; x < array.length;){
            if (array[0] != array[x]){
                isHomogenous = false;
            }
            x++;
        }
        return isHomogenous;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public long firstDayOfMonth() {
        long firstDay = 0;
        Calendar c = Calendar.getInstance();
        c.set(DEFAULT_YEAR, DEFAULT_MONTH, 1);
        firstDay = c.getTimeInMillis();
        return firstDay;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public long lastDayOfMonth() {
        long lastDay = 0;
        Calendar c = Calendar.getInstance();
        c.set(DEFAULT_YEAR, DEFAULT_MONTH, 1);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        lastDay = c.getTimeInMillis();
        return lastDay;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public DataPoint[] fetchGraphData(int length) {
        DataPoint[] dataPointsSize = new DataPoint[length];
        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_ANALYSIS);
        Calendar c = Calendar.getInstance();

        int i = 0;
        while (res.moveToNext()) {
            if (!Objects.equals(res.getString(1), null)) {
                if (!Objects.equals(res.getString(1), "")) {
                    try {
                        Date date = new java.text.SimpleDateFormat("dd/MM/yy").parse(res.getString(4));
                        c.setTime(date);
                        DEFAULT_YEAR = c.get(Calendar.YEAR);
                        DEFAULT_MONTH = c.get(Calendar.MONTH);
                        if(Double.valueOf(res.getString(1)) < 50.0){
                            GOAL_REACHED = true;
                        }
                        dataPointsSize[i] = new DataPoint(date, Double.valueOf(res.getString(1)));
                        i++;
                    } catch (Exception e) {

                    }
                }
            }
        }
        return dataPointsSize;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void graphDataOneLine_v2(DataPoint[] data) {
        LineGraphSeries series, series3;
        PointsGraphSeries series2, series4;
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setPathEffect(new DashPathEffect(new float[]{8, 9}, 0));
        if (GOAL_REACHED == false) {
            paint.setColor(Color.rgb(243,113,33));
        }else{
            paint.setColor(Color.rgb(255,192,0));
        }

        GraphView graph = (GraphView) findViewById(R.id.graph);

        DateFormat format = new SimpleDateFormat("dd");
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this, format));
        graph.getViewport().setXAxisBoundsManual(true);

        graph.getGridLabelRenderer().setTextSize(30);
        graph.getGridLabelRenderer().reloadStyles();

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(findGreatestEntry()+20);

        graph.getViewport().setXAxisBoundsManual(true);
        long dateStart = firstDayOfMonth();
        graph.getViewport().setMinX(dateStart-86400000);
        long dateEnd = lastDayOfMonth();
        graph.getViewport().setMaxX(dateEnd+86400000);

        if (data.length != 0) {
            if (data[0] != null) {
                series = new LineGraphSeries<DataPoint>(data);
                series.setColor(Color.rgb(243,113,33));
                series.setThickness(10);

                series2 = new PointsGraphSeries<DataPoint>(data);
                series2.setShape(PointsGraphSeries.Shape.POINT);
                series2.setCustomShape(new PointsGraphSeries.CustomShape() {
                    @Override
                    public void draw(Canvas canvas, Paint paint, float x, float y, DataPointInterface dataPoint) {
                        Bitmap graphPoint = BitmapFactory.decodeResource(getResources(),R.drawable.graphpoint_small_new_3);
                        graphPoint = Bitmap.createScaledBitmap(graphPoint,75,75,false);
                        canvas.drawBitmap(graphPoint, x-(graphPoint.getWidth()/2), y-(graphPoint.getHeight()/2), paint);
                    }
                });
                series2.setColor(Color.rgb(0, 133, 152));

                if(findSmallestEntry() < 50.0){
                    series3 = new LineGraphSeries<>(new DataPoint[] {
                            new DataPoint(findFirstEntry(), 50.0),
                            new DataPoint(findFirstEntry()+2246400000.0, 50.0),
                            new DataPoint(findGoalReachedEntry(), 50.0)
                    });
                }else{
                    series3 = new LineGraphSeries<>(new DataPoint[] {
                            new DataPoint(findFirstEntry(), 50.0),
                            new DataPoint(findFirstEntry()+2246400000.0, 50.0),
                            new DataPoint(findLastEntry(), 50.0)
                    });
                }
                series3.setDrawAsPath(true);
                series3.setCustomPaint(paint);

                series4 = new PointsGraphSeries<>(new DataPoint[] {
                        new DataPoint(findFirstEntry()+2246400000.0, 50.0),
                });
                series4.setShape(PointsGraphSeries.Shape.POINT);
                if (GOAL_REACHED == false) {
                    series4.setCustomShape(new PointsGraphSeries.CustomShape() {
                        @Override
                        public void draw(Canvas canvas, Paint paint, float x, float y, DataPointInterface dataPoint) {
                            Bitmap graphPoint = BitmapFactory.decodeResource(getResources(),R.drawable.graphpoint_small_new_6);
                            graphPoint = Bitmap.createScaledBitmap(graphPoint,75,75,false);
                            canvas.drawBitmap(graphPoint, x-(graphPoint.getWidth()/2), y-(graphPoint.getHeight()/2), paint);
                        }
                    });
                }else{
                    series4.setCustomShape(new PointsGraphSeries.CustomShape() {
                        @Override
                        public void draw(Canvas canvas, Paint paint, float x, float y, DataPointInterface dataPoint) {
                            Bitmap graphPoint = BitmapFactory.decodeResource(getResources(),R.drawable.graphpoint_small_new_5);
                            graphPoint = Bitmap.createScaledBitmap(graphPoint,75,75,false);
                            canvas.drawBitmap(graphPoint, x-(graphPoint.getWidth()/2), y-(graphPoint.getHeight()/2), paint);
                        }
                    });
                }

                series4.setColor(Color.rgb(0, 133, 152));

                graph.addSeries(series3);
                graph.addSeries(series4);

                series4.setOnDataPointTapListener(new OnDataPointTapListener() {
                    @Override
                    public void onTap(Series series2, DataPointInterface dataPoint) {
                        Toast.makeText(getApplicationContext(), "Your goal is to reach this point!",
                                Toast.LENGTH_LONG).show();
                    }
                });


                graph.addSeries(series);
                graph.addSeries(series2);

                series2.setOnDataPointTapListener(new OnDataPointTapListener() {
                    @Override
                    public void onTap(Series series2, DataPointInterface dataPoint) {
                        myDB.insertUserData(getCurrentDateString(), "Foot photos through graph");
                        double x = dataPoint.getX();
                        long date = (long) x;
                        Intent intent = new Intent(MainActivity.this, Details.class);
                        intent.putExtra("Date", date);
                        startActivity(intent);
                    }
                });

            }
        } else {

        }
    }

    public long findLastEntry(){
        myDB = new DatabaseHelper(getApplicationContext());
        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_ANALYSIS);
        long datelong = 0;
        while (res.moveToNext()) {
            if (!Objects.equals(res.getString(2), null)) {
                if (!Objects.equals(res.getString(2), "")) {
                    try {
                        Date date = new java.text.SimpleDateFormat("dd/MM/yy").parse(res.getString(4));
                        datelong = date.getTime();
                    } catch (Exception e) {

                    }

                }
            }
        }
        return datelong;
    }

    public long findFirstEntry(){
        myDB = new DatabaseHelper(getApplicationContext());
        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_ANALYSIS);
        long datelong = 0;
        Date date = new java.text.SimpleDateFormat("dd/MM/yy").get2DigitYearStart();
        while (res.moveToNext()) {
            if (!Objects.equals(res.getString(2), null)) {
                if (!Objects.equals(res.getString(2), "")) {
                    try {
                        date = new java.text.SimpleDateFormat("dd/MM/yy").parse(res.getString(4));
                        datelong = date.getTime();
                        break;
                    } catch (Exception e) {

                    }

                }
            }
        }
        return datelong;
    }

    public double findSmallestEntry(){
        myDB = new DatabaseHelper(getApplicationContext());
        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_ANALYSIS);
        double minPercent = 100;
        while (res.moveToNext()) {
            if (!Objects.equals(res.getString(2), null)) {
                if (!Objects.equals(res.getString(2), "")) {
                    try {
                        double newPercent = Double.valueOf(res.getString(1));
                        if(newPercent < minPercent){
                            minPercent = newPercent;
                        }
                    } catch (Exception e) {

                    }

                }
            }
        }
        return minPercent;
    }

    public double findGreatestEntry(){
        myDB = new DatabaseHelper(getApplicationContext());
        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_ANALYSIS);
        double maxPercent = 100;
        while (res.moveToNext()) {
            if (!Objects.equals(res.getString(2), null)) {
                if (!Objects.equals(res.getString(2), "")) {
                    try {
                        double newPercent = Double.valueOf(res.getString(1));
                        if(newPercent > maxPercent){
                            maxPercent = newPercent;
                        }
                    } catch (Exception e) {

                    }

                }
            }
        }
        return maxPercent;
    }

    public long findGoalReachedEntry(){
        myDB = new DatabaseHelper(getApplicationContext());
        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_ANALYSIS);
        long datelong = 0;
        Date date = new java.text.SimpleDateFormat("dd/MM/yy").get2DigitYearStart();
        while (res.moveToNext()) {
            if (!Objects.equals(res.getString(2), null)) {
                if (!Objects.equals(res.getString(2), "")) {
                    try {
                        date = new java.text.SimpleDateFormat("dd/MM/yy").parse(res.getString(4));
                        datelong = date.getTime();
                        double newPercent = Double.valueOf(res.getString(1));
                        if(newPercent < 50.0){
                            break;
                        }
                    } catch (Exception e) {

                    }

                }
            }
        }
        return datelong;
    }

    public void monthButtonHandler(final DataPoint[] data) {
        final Button month0 = (Button) this.findViewById(R.id.month0);
        final Button month1 = (Button) this.findViewById(R.id.month1);
        final Button month2 = (Button) this.findViewById(R.id.month2);
        final Button month3 = (Button) this.findViewById(R.id.month3);
        final Button month4 = (Button) this.findViewById(R.id.month4);
        final Button month5 = (Button) this.findViewById(R.id.month5);
        final Button month6 = (Button) this.findViewById(R.id.month6);
        final Button month7 = (Button) this.findViewById(R.id.month7);
        final Button month8 = (Button) this.findViewById(R.id.month8);
        final Button month9 = (Button) this.findViewById(R.id.month9);
        final Button month10 = (Button) this.findViewById(R.id.month10);
        final Button month11 = (Button) this.findViewById(R.id.month11);

        month0.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                DEFAULT_MONTH = 0;
                DEFAULT_YEAR = 2018;
                graphDataOneLine_v2(data);
                month0.setBackgroundResource(R.drawable.button_border_pressed);
                month1.setBackgroundResource(R.drawable.button_border);
                month2.setBackgroundResource(R.drawable.button_border);
                month3.setBackgroundResource(R.drawable.button_border);
                month4.setBackgroundResource(R.drawable.button_border);
                month5.setBackgroundResource(R.drawable.button_border);
                month6.setBackgroundResource(R.drawable.button_border);
                month7.setBackgroundResource(R.drawable.button_border);
                month8.setBackgroundResource(R.drawable.button_border);
                month9.setBackgroundResource(R.drawable.button_border);
                month10.setBackgroundResource(R.drawable.button_border);
                month11.setBackgroundResource(R.drawable.button_border);
            }
        });

        month1.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                DEFAULT_MONTH = 1;
                DEFAULT_YEAR = 2018;
                graphDataOneLine_v2(data);
                month0.setBackgroundResource(R.drawable.button_border_pressed);
                month1.setBackgroundResource(R.drawable.button_border);
                month2.setBackgroundResource(R.drawable.button_border);
                month3.setBackgroundResource(R.drawable.button_border);
                month4.setBackgroundResource(R.drawable.button_border);
                month5.setBackgroundResource(R.drawable.button_border);
                month6.setBackgroundResource(R.drawable.button_border);
                month7.setBackgroundResource(R.drawable.button_border);
                month8.setBackgroundResource(R.drawable.button_border);
                month9.setBackgroundResource(R.drawable.button_border);
                month10.setBackgroundResource(R.drawable.button_border);
                month11.setBackgroundResource(R.drawable.button_border);
            }
        });

        month2.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                DEFAULT_MONTH = 2;
                DEFAULT_YEAR = 2018;
                graphDataOneLine_v2(data);
                month0.setBackgroundResource(R.drawable.button_border);
                month1.setBackgroundResource(R.drawable.button_border_pressed);
                month2.setBackgroundResource(R.drawable.button_border);
                month3.setBackgroundResource(R.drawable.button_border);
                month4.setBackgroundResource(R.drawable.button_border);
                month5.setBackgroundResource(R.drawable.button_border);
                month6.setBackgroundResource(R.drawable.button_border);
                month7.setBackgroundResource(R.drawable.button_border);
                month8.setBackgroundResource(R.drawable.button_border);
                month9.setBackgroundResource(R.drawable.button_border);
                month10.setBackgroundResource(R.drawable.button_border);
                month11.setBackgroundResource(R.drawable.button_border);
            }
        });

        month3.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                DEFAULT_MONTH = 3;
                DEFAULT_YEAR = 2018;
                graphDataOneLine_v2(data);
                month0.setBackgroundResource(R.drawable.button_border);
                month1.setBackgroundResource(R.drawable.button_border);
                month2.setBackgroundResource(R.drawable.button_border_pressed);
                month3.setBackgroundResource(R.drawable.button_border);
                month4.setBackgroundResource(R.drawable.button_border);
                month5.setBackgroundResource(R.drawable.button_border);
                month6.setBackgroundResource(R.drawable.button_border);
                month7.setBackgroundResource(R.drawable.button_border);
                month8.setBackgroundResource(R.drawable.button_border);
                month9.setBackgroundResource(R.drawable.button_border);
                month10.setBackgroundResource(R.drawable.button_border);
                month11.setBackgroundResource(R.drawable.button_border);
            }
        });

        month4.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                DEFAULT_MONTH = 4;
                DEFAULT_YEAR = 2018;
                graphDataOneLine_v2(data);
                month0.setBackgroundResource(R.drawable.button_border);
                month1.setBackgroundResource(R.drawable.button_border);
                month2.setBackgroundResource(R.drawable.button_border);
                month3.setBackgroundResource(R.drawable.button_border_pressed);
                month4.setBackgroundResource(R.drawable.button_border);
                month5.setBackgroundResource(R.drawable.button_border);
                month6.setBackgroundResource(R.drawable.button_border);
                month7.setBackgroundResource(R.drawable.button_border);
                month8.setBackgroundResource(R.drawable.button_border);
                month9.setBackgroundResource(R.drawable.button_border);
                month10.setBackgroundResource(R.drawable.button_border);
                month11.setBackgroundResource(R.drawable.button_border);
            }
        });

        month5.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                DEFAULT_MONTH = 5;
                DEFAULT_YEAR = 2018;
                graphDataOneLine_v2(data);
                month0.setBackgroundResource(R.drawable.button_border);
                month1.setBackgroundResource(R.drawable.button_border);
                month2.setBackgroundResource(R.drawable.button_border);
                month3.setBackgroundResource(R.drawable.button_border);
                month4.setBackgroundResource(R.drawable.button_border);
                month5.setBackgroundResource(R.drawable.button_border_pressed);
                month6.setBackgroundResource(R.drawable.button_border);
                month7.setBackgroundResource(R.drawable.button_border);
                month8.setBackgroundResource(R.drawable.button_border);
                month9.setBackgroundResource(R.drawable.button_border);
                month10.setBackgroundResource(R.drawable.button_border);
                month11.setBackgroundResource(R.drawable.button_border);
            }
        });

        month6.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                DEFAULT_MONTH = 6;
                DEFAULT_YEAR = 2018;
                graphDataOneLine_v2(data);
                month0.setBackgroundResource(R.drawable.button_border);
                month1.setBackgroundResource(R.drawable.button_border);
                month2.setBackgroundResource(R.drawable.button_border);
                month3.setBackgroundResource(R.drawable.button_border);
                month4.setBackgroundResource(R.drawable.button_border);
                month5.setBackgroundResource(R.drawable.button_border);
                month6.setBackgroundResource(R.drawable.button_border_pressed);
                month7.setBackgroundResource(R.drawable.button_border);
                month8.setBackgroundResource(R.drawable.button_border);
                month9.setBackgroundResource(R.drawable.button_border);
                month10.setBackgroundResource(R.drawable.button_border);
                month11.setBackgroundResource(R.drawable.button_border);
            }
        });

        month7.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                DEFAULT_MONTH = 7;
                DEFAULT_YEAR = 2018;
                graphDataOneLine_v2(data);
                month0.setBackgroundResource(R.drawable.button_border);
                month1.setBackgroundResource(R.drawable.button_border);
                month2.setBackgroundResource(R.drawable.button_border);
                month3.setBackgroundResource(R.drawable.button_border);
                month4.setBackgroundResource(R.drawable.button_border);
                month5.setBackgroundResource(R.drawable.button_border);
                month6.setBackgroundResource(R.drawable.button_border);
                month7.setBackgroundResource(R.drawable.button_border_pressed);
                month8.setBackgroundResource(R.drawable.button_border);
                month9.setBackgroundResource(R.drawable.button_border);
                month10.setBackgroundResource(R.drawable.button_border);
                month11.setBackgroundResource(R.drawable.button_border);
            }
        });

        month8.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                DEFAULT_MONTH = 8;
                DEFAULT_YEAR = 2018;
                graphDataOneLine_v2(data);
                month0.setBackgroundResource(R.drawable.button_border);
                month1.setBackgroundResource(R.drawable.button_border);
                month2.setBackgroundResource(R.drawable.button_border);
                month3.setBackgroundResource(R.drawable.button_border);
                month4.setBackgroundResource(R.drawable.button_border);
                month5.setBackgroundResource(R.drawable.button_border);
                month6.setBackgroundResource(R.drawable.button_border);
                month7.setBackgroundResource(R.drawable.button_border);
                month8.setBackgroundResource(R.drawable.button_border_pressed);
                month9.setBackgroundResource(R.drawable.button_border);
                month10.setBackgroundResource(R.drawable.button_border);
                month11.setBackgroundResource(R.drawable.button_border);
            }
        });

        month9.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                DEFAULT_MONTH = 9;
                DEFAULT_YEAR = 2018;
                graphDataOneLine_v2(data);
                month0.setBackgroundResource(R.drawable.button_border);
                month1.setBackgroundResource(R.drawable.button_border);
                month2.setBackgroundResource(R.drawable.button_border);
                month3.setBackgroundResource(R.drawable.button_border);
                month4.setBackgroundResource(R.drawable.button_border);
                month5.setBackgroundResource(R.drawable.button_border);
                month6.setBackgroundResource(R.drawable.button_border);
                month7.setBackgroundResource(R.drawable.button_border);
                month8.setBackgroundResource(R.drawable.button_border);
                month9.setBackgroundResource(R.drawable.button_border_pressed);
                month10.setBackgroundResource(R.drawable.button_border);
                month11.setBackgroundResource(R.drawable.button_border);
            }
        });

        month10.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                DEFAULT_MONTH = 10;
                DEFAULT_YEAR = 2018;
                graphDataOneLine_v2(data);
                month0.setBackgroundResource(R.drawable.button_border);
                month1.setBackgroundResource(R.drawable.button_border);
                month2.setBackgroundResource(R.drawable.button_border);
                month3.setBackgroundResource(R.drawable.button_border);
                month4.setBackgroundResource(R.drawable.button_border);
                month5.setBackgroundResource(R.drawable.button_border);
                month6.setBackgroundResource(R.drawable.button_border);
                month7.setBackgroundResource(R.drawable.button_border);
                month8.setBackgroundResource(R.drawable.button_border);
                month9.setBackgroundResource(R.drawable.button_border);
                month10.setBackgroundResource(R.drawable.button_border_pressed);
                month11.setBackgroundResource(R.drawable.button_border);
            }
        });

        month11.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                DEFAULT_MONTH = 11;
                DEFAULT_YEAR = 2018;
                graphDataOneLine_v2(data);
                month0.setBackgroundResource(R.drawable.button_border);
                month1.setBackgroundResource(R.drawable.button_border);
                month2.setBackgroundResource(R.drawable.button_border);
                month3.setBackgroundResource(R.drawable.button_border);
                month4.setBackgroundResource(R.drawable.button_border);
                month5.setBackgroundResource(R.drawable.button_border);
                month6.setBackgroundResource(R.drawable.button_border);
                month7.setBackgroundResource(R.drawable.button_border);
                month8.setBackgroundResource(R.drawable.button_border);
                month9.setBackgroundResource(R.drawable.button_border);
                month10.setBackgroundResource(R.drawable.button_border);
                month11.setBackgroundResource(R.drawable.button_border_pressed);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        String theDate = getCurrentDateString();
        switch(menuItem.getItemId()) {
            case R.id.photos:
                myDB.insertUserData(theDate, "Foot photos through menu");
                long date = findFirstEntry();
                if(date == 0){
                    Toast.makeText(getApplicationContext(), "No Photos! Press 'Check my foot' to perform a foot check!", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intentphotos = new Intent(this, Details.class);
                    intentphotos.putExtra("Date", date);
                    this.startActivity(intentphotos);
                }
                break;
            case R.id.notifications:
                myDB.insertUserData(theDate, "Notifications");
                Intent intentnotifications = new Intent(this, Notifications.class);
                startActivity(intentnotifications);
                break;
            case R.id.settings:
                myDB.insertUserData(theDate, "Settings");
                Intent intentdetails = new Intent(this, Settings.class);
                startActivity(intentdetails);
                break;
            case R.id.credits:
                myDB.insertUserData(theDate, "Credits");
                Intent intentcredits = new Intent(this, Credits.class);
                startActivity(intentcredits);
                break;
            case R.id.terms:
                myDB.insertUserData(theDate, "Terms");
                Intent intentterms = new Intent(this, Terms.class);
                startActivity(intentterms);
                break;
            case R.id.export:
                myDB.insertUserData(theDate, "Export to CSV");
                exportCSV();
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        return false;
    }

    public void exportCSV(){
        // Based on http://stackoverflow.com/questions/31367270/exporting-sqlite-database-to-csv-file-in-android
        Log.d("Button Clicked", "CSV Export Started");
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/FootImages/");
        if (!folder.exists()) {
            folder.mkdir();
        }
        File logFile = new File(folder, "activityLog.csv");
        try
        {
            logFile.createNewFile();
            CSVWriter writer = new CSVWriter(new FileWriter(logFile));
            Cursor res = myDB.getAllData(DatabaseHelper.TABLE_USER_DATA);
            writer.writeNext(res.getColumnNames());
            while(res.moveToNext())
            {
                String arrStr[] ={res.getString(0),res.getString(1), res.getString(2),res.getString(3),res.getString(4),res.getString(5)};
                writer.writeNext(arrStr);
            }
            writer.close();
            res.close();
            Toast.makeText(getApplicationContext(), "CSV Exported", Toast.LENGTH_SHORT).show();
        }

        catch(Exception sqlEx)
        {
            Toast.makeText(getApplicationContext(), "Export Error", Toast.LENGTH_SHORT).show();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String getCurrentDateString(){
        Calendar c = Calendar.getInstance();
        Date currentTime = c.getTime();
        String dateString = currentTime.toString();
        return dateString;
    }
}