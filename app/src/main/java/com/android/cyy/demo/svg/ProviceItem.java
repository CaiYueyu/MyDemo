package com.android.cyy.demo.svg;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

public class ProviceItem {
    private Path mPath;
    private int mDrawColor;
    public ProviceItem(){
    }

    public void setmPath(Path mPath) {
        this.mPath = mPath;
    }

    public void setmDrawColor(int mDrawColor) {
        this.mDrawColor = mDrawColor;
    }

    public void draw(Canvas canvas, Paint paint){
        paint.setStrokeWidth(2);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setShadowLayer(8,0,0,0xffffff);
        canvas.drawPath(mPath,paint);

//            绘制边界
        paint.clearShadowLayer();
        paint.setColor(mDrawColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(2);
        canvas.drawPath(mPath, paint);
    }
}
