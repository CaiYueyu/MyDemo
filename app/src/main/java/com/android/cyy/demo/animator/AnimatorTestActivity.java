package com.android.cyy.demo.animator;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.android.cyy.demo.R;

public class AnimatorTestActivity extends AppCompatActivity {
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.text);
        scale(textView);

    }

    public void scale(View view) {
//
        MobjectAnimator objectAnimator = MobjectAnimator.
                OfFloat(textView, "scaleX", 1f,2f);
        objectAnimator.setDuration(3000);
        objectAnimator.setmInterPolator(new LinearInterpolator());
        objectAnimator.start();
    }
}
