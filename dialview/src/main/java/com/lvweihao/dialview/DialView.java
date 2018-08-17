package com.lvweihao.dialview;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

/**
 * 自定义刻度盘
 * <p>
 * Created by lv.weihao on 2018/7/5.
 */

public class DialView extends View {
    private final LinearGradientUtil linearGradientUtil;
    private Context context;
    private int width;
    private int height;
    private Paint mBackgroundPaint;
    private Paint mPaint;
    private Paint mDottedLinePaint;
    private Paint mTextPaint;
    private TextPaint mCenterTextPaint;
    private Paint mCirclePaint;
    private int start, end;

    private int mExternalDottedLineRadius;
    private int mInsideDottedLineRadius;
    int actionBarHeight = 0;

    private int mEvent;
    private float eventX, eventY;

    private boolean isFirst = true;
    private boolean isLongBg = true;
    private boolean isLong = true;
    private int centerHig;
    private int strwidth, strheight;

    private OnDialViewTouch onDialViewTouch;

    //自定义属性
    private int startColor = Color.parseColor("#ffcc00"); //刻度渐变色开始值
    private int endColor = Color.parseColor("#ff3300"); //刻度渐变色结束值
    private int textColor = Color.parseColor("#cccccc"); //刻度文字颜色
    private int centerTextColor = Color.parseColor("#cccccc"); //中间当前值文字颜色
    private int backGroundColor = Color.TRANSPARENT; //背景色
    private int lineBackGroundColor = Color.parseColor("#cccccc"); //刻度背景色
    private int dottedLineBackGroundColor = Color.parseColor("#cccccc"); //虚线背景色
    private int circleColor = Color.parseColor("#E6E8FA"); //圆球背景色
    private boolean isBalance = false; //刻度是否等长
    private boolean hasActionBar = true; //是否有actionBar
    private int textSize = 20; // 刻度文字大小
    private int centerTextSize = 30; //中心文字大小
    private int angle = 45; //当前角度
    private int blankAngle = 90; //下方影藏刻度线的弧度
    private int eachAngle = 3; //每隔几度画一条刻度
    private int strokeWidth = 5; //刻度线宽
    private int lineLong = 70; //刻度线长
    private String startStr = "0"; //刻度初始值文字
    private String endStr = "100"; //刻度结束值文字
    private String midStr = "50"; //刻度中间值文字
    private String unitStr = "%"; //单位文字


    public DialView(Context context) {
        this(context, null);
    }

