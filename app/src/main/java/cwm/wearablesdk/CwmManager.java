package cwm.wearablesdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import cwm.wearablesdk.constants.ID;
import cwm.wearablesdk.constants.Type;
import cwm.wearablesdk.events.AckEvents;
import cwm.wearablesdk.events.CwmEvents;
import cwm.wearablesdk.events.ErrorEvents;
import cwm.wearablesdk.handler.BleReceiver;
import cwm.wearablesdk.settings.UserConfig;

/**
 * Created by user on 2017/8/31.
 */

public class CwmManager{

    private String TAG = "CwmManager";
    private String SDK_VERSION = "Hoin-V1.0";
    private WearableService mService = null;
    private Activity mActivity = null;
    private WearableServiceListener mStatusListener = null;
    private ErrorListener mErrorListener = null;
    private EventListener mListener = null;
    private AckListener mAckListener = null;
    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_SELECT_DEVICE = 2;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothManager mBluetoothManager = null;
    private boolean mConnectStatus = false;
    private JniManager jniMgr; //JNI
    private BleReceiver mBleReceiver = new BleReceiver(this);

    /*********Upadet BitMap************/
    private byte[] mapArray;
    public  long bitMapLength = 0;
    public static boolean oledAccomplish = false;
    public static boolean bitMapAccomplish = false;
    public static boolean fontLitAccomplish = false;
    public static int endPos = 0x1000;
    public static int currentMapSize = 0;
    public static int mapSize = 0;
    /*************************************/

    public static ReentrantLock lock = new ReentrantLock();
    // interface -----------------------------------------------------------------------------------
    public interface EventListener {
        void onEventArrival(CwmEvents cwmEvents);
    }
    public interface AckListener{
        void onAckArrival(AckEvents ackEvents);
    }
    public interface WearableServiceListener {
        void onConnected();
        void onDisconnected();
        void onServiceDiscovery(String deviceName, String deviceAddress);
        void onNotSupport();
        void onUnknownProblem();
    }
    public interface ErrorListener{
        void onErrorArrival(ErrorEvents errorEvents);
    }

