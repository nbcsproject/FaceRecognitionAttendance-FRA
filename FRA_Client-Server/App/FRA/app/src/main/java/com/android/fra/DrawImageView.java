package com.android.fra;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

public class DrawImageView extends android.support.v7.widget.AppCompatImageView {

    private int ScreenWidth;
    private int ScreenHeight;
    private int diameter;

    public DrawImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        this.ScreenHeight = dm.heightPixels;
        this.ScreenWidth = dm.widthPixels;
    }

    Paint paint = new Paint();

    {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint p = new Paint();
        p.setColor(getResources().getColor(R.color.grayPrimary));
        p.setStyle(Paint.Style.FILL);
        this.diameter = getDiameter();
        Paint paintLine = new Paint();
        paintLine.setColor(getResources().getColor(R.color.colorPrimary));
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeWidth(10f);
        paintLine.setAntiAlias(true);
        Paint rect = new Paint();
        rect.setColor(getResources().getColor(R.color.grayPrimary));
        rect.setStyle(Paint.Style.FILL);

        canvas.drawARGB(110, 255, 255, 255);
        canvas.drawCircle(ScreenWidth / 2, (int) (ScreenHeight * 0.06 + diameter / 2), diameter / 2, paint);
        canvas.drawCircle(ScreenWidth / 2, (int) (ScreenHeight * 0.06 + diameter / 2), diameter / 2, paintLine);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.DST_OVER);
        canvas.drawRect(0, (int) (ScreenHeight * 0.15 + diameter), ScreenWidth, ScreenHeight, rect);
    }

    private int getDiameter() {
        double diameter = ScreenHeight * 0.5;
        int Diameter;
        if ((int) diameter / 2 != 0) {
            Diameter = (int) diameter - 1;
        } else {
            Diameter = (int) diameter;
        }
        return Diameter;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

}
