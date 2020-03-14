package com.android.cyy.demo.animator;

public class MyFloatKeyframe {
    float mFraction;
    Class mValueType;
    float mValue;

    public MyFloatKeyframe(float fractoin,float value){
        mFraction = fractoin;
        mValue = value;
        mValueType = float.class;
    }
    public Object getmValue(){
        return mValue;
    }
    public float getmFraction(){
        return mFraction;
    }
}
