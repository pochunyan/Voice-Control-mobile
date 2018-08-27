package com.example.tony.smarthelper.Main;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.location.Location;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.example.tony.smarthelper.Geocoding.Geocode;
import com.example.tony.smarthelper.R;
import com.example.tony.smarthelper.Recog.CommonRecogParams;
import com.example.tony.smarthelper.Recog.RecogResult;
import com.example.tony.smarthelper.USB.UsbService;
import com.example.tony.smarthelper.Util.CWebViewVideo;
import com.example.tony.smarthelper.Util.ErroTranslation;
import com.example.tony.smarthelper.Util.Gccphat;
import com.example.tony.smarthelper.Util.Logger;
import com.example.tony.smarthelper.Util.Synthesizer;
import com.example.tony.smarthelper.WakeUp.IStatus;
import com.example.tony.smarthelper.WakeUp.IWakeupListener;
import com.example.tony.smarthelper.WakeUp.MyWakeup;
import com.example.tony.smarthelper.WakeUp.RecogWakeupListener;
import com.example.tony.smarthelper.WakeUp.WakeUpResult;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import taobe.tec.jcc.JChineseConvertor;

public class WakeUpActivity extends AppCompatActivity implements IStatus, EventListener{
    // retrieve youtube video key by keyword with get/post method
    // global speech
    String commandForSpeech = "";

    // using task to avoid MainthreadException
    public class Task extends AsyncTask<Void,Void,String> {

        JSONObject responseJSON= null;
        String input = "";
        CWebViewVideo cWebViewVideo;

        public Task(String keyword, CWebViewVideo cWebViewVideo) {
            input = keyword;
            this.cWebViewVideo = cWebViewVideo;
        }
        @Override
        public void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        public String doInBackground(Void... arg0) {
            Log.d(TAG, "do in background.");
            URL url = null;
            BufferedReader reader = null;
            StringBuilder stringBuilder;
            String key = "AIzaSyDw7WRtm_hpY4e1VNxr3QEr0GasfoXbOJ8";

            try
            {
                // create the HttpURLConnection

                if(input.contains(" "))
                    input = input.replace(" ","+");
                url = new URL("https://www.googleapis.com/youtube/v3/search?part=snippet&q=%22="+input +"%22&type=video&key="+ key);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // 使用甚麼方法做連線
                connection.setRequestMethod("GET");

                // 設定TimeOut時間
                connection.setReadTimeout(15*1000);
                connection.connect();

                // 伺服器回來的參數
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                stringBuilder = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    stringBuilder.append(line + "\n");
                }
                return stringBuilder.toString();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                // close the reader; this can throw an exception too, so
                // wrap it in another try/catch block.
                if (reader != null)
                {
                    try
                    {
                        reader.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        public void onPostExecute(String result) {
            super.onPostExecute(result);
            if(!"".equals(result) || null != result){
                Log.d(TAG, "result:test:" + result);

                try {
                    ArrayList<String> playlist = new ArrayList<String>();
                    responseJSON = new JSONObject(result);
                    JSONArray items = responseJSON.getJSONArray("items");
                    for(int i=0;i<items.length();i++) {
                        String vid = items.getJSONObject(i).getJSONObject("id").getString("videoId").toString();
                        playlist.add(vid);
                        Log.d(TAG, "final result: "+ vid);
                    }
                    String[] tmpList = new String[playlist.size()];
                    String videoId = playlist.toArray(tmpList)[0];

                    String frameVideo = "<html><body>Video From YouTube<br><iframe width=\"420\" height=\"315\" src=\"https://www.youtube.com/embed/"+ videoId + " \" frameborder=\"0\" allowfullscreen></iframe></body></html>";

                    final WebView displayYoutubeVideo = (WebView) findViewById(R.id.video);
                    WebSettings webSettings = displayYoutubeVideo.getSettings();
                    webSettings.setJavaScriptEnabled(true);
                    webSettings.setMediaPlaybackRequiresUserGesture(false);
                    displayYoutubeVideo.setWebViewClient(new WebViewClient(){
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            super.onPageFinished(view, url);
                            simulateTouchEvent(view, view.getWidth() / 2f, view.getHeight() / 2f);
                        }
                    });
                    displayYoutubeVideo.loadData(frameVideo, "text/html", "utf-8");
                    displayYoutubeVideo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "test auto click");
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static final String TAG = "WakeUpActivity";

    // USB
    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }; 
    private UsbService usbService;
    private MyHandler mHandler;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {

            Log.d(TAG, "connection");
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    protected Handler handler;
    private static boolean isInited = false;
    private static boolean isInited_recog = false;
    private EventManager wp;
    private EventManager asr;

    private String realChinese="";
    protected String currentJson;
    private String finalOutput = "";

    // widget
    private TextView txv_command;
    private TextView txv_degree;
    private TextView txv_usb;
    private WebView videoView;
    private CWebViewVideo cWebViewVideo;

    // textview status
    final static String WAIT_WAKEUP = "您好！ 有事可呼叫 小威小威";
    final static String WAIT_COMMAND = "在這！ 有什麼事情需要幫忙的嗎?";
    final static String SPEECH_WAIT_COMMAND = "在這呢";
    final static String SPEECH_NOT_UNDERSTAND_COMMAND = "沒有聽懂!請再說一遍..";
    final static String SPEECH_RESPONSE_COMMAND = "知道了";

    // package
    String line_package = "jp.naver.line.android";
    String weChat_package = "com.tencent.mm";

    Synthesizer synthesizer;

    // map parameter
    String stlat;
    String stlong;
    String endlat;
    String endlong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wake_up);
        Log.d(TAG, "onCreate.");
        initView();
        // 語音合成
        synthesizer = new Synthesizer(WakeUpActivity.this);
        synthesizer.initTTs();
        // handle the print out messages
        mHandler = new MyHandler(this);
        // handle messages
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                handleMsg(msg);
            }
        };
        com.example.tony.smarthelper.Util.Logger.setHandler(handler);
    }

