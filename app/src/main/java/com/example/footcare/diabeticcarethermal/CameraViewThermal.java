package com.example.footcare.diabeticcarethermal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.flir.flironesdk.*;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;

import static org.opencv.imgproc.Imgproc.COLOR_BGR2HSV;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2RGB;
import static org.opencv.imgproc.Imgproc.COLOR_GRAY2BGR;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2HSV;

public class CameraViewThermal extends AppCompatActivity implements Device.Delegate, FrameProcessor.Delegate{
    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    private FrameProcessor frameProcessor;
    GLSurfaceView thermalSurfaceView;
    Device flirDevice;
    boolean imageCaptureRequested = false;
    private String lastSavedPath;
    int numPhotos = 0;
    double footTemp;
    private String LeftOrRight;
    Boolean RGBImage = false;
    Boolean ThermalImage = false;
    Boolean KelvinTempImage = false;
    String imagePathVisible, imagePathThermal;
    int[] thermalPixels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view_thermal);

        LeftOrRight = getIntent().getStringExtra("LeftOrRight");
        RenderedImage.ImageType defaultImageType = RenderedImage.ImageType.ThermalRGBA8888Image;

        frameProcessor = new FrameProcessor(this, this, EnumSet.of(RenderedImage.ImageType.ThermalRadiometricKelvinImage, RenderedImage.ImageType.VisibleAlignedRGBA8888Image, RenderedImage.ImageType.ThermalRGBA8888Image), true);
        frameProcessor.setImagePalette(RenderedImage.Palette.Gray);
        frameProcessor.setGLOutputMode(defaultImageType);
        frameProcessor.setMSXDistance(0.15f);

        thermalSurfaceView = (GLSurfaceView) findViewById(R.id.imageView);
        thermalSurfaceView.setPreserveEGLContextOnPause(true);
        thermalSurfaceView.setEGLContextClientVersion(2);
        thermalSurfaceView.setRenderer(frameProcessor);
        thermalSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        thermalSurfaceView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS);

        if(Objects.equals(LeftOrRight, "left")){
            thermalSurfaceView.setBackground(getResources().getDrawable(R.drawable.foot4_left));
        }else{
            thermalSurfaceView.setBackground(getResources().getDrawable(R.drawable.foot4_right));
        }
    }

    public void onClickBtn(View v)
    {
        imageCaptureRequested = true;
    }

    @Override
    public void onPause(){
        super.onPause();
        thermalSurfaceView.onPause();
        flirDevice.stopDiscovery();
    }
    @Override
    public void onResume(){
        super.onResume();
        thermalSurfaceView.onResume();
        flirDevice.startDiscovery(this,this);
    }

    @Override
    public void onTuningStateChanged(Device.TuningState tuningState) {

    }

    @Override
    public void onAutomaticTuningChanged(boolean b) {

    }

    @Override
    public void onDeviceConnected(Device device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.imageView).setVisibility(View.VISIBLE);
                findViewById(R.id.pleaseConnect).setVisibility(View.GONE);
                findViewById(R.id.btn).setVisibility(View.VISIBLE);
            }
        });
        flirDevice = device;
        flirDevice.setTorchMode(true);
