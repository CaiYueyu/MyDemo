package com.android.cyy.demo.animator;


import android.animation.FloatEvaluator;
import android.animation.TypeEvaluator;

import java.util.Arrays;
import java.util.List;

public class MyKeyFrameSet {

    //    类型估值器
    TypeEvaluator mEvaluator;
    private List<MyFloatKeyframe> myFloatKeyframeList;
    private MyFloatKeyframe mFirstFrame;

    public MyKeyFrameSet(MyFloatKeyframe ...list) {
        mFirstFrame = list[0];
        myFloatKeyframeList = Arrays.asList(list);
        mEvaluator = new FloatEvaluator();
    }

    public static MyKeyFrameSet OfFloat(float ...values){
        int frameCnt = values.length;
        MyFloatKeyframe frameList[] = new MyFloatKeyframe[frameCnt];
        frameList[0] = new MyFloatKeyframe(0,values[0]);
        for(int i = 1;i<frameCnt;i++){
            frameList[i] = new MyFloatKeyframe((float) i / (frameCnt -1),values[i]);
        }
        return new MyKeyFrameSet(frameList);
    }

    public Object getValue(float fraction){
        MyFloatKeyframe preFrame = mFirstFrame;
        for(int i =1;i<myFloatKeyframeList.size();i++){
            MyFloatKeyframe nextFrame = myFloatKeyframeList.get(i);
            if(fraction < nextFrame.getmFraction()){
                return mEvaluator.evaluate(fraction,preFrame.getmValue(),nextFrame.getmValue());
            }
            preFrame = nextFrame;
        }
        return null;
    }
}
