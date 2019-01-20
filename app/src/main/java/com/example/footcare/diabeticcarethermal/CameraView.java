package com.example.footcare.diabeticcarethermal;

import java.io.File;
import java.security.Policy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;
import org.opencv.videoio.Videoio;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.hardware.Camera.Size;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

public class CameraView extends Activity implements CvCameraViewListener2, OnTouchListener {
    private int direction_counter = 0;
    private static final String TAG = "OCVSample::Activity";
    private String LeftOrRight;
    private Tutorial3View mOpenCvCameraView;
    private List<Size> mResolutionList;
    private MenuItem[] mEffectMenuItems;
    private SubMenu mColorEffectsMenu;
    private MenuItem[] mResolutionMenuItems;
    private SubMenu mResolutionMenu;
    private Scalar skin_ycrcb_min = new Scalar(0, 133, 77);
    private Scalar skin_ycrcb_max = new Scalar(255, 173, 127);
    private boolean AutoPhotoTaker = false;
    private boolean togglelight = true;
    private Switch AutoPhotoTakerSwitch, togglelightSwitch;
    private Button continueButton, retryButton;
    private ImageButton takePicButton;
    private LinearLayout lLayout;
    private Drawable leftFoot, rightFoot;
    private Mat temp_mat;
    private boolean img_taken = false;

    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                   // mOpenCvCameraView.enableFlash();
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(CameraView.this);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    public CameraView() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @TargetApi(23)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.hardware.camera",
                "android.hardware.camera.autofocus",
                "android.permission.FLASHLIGHT"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }

    /**
     * Called when the activity is first created.
     */
    static {
        OpenCVLoader.initDebug();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        if (shouldAskPermissions()) {
            askPermissions();
        }
        temp_mat = new Mat();


        setContentView(R.layout.tutorial3_surface_view);
        LeftOrRight = getIntent().getStringExtra("LeftOrRight");
        continueButton = (Button) findViewById(R.id.continueButton);
        retryButton = (Button) findViewById(R.id.retryButton);
        takePicButton = (ImageButton) findViewById(R.id.btnTakePic);
        continueButton.setVisibility(View.GONE);
        lLayout = (LinearLayout) findViewById((R.id.lLayout));
        retryButton.setVisibility(View.GONE);

        leftFoot = getResources().getDrawable(getResources().getIdentifier("@drawable/foot4_right", "drawable", getPackageName()));
        rightFoot = getResources().getDrawable(getResources().getIdentifier("@drawable/foot4_left", "drawable", getPackageName()));

        lLayout.setBackground(rightFoot);

        Switch switchLeftRight = (Switch) findViewById(R.id.leftright);
        final Tutorial3View cameraImage = (Tutorial3View) findViewById(R.id.tutorial3_activity_java_surface_view);
        switchLeftRight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (!isChecked) {
                    Toast.makeText(getApplicationContext(), "Left Foot", Toast.LENGTH_SHORT).show();
                    lLayout.setBackground(rightFoot);
                } else {
                    Toast.makeText(getApplicationContext(), "Right Foot", Toast.LENGTH_SHORT).show();
                    lLayout.setBackground(leftFoot);
                }

            }
        });
        AutoPhotoTakerSwitch = (Switch) findViewById(R.id.AutoTakerSwitchID);
//       togglelightSwitch = (Switch) findViewById(R.id.togglelight);
//        //set the switch to ON
        AutoPhotoTakerSwitch.setChecked(AutoPhotoTaker);
//        togglelightSwitch.setChecked(false);
//        //attach a listener to check for changes in state
        AutoPhotoTakerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(getApplicationContext(), "Auto Photo Taker On", Toast.LENGTH_SHORT).show();
                    AutoPhotoTaker = true;
                } else {
                    Toast.makeText(getApplicationContext(), "Auto Photo Taker Off", Toast.LENGTH_SHORT).show();
                    AutoPhotoTaker = false;
                }

            }
        });

