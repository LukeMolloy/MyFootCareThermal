package com.example.footcare.diabeticcarethermal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

public class ThermalFeedback extends AppCompatActivity {
    CheckBox redness, pus, swelling, pain, odour, fluid, flu, noneOfTheAbove;
    Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thermal_feedback);

        redness = (CheckBox) findViewById(R.id.boxRedness);
        pus = (CheckBox) findViewById(R.id.boxPus);
        swelling = (CheckBox) findViewById(R.id.boxSwelling);
        pain = (CheckBox) findViewById(R.id.boxPain);
        odour = (CheckBox) findViewById(R.id.boxOdour);
        fluid = (CheckBox) findViewById(R.id.boxFluid);
        flu = (CheckBox) findViewById(R.id.boxFlu);
        noneOfTheAbove = (CheckBox) findViewById(R.id.boxNone);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnNext.setEnabled(false);
        btnNext.setBackgroundResource(R.drawable.button_rounded_redesign_3);
    }

    public void noneOfTheAboveActivity(View view){
        if(noneOfTheAbove.isChecked()){
            redness.setChecked(false);
            pus.setChecked(false);
            swelling.setChecked(false);
            pain.setChecked(false);
            odour.setChecked(false);
            fluid.setChecked(false);
            flu.setChecked(false);
            btnNext.setEnabled(true);
            btnNext.setBackgroundResource(R.drawable.button_rounded_redesign);
        }else{
            noneOfTheAbove.setChecked(false);
            btnNext.setEnabled(false);
            btnNext.setBackgroundResource(R.drawable.button_rounded_redesign_3);
        }
    }

    public void checkedBox(View view){
        if(redness.isChecked() || pus.isChecked() || swelling.isChecked() || pain.isChecked() || odour.isChecked() || fluid.isChecked() || flu.isChecked()){
            btnNext.setEnabled(true);
            noneOfTheAbove.setChecked(false);
            btnNext.setBackgroundResource(R.drawable.button_rounded_redesign);
        }else{
            btnNext.setEnabled(false);
            btnNext.setBackgroundResource(R.drawable.button_rounded_redesign_3);
        }
    }

    public void btnNextActivity(View view){

        if(noneOfTheAbove.isChecked()){
            finish();
            Intent intent = new Intent(getApplicationContext(), ThermalFeedbackMessage.class);
            startActivity(intent);
        }else{
            finish();
            Intent intent = new Intent(getApplicationContext(), ThermalFeedbackNegative.class);
            startActivity(intent);
        }

    }
}
