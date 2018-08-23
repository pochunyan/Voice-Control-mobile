package com.example.tony.smarthelper.WakeUp;

import android.content.Context;
import android.util.Log;

import com.baidu.speech.EventListener;
import com.baidu.speech.asr.SpeechConstant;
import com.example.tony.smarthelper.Util.ErroTranslation;
import com.example.tony.smarthelper.Util.Logger;

public class WakeupEventAdapter implements EventListener, IStatus{
    private IWakeupListener listener;
    private Context context;

    public WakeupEventAdapter(IWakeupListener listener, Context context) {
        this.listener = listener;
        this.context = context;
    }

    private static final String TAG = "WakeupEventAdapter";


    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {

        // android studio日志Monitor 中搜索 WakeupEventAdapter即可看见下面一行的日志
        Logger.info(TAG, "wakeup name:" + name + "; params:" + params);
        if (SpeechConstant.CALLBACK_EVENT_WAKEUP_SUCCESS.equals(name)) {
            WakeUpResult result = WakeUpResult.parseJson(name, params);
            int errorCode = result.getErrorCode();
            if (result.hasError()) { // error不為0依舊有可能是異常狀況
                Log.d(TAG, ":喚醒成功但有錯");
                listener.onError(errorCode, ErroTranslation.wakeupError(errorCode), result);
            } else {
                String word = result.getWord();
                Log.d(TAG, ":喚醒成功");
                listener.onSuccess(word, result, context);
            }
        } else if (SpeechConstant.CALLBACK_EVENT_WAKEUP_ERROR.equals(name)) {
            WakeUpResult result = WakeUpResult.parseJson(name, params);
            int errorCode = result.getErrorCode();
            Log.d(TAG, ":喚醒報錯");
            if (result.hasError()) {
                listener.onError(errorCode, ErroTranslation.wakeupError(errorCode), result);
            }
        } else if (SpeechConstant.CALLBACK_EVENT_WAKEUP_STOPED.equals(name)) {
            listener.onStop();
            Log.d(TAG, ":喚醒關閉");
        } else if (SpeechConstant.CALLBACK_EVENT_WAKEUP_AUDIO.equals(name)) {

            listener.onASrAudio(data, offset, length);
            Log.d(TAG, ":音調回調");
        }
    }
}
