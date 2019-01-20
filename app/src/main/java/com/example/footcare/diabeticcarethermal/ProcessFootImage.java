package com.example.footcare.diabeticcarethermal;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.series.DataPoint;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.opencv.core.Core.FONT_HERSHEY_PLAIN;
import static org.opencv.core.Core.max;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_GRAY2BGR;


//public class runProcessing extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//
//
//    }
//}

public class ProcessFootImage extends AppCompatActivity {
    private ImageView mImageView;
    private String LeftOrRight, notes;
    private String mProcessedFile, mWorkingFile, mOrgFile;
    private Mat mRgba, markerMask, imgGray, dst, OrgMat;
    private boolean enableDraw = true;
    private boolean notProcessed = true;
    private boolean foundSticker = false;
    private boolean houghFound = false;
    private CharSequence errorMsg;
    int Linecounter, LoopCounter;
    double area;
    Point prevpt;
    Button btnProcess, btnContinue, btnUndo, btnNotes;
    TextView messages, Notes;
    LinearLayout notesLayout;
    ProgressDialog dlg;
    DatabaseHelper myDB;
    Bitmap resizedBitmap, OrgImg;
    String dateLong;
    private static final double DOTSIZE = 24.0;
    private double circleLength = 0.0;
    private double woundLength = 0.0;
    DateFormat dateFormat;
    Scalar StickerColour = new Scalar(255, 0, 0);
    Scalar WoundColour = new Scalar(0, 255, 0);
    private static final String TAG = "OCVSample::Activity";

    static {
        OpenCVLoader.initDebug();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_foot_image);
        Mat m = new Mat(100,100, CvType.CV_8UC4);
        //myDB = new DatabaseHelper(this);
        mImageView = (ImageView) findViewById(R.id.imgDisplay1);
        mProcessedFile = "";
        mProcessedFile = getIntent().getStringExtra("IMAGE_PATH");
        LeftOrRight = getIntent().getStringExtra("LeftOrRight");
        LoopCounter = getIntent().getIntExtra("LOOP_COUNT",0);
        btnProcess = (Button) findViewById(R.id.processimage);
        btnContinue = (Button) findViewById(R.id.btnContinue);
        btnUndo = (Button) findViewById(R.id.btnUndo);
        btnNotes = (Button) findViewById(R.id.btnNotes);
        messages = (TextView) findViewById(R.id.messages);
        Notes = (TextView) findViewById(R.id.Notes);

        notesLayout = (LinearLayout) findViewById(R.id.notesLayout);

        if(LoopCounter == 0){
            setMessageBox("Please draw on & a circle around the wound", true);
        }else{
            setMessageBox("Please draw on & a circle around the foot", true);
        }

        dlg = new ProgressDialog(this);
        Linecounter = 0;

        //setPic();

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
                        Intent intent = new Intent(getApplicationContext(), TakeFootImage.class);
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



