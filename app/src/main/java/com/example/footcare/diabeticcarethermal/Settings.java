package com.example.footcare.diabeticcarethermal;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.DateFormat;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static android.app.AlarmManager.INTERVAL_DAY;

public class Settings extends AppCompatActivity {

    EditText c1Name, c1Number, c2Name, c2Number, c3Name, c3Number;
    Spinner spinner;
    Button update;
    DatabaseHelper myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean populatedEntries = false;
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myDB = new DatabaseHelper(getApplicationContext());

        spinner = (Spinner) findViewById(R.id.spinner);
        c1Name = (EditText) findViewById(R.id.c1Name);
        c1Number = (EditText) findViewById(R.id.c1Number);
        c2Name = (EditText) findViewById(R.id.c2Name);
        c2Number = (EditText) findViewById(R.id.c2Number);
        c3Name = (EditText) findViewById(R.id.c3Name);
        c3Number = (EditText) findViewById(R.id.c3Number);
        update = (Button) findViewById(R.id.update);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.left_right, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if(!checkEntries()){
            myDB.insertContact("left", "");
            myDB.insertContact("b", "b");
            myDB.insertContact("c", "c");
            myDB.insertContact("d", "d");
        }

        populatedEntries = populateFields();
    }


    public void btnUpdate(View view){
        myDB.updateContact(spinner.getSelectedItem().toString(), "", 1);
        myDB.updateContact(c1Name.getText().toString(), c1Number.getText().toString(), 2);
        myDB.updateContact(c2Name.getText().toString(), c2Number.getText().toString(), 3);
        myDB.updateContact(c3Name.getText().toString(), c3Number.getText().toString(), 4);

        Intent intent;
        intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    public boolean checkEntries(){
        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_SETTINGS);
        boolean result = false;
        while (res.moveToNext()) {
            res.getColumnNames();
            if (!Objects.equals(res.getString(2), null)) {
                result = true;
            }
        }
        return result;
    }

    public boolean populateFields(){
        int count = 0;
        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_SETTINGS);
        boolean result = true;
        while (res.moveToNext()) {
            String a = res.getString(0);
            String b = res.getString(1);
            String c = res.getString(2);
            if (!Objects.equals(res.getString(1), null)) {
                if(count == 0) {
                    if(Objects.equals(res.getString(1), "Left")){
                        spinner.setSelection(0);
                    }else{
                        spinner.setSelection(1);
                    }
                }else if(count == 1){
                    c1Name.setText(res.getString(1));
                    c1Number.setText(res.getString(2));
                }else if(count == 2){
                    c2Name.setText(res.getString(1));
                    c2Number.setText(res.getString(2));
                }else if(count == 3){
                    c3Name.setText(res.getString(1));
                    c3Number.setText(res.getString(2));
                }
                count++;
            }
        }
        return result;
    }
}
