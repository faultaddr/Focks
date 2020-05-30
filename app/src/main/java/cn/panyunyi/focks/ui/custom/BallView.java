package cn.panyunyi.focks.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class BallView extends View {
    //圆的初始位置
    private int x = 500;
    private int y = 500;
    private int touchX,touchY;
    Context context;
    Paint paint = new Paint();
    /**
     * 有style资源文件时调用
     * @param context
     * @param attrs
     * @param defStyle
     */
    public BallView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }
    /**
     * xml创建时调用
     * @param context
     * @param attrs
     */
    public BallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }
    /**
     * java代码创建时调用
     * @param context
     */
    public BallView(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 画笔

        paint.setColor(Color.RED);
        paint.setAntiAlias(true);

        //绘制圆
        //cx :圆心的x坐标
        //cy :圆心的y坐标
        //radius ：圆的半径
        //paint ：画笔
        canvas.drawCircle(x, y, 40, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

            case MotionEvent.ACTION_MOVE:

            case MotionEvent.ACTION_UP:
                // 获取当前触摸点的x,y坐标

                touchX = (int) event.getX();
                touchY = (int) event.getY();

                break;
        }
        //获取屏幕宽高
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        assert manager != null;
        int width = manager.getDefaultDisplay().getWidth();
        int height = manager.getDefaultDisplay().getHeight();

        //重新绘制圆 ,控制小球不会被移出屏幕
        Log.e(">>>",Math.sqrt(Math.pow(x-touchX,2)+Math.pow(y-touchY,2))+"");
        if(Math.sqrt(Math.pow(x-touchX,2)+Math.pow(y-touchY,2))<10){
            x=touchX;
            y=touchY;
            invalidate();
        }
        invalidate();
        // 自己处理触摸事件
        return true;
    }


}
