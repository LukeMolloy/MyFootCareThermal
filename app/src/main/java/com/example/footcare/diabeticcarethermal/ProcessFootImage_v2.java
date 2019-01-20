package com.example.footcare.diabeticcarethermal;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2RGB;
import static org.opencv.imgproc.Imgproc.COLOR_GRAY2BGR;
import static org.opencv.imgproc.Imgproc.Canny;

public class ProcessFootImage_v2 extends AppCompatActivity {
    private Mat mRgba, markerMask, imgGray, dst, OrgMat;
    private static final String TAG = "OCVSample::Activity";
    private String mWorkingFile, mOriginalFile, LeftOrRight;
    private boolean enableDraw = true;
    TouchImageView mImageView;
    Point prevpt;
    Button btnProcess, btnContinue, btnUndo, btnNotes, btnNext;
    TextView messages;
    ProgressDialog dlg;
    DatabaseHelper myDB;
    Bitmap OrgImg;
    int Linecounter, phase;
    double woundArea, footArea;
    float zoom;
    PointF focalPoint;

    static {
        OpenCVLoader.initDebug();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_foot_image);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        myDB = new DatabaseHelper(this);
        mImageView = (TouchImageView) findViewById(R.id.imgDisplay1);
        mOriginalFile = getIntent().getStringExtra("IMAGE_PATH");
        LeftOrRight = getIntent().getStringExtra("LeftOrRight");
        btnProcess = (Button) findViewById(R.id.processimage);
        btnContinue = (Button) findViewById(R.id.btnContinue);
        btnUndo = (Button) findViewById(R.id.btnUndo);
        btnNotes = (Button) findViewById(R.id.btnNotes);
        btnNext = (Button) findViewById(R.id.btnNext);
        messages = (TextView) findViewById(R.id.messages);
        prevpt = new Point(0, 0);
        dlg = new ProgressDialog(this);
        Linecounter = 0;
        phase = 0;
        mRgba = new Mat();
        Bitmap jpg = preprocessImage(BitmapFactory.decodeFile(mOriginalFile));
        setPicture(jpg);
        zoom = mImageView.getCurrentZoom();
//        focalPoint = mImageView.getScrollPosition();
//        focalPoint.equals((float)0.5, (float)0.5);
        DrawOnWound();
//        ProcessImage();
        ContinueProcess();
        UndoButton();
        NextButton();
        instruction();

    }

    public Bitmap preprocessImage(Bitmap jpg) {
        Bitmap preprocessed;

        if(jpg.getWidth() == 600){
            preprocessed = jpg;
        }else if(jpg.getWidth() > jpg.getHeight()){
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap scaledBitmap = jpg;
            double shrinkFactor = 0.9;
            while(scaledBitmap.getWidth()>1000){
                scaledBitmap = Bitmap.createScaledBitmap(jpg, (int)(jpg.getWidth()*shrinkFactor), (int)(jpg.getHeight()*shrinkFactor), true);
                shrinkFactor = shrinkFactor - 0.1;
            }
            preprocessed = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        }else{
            Bitmap scaledBitmap = jpg;
            double shrinkFactor = 0.9;
            while(scaledBitmap.getWidth()>600){
                scaledBitmap = Bitmap.createScaledBitmap(jpg, (int)(jpg.getWidth()*shrinkFactor), (int)(jpg.getHeight()*shrinkFactor), true);
                shrinkFactor = shrinkFactor - 0.1;
            }
            preprocessed = scaledBitmap;
        }

        return preprocessed;
    }

    @Override
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
                        finish();
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

    /*setPicture((Bitmap jpg, boolean setOrg)
        params: Bitmap image of the image taken
                boolean: true to set the current bitmap as the original image
                        false: just updates the screen
        Description: Used to determine if resizing the image to fit to the screen.
                    Prevents large images used for processing
    */
    private void setPicture(Bitmap jpg) {

        mImageView.setImageBitmap(null);
        OrgImg = jpg.copy(jpg.getConfig(),true);
        mImageView.setImageBitmap(jpg);
    }

    private void rotateImage(Bitmap jpg) {
        Matrix matrix = new Matrix();
        mImageView.setScaleType(ImageView.ScaleType.MATRIX);   //required
        matrix.postRotate(90);
        mImageView.setImageMatrix(matrix);
    }


    private float[] getLocationOnImg(float x, float y) {
        Matrix inverse = new Matrix();
        mImageView.getImageMatrix().invert(inverse);
        float[] touchPoint = new float[]{x, y};
        inverse.mapPoints(touchPoint);
        return touchPoint;
    }

    public void DrawOnWound() {
        mRgba = new Mat();
        OrgMat = new Mat();
        Utils.bitmapToMat(OrgImg, mRgba);
        Utils.bitmapToMat(OrgImg, OrgMat);
        markerMask = new Mat();
        imgGray = new Mat();
        Imgproc.cvtColor(mRgba, markerMask, COLOR_BGR2GRAY);
        Imgproc.cvtColor(markerMask, imgGray, COLOR_GRAY2BGR);

        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ThermalFootImages/");
        if (!folder.exists()) {
            folder.mkdir();
        }

        mWorkingFile = folder.toString() + "/Working.jpg";
        Imgcodecs.imwrite( mWorkingFile, OrgMat);
        markerMask.setTo(Scalar.all(0));
        mImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(enableDraw){
                    if(focalPoint == null){
                        mImageView.setZoom(zoom,(float)0.5,(float)0.5);
                    }else{
                        mImageView.setZoom(zoom,focalPoint.x,focalPoint.y);
                    }
                    int action = (event.getAction() & MotionEvent.ACTION_MASK);
                    float[] touchPoint;
                    int x,y;
                    switch (action) {
                        case MotionEvent.ACTION_POINTER_DOWN:
//                            Toast.makeText(getApplicationContext(), "Action pointer down", Toast.LENGTH_LONG).show();
                            return true;
                        case MotionEvent.ACTION_POINTER_UP:
                            return true;
                        case MotionEvent.ACTION_DOWN:
                            touchPoint = getLocationOnImg(event.getX(), event.getY());
                            x = (int) touchPoint[0];
                            y = (int) touchPoint[1];
                            prevpt = new Point(x, y);
                            break;
                        case MotionEvent.ACTION_UP:
//                            Toast.makeText(getApplicationContext(), "Action up", Toast.LENGTH_LONG).show();
                            Linecounter++;
                            instruction();
                            break;

                        case MotionEvent.ACTION_MOVE:
                            touchPoint = getLocationOnImg(event.getX(), event.getY());
                            x = (int) touchPoint[0];
                            y = (int) touchPoint[1];
                            Point pt = new Point(x, y);
                            if (prevpt.x == 0) {
                                prevpt = pt;
                            }
                            Imgproc.line(markerMask, prevpt, pt, new Scalar(255, 255, 255), 3);
                            Imgproc.line(mRgba, prevpt, pt, new Scalar(0, 255, 0), 3);
                            btnUndo.setVisibility(v.VISIBLE);
                            prevpt = pt;
                            Bitmap jpg = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(mRgba, jpg);
                            setPicture(jpg);
                            break;
                    }

                }else{
                    zoom = mImageView.getCurrentZoom();
                    focalPoint = mImageView.getScrollPosition();
                }

                return true;
            }
        });
    }

    public void instruction(){
        if(Linecounter == 0){
            messages.setText("1. Draw around your foot.");
            if(phase == 0){
                alertmessage("Draw around your foot","Scribble through all different background colours that are not your foot", R.drawable.stage1, R.drawable.stage1bar);
            }
        }else if(Linecounter == 1){
            enableDraw = false;
            btnNext.setVisibility(View.VISIBLE);
            if(phase == 1){
                enableDraw = true;
                btnNext.setVisibility(View.GONE);
                messages.setText("2. Draw around your wound.");
                alertmessage("Draw inside your foot","NOT touching your wound. Scribble through the different colours in your foot.", R.drawable.stage2, R.drawable.stage2bar);
            }
        }else if(Linecounter == 2){
            enableDraw = false;
            btnNext.setVisibility(View.VISIBLE);
            if(phase == 2){
                btnNext.setVisibility(View.VISIBLE);
                messages.setText("3. Zoom in on wound.");
                alertmessage("Zoom in on your wound","Use two fingers to zoom in on your wound", R.drawable.stage3, R.drawable.stage3bar);
            }else if(phase == 3){
                enableDraw = true;
                btnNext.setVisibility(View.GONE);
//                ProcessImage_v2();
                messages.setText("4. Draw inside your wound.");
                alertmessage("Draw inside your wound","Draw inside your wound. Make sure to stay inside the wound otherwise the analysis will not work.", R.drawable.stage4, R.drawable.stage4bar);
            }
        }else if(Linecounter == 3){
            enableDraw = false;
            btnNext.setVisibility(View.VISIBLE);
            if(phase == 4){
                enableDraw = true;
                btnNext.setVisibility(View.GONE);
                ProcessImage_v2();
                messages.setText("5. Check the outline");
                alertmessage("Check the outline", "Does the green outline go around your whole foot? And around your wound?", R.drawable.stage5, R.drawable.stage5bar);
                mImageView.resetZoom();
            }
        }
    }

    public void alertmessage(String title, String message, int picture, int stage){
        LayoutInflater inflater = this.getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = inflater.inflate(R.layout.dialog_workflow, null);
        builder.setView(dialogView);

        Button button = (Button) dialogView.findViewById(R.id.button);
        TextView messageView = (TextView) dialogView.findViewById(R.id.messageView);
        TextView titleView = (TextView) dialogView.findViewById(R.id.titleView);
        ImageView stageView = (ImageView) dialogView.findViewById(R.id.stageView);
        ImageView pictureView = (ImageView) dialogView.findViewById(R.id.pictureView);

        titleView.setText(title);
        messageView.setText(message);
        stageView.setImageResource(stage);
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

    /*UndoButton()
        Description: Listener to reset the image and buttons if a mistake when either processing or drawing
        on the image.
    */
    public void UndoButton() {
        btnUndo.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                        Bitmap jpg = BitmapFactory.decodeFile(mOriginalFile);
                        setPicture(jpg);
                        DrawOnWound();
                        enableDraw = true;
                        btnContinue.setVisibility(view.GONE);
                        btnProcess.setVisibility(view.GONE);
//                        Toast.makeText(getApplicationContext(), String.valueOf(Linecounter), Toast.LENGTH_LONG).show();
                        Linecounter = 0;
                        phase = 0;
                        instruction();
                        Intent intent = new Intent(getApplicationContext(), ProcessFootImage_v2.class);
                        intent.putExtra("IMAGE_PATH", mOriginalFile);
                        intent.putExtra("LeftOrRight", LeftOrRight);
                        startActivity(intent);
                        overridePendingTransition( R.anim.instant_change, R.anim.instant_change );
                    }
                });

    }

    public void NextButton(){
        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                enableDraw = true;
                phase++;
                btnNext.setVisibility(view.GONE);
                instruction();
            }
        });
    }

    public void ProcessImage_v2(){
        if (Linecounter < 3){
            Context context = getApplicationContext();
            CharSequence text = "";

            int duration = Toast.LENGTH_LONG;

            if(Linecounter == 0){
                text = "You need to draw a circle around your foot";
            }else if(Linecounter == 1){
                text = "You need to draw a circle around your wound";
            }else if(Linecounter == 2){
                text = "You need to draw a circle in your wound";
            }else if(Linecounter == 3){
                text = "Check your segmentation";
            }
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }else if(Linecounter > 3) {
            Context context = getApplicationContext();
            CharSequence text;

            int duration = Toast.LENGTH_LONG;
            text = "Too many lines! Press undo to try again.";

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else {
            enableDraw = false;

            btnContinue.setVisibility(View.VISIBLE);
            btnProcess.setVisibility(View.GONE);

            new ProcessImageTask().execute();



        }

    }

    public void ProcessImage(){
        btnProcess.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (Linecounter < 3){
                            Context context = getApplicationContext();
                            CharSequence text = "";

                            int duration = Toast.LENGTH_LONG;

                            if(Linecounter == 0){
                                text = "You need to draw a circle around your foot";
                            }else if(Linecounter == 1){
                                text = "You need to draw a circle around your wound";
                            }else if(Linecounter == 2){
                                text = "You need to draw a circle in your wound";
                            }else if(Linecounter == 3){
                                text = "Check your segmentation";
                            }
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }else if(Linecounter > 3) {
                            Context context = getApplicationContext();
                            CharSequence text;

                            int duration = Toast.LENGTH_LONG;
                            text = "Too many lines! Press undo to try again.";

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        } else {
                            enableDraw = false;

                            btnContinue.setVisibility(view.VISIBLE);
                            btnProcess.setVisibility(view.GONE);

                            new ProcessImageTask().execute();

                            if(woundArea == 2){
                                processError();
                            }else if(footArea == 2){
                                processError();
                            }

                        }
                    }
                });
    }

    public void ContinueProcess(){
        btnContinue.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        try {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
                            SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyyMMdd");
                            String dateNow = dateFormat.format(new Date());
                            String dateLong = dateFormat2.format(new Date());

                            myDB = new DatabaseHelper(getApplicationContext());

                            Cursor data = myDB.getAllData(myDB.TABLE_ANALYSIS);
                            boolean foundWoundsize = false;
                            while (data.moveToNext()) {
                                String analysisDate = data.getString(data.getColumnIndex("DATE"));
                                if (analysisDate.equals(dateNow)) {
                                    foundWoundsize = true;
                                    break;
                                }
                            }
                            data.close();

                            Cursor imageData = myDB.getAllData(myDB.TABLE_IMAGES);
                            boolean foundImage = false;
                            while (imageData.moveToNext()) {
                                String ImageDate = imageData.getString(imageData.getColumnIndex("DATE"));

                                if (ImageDate.equals(dateNow)) {
                                    foundImage = true;
                                    break;
                                }
                            }

                            int numEntries = findNumEntries();
                            double firstEntry = findFirstEntry();
                            String FirstDate = findFirstDate();
                            double woundPercent = (woundArea / footArea) * 100;
                            double woundFootPercent = 100.0;
                            if (FirstDate.equals(dateNow)){
                                woundFootPercent = 100.0;
                            }else if(firstEntry != 0.0){
                                woundFootPercent = woundPercent/firstEntry * 100;
                            }

                            Intent intent = new Intent(getApplicationContext(), Notes.class);
                            intent.putExtra("imageUri", mWorkingFile);
                            intent.putExtra("dateNow", dateNow);
                            intent.putExtra("dateLong", dateLong);
                            intent.putExtra("woundFootPercent", woundFootPercent);
                            intent.putExtra("firstFootPercent", firstEntry);
                            intent.putExtra("woundPercent", woundPercent);
                            intent.putExtra("foundWoundSize", foundWoundsize);
                            intent.putExtra("foundImage", foundImage);
                            intent.putExtra("LeftOrRight", LeftOrRight);
                            startActivity(intent);
                            finish();
//
                        } catch (Exception e){
                            Context context = getApplicationContext();
                            CharSequence text = "Unable to save results";
                            int duration = Toast.LENGTH_SHORT;
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }

                    }
                });
    }

    public double findFirstEntry(){
        myDB = new DatabaseHelper(getApplicationContext());
        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_ANALYSIS);
        double firstArea = 0.0;
        while (res.moveToNext()) {
            if (!Objects.equals(res.getString(2), null)) {
                if (!Objects.equals(res.getString(2), "")) {
//                    if (Objects.equals(res.getString(3),LeftOrRight)){
                    firstArea = Double.valueOf(res.getString(2));
                    break;

//                    }
                }
            }
        }
        return firstArea;
    }

    public String findFirstDate(){
        myDB = new DatabaseHelper(getApplicationContext());
        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_IMAGES);
        String ImageDate = "";
        while (res.moveToNext()) {
            ImageDate = res.getString(res.getColumnIndex("DATE"));
            break;
        }
        return ImageDate;
    }

    public int findNumEntries(){
        myDB = new DatabaseHelper(getApplicationContext());
        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_ANALYSIS);
        int datalength = 0;
        while (res.moveToNext()) {
            datalength++;
        }
        return datalength;
    }

    public List<MatOfPoint> removeDuplicates(List<MatOfPoint> contours){

        for(int i = 0; i < contours.size();){
            double area1 = Imgproc.contourArea(contours.get(i));
            for(int j = 0; j < contours.size();){
                double area2 = Imgproc.contourArea(contours.get(j));
                if(i != j){
                    if(area1 == area2){
                        contours.remove(j);
                    }
                }
                j++;
            }
            i++;
        }
        return contours;
    }

    public List<MatOfPoint> returnTwoSmallest(List<MatOfPoint> contours){
        List<MatOfPoint> newContours = new ArrayList<MatOfPoint>();

        for(int i = 0; i < 2;){
            double smallest = 1000000000.0;
            int smallestIdx = 0;
            for(int j = 0; j < contours.size();) {
                double area = Imgproc.contourArea(contours.get(j));
                if (area < smallest) {
                    smallest = area;
                    smallestIdx = j;
                }
                j++;
            }
            newContours.add(contours.get(smallestIdx));
            contours.remove(smallestIdx);
            i++;
        }

        return newContours;
    }

    public List<MatOfPoint> returnLargest(List<MatOfPoint> contours){
        List<MatOfPoint> newContours = new ArrayList<MatOfPoint>();
        double largest = 0.0;
        int largestIndex = 0;
        for(int i = 0; i < contours.size();){
            double area = Imgproc.contourArea(contours.get(i));
            if(area > largest){
                largest = area;
                largestIndex = i;
            }
            i++;
        }
        newContours.add(contours.get(largestIndex));

        return  newContours;
    }

    public void processError(){

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Oops")
                .setMessage("Something went wrong when processing your image, please try again.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        Bitmap jpg = BitmapFactory.decodeFile(mOriginalFile);
                        setPicture(jpg);
                        DrawOnWound();
                        enableDraw = true;
//                        Toast.makeText(getApplicationContext(), String.valueOf(Linecounter), Toast.LENGTH_LONG).show();
                        Linecounter = 0;
                        phase = 0;
                        instruction();
                        Intent intent = new Intent(getApplicationContext(), ProcessFootImage_v2.class);
                        intent.putExtra("IMAGE_PATH", mOriginalFile);
                        startActivity(intent);
                        overridePendingTransition( R.anim.instant_change, R.anim.instant_change );
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    public String saveMat(Mat mat, String type){
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ThermalFootImages/");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        //Creates the label for the thermal image
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formatedDate = sdf.format(new Date());
        String fileName = "/FLIROne-" + type + formatedDate + ".jpg";

        Imgcodecs.imwrite( folder.toString()+fileName, mat );

        return folder.toString()+fileName;
    }

    private class ProcessImageTask extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dlg.setMessage("Processing Image...");
            dlg.setCancelable(false);
            dlg.setIndeterminate(true);
            dlg.show();
        }

        @Override
        protected Integer  doInBackground (Integer...params) {
            Mat img1 = Imgcodecs.imread(mWorkingFile);
            dst = new Mat();
            dst = FindWound(img1);

            return 0;
        }


        /*FindWound(Mat img0)
            params: Mat: of the original image that is about to be processed.
            Description: Used to determine the location of the wound using markerMask created
                         earlier by the user drawing on the screen.
                          Does the following:
                          1. Finds contours on markerMask (expected a black and white mat)
                          2. Draw the contours (based on what the user has selected)
                          3. Uses Imgproc.watershed algorithm to find the wound based on the contours
                          by the user
                          4. Changes the pixels around the edges of the wound to green to be later
                            seen by the user.
                          5. finds the area size of the wound.
                          6. Uses the area to determine the size, using the size of the sticker
            returns: Mat of the results found.
        */
        private Mat FindWound(Mat img0) {
            List<MatOfPoint> contours3 = new ArrayList<MatOfPoint>();
            Mat img = new Mat();
            Mat temp = new Mat();
            img0.copyTo(img);
            Mat hierarchy = new MatOfInt4();
            List<MatOfPoint> contours1 = new ArrayList<MatOfPoint>();

            //Finds contours on markerMask (expected a black and white mat)
            Imgproc.findContours(markerMask, contours1, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
            Mat markers = new Mat(img.size(), CvType.CV_32SC1);
            markers.setTo(Scalar.all(0));
            int compCount = 0;

            //Draw the contours (based on what the user has selected)
            for (; compCount < contours1.size();compCount++){
                Imgproc.drawContours(markers, contours1, compCount, new Scalar((compCount + 10) % 255, 255,87),8);
            }

            //Uses Imgproc.watershed algorithm to find the wound based on the contours
            // by the user.
            Imgproc.watershed( img, markers );
            Mat wshed = new Mat(markers.size(), CvType.CV_8UC3);
            int size = (int)(markers.total() * markers.channels());
            int [] markersValues = new int[size];

            //Stores the pixels of the watersheded Mat into an array for use below.
            // This saves proessing time on having to access every pixel, rather than using wshed.get()
            markers.get(0, 0, markersValues);

            int rows = 0;
            int maxcols = markers.cols();
            int cols = 0;

            //Loops through the Mat to find the outline of the wound.
            // These are located with a -1. Colours these pixels in green when found.
            for (int j = 0; j < size; j++) {
                if (markersValues[j] == -1) {
                    wshed.put(rows, cols,new double[]{255,255,255});
                    wshed.put(rows, cols,new double[]{255,255,255});
                    //img.put(rows, cols,new double[]{34,255,210});
                }else{
                    wshed.put(rows,cols, new double[]{0,0,0});
                }
                cols++;
                //Used to keep track of the columns and rows in the mat, as markers.get
                // does store the data in a 2D array
                if (cols == maxcols ){
                    rows++;
                    cols = 0;
                }
            }

            Mat scy_gry = new Mat(wshed.size(),COLOR_BGR2GRAY);
            saveMat(wshed, "scy_grey");
            List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();
            Imgproc.cvtColor( wshed, scy_gry, COLOR_BGR2GRAY);

            //get the contours once outline of the wound has been found.
            Imgproc.findContours(scy_gry, contours2, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0,0));

//            if(contours2.size() <= 10){
            contours2 = removeDuplicates(contours2);
//            if (contours2.size() <=10 && contours2.size() >=2){
            contours2 = returnTwoSmallest(contours2);
            Scalar WoundColour2 = new Scalar(0, 255, 0);
            Imgproc.drawContours(img, contours2, -1, WoundColour2, 3);
            Imgproc.drawContours(scy_gry, contours2, -1, new Scalar(255, 255, 255), 1);
            contours3 = returnLargest(contours2);
            Imgproc.fillPoly(scy_gry, contours3, new Scalar(255, 255, 255));
            saveMat(scy_gry, "thermal_mask");
            woundArea = Imgproc.contourArea(contours2.get(0));
            footArea = Imgproc.contourArea(contours2.get(1));

            return img;
        }

        @Override
        protected void onPostExecute (Integer result){

            if(woundArea == 2){
                processError();
            }else if(footArea == 2){
                processError();
            }
            super.onPostExecute(result);
            File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ThermalFootImages/");
            if (!folder.exists()) {
                folder.mkdir();
            }

            Mat finalmat = new Mat(dst.size(),COLOR_BGR2GRAY);
            Imgproc.cvtColor( dst, finalmat, COLOR_BGR2RGB );

            Imgcodecs.imwrite(mWorkingFile, finalmat);

            Bitmap jpg = BitmapFactory
                    .decodeFile(mWorkingFile);
            setPicture(jpg);
            dlg.dismiss();

        }

    }
}
