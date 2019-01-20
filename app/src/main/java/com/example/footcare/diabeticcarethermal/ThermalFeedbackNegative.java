package com.example.footcare.diabeticcarethermal;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Objects;

public class ThermalFeedbackNegative extends AppCompatActivity {

    DatabaseHelper myDB;
    TextView c1Name, c1Number, c2Name, c2Number, c3Name, c3Number;
    LinearLayout number1, number2, number3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myDB = new DatabaseHelper(getApplicationContext());
        setContentView(R.layout.activity_thermal_feedback_negative);

        c1Name = (TextView) findViewById(R.id.c1Name);
        c1Number = (TextView) findViewById(R.id.c1Number);
        c2Name = (TextView) findViewById(R.id.c2Name);
        c2Number = (TextView) findViewById(R.id.c2Number);
        c3Name = (TextView) findViewById(R.id.c3Name);
        c3Number = (TextView) findViewById(R.id.c3Number);

        number1 = (LinearLayout) findViewById(R.id.number1);

        getContactInfo();
    }

    public void getContactInfo(){
        int count = 0;
        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_SETTINGS);
        while (res.moveToNext()) {
            String a = res.getString(0);
            String b = res.getString(1);
            String c = res.getString(2);
            if (!Objects.equals(res.getString(1), null)) {
                if (!Objects.equals(res.getString(1), "")) {
                    if (count == 1) {
                        c1Name.setText(res.getString(1));
                        c1Number.setText(res.getString(2));
                        c1Name.setVisibility(View.VISIBLE);
                        c1Number.setVisibility(View.VISIBLE);
                        Linkify.addLinks(c1Number, Linkify.PHONE_NUMBERS);
                    } else if (count == 2) {
                        c2Name.setText(res.getString(1));
                        c2Number.setText(res.getString(2));
                        c2Name.setVisibility(View.VISIBLE);
                        c2Number.setVisibility(View.VISIBLE);
                        Linkify.addLinks(c2Number, Linkify.PHONE_NUMBERS);
                    } else if (count == 3) {
                        c3Name.setText(res.getString(1));
                        c3Number.setText(res.getString(2));
                        c3Name.setVisibility(View.VISIBLE);
                        c3Number.setVisibility(View.VISIBLE);
                        Linkify.addLinks(c3Number, Linkify.PHONE_NUMBERS);
                    }
                    count++;
                }
            }
        }
    }

    public void btnDone(View view){
        finish();
        Intent intent;
        intent = new Intent(getApplicationContext(), ActionHub.class);
        startActivity(intent);
    }
}