    public DialView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            if (hasActionBar) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
            }
        }

        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.DialView);
        startColor = array.getColor(R.styleable.DialView_startColor, Color.parseColor("#ffcc00"));
        endColor = array.getColor(R.styleable.DialView_endColor, Color.parseColor("#ff3300"));
        textColor = array.getColor(R.styleable.DialView_textColor, Color.parseColor("#cccccc"));
        centerTextColor = array.getColor(R.styleable.DialView_centerTextColor, Color.parseColor("#cccccc"));
        backGroundColor = array.getColor(R.styleable.DialView_backGroundColor, Color.TRANSPARENT);
        lineBackGroundColor = array.getColor(R.styleable.DialView_lineBackGroundColor, Color.parseColor("#cccccc"));
        dottedLineBackGroundColor = array.getColor(R.styleable.DialView_dottedLineBackGroundColor, Color.parseColor("#cccccc"));
        circleColor = array.getColor(R.styleable.DialView_circleColor, Color.parseColor("#E6E8FA"));
        hasActionBar = array.getBoolean(R.styleable.DialView_hasActionBar, true);
        isBalance = array.getBoolean(R.styleable.DialView_isBalance, true);
        textSize = array.getInt(R.styleable.DialView_textSize, 20);
        centerTextSize = array.getInt(R.styleable.DialView_centerTextSize, 30);
        blankAngle = array.getInt(R.styleable.DialView_blankAngle, 90);
        angle = array.getInt(R.styleable.DialView_initAngle, blankAngle / 2);
        eachAngle = array.getInt(R.styleable.DialView_eachAngle, 3);
        strokeWidth = array.getInt(R.styleable.DialView_strokeWidth, 5);
        lineLong = array.getInt(R.styleable.DialView_lineLong, 70);
        startStr = array.getString(R.styleable.DialView_startStr);
        endStr = array.getString(R.styleable.DialView_endStr);
        midStr = array.getString(R.styleable.DialView_midStr);
        unitStr = array.getString(R.styleable.DialView_unitStr);

        if (startStr == null) {
            startStr = "";
        }
        if (endStr == null) {
            endStr = "";
        }
        if (midStr == null) {
            midStr = "";
        }
        if (unitStr == null) {
            unitStr = "";
        }

        initPaint();
        linearGradientUtil = new LinearGradientUtil(startColor, endColor);

        Rect rect = new Rect();
        //返回包围整个字符串的最小的一个Rect区域
        mTextPaint.getTextBounds(midStr + unitStr, 0, (midStr + unitStr).length(), rect);
        strwidth = rect.width();
        strheight = rect.height();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        Log.e("lwh", "height:" + height);
        end = width / 2 - 100 - getPaddingLeft(); //适配padding
        start = end - lineLong;

        // 内部虚线的外部半径
        mExternalDottedLineRadius = start - 45;
        // 内部虚线的内部半径
        mInsideDottedLineRadius = mExternalDottedLineRadius - 5;

        centerHig = height / 2 + 50;
        if (blankAngle >= 180) {
            centerHig = height - strheight;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measureHeight = measureWidth - 100;
        if (blankAngle >= 180) {
            measureHeight = measureWidth / 2 + strheight * 2;
        }
        Log.e("lwh", "measureHeight:" + measureHeight);
        setMeasuredDimension(measureWidth, measureHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(backGroundColor);
        canvas.translate(width / 2, centerHig);
        Log.e("lwh", "centerHig:" + centerHig);
        float[] pts = {eventX - getLeft(), eventY - getTop() - actionBarHeight}; //减去margin的值
        if ((mEvent == MotionEvent.ACTION_DOWN || mEvent == MotionEvent.ACTION_MOVE) && !isFirst) {
            changeCanvasXY(canvas, pts);//触摸点的坐标转换
        }

        drawDottedLineArc(canvas); //画虚线

        if (blankAngle >= 180) { //画文字
            drawText(canvas);
        } else {
            drawTextAll(canvas);
        }
        if (isBalance) {
            drawBackCircleBalance(canvas); //画背景刻度
            drawCircleBalance(canvas); //画渐变刻度
        } else {
            drawBackCircleNotBalance(canvas);
            drawCircleNotBalance(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mEvent = event.getAction();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                //此处使用 getRawX，而不是 getX
                eventX = event.getRawX();
                eventY = event.getRawY();
                isFirst = false;
                if (onDialViewTouch != null) {
                    onDialViewTouch.onTouched(getCurrentPrecent());
                }
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    private void changeCanvasXY(Canvas canvas, float[] pts) {
        // 获得当前矩阵的逆矩阵
        Matrix invertMatrix = new Matrix();
        canvas.getMatrix().invert(invertMatrix);
        // 使用 mapPoints 将触摸位置转换为画布坐标
        invertMatrix.mapPoints(pts);
        float x = Math.abs(pts[0]);
        float y = Math.abs(pts[1]);
        double z = Math.sqrt(x * x + y * y);
        float round = (float) (Math.asin(y / z) / Math.PI * 180);
        Log.e("lwh", "触摸的点：X===" + pts[0] + "Y===" + pts[1] + "===当前角度：" + round);
        if (pts[0] >= 0 && pts[1] <= 0) { //第一象限
            angle = getAngle((int) (180 + 90 - round));
        } else if (pts[0] >= 0 && pts[1] >= 0) { //第二象限
            angle = getAngle((int) (270 + round)) >= (360 - blankAngle / 2) ? (360 - blankAngle / 2) : getAngle((int) (270 + round));
        } else if (pts[0] <= 0 && pts[1] >= 0) { //第三象限
            angle = getAngle((int) (90 - round)) <= blankAngle / 2 ? blankAngle / 2 : getAngle((int) (90 - round));
        } else if (pts[0] <= 0 && pts[1] <= 0) { //第四象限
            angle = getAngle((int) (90 + round));
        }
        Log.e("lw", "angle:" + angle);
    }

    private int getAngle(int value) {// 获取当前角度四舍五入
        int result = value;
        if (value % eachAngle >= eachAngle / 2) {
            int base = value / eachAngle;
            result = (base + 1) * eachAngle;
        } else if (value % eachAngle < eachAngle / 2 && value % eachAngle > 0) {
            int base = value / eachAngle;
            result = base * eachAngle;
        }
        return result;
    }

    private void drawBackCircleBalance(Canvas canvas) {
        canvas.save();
        for (int i = 0; i <= 360; i += eachAngle) {
            if (i >= blankAngle / 2 && i <= (360 - blankAngle / 2)) {
                canvas.drawLine(0, start, 0, end, mBackgroundPaint);
                canvas.rotate(eachAngle);
            } else {
                canvas.rotate(eachAngle);
            }
        }
    }

    private void drawCircleBalance(Canvas canvas) {
        canvas.restore();
        for (int i = 0; i <= angle; i += eachAngle) {
            if (i >= blankAngle / 2 && i <= (360 - blankAngle / 2)) {
                setPaintColor(i);
                if (i == angle) {
                    canvas.drawLine(0, start, 0, end + 30, mPaint);
                    canvas.drawCircle(0, start - dip2px(5), dip2px(5), mCirclePaint);
                } else {
                    canvas.drawLine(0, start, 0, end, mPaint);
                }
                canvas.rotate(eachAngle);
            } else {
                canvas.rotate(eachAngle);
            }
        }
    }

    private void drawBackCircleNotBalance(Canvas canvas) {
        canvas.save();
        isLongBg = true;
        for (int i = 0; i <= 360; i += eachAngle) {
            if (i >= blankAngle / 2 && i <= (360 - blankAngle / 2)) {
                if (isLongBg) {
                    canvas.drawLine(0, start, 0, end + 30, mBackgroundPaint);
                    isLongBg = false;
                } else {
                    canvas.drawLine(0, start, 0, end, mBackgroundPaint);
                    isLongBg = true;
                }
                canvas.rotate(eachAngle);
            } else {
                canvas.rotate(eachAngle);
            }
        }
    }

    private void drawCircleNotBalance(Canvas canvas) {
        canvas.restore();
        isLong = true;
        canvas.rotate(blankAngle / 2);
        for (int i = blankAngle / 2; i <= angle; i += eachAngle) {
            if (i >= blankAngle / 2 && i <= (360 - blankAngle / 2)) {
                setPaintColor(i);
                if (isLong) {
                    canvas.drawLine(0, start, 0, end + 30, mPaint);
                    isLong = false;
                } else {
                    canvas.drawLine(0, start, 0, end, mPaint);
                    isLong = true;
                }
                if (i == angle) {
                    canvas.drawCircle(0, start - dip2px(5), dip2px(5), mCirclePaint);
                }
                canvas.rotate(eachAngle);
            } else {
                canvas.rotate(eachAngle);
            }
        }
    }

    private void setPaintColor(int i) { //渐变色
        int startAng = blankAngle / 2;
        int aveAng = (360 - blankAngle) / 5;
        if (i >= startAng && i <= startAng + aveAng) {
            mPaint.setColor(linearGradientUtil.getColor(0.2f));
        } else if (i >= startAng + aveAng && i <= startAng + aveAng * 2) {
            mPaint.setColor(linearGradientUtil.getColor(0.4f));
        } else if (i >= startAng + aveAng * 2 && i <= startAng + aveAng * 3) {
            mPaint.setColor(linearGradientUtil.getColor(0.6f));
        } else if (i >= startAng + aveAng * 3 && i <= startAng + aveAng * 4) {
            mPaint.setColor(linearGradientUtil.getColor(0.8f));
        } else if (i >= startAng * 4 + aveAng && i <= startAng + aveAng * 5) {
            mPaint.setColor(linearGradientUtil.getColor(1f));
        }
    }

    private void drawDottedLineArc(Canvas canvas) { //画虚线
        int mDottedLineCount = 100; //线条数
        // 360 * Math.PI / 180
        float evenryDegrees = (float) (2.0f * Math.PI / mDottedLineCount);

        float startDegress = (float) (((360 - blankAngle) / 2) * Math.PI / 180);
        float endDegress = (float) ((360 - ((360 - blankAngle) / 2)) * Math.PI / 180);

        for (int i = 0; i < mDottedLineCount; i++) {
            float degrees = i * evenryDegrees;
            // 过滤底部90度的弧长
            if (degrees > startDegress && degrees < endDegress) {
                continue;
            }

            float startX = +(float) Math.sin(degrees) * mInsideDottedLineRadius;
            float startY = -(float) Math.cos(degrees) * mInsideDottedLineRadius;

            float stopX = +(float) Math.sin(degrees) * mExternalDottedLineRadius;
            float stopY = -(float) Math.cos(degrees) * mExternalDottedLineRadius;


            canvas.drawLine(startX, startY, stopX, stopY, mDottedLinePaint);
        }
    }

    private void drawTextAll(Canvas canvas) {
        Rect rect = new Rect();
        //返回包围整个字符串的最小的一个Rect区域
        mTextPaint.getTextBounds(midStr + unitStr, 0, (midStr + unitStr).length(), rect);
        int strwid = rect.width();
        int strhei = rect.height();
        canvas.drawText(startStr + unitStr, -end + strwid, end - strhei, mTextPaint);
        canvas.drawText(midStr + unitStr, -strwid / 2, -end - 40, mTextPaint);
        canvas.drawText(endStr + unitStr, end - strwid * 2, end - strhei, mTextPaint);

        String str = getCurrentPrecent() + unitStr;
        mCenterTextPaint.getTextBounds(str, 0, str.length(), rect);
        canvas.drawText(str, -rect.width() / 2, rect.height() / 2, mCenterTextPaint);
    }

    private void drawText(Canvas canvas) {
        Rect rect = new Rect();
        //返回包围整个字符串的最小的一个Rect区域
        mTextPaint.getTextBounds(midStr + unitStr, 0, (midStr + unitStr).length(), rect);
        int strwid = rect.width();
        int strhei = rect.height();
        canvas.drawText(startStr + unitStr, -end + strwid, strhei, mTextPaint);
        canvas.drawText(midStr + unitStr, -strwid / 2, -centerHig + strhei + 50, mTextPaint);
        canvas.drawText(endStr + unitStr, end - strwid * 2, strhei, mTextPaint);

        String str = getCurrentPrecent() + unitStr;
        mCenterTextPaint.getTextBounds(str, 0, str.length(), rect);
        canvas.drawText(str, -rect.width() / 2, -centerHig / 2 + rect.height() * 3, mCenterTextPaint);
    }

    private void initPaint() {
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setStrokeWidth(strokeWidth);
        mBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        mBackgroundPaint.setColor(lineBackGroundColor);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(0xFFFFA350);

        // 内测虚线的画笔
        mDottedLinePaint = new Paint();
        mDottedLinePaint.setAntiAlias(true);
        mDottedLinePaint.setStrokeWidth(5);
        mDottedLinePaint.setColor(dottedLineBackGroundColor);

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(sp2px(textSize));

        mCenterTextPaint = new TextPaint();
        mCenterTextPaint.setAntiAlias(true);
        mCenterTextPaint.setColor(centerTextColor);
        mCenterTextPaint.setTextSize(sp2px(centerTextSize));

        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setColor(circleColor);
    }

    public void setOnDialViewTouchListener(OnDialViewTouch onDialViewTouchListener) {
        this.onDialViewTouch = onDialViewTouchListener;
    }

    /**
     * 获取当前百分比（0-100）
     *
     * @return
     */
    public int getCurrentPrecent() {
        return 100 * (angle - blankAngle / 2) / (360 - blankAngle);
    }

    public int getStartColor() {
        return startColor;
    }

    public void setStartColor(int startColor) {
        this.startColor = startColor;
    }

    public int getEndColor() {
        return endColor;
    }

    public void setEndColor(int endColor) {
        this.endColor = endColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getCenterTextColor() {
        return centerTextColor;
    }

    public void setCenterTextColor(int centerTextColor) {
        this.centerTextColor = centerTextColor;
    }

    public int getBackGroundColor() {
        return backGroundColor;
    }

    public void setBackGroundColor(int backGroundColor) {
        this.backGroundColor = backGroundColor;
    }

    public int getLineBackGroundColor() {
        return lineBackGroundColor;
    }

    public void setLineBackGroundColor(int lineBackGroundColor) {
        this.lineBackGroundColor = lineBackGroundColor;
    }

    public int getDottedLineBackGroundColor() {
        return dottedLineBackGroundColor;
    }

    public void setDottedLineBackGroundColor(int dottedLineBackGroundColor) {
        this.dottedLineBackGroundColor = dottedLineBackGroundColor;
    }

    public int getCircleColor() {
        return circleColor;
    }

    public void setCircleColor(int circleColor) {
        this.circleColor = circleColor;
    }

    public boolean isBalance() {
        return isBalance;
    }

    public void setBalance(boolean balance) {
        isBalance = balance;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public int getCenterTextSize() {
        return centerTextSize;
    }

    public void setCenterTextSize(int centerTextSize) {
        this.centerTextSize = centerTextSize;
    }

    public int getAngle() {
        return angle;
    }

    /**
     * 设置当前百分比（0-100）
     *
     * @param percent
     */
    public void setAngle(int percent) {
        int ang = (int) (blankAngle / 2 + (percent / 100f) * (360 - blankAngle));
        this.angle = getAngle(ang);
        this.postInvalidate();
    }

    public int getBlankAngle() {
        return blankAngle;
    }

    public void setBlankAngle(int blankAngle) {
        this.blankAngle = blankAngle;
    }

    public int getEachAngle() {
        return eachAngle;
    }

    public void setEachAngle(int eachAngle) {
        this.eachAngle = eachAngle;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public int getLineLong() {
        return lineLong;
    }

    public void setLineLong(int lineLong) {
        this.lineLong = lineLong;
    }

    public String getStartStr() {
        return startStr;
    }

    public void setStartStr(String startStr) {
        this.startStr = startStr;
    }

    public String getEndStr() {
        return endStr;
    }

    public void setEndStr(String endStr) {
        this.endStr = endStr;
    }

    public String getMidStr() {
        return midStr;
    }

    public void setMidStr(String midStr) {
        this.midStr = midStr;
    }

    public String getUnitStr() {
        return unitStr;
    }

    public void setUnitStr(String unitStr) {
        this.unitStr = unitStr;
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public int sp2px(float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue （DisplayMetrics类中属性density）
     * @return
     */
    public int dip2px(float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
