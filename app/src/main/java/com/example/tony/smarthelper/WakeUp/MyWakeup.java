package com.example.tony.smarthelper.WakeUp;

import android.content.Context;
import android.util.Log;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.example.tony.smarthelper.Util.Logger;

import org.json.JSONObject;

import java.util.Map;

public class MyWakeup{
    private static boolean isInited = false;

    private EventManager wp;
    private EventListener eventListener;

    private static final String TAG = "MyWakeup";

    public MyWakeup(Context context, EventListener eventListener) {
        if (isInited) {
            Logger.error(TAG, "還未使用release()，請勿新建一個新類");
            throw new RuntimeException("還未使用release()，請勿新建一個新類");
        }
        isInited = true;
        this.eventListener = eventListener;

        wp = EventManagerFactory.create(context, "wp");
        wp.registerListener(eventListener);
    }

    public MyWakeup(Context context, IWakeupListener eventListener) {
        this(context, new WakeupEventAdapter(eventListener, context));
    }

    public void start(Map<String, Object> params) {
        String json = new JSONObject(params).toString();
        Logger.info(TAG + ":start().Debug", "wakeup params(反饋請帶上此行日誌):" + json);
        wp.send(SpeechConstant.WAKEUP_START, json, null, 0, 0);
    }

    public void stop() {
        Log.d(TAG, "喚醒結束");
        wp.send(SpeechConstant.WAKEUP_STOP, null, null, 0, 0);
    }

    public void release() {
        stop();
        wp.unregisterListener(eventListener);
        wp = null;
        isInited = false;
    }

}
