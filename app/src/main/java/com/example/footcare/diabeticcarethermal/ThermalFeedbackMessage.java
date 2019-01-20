package com.example.footcare.diabeticcarethermal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ThermalFeedbackMessage extends AppCompatActivity {
    Boolean noneOfTheAbove;
    ImageView symbol1, symbol2;
    TextView titleText1, titleText2, textBody1, textBody2;
    LinearLayout background1, background2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thermal_feedback_message);
        noneOfTheAbove = getIntent().getBooleanExtra("None", false);

        symbol1 = (ImageView) findViewById(R.id.symbol1);
        symbol2 = (ImageView) findViewById(R.id.symbol2);

        titleText1 = (TextView) findViewById(R.id.textView1);
        titleText2 = (TextView) findViewById(R.id.textView2);
        textBody1 = (TextView) findViewById(R.id.textView4);
        textBody2 = (TextView) findViewById(R.id.textView3);

        background1 = (LinearLayout) findViewById(R.id.backgroundtop);
        background2 = (LinearLayout) findViewById(R.id.backgroundbottom);
    }

    public void btnDone(View view){
        finish();
        Intent intent;
        intent = new Intent(getApplicationContext(), ActionHub.class);
        startActivity(intent);
    }
}
