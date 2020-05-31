package cn.panyunyi.focks.utils;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeMap;

public class CheckApp {
    Field mLastEventField;
    private boolean isFirst;
    private volatile String topPackageName;
    private ComponentName runningTopActivity;
    private static final long TWENTYSECOND = 20000;
    private static final long THIRTYSECOND = 30000;
    private Context mContext;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static CheckApp getInstance(){
        return new CheckApp();
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ComponentName judgeAppState(Context context) {
        //改进版本的通过使用量统计功能获取前台应用
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> stats;
        if (isFirst) {
            stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - TWENTYSECOND, time);
        } else {
            stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - THIRTYSECOND, time);
        }


        // Sort the stats by the last time used
        if (stats != null) {
            TreeMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
            long start = System.currentTimeMillis();
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
                            if(!secondFlag){
                                secondFlag=true;
                            }else {
                                topPackageName = usageStats.getPackageName();
                                break;
                            }
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
    return runningTopActivity;
    }
}
