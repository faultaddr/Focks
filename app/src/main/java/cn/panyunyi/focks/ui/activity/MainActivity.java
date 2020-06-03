package cn.panyunyi.focks.ui.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.internal.NavigationMenuItemView;
import com.google.android.material.navigation.NavigationView;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeMap;

import javax.security.auth.login.LoginException;

import cn.panyunyi.focks.R;
import cn.panyunyi.focks.service.GetAppStateService;
import cn.panyunyi.focks.ui.custom.CircleRotate;
import cn.panyunyi.focks.ui.fragment.FinishDialogFragment;
import cn.panyunyi.focks.utils.CheckApp;
import cn.panyunyi.focks.utils.ScreenObserver;
import cn.panyunyi.focks.utils.StatusBarUtils;

import static android.app.Service.START_STICKY;

public class MainActivity extends AppCompatActivity implements CircleRotate.RadiusListener, View.OnClickListener {

    TextView textView;
    CircleRotate clockView;
    Button mFuncButton;
    TextView hintTextView;
    ImageView settingButton;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    ImageView menuAddBtn;
    double radius;
    int displayTime;
    CountDownTimer timer;
    ScreenObserver observer;
    GetAppStateService.MyBinder binder;
    public static boolean isAlwaysForeGround=true;
    FinishDialogFragment fragment;
    FragmentTransaction transaction;

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (GetAppStateService.MyBinder) service;


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    View.OnTouchListener listener = new View.OnTouchListener() {
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

                    clockView.x = (int) (Math.cos((float) ((clockView.totalRadius - 90) / 360.0) * 2 * Math.PI) * clockView.rawRadius) + 500;
                    clockView.y = (int) (Math.sin((float) ((clockView.totalRadius - 90) / 360.0) * 2 * Math.PI) * clockView.rawRadius) + 500;
                    //Log.e(">>>",clockView.x+"----"+clockView.y);
                    clockView.invalidate();
                    break;
                case 2:
                    clockView.setOnTouchListener(null);
                    timer.cancel();
                    textView.setText("10:00");
                    clockView.drawable=getResources().getDrawable(R.drawable.ic_tree);
                    clockView.changeData(0, 500, 200);
                    clockView.invalidate();
                    mFuncButton.setText("开始");
                    break;
                case 3:
                    Log.e("finish","finish");
                    fragment=new FinishDialogFragment();
                    fragment.show(getSupportFragmentManager(),"FinishDialog");
                    Menu menu=navigationView.getMenu();
                    View v=menu.getItem(0).getActionView();
                    TextView view=(TextView)v.findViewById(R.id.msg_bg);
                    DecimalFormat decimalFormat=new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.

                    float totalHours= (float) (Integer.parseInt(view.getText().toString())+displayTime/60.0);
                    String formatHours=decimalFormat.format(totalHours);//format 返回的是字符串
                    view.setText(formatHours);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("onCreate","create");
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        StatusBarUtils.setWindowStatusBarColor(this, R.color.main_color);

        setContentView(R.layout.activity_main);

        checkUsagePermission();
        initView();

    }

    private void initView() {
        hintTextView = findViewById(R.id.main_hint);
        textView = findViewById(R.id.main_time);
        clockView = findViewById(R.id.main_clock);
        mFuncButton = findViewById(R.id.main_func_btn);
        settingButton=findViewById(R.id.setting_btn);
        drawerLayout=findViewById(R.id.drawer_layout);
        navigationView=findViewById(R.id.navigation_view);
        menuAddBtn=findViewById(R.id.menu_add_button);

        observer = new ScreenObserver(this);
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

            }
        });

        clockView.setRadiusChangeListener(new CircleRotate.RadiusListener() {
            @Override
            public void onRadiusChangeListener(double radius) {

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
                    Intent intent = new Intent(MainActivity.this, GetAppStateService.class);
                    startService(intent);
                    hintTextView.setText("请好好学习，别玩手机");
                    clockView.drawable = getDrawable(R.drawable.ic_femail);
                    timer = new CountDownTimer((long) (displayTime * 10 * 100), 1000) {
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
                            Message msg=new Message();
                            msg.what=3;
                            mHandler.sendMessage(msg);
                        }
                    }.start();
                } else {
                    Message msg = new Message();
                    msg.what = 2;
                    mHandler.sendMessage(msg);
                }
            }
        });
        settingButton.setOnClickListener(this);
        menuAddBtn.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("onStart","start");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unbindService(connection);
        observer.shutdownObserver();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent(this, GetAppStateService.class);
        bindService(intent, connection, Context.BIND_IMPORTANT);
        Log.e("onResume","resume");
        if(!isAlwaysForeGround) {
            if (mFuncButton.getText() == "取消") {
                hintTextView.setText("对不起你失败了，该人已变猪");
                timer.cancel();
                stopService(intent);
                mFuncButton.setText("开始");
                clockView.changeData(0, 500, 200);
                textView.setText("10:00");
                clockView.drawable = getDrawable(R.drawable.ic_pig);
                clockView.invalidate();
            }
        }

    }
    /**
     * 注册权限申请回调
     *
     * @param requestCode  申请码
     * @param permissions  申请的权限
     * @param grantResults 结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ;
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "CALL_PHONE Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }

    }

    /*
     * android 6.0+
     * 相机权限需要动态获取
     *
     *
     *
     * */
    private int requestPermission() {
        //判断Android版本是否大于23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.PACKAGE_USAGE_STATS);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.PACKAGE_USAGE_STATS}, 0);
                return 0;
            } else {
                //已有权限
                return 1;
            }

        } else {
            //API 版本在23以下
            return 1;
        }

    }
    @Override
    public void onRadiusChangeListener(double radius) {

    }
    private boolean checkUsagePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            mode = appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), getPackageName());
            boolean granted = mode == AppOpsManager.MODE_ALLOWED;
            if (!granted) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivityForResult(intent, 1);
                return false;
            }
        }
        return true;
    }
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            mode = appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), getPackageName());
            boolean granted = mode == AppOpsManager.MODE_ALLOWED;
            if (!granted) {
                Toast.makeText(this, "请开启该权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.setting_btn:
                drawerLayout.openDrawer(Gravity.LEFT);
                break;
            case R.id.menu_add_button:
                navigationView.getMenu().add(1,1,1,"hhh").setActionView(R.layout.base_hint);
        }
    }
}
