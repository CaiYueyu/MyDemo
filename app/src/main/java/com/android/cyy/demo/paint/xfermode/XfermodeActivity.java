package com.android.cyy.demo.paint.xfermode;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class XfermodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new XfermodeEraserView(this));
    }
}
