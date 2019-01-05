package com.bachkhoa.recognize_leaf.custom_ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class CustomDrawView extends ImageView {

    public CustomDrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private Paint getNewPaintPen(int color){

        Paint mPaintPen =new Paint();
        //Gives the width to the stroke.
        mPaintPen.setStrokeWidth(2);
        //To give the smoothness to the outer side of the paint.
        mPaintPen.setAntiAlias(true);
        //The colors that are higher precision than the device are   down-sampled y this flag.
        mPaintPen.setDither(true);
        //It defines the style of how you want the paint to work, other is fill style which fills the area within the s
        mPaintPen.setStyle(Paint.Style.STROKE);
        //It defines how the end of the line should look.
        mPaintPen.setStrokeCap(Paint.Cap.ROUND);
        //Set the color to the line.
        mPaintPen.setColor(color);

        return mPaintPen;

    }

    @Override
    public boolean onTouchEvent( MotionEvent event) {
        float x=event.getX();
        float y=event.getY();

        if(event.getAction() == MotionEvent.ACTION_DOWN){

        }else if(event.getAction() == MotionEvent.ACTION_MOVE){

        }else if(event.getAction()== MotionEvent.ACTION_UP){

        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

}
