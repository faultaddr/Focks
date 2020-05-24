package cn.panyunyi.focks.app;

import java.util.List;

public class Application extends android.app.Application {
    private final static String TAG="Application";
    public static List<Application>mApplicationInfos;
    @Override
    public void onCreate() {
        super.onCreate();
    }
}