//        togglelightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView,
//                                         boolean isChecked) {
//                if (isChecked) {
//                    Toast.makeText(getApplicationContext(), "Flashlight On", Toast.LENGTH_SHORT).show();
//                    togglelight = true;
//                } else {
//                    Toast.makeText(getApplicationContext(), "Flashlight Off", Toast.LENGTH_SHORT).show();
//                    togglelight = false;
//                }
//
//            }
//        });
//        togglelight = true;
        retryButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                img_taken = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        retryButton.setVisibility(View.GONE);
                        continueButton.setVisibility(View.GONE);
                        takePicButton.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/FootImages/");
                if (!folder.exists()) {
                    folder.mkdir();
                }


                Imgproc.cvtColor(temp_mat, temp_mat, Imgproc.COLOR_RGB2BGRA);
                Mat tmp = new Mat();
                temp_mat.copyTo(tmp);

                Core.flip(tmp.t(), tmp, 1);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String formatedDate = sdf.format(new Date());

                String mWorkingFile = folder.toString() + "/" + LeftOrRight + formatedDate + ".jpg";

                File file = new File(mWorkingFile);
                if(file.exists()){
                    file.delete();
                }

                Imgcodecs.imwrite( mWorkingFile, tmp );
                tmp.release();

//                Intent intent = new Intent(getApplicationContext(), ProcessFootImage_v2.class);
//                intent.putExtra("IMAGE_PATH", mWorkingFile);
//                intent.putExtra("LOOP_COUNT", 0);
//                intent.putExtra("LeftOrRight", LeftOrRight);
//                startActivity(intent);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("IMAGE_PATH", mWorkingFile);
                resultIntent.putExtra("LOOP_COUNT", 0);
                resultIntent.putExtra("LeftOrRight", LeftOrRight);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();

            }
        });


        takePicButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if (!img_taken) {
                    Log.i(TAG, "onTouch event");
                    img_taken = true;
                    retryButton.setVisibility(v.VISIBLE);
                    continueButton.setVisibility(v.VISIBLE);
                    takePicButton.setVisibility(v.GONE);
                }
            }

        });

//        check the current state before we display the screen
        if (AutoPhotoTakerSwitch.isChecked()) {
            Toast.makeText(getApplicationContext(), "Auto Photo Taker On", Toast.LENGTH_SHORT).show();
            takePicButton.setVisibility(View.GONE);
            AutoPhotoTaker = true;
        } else {
            Toast.makeText(getApplicationContext(), "Auto Photo Taker Off", Toast.LENGTH_SHORT).show();
            takePicButton.setVisibility(View.VISIBLE);
            AutoPhotoTaker = false;
        }
//      if (togglelightSwitch.isChecked()) {
//            togglelight = true;
//        } else {
//            togglelight = false;
//        }


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Context context = this;
        PackageManager pm = context.getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Log.e("err", "Device has no camera!");
            return;
        }
//        mOpenCvCameraView.setMinimumHeight(700);
//        mOpenCvCameraView.setMinimumWidth(700);
//        mOpenCvCameraView.setMaxFrameSize(2960, 1440);

        mOpenCvCameraView = (Tutorial3View) findViewById(R.id.tutorial3_activity_java_surface_view);