//        flirDevice.setPowerUpdateDelegate((Device.PowerUpdateDelegate) this);
//        flirDevice.startFrameStream(this);
        final Intent resultIntent = new Intent(this, ActionHub.class);
        flirDevice.startFrameStream(new Device.StreamDelegate() {
            @Override
            public void onFrameReceived(Frame frame) {
                if (!imageCaptureRequested) {
                    thermalSurfaceView.requestRender();
                }
                frameProcessor.processFrame(frame, FrameProcessor.QueuingOption.SKIP_FRAME);
                if(RGBImage && ThermalImage && KelvinTempImage){
                    imageCaptureRequested = false;
                    Mat matThermal = Imgcodecs.imread( imagePathThermal );
                    Mat matRGB = Imgcodecs.imread( imagePathVisible );
                    footTemp = avgTempFinder(matThermal, matRGB);
                    resultIntent.putExtra("IMAGE_PATH", imagePathVisible);
                    resultIntent.putExtra("FOOT_TEMP", footTemp);
                    resultIntent.putExtra("LeftOrRight", LeftOrRight);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }

            }
        });
    }

    @Override
    public void onDeviceDisconnected(Device device) {
        final Intent resultIntent = new Intent(this, ActionHub.class);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.imageView).setVisibility(View.GONE);
                findViewById(R.id.pleaseConnect).setVisibility(View.VISIBLE);
            }
        });

        flirDevice = null;
        setResult(Activity.RESULT_CANCELED, resultIntent);
        finish();
    }

    @Override
    public void onFrameProcessed(final RenderedImage renderedImage) {
        if (imageCaptureRequested) {
//            imageCaptureRequested = false;
            final Context context = this;
            final String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String formatedDate = sdf.format(new Date());
            final String fileName = LeftOrRight + formatedDate + ".jpg";

            if(renderedImage.imageType() == RenderedImage.ImageType.VisibleAlignedRGBA8888Image) {
                Bitmap recievedImage = renderedImage.getBitmap();
                Mat mat = new Mat();
                Bitmap bmp32 = recievedImage.copy(Bitmap.Config.ARGB_8888, true);
                Utils.bitmapToMat(bmp32, mat);
                Imgproc.cvtColor(mat, mat, COLOR_BGR2RGB);
                imagePathVisible = saveMat(mat, "Visible");
                RGBImage = true;
            }else if(renderedImage.imageType() == RenderedImage.ImageType.ThermalRGBA8888Image){
                Bitmap recievedImage = renderedImage.getBitmap();
                Mat mat = new Mat();
                Bitmap bmp32 = recievedImage.copy(Bitmap.Config.ARGB_8888, true);
                Utils.bitmapToMat(bmp32, mat);
                Imgproc.cvtColor(mat, mat, COLOR_BGR2RGB);
                imagePathThermal = saveMat(mat, "Thermal");
                ThermalImage = true;
            }else if(renderedImage.imageType() == RenderedImage.ImageType.ThermalRadiometricKelvinImage){
                thermalPixels = renderedImage.thermalPixelValues();
//                Mat mat = Imgcodecs.imread( imagePathThermal );
//                double test = avgTempFinder(mat);
//                footTemp = test;
                KelvinTempImage = true;
            }
        }
    }

    public String saveMat(Mat mat, String type){
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ThermalFootImages/");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        //Creates the label for the thermal image
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formatedDate = sdf.format(new Date());
        String fileName = "/FLIROne-" + type + LeftOrRight + formatedDate + ".jpg";

        Imgcodecs.imwrite( folder.toString()+fileName, mat );

        return folder.toString()+fileName;
    }

    public double avgTempFinder(Mat thermalImage, Mat visibleImage){
        double avgTemp = 0.0;
        Mat threshImage = new Mat();
        Mat skinMask = new Mat();
        Mat reshapedThreshImage = new Mat();
        final Point anchor = new Point(-1, -1);
        Imgproc.cvtColor(thermalImage, thermalImage, Imgproc.COLOR_RGB2GRAY);
        threshImage = detectSkin(visibleImage);
//        threshImage = centerPixels(visibleImage);
//        Imgproc.cvtColor(threshImage, threshImage, Imgproc.COLOR_RGB2GRAY);
//        Imgproc.threshold(thermalImage,threshImage,180.0,255.0, Imgproc.THRESH_OTSU);
//        threshImage = removeNotSkinPositives(threshImage, visibleImage, thermalPixels);
//        Imgproc.erode(threshImage, threshImage, sImgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,3)),anchor,10);
//        Imgproc.dilate(threshImage, threshImage, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(4,4)),anchor,5);
        threshImage = pictureBorder(threshImage);
        saveMat(threshImage, "Thresh");

        reshapedThreshImage = threshImage.reshape(0,307200);

//        for(int i = 0; i < threshImage.rows(); i++){
//            for(int x = 0; x < threshImage.cols(); x++){
//
//            }
//        }
        for(int i = 0; i < reshapedThreshImage.rows();){
            if(reshapedThreshImage.get(i,0)[0] == 0.0){
                thermalPixels[i] = 0;
            }
            i++;
        }

        saveMat(threshImage, "Thresh");

        avgTemp = calculateTempValue(thermalPixels);
        return avgTemp;
    }

    public double[] skinColour(Mat visibleImage){
        Imgproc.cvtColor(visibleImage, visibleImage, COLOR_BGR2RGB);
        int centerX = visibleImage.width()/2;
        int centery = visibleImage.height()/2;

        double R = 0.0;
        double B = 0.0;
        double G = 0.0;

        for(int i = centerX-10; i < centerX+10; i++){
            for(int j = centery-10; j < centery+10; j++){
                double[] colour = visibleImage.get(i, j);
                R = R + colour[0];
                G = G + colour[1];
                B = B + colour[2];
            }
        }

        double[] avgColour = {R/400, G/400, B/400};

        return avgColour;
    }

    public Mat centerPixels(Mat threshImage){
        int centerX = threshImage.width()/2;
        int centery = threshImage.height()/2;
        for(int i = 0; i < threshImage.cols(); i++) {
            for (int j = 0; j < threshImage.rows(); j++) {
                threshImage.put(i,j,0);
            }
        }
        for(int i = centerX-15; i < centerX+15; i++) {
            for (int j = centery; j < centery+30; j++) {
                threshImage.put(i,j,255);
            }
        }

        return threshImage;
    }

    public Mat pictureBorder(Mat threshImage){
        for(int i = 0; i < threshImage.cols(); i++){
            for(int j = 0; j < threshImage.rows(); j++){
                if(i <= 10){
                    threshImage.put(j,i,0);
                }else if(i >= threshImage.cols()-10){
                    threshImage.put(j,i,0);
                }
                if(j <= 10){
                    threshImage.put(j,i,0);
                }else if(j >= threshImage.rows()-10){
                    threshImage.put(j,i,0);
                }

//                double[] colour = visibleImage.get(j, i);
//                if(colour[0] >= 255 && colour[1] >= 255 && colour[2] >= 20){
//                    threshImage.put(j,i,0);
//                }else if(colour[0] <= 80 && colour[1] <= 48 && colour[2] <= 0){
//                    threshImage.put(j,i,0);
//                }

            }
        }
        return threshImage;
    }

    public Mat detectSkin(Mat visibleImage){
        Mat convertedImage = new Mat();
        Mat skinMask = new Mat();
        Imgproc.GaussianBlur(visibleImage,visibleImage,new Size(3,3),0);

//        Imgproc.cvtColor(visibleImage, convertedImage, COLOR_BGR2HSV);
        double[] sampleColour = skinColour(visibleImage);
//        saveMat(convertedImage, "skinmask2");
//
        Scalar lower = new Scalar(sampleColour[0] - 10, sampleColour[1] - 20, sampleColour[2] - 50);
        Scalar upper = new Scalar(sampleColour[0] + 10, sampleColour[1] + 20, sampleColour[2] + 50);
//        Scalar lower = new Scalar(80, 48, 0);
//        Scalar upper = new Scalar(255, 255, 20);

        Core.inRange(visibleImage, lower, upper, skinMask);

        saveMat(skinMask, "skinmask");
        return skinMask;
    }

    public Mat removeNotSkinPositives(Mat threshImage, Mat visibleImage, int[] thermalPixels){
        double[] sampleColour = skinColour(visibleImage);
        for(int i = 0; i < threshImage.cols(); i++){
            for(int j = 0; j < threshImage.rows(); j++){
//                double[] colour = visibleImage.get(j, i);
//                if(colour[0] >= sampleColour[0]+50 && colour[1] >= sampleColour[1]+50 && colour[2] >= sampleColour[2]+50){
//                    threshImage.put(j,i,0);
//                }else if(colour[0] <= sampleColour[0]-50 && colour[1] <= sampleColour[1]-50 && colour[2] <= sampleColour[2]-50){
//                    threshImage.put(j,i,0);
//                }

                if(((thermalPixels[j+(480*i)]/100) - 273.15) < 15){
                    threshImage.put(j,i,0);
                }else if(((thermalPixels[j+(480*i)]/100) - 273.15) > 37){
                    threshImage.put(j,i,0);
                }
            }
        }
        return threshImage;
    }

    public double calculateTempValue(int[] thermalPixels){
        double avgTemp = 0.0;
        double testTemp = 0.0;
        int numEntries = 0;
        for (int i = 0; i < thermalPixels.length;){
            if(thermalPixels[i] != 0){
                testTemp = (thermalPixels[i]/100) - 273.15;
                avgTemp = thermalPixels[i] + avgTemp;
                numEntries++;
            }
            i++;
        }
        avgTemp = avgTemp/(numEntries);
        return (avgTemp/100) - 273.15;
    }
}
