package com.android.cyy.demo.animator;

public class LinearInterpolator implements TimeInterpolator{
    @Override
    public float getInterpolation(float input) {
        return input;
    }
}
