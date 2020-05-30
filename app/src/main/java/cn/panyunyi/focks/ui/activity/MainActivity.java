package cn.panyunyi.focks.ui.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import javax.security.auth.login.LoginException;

import cn.panyunyi.focks.R;
import cn.panyunyi.focks.ui.custom.CircleRotate;
import cn.panyunyi.focks.utils.ScreenObserver;
import cn.panyunyi.focks.utils.StatusBarUtils;

public class MainActivity extends AppCompatActivity implements CircleRotate.RadiusListener {
    TextView textView;
    CircleRotate clockView;
    Button mFuncButton;
    TextView hintTextView;
    double radius;
    int displayTime;
    CountDownTimer timer;
    ScreenObserver observer;
    View.OnTouchListener listener=new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    };
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    displayTime = (int) (((double) msg.getData().get("radius") / 360 * 120) + 10) / 10 * 10;

                    textView.setText((int) (displayTime / 10) * 10 + ":00");
                    radius = ((double) msg.getData().get("radius"));
                    break;
                case 1:
                    if ((msg.arg2 + "").length() == 1) {
                        textView.setText(msg.arg1 + ":0" + msg.arg2);
                    } else {
                        textView.setText(msg.arg1 + ":" + msg.arg2);
                    }
                    mFuncButton.setText("取消");
                    clockView.setOnTouchListener(listener);

                    clockView.totalRadius = (float) (msg.arg1 * 60 + msg.arg2) / (120 * 60) * 360;
                    //TODO: x,y 位置
                    Log.e(">>>", (clockView.totalRadius / 360) + "-----" + Math.sin((float) (clockView.totalRadius / 360) * 2 * Math.PI));
                    clockView.x = (int) (Math.cos((float) ((clockView.totalRadius - 90) / 360.0) * 2 * Math.PI) * clockView.rawRadius) + 500;
                    clockView.y = (int) (Math.sin((float) ((clockView.totalRadius - 90) / 360.0) * 2 * Math.PI) * clockView.rawRadius) + 500;
                    //Log.e(">>>",clockView.x+"----"+clockView.y);
                    clockView.invalidate();
                    break;
                case 2:
                    clockView.setOnTouchListener(null);
                    timer.cancel();
                    textView.setText("10:00");
                    clockView.changeData(0,500,200);
                    clockView.invalidate();
                    mFuncButton.setText("开始");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        StatusBarUtils.setWindowStatusBarColor(this, R.color.main_color);
        setContentView(R.layout.activity_main);
        hintTextView=findViewById(R.id.main_hint);
        textView = findViewById(R.id.main_time);
        clockView = findViewById(R.id.main_clock);
        mFuncButton = findViewById(R.id.main_func_btn);
        observer=new ScreenObserver(this);
        observer.startObserver(new ScreenObserver.ScreenStateListener() {
            @Override
            public void onScreenOn() {

            }

            @Override
            public void onScreenOff() {

            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onUserPresent() {
                if(mFuncButton.getText()=="取消") {
                    hintTextView.setText("对不起你失败了，该人已变猪");
                    timer.cancel();
                    clockView.changeData(0, 500, 200);
                    textView.setText("10:00");
                    clockView.drawable=getDrawable(R.drawable.ic_pig);
                    clockView.invalidate();
                }
            }
        });

        clockView.setRadiusChangeListener(new CircleRotate.RadiusListener() {
            @Override
            public void onRadiusChangeListener(double radius) {
                Log.e("radius", radius + "");
                //textView.setText((int)((radius/360)*120));
                if (mFuncButton.getText().equals("开始")) {
                    Message message = new Message();
                    message.what = 0;
                    Bundle bundle = new Bundle();
                    bundle.putDouble("radius", radius);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }
            }
        });
        mFuncButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (((Button) v).getText().equals("开始")) {
                    hintTextView.setText("请好好学习，别玩手机");
                    clockView.drawable=getDrawable(R.drawable.ic_femail);
                    timer= new CountDownTimer((long) (displayTime * 60 * 1000), 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            Message msg = new Message();
                            msg.what = 1;
                            msg.arg1 = (int) (millisUntilFinished / 60000);
                            msg.arg2 = (int) ((millisUntilFinished % 60000) / 1000);
                            mHandler.sendMessage(msg);
                        }

                        @Override
                        public void onFinish() {

                        }
                    }.start();
                } else {
                    Message msg = new Message();
                    msg.what = 2;
                    mHandler.sendMessage(msg);
                }
            }
        });
    }

    @Override
    public void onRadiusChangeListener(double radius) {

    }
}
