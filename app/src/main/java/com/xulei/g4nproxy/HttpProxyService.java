package com.xulei.g4nproxy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.xulei.g4nproxy_client.ProxyClient;
import com.xulei.g4nproxy_protocol.protocol.Constants;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

/**
 * Created by virjar on 2019/2/22.
 */

public class HttpProxyService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        startService();
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startService();
        return START_STICKY;
    }

    private AtomicBoolean serviceStarted = new AtomicBoolean(false);

    private void startService() {
        if (serviceStarted.compareAndSet(false, true)) {
            Thread thread = new Thread("g4ProxyThread") {
                @Override
                public void run() {
                    startServiceInternal();
                }
            };
            thread.setDaemon(true);
            thread.start();

        }
    }

    private void setNotifyChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel notificationChannel = new NotificationChannel(BuildConfig.APPLICATION_ID,
                "channel", NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.YELLOW);
        notificationChannel.setShowBadge(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }
        manager.createNotificationChannel(notificationChannel);
    }

    private void startServiceInternal() {

        setNotifyChannel();

        Notification.Builder builder = new Notification.Builder(this.getApplicationContext()); //获取一个Notification构造器
        // 设置PendingIntent
        Intent nfIntent = new Intent(this, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, FLAG_UPDATE_CURRENT))
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher)) // 设置下拉列表中的图标(大图标)
                .setContentTitle("G4Proxy") // 设置下拉列表里的标题
                .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                .setContentText("代理服务agent") // 设置上下文内容
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(BuildConfig.APPLICATION_ID);
        }

        Notification notification = builder.build(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        startForeground(110, notification);// 开始前台服务

        ALOG.setUpLogComponent(new ALOG.LogImpl() {
            @Override
            public void i(String tag, String msg) {
                Log.i(tag, msg);
            }

            @Override
            public void w(String tag, String msg) {
                Log.w(tag, msg);
            }

            @Override
            public void w(String tag, String msg, Throwable throwable) {
                Log.w(tag, msg, throwable);
            }

            @Override
            public void e(String tag, String msg) {
                Log.e(tag, msg);
            }

            @Override
            public void e(String tag, String msg, Throwable throwable) {
                Log.e(tag, msg, throwable);
            }
        });

//        Log.i("weijia", "start G4Proxy front service");
//        Launcher.startHttpProxyService(3128);  // 启动代理服务器

        Log.i("weijia", "start private network forward task");
        String clientID = Settings.System.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
//        ProxyClient.start(Constants.g4ProxyServerHost_1, Constants.g4ProxyServerPort, clientID);
        ProxyClient.start(Constants.g4nproxyServerHost,Constants.g4nproxyServerPort,clientID);

//        ProxyClient.start(Constants.g4ProxyServerHost_2, Constants.g4ProxyServerPort, clientID);
    }

}