    public CwmManager(Activity activity, WearableServiceListener wListener,
                      EventListener iLlistener, AckListener ackListener, ErrorListener errorListener){

        mActivity = activity;
        mStatusListener = wListener;
        mListener = iLlistener;
        mAckListener = ackListener;
        mErrorListener = errorListener;

       // endPos = 0x1000;
       // currentMapSize = 0;

        jniMgr = new JniManager();

        systemBluetoothCheck();

        // bind to wearableService
        Intent bindIntent = new Intent(mActivity, cwm.wearablesdk.WearableService.class);
        mActivity.bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private void systemBluetoothCheck(){

        if (!mActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(mActivity,"This hardware is not supporting BLE", Toast.LENGTH_LONG).show();
            mActivity.finish();
        }

        mBluetoothManager = (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            new AlertDialog.Builder(mActivity)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Warning")
                    .setMessage("your ble is not supported")
                    .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mActivity.finish();
                        }
                    })
                    .setNegativeButton("no", null)
                    .show();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    // Intent Filter -------------------------------------------------------------------------------
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WearableService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(WearableService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(WearableService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(WearableService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(WearableService.APK_DOES_NOT_SUPPORT_WEARABLE);
        intentFilter.addAction(WearableService.MCU_HAS_UNKNOWN_PROBLEM);
        return intentFilter;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((WearableService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
        }
    };

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final String deviceName = intent.getStringExtra("DeviceName");
            final String deviceAddress = intent.getStringExtra("DeviceAddress");

            if (action.equals(cwm.wearablesdk.WearableService.ACTION_GATT_CONNECTED)) {
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        mConnectStatus = true;
                        mStatusListener.onConnected();
                    }
                });
            }
            if (action.equals(WearableService.ACTION_GATT_DISCONNECTED)) {
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        mConnectStatus = false;
                        mService.close();
                        mStatusListener.onDisconnected();
                    }
                });
            }
            if (action.equals(WearableService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mConnectStatus = true;
                if(mService.enableTXNotification())
                   mStatusListener.onServiceDiscovery(deviceName, deviceAddress);
            }
            //*********************//
            if (action.equals(WearableService.ACTION_DATA_AVAILABLE)) {

                final byte[] rxBuffer = intent.getByteArrayExtra(WearableService.EXTRA_DATA);
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            mBleReceiver.receiveRawByte(rxBuffer);
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }
            //*********************//
            if (action.equals(WearableService.APK_DOES_NOT_SUPPORT_WEARABLE)){
                mConnectStatus = false;
                mService.disconnect();
                mStatusListener.onNotSupport();
            }

            if(action.equals(WearableService.MCU_HAS_UNKNOWN_PROBLEM)){
                mConnectStatus = false;
                mService.disconnect();
                mStatusListener.onUnknownProblem();
            }
        }
    };

    public JniManager getJniManager(){
        return jniMgr;
    }
    public EventListener getListener(){return mListener;}
    public AckListener getAckListener(){return mAckListener;}
    public ErrorListener getErrorListener(){return mErrorListener;}

    public boolean CwmBleStatus(){
        if(mBluetoothAdapter.isEnabled())
          return true;
        else
            return false;
    }
    public void CwmReleaseResource(){
        if(mActivity != null) {
            LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(UARTStatusChangeReceiver);
            mActivity.unbindService(mServiceConnection);
            Task.taskReceivedHandler.removeCallbacks(Task.currentTask);
            mActivity = null;
            mStatusListener = null;
            mListener = null;
            mAckListener = null;
            mErrorListener = null;
        }
    }
    public void CwmBleSearch() {
        if(mBluetoothAdapter.isEnabled()){
            Intent newIntent = new Intent(mActivity, DeviceListActivity.class);
            mActivity.startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
        }
        else{
            Toast.makeText(mActivity,"Your bluetooth is not enabled",Toast.LENGTH_LONG).show();
        }
    }
    public void CwmBleConnect(String address){
        mService.connect(address);
    }
    public void CwmBleDisconnect(){
        mService.disconnect();
    }
    public void CwmBleClose(){
        mService.close();
    }
    public void CwmNotification(NotificationData data){
        if(lock.tryLock()) {
            int j;
            int msgId = data.getNotifyId();
            int identifier;
            String personName;
            String appName;
            byte[] payload;
            switch (msgId) {
                case ID.INCOMING_CALL:
                    personName = data.getPersoneName();
                    Log.d("bernie","sdk person name:"+personName);
                    if(personName.getBytes().length == personName.length()){
                        //english
                        payload = new byte[1 + 1 + 32]; //type + id+ (name or number)
                        payload[0] = (byte) 0x86;
                        payload[1] = (byte) ID.INCOMING_CALL;
                        j = 2;
                        if(personName.length() <= 32) {
                            for (int i = 0; i < personName.length(); i++) {
                                payload[j] = (byte) personName.charAt(i);
                                j++;
                            }
                        }
                        else{
                            for (int i = 0; i < 32; i++) {
                                payload[j] = (byte) personName.charAt(i);
                                j++;
                            }
                        }
                        splitCommand(payload);
                        Log.d("bernie","sdk calling notify is english");
                    }
                    else{
                        //not english
                        try {
                            byte[] name = personName.getBytes("UTF-8");
                            payload = new byte[1 + 1 + 32 ]; //message type + message id + (name or number)
                            payload[0] = (byte) 0x86;
                            payload[1] = (byte) ID.INCOMING_CALL;
                            j = 2;
                            if(name.length <= 32) {
                                for (int i = 0; i < name.length; i++) {
                                    payload[j] = name[i];
                                    j++;
                                }
                            }
                            else{
                                for (int i = 0; i < 32; i++) {
                                    payload[j] = name[i];
                                    j++;
                                }
                            }
                            splitCommand(payload);
                            Log.d("bernie","sdk calling notify not english");
                        }catch (IOException e){
                        }
                    }
                    break;
                case ID.SOCIAL:
                    identifier = data.getAppIdentifier();
                    appName = data.getAppName();
                    payload = new byte[1 + 1 + 1 + 16]; //message type + message id + app identifier + app name length
                    payload[0] = (byte) 0x86;
                    payload[1] = (byte) ID.SOCIAL;
                    switch (identifier){
                        case Type.QQ_MESSAGE:
                            payload[2] = (byte) Type.QQ_MESSAGE;
                            break;
                        case Type.WECHART_MESSAGE:
                            payload[2] = (byte) Type.WECHART_MESSAGE;
                            break;
                        case Type.DOBAN_MESSAGE:
                            payload[2] = (byte) Type.DOBAN_MESSAGE;
                            break;
                        case Type.OTHER:
                            payload[2] = (byte) Type.OTHER;
                            break;
                    }
                    if(appName.getBytes().length == appName.length()){ // is english
                        j = 3;
                        if(appName.length() <= 16) {
                            for (int i = 0; i < appName.length(); i++) {
                                payload[j] = (byte) appName.charAt(i);
                                j++;
                            }
                        }
                        else{
                            for (int i = 0; i < 16; i++) {
                                payload[j] = (byte) appName.charAt(i);
                                j++;
                            }
                        }
                        splitCommand(payload);
                    }
                    else{ // is not english
                        try {
                            byte[] name = appName.getBytes("UTF-8");
                            j = 3;
                            if(name.length <= 16) {
                                for (int i = 0; i < name.length; i++) {
                                    payload[j] = name[i];
                                    j++;
                                }
                            }
                            else{
                                for (int i = 0; i < 16; i++) {
                                    payload[j] = name[i];
                                    j++;
                                }
                            }
                            splitCommand(payload);
                        }
                        catch (IOException e){

                        }
                    }
                    break;
                case ID.EMAIL:
                    payload = new byte[2]; //message type + message id
                    payload[0] = (byte) 0x86;
                    payload[1] = (byte) ID.EMAIL;
                    splitCommand(payload);
                    break;
                case ID.NEWS:
                    payload = new byte[2]; //message type + message id
                    payload[0] = (byte) 0x86;
                    payload[1] = (byte) ID.NEWS;
                    splitCommand(payload);
                    break;
                case ID.MISSING_CALL:
                    payload = new byte[2]; //message type + message id
                    payload[0] = (byte) 0x86;
                    payload[1] = (byte) ID.MISSING_CALL;
                    splitCommand(payload);
                    Log.d("bernie","sdk missing call");
                    break;
                case ID.PICK_UP:
                    payload = new byte[2]; //message type + message id
                    payload[0] = (byte) 0x86;
                    payload[1] = (byte) ID.PICK_UP;
                    splitCommand(payload);
                    Log.d("bernie","sdk pick up call");
                    break;
                default:
                    break;
            }
            lock.unlock();
        }
    }
    public void CwmRequestUserConfig(){
        if(lock.tryLock()) {
            if(BleReceiver.hasLongTask == false) {
                byte[] payload = new byte[4];

                payload[0] = (byte) 0x81; // command type
                payload[1] = (byte) 0x04; // command id

                splitCommand(payload);
            }
            lock.unlock();
        }
    }
    public void CwmResetUserConfig(){
        if(lock.tryLock()) {
            byte[] payload = new byte[4];

            payload[0] = (byte) 0x81; // command type
            payload[1] = (byte) ID.RESET_USERCONFIG; // command id

            splitCommand(payload);

            lock.unlock();
        }
    }
    public void CwmSendUserConfig(UserConfig userConfig) {
        int osType = userConfig.getSystemSetting().getOsType();
        int timeFormat = userConfig.getSystemSetting().getTimeFormat();
        int historyDetect = userConfig.getSystemSetting().getHistoryDetectPeriod();
        int screenTimout = userConfig.getSystemSetting().getScreenTimeOut();
        int screens = userConfig.getSystemSetting().getScreens();
        int functions = userConfig.getSystemSetting().getFunctions();
        int noDisturbStart = userConfig.getSystemSetting().getNoDisturbStart();
        int noDisturbStop = userConfig.getSystemSetting().getNoDisturbStop();
        int sleepStartTime = userConfig.getSystemSetting().getSleepStart();
        int sleepStopTime = userConfig.getSystemSetting().getSleepStop();
        int brightness = userConfig.getSystemSetting().getBrightness();

        int screens_I = 0;
        int screens_II = 0;

        int functions_I = 0;
        int functions_II = 0;

        long unixTime = 0;
        int unixTime_I;
        int unixTime_II;
        int unixTime_III;
        int unixTime_IV;
        int j = 0;

        // Person Profile
        int gender = userConfig.getBodySetting().getSex();
        int age = userConfig.getBodySetting().getOld();
        int height = userConfig.getBodySetting().getHight();
        int weight = userConfig.getBodySetting().getWeight();

        //Intelligent settings
        boolean[] gestureList = userConfig.getIntelligentSetting().getGesture();
        int sedentaryTime = userConfig.getIntelligentSetting().getTime();
        int targetStep = userConfig.getIntelligentSetting().getGoal();

        int gestureConfig = 0;
        int gestureCongig_L = 0;
        int gestureConfig_H = 0;
        int targetStep_I = 0;
        int targetStep_II = 0;
        int targetStep_III = 0;
        int targetStep_IV = 0;
        int checksum = 0;

        //Alarm settings
        int hour1 = userConfig.getAlarmSetting().getHour(1);
        int hour2 = userConfig.getAlarmSetting().getHour(2);
        int hour3 = userConfig.getAlarmSetting().getHour(3);
        int hour4 = userConfig.getAlarmSetting().getHour(4);
        int hour5 = userConfig.getAlarmSetting().getHour(5);
        int hour6 = userConfig.getAlarmSetting().getHour(6);
        int minute1 = userConfig.getAlarmSetting().getMinute(1);
        int minute2 = userConfig.getAlarmSetting().getMinute(2);
        int minute3 = userConfig.getAlarmSetting().getMinute(3);
        int minute4 = userConfig.getAlarmSetting().getMinute(4);
        int minute5 = userConfig.getAlarmSetting().getMinute(5);
        int minute6 = userConfig.getAlarmSetting().getMinute(6);
        int vibrate1 = userConfig.getAlarmSetting().getVibrat(1);
        int vibrate2 = userConfig.getAlarmSetting().getVibrat(2);
        int vibrate3 = userConfig.getAlarmSetting().getVibrat(3);
        int vibrate4 = userConfig.getAlarmSetting().getVibrat(4);
        int vibrate5 = userConfig.getAlarmSetting().getVibrat(5);
        int vibrate6 = userConfig.getAlarmSetting().getVibrat(6);
        int week1 = userConfig.getAlarmSetting().getWeek(1);
        int week2 = userConfig.getAlarmSetting().getWeek(2);
        int week3 = userConfig.getAlarmSetting().getWeek(3);
        int week4 = userConfig.getAlarmSetting().getWeek(4);
        int week5 = userConfig.getAlarmSetting().getWeek(5);
        int week6 = userConfig.getAlarmSetting().getWeek(6);

        if(gestureList[Type.GESTURE.SIGNIFICANT_MOTION.ordinal()]){
            gestureConfig = gestureConfig | 4;
        }
        else
            gestureConfig = gestureConfig & ~4;

        if(gestureList[Type.GESTURE.HAND_UP.ordinal()]){
            gestureConfig = gestureConfig | 8;
        }
        else
            gestureConfig = gestureConfig & ~8;

        if(gestureList[Type.GESTURE.TAP.ordinal()]){
            gestureConfig = gestureConfig | 16;
        }
        else
            gestureConfig = gestureConfig & ~16;

        if(gestureList[Type.GESTURE.WATCH_TAKE_OFF.ordinal()]){
            gestureConfig = gestureConfig | 32;
        }
        else
            gestureConfig = gestureConfig & ~32;

        if(gestureList[Type.GESTURE.SEDENTARY.ordinal()]){
            gestureConfig = gestureConfig | 256;
        }
        else
            gestureConfig = gestureConfig & ~256;

        if(gestureList[Type.GESTURE.WRIST_SCROLL.ordinal()]){
            gestureConfig = gestureConfig | 512;
        }
        else
            gestureConfig = gestureConfig & ~512;

        if(gestureList[Type.GESTURE.SHAKE.ordinal()]){
            gestureConfig = gestureConfig | 1024;
        }
        else
            gestureConfig = gestureConfig & ~1024;

        gestureCongig_L  = gestureConfig & 0xFF;
        gestureConfig_H = (gestureConfig >> 8) & 0xFF;

        byte[] config1 = new byte[20];
        byte[] config2 = new byte[20];
        byte[] config3 = new byte[20];
        byte[] config4 = new byte[20];
        byte[] config5 = new byte[3];

        byte[] config6 = new byte[75];

        config6[0] = (byte)0x81;
        config6[1] = (byte)0x01;
        config6[2] = (byte)0x00;
        config6[3] = (byte)0x00;
        config6[4] = (byte)0x00;
        config6[5] = (byte)0x00;
        config6[6] = (byte)osType;
        config6[7] = (byte)timeFormat;
        config6[8] = (byte)historyDetect;
        config6[9] = (byte)screenTimout;

        screens_I = screens & 0xFF;
        screens_II = (screens >> 8) & 0xFF;

        config6[10] = (byte)screens_I;
        config6[11] = (byte)screens_II;

        functions_I = functions & 0xFF;
        functions_II = (functions >> 8) & 0xFF;

        config6[12] = (byte)functions_I;
        config6[13] = (byte)functions_II;
        config6[14] = (byte)gestureCongig_L;
        config6[15] = (byte)gestureConfig_H;
        config6[16] = (byte)0xFF;
        config6[17] = (byte)0xFF;

        Date date = new Date();
        date.setHours(date.getHours()+8);
        unixTime = date.getTime()/1000;

        unixTime_I = ((int)unixTime & 0xFF);
        unixTime_II = ((int)unixTime >> 8 & 0xFF);
        unixTime_III = ((int)unixTime >> 16 & 0xFF);
        unixTime_IV = ((int)unixTime >> 24 & 0xFF);

        config6[18] = (byte)unixTime_I;
        config6[19] = (byte)unixTime_II;
        config6[20] = (byte)unixTime_III;
        config6[21] = (byte)unixTime_IV;
        config6[22] = (byte)0x00;
        config6[23] = (byte)0x00;
        config6[24] = (byte)0x00;
        config6[25] = (byte)0x00;
        config6[26] = (byte)gender;
        config6[27] = (byte)age;
        config6[28] = (byte)height;
        config6[29] = (byte)weight;

        targetStep_I  = targetStep & 0xFF;
        targetStep_II = (targetStep >> 8) & 0xFF;
        targetStep_III = (targetStep >> 16) & 0xFF;
        targetStep_IV = (targetStep_IV >> 24) & 0xFF;

        config6[30] = (byte)targetStep_I;
        config6[31] = (byte)targetStep_II;
        config6[32] = (byte)targetStep_III;
        config6[33] = (byte)targetStep_IV;
        config6[34] = (byte)0x00;
        config6[35] = (byte)0x00;
        config6[36] = (byte)0x00;
        config6[37] = (byte)0x00;
        config6[38] = (byte)0x00;
        config6[39] = (byte)0x00;
        config6[40] = (byte)0x00;
        config6[41] = (byte)0x00;
        config6[42] = (byte)week1;
        config6[43] = (byte)vibrate1;
        config6[44] = (byte)hour1;
        config6[45] = (byte)minute1;
        config6[46] = (byte)week2;
        config6[47] = (byte)vibrate2;
        config6[48] = (byte)hour2;
        config6[49] = (byte)minute2;
        config6[50] = (byte)week3;
        config6[51] = (byte)vibrate3;
        config6[52] = (byte)hour3;
        config6[53] = (byte)minute3;
        config6[54] = (byte)week4;
        config6[55] = (byte)vibrate4;
        config6[56] = (byte)hour4;
        config6[57] = (byte)minute4;
        config6[58] = (byte)week5;
        config6[59] = (byte)vibrate5;
        config6[60] = (byte)hour5;
        config6[61] = (byte)minute5;
        config6[62] = (byte)week6;
        config6[63] = (byte)vibrate6;
        config6[64] = (byte)hour6;
        config6[65] = (byte)minute6;
        config6[66] = (byte)sleepStartTime;
        config6[67] = (byte)sleepStopTime;
        config6[68] = (byte)0x00;
        config6[69] = (byte)0x00;
        config6[70] = (byte)sedentaryTime;
        config6[71] = (byte)noDisturbStart;
        config6[72] = (byte)noDisturbStop;
        config6[73] = (byte)0x00;
        config6[74] = (byte)brightness;

        splitCommand(config6);



        /*config1[0] = (byte)0xE7;
        config1[1] = (byte)0x4E; // length I
        config1[2] = (byte)0x00; // length II

        config1[3] = (byte)0x81;
        config1[4] = (byte)0x01;

        config1[5] = (byte)0x00; //Reserved 0
        config1[6] = (byte)0x00; //Reserved 1
        config1[7] = (byte)0x00; //Reserved 2
        config1[8] = (byte)0x00; //Reserved 3
        //system
        config1[9] = (byte)osType; //OS type 0: Android 1: iOS 4
        config1[10] = (byte)timeFormat; //Time format 0: 0~23 1: 1~12 5
        config1[11] = (byte)historyDetect; //History detect period 10 min 6
        config1[12] = (byte)screenTimout; //Screen time out 7

        screens_I = screens & 0xFF;
        screens_II = (screens >> 8) & 0xFF;

        config1[13] = (byte)screens_I; //Message screen I  8
        config1[14] = (byte)screens_II; //Message screen II

        functions_I = functions & 0xFF;
        functions_II = (functions >> 8) & 0xFF;

        config1[15] = (byte)functions_I; //Function switch I
        config1[16] = (byte)functions_II; //Function swtich II
        config1[17] = (byte)gestureCongig_L; //Gesture switch I
        config1[18] = (byte)gestureConfig_H; //Gesture switch II
        config1[19] = (byte)0xFF; //Gesture switch III

        config2[0] = (byte)0xE8;
        config2[1] = (byte)0xFF; //Gesture IV

        Date date = new Date();
        date.setHours(date.getHours()+8);
        unixTime = date.getTime()/1000;

        unixTime_I = ((int)unixTime & 0xFF);
        unixTime_II = ((int)unixTime >> 8 & 0xFF);
        unixTime_III = ((int)unixTime >> 16 & 0xFF);
        unixTime_IV = ((int)unixTime >> 24 & 0xFF);

        config2[2] = (byte)unixTime_I; //OS time I
        config2[3] = (byte)unixTime_II; //OS time II
        config2[4] = (byte)unixTime_III; //OS time III
        config2[5] = (byte)unixTime_IV; //OS time IV;
        config2[6] = (byte)0x00; //Reserved
        config2[7] = (byte)0x00; //Reserved
        config2[8] = (byte)0x00; //Reserved
        config2[9] = (byte)0x00; //Reserved
        config2[10] = (byte)gender;
        config2[11] = (byte)age;
        config2[12] = (byte)height;
        config2[13] = (byte)weight;

        targetStep_I  = targetStep & 0xFF;
        targetStep_II = (targetStep >> 8) & 0xFF;
        targetStep_III = (targetStep >> 16) & 0xFF;
        targetStep_IV = (targetStep_IV >> 24) & 0xFF;

        config2[14] = (byte) targetStep_I;
        config2[15] = (byte) targetStep_II;
        config2[16] = (byte) targetStep_III;
        config2[17] = (byte) targetStep_IV;
        config2[18] = (byte)0x00; //User Profile
        config2[19] = (byte)0x00; //USer Profile

        config3[0] = (byte)0xE8;
        config3[1] = 0x00; //User Profile
        config3[2] = 0x00; //User Profile
        config3[3] = 0x00; //User Profile
        config3[4] = 0x00; //User Profile
        config3[5] = 0x00; //User Profile
        config3[6] = 0x00; //User Profile
        config3[7] = (byte)week1;
        config3[8] = (byte)vibrate1;
        config3[9] = (byte)hour1;
        config3[10] = (byte)minute1;
        config3[11] = (byte)week2;
        config3[12] = (byte)vibrate2;
        config3[13] = (byte)hour2;
        config3[14] = (byte)minute2;
        config3[15] = (byte)week3;
        config3[16] = (byte)vibrate3;
        config3[17] = (byte)hour3;
        config3[18] = (byte)minute3;
        config3[19] = (byte)week4;

        config4[0] = (byte)0xE8;
        config4[1] = (byte)vibrate4;
        config4[2] = (byte)hour4;
        config4[3] = (byte)minute4;
        config4[4] = (byte)week5;
        config4[5] = (byte)vibrate5;
        config4[6] = (byte)hour5;
        config4[7] = (byte)minute5;
        config4[8] = (byte)week6;
        config4[9] = (byte)vibrate6;
        config4[10] = (byte)hour6;
        config4[11] = (byte)minute6;
        config4[12] = (byte)sleepStartTime;
        config4[13] = (byte)sleepStopTime;
        config4[14] = (byte)0x00; //Reserved for sleep
        config4[15] = (byte)0x00; //Reserved for sleep
        config4[16] = (byte)sedentaryTime;
        config4[17] = (byte)noDisturbStart;
        config4[18] = (byte)noDisturbStop;
        config4[19] = (byte)0x00; //Reserved for sedentary

        config5[0] = (byte)0xE9;
        config5[1] = (byte)brightness;


        for(int i = 1 ; i < config1.length ; i++)
            checksum += config1[i];

        for(int i = 1 ; i < config2.length ; i++ )
            checksum += config2[i];

        for(int i = 1 ; i < config3.length ; i++)
            checksum += config3[i];

        for(int i = 1 ; i < config4.length ; i++)
            checksum += config4[i];

        for(int i = 1 ; i < config5.length - 1 ; i++)
            checksum += config5[i];


        config5[2] = (byte)checksum; //Checksum


        if (mConnectStatus != false) {
            mService.writeRXCharacteristic(config1);
            try{
                Thread.sleep(10);
            } catch(InterruptedException e){
                e.printStackTrace();
            }
            mService.writeRXCharacteristic(config2);
            try{
                Thread.sleep(10);
            } catch(InterruptedException e){
                e.printStackTrace();
            }
            mService.writeRXCharacteristic(config3);
            try{
                Thread.sleep(10);
            } catch(InterruptedException e){
                e.printStackTrace();
            }
            mService.writeRXCharacteristic(config4);
            try{
                Thread.sleep(10);
            } catch(InterruptedException e){
                e.printStackTrace();
            }
            mService.writeRXCharacteristic(config5);
        }*/

        Log.d("bernie","send user config");

    }
    public void CwmSensorReport(int sensorType){
        if(lock.tryLock()) {
            byte[] payload = new byte[4];

            payload[0] = (byte) 0x82; // command type
            payload[1] = (byte) 0x01; // command id
            payload[2] = (byte) sensorType;
            payload[3] = (byte) 0x01;

            splitCommand(payload);
        }
    }
    public void CwmFactory(int commandID, int sensor_id){
        if(lock.tryLock()) {
            if (commandID == Type.FACTORY_OPERATE.UPDATE_BITMAP.ordinal() ||
                    commandID == Type.FACTORY_OPERATE.UPDATE_FONT_LIB.ordinal() ||
                    commandID == Type.FACTORY_OPERATE.DFU.ordinal()) {
                byte[] payload = new byte[2];

                payload[0] = (byte) 0x85; // command type : Systtem information command
                payload[1] = (byte) commandID; // command id
                if (sensor_id >= 0x01 && sensor_id <= 0x08)
                    payload[2] = (byte) sensor_id;
                else
                    payload[2] = 0x01;

                splitCommand(payload);
            }
            else if(commandID == Type.FACTORY_OPERATE.SELF_TEST.ordinal() ||
                    commandID == Type.FACTORY_OPERATE.CALIBRATE.ordinal() ||
                    commandID == Type.FACTORY_OPERATE.RECORD_SENSOR_DATA.ordinal()) {
                byte[] payload = new byte[3];

                payload[0] = (byte) 0x85; // command type : Systtem information command
                payload[1] = (byte) commandID; // command id
                if (sensor_id >= 0x01 && sensor_id <= 0x08)
                    payload[2] = (byte) sensor_id;
                else
                    payload[2] = 0x01;

                splitCommand(payload);
            }
            lock.unlock();
        }
    }
    public void CwmRequestBattery(){
        if (lock.tryLock()) {
            byte[] payload = new byte[2];

            payload[0] = (byte) 0x81; // command type : Systtem information command
            payload[1] = (byte) 0x02; // command id : request battery

            splitCommand(payload);
            lock.unlock();
        }
    }
    public void CwmRequestSwVersion(){
        if(lock.tryLock()) {
            byte[] payload = new byte[2];

            payload[0] = (byte) 0x81; // command type : Systtem information command
            payload[1] = (byte) 0x03; // command id : request device version

            splitCommand(payload);
            lock.unlock();
        }
    }
    public void CwmSwitchOTA(){
        if(lock.tryLock()) {
            byte[] payload = new byte[2];

            payload[0] = (byte) 0x85; // command type : Switch OTA
            payload[1] = (byte) 0x01; // command id : request device version

            splitCommand(payload);
            lock.unlock();
        }
    }
    public void CwmTabataCommand(int operate, int prepare, int interval, int action_item){
        //if(lock.tryLock()) {

        if(operate >= Type.ITEMS.TABATA_INIT.ordinal() && operate <= Type.ITEMS.TABATA_RESUME.ordinal()) {
            if(operate == Type.ITEMS.TABATA_ACTION_ITEM.ordinal()){
                byte[] payload = new byte[4];

                payload[0] = (byte) 0x82; // Message type sensor & gesture command
                payload[1] = (byte) 0x15; // command id : TABATA response message
                payload[2] = (byte) operate; //TABATA Operateion: Action Item
                payload[3] = (byte) action_item;
                splitCommand(payload);
            }
            else{
                byte[] payload = new byte[3];

                payload[0] = (byte) 0x82; // Message type sensor & gesture command
                payload[1] = (byte) 0x15; // command id : TABATA response message
                payload[2] = (byte) operate; //TABATA Operation

                if(operate == 12)
                    Log.d("bernie","sdk tabata done");

                splitCommand(payload);
            }
        }
    }
    public void CwmEnableRun(int cmd, float para){
        byte[] floatArray;
        if(lock.tryLock()) {
            byte[] payload = new byte[7];

            payload[0] = (byte) 0x82; // command type : Switch OTA
            payload[1] = (byte) 0x20; // command id : request device version
            payload[2] = (byte) cmd;
            if(cmd == 0x01 || cmd == 0x02){
                floatArray = ByteBuffer.allocate(4).putFloat(para).array();
                payload[3] = floatArray[0];
                payload[4] = floatArray[1];
                payload[5] = floatArray[2];
                payload[6] = floatArray[3];
            }
            splitCommand(payload);
            lock.unlock();
        }
    }
    public String CwmSdkVersion(){
        return SDK_VERSION;
    }
    public void CwmSendBitMap(){
        if(bitMapLength >= 0) {
            byte[] payload = new byte[128+3+4]; // type + id + partiion id +(4 byte address)

            payload[0] = (byte)0x85;
            payload[1] = (byte)0x05;
            if(oledAccomplish == false) {
                payload[2] = (byte) ID.OLED_PAGE;
                System.arraycopy(mapArray, endPos, payload, 7, 128);

                payload[3] = (byte)(endPos & 0xFF);
                payload[4] = (byte)((endPos >> 8) & 0xFF);
                payload[5] = (byte)((endPos >> 16) & 0xFF);
                payload[6] = (byte)((endPos >> 24) & 0xFF);
                Log.d("bernie","oled endPos is: "+Integer.toHexString(endPos & 0xFFFFF));
                splitCommand(payload);

              //  endPos = endPos + 128;
               // currentMapSize = currentMapSize + 128;
                //mapSize = mapSize + 128;

                Log.d("bernie","oled mapSize is: "+Integer.toHexString(mapSize & 0xFFFFF));
          /*      if(mapSize == Type.OLED_PAGE_SIZE) {
                    Log.d("bernie","endPos size is oled page size ");
                    oledAccomplish = true;
                    mapSize = 0;
                    endPos = 0xE000;
                }*/
            }
            else if(bitMapAccomplish == false){
                payload[2] = (byte) ID.BITMAP_PAGE;
                System.arraycopy(mapArray, endPos, payload, 7, 128);

                payload[3] = (byte)(endPos & 0xFF);
                payload[4] = (byte)((endPos >> 8) & 0xFF);
                payload[5] = (byte)((endPos >> 16) & 0xFF);
                payload[6] = (byte)((endPos >> 24) & 0xFF);
                Log.d("bernie","bitmap endPos is: "+Integer.toHexString(endPos & 0xFFFFF));
                splitCommand(payload);

                //endPos = endPos + 128;
                //currentMapSize = currentMapSize + 128;
                //mapSize = mapSize + 128;

                Log.d("bernie","bitmap mapSize is: "+Integer.toHexString(mapSize & 0xFFFFF));
          /*      if(mapSize == Type.BITMAP_PAHE_SIZE) {
                    Log.d("bernie","endPos size is bitmap page size ");
                    bitMapAccomplish = true;
                    mapSize = 0;
                    endPos = 0x50000;
                }*/
            }
            else if(fontLitAccomplish == false){
                payload[2] = (byte) ID.FONT_LIB;
                System.arraycopy(mapArray, endPos, payload, 7, 128);

                payload[3] = (byte)(endPos & 0xFF);
                payload[4] = (byte)((endPos >> 8) & 0xFF);
                payload[5] = (byte)((endPos >> 16) & 0xFF);
                payload[6] = (byte)((endPos >> 24) & 0xFF);
                Log.d("bernie","font endPos is: "+Integer.toHexString(endPos & 0xFFFFF));
                splitCommand(payload);

                //endPos = endPos + 128;
                //currentMapSize = currentMapSize + 128;
                //mapSize = mapSize + 128;

                Log.d("bernie","font mapSize is: "+Integer.toHexString(mapSize & 0xFFFFF));
               /* if(mapSize == Type.FONT_LIB) {
                    Log.d("bernie","endPos size is font lib page size ");
                    fontLitAccomplish = true;
                    mapSize = 0;
                }*/
            }
        }
    }
    public void CwmReSendBitMap(){
        //endPos = endPos - 128;
        //mapSize = mapSize - 128;
        //currentMapSize = currentMapSize - 128;
        CwmSendBitMap();
    }
    public void CwmUpdateBitMapInit(){
        File file = new File(Environment.getExternalStorageDirectory().toString() + "/Download/Bitmap_to_binary.bin");
        bitMapLength = file.length();
        Log.d("bernie","sdk bitMapLength:"+Integer.toString((int)bitMapLength));
        mapArray = new byte[(int)file.length()];
        try {
            FileInputStream fis = new FileInputStream(file);
            fis.read(mapArray);
            fis.close();
            //for(int i = 0 ; i < mapArray.length ; i++)
             //   Log.d("bernie",Integer.toHexString(mapArray[i] & 0xFF));
        } catch (IOException e){
            e.printStackTrace();
        }
        if(oledAccomplish == true && bitMapAccomplish == true && fontLitAccomplish == true) {
            oledAccomplish = false;
            bitMapAccomplish = false;
            fontLitAccomplish = false;
            mapSize = 0;
        }
    }
    public void CwmSyncRequest(){
        if (lock.tryLock()) {
            byte[] payload = new byte[3];

            payload[0] = (byte) 0x83;
            payload[1] = (byte) ID.REQUEST_HISTORY;
            payload[2] = (byte) Type.FLASH_SYNC_TYPE.SYNC_DATA_LENGTH.ordinal();

            splitCommand(payload);
            lock.unlock();
        }
    }
    public void CwmSyncStart(){
        if (lock.tryLock()) {
            if(BleReceiver.hasLongTask == false) {
                byte[] payload = new byte[3];

                payload[0] = (byte) 0x83;
                payload[1] = (byte) ID.REQUEST_HISTORY;
                payload[2] = (byte) Type.FLASH_SYNC_TYPE.SYNC_START.ordinal();

                splitCommand(payload);
            }
            lock.unlock();
        }
    }
    public void CwmSyncSucces(){
        if (lock.tryLock()) {
            if(BleReceiver.hasLongTask == false) {
                byte[] payload = new byte[3];

                payload[0] = (byte) 0x83;
                payload[1] = (byte) ID.REQUEST_HISTORY;
                payload[2] = (byte) Type.FLASH_SYNC_TYPE.SYNC_SUCCESS.ordinal();

                splitCommand(payload);
            }
            lock.unlock();
        }
    }
    public void CwmSyncFail(){
        if (lock.tryLock()) {
            if(BleReceiver.hasLongTask == false) {
                byte[] payload = new byte[3];

                payload[0] = (byte) 0x83;
                payload[1] = (byte) ID.REQUEST_HISTORY;
                payload[2] = (byte) Type.FLASH_SYNC_TYPE.SYNC_FAIL.ordinal();

                splitCommand(payload);
            }
            lock.unlock();
        }
    }
    public void CwmEraseLog(){
        if (lock.tryLock()) {
            byte[] payload = new byte[2];

            payload[0] = (byte) 0x83;
            payload[1] = (byte) ID.ERASE_HISTORY;
            splitCommand(payload);
            lock.unlock();
        }
    }
    public void CwmEraseBaseMap(int id){
        if (lock.tryLock()) {
            byte[] payload = new byte[3];
            payload[0] = (byte) 0x85;
            payload[1] = (byte) 0x06;
            if(id == 1)
              payload[2] = (byte)ID.OLED_PAGE;
            else if(id == 2)
              payload[2] = (byte)ID.BITMAP_PAGE;
            else if(id == 3)
              payload[2] = (byte)ID.FONT_LIB;

            splitCommand(payload);
            lock.unlock();
        }
    }
    public void CwmUnBond(){
        if (lock.tryLock()) {
            byte[] payload = new byte[2];

            payload[0] = (byte) 0x81;
            payload[1] = (byte) ID.UNBOND;
            splitCommand(payload);
            lock.unlock();
            }
    }

    public void splitCommand(byte[] payload){

        byte[] command;

        Task task = new Task(payload[0], payload[1]); //type, id
        task.registerManager(this);
        Task.currentTask = task;

        Task.taskReceivedHandler.postDelayed(task, 4000); //timer 4 sec

        if(payload.length <= 17){
            command = Protocol.addBleProtocol(payload);
            if (mConnectStatus != false) {
                mService.writeRXCharacteristic(command);
            }
        }
        else{
            command = Protocol.addBleProtocol(payload);
            final int UNIT = 19;
            int length = command.length;

            for(int i = 0 ; i < command.length ; i+=19) {
                if (length > UNIT) {
                    length -= UNIT;
                    byte[] partCommand = new byte[20]; //+header1
                    if(i == 0)
                        partCommand[0] = (byte) 0xE7;
                    else
                        partCommand[0] = (byte) 0xE8;

                    System.arraycopy(command, i, partCommand, 1, UNIT);

                    if (mConnectStatus != false) {
                        mService.writeRXCharacteristic(partCommand);
                    }
                }
                else {
                    byte[] lastCommand = new byte[length + 1]; //+header1
                    lastCommand[0] = (byte) 0xE9;
                    System.arraycopy(command, i, lastCommand, 1, length);

                    if (mConnectStatus != false) {
                        mService.writeRXCharacteristic(lastCommand);
                    }
                }

                try {
                    Thread.sleep(20);
                }
                catch (Exception e){

                }
            }
        }
    }

}
