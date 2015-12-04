package com.zvin.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class RotateButton extends View {
    private int DEFAULT_SIZE;
    private Bitmap circleBitmap;

    private float mIndicatorLeft;
    private float mIndicatorTop;

    private int mInnerCircleRadius;

    private int mIndicatorRadius;

    private final int DEFAULT_START_DEGRESS = 140;
    private final int DEFAULT_SWEEP_DEGRESS = 260;

    private int mStartDegress;
    private int mSweepDegress;

    private int DEFAULT_SENSATIVE_OFFSET = 70;
    private int mSensativeOffset;

    private int mPointerInterval;
    private int DEFAULT_POINTER_INTERVAL = 10;

    private int mRadius;
    private String mUnit;
    private int mUnitSize;
    private int mTxtSize;
    private int mTxtColor;
    private int mRingColor;
    private int mRingWrapColor;
    private int mScaleColor;
    private int mBg;
    private int mRingThickness;
    private int mBasePadding;

    private int DEFAULT_VALUE_TEXT_SIZE;
    private int DEFAULT_UNIT_TEXT_SIZE;
    private final int DEFAULT_RORATEBT_BG = Color.argb(255, 24, 142, 190);
    private int DEFAULT_RING_COLOR = Color.rgb(147, 199, 223);
    private int DEFAULT_RING_WRAP_COLOR = Color.rgb(116, 193, 215);

    private int mAngle;
    private Paint mArcPaint, mLinePaint;
    private int mCircleCentreX, mCircleCentreY;
    private int mArcStrokeWidth;
    private RectF mRingRect, mOuterArcRect, mInnerArcRect;
    private boolean mLastActionOverRotate = false;

    private int POINTER_LINE_LEN;
    public RotateButton(Context context) {
        this(context, null);
    }

    public RotateButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotateButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr){

        mArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        float density = getResources().getDisplayMetrics().density;
        mIndicatorRadius = (int)(7 * density);

        DEFAULT_VALUE_TEXT_SIZE = (int)(50 * density);
        DEFAULT_UNIT_TEXT_SIZE = (int)(20 * density);
        DEFAULT_SIZE = (int)(200 * density);
        POINTER_LINE_LEN = (int)(7 * density);

        mArcStrokeWidth = (int)(1 * density);
        mStartDegress = DEFAULT_START_DEGRESS;
        mSweepDegress = DEFAULT_SWEEP_DEGRESS;
        mBasePadding = (int)(3 * density);
        mSensativeOffset = (int)(DEFAULT_SENSATIVE_OFFSET * density);
        mPointerInterval = DEFAULT_POINTER_INTERVAL;
        mAngle = 180 - mSweepDegress/2;
        mRingThickness = (int)(4 * density);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.RotateButton);

        mUnit = attributes.getString(R.styleable.RotateButton_unit);
        if(TextUtils.isEmpty(mUnit))
            throw new IllegalArgumentException("the unit of the rotate button should not be null!");
        mRadius = attributes.getDimensionPixelSize(R.styleable.RotateButton_radius, DEFAULT_SIZE);
        mTxtSize = attributes.getDimensionPixelSize(R.styleable.RotateButton_txt_size, DEFAULT_VALUE_TEXT_SIZE);
        mBg = attributes.getColor(R.styleable.RotateButton_bg, DEFAULT_RORATEBT_BG);
        mUnitSize = attributes.getDimensionPixelSize(R.styleable.RotateButton_unittxt_size, DEFAULT_UNIT_TEXT_SIZE);
        mRingColor = attributes.getColor(R.styleable.RotateButton_ring_color, DEFAULT_RING_COLOR);
        mRingWrapColor = attributes.getColor(R.styleable.RotateButton_ring_wrap_color, DEFAULT_RING_WRAP_COLOR);
        mTxtColor = attributes.getColor(R.styleable.RotateButton_txt_color, Color.WHITE);
        mScaleColor = attributes.getColor(R.styleable.RotateButton_scale_color, Color.WHITE);
        attributes.recycle();

        drawIndicator();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mRadius, mRadius);
        mCircleCentreX = getMeasuredWidth()/2;
        mCircleCentreY = getMeasuredHeight()/2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //draw the bottom background
        mArcPaint.setColor(mBg);
        mArcPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mCircleCentreX, mCircleCentreY, getMeasuredWidth() / 2, mArcPaint);

        //draw outer arc
        mArcPaint.setColor(mRingWrapColor);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(mArcStrokeWidth);
        int arcOffset = mArcStrokeWidth/2;
        if(mOuterArcRect == null){
            mOuterArcRect = new RectF(mBasePadding + arcOffset, mBasePadding + arcOffset, getMeasuredWidth() - mBasePadding - arcOffset, getMeasuredHeight() - mBasePadding - arcOffset);
        }
        canvas.drawArc(mOuterArcRect, mStartDegress, mSweepDegress, false, mArcPaint);

        //draw inside ring
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(mRingThickness);
        mArcPaint.setColor(mRingColor);
        int innerRingOffset = mRingThickness/2;
        if(mRingRect == null){
            mRingRect = new RectF(mOuterArcRect.left + arcOffset + 4 + innerRingOffset, mOuterArcRect.top + arcOffset + 4 + innerRingOffset, mOuterArcRect.right - arcOffset - 4 - innerRingOffset, mOuterArcRect.bottom - arcOffset - 4 - innerRingOffset);
        }
        canvas.drawArc(mRingRect, mStartDegress, mSweepDegress, false, mArcPaint);

        //draw inner arc
        mArcPaint.setStrokeWidth(mArcStrokeWidth);
        mArcPaint.setColor(mRingWrapColor);
        if(mInnerArcRect == null){
            mInnerArcRect = new RectF(mRingRect.left + innerRingOffset + 4 + arcOffset, mRingRect.top + innerRingOffset + 4 + arcOffset, mRingRect.right - innerRingOffset - 4 - arcOffset, mRingRect.bottom - innerRingOffset - 4 - arcOffset);
        }
        canvas.drawArc(mInnerArcRect, mStartDegress, mSweepDegress, false, mArcPaint);

        mInnerCircleRadius = (int)(mCircleCentreX - mInnerArcRect.left - arcOffset - 3);
        mLinePaint.setColor(mScaleColor);

        int totalPointCount = (DEFAULT_SWEEP_DEGRESS / mPointerInterval) * 4 + 4;
        float[] lines = new float[totalPointCount];

        for (int i = 0; i < totalPointCount; i += 4) {
            int lineIndex = i / 4;
            lines[i] = getLineXcoordinate(lineIndex, true);
            lines[i + 1] = getLineYcoordinate(lineIndex, true);
            lines[i + 2] = getLineXcoordinate(lineIndex, false);
            lines[i + 3] = getLineYcoordinate(lineIndex, false);
        }
        //draw pointer lines
        canvas.drawLines(lines, mLinePaint);

        setCirclePosition(mAngle);

        String str = convertAngleToValue(mAngle);
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(mTxtSize);
        textPaint.setColor(mTxtColor);

        Paint unitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        unitPaint.setTextSize(mUnitSize);
        unitPaint.setColor(mTxtColor);

        float valTxtWidth = textPaint.measureText(str);
        float valTxtHeight = textPaint.ascent() + textPaint.descent();

        float unitTxtWidth = unitPaint.measureText(mUnit);
        float unitTxtHeight = unitPaint.ascent() + unitPaint.descent();

        //draw text
        canvas.drawText(str, (getMeasuredWidth() - valTxtWidth - unitTxtWidth)/2, (getMeasuredHeight() - valTxtHeight)/2, textPaint);
        canvas.drawText(mUnit, (getMeasuredWidth() + valTxtWidth)/2, (getMeasuredHeight() + Math.abs(valTxtHeight))/2, unitPaint);

        //draw switcher bitmap
        canvas.drawBitmap(circleBitmap, mIndicatorLeft, mIndicatorTop, null);
    }

    private void drawIndicator(){

        circleBitmap = Bitmap.createBitmap(mIndicatorRadius * 2, mIndicatorRadius * 2, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(circleBitmap);

        Paint mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(Color.WHITE);
        mCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(mIndicatorRadius, mIndicatorRadius, mIndicatorRadius, mCirclePaint);

        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setColor(Color.rgb(61, 169, 255));
        canvas.drawCircle(mIndicatorRadius, mIndicatorRadius, mIndicatorRadius/2, mCirclePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:

                float eventX = event.getX();
                float eventY = event.getY();
                float x = Math.abs(eventX - mCircleCentreX);
                float y = Math.abs(eventY - mCircleCentreY);

                double distance = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
                if(Math.abs(distance - (mCircleCentreX - mRingRect.left)) > mSensativeOffset){
                    return true;
                }

                int angle = (int)(Math.toDegrees(Math.atan2(y, x)));

                if(eventX < mCircleCentreX && eventY > mCircleCentreY){
                    angle = 90 - angle;
                }

                if(eventX < mCircleCentreX && eventY < mCircleCentreY){
                    angle += 90;
                }

                if(eventX > mCircleCentreX && eventY < mCircleCentreY){
                    angle = 270 - angle;
                }

                if(eventX > mCircleCentreX && eventY > mCircleCentreY){
                    angle += 270;
                }

                Log.i(Debug.DEBUG_TAG, "action=" + event.getAction() + ", angle=" + angle);

                boolean isOverRotate = false;

                if(angle < (360 - mSweepDegress)/2){
                    angle = (360 - mSweepDegress)/2;
                    isOverRotate = true;
                    if(mLastActionOverRotate){
                        return true;
                    }
                }

                if(angle > 180 + mSweepDegress/2){
                    angle = 180 + mSweepDegress/2;
                    isOverRotate = true;
                    if(mLastActionOverRotate){
                        return true;
                    }
                }

                mLastActionOverRotate = isOverRotate;

                mAngle = angle;
                setCirclePosition(mAngle);
                return true;
        }

        return super.onTouchEvent(event);
    }

    private float getLineXcoordinate(int lineIndex, boolean isFrist){
        float result = 0;
        if (isFrist) {
            result = (float) (getMeasuredWidth() /2 - Math.sin(Math.toRadians(mStartDegress - 90 + lineIndex * 10)) * mInnerCircleRadius);
        } else {
            result = (float) (getMeasuredWidth() /2 - Math.sin(Math.toRadians(mStartDegress - 90 + lineIndex * 10)) * (mInnerCircleRadius - POINTER_LINE_LEN));
        }
        return result;
    }

    private float getLineYcoordinate(int lineIndex, boolean isFirst){
        float result = 0;
        if(isFirst){
            result = (float)(getMeasuredHeight()/2 + Math.cos(Math.toRadians(mStartDegress - 90 + lineIndex * 10)) * mInnerCircleRadius);
        }else{
            result = (float)(getMeasuredHeight()/2 + Math.cos(Math.toRadians(mStartDegress - 90 + lineIndex * 10)) * (mInnerCircleRadius - POINTER_LINE_LEN));
        }
        return result;
    }

    public void setCirclePosition(int angle){
        mIndicatorLeft = (float)(getMeasuredWidth()/2 - Math.sin(Math.toRadians(angle)) * (mCircleCentreX - mRingRect.left) - mIndicatorRadius);
        mIndicatorTop = (float)(getMeasuredHeight()/2 + Math.cos(Math.toRadians(angle)) * (mCircleCentreY - mRingRect.top) - mIndicatorRadius);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public String convertAngleToValue(int angle){
        int val = angle - (360 - mSweepDegress)/2;
        return String.valueOf(val);
    }
}