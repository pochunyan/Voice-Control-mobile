package com.example.tony.smarthelper.WakeUp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

public class RecogWakeupListener extends SimpleWakeupListener implements IStatus {
    private static final String TAG = "RecogWakeupListener";

    private Handler handler;

    public RecogWakeupListener(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onSuccess(String word, WakeUpResult result, Context context) {
        super.onSuccess(word, result, context);
        Log.d(TAG, "onSuccess");
        handler.sendMessage(handler.obtainMessage(STATUS_WAKEUP_SUCCESS));

        // 喚醒成功的話, 關閉喚醒本身recorder, 並且啟動識別的recorder...
        Log.d(TAG, "喚醒成功的話, 關閉喚醒本身recorder, 並且啟動識別的recorder...");

        // 在view 裡面navigate to another Activity
        /*
        Intent intent = new Intent();

        intent.setClass(context, HomeActivity.class);    //設置成要啟動的Activity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        Activity activity = (Activity) context;
        activity.finish();
        */

    }
}
