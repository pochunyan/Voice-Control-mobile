package com.example.tony.smarthelper.WakeUp;

import android.content.Context;

import com.example.tony.smarthelper.Util.Logger;

public class SimpleWakeupListener implements IWakeupListener{

    private static final String TAG = "SimpleWakeupListener";

    @Override
    public void onSuccess(String word, WakeUpResult result, Context context) {
        Logger.info(TAG, "喚醒成功，喚醒詞：" + word);
    }

    @Override
    public void onStop() {
        Logger.info(TAG, "喚醒識別結束：");
    }

    @Override
    public void onError(int errorCode, String errorMessge, WakeUpResult result) {
        Logger.info(TAG, "喚醒錯誤：" + errorCode + ";錯誤訊息：" + errorMessge + "; 原始返回" + result.getOrigalJson());
    }

    @Override
    public void onASrAudio(byte[] data, int offset, int length) {
        Logger.info(TAG, "audio data： " + data.length + "/offset:" + offset + "/length:" + length);
    }
}
