package com.android.cyy.demo.svg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.graphics.PathParser;
import android.util.AttributeSet;
import android.view.View;

import com.android.cyy.demo.R;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MapView extends View {

    private int[] colorArray = new int[]{0xFF239BD7, 0xFF30A9E5, 0xFF80CBF1, 0xFFFFFFFF};
    private Paint mPaint;
    private Context mContext;
    private List<ProviceItem> mProviceList;

    public MapView(Context context) {
        super(context);
        init(context);
    }

    public MapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mProviceList = new ArrayList<>();
        loadThread.start();
    }

    private Thread loadThread = new Thread(){
        @Override
        public void run() {
            final InputStream inputStream = mContext.getResources().openRawResource(R.raw.china);
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = null;
            try {
                builder = builderFactory.newDocumentBuilder();
                Document doc = builder.parse(inputStream);
                Element root = doc.getDocumentElement();
                NodeList lists = root.getElementsByTagName("path");
                List<ProviceItem> list = new ArrayList<>();
                for(int i=0;i<lists.getLength();i++){
                    Element element = (Element) lists.item(i);
                    String parseData = element.getAttribute("android:pathData");
                    @SuppressLint("RestrictedApi")
                    Path path = PathParser.createPathFromPathData(parseData);
                    ProviceItem item = new ProviceItem();
                    item.setmPath(path);
                    item.setmDrawColor(colorArray[i%4]);
                    list.add(item);
                }
                mProviceList = list;

                post(new Runnable() {
                    @Override
                    public void run() {
                        requestLayout();
                        invalidate();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mProviceList != null){
            canvas.save();
            for(ProviceItem item : mProviceList){
                item.draw(canvas,mPaint);
            }
        }
    }
}
