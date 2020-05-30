package cn.panyunyi.focks.ui.custom;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import org.xmlpull.v1.XmlPullParser;

import java.util.Timer;
import java.util.TimerTask;

import cn.panyunyi.focks.R;

public class CircleRotate extends View {
    private Paint textPaint, paint, btnPaint, shaderPaint, innerPaint,imgPaint;
    private Path mBtn;
    private Timer mTimer;
    private float agree = 1;
    private Shader mshader;
    public int rawRadius=300;
    public int centerH=400;
    public int centerW=400;
    private PathEffect mEffect;
    public int x = 500;
    public int y = 200;
    private int touchX, touchY;
    public double radius, totalRadius;
    float mPosX, mPosY, mCurPosX, mCurPosY;
    boolean leftMove, rightMove;
    boolean canForward = true;
    private boolean canBack = true;
    private XmlPullParser XmlPullParser;
    private Context mContext;
    public RadiusListener listener;
    public Drawable drawable;
    public CircleRotate(Context context) {
        super(context);
    }

    public CircleRotate(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
        //mEffect = new DashPathEffect(new float[]{1,2,5,10,50,20}, 0);    // float[]{ 虚线的厚度, 虚线的间距,虚线的厚度, 虚线的间距 ......}
        mEffect = new DashPathEffect(new float[]{5, 15}, 0);    // float[]{ 虚线的厚度, 虚线的间距,虚线的厚度, 虚线的间距 ......}
        mshader = new SweepGradient(500, 500, Color.parseColor("#F5B041"), Color.parseColor("#F5B041"));    //渐变遮罩样式


        textPaint = new Paint();
        textPaint.setStrokeWidth(16);
        textPaint.setColor(Color.WHITE);
        textPaint.setStrokeCap(Paint.Cap.ROUND);
        textPaint.setAntiAlias(true);

        paint = new Paint();
        paint.setAntiAlias(true);


        btnPaint = new Paint();
        btnPaint.setAntiAlias(true);
        shaderPaint = new Paint();
        shaderPaint.setAntiAlias(true);
        innerPaint = new Paint();
        innerPaint.setAntiAlias(true);
        imgPaint=new Paint();
        imgPaint.setAntiAlias(true);


//        mBtn = new Path();
//        mBtn.moveTo(500, 500);
//        mBtn.addCircle(500, 250, 40, Path.Direction.CCW);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        //绘画手表盘
        drawbeauty(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //设置宽高
        setMeasuredDimension(1000, 1000);  //画布大小
    }
    public void changeData(double radius,double x,double y){
        this.totalRadius=radius;
        this.x= (int) x;
        this.y= (int) y;
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void drawbeauty(Canvas canvas) {
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        //设置缓存层，因为下面要实用xfermode，使用xfemode必须使用缓存层，否则会出现黑色背景
        int layerId = canvas.saveLayer(0, 0, canvasWidth, canvasHeight, null, Canvas.ALL_SAVE_FLAG);

        //初始化画笔，因为上下两层做画需要的画笔属性不一样，所以只能每次重新设置一次
        paint.setStyle(Paint.Style.STROKE);     //设置画笔为不填充模式

        btnPaint.setStyle(Paint.Style.FILL);
        btnPaint.setColor(Color.GREEN);
        innerPaint.setStyle(Paint.Style.FILL);
        innerPaint.setColor(Color.parseColor("#F7DC6F"));
        //paint.setPathEffect(mEffect);           //设置笔画样式，这里设置的是虚线样式
        paint.setStrokeWidth(30);//设置笔画宽度
        paint.setColor(Color.parseColor("#F4D03F"));
        shaderPaint.setStrokeWidth(30);
        shaderPaint.setColor(Color.parseColor("#28B463"));
        shaderPaint.setStyle(Paint.Style.STROKE);

        // canvas.drawCircle(500, 500, 200, paint);//画一个纯色表盘，虚线，空心圆形 【表盘位置和大小】
        float x_ = (getWidth() - getHeight() / 2) / 2;
        float y_ = getHeight() / 4;

        RectF oval = new RectF(x_-50, y_-50,
                getWidth() - x_+50, getHeight() - y_+50);

        //canvas.drawArc(oval, 360, 360, false, paint);
        canvas.drawCircle(500, 500, rawRadius, paint);
        canvas.drawCircle(500, 500, rawRadius, innerPaint);

        canvas.drawArc(oval, -90, (float) totalRadius, false, shaderPaint);
        listener.onRadiusChangeListener(totalRadius);
        //canvas.drawArc(rectF,-90,180,true,shaderPaint);
        canvas.drawCircle(x, y, 40, btnPaint);
        if(drawable==null) {
            drawable = ContextCompat.getDrawable(mContext,R.drawable.ic_tree);
        }

        System.out.println(canvas.getClipBounds().left+"--"+canvas.getClipBounds().right);
        drawable.setBounds(200,rawRadius,canvas.getClipBounds().right-200,700);
        drawable.draw(canvas);
        //canvas.drawPath(mBtn, btnPaint);
        //设置画笔属性，SRC_IN属性，让第二个图案只能花在第一个图案上面，也就是只能画在上面所说那个纯色表盘里面
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //把画笔虚线属性去掉，因为我要的是一个实心圆形，然后让这个实心但是颜色不一样圆形画在上面所说表盘上面，因为设置了xfermode，所以显示的一样会是虚线圆形表盘，但是颜色会变成你现在的颜色
        paint.setPathEffect(null);

        //设置画笔shader属性，这里设置的是SweepGradient模式，可以让颜色过渡泾渭分明，以圆形为中心开始变化
        //paint.setShader(mshader);
        paint.setStyle(Paint.Style.FILL);

        canvas.save();      //保存画布

        //旋转画布，然后你就会发现时钟表盘开始动了
//        if(Static.cartoon){
//            if(Static.agree_flag){
//                agree = 1;
//                Static.agree_flag = false;
//            }
//            canvas.rotate(agree, 500, 500);    //画布旋转的中心点
//        }else{
//            agree = 1;
//        }

        //canvas.drawRect(10, 10, 1000, 1100, paint);   //渐变矩形绘制
        //canvas.drawPath(mTriangle, textPaint);    //绘制小三角形
        canvas.restore();

        //最后将画笔去除Xfermode
        paint.setXfermode(null);
        canvas.restoreToCount(layerId);
    }

    private void setmTimer() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                agree = agree + 0.022f;
                if (agree > 360)
                    agree = 1;
                postInvalidate();
            }
        }, 1000, 3);  //延时/周期
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mPosX = event.getX();
                mPosY = event.getY();
            case MotionEvent.ACTION_MOVE:
                mCurPosX = event.getX();
                mCurPosY = event.getY();
                float Y = mCurPosY - mPosY;
                float X = mCurPosX - mPosX;
                // 获取当前触摸点的x,y
                leftMove = X < 0;

                rightMove = X > 0;
