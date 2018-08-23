package com.example.tony.smarthelper.WakeUp;

import android.content.Context;

public interface IWakeupListener {
    void onSuccess(String word, WakeUpResult result, Context context);

    void onStop();

    void onError(int errorCode, String errorMessge, WakeUpResult result);

    void onASrAudio(byte[] data, int offset, int length);
}