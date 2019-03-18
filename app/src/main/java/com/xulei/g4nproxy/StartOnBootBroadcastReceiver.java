package com.xulei.g4nproxy;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by virjar on 2018/8/23.
 */
public class StartOnBootBroadcastReceiver extends BroadcastReceiver {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("weijia", "receive start service broadcast");
        Intent service = new Intent(context, HttpProxyService.class);
        context.startService(service);


        // clearAbortBroadcast();
    }
}
