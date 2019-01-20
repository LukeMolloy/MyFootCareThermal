package com.example.footcare.diabeticcarethermal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class LeftOrRight extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_left_or_right);
    }

    public void leftFoot(View view) {
        Intent intent = new Intent(this, TakeFootImage.class);
        intent.putExtra("LeftOrRight", "Left");
        startActivity(intent);
    }

    public void rightFoot(View view) {
        Intent intent = new Intent(this, TakeFootImage.class);
        intent.putExtra("LeftOrRight", "Right");
        startActivity(intent);
    }
}
