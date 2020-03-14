package com.android.cyy.demo.animator;

import android.view.View;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MFloatPropertyValuesHolder {

    private MyKeyFrameSet mMykeyFrameSet;
    //    属性名
    String mPropertyName;
    //float
    Class mValueType;
    Method mSetter = null;
    public MFloatPropertyValuesHolder(String propertyName,float...values){
        this.mPropertyName = propertyName;
        mValueType = float.class;
        mMykeyFrameSet = MyKeyFrameSet.OfFloat(values);
    }

    public void setupSetter(WeakReference<View> target){
        char firstLetter = Character.toUpperCase(mPropertyName.charAt(0));
        String lastLatter = mPropertyName.substring(1);
        String methodName = "set" + firstLetter + lastLatter;
        try {
            mSetter = View.class.getMethod(methodName,mValueType);
        }catch (NoSuchMethodException e){
            e.printStackTrace();
        }
    }
    public void setAnimatorValue(View target,float fraction) {
        Object value = mMykeyFrameSet.getValue(fraction);
        try {
            mSetter.invoke(target, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }
}