    // get elements from layout
    private void initView() {
        txv_command = (TextView) findViewById(R.id.txv_command);
        txv_degree = (TextView) findViewById(R.id.txv_degree);
        txv_usb = (TextView) findViewById(R.id.txv_usb);
        videoView = (WebView)findViewById(R.id.video);
        videoView.setVisibility(View.VISIBLE);
        cWebViewVideo = new CWebViewVideo(this, videoView);
    }

    // show msg function
    protected void handleMsg(Message msg) {
        if (msg.obj != null) {
            Log.d(TAG, msg.obj.toString() + "\n");
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart...");
        super.onStart();
        wakeup();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop...");
        super.onStop();
    }

    private void wakeup(){
        Log.d(TAG, "啟動喚醒...");
        txv_command.setText(WAIT_WAKEUP);
        if (isInited) {
            Logger.error(TAG, "wakeup: 還未使用release()，請勿新建一個新類");
            //throw new RuntimeException("還未使用release()，請勿新建一個新類");
            if(wp!=null)
                endWakeup();
        }
        // Baidu SDK
        // infile参數用於控制識別一個PCM音頻流（或文件），每次進入程序都將該值清除，以避免體驗時沒有使用錄音的問題
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().remove(SpeechConstant.IN_FILE).commit();
        isInited = true;
        wp = EventManagerFactory.create(this, "wp");
        wp.registerListener(this);

        // set parameter of wp, initialize wp.
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(SpeechConstant.WP_WORDS_FILE, "assets:///WakeUp.bin");
        //params.put(SpeechConstant.APP_ID, "11570097");
        String json = new JSONObject(params).toString();
        Logger.info(TAG + ":start().Debug", "wakeup params(反饋請帶上此行日誌):" + json);
        wp.send(SpeechConstant.WAKEUP_START, json, null, 0, 0);
    }


    private void endWakeup(){
        // end using wp
        Log.d(TAG, "喚醒結束");
        wp.send(SpeechConstant.WAKEUP_STOP, null, null, 0, 0);
        wp.unregisterListener(this);
        wp = null;
        isInited = false;
    }

    // wake up callback function
    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        Logger.info(TAG, "wakeup name:" + name + "; params:" + params);
        if (SpeechConstant.CALLBACK_EVENT_WAKEUP_SUCCESS.equals(name)) {
            WakeUpResult result = WakeUpResult.parseJson(name, params);
            int errorCode = result.getErrorCode();
            // error 不為0,還是有可能是異常狀況
            if (result.hasError()) {
                Log.d(TAG, ":喚醒成功但有錯:" + errorCode + "/" + result);
            } else {
                String word = result.getWord();
                Log.d(TAG, ":喚醒成功:" + word);
                txv_command.setText(WAIT_COMMAND);
                commandForSpeech = SPEECH_WAIT_COMMAND;
                synthesizer.speak(commandForSpeech);

                // delay function
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after 2.2s = 2200ms
                        endWakeup();
                        recog();
                    }
                }, 2200);
            }
        } else if (SpeechConstant.CALLBACK_EVENT_WAKEUP_ERROR.equals(name)) {
            WakeUpResult result = WakeUpResult.parseJson(name, params);
            int errorCode = result.getErrorCode();
            if (result.hasError()) {
                Log.d(TAG, ":喚醒報錯:" + errorCode + "/" + result);
            }
        } else if (SpeechConstant.CALLBACK_EVENT_WAKEUP_STOPED.equals(name)) {
            Log.d(TAG, ":喚醒關閉");
        }
    }

    // 語音識別
    private void recog(){
        // set recog asr parameters, and initialize
        Log.d(TAG, "開始識別");
        if (isInited_recog) {
            Logger.error(TAG, "recog : 還未使用release()，請勿新建一個新類");
            throw new RuntimeException("還未使用release()，請勿新建一個新類");
        }
        isInited_recog = true;
        asr = EventManagerFactory.create(this, "asr");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        final Map<String, Object> params =  new HashMap<String, Object>();
        params.put(SpeechConstant.ACCEPT_AUDIO_DATA, false);
        params.put(SpeechConstant.DISABLE_PUNCTUATION, false);
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, true);
        String json = new JSONObject(params).toString();
        Logger.info(TAG + ".Debug", "asr params(識別參數，反饋請帶上此行日誌):" + json);
        asr.send(SpeechConstant.ASR_START, json, null, 0, 0);
        asr.registerListener(recogEventListener);
    }


    private void endRecog(){
        // end using asr
        if (asr == null) {
            return;
        }
        Logger.info(TAG, "取消識別");
        if (asr != null) {
            asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        }
        asr.unregisterListener(recogEventListener);
        asr = null;
        isInited_recog = false;
    }


    // callback function of asr
    EventListener recogEventListener = new EventListener() {
        @Override
        public void onEvent(String name, String params, byte[] data, int offset, int length) {
            currentJson = params;
            if (false) {
                return;
            }
            if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_LOADED)) {
                Log.d(TAG, "CALLBACK_EVENT_ASR_LOADED");

            } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_UNLOADED)) {
                Log.d(TAG, "CALLBACK_EVENT_ASR_UNLOADED");

            } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_READY)) {
                Log.d(TAG, "CALLBACK_EVENT_ASR_READY");

            } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_BEGIN)) {
                Log.d(TAG, "CALLBACK_EVENT_ASR_BEGIN");

            } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_END)) {
                Log.d(TAG, "CALLBACK_EVENT_ASR_END");
                txv_command.setText("");

            } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
                Log.d(TAG, "CALLBACK_EVENT_ASR_PARTIAL");
                RecogResult recogResult = RecogResult.parseJson(params);
                if (recogResult.isFinalResult()) {

                } else if (recogResult.isPartialResult()) {
                    Log.d(TAG, "識別部分結果！");
                    Log.d(TAG, "文字顯示:" + recogResult.getResultsRecognition()[0]);
                    finalOutput = recogResult.getResultsRecognition()[0];
                }
            } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_FINISH)) {
                Log.d(TAG, "CALLBACK_EVENT_ASR_FINISH");
                // 識別結束, 最終識別結果或可能錯誤
                RecogResult recogResult = RecogResult.parseJson(params);
                if (recogResult.hasError()) {
                    Log.d(TAG, "識別發生錯誤");
                    synthesizer.speak(SPEECH_NOT_UNDERSTAND_COMMAND);
                    try{
                        // delay 1 second
                        Thread.sleep(1000);
                    } catch(InterruptedException e){
                        e.printStackTrace();

                    }
                    endRecog();
                    wakeup();
                } else {
                    Log.d(TAG, "Ans:" + finalOutput);
                    commandForSpeech = SPEECH_RESPONSE_COMMAND;
                    // 繁體中文 converter
                    JChineseConvertor jChineseConvertor = null;
                    try {
                        jChineseConvertor = JChineseConvertor.getInstance();
                        realChinese = jChineseConvertor.s2t(finalOutput);
                        txv_usb.setText(realChinese);
                        Log.d(TAG, "繁體中文: " + realChinese);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // delay
                    if(!realChinese.isEmpty()){
                        if (realChinese.equals("調整方向") || realChinese.equals("調整方")){
                            synthesizer.speak(commandForSpeech);
                            try{
                                // delay 1 second
                                Thread.sleep(1900);
                            } catch(InterruptedException e){
                                e.printStackTrace();

                            }
                            autoRecording();
                        }
                        else if (realChinese.length()>=3 && realChinese.substring(0,2).equals("播放")){
                            synthesizer.speak(commandForSpeech);
                            String songName = realChinese.substring(2);
                            Log.d(TAG, "歌曲:" + songName);
                            new Task(songName, cWebViewVideo).execute();
                        }
                        else if (realChinese.equals("打開賴") || realChinese.equals("打開line")){
                            synthesizer.speak(commandForSpeech);
                            startNewActivity(WakeUpActivity.this, line_package);
                        }
                        else if (realChinese.equals("打開微信")){
                            synthesizer.speak(commandForSpeech);
                            startNewActivity(WakeUpActivity.this, weChat_package);
                        }
                        else if (realChinese.length()>=3 && realChinese.substring(0,2).equals("前往")){
                            synthesizer.speak(commandForSpeech);
                            String locationName = realChinese.substring(2);
                            Log.d(TAG, "地名:" + locationName);
                            navigateMap(locationName);
                        }
                        else if (realChinese.equals("回到主畫面") || realChinese.equals("到主畫面") || realChinese.equals("回到主畫") || realChinese.equals("主畫面")){
                            Log.d(TAG, "Resume to the activity");
                            synthesizer.speak(commandForSpeech);
                            Intent intent = new Intent(WakeUpActivity.this, WakeUpActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivityIfNeeded(intent, 0);
                        }
                        else if (realChinese.length()>=4 && (realChinese.substring(0,4).equals("傳送訊息") || realChinese.substring(0,4).equals("傳送信息"))){
                            Log.d(TAG, realChinese);
                            isInited = false;
                            endRecog();
                            Intent intent = new Intent(WakeUpActivity.this, WhatsAppActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            commandForSpeech = realChinese;
                            synthesizer.speak(commandForSpeech);
                        }
                    }
                    endRecog();
                    wakeup();
                }
            } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_LONG_SPEECH)) {
                Log.d(TAG, "CALLBACK_EVENT_ASR_LONG_SPEECH");
            } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_EXIT)) {
                // isinited = false -> is for end the wakeup, if this does not call, then the activity resume will failed.
                isInited = false;
                Log.d(TAG, "CALLBACK_EVENT_ASR_EXIT");
            }
        }
    };

    // navigate to google map and show the direction
    private void navigateMap(String locationName){
        Log.d(TAG, "navigation Map:" + locationName);
        Geocode geocode = new Geocode(WakeUpActivity.this);
        // get the destination lat/long
        List ans = geocode.findLocation(locationName);
        endlat = ans.get(0).toString();
        endlong = ans.get(1).toString();

        // get the current position lat/long
        Location location = geocode.getLastKnownLocation();
        if(location != null){
            stlat = String.valueOf(location.getLatitude());
            stlong = String.valueOf(location.getLongitude());
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?saddr=" + stlat + "," + stlong + "&daddr="+ endlat +","+ endlong));
            startActivity(intent);
        }
        else
            Log.d(TAG, "Location = null");
    }



    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume...");
        // Start listening notifications from UsbService
        setFilters();
        // Start UsbService(if it was not started before) and Bind it
        startService(UsbService.class, usbConnection, null);

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause...");
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    // For USB services
    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        Log.d(TAG, "startService.");
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        Log.d(TAG, "setFilters");
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<WakeUpActivity> mActivity;

        public MyHandler(WakeUpActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    //mActivity.get().display.append(data);
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    // navigate to other application
    public void startNewActivity(Context context, String packageName) {
        Intent intent1 = new Intent(Intent.ACTION_SEND);
        intent1.setType("text/plain");
        String title = " this is message ";
        intent1.setPackage(packageName);
        intent1.putExtra(Intent.EXTRA_TEXT,title);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent1);
    }


    // auto click screen
    private float simulateTouchEvent(View view, float x, float y) {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        int metaState = 0;

        view.dispatchTouchEvent(MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_DOWN, x, y, metaState));

        view.dispatchTouchEvent(MotionEvent.obtain(downTime + 1000, eventTime + 1000,
                MotionEvent.ACTION_UP, x,y, metaState));

        return 0;
    }




    private static int RECORDER_SAMPLERATE = 44100;
    private static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    boolean isRecording = false;
    int bufferSizeInBytes = 0;


    private double [] return_Data = new double[5];
    private double [] SUM = new double[2];

    public void autoRecording(){
        txv_degree.setText("調整方向");
        // Get the minimum buffer size required for the successful creation of an AudioRecord object.
        bufferSizeInBytes = AudioRecord.getMinBufferSize( RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING
        );
        // Initialize Audio Recorder.
        AudioRecord audioRecorder = new AudioRecord( MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING,
                bufferSizeInBytes
        );
        // Start Recording.
        audioRecorder.startRecording();
        isRecording = true;
        // for auto stop
        int numberOfReadBytes   = 0;
        byte audioBuffer[]      = new  byte[bufferSizeInBytes];
        boolean recording       = false;
        float tempFloatBuffer[] = new float[3];
        int tempIndex           = 0;
        short[] BUFFER = new short[bufferSizeInBytes];

        ArrayList<Double> total = new ArrayList<>();
        ArrayList<ArrayList<Double>> testtry = new ArrayList<>();
        while( true )
        {
            float totalAbsValue = 0.0f;
            short sample        = 0;

            numberOfReadBytes = audioRecorder.read( audioBuffer, 0, bufferSizeInBytes );

            // Analyze Sound.
            for( int i=0; i<bufferSizeInBytes; i+=2 )
            {
                sample = (short)( (audioBuffer[i]) | audioBuffer[i + 1] << 8 );
                totalAbsValue += (float)Math.abs( sample ) / ((float)numberOfReadBytes/(float)2);
            }

            // Jason Chen
            for (int i = 0; i < numberOfReadBytes / 2; i++){
                short LowData = (short)audioBuffer[2 * i];
                short HighData = (short)audioBuffer[2 * i + 1];
                if (audioBuffer[2 * i] >= 0){
                    BUFFER[i] = (short) (LowData + (HighData << 8));
                }else{
                    BUFFER[i] = (short) ((LowData - (short)0xFF00) + (HighData << 8));
                }
                total.add(BUFFER[i] / 32768.0);
            }

            // Analyze temp buffer.
            tempFloatBuffer[tempIndex%3] = totalAbsValue;
            float temp = 0.0f;
            for( int i=0; i<3; ++i )
                temp += tempFloatBuffer[i];
            Log.d(TAG, "temp:" + temp);
            if( (temp >=0 && temp <= 600) && recording == false )
            {
                Log.i("TAG", "1");
                tempIndex++;
                continue;
            }
            if( temp > 600 && recording == false )
            {
                Log.i("TAG", "2");
                recording = true;
            }
            if( (temp >= 0 && temp <= 600) && recording == true )
            {
                Log.i("TAG", "final run");
                tempIndex++;
                audioRecorder.stop();
                break;
            }
        }
        Double [] finaldata = new Double[total.size()];
        total.toArray(finaldata);
        testtry.add(total);

        /* Gccphat Implement*/
        // 2 channel

        int total_length = testtry.get(0).size();
        ArrayList<Double> testdata = testtry.get(0);

        // get shortdata 0.5sec audio
        int frame = 0;
        if (total_length / 2 > 44100*2*1){
            frame = 44100;
        }
        else{
            frame = total_length;
        }

        double[] shortdata = new double[frame];
        for (int i = 0; i < frame; i++){
            shortdata[i] = testdata.get(total_length - frame + i);
        }

        double[] leftData = new double[frame / 2];
        double[] rightData = new double[frame / 2];
        double sum1 = 0;
        double sum2 = 0;

        // PCM-16bits Left, Right
        for (int i = 0; i < frame / 2; i++) {
            leftData[i] = shortdata[2*i];
            sum1 = sum1 + Math.abs(leftData[i]);

            rightData[i] = shortdata[2*i + 1];
            sum2 = sum2 + Math.abs(rightData[i]);
        }
        SUM[0] = sum1;
        SUM[1] = sum2;

        Gccphat correlation = new Gccphat(leftData, rightData);

        double latency = correlation.getLag();
        System.out.println("latency：" + latency);
        // Define right or left delay time
        if (latency >= correlation.getLen() / 2){
            latency = latency - correlation.getLen();
            if (latency <= -12 ){
                latency = -11.67f;
            }
        }
        else if(latency >= 12 ){
            latency = 11.67f;
        }
        double tau = latency / RECORDER_SAMPLERATE;

        float angle = (float)(Math.acos(tau * 340 / 0.09) * 180 / Math.PI) - 90;
        Log.d(TAG, "angle：" + angle);
        return_Data[0] = latency;
        return_Data[1] = angle;
        return_Data[2] = correlation.RealPosition();
        return_Data[3] = correlation.getLen();

        String tmpDegree = Float.toString(angle);
        txv_degree.setText(tmpDegree);
        if (usbService != null) {
            Log.d(TAG, "讀取度數成功, usbService != null");
            usbService.write(tmpDegree.getBytes());
        }else {
            txv_usb.setText("並無傳送");
        }
    }
}
