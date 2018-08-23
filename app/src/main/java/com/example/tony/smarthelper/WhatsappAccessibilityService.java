package com.example.tony.smarthelper;

import android.accessibilityservice.AccessibilityService;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class WhatsappAccessibilityService extends AccessibilityService {
    private static final String TAG = "WhatsappAccessibilitySe";

    @Override
    protected void onServiceConnected() {
        Log.d(TAG, "onServiceConnected.");
        super.onServiceConnected();
    }

    @Override
    public void onAccessibilityEvent (AccessibilityEvent event) {
        if (getRootInActiveWindow () == null) {
            return;
        }

        AccessibilityNodeInfoCompat rootInActiveWindow = AccessibilityNodeInfoCompat.wrap (getRootInActiveWindow ());

        // Whatsapp Message EditText id
        List<AccessibilityNodeInfoCompat> messageNodeList = rootInActiveWindow.findAccessibilityNodeInfosByViewId ("com.whatsapp:id/entry");
        if (messageNodeList == null || messageNodeList.isEmpty ()) {
            return;
        }
        // Whatsapp send button id
        List<AccessibilityNodeInfoCompat> sendMessageNodeInfoList = rootInActiveWindow.findAccessibilityNodeInfosByViewId ("com.whatsapp:id/send");
        if (sendMessageNodeInfoList == null || sendMessageNodeInfoList.isEmpty ()) {
            return;
        }

        AccessibilityNodeInfoCompat sendMessageButton = sendMessageNodeInfoList.get (0);
        if (!sendMessageButton.isVisibleToUser ()) {
            return;
        }

        // Now fire a click on the send button
        sendMessageButton.performAction (AccessibilityNodeInfo.ACTION_CLICK);

        // Now go back to your app by clicking on the Android back button twice:
        // First one to leave the conversation screen
        // Second one to leave whatsapp
        try {
            Thread.sleep (500); // hack for certain devices in which the immediate back click is too fast to handle
            performGlobalAction (GLOBAL_ACTION_BACK);
            Thread.sleep (500);  // same hack as above
        } catch (InterruptedException ignored) {}
        performGlobalAction (GLOBAL_ACTION_BACK);
    }

    @Override
    public void onInterrupt() {

    }
}