    /*onWindowFocusChanged
       Description: Calls all the needed buttons and functions at startup once
       everything on onCreate has finished.
    */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && notProcessed) {
            notProcessed = false;
            Bitmap jpg = BitmapFactory.decodeFile(mProcessedFile);
            setPicture(jpg, true);
            setVars();
            DrawOnWound();
            ProcessImage();
            ContinueProcess();
            UndoButton();
        }
    }

    public void setMessageBox(String Msg, boolean changeSize){
        if (changeSize)
            messages.setTextSize(15);
        messages.setText(Msg);
    }


    /*setPicture((Bitmap jpg, boolean setOrg)
        params: Bitmap image of the image taken
                boolean: true to set the current bitmap as the original image
                        false: just updates the screen
        Description: Used to determine if resizing the image to fit to the screen.
                    Prevents large images used for processing
    */
    private void setPicture(Bitmap jpg, boolean setOrg) {

        mImageView.setImageBitmap(null);
        int targetW = 1081;
        int targetH = 1441;
        int width = jpg.getWidth();
        int height = jpg.getHeight();

       if (height < targetH || width < targetW){
            if (setOrg){

                mImageView.setImageBitmap(OrgImg);
            } else {
                resizedBitmap = jpg.copy(jpg.getConfig(),true);
                mImageView.setImageBitmap(resizedBitmap);
            }
        } else {
           float scaleHeight;
           float scaleWidth;
           scaleWidth = ((float)targetW ) / width;
           scaleHeight = ((float) targetH) / height;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
           Matrix m = new Matrix();
           m.setRectToRect(new RectF(0,0, width, height),new RectF(0,0,(int)(width * 0.2), (int)(height * 0.2)),Matrix.ScaleToFit.CENTER);
           if (setOrg){
                OrgImg = Bitmap.createBitmap(jpg, 0, 0, width, height, m, true);
                mImageView.setImageBitmap(OrgImg);
            } else {
                resizedBitmap = Bitmap.createBitmap(jpg, 0, 0, width, height, m, true);
                mImageView.setImageBitmap(resizedBitmap);
            }
        }
    }

    /*setVars()
      Description: Loading the bitmap, setting up file directories and needed mats
                   for image processing.
    */
    private void setVars(){
        mRgba = new Mat();
        OrgMat = new Mat();
        Utils.bitmapToMat(OrgImg, mRgba);
        Utils.bitmapToMat(OrgImg, OrgMat);
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyyMMdd");

        dateLong = dateFormat2.format(new Date());
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/FootImages/");
        if (!folder.exists()) {
            folder.mkdir();
        }
        Imgproc.cvtColor(OrgMat, OrgMat, Imgproc.COLOR_RGB2BGRA);
        mWorkingFile = folder.toString() + "/Working.jpg";
        Imgcodecs.imwrite( mWorkingFile, OrgMat);
        markerMask = new Mat();
        imgGray = new Mat();
        Imgproc.cvtColor(mRgba, markerMask, COLOR_BGR2GRAY);
        Imgproc.cvtColor(markerMask, imgGray, COLOR_GRAY2BGR);
        markerMask.setTo(Scalar.all(0));

    }

    /*setPicture(Mat img)
        params: Mat: Takes an image converted into a Mat
        Description: Used to find the green sticker on the screen using HoughCircles.
        Returns: Mat of the sticker found.
    */
    private Mat findStickerHoughCircles(Mat img){

        Mat hsv = new Mat();
        Mat circles = new Mat();
        Mat result;
        Imgproc.cvtColor(img, hsv, Imgproc.COLOR_BGR2HSV);
        Core.inRange(hsv, new Scalar(37, 38, 70), new Scalar(85, 255, 200), hsv);
        Imgproc.GaussianBlur( hsv, hsv,new Size(9, 9), 2, 2 );
        Imgproc.HoughCircles(hsv, circles, Imgproc.CV_HOUGH_GRADIENT, 2, 30, 100, 100, 1,1000);
        result = new Mat(hsv.rows(), hsv.cols(), CvType.CV_8UC1, new Scalar(0,0,0));
        int circleCount = 0;
        for (int i = 0; i < circles.cols(); i++) {
            double[] circle = circles.get(0, i);
            if (circle == null) break;
            Point center = new Point(Math.round(circle[0]), Math.round(circle[1]));
            int radius = (int)Math.round(circle[2]);
            Imgproc.circle(result, center, radius, new Scalar(255, 0, 0));
            circleCount++;

        }
        List<MatOfPoint> stickerCon = new ArrayList();
        Imgproc.findContours(result, stickerCon, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        if (stickerCon.size() == 1 && circleCount == 1) {
            Imgproc.drawContours(img, stickerCon, -1, StickerColour, 2);
            Rect rect = Imgproc.boundingRect(stickerCon.get(0));
            circleLength = Math.max(rect.height, rect.width);
            foundSticker = true;
            houghFound = true;
        }else if (stickerCon.size() > 1) {
            foundSticker = false;
            houghFound = false;
            // Imgproc.drawContours(img,stickerCon,-1,new Scalar(0,0,255),2);
           errorMsg = "Could not determine sticker, found " + circleCount + " possible stickers";
        } else {
            foundSticker = false;
            houghFound = false;
           errorMsg = "Was unable to find sticker. Attempting to find just colour";
        }
        return img;
    }
    /*findStickerColor(Mat img)
        params: Mat of the image converted to a Mat
        Description: Used to find the green sticker using just colours.
        returns: Mat with the sticker found (or not)
    */
    private Mat findStickerColor(Mat img){

        Mat hsv = new Mat();
        Mat circles = new Mat();
       Imgproc.cvtColor(img, hsv, Imgproc.COLOR_BGR2HSV);
        //Core.inRange(hsv, new Scalar(37, 38, 70), new Scalar(85, 255, 200), hsv);
       // img.copyTo(hsv);
        Core.inRange(hsv, new Scalar(45, 100, 100), new Scalar(75, 255, 255), hsv);
       // Core.inRange(hsv, new Scalar(0, 0, 0), new Scalar(0, 255, 0), hsv);
        Imgproc.erode(hsv,hsv,new Mat(),new Point(),2);
        Imgproc.dilate(hsv,hsv,new Mat(),new Point(),2);


        List<MatOfPoint> stickerCon = new ArrayList();
        Imgproc.findContours(hsv, stickerCon, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        double largest = 0.0;
        int element = 0;

        for ( int i=0; i < stickerCon.size(); i++ ){
            double area = Imgproc.contourArea(stickerCon.get(i));
            if (largest < area ){
                largest = area;
                element = i;
            }
        }


        if  (largest > 0.0) {
          //Imgproc.drawContours(img,stickerCon,-1,StickerColour,2);
            Imgproc.drawContours(img,stickerCon,element,StickerColour,2);
            Rect rect = Imgproc.boundingRect(stickerCon.get(element));
            circleLength = Math.max(rect.height, rect.width);
            foundSticker = true;
     //   }else if (stickerCon.size() > 1 ) {
     //       foundSticker = false;
     //       //Imgproc.drawContours(img,stickerCon,-1,new Scalar(0,0,255),2);
     //       errorMsg = "Could not determine sticker with just colour, found " + stickerCon.size() + " possible stickers";
        } else {
            foundSticker = false;
            errorMsg = "Was unable to find sticker. Can it be clearly seen on the picture?";
        }
        return img;
    }

    /*getLocationOnImg(float x, float y
        params: x and y coord of where the user has touched
        Description: Helper method to get the absolute touched area based on the image size
        returns: Float[] of the real coord of where the user has touched
    */
    private float[] getLocationOnImg(float x, float y) {
        Matrix inverse = new Matrix();
        mImageView.getImageMatrix().invert(inverse);
        float[] touchPoint = new float[]{x, y};
        inverse.mapPoints(touchPoint);
        return touchPoint;
    }

    /*DrawOnWound()
        Description: Records and draws on the area the user has touched on the image.
        Draws on markerMask(blank) which used later to determine the location of the wound.
        Draws on mRgba to show feed back to the user on their image.
    */
    public boolean DrawOnWound() {
        mImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    prevpt = new Point(-1, -1);
                    Linecounter++;
                } else if (event.getAction() == MotionEvent.ACTION_DOWN && enableDraw) {
                    float[] touchPoint = getLocationOnImg(event.getX(), event.getY());
                    int x = (int) touchPoint[0];
                    int y = (int) touchPoint[1];
                    prevpt = new Point(x, y);

                } else if (event.getAction() == MotionEvent.ACTION_MOVE && enableDraw) {
                    float[] touchPoint = getLocationOnImg(event.getX(), event.getY());
                    int x = (int) touchPoint[0];
                    int y = (int) touchPoint[1];
                    Point pt = new Point(x, y);
                    prevpt = pt;
                    Imgproc.line(markerMask, prevpt, pt, new Scalar(255, 255, 255), 3);
                    Imgproc.line(mRgba, prevpt, pt, new Scalar(0, 255, 0), 3);
                    btnUndo.setVisibility(v.VISIBLE);
                    prevpt = pt;
                    Bitmap jpg = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
                    //Utils.matToBitmap(mRgba, jpg);
                    setPicture(jpg, false);
                }

                return true;
            }
        });
        return true;
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
                        setPicture(OrgImg, false);
                        setVars();
                        enableDraw = true;
                        Linecounter = 0;
                        btnContinue.setVisibility(view.GONE);
                        btnProcess.setVisibility(view.VISIBLE);

                    }
                });

    }

    public void NotesButton() {
        btnNotes.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        notes = Notes.getText().toString();
                        btnNotes.setVisibility(view.GONE);
                        Notes.setVisibility(view.GONE);

                    }
                });

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
    public void insertIntoDB(String dateNow, double woundsize, double woundfootpercent, boolean foundWoundsize, boolean foundImage){

        String woundStr = String.valueOf(woundsize);
        String woundfootStr = String.valueOf(woundfootpercent);
        if (woundsize > 0)  {
            if (foundWoundsize) {
                myDB.updateAnalysis(woundStr, woundfootStr, LeftOrRight, dateNow);
            }else{
                myDB.insertData(woundStr, woundfootStr, LeftOrRight, dateNow);
            }
        }

//        if (!foundImage){
        myDB.insertImage("file://" + mOrgFile, LeftOrRight, notes, dateNow);
//        myDB.insertImage("file://" + mProcessedFile, LeftOrRight, dateNow);s
//        }
        Imgcodecs.imwrite(mOrgFile, OrgMat);
        Imgcodecs.imwrite(mProcessedFile, dst);

    }

    /*ContinueProcess()
        Description: Before saving to the database determines if there are already an entry
                     for that day for the woundsize and/or image. This button will appear once
                     the image has been processed.
        returns: None
    */
    public void ContinueProcess(){
        NotesButton();
        btnContinue.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        try {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
                            String dateNow = dateFormat.format(new Date());
                            myDB = new DatabaseHelper(getApplicationContext());

                            Cursor data = myDB.getAllData(myDB.TABLE_ANALYSIS);
                            boolean foundWoundsize = false;
                            while (data.moveToNext()) {
                                String analysisDate = data.getString(data.getColumnIndex("DATE"));
                                String analysisFoot = data.getString(2);
                                if (analysisDate.equals(dateNow)){
//                                    if (analysisFoot.equals(LeftOrRight)) {
                                    foundWoundsize = true;
                                    break;
//                                    }
                                }
                            }
                            data.close();

                            Cursor imageData = myDB.getAllData(myDB.TABLE_IMAGES);
                            boolean foundImage= false;
                            while (imageData.moveToNext()) {
                                String ImageDate = imageData.getString(imageData.getColumnIndex("DATE"));

                                if (ImageDate.equals(dateNow)){
                                    foundImage = true;
                                    break;
                                }
                            }
                            imageData.close();
                            //insert entry into DB
                            if(LoopCounter == 1){
                                double firstEntry = findFirstEntry();
                                double footArea = getIntent().getDoubleExtra("FOOT_AREA",0.0);
                                double woundPercent = (footArea/area) * 100;
                                double woundFootPercent = 100.0;
                                if(firstEntry != 0.0){
                                    woundFootPercent = woundPercent/firstEntry * 100;
                                }
                                insertIntoDB(dateNow, woundFootPercent, woundPercent, foundWoundsize, foundImage);
                            }
//                            double a  = 20.0;
//                            insertIntoDB(dateNow,a, foundWoundsize, foundImage);

                            Intent intent;
                            if (woundLength > 0) {
                                intent = new Intent(getApplicationContext(), Analysis.class);
                            } else {
                                intent = new Intent(getApplicationContext(), MainActivity.class);
                            }
                            startActivity(intent);
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

    /*ProcessImage()
    Description: Used to begin the process of finding the wound and sticker. First checks to
                see if the user has drawn on the image.
    returns: None
*/
    public void ProcessImage(){
        btnProcess.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mProcessedFile != null ) {
                            if (Linecounter < 2){
                                Context context = getApplicationContext();
                                CharSequence text;
                                if(LoopCounter == 0){
                                    text = "You need to highlight on and around the wound";
                                }else{
                                    text = "You need to highlight on and around the foot";
                                }

                                int duration = Toast.LENGTH_LONG;

                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                            } else {
                                enableDraw = false;
                                Mat drawMat = new Mat();

                                Utils.bitmapToMat(resizedBitmap, drawMat);
                                File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/FootImages/");
                                if (!folder.exists()) {
                                    folder.mkdir();
                                }
                                btnContinue.setVisibility(view.VISIBLE);
                                btnProcess.setVisibility(view.GONE);

                                if(LoopCounter == 1){
                                    notesLayout.setVisibility(view.VISIBLE);
                                }
                                new ProcessImageTask().execute();
//                                Intent intent = new Intent(getApplicationContext(), ProcessFootImage.class);
//                                intent.putExtra("IMAGE_PATH", mWorkingFile);
//                                startActivity(intent);
                            }

                        }
                    }
                });
    }


    public double findFirstEntry(){
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

    /*ProcessImageTask()
        Processing the image in the background of the application.
    */
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
        protected Integer doInBackground (Integer...params) {
            Mat img1 = Imgcodecs.imread(mWorkingFile);
            dst = new Mat();

            dst = findStickerColor(img1);
            //if findStickerColor fails to find sticker, try colour only.
           // if (!foundSticker){
            //    dst = findStickerHoughCircles(img1);
          //  }
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

            Mat img = new Mat();
            img0.copyTo(img);
            Mat hierarchy = new MatOfInt4();
            List<MatOfPoint> contours1 = new ArrayList<MatOfPoint>();

            //Finds contours on markerMask (expected a black and white mat)
            Imgproc.findContours(markerMask, contours1, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
            Mat markers = new Mat(img.size(), CvType.CV_32S);
            markers.setTo(Scalar.all(0));
            double[] idx ={0.0};
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
            List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();
            Imgproc.cvtColor( wshed, scy_gry, COLOR_BGR2GRAY );

            //get the contours once outline of the wound has been found.
            Imgproc.findContours(scy_gry, contours2, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0,0));
            Rect rect = new Rect();
            double largest = 0.0;
            int secondIdx = 0;
            int largestIdx = 0;

            Imgproc.drawContours(img, contours2, -1, WoundColour, 3);
            //Measures the area of the wound. Finding the largest area
//           for ( int contourIdx=0; contourIdx < contours2.size(); contourIdx++ ){
//               area = Imgproc.contourArea(contours2.get(contourIdx));
//                if (largest < area ){
//                    largest = area;
//                    secondIdx = largestIdx;
//                    largestIdx = contourIdx;
//                }
//            }

            //If the sticker has been found either. Uses its expected size DOTSIZE and found size
            // circleLength, to figure out the distance of the picture taken.
            // then uses this and the area to determine the woundsize

            area = Imgproc.contourArea(contours2.get(secondIdx));
            double area1 = Imgproc.contourArea(contours2.get(0));
            double area2 = Imgproc.contourArea(contours2.get(1));
            double area3 = Imgproc.contourArea(contours2.get(2));
//            double area4 = Imgproc.contourArea(contours2.get(3));
//            double area5 = Imgproc.contourArea(contours2.get(4));

//                woundLength = (( DOTSIZE / circleLength) * ( DOTSIZE / circleLength)) * area;
//                woundLength = woundLength / 100.00;
//                woundLength = (double)Math.round(woundLength * 100d) / 100d;
//                //double wounds = (( DOTSIZE / circleLength) * area);
//                //double wounds = (DOTSIZE / circleLength) * woundSizePix;
//
            dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date date = new Date();
//                Log.i(TAG, "woundSize: " + woundLength);

                //Write the results on the image with a date.
            String currentDate = dateFormat.format(date).toString();
//                Imgproc.putText(img, currentDate + "  " + area + " cm2", new Point(50, 50), FONT_HERSHEY_PLAIN, 2.0,new Scalar (0,255,0), 2);

            return img;
        }

//        private Mat FindFoot(Mat img0) {
//
//            Mat img = new Mat();
//            img0.copyTo(img);
//            Mat hierarchy = new MatOfInt4();
//            List<MatOfPoint> contours1 = new ArrayList<MatOfPoint>();
//
//            //Finds contours on markerMask (expected a black and white mat)
//            Imgproc.findContours(markerMask, contours1, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
//            Mat markers = new Mat(markerMask.size(), CvType.CV_32S);
//            markers.setTo(Scalar.all(0));
//            double[] idx ={0.0};
//            int compCount = 0;
//
//            //Draw the contours (based on what the user has selected)
//            for (; compCount < contours1.size();compCount++){
//                Imgproc.drawContours(markers, contours1, compCount, new Scalar((compCount + 10) % 255, 255,87),8);
//            }
//
//            //Uses Imgproc.watershed algorithm to find the wound based on the contours
//            // by the user.
//            Imgproc.watershed( img, markers );
//            Mat wshed = new Mat(markers.size(), CvType.CV_8UC3);
//            int size = (int)(markers.total() * markers.channels());
//            int [] markersValues = new int[size];
//
//            //Stores the pixels of the watersheded Mat into an array for use below.
//            // This saves proessing time on having to access every pixel, rather than using wshed.get()
//            markers.get(0, 0, markersValues);
//
//            int rows = 0;
//            int maxcols = markers.cols();
//            int cols = 0;
//
//            //Loops through the Mat to find the outline of the wound.
//            // These are located with a -1. Colours these pixels in green when found.
//            for (int j = 0; j < size; j++) {
//                if (markersValues[j] == -1) {
//                    wshed.put(rows, cols,new double[]{255,255,255});
//                    wshed.put(rows, cols,new double[]{255,255,255});
//                    //img.put(rows, cols,new double[]{34,255,210});
//                }
//                cols++;
//                //Used to keep track of the columns and rows in the mat, as markers.get
//                // does store the data in a 2D array
//                if (cols == maxcols ){
//                    rows++;
//                    cols = 0;
//                }
//            }
//
//            Mat scy_gry = new Mat(wshed.size(),COLOR_BGR2GRAY);
//            List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();
//            Imgproc.cvtColor( wshed, scy_gry, COLOR_BGR2GRAY );
//
//            //get the contours once outline of the wound has been found.
//            Imgproc.findContours(scy_gry, contours2, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0,0));
//            Rect rect = new Rect();
//            double largest = 0.0;
//            int secondIdx = 0;
//            int largestIdx = 0;
//
//            Imgproc.drawContours(img, contours2, -1, WoundColour, 3);
//            //Measures the area of the wound. Finding the largest area
//            for ( int contourIdx=0; contourIdx < contours2.size(); contourIdx++ ){
//                double area = Imgproc.contourArea(contours2.get(contourIdx));
//                if (largest < area ){
//                    largest = area;
//                    secondIdx = largestIdx;
//                    largestIdx = contourIdx;
//                }
//            }
//
//            //If the sticker has been found either. Uses its expected size DOTSIZE and found size
//            // circleLength, to figure out the distance of the picture taken.
//            // then uses this and the area to determine the woundsize
//            if (foundSticker){
//                double area = Imgproc.contourArea(contours2.get(secondIdx));
//
//                woundLength = (( DOTSIZE / circleLength) * ( DOTSIZE / circleLength)) * area;
//                woundLength = woundLength / 100.00;
//                woundLength = (double)Math.round(woundLength * 100d) / 100d;
//                //double wounds = (( DOTSIZE / circleLength) * area);
//                //double wounds = (DOTSIZE / circleLength) * woundSizePix;
//
//                dateFormat = new SimpleDateFormat("dd/MM/yyyy");
//                Date date = new Date();
//                Log.i(TAG, "woundSize: " + woundLength);
//
//                //Write the results on the image with a date.
//                String currentDate = dateFormat.format(date).toString();
//                Imgproc.putText(img, currentDate + "  " + woundLength + " cm2", new Point(50, 50), FONT_HERSHEY_PLAIN, 2.0,new Scalar (0,255,0), 2);
//            } else {
//                woundLength = -1;
//            }
//            return img;
//        }

        @Override
        protected void onPostExecute (Integer result){
            super.onPostExecute(result);
            File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/FootImages/");
            if (!folder.exists()) {
                folder.mkdir();
            }

            //Create file names of the image. eg rgb_20170520.jpg, rgb = processed image, Org = the original image.
            mProcessedFile = folder.toString() + "/rgb_" + dateLong + ".jpg";
            mOrgFile = folder.toString() + "/Org_" + dateLong + ".jpg";

            //saves a temp file to the phone.
            Imgcodecs.imwrite(mWorkingFile, dst);

            Context context = getApplicationContext();
            int duration = Toast.LENGTH_LONG;

            //If it could not find the sticker sends a toast message warning the user.
//            if (!foundSticker){
//
//                Toast toast = Toast.makeText(context, errorMsg, duration);
//                toast.show();
//            }
            String msg = "";
      /*      if (woundLength > 0){
                msg = msg+ "Current Wound Size: " + woundLength + "cm2";
            }
            else {
                msg = msg+ "Current Wound Size: Not found";
            }

            if (foundSticker){
                msg = msg+ "\n Sticker Size: " + circleLength;
            } else {
                msg = msg+ "\n Sticker Size: Not found";
            }

            setMessageBox(msg, true); */
            //Redraws the processed image to the screen.
           Bitmap jpg = BitmapFactory
                    .decodeFile(mWorkingFile);
            setPicture(jpg, false);
            dlg.dismiss();

//            if(LoopCounter == 0){
//
//                Intent intent = new Intent(getApplicationContext(), ProcessFootImage.class);
//                intent.putExtra("IMAGE_PATH", mWorkingFile);
//                intent.putExtra("LOOP_COUNT", 1);
//                intent.putExtra("LeftOrRight", LeftOrRight);
//                intent.putExtra("FOOT_AREA", area);
//                startActivity(intent);
//            }
        }

    }
}
