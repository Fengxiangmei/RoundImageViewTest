package com.example.jin.roundimageviewtest;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * 显示圆形图片的控件，带边框
 * Created by Jin on 2017/7/19.
 */

public class RoundImageView extends AppCompatImageView {

    /**
     * 上下文
     */
    private Context mContext;

    /**
     * 宽度
     */
    private int mWidth;

    /**
     * 高度
     */
    private int mHeight;

    /**
     * 边框宽度
     */
    private int mBorderWidth;

    /**
     * 边框颜色
     */
    private int mBorderColor;

    /**
     * 默认边框的宽度为 0，也就是没有边框
     */
    private final int mDefaultWidth = 0;

    /**
     * 默认边框的颜色为纯白
     */
    private final int mDefaultColor = 0xffffffff;

    public RoundImageView(Context context) {
        this(context, null);
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        setAttributes(attrs);
    }

    /**
     * 从配置文件中获取属性值
     * @param attrs 属性列表
     */
    private void setAttributes(AttributeSet attrs) {
        TypedArray ta = mContext.obtainStyledAttributes(attrs,
                R.styleable.RoundImageView);
        mBorderWidth = ta.getDimensionPixelSize(
                R.styleable.RoundImageView_border_width, mDefaultWidth);
        mBorderColor = ta.getColor(
                R.styleable.RoundImageView_border_color, mDefaultColor);
        ta.recycle();//回收
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //获取控件的宽与高
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        if (mWidth == 0) {
            mWidth = getWidth();
        }
        if (mHeight == 0) {
            mHeight = getHeight();
        }

        //根据是否有边框，进行处理
        int radius;
        boolean b = mWidth < mHeight;
        if (mBorderWidth == 0) {//没有边框时，半径为宽高较小值的一半
            radius = b ? mWidth / 2 : mHeight / 2;
        } else {//有边框时，半径为宽高较小值的一半，减去边框宽度
            radius = b ? mWidth / 2 - mBorderWidth : mHeight / 2 - mBorderWidth;
            //然后先绘制边框
            drawBorder(canvas, radius, mBorderColor);
        }

        //获取资源图片，裁剪成圆形
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }
        Bitmap resource = ((BitmapDrawable) drawable).getBitmap();
        if (resource == null) {
            return;
        }
        Bitmap result = getRoundBitmap(
                resource.copy(Bitmap.Config.ARGB_8888, true), radius);

        //绘制到view的正中央
        canvas.drawBitmap(result, mWidth / 2 - radius, mHeight / 2 - radius, null);
    }

    /**
     * 获取裁剪后的圆形图片
     * @param bmp 原 bitmap
     * @param radius 半径
     * @return bitmap
     */
    public Bitmap getRoundBitmap(Bitmap bmp, int radius) {
        Bitmap squareBmp, scaledBmp;//正方形图，适配后的正方形图
        int x, y, squareSize;//横坐标，纵坐标，正方形边长

        //将原图片裁剪成正方形，边长为长与宽的较小值
        int bmpWidth = bmp.getWidth();
        int bmpHeight = bmp.getHeight();
        if (bmpWidth > bmpHeight) {
            squareSize = bmpHeight;
            x = (bmpWidth - bmpHeight) / 2;
            y = 0;
            squareBmp = Bitmap.createBitmap(bmp, x, y, squareSize, squareSize);
        } else if (bmpHeight > bmpWidth) {
            squareSize = bmpWidth;
            x = 0;
            y = (bmpHeight - bmpWidth) / 2;
            squareBmp = Bitmap.createBitmap(bmp, x, y, squareSize, squareSize);
        } else {
            squareBmp = bmp;
        }

        //将正方形图片与view的半径适配
        int diameter = radius * 2;
        if (squareBmp.getWidth() != diameter || squareBmp.getHeight() != diameter) {
            scaledBmp = Bitmap.createScaledBitmap(
                    squareBmp, diameter, diameter, true);
        } else {
            scaledBmp = squareBmp;
        }

        //绘制圆环
        Bitmap result = Bitmap.createBitmap(
                scaledBmp.getWidth(), scaledBmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawCircle(scaledBmp.getWidth() / 2, scaledBmp.getHeight() / 2,
                radius, paint);

        //绘制混合图像，这样最终显示的就是图像与圆环相交的部分，即圆形图片
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        Rect rect = new Rect(0, 0, scaledBmp.getWidth(), scaledBmp.getHeight());
        canvas.drawBitmap(scaledBmp, rect, rect, paint);

        //回收
        bmp.recycle();
        squareBmp.recycle();
        scaledBmp.recycle();

        return result;
    }

    /**
     * 绘制边框
     * @param canvas 画布
     * @param radius 半径
     * @param color 颜色
     */
    private void drawBorder(Canvas canvas, int radius, int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(mBorderWidth);

        canvas.drawCircle(mWidth / 2, mHeight / 2, radius, paint);
    }

}
