package cn.panyunyi.focks.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import cn.panyunyi.focks.ui.activity.MainActivity;

public class GetAppStateService extends Service {
    Field mLastEventField;
    private boolean isFirst=false;
    private volatile String topPackageName;
    private ComponentName runningTopActivity;
    private static final long TWENTYSECOND = 20000;
    private static final long THIRTYSECOND = 30000;
    private Context mContext;
    public class MyBinder extends Binder {
        public GetAppStateService getService() {
            return GetAppStateService.this;
        }
    }
    //通过binder实现调用者client与Service之间的通信
    private MyBinder binder = new MyBinder();



    @Override
    public void onCreate() {
        super.onCreate();
        this.mContext=this;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.e("onStartCommand","startCommand");
        Timer timer=new Timer();
        TimerTask task=new TimerTask() {
            @Override
            public void run() {
                judgeAppState(mContext);
                if(topPackageName!=null) {
                    if (!topPackageName.equals(mContext.getPackageName())&&!topPackageName.contains("home")) {
                        MainActivity.isAlwaysForeGround = false;
                    }
                }
            }
        };
        timer.schedule(task,1000,1000);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }
    public String getTopPackageName(){
        return topPackageName;
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public String judgeAppState(Context context) {
        //改进版本的通过使用量统计功能获取前台应用
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> stats;
        if (isFirst) {
            assert mUsageStatsManager != null;
            stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - TWENTYSECOND, time);
        } else {
            assert mUsageStatsManager != null;
            stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - THIRTYSECOND, time);
        }

        // Sort the stats by the last time used
        if (stats != null) {
            TreeMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
            long start = System.currentTimeMillis();
            topPackageName=null;
            for (UsageStats usageStats : stats) {
                mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
            }
            if (mySortedMap != null && !mySortedMap.isEmpty()) {

                NavigableSet<Long> keySet = mySortedMap.navigableKeySet();
                Iterator iterator = keySet.descendingIterator();
                boolean secondFlag=false;
                while (iterator.hasNext()) {

                    UsageStats usageStats = mySortedMap.get(iterator.next());
                    if (mLastEventField == null) {
                        try {
                            mLastEventField = UsageStats.class.getField("mLastEvent");
                        } catch (NoSuchFieldException e) {
                            break;
                        }
                    }
                    if (mLastEventField != null) {
                        int lastEvent = 0;
                        try {
                            lastEvent = mLastEventField.getInt(usageStats);
                        } catch (IllegalAccessException e) {
                            break;
                        }
                        if (lastEvent == 1) {
                                topPackageName = usageStats.getPackageName();
                                break;
                        }
                    } else {
                        break;
                    }
                }

                if (topPackageName == null) {
                    topPackageName = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
                runningTopActivity = new ComponentName(topPackageName, "");
            }
        }

        return topPackageName;
    }
}