//        mOpenCvCameraView.setMaxFrameSize(400, 400);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
        temp_mat.release();
    }

    public void onCameraViewStarted(int width, int height) {

    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        if (togglelight){
            mOpenCvCameraView.turnOnTheFlash();
        } else {
             mOpenCvCameraView.turnOffTheFlash();
        }

        if (img_taken) {
            return temp_mat;
        } else {
            if (!AutoPhotoTaker) {
                inputFrame.rgba().copyTo(temp_mat);
                return inputFrame.rgba();
            } else {
                Context ctx = getApplicationContext();
                Mat ycrcb_frame = new Mat();
                Mat rbga_frame;
                Mat bgr_frame = new Mat();
                Mat skin_only_frame = new Mat();
                rbga_frame = inputFrame.rgba();
                Imgproc.cvtColor(rbga_frame, bgr_frame, Imgproc.COLOR_RGBA2BGR);
                Imgproc.cvtColor(bgr_frame, ycrcb_frame, Imgproc.COLOR_BGR2YCrCb);
                bgr_frame.release();
                Core.inRange(ycrcb_frame, skin_ycrcb_min, skin_ycrcb_max, skin_only_frame);
                ycrcb_frame.release();
                List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
                Mat hierarchy = new Mat();
                // Finding contours in image
                Imgproc.findContours(skin_only_frame, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                Rect largest = new Rect();

                // Finding the largest rectangle and save as largest
                for (int idx = 0; idx < contours.size(); idx++) {
                    Rect rect = Imgproc.boundingRect(contours.get(idx));
                    if (rect.area() > largest.area()) {
                        largest = rect;
                    }
                }
                rbga_frame.copyTo(temp_mat);
                int right, left, up = 0;

                // Drawing of the rectangle
                Imgproc.rectangle(rbga_frame, new Point(largest.x, largest.y), new Point(largest.x + largest.width, largest.y + largest.height), new Scalar(255, 0, 0));
                skin_only_frame.release();

                if (direction_counter == 10) {
                    double mat_area = rbga_frame.width() * rbga_frame.height();
                    double per_of_mat = (largest.area() / mat_area) * 100.0;

                    if (per_of_mat > 50.0 && per_of_mat < 60.0) {
                        Log.i(TAG, "Image Captured");
                        MediaPlayer mediaPlayer = MediaPlayer.create(ctx, R.raw.success);
                        mediaPlayer.start();
                        img_taken = true;
                        runOnUiThread(new Runnable() {
                          @Override
                            public void run() {
                                retryButton.setVisibility(View.VISIBLE);
                                continueButton.setVisibility(View.VISIBLE);
                                takePicButton.setVisibility(View.GONE);
                           }
                        });
                        return rbga_frame;
                        // succ
                    } else if (per_of_mat > 60.0) {
                        Log.i(TAG, "Raise");
                        MediaPlayer mediaPlayer = MediaPlayer.create(ctx, R.raw.raise);
                        mediaPlayer.start();
                        // lower
                    } else if (per_of_mat < 50.0) {
                        Log.i(TAG, "Lower");
                        MediaPlayer mediaPlayer = MediaPlayer.create(ctx, R.raw.lower);
                        mediaPlayer.start();
                        // raise
                    }
                    direction_counter = 0;
                } else {
                    direction_counter++;
                }


                return rbga_frame;
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        List<String> effects = mOpenCvCameraView.getEffectList();

        if (effects == null) {
            Log.e(TAG, "Color effects are not supported by device!");
            return true;
        }
       // mOpenCvCameraView.set(Videoio.CV_CAP_PROP_ANDROID_FLASH_MODE, CV_CAP_ANDROID_FLASH_MODE_TORCH);

        mColorEffectsMenu = menu.addSubMenu("Color Effect");
        mEffectMenuItems = new MenuItem[effects.size()];

        int idx = 0;
        ListIterator<String> effectItr = effects.listIterator();
        while(effectItr.hasNext()) {
            String element = effectItr.next();
            mEffectMenuItems[idx] = mColorEffectsMenu.add(1, idx, Menu.NONE, element);
            idx++;
        }

        mResolutionMenu = menu.addSubMenu("Resolution");

        mResolutionList = mOpenCvCameraView.getResolutionList();
        mResolutionMenuItems = new MenuItem[mResolutionList.size()];

        ListIterator<Size> resolutionItr = mResolutionList.listIterator();
        idx = 0;
        while(resolutionItr.hasNext()) {
            Size element = resolutionItr.next();
            mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE,
                    Integer.valueOf(element.width).toString() + "x" + Integer.valueOf(element.height).toString());
            idx++;
        }
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item.getGroupId() == 1)
        {
            mOpenCvCameraView.setEffect((String) item.getTitle());
            Toast.makeText(this, mOpenCvCameraView.getEffect(), Toast.LENGTH_SHORT).show();
        }
        else if (item.getGroupId() == 2)
        {
            int id = item.getItemId();
            //Size    =mResolutionList.get(id);
            Size resolution = mOpenCvCameraView.getResolution();
            mOpenCvCameraView.setResolution(resolution);
            String caption = Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
            Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
        }

        return true;
    }



    @SuppressLint("SimpleDateFormat")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!img_taken) {
            Log.i(TAG, "onTouch event");
            //img_taken = true;
            //retryButton.setVisibility(v.VISIBLE);
           // continueButton.setVisibility(v.VISIBLE);
            return false;
        } else {
            return false;
        }

        /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());
        String fileName = Environment.getExternalStorageDirectory().getPath() +
                "/sample_picture_" + currentDateandTime + ".jpg";
        mOpenCvCameraView.takePicture(fileName);
        Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();*/

    }
}
