package com.example.tony.smarthelper.Main;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tony.smarthelper.R;
import com.example.tony.smarthelper.WhatsappAccessibilityService;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MainActivity";

    // widget
    private Button btn_start, btn_test;

    Context context = MainActivity.this;

    // permissions
    String[] permissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,

            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.WRITE_SETTINGS,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CLEAR_APP_CACHE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.GET_TASKS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate.");
        initView();


        if (!isAccessibilityOn (context, WhatsappAccessibilityService.class)) {
            Log.d(TAG, "Navigate to Accessibility.");
            Intent intent = new Intent (Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity (intent);
        }
    }
    private void initView(){
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_start.setOnClickListener(this);

        btn_test = (Button) findViewById(R.id.btn_test);
        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactData();
                Log.d(TAG, "Test whatsapp.");

                PackageManager packageManager = getPackageManager();
                Intent i = new Intent(Intent.ACTION_VIEW);
                String message = "send text";
                String phone = "+886 958761491"; // https://api.whatsapp.com/send?phone=+886%20958761491&text=
                try {
                    String url = "https://api.whatsapp.com/send?phone="+ phone +"&text=" + URLEncoder.encode(message, "UTF-8");
                    i.setPackage("com.whatsapp");
                    i.setData(Uri.parse(url));
                    if (i.resolveActivity(packageManager) != null) {
                        startActivity(i);
                        Log.d(TAG, "can click the button.");
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
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

        HashMap<String, String> contactInfo;
        contactInfo = new HashMap<String, String>();



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
    public void onClick(View v) {
        Log.d(TAG, "press start button.");
        Intent intent = new Intent(MainActivity.this, WakeUpActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        permissions();
    }

    private void permissions() {
        Log.d(TAG, "permissions.");
        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }
    }


    // check the Accessibility
    private boolean isAccessibilityOn (Context context, Class<? extends AccessibilityService> clazz) {
        Log.d(TAG, "check the Accessibility");
        int accessibilityEnabled = 0;
        final String service = context.getPackageName () + "/" + clazz.getCanonicalName ();
        Log.d(TAG, "String service: " + service);
        try {
            accessibilityEnabled = Settings.Secure.getInt (context.getApplicationContext ().getContentResolver (), Settings.Secure.ACCESSIBILITY_ENABLED, 0);
            Log.d(TAG, "accessibilityEnabled:" + accessibilityEnabled);
        } catch (Exception e) {
            Log.e(TAG, "get accessibility enable failed, the err:" + e.getMessage());
        }
        Log.d(TAG, "debug for accessibility.");

        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter (':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString (context.getApplicationContext ().getContentResolver (), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue == null)
                Log.d(TAG, "settingValue == null");
            if (settingValue != null) {
                Log.d(TAG, "settingValue != null");
                colonSplitter.setString (settingValue);

                Log.d(TAG, "colonSplitter:" + colonSplitter);
                Log.d(TAG, "settingValue:" + settingValue);

                while (colonSplitter.hasNext ()) {
                    String accessibilityService = colonSplitter.next ();
                    Log.d(TAG, "while loop.");
                    Log.d(TAG, "accessibilityService:" + accessibilityService);
                    if (accessibilityService.equalsIgnoreCase (service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        }else{
            Log.d(TAG,"Accessibility service disable");
        }
        return false;
    }
}
