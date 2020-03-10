package com.android.cyy.demo.canvas.transform;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class TransformActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SaveRestoreView(this));
    }
}