//                if(mPosX<500&&mCurPosX>500&&mPosY<500&&mCurPosY<500){
//                    return true;
//                }
//                if(mPosX>500&&mCurPosX<500&&mPosY<500&&mCurPosY<500){
//                    return true;
//                }
            case MotionEvent.ACTION_UP:

                touchX = (int) event.getX();
                touchY = (int) event.getY();

                break;
        }
        //获取屏幕宽高
        //重新绘制圆 ,控制小球不会被移出屏幕
        if (Math.sqrt(Math.pow(x - touchX, 2) + Math.pow(y - touchY, 2)) < rawRadius) {
            int rawX = x - 500;
            int rawY = y - 500;
            int rawTouchX = touchX - 500;
            int rawTouchY = touchY - 500;
            Point point1 = new Point();
            Point point2 = new Point();
            if (rawTouchX == 0) {
                    point1.x = 0;
                    point1.y = 300;
                    point2.x = 0;
                    point2.y = -300;

            } else {
                double cX1 = Math.sqrt(Math.pow(rawRadius, 2) / (1 + Math.pow(rawTouchY, 2) / Math.pow(rawTouchX, 2)));
                double cX2 = -Math.sqrt(Math.pow(rawRadius, 2) / (1 + Math.pow(rawTouchY, 2) / Math.pow(rawTouchX, 2)));

                point1 = new Point(cX1, cX1 * rawTouchY / (rawTouchX));
                point2 = new Point(cX2, cX2 * rawTouchY / (rawTouchX));
            }
            Point rawPoint = new Point(rawX, rawY);
            Point realPoint = getDistance(point1, point2, rawPoint);
            double realX = realPoint.x;
            double realY = realPoint.y;
            double realR = 0;
            double fakeR = Math.atan(realY / realX) / 2 / Math.PI * 360;
            if (fakeR < 0) {
                realR = 90 + fakeR;
            } else {
                realR = fakeR;
            }
            double t = 0;
            if (fakeR < 0 && realX > 0) {
                t = realR;


            } else if (fakeR > 0 && realX > 0) {

                t = 90 + realR;
            } else if (fakeR < 0 && realX < 0) {
                t = 180 + realR;

            } else {
                t = 270 + realR;

            }
                if(rawX<0&&rawY<0&&realX>0&&realY<0){
                    return true;
                }
                else if(rawX>0&&rawY<0&&realX<0&&realY<0){
                    return true;
                }else {
                    totalRadius = t;
                    x = (int) realX + 500;
                    y = (int) realY + 500;
                    invalidate();
                }

        }
        // 自己处理触摸事件
        return true;
    }

    private Point getDistance(Point point1, Point point2, Point raw) {
        if (Math.sqrt(Math.pow(point1.x - raw.x, 2) + Math.pow(point1.y - raw.y, 2)) > Math.sqrt(Math.pow(point2.x - raw.x, 2) + Math.pow(point2.y - raw.y, 2))) {
            return point2;
        } else {
            return point1;
        }
    }

    class Point {
        public Point() {

        }

        public double x;
        public double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
    public interface RadiusListener{
        public void onRadiusChangeListener(double radius);
    }
    public void setRadiusChangeListener(RadiusListener listener) {
        this.listener = listener;
    }
}
