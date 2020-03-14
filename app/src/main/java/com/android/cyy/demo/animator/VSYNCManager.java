package com.android.cyy.demo.animator;

import java.util.ArrayList;
import java.util.List;

public class VSYNCManager {

    private static final VSYNCManager mInstance = new VSYNCManager();
    private List<AnimationFrameCallback>mCallbackList = new ArrayList<>();
    public static VSYNCManager getInstance() {
        return mInstance;
    }
    private VSYNCManager(){
        new Thread(runnable).start();
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (true){
                try {
                    Thread.sleep(16);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                for (AnimationFrameCallback item : mCallbackList) {
                    item.doAnimationFrame(System.currentTimeMillis());
                }
            }
        }
    };
    public void addListener(AnimationFrameCallback listener){
        mCallbackList.add(listener);
    }
    public void removeListener(AnimationFrameCallback listener){
        mCallbackList.remove(listener);
    }
    interface AnimationFrameCallback {
        boolean doAnimationFrame(long currentTime);
    }
}
