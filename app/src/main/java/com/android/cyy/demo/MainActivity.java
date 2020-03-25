package com.android.cyy.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.android.cyy.demo.canvas.bezier.DragBubbleView;
import com.android.cyy.demo.svg.MapView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new MapView(this));
    }
}
