package com.example.tony.smarthelper.Main;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.example.tony.smarthelper.R;
import com.example.tony.smarthelper.Recog.RecogResult;
import com.example.tony.smarthelper.Util.Logger;
import com.example.tony.smarthelper.Util.Synthesizer;
import com.example.tony.smarthelper.WakeUp.WakeUpResult;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import taobe.tec.jcc.JChineseConvertor;

public class WhatsAppActivity extends AppCompatActivity implements EventListener{
    private static final String TAG = "WhatsAppActivity";


    private static boolean isInited_recog = false;
    private int step = 1;
    private static boolean back_wakeup = false;
    private EventManager asr;

    private Context context = WhatsAppActivity.this;
    Synthesizer synthesizer;

    // contact information
    HashMap<String, String> contactInfo;

    // string constant
    final static String CONTACT_WHO = "傳送訊息給誰?";
    final static String CONTENT = "訊息內容?";
    final static String SPEECH_NO_CONTENT = "查無此人";
    final static String SPEECH_NOT_UNDERSTAND_COMMAND = "沒有聽懂!請再說一遍..";

    // variables
    private String realChinese="";
    protected String currentJson;
    private String finalOutput = "";

    private String deliever_name="";
    private String deliever_content="";

    // widget
    private TextView txv_answer, txv_conversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whats_app);
        Log.d(TAG, "onCreate...");



        initView();
        // 語音合成
        synthesizer = new Synthesizer(WhatsAppActivity.this);
        synthesizer.initTTs();
        // get contact data
        contactData();
    }

    private void initView() {
        txv_answer = (TextView) findViewById(R.id.txv_answer);
        txv_conversation = (TextView) findViewById(R.id.txv_conversation);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(!back_wakeup){
            step = 1;
            stepDecision();
        }
    }

    private void stepDecision(){
        if(step == 1){
            synthesizer.speak(CONTACT_WHO);
            txv_conversation.setText(CONTACT_WHO);
            try{
                // delay 1 second
                Thread.sleep(1000);
            } catch(InterruptedException e){
                e.printStackTrace();
            }
            recog();
        }else if(step == 2){
            synthesizer.speak(CONTENT);
            txv_conversation.setText(CONTENT);
            try{
                // delay 1 second
                Thread.sleep(1000);
            } catch(InterruptedException e){
                e.printStackTrace();
            }
            recog();
        }else{
            PackageManager packageManager = getPackageManager();
            Intent i = new Intent(Intent.ACTION_VIEW);
            back_wakeup = true;
            try {
                String url = "https://api.whatsapp.com/send?phone="+ deliever_name +"&text=" + URLEncoder.encode(deliever_content, "UTF-8");
                i.setPackage("com.whatsapp");
                i.setData(Uri.parse(url));
                if (i.resolveActivity(packageManager) != null) {
                    endRecog();
                    startActivity(i);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
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
        params.put(SpeechConstant.PID,"1737");
        String json = new JSONObject(params).toString();
        Logger.info(TAG + ".Debug", "asr params(識別參數，反饋請帶上此行日誌):" + json);
        asr.send(SpeechConstant.ASR_START, json, null, 0, 0);
        asr.registerListener(this);
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
        asr.unregisterListener(this);
        asr = null;
        isInited_recog = false;
    }

    // callback function of asr
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

        } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
            Log.d(TAG, "CALLBACK_EVENT_ASR_PARTIAL");
            RecogResult recogResult = RecogResult.parseJson(params);
            if (recogResult.isFinalResult()) {

                Log.d(TAG, "最後結果！");
                Log.d(TAG, "最後文字顯示:" + recogResult.getResultsRecognition()[0]);
                finalOutput = recogResult.getResultsRecognition()[0];
            } else if (recogResult.isPartialResult()) {
                Log.d(TAG, "識別部分結果！");
                Log.d(TAG, "文字顯示:" + recogResult.getResultsRecognition()[0]);
                //finalOutput = recogResult.getResultsRecognition()[0];
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
                stepDecision();
            } else {
                Log.d(TAG, "Ans:" + finalOutput);
                realChinese = finalOutput;
                // 繁體中文 converter
                /*
                JChineseConvertor jChineseConvertor = null;
                try {
                    jChineseConvertor = JChineseConvertor.getInstance();
                    realChinese = jChineseConvertor.s2t(finalOutput);
                    Log.d(TAG, "繁體中文: " + realChinese);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                */
                // delay
                if(!realChinese.isEmpty()){
                    Log.d(TAG, "realChinese:" + realChinese);
                    if (step == 1){
                        // check if contact exist
                        String contact_name = contactInfo.get(realChinese);
                        if (contact_name != null) {
                            Log.d(TAG, "contact name:" + contact_name);
                            deliever_name = contact_name; // id
                            txv_answer.setText(realChinese);
                            step += 1;
                        }else{
                            Log.d(TAG, "nobody");
                            synthesizer.speak(SPEECH_NO_CONTENT);
                            try{
                                // delay 1 second
                                Thread.sleep(1000);
                            } catch(InterruptedException e){
                                e.printStackTrace();
                            }
                        }
                    }else if (step == 2){
                        deliever_content = realChinese;
                        txv_answer.setText(realChinese);
                        step += 1;
                    }
                }
                endRecog();
                stepDecision();
            }
        } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_LONG_SPEECH)) {
            Log.d(TAG, "CALLBACK_EVENT_ASR_LONG_SPEECH");
        } else if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_EXIT)) {
            Log.d(TAG, "CALLBACK_EVENT_ASR_EXIT");
        }
    }

    // Retrieving Contact data / in WhatsApp
    private void contactData(){
        Log.d(TAG, "contactData.");
        //This class provides applications access to the content model.
        ContentResolver cr = context.getContentResolver();

        //RowContacts for filter Account Types
        Cursor contactCursor = cr.query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID,
                        ContactsContract.RawContacts.CONTACT_ID},
                ContactsContract.RawContacts.ACCOUNT_TYPE + "= ?",
                new String[]{"com.whatsapp"},
                null);

        //ArrayList for Store Whatsapp Contact/Name
        ArrayList<String> myWhatsappContacts = new ArrayList<>();
        ArrayList<String> myWhatsappName = new ArrayList<>();

        contactInfo = new HashMap<>();



        if (contactCursor != null) {
            if (contactCursor.getCount() > 0) {
                if (contactCursor.moveToFirst()) {
                    do {
                        //whatsappContactId for get Number,Name,Id ect... from  ContactsContract.CommonDataKinds.Phone
                        String whatsappContactId = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));

                        if (whatsappContactId != null) {
                            //Get Data from ContactsContract.CommonDataKinds.Phone of Specific CONTACT_ID
                            Cursor whatsAppContactCursor = cr.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    new String[]{whatsappContactId}, null);

                            if (whatsAppContactCursor != null) {
                                whatsAppContactCursor.moveToFirst();
                                String id = whatsAppContactCursor.getString(whatsAppContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                                String name = whatsAppContactCursor.getString(whatsAppContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                String number = whatsAppContactCursor.getString(whatsAppContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                                number = number.substring(1);
                                number = number.replaceAll("\\s+", "");
                                number = "+886 " + number;
                                whatsAppContactCursor.close();

                                //Add Number to ArrayList
                                myWhatsappContacts.add(number);
                                myWhatsappName.add(name);

                                contactInfo.put(name, number);
                                Log.d(TAG, " WhatsApp contact id  :  " + id);
                                Log.d(TAG, " WhatsApp contact name :  " + name);
                                Log.d(TAG, " WhatsApp contact number :  " + number);
                            }
                        }
                    } while (contactCursor.moveToNext());
                    contactCursor.close();
                }
            }
        }
        Log.d(TAG, " WhatsApp contact size :  " + myWhatsappContacts.size());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        if (back_wakeup){
            back_wakeup = false;
            Intent intent = new Intent(WhatsAppActivity.this, WakeUpActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

    }
}
