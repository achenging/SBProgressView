package com.achenging.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;


/**
 * Created by achenging on 2017/11/20.
 */

public class SBProgressView extends View {
    private static final int BASE_RADIUS             = 270;
    private static final int BASE_ANIMATION_DURATION = 2000;
    private static final int OUTTER_LINE_NUMBER      = 3;
    public static final  int CIRCLE_COLOR            = Color.BLUE;


    private int mViewWidth;
    private int mViewHeight;

    private int mCenterX;
    private int mCenterY;

    private Path mPath;

    private Paint mPaint;

    private int mCircleRadius = 50;

    private int   mCircleColor;
    private int[] mLineColor;
    private int   mLineSize;
    private int   mLineCircleRadius;
    private int[] mLineCircleColor;

    private int mLineOffset = 30;

    private ValueAnimator[] mValueAnimator;

    private RectF[] mRectF;


    private int mOutterLineNumber = 3;

    private int[] mAnimRadius;

    private int[] mInitRadius;

    private int mBaseRadius;
    private int mDuration;

    private String mText;
    private float  mTextSize;
    private int    mTextColor;


    private Rect mTextRect = new Rect();


    public SBProgressView(Context context) {
        this(context, null);
    }

    public SBProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SBProgressView);
        mBaseRadius = a.getInteger(R.styleable.SBProgressView_outter_base_radius, BASE_RADIUS);
        mDuration = a.getInteger(R.styleable.SBProgressView_outer_line_rotation_duration, BASE_ANIMATION_DURATION);
        mOutterLineNumber = a.getInteger(R.styleable.SBProgressView_outter_line_number, OUTTER_LINE_NUMBER);
        mCircleColor = a.getColor(R.styleable.SBProgressView_circle_color, CIRCLE_COLOR);
        mCircleRadius = a.getDimensionPixelSize(R.styleable.SBProgressView_circle_radius, dp2px(30));
        mLineOffset = a.getDimensionPixelSize(R.styleable.SBProgressView_outter_offset_radius, dp2px(10));
        int lineColorResId = a.getResourceId(R.styleable.SBProgressView_outter_line_color, 0);
        int lineCircleColorResId = a.getResourceId(R.styleable.SBProgressView_outter_line_circle_color, 0);
        mLineSize = a.getDimensionPixelSize(R.styleable.SBProgressView_outter_line_size, dp2px(5));
        mLineCircleRadius = a.getDimensionPixelSize(R.styleable.SBProgressView_outter_line_circle_radius, dp2px(5));
        mText = a.getString(R.styleable.SBProgressView_circle_text);
        mTextSize = a.getDimensionPixelSize(R.styleable.SBProgressView_circle_text_size, sp2px(13));
        mTextColor = a.getColor(R.styleable.SBProgressView_circle_text_color, Color.WHITE);
        a.recycle();

        mLineColor = new int[mOutterLineNumber];
        try {
            TypedArray lineColorTypeArray = context.getResources().obtainTypedArray(lineColorResId);
            if (lineColorTypeArray.length() == 0 || mOutterLineNumber != lineColorTypeArray.length()) {
                for (int i = 0; i < mOutterLineNumber; i++) {
                    mLineColor[i] = mCircleColor;
                }
            } else {
                for (int i = 0; i < mLineColor.length; i++) {
                    mLineColor[i] = lineColorTypeArray.getColor(i, 0);
                }
            }
            lineColorTypeArray.recycle();
        } catch (Exception e) {
            for (int i = 0; i < mOutterLineNumber; i++) {
                mLineColor[i] = mCircleColor;
            }
        }


        mLineCircleColor = new int[mOutterLineNumber];
        try {
            TypedArray lineCircleColorTypeArray = context.getResources().obtainTypedArray(lineCircleColorResId);
            if (lineCircleColorTypeArray.length() == 0 || mOutterLineNumber != lineCircleColorTypeArray.length()) {
                for (int i = 0; i < mOutterLineNumber; i++) {
                    mLineCircleColor[i] = mCircleColor;
                }
            } else {
                for (int i = 0; i < mLineColor.length; i++) {
                    mLineCircleColor[i] = lineCircleColorTypeArray.getColor(i, 0);
                }
            }
            lineCircleColorTypeArray.recycle();
        } catch (Exception e) {
            for (int i = 0; i < mLineColor.length; i++) {
                mLineCircleColor[i] = mCircleColor;
            }
        }


        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(10);
        mPaint.setColor(Color.BLACK);

        mInitRadius = new int[mOutterLineNumber];


        mRectF = new RectF[mOutterLineNumber];
        for (int i = 0; i < mOutterLineNumber; i++) {
            int idle = i % 2;
            if (idle == 1) {
                mInitRadius[i] = mBaseRadius - 90 * i;
            } else {
                mInitRadius[i] = mBaseRadius + 90 * i;  //初始化圆外的线的起始角度
            }
            mRectF[i] = new RectF();
        }

        mAnimRadius = new int[mOutterLineNumber];
        mValueAnimator = new ValueAnimator[mOutterLineNumber];

        for (int i = 0; i < mOutterLineNumber; i++) {

            mValueAnimator[i] = new ValueAnimator();
            ValueAnimator valueAnimator = mValueAnimator[i];
            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);

            int idle = i % 2;
            if (idle == 1) {
                valueAnimator.setIntValues(0, -360);
            } else {
                valueAnimator.setIntValues(0, 360);
            }
            valueAnimator.setDuration(mDuration);
            valueAnimator.start();

            final int finalI = i;
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mAnimRadius[finalI] = (int) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCircle(canvas);
        drawText(canvas);
        drawLine(canvas);
    }

    private void drawCircle(Canvas canvas) {
        mPaint.setColor(mCircleColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mCenterX, mCenterY, mCircleRadius, mPaint);

        mTextRect.set(mCenterX - mCircleRadius, mCenterY - mCircleRadius,
                mCenterX + mCircleRadius, mCenterY + mCircleRadius);
    }


    private void drawLine(Canvas canvas) {
        for (int i = 0; i < mOutterLineNumber; i++) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(mCircleColor);
            int baseLeft = mCenterX - mCircleRadius - mLineOffset * (i + 1);
            int baseTop = mCenterY - mCircleRadius - mLineOffset * (i + 1);
            int baseRight = mCenterX + mCircleRadius + mLineOffset * (i + 1);
            int baseBottom = mCenterY + mCircleRadius + mLineOffset * (i + 1);

            int r = mInitRadius[i] + mAnimRadius[i];    //半径
            mRectF[i].set(baseLeft, baseTop, baseRight, baseBottom);

            mPaint.setColor(mLineCircleColor[i]);
            canvas.drawArc(mRectF[i],
                    r, 270, false, mPaint); //圆外边的线

            float smallCircleX = (float) (mCenterX + (mCircleRadius
                    + mLineOffset * (i + 1)) * Math.cos(Math.toRadians(r)));
            float smallCircleY = (float) (mCenterY + (mCircleRadius
                    + mLineOffset * (i + 1)) * Math.sin(Math.toRadians(r)));

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mLineCircleColor[i]);
            mPaint.setStrokeWidth(mLineSize);
            canvas.drawCircle(smallCircleX, smallCircleY, mLineCircleRadius, mPaint);//圆的切面圆形
        }
    }

    private void drawText(Canvas canvas) {
        if (TextUtils.isEmpty(mText)) return;
        Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
        int baseline = (mTextRect.bottom + mTextRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
        mPaint.setTextSize(mTextSize);
        mPaint.setColor(mTextColor);
        mPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(mText, mTextRect.centerX(), baseline, mPaint);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST) {
            int width = 2 * mCircleRadius + 2 * (mOutterLineNumber + 1) * mLineOffset;
            int height = width;
            setMeasuredDimension(MeasureSpec.makeMeasureSpec(width, widthMode),
                    MeasureSpec.makeMeasureSpec(height, heightMode));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w - getPaddingLeft() - getPaddingRight();
        mViewHeight = h - getPaddingTop() - getPaddingBottom();

        mCenterX = mViewWidth / 2;
        mCenterY = mViewHeight / 2;
    }

    public void setText(String text) {
        mText = text;
        postInvalidate();
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int sp2px(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

    public void startAnimation() {
        for (int i = 0; i < mValueAnimator.length; i++) {
            if (!mValueAnimator[i].isRunning()) {
                mValueAnimator[i].start();
            }
        }

    }

    public void pauseAnimation() {
        for (int i = 0; i < mValueAnimator.length; i++) {
            mValueAnimator[i].pause();
        }
    }

    public void resumeAnimation() {
        for (int i = 0; i < mValueAnimator.length; i++) {
            mValueAnimator[i].resume();
        }
    }

    public void stopAnimation() {
        for (int i = 0; i < mValueAnimator.length; i++) {
            mValueAnimator[i].cancel();
        }

    }
}
