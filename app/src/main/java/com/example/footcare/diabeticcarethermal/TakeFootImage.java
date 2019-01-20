package com.example.footcare.diabeticcarethermal;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TakeFootImage extends AppCompatActivity {
    DatabaseHelper myDB;
    TextView imageURI;
    Button btnAddData;
    Button btnviewAll;
    Button btnBrowse;
    String Date = "";
    String mCurrentPhotoPath;
    String LeftOrRight;

  static final int REQUEST_OPEN_IMAGE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LeftOrRight = getIntent().getStringExtra("LeftOrRight");
        setContentView(R.layout.activity_take_foot_image);
        myDB = new DatabaseHelper(this);
        Cursor data = myDB.getAllData(myDB.TABLE_IMAGES);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
        String dateNow = dateFormat.format(new Date());
        Boolean ImageFound = false;
        while (data.moveToNext()) {
            String ImageDate = data.getString(data.getColumnIndex("DATE"));
            if (ImageDate.equals(dateNow)){
                ImageFound = true;
                break;
//                if (data.getString(3).equals("Left")) {
//                    if (LeftOrRight.equals("Left")) {
//                        ImageFound = true;
//                        break;
//                    }
//                }
//
//                if (data.getString(3).equals("Right")) {
//                    if (LeftOrRight.equals("Right")) {
//                        ImageFound = true;
//                        break;
//                    }
//                }
            }
        }
        data.close();

        if (ImageFound) {
            entryFound();
        }
       btnBrowse = (Button)findViewById(R.id.action_open_img);
       mCurrentPhotoPath = "";
       browse();
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

//    public void AddData(){
//        btnAddData.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
//                        String dateNow = dateFormat.format(new Date());
//                        boolean isInserted = myDB.insertImage(imageURI.getText().toString(), dateNow);
//                        if(isInserted = true)
//                            Toast.makeText(TakeFootImage.this,"Data Inserted",Toast.LENGTH_LONG).show();
//                        else
//                            Toast.makeText(TakeFootImage.this,"Data not Inserted",Toast.LENGTH_LONG).show();
//                    }
//                }
//        );
//    }

    public void viewAll() {
        btnviewAll.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_IMAGES);
                        if(res.getCount() == 0){
                            showMessage("Error:","Nothing Found");
                            return;
                        }
                        StringBuffer buffer = new StringBuffer();
                        while (res.moveToNext()) {
                            buffer.append("ID :"+ res.getString(0)+"\n");
                            buffer.append("Image URI:"+ res.getString(2)+"\n");
                            buffer.append("Date:"+ res.getString(3)+"\n");
                        }
                        //show data
                        showMessage("Data",buffer.toString());
                    }
                }
        );
    }
    public void entryFound(){

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Today's entry found")
                .setMessage("You should only add 1 entry per day. Would you like to overwrite exiting?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    //Continue
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


   public void SendImageToProcess(String mCurrentPhotoPath){
       Intent intent = new Intent(this, ProcessFootImage_v2.class);
       intent.putExtra("IMAGE_PATH", mCurrentPhotoPath);
       intent.putExtra("LeftOrRight", LeftOrRight);
       startActivity(intent);
   }

   @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
          super.onActivityResult(requestCode, resultCode, data);

          if (requestCode == REQUEST_OPEN_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
              Uri imgUri = data.getData();
              String[] filePathColumn = {MediaStore.Images.Media.DATA};

              Cursor cursor = getContentResolver().query(imgUri, filePathColumn,
                      null, null, null);
              cursor.moveToFirst();

              int colIndex = cursor.getColumnIndex(filePathColumn[0]);
              mCurrentPhotoPath = cursor.getString(colIndex);
              SendImageToProcess(mCurrentPhotoPath);
              cursor.close();

              //setPic();
          }
      }

    public void browse(){
        btnBrowse.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                          Intent getPictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
//                          getPictureIntent.setType("image/*");
//
//                          Intent pickPictureIntent = new Intent(Intent.ACTION_PICK,
//                                  MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                          Intent getPictureIntent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                          getPictureIntent.setType("image/*");

                          Intent chooserIntent = Intent.createChooser(getPictureIntent, "Select Image");

//                          chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{
//                                  pickPictureIntent
//                          });
                          startActivityForResult(getPictureIntent, REQUEST_OPEN_IMAGE);

                    }
                }
        );
        }

    public void openCamera(View view){
        finish();
        Intent intent = new Intent(this, CameraView.class);
        intent.putExtra("LeftOrRight", LeftOrRight);
        startActivity(intent);
    }

    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

}
