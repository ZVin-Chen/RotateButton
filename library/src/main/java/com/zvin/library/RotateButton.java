package com.zvin.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class RotateButton extends View {
    private int DEFAULT_SIZE = 200;
    private Bitmap circleBitmap;

    private float xCoordinate;
    private float yCoordinate;

    private int mOuterCircleRadius;
    private int mInnerCircleRadius;

    private static final int MOVE_ABLE_CIRCLE_RADIUS = 7;

    private static final int DEFAULT_START_DEGRESS = 140;
    private static final int DEFAULT_SWEEP_DEGRESS = 260;

    private int mStartDegress;
    private int mSweepDegress;

    private boolean isInit = true;

    private static int DEFAULT_SENSATIVE_OFFSET = 70;
    private int mSensativeOffset;

    private int mPointerInterval;
    private static final int DEFAULT_POINTER_INTERVAL = 10;

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

    private int DEFAULT_VALUE_TEXT_SIZE = 50;
    private int DEFAULT_UNIT_TEXT_SIZE = 20;
    private int DEFAULT_RORATEBT_BG = Color.argb(255, 24, 142, 190);
    private int DEFAULT_RING_COLOR = Color.rgb(147, 199, 223);
    private int DEFAULT_RING_WRAP_COLOR = Color.rgb(116, 193, 215);

    private int mAngle;

    private static int POINTER_LINE_LEN = 5;
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
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.RotateButton);
        mUnit = attributes.getString(R.styleable.RotateButton_unit);

        if(TextUtils.isEmpty(mUnit))
            throw new IllegalArgumentException("the unit of the rotate button should not be null!");

        float density = getResources().getDisplayMetrics().density;
        DEFAULT_VALUE_TEXT_SIZE *= density;
        DEFAULT_UNIT_TEXT_SIZE *= density;
        DEFAULT_SIZE *= density;
        POINTER_LINE_LEN *= density;

        mRadius = attributes.getDimensionPixelSize(R.styleable.RotateButton_radius, DEFAULT_SIZE);
        mBasePadding = 10;
        mSensativeOffset = DEFAULT_SENSATIVE_OFFSET;
        mPointerInterval = DEFAULT_POINTER_INTERVAL;
        mTxtSize = attributes.getDimensionPixelSize(R.styleable.RotateButton_txt_size, DEFAULT_VALUE_TEXT_SIZE);
        mBg = attributes.getColor(R.styleable.RotateButton_bg, DEFAULT_RORATEBT_BG);

        mUnitSize = attributes.getDimensionPixelSize(R.styleable.RotateButton_unittxt_size, DEFAULT_UNIT_TEXT_SIZE);
        mStartDegress = DEFAULT_START_DEGRESS;
        mSweepDegress = DEFAULT_SWEEP_DEGRESS;
        mRingColor = attributes.getColor(R.styleable.RotateButton_ring_color, DEFAULT_RING_COLOR);
        mRingWrapColor = attributes.getColor(R.styleable.RotateButton_ring_wrap_color, DEFAULT_RING_WRAP_COLOR);

        mTxtColor = attributes.getColor(R.styleable.RotateButton_txt_color, Color.WHITE);
        mScaleColor = attributes.getColor(R.styleable.RotateButton_scale_color, Color.WHITE);
        mRingThickness = mRadius / 40;
        drawIndicator();

        attributes.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mRadius, mRadius);
        mOuterCircleRadius = (getMeasuredWidth() - 20) / 2;
        mInnerCircleRadius = (getMeasuredWidth() - 60) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setColor(mRingWrapColor);
        arcPaint.setStyle(Paint.Style.STROKE);

        RectF outerRectF = new RectF(mBasePadding, mBasePadding, getMeasuredWidth() - mBasePadding, getMeasuredHeight() - mBasePadding);
        //draw outer arc
        canvas.drawArc(outerRectF, mStartDegress, mSweepDegress, false, arcPaint);

        arcPaint.setStyle(Paint.Style.FILL);
        arcPaint.setColor(mBg);
        RectF innerRectF = new RectF(outerRectF.left + mRingThickness, outerRectF.top + mRingThickness, outerRectF.right - mRingThickness, outerRectF.bottom - mRingThickness);
        int radius = getMeasuredWidth()/2 - mRingThickness - mBasePadding;
        //draw inner arc
        canvas.drawArc(innerRectF, mStartDegress, mSweepDegress, false, arcPaint);
        canvas.drawCircle(getMeasuredWidth()/2, getMeasuredHeight()/2, radius, arcPaint);

        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(mRingThickness * 3/5);
        arcPaint.setColor(mRingColor);
        RectF ringRectF = new RectF(10 + 10, 10 + 10, getMeasuredWidth() - 10 - 10, getMeasuredHeight() - 10 - 10);
        //draw inside ring
        canvas.drawArc(ringRectF, DEFAULT_START_DEGRESS, DEFAULT_SWEEP_DEGRESS, false, arcPaint);

        Paint pointerLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointerLinePaint.setColor(mScaleColor);

        int totalPointCount = (DEFAULT_SWEEP_DEGRESS / mPointerInterval) * 4 + 4;
        float[] lines = new float[totalPointCount];

        for (int i = 0; i < totalPointCount; i += 4) {
            int lineIndex = i / 4;
            lines[i] = getPointerLineXcoordinate(lineIndex, true);
            lines[i + 1] = getPointerLineYcoordinate(lineIndex, true);
            lines[i + 2] = getPointerLineXcoordinate(lineIndex, false);
            lines[i + 3] = getPointerLineYcoordinate(lineIndex, false);
        }
        //draw pointer lines
        canvas.drawLines(lines, pointerLinePaint);

        if(isInit){
            mAngle = (360 - mSweepDegress)/2;
            setCirclePosition(mAngle);
        }

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
        canvas.drawBitmap(circleBitmap, xCoordinate, yCoordinate, null);
    }

    private void drawIndicator(){
        int innerCircleRadius = (int)(getContext().getResources().getDisplayMetrics().density * MOVE_ABLE_CIRCLE_RADIUS/2);
        int outerCircleRadius = (int)(getContext().getResources().getDisplayMetrics().density * MOVE_ABLE_CIRCLE_RADIUS);

        circleBitmap = Bitmap.createBitmap(outerCircleRadius * 2, outerCircleRadius * 2, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(circleBitmap);

        Paint mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(Color.WHITE);
        mCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(outerCircleRadius, outerCircleRadius, outerCircleRadius, mCirclePaint);

        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setColor(Color.rgb(61, 169, 255));
        canvas.drawCircle(outerCircleRadius, outerCircleRadius, innerCircleRadius, mCirclePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                int width = getMeasuredWidth();
                int height = getMeasuredHeight();
                float eventX = event.getX();
                float eventY = event.getY();
                float x = Math.abs(eventX - width / 2);
                float y = Math.abs(eventY - height / 2);

                double distance = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
                if(Math.abs(distance - mOuterCircleRadius) > mSensativeOffset){
                    return true;
                }

                int angle = (int)(Math.toDegrees(Math.atan2(y, x)));

                if(eventX < width/2 && eventY > height/2){
                    angle = 90 - angle;
                }

                if(eventX < width/2 && eventY < height/2){
                    angle += 90;
                }

                if(eventX > width/2 && eventY < height/2){
                    angle = 270 - angle;
                }

                if(eventX > width/2 && eventY > height/2){
                    angle += 270;
                }

                if(angle < (360 - mSweepDegress)/2 || (360 - angle) < (360 - mSweepDegress)/2){
                    return true;
                }

                mAngle = angle;
                setCirclePosition(mAngle);
                return true;
        }

        return super.onTouchEvent(event);
    }

    private float getPointerLineXcoordinate(int lineIndex, boolean isFrist){
        float result = 0;
        if(isFrist){
            result = (float)(getMeasuredWidth()/2 - Math.sin(Math.toRadians(50 + lineIndex * 10)) * mInnerCircleRadius);
        }else{
            result = (float)(getMeasuredWidth()/2 - Math.sin(Math.toRadians(50 + lineIndex * 10)) * (mInnerCircleRadius - POINTER_LINE_LEN));
        }
        return result;
    }

    private float getPointerLineYcoordinate(int lineIndex, boolean isFirst){
        float result = 0;
        if(isFirst){
            result = (float)(getMeasuredHeight()/2 + Math.cos(Math.toRadians(50 + lineIndex * 10)) * mInnerCircleRadius);
        }else{
            result = (float)(getMeasuredHeight()/2 + Math.cos(Math.toRadians(50 + lineIndex * 10)) * (mInnerCircleRadius - POINTER_LINE_LEN));
        }
        return result;
    }

    public void setCirclePosition(int angle){
        int outerCircleRadius = (int)(getContext().getResources().getDisplayMetrics().density * MOVE_ABLE_CIRCLE_RADIUS);
        this.xCoordinate = (float)((getWidth()/2 - outerCircleRadius) - Math.sin(Math.toRadians(angle)) * (mInnerCircleRadius + 10));
        this.yCoordinate = (float)((getHeight()/2 - outerCircleRadius) + Math.cos(Math.toRadians(angle)) * (mInnerCircleRadius + 10));
        isInit = false;
        invalidate();
    }

    public void setCirclePosition(double angleRadians){
        int outerCircleRadius = (int)(getContext().getResources().getDisplayMetrics().density * MOVE_ABLE_CIRCLE_RADIUS);
        this.xCoordinate = (float)((getWidth()/2 - outerCircleRadius) - Math.sin(angleRadians) * (mInnerCircleRadius + 10));
        this.yCoordinate = (float)((getHeight()/2 - outerCircleRadius) + Math.cos(angleRadians) * (mInnerCircleRadius + 10));
        isInit = false;
        invalidate();
    }

    public String convertAngleToValue(int angle){
        int val = angle - (360 - mSweepDegress)/2;
        return String.valueOf(val);
    }

    public String convertAngleToValue(double angleRadians){
        int val = (int)(Math.toDegrees(angleRadians)/mPointerInterval - (360 - mSweepDegress)/2);
        return String.valueOf(val);
    }

}