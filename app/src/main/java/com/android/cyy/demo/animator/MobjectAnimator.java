package com.android.cyy.demo.animator;

import android.view.View;

import java.lang.ref.WeakReference;

public class MobjectAnimator implements VSYNCManager.AnimationFrameCallback{

    private long mDuration = 3000;
    private long mStartTime = 0;
    private WeakReference<View> target;
    private MFloatPropertyValuesHolder mHolder;
    private TimeInterpolator mInterPolator;


    public MobjectAnimator(View view,String propertyName,float ...values){
        target = new WeakReference<>(view);
        mHolder = new MFloatPropertyValuesHolder(propertyName,values);
    }

    public static  MobjectAnimator OfFloat(View view,String propertyName,float ...values){
        return new MobjectAnimator(view,propertyName,values);
    }

    public void setmInterPolator(TimeInterpolator interPolator){
        this.mInterPolator = interPolator;
    }
    public void setDuration(int duration) {
        this.mDuration = duration;
    }
    public void start(){
        VSYNCManager.getInstance().addListener(this);
        mHolder.setupSetter(target);
        mStartTime = System.currentTimeMillis();
    }
    int index = 0;
    @Override
    public boolean doAnimationFrame(long currentTime) {
        float total = mDuration / 16;
        index ++;
        float fraction = index / total;
        if(index > total){
            index = 0;
        }
        if(mInterPolator != null){
            fraction = mInterPolator.getInterpolation(fraction);
        }
        mHolder.setAnimatorValue(target.get(),fraction);
        return false;
    }
}
