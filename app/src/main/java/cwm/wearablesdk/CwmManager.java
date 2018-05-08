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
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
    private final int SHORT = 17;
    private final int UNIT = 19;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothManager mBluetoothManager = null;
    private boolean mConnectStatus = false;

    private BleReceiver mBleReceiver = new BleReceiver(this);

    /*********Upadet BitMap************/
    private byte[] mapArray;
    public static int currentMapSize = 0;
    public static int startAddress = 0;
    public static int startIndex = 16;
    public static int targetSize = 0;
    public static int targetPartionID = 0;
    public Command command;
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

    public EventListener getListener(){return mListener;}
    public AckListener getAckListener(){return mAckListener;}
    public ErrorListener getErrorListener(){return mErrorListener;}

    public boolean bleStatus(){
        if(mBluetoothAdapter.isEnabled())
          return true;
        else
            return false;
    }
    public void releaseResource(){
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
    public void bleSearch() {
        if(mBluetoothAdapter.isEnabled()){
            Intent newIntent = new Intent(mActivity, DeviceListActivity.class);
            mActivity.startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
        }
        else{
            Toast.makeText(mActivity,"Your bluetooth is not enabled",Toast.LENGTH_LONG).show();
        }
    }
    public void bleConnect(String address){
        mService.connect(address);
    }
    public void bleDisconnect(){
        mService.disconnect();
    }
    public void bleClose(){
        mService.close();
    }
    public void notification(NotificationData data){
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
                        sendCommand(payload);
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
                            sendCommand(payload);
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
                        case Type.NEWS:
                            payload[2] = (byte) Type.NEWS;
                            break;
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
                        sendCommand(payload);
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
                            sendCommand(payload);
                        }
                        catch (IOException e){

                        }
                    }
                    break;
                case ID.EMAIL:
                    payload = new byte[2]; //message type + message id
                    payload[0] = (byte) 0x86;
                    payload[1] = (byte) ID.EMAIL;
                    sendCommand(payload);
                    break;
                case ID.MISSING_CALL:
                    payload = new byte[2]; //message type + message id
                    payload[0] = (byte) 0x86;
                    payload[1] = (byte) ID.MISSING_CALL;
                    sendCommand(payload);
                    Log.d("bernie","sdk missing call");
                    break;
                case ID.PICK_UP:
                    payload = new byte[2]; //message type + message id
                    payload[0] = (byte) 0x86;
                    payload[1] = (byte) ID.PICK_UP;
                    sendCommand(payload);
                    Log.d("bernie","sdk pick up call");
                    break;
                default:
                    break;
            }
            lock.unlock();
        }
    }
    public void requestUserConfig(){
        if(lock.tryLock()) {
            if(BleReceiver.hasLongTask == false) {
                byte[] payload = new byte[4];

                payload[0] = (byte) 0x81; // command type
                payload[1] = (byte) 0x04; // command id

                sendCommand(payload);
            }
            lock.unlock();
        }
    }
    public void resetUserConfig(){
        if(lock.tryLock()) {
            byte[] payload = new byte[4];

            payload[0] = (byte) 0x81; // command type
            payload[1] = (byte) ID.RESET_USERCONFIG; // command id

            sendCommand(payload);

            lock.unlock();
        }
    }
    public void sendUserConfig(UserConfig userConfig) {
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

        byte[] config = new byte[75];

        config[0] = (byte)0x81;
        config[1] = (byte)0x01;
        config[2] = (byte)0x00;
        config[3] = (byte)0x00;
        config[4] = (byte)0x00;
        config[5] = (byte)0x00;
        config[6] = (byte)osType;
        config[7] = (byte)timeFormat;
        config[8] = (byte)historyDetect;
        config[9] = (byte)screenTimout;

        screens_I = screens & 0xFF;
        screens_II = (screens >> 8) & 0xFF;

        config[10] = (byte)screens_I;
        config[11] = (byte)screens_II;

        functions_I = functions & 0xFF;
        functions_II = (functions >> 8) & 0xFF;

        config[12] = (byte)functions_I;
        config[13] = (byte)functions_II;
        config[14] = (byte)gestureCongig_L;
        config[15] = (byte)gestureConfig_H;
        config[16] = (byte)0xFF;
        config[17] = (byte)0xFF;

        Date date = new Date();
        date.setHours(date.getHours()+8);
        unixTime = date.getTime()/1000;

        unixTime_I = ((int)unixTime & 0xFF);
        unixTime_II = ((int)unixTime >> 8 & 0xFF);
        unixTime_III = ((int)unixTime >> 16 & 0xFF);
        unixTime_IV = ((int)unixTime >> 24 & 0xFF);

        config[18] = (byte)unixTime_I;
        config[19] = (byte)unixTime_II;
        config[20] = (byte)unixTime_III;
        config[21] = (byte)unixTime_IV;
        config[22] = (byte)0x00;
        config[23] = (byte)0x00;
        config[24] = (byte)0x00;
        config[25] = (byte)0x00;
        config[26] = (byte)gender;
        config[27] = (byte)age;
        config[28] = (byte)height;
        config[29] = (byte)weight;

        targetStep_I  = targetStep & 0xFF;
        targetStep_II = (targetStep >> 8) & 0xFF;
        targetStep_III = (targetStep >> 16) & 0xFF;
        targetStep_IV = (targetStep_IV >> 24) & 0xFF;

        config[30] = (byte)targetStep_I;
        config[31] = (byte)targetStep_II;
        config[32] = (byte)targetStep_III;
        config[33] = (byte)targetStep_IV;
        config[34] = (byte)0x00;
        config[35] = (byte)0x00;
        config[36] = (byte)0x00;
        config[37] = (byte)0x00;
        config[38] = (byte)0x00;
        config[39] = (byte)0x00;
        config[40] = (byte)0x00;
        config[41] = (byte)0x00;
        config[42] = (byte)week1;
        config[43] = (byte)vibrate1;
        config[44] = (byte)hour1;
        config[45] = (byte)minute1;
        config[46] = (byte)week2;
        config[47] = (byte)vibrate2;
        config[48] = (byte)hour2;
        config[49] = (byte)minute2;
        config[50] = (byte)week3;
        config[51] = (byte)vibrate3;
        config[52] = (byte)hour3;
        config[53] = (byte)minute3;
        config[54] = (byte)week4;
        config[55] = (byte)vibrate4;
        config[56] = (byte)hour4;
        config[57] = (byte)minute4;
        config[58] = (byte)week5;
        config[59] = (byte)vibrate5;
        config[60] = (byte)hour5;
        config[61] = (byte)minute5;
        config[62] = (byte)week6;
        config[63] = (byte)vibrate6;
        config[64] = (byte)hour6;
        config[65] = (byte)minute6;
        config[66] = (byte)sleepStartTime;
        config[67] = (byte)sleepStopTime;
        config[68] = (byte)0x00;
        config[69] = (byte)0x00;
        config[70] = (byte)sedentaryTime;
        config[71] = (byte)noDisturbStart;
        config[72] = (byte)noDisturbStop;
        config[73] = (byte)0x00;
        config[74] = (byte)brightness;

        sendCommand(config);

        Log.d("bernie","send user config");

    }
    public void sensorReport(int sensorType){
        if(lock.tryLock()) {
            byte[] payload = new byte[4];

            payload[0] = (byte) 0x82; // command type
            payload[1] = (byte) 0x01; // command id
            payload[2] = (byte) sensorType;
            payload[3] = (byte) 0x01;

            sendCommand(payload);
        }
    }
    public void factory(int commandID, int sensor_id){
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

                sendCommand(payload);
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

                sendCommand(payload);
            }
            else if(commandID == ID.HEART_RATE_MECHANICAL_TEST_RESULT){
                byte[] payload = new byte[3];

                payload[0] = (byte) 0x85; // command type : Systtem information command
                payload[1] = (byte) commandID; // command id
                if (sensor_id >= ID.HR_GOLDEN_TEST && sensor_id <= ID.HR_LIGHT_LEAK_TEST) //HR golden test: 0x01 HR tartget test:0x02 HR Light Leak Test:0x03
                    payload[2] = (byte) sensor_id;

                sendCommand(payload);
            }
            lock.unlock();
        }
    }
    public void requestBattery(){
        if (lock.tryLock()) {
            byte[] payload = new byte[2];

            payload[0] = (byte) 0x81; // command type : Systtem information command
            payload[1] = (byte) 0x02; // command id : request battery

            sendCommand(payload);
            lock.unlock();
        }
    }
    public void requestSwVersion(){
        if(lock.tryLock()) {
            byte[] payload = new byte[2];

            payload[0] = (byte) 0x81; // command type : Systtem information command
            payload[1] = (byte) 0x03; // command id : request device version

            sendCommand(payload);
            lock.unlock();
        }
    }
    public void switchOTA(){
        if(lock.tryLock()) {
            byte[] payload = new byte[2];

            payload[0] = (byte) 0x85; // command type : Switch OTA
            payload[1] = (byte) 0x01; // command id : request device version

            sendCommand(payload);
            lock.unlock();
        }
    }
    public void tabataCommand(int operate, int prepare, int interval, int action_item){
        //if(lock.tryLock()) {

        if(operate >= Type.ITEMS.TABATA_INIT.ordinal() && operate <= Type.ITEMS.TABATA_SEND_HEART_RATE.ordinal()) {
            if(operate == Type.ITEMS.TABATA_ACTION_ITEM.ordinal()){
                byte[] payload = new byte[4];

                payload[0] = (byte) 0x82; // Message type sensor & gesture command
                payload[1] = (byte) 0x15; // command id : TABATA response message
                payload[2] = (byte) operate; //TABATA Operateion: Action Item
                payload[3] = (byte) action_item;
                sendCommand(payload);
            }
            else{
                byte[] payload = new byte[3];

                payload[0] = (byte) 0x82; // Message type sensor & gesture command
                payload[1] = (byte) 0x15; // command id : TABATA response message
                payload[2] = (byte) operate; //TABATA Operation

                if(operate == 12)
                    Log.d("bernie","sdk tabata done");

                sendCommand(payload);
            }
        }
    }
    public void enableRun(int cmd, float para){
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
            sendCommand(payload);
            lock.unlock();
        }
    }
    public String sdkVersion(){
        return SDK_VERSION;
    }
    public void sendBitMap(){
            byte[] payload = new byte[128+3+4]; // type + id + partiion id +(4 byte address)

            payload[0] = (byte)0x85;
            payload[1] = (byte)0x05;
            payload[2] = (byte)targetPartionID;

            payload[3] = (byte)(startAddress & 0xFF);
            payload[4] = (byte)((startAddress >> 8) & 0xFF);
            payload[5] = (byte)((startAddress >> 16) & 0xFF);
            payload[6] = (byte)((startAddress >> 24) & 0xFF);

        System.arraycopy(mapArray, startIndex, payload, 7, 128);

            //Log.d("bernie","startAddress is: "+Integer.toHexString(startAddress & 0xFFFFF));
            sendCommand(payload);
    }
    public void reSendBitMap(){
        sendBitMap();
    }
    public int updateBitMapInit(String filePath){
        File file = new File(filePath);
        mapArray = new byte[(int)file.length()];
        startIndex = 16;
        try {
            FileInputStream fis = new FileInputStream(file);
            fis.read(mapArray);
            fis.close();
        } catch (IOException e){
            e.printStackTrace();
        }

        if(!file.exists())
            return -1;

        //Validate
        if(mapArray[0] == 0x5a){
            byte[] temp = new byte[4];
            //byte[] test = new byte[49152];
            targetPartionID = mapArray[9];
            System.arraycopy(mapArray, 1, temp, 0, 4);
            startAddress = ByteBuffer.wrap(temp).order(ByteOrder.LITTLE_ENDIAN).getInt();
            System.arraycopy(mapArray, 5, temp, 0, 4);
            targetSize = ByteBuffer.wrap(temp).order(ByteOrder.LITTLE_ENDIAN).getInt();
            currentMapSize = 0;
            //System.arraycopy(test, 0, mapArray, 16, 49152);
            Log.d("bernie","targetPartionID:"+Integer.toString(targetPartionID));
            Log.d("bernie","startAddress:"+Integer.toHexString(startAddress));
            Log.d("bernie","targetSize:"+Integer.toString(targetSize));
            return targetSize;
        }

        return -1;
    }
    public void syncRequest(){
        if (lock.tryLock()) {
            byte[] payload = new byte[3];

            payload[0] = (byte) 0x83;
            payload[1] = (byte) ID.REQUEST_HISTORY;
            payload[2] = (byte) Type.FLASH_SYNC_TYPE.SYNC_DATA_LENGTH.ordinal();

            sendCommand(payload);
            lock.unlock();
        }
    }
    public void syncStart(){
        if (lock.tryLock()) {
            if(BleReceiver.hasLongTask == false) {
                byte[] payload = new byte[3];

                payload[0] = (byte) 0x83;
                payload[1] = (byte) ID.REQUEST_HISTORY;
                payload[2] = (byte) Type.FLASH_SYNC_TYPE.SYNC_START.ordinal();

                sendCommand(payload);
            }
            lock.unlock();
        }
    }
    public void syncSucces(){
        if (lock.tryLock()) {
            if(BleReceiver.hasLongTask == false) {
                byte[] payload = new byte[3];

                payload[0] = (byte) 0x83;
                payload[1] = (byte) ID.REQUEST_HISTORY;
                payload[2] = (byte) Type.FLASH_SYNC_TYPE.SYNC_SUCCESS.ordinal();

                sendCommand(payload);
            }
            lock.unlock();
        }
    }
    public void syncFail(){
        if (lock.tryLock()) {
            if(BleReceiver.hasLongTask == false) {
                byte[] payload = new byte[3];

                payload[0] = (byte) 0x83;
                payload[1] = (byte) ID.REQUEST_HISTORY;
                payload[2] = (byte) Type.FLASH_SYNC_TYPE.SYNC_FAIL.ordinal();

                sendCommand(payload);
            }
            lock.unlock();
        }
    }
    public void eraseLog(){
        if (lock.tryLock()) {
            byte[] payload = new byte[2];

            payload[0] = (byte) 0x83;
            payload[1] = (byte) ID.ERASE_HISTORY;
            sendCommand(payload);
            lock.unlock();
        }
    }
    public void eraseBaseMap(int id){
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
            else if(id == 4)
                payload[2] = (byte)ID.TFT;
            sendCommand(payload);
            lock.unlock();
        }
    }
    public void unBond(){
        if (lock.tryLock()) {
            byte[] payload = new byte[2];

            payload[0] = (byte) 0x81;
            payload[1] = (byte) ID.UNBOND;
            sendCommand(payload);
            lock.unlock();
            }
    }
    public void softReset(){
        if (lock.tryLock()) {
            byte[] payload = new byte[2];

            payload[0] = (byte) 0x85;
            payload[1] = (byte) ID.SOFTRESET;
            sendCommand(payload);
            lock.unlock();
        }
    }
    public void syncCurrent(){
        if (lock.tryLock()) {
            byte[] payload = new byte[2];

            payload[0] = (byte) 0x81;
            payload[1] = (byte) ID.CURRENT;
            sendCommand(payload);
            lock.unlock();
        }
    }
    public void sendAccount(String account){
        if (lock.tryLock()) {
            int j = 2;
            byte[] payload = new byte[12];
            payload[0] = (byte) 0x81;
            payload[1] = (byte) ID.SEND_ACCOUNT;
            if(account.length() <= 10) {
                for (int i = 0; i < account.length(); i++) {
                    payload[j] = (byte) account.charAt(i);
                    j++;
                }
            }
            else{
                for (int i = 0; i < 10; i++) {
                    payload[j] = (byte) account.charAt(i);
                    j++;
                }
            }
            sendCommand(payload);
            lock.unlock();
        }
    }
    public void clearStep(){
        if (lock.tryLock()) {
            int j = 2;
            byte[] payload = new byte[12];
            payload[0] = (byte) 0x81;
            payload[1] = (byte) ID.CLEAR_STEP;
            sendCommand(payload);
            lock.unlock();
        }
    }


    public void sendCommand(byte[] payload){
        Task task = new Task(payload[0], payload[1]); //type, id
        task.registerManager(this);
        Task.currentTask = task;

        Task.taskReceivedHandler.postDelayed(task, 4000); //timer 4 sec

        command = Protocol.addBleProtocol(payload);

        if(mConnectStatus != false)
            mService.writeRXCharacteristic(command.getTransmitted());
    }
    public void sendRemindCommand(){
        command.moveNext();

        if(mConnectStatus != false){
            mService.writeRXCharacteristic(command.getTransmitted());
        }
    }
    public void sendFinishCommand(int type){
        Log.d("bernie","end up turbo mode");
        byte[] payload = new byte[2];
        payload[0] = (byte)type;
        payload[1] = (byte)0x0B;
        command = Protocol.addBleProtocol(payload);
        if (mConnectStatus != false) {
            mService.writeRXCharacteristic(command.getTransmitted());
        }
    }
}
