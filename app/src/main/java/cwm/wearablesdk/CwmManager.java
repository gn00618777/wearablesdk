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
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by user on 2017/8/31.
 */

public class CwmManager{

    private String TAG = "CwmManager";

    private String SDK_VERSION = "V0.2";

    private final int NON_PENDING = 1;
    private final int PENDING = 2;
    private final int LONE_MESSAGE = 3;

    final static int PACKET_SIZE = 20;

    private final Queue<Data> mOutPutQueue = new LinkedList<>();
    private final Queue<Data> mPendingQueue = new LinkedList<>();

    /******** protoco l************/
    private enum TYPE{ ACK, MESSAGE, LONG_MESSAGE, PENDING };
    private final int ACK = 0xAC;
    private final int NACK = 0x15;
    /***************************/
    //response id received from ring
    private final int SYNC_TIME_RESPONSE_ID = 0x02;
    private final int BODY_PARAMETER_RESPONSE_ID = 0x14;
    private final int INTELLIGENT_FEATURE_RESPONSE_ID = 0x12;
    private final int CLEAN_BOND_RESPONSE_ID = 0x70;
    // message id recevied from ring.
    private final int MOTION_DATA_REPORT_MESSAGE_ID = 0xAF;
    private final int BATTERY_STATUS_REPORT_MESSAGE_ID = 0xED;
    private final int TAP_EVENT_MESSAGE_ID = 0x01;
    private final int WRIST_SCROLL_EVENT_MESSAGE_ID = 0x02;
    private final int SEDENTARY_EVENT_MESSAGE_ID = 0x03;
    private final int HART_RATE_EVENT_MESSAGE_ID = 0x04;
    private final int TABATA_EVENT_MESSAGE_ID = 0x05;
    private final int SHAKE_EVENT_MESSAGE_ID = 0x06;
    private final int SIGNIFICANT_EVENT_MESSAGE_ID = 0x08;
    private final int SOFTWARE_VERSION_MESSAGE_ID = 0x90;
    private final int SLEEP_REPORT_MESSAGE_ID = 0xBE;

    private WearableService mService = null;
    private Context mContext = null;
    private Activity mActivity = null;
    private WearableServiceListener mStatusListener = null;
    private AckListener mAckListener = null;
    private ErrorListener mErrorListener = null;
    private EventListener mListener = null;

    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_SELECT_DEVICE = 2;

    private final int TIMESTAMP_BYTE_LENGTH = 4;
    private final int HEADER_AND_CHECKSUM_LENGTH = 6;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothManager mBluetoothManager = null;

    private boolean mConnectStatus = false;

    // Keep Settings
    public final static int BODY = 1;
    public final static int INTELLIGENT = 2;
    private BodySettings bodySettings;
    private IntelligentSettings  intelligentSettings;

    //JNI
    JniManager jniMgr;

    //the usage for combining packets to one packet
    private int lengthMeasure = 0;
    private int targetLength = 0;
    private int targetID = 0;
    private int messageID = 0;

    private boolean hasLongMessage = false;

    private Handler longMessageHandler = new Handler();
    private Runnable mLongMessageTask = new Runnable() {
        @Override
        public void run() {
            hasLongMessage = false;
            mPendingQueue.clear();
            lengthMeasure = 0;
            targetLength = 0;
            targetID = 0;
            messageID = 0;
            mErrorListener.onPacketLost();
        }
    };

    // interface -----------------------------------------------------------------------------------
    public interface EventListener {
        void onEventArrival(CwmEvents cwmEvents);
    }

    public interface WearableServiceListener {
        void onConnected();
        void onDisconnected();
        void onServiceDiscovery(String deviceName, String deviceAddress);
        void onNotSupport();
    }

    public interface AckListener{
        void onAckArrival(AckEvents ackEvents);
    }

    public interface ErrorListener{
        void onPacketLost();
    }

    public CwmManager(Activity activity, WearableServiceListener wListener,
                      EventListener iLlistener, AckListener ackListener, ErrorListener errorListener){

        mActivity = activity;
        mStatusListener = wListener;
        mListener = iLlistener;
        mAckListener = ackListener;
        mErrorListener = errorListener;

        jniMgr = new JniManager();

        bodySettings = new BodySettings();
        intelligentSettings = new IntelligentSettings();

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
            final Intent mIntent = intent;
            final String deviceName = intent.getStringExtra("DeviceName");
            final String deviceAddress = intent.getStringExtra("DeviceAddress");
            //*********************//
            if (action.equals(cwm.wearablesdk.WearableService.ACTION_GATT_CONNECTED)) {
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        mConnectStatus = true;
                        mStatusListener.onConnected();
                    }
                });
            }

            //*********************//
            if (action.equals(WearableService.ACTION_GATT_DISCONNECTED)) {
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        mConnectStatus = false;
                        mService.close();
                        mStatusListener.onDisconnected();
                    }
                });
            }

            //*********************//
            if (action.equals(WearableService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mConnectStatus = true;
                mService.enableTXNotification();
                mStatusListener.onServiceDiscovery(deviceName, deviceAddress);
            }
            //*********************//
            if (action.equals(WearableService.ACTION_DATA_AVAILABLE)) {

                final byte[] rxBuffer = intent.getByteArrayExtra(WearableService.EXTRA_DATA);
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            receiveRawByte(rxBuffer);
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

        }
    };

    // function
    private void receiveRawByte(byte[] rxBuffer){
        int packet_type = 0;
        int packet_length = 0;
        int packet_id_type = 0;
        int packet_message_id = 0;
        byte[] packet = null;

        if(TYPE.ACK.ordinal() == jniMgr.getType(rxBuffer)){
            packet_type = NON_PENDING;
            packet_length = ((rxBuffer[3] & 0xFF) << 8) | (rxBuffer[2] & 0xFF);
            packet_id_type = rxBuffer[4] & 0xFF;
            packet_message_id = rxBuffer[5] & 0xFF;
            packet = rxBuffer;
            Data data = new Data(packet_type, packet_length, packet_id_type, packet_message_id, packet);
            enqueue(data);
        }
        else if(TYPE.MESSAGE.ordinal() == jniMgr.getType(rxBuffer)){
            packet_type = NON_PENDING;
            packet_length = ((rxBuffer[3] & 0xFF) << 8) | (rxBuffer[2] & 0xFF);
            packet_id_type = rxBuffer[4] & 0xFF;
            packet_message_id = 0;
            packet = rxBuffer;
            Data data = new Data(packet_type, packet_length, packet_id_type, packet_message_id, packet);
            enqueue(data);
        }
        else if(TYPE.LONG_MESSAGE.ordinal() == jniMgr.getType(rxBuffer)){
            packet_type = LONE_MESSAGE;
            packet_length = ((rxBuffer[3] & 0xFF) << 8) | (rxBuffer[2] & 0xFF);
            packet_id_type = rxBuffer[4] & 0xFF;
            packet_message_id = 0;
            packet = rxBuffer;
            Data data = new Data(packet_type, packet_length, packet_id_type, packet_message_id, packet);
            enqueue(data);
        }
        else if(TYPE.PENDING.ordinal() == jniMgr.getType(rxBuffer)){
            packet_type = PENDING;
            packet_length = rxBuffer.length;
            packet_id_type = 0;
            packet_message_id = 0;
            packet = rxBuffer;
            Data data = new Data(packet_type, packet_length, packet_id_type, packet_message_id, packet);
            //Log.d("bernie","[0]:"+Byte.toString(rxBuffer[0])+" [1]:"+Byte.toString(rxBuffer[1])+" [2]:"+Byte.toString(rxBuffer[2])+" [3]:"+Byte.toString(rxBuffer[3]));
            enqueue(data);
        }
        parser();
    }
    public boolean CwmBleStatus(){
        if(mBluetoothAdapter.isEnabled())
          return true;
        else
            return false;
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
        mService.disconnect();
        mService.close();
        mService.connect(address);
    }

    public void CwmBleDisconnect(){
        mService.disconnect();
    }

    public void CwmBleClose(){
        mService.close();
    }
    public boolean CwmDeviceUnregister(){
        return true;
    }

    public void CwmSaveBodySettings(BodySettings settings){
            bodySettings = settings;
    }
    public void CwmSaveIntelligentSettings(IntelligentSettings settings){
            intelligentSettings = settings;
    }

    public BodySettings CwmGetBodySettings(){
        return bodySettings;
    }

    public IntelligentSettings CwmGetIntelligentSettings(){
        return intelligentSettings;
    }

    public void CwmSyncBodySettings(){
        if(mConnectStatus == true) {
            byte[] command = new byte[9];
            int[] body = new int[4];

            body[0] = bodySettings.getOld();
            body[1] = bodySettings.getHight();
            if (bodySettings.getSex() == 'm' || bodySettings.getSex() == 'M')
                body[2] = 1;
            else
                body[2] = 2;
            body[3] = bodySettings.getWeight();

            /*******************************************************/
            jniMgr.getSyncBodyCommandCommand(body,command);
            /*********************************************************/
            if((mConnectStatus != false) && (hasLongMessage == false))
               mService.writeRXCharacteristic(command);
        }
    }

    public void CwmSyncIntelligentSettings(){
        if(mConnectStatus == true) {
            byte[] command = new byte[9];
            byte[] command1 = new byte[7];
            boolean[] feature = new boolean[7];
            int goal = intelligentSettings.getGoal();
            int remindTIme = intelligentSettings.getTime();
            feature[0] = intelligentSettings.getSedtentary();
            feature[1] = intelligentSettings.getHangUp();
            feature[2] = intelligentSettings.getOnWear();
            feature[3] = intelligentSettings.getDoubleTap();
            feature[4] = intelligentSettings.getWristSwitch();
            feature[5] = intelligentSettings.getShakeSwitch();
            feature[6] = intelligentSettings.getSignificant();

            /***************************************************************/
            jniMgr.getSyncIntelligentCommand(feature, goal, command);
            jniMgr.getSedentaryRemindTimeCommand(remindTIme, command1);
            /***********************************************************************************/
            if((mConnectStatus != false) && (hasLongMessage == false)) {
                mService.writeRXCharacteristic(command1);
                mService.writeRXCharacteristic(command);
            }
        }
    }

    public void CwmSyncCurrentTime(){
        int[] time = new int[7];
        byte[] command = new byte[12];
        boolean isFirstSunday;
        Calendar c = Calendar.getInstance();
        time[0] = c.get(Calendar.YEAR);
        time[1] = c.get(Calendar.MONTH);
        time[2] = c.get(Calendar.DATE);
        time[3] = c.get(Calendar.DAY_OF_WEEK);
        time[4] = c.get(Calendar.HOUR_OF_DAY);
        time[5] = c.get(Calendar.MINUTE);
        time[6] = c.get(Calendar.SECOND);

        switch(time[1]) {
            case Calendar.JANUARY:
                time[1] = 1;
                break;
            case Calendar.FEBRUARY:
                time[1] = 2;
                break;
            case Calendar.MARCH:
                time[1] = 3;
                break;
            case Calendar.APRIL:
                time[1] = 4;
                break;
            case Calendar.MAY:
                time[1] = 5;
                break;
            case Calendar.JUNE:
                time[1] = 6;
                break;
            case Calendar.JULY:
                time[1] = 7;
                break;
            case Calendar.AUGUST:
                time[1] = 8;
                break;
            case Calendar.SEPTEMBER:
                time[1] = 9;
                break;
            case Calendar.OCTOBER:
                time[1] = 10;
                break;
            case Calendar.NOVEMBER:
                time[1] = 11;
                break;
            case Calendar.DECEMBER:
                time[1] = 12;
                break;
        }

        isFirstSunday = (c.getFirstDayOfWeek() == Calendar.SUNDAY);
        if(isFirstSunday){
            time[3] = time[3] - 1;
            if(time[3] == 0){
                time[3] = 7;
            }
        }

        /****************************************/
        jniMgr.getSyncCurrentCommand(time, command);
        /****************************************/
        if((mConnectStatus != false) && (hasLongMessage == false))
           mService.writeRXCharacteristic(command);

    }

    public void CwmRequestBattery(){
        byte[] command = new byte[5];
        /*******************************************************************************/
        jniMgr.getRequestBatteryCommand(command);
         /******************************************************************************/
         if(mConnectStatus != false)
             mService.writeRXCharacteristic(command);
    }

    public void CwmRequestSwVersion(){
        byte[] command = new byte[5];
        /*******************************************************************************/
        jniMgr.getRequestSwVersionCommand(command);
        /*******************************************************************************/
        if((mConnectStatus != false) && (hasLongMessage == false))
            mService.writeRXCharacteristic(command);

    }

    public void CwmSwitchOTA(){
        byte[] command = new byte[5];
        /*******************************************************************************/
        jniMgr.getSwitchOTACommand(command);
        /*******************************************************************************/
        if((mConnectStatus != false) && (hasLongMessage == false))
            mService.writeRXCharacteristic(command);
    }

    public void CwmSendTabataParameters(TabataSettings tabataSettings){
           int[] settings = new int[6];
           boolean[] items;
           byte[] command = new byte[14];
           boolean itemSelected = false;

           settings[0] = tabataSettings.getPrepareTime();
           settings[1] = tabataSettings.getActionType();
           settings[2] = tabataSettings.getActionTime();
           settings[3] = tabataSettings.getActionTimes();
           settings[4] = tabataSettings.getIntervalTime();
           settings[5] = tabataSettings.getCycle();
           items = tabataSettings.getExerciseItems();
           for(int i = TabataSettings.ITEMS.PUSHUP.ordinal() ; i <= TabataSettings.ITEMS.PUSHUP_ROTATION.ordinal() ; i++) {
               if (items[i] == true) {
                   itemSelected = true;
                   break;
               }
           }
           // When any exercise item has selected
        if(itemSelected == true) {
            /********************************************************************************/
            jniMgr.getTabataParameterCommand(settings, items, command);
            /********************************************************************************/
            if((mConnectStatus != false) && (hasLongMessage == false))
               mService.writeRXCharacteristic(command);
        }

    }

    public void CwmRequestSleepLog(){
            byte[] command = new byte[5];
            jniMgr.getSleepLogCommand(command);
            if((mConnectStatus != false) && (hasLongMessage == false))
               mService.writeRXCharacteristic(command);
    }

    public String CwmSdkVersion(){
        return SDK_VERSION;
    }



    private void enqueue(Data data){
        if (data.type == NON_PENDING && data.length <= PACKET_SIZE) {
            mOutPutQueue.add(data);
        }
        else if(data.type == LONE_MESSAGE){
            hasLongMessage = true;
            targetLength = data.length - PACKET_SIZE;
            messageID = data.getMessageID();
            data.length = PACKET_SIZE;
            mPendingQueue.add(data);

            //The timer for receiving long message
            longMessageHandler.postDelayed(mLongMessageTask,5000);
        }
        else if((data.type == PENDING) && (hasLongMessage == true)){
            lengthMeasure += data.length;
            mPendingQueue.add(data);
            Log.d("bernie","lengthMeasure:"+Integer.toString(lengthMeasure)+" targetLength:"+Integer.toString(targetLength));
            if(lengthMeasure == targetLength){
                longMessageHandler.removeCallbacks(mLongMessageTask);
                hasLongMessage = false; //long message has been received completely.
                byte[] value = new byte[targetLength+PACKET_SIZE];
                int queueSize = 0;
                int desPos = 0;

                queueSize = mPendingQueue.size();

                for(int i = 0 ; i < queueSize ; i++) {
                    Data entry = mPendingQueue.poll();
                    System.arraycopy(entry.getValue(), 0, value, desPos, entry.length);
                    desPos += entry.length;
                }

                mOutPutQueue.add(new Data(LONE_MESSAGE, (targetLength+PACKET_SIZE), messageID, 0, value));

                lengthMeasure = 0;
                targetLength = 0;
                targetID = 0;
                messageID = 0;
            }
        }
    }
    private void parser(){
        if(mOutPutQueue.size() != 0){
            Data data = mOutPutQueue.poll();
            if(data.getIdType() == ACK) {
                int id = data.getMessageID();
                AckEvents ackEvents = new AckEvents();
                switch (id) {
                    case SYNC_TIME_RESPONSE_ID:
                        ackEvents.setId(SYNC_TIME_RESPONSE_ID);
                        mAckListener.onAckArrival(ackEvents);
                        break;
                    case BODY_PARAMETER_RESPONSE_ID:
                        ackEvents.setId(BODY_PARAMETER_RESPONSE_ID);
                        mAckListener.onAckArrival(ackEvents);
                        break;
                    case INTELLIGENT_FEATURE_RESPONSE_ID:
                        ackEvents.setId(INTELLIGENT_FEATURE_RESPONSE_ID);
                        mAckListener.onAckArrival(ackEvents);
                        break;
                    case CLEAN_BOND_RESPONSE_ID:
                        break;
                    default:
                        break;
                }
            }
            else if(data.getIdType() == NACK){

            }
            else{
                int id = data.getIdType();
                byte[] value = data.getValue();
                CwmEvents cwmEvent;
                switch (id){
                    case MOTION_DATA_REPORT_MESSAGE_ID:
                         cwmEvent = getInfomation(MOTION_DATA_REPORT_MESSAGE_ID, value);
                        mListener.onEventArrival(cwmEvent);
                        break;
                    case BATTERY_STATUS_REPORT_MESSAGE_ID:
                        cwmEvent = getInfomation(BATTERY_STATUS_REPORT_MESSAGE_ID, value);
                        mListener.onEventArrival(cwmEvent);
                        break;
                    case TAP_EVENT_MESSAGE_ID:
                        cwmEvent = getInfomation(TAP_EVENT_MESSAGE_ID, value);
                        mListener.onEventArrival(cwmEvent);
                        break;
                    case WRIST_SCROLL_EVENT_MESSAGE_ID:
                        cwmEvent = getInfomation(WRIST_SCROLL_EVENT_MESSAGE_ID, value);
                        mListener.onEventArrival(cwmEvent);
                        break;
                    case SEDENTARY_EVENT_MESSAGE_ID:
                        cwmEvent = getInfomation(SEDENTARY_EVENT_MESSAGE_ID, value);
                        mListener.onEventArrival(cwmEvent);
                        break;
                    case SHAKE_EVENT_MESSAGE_ID:
                        cwmEvent = getInfomation(SHAKE_EVENT_MESSAGE_ID, value);
                        mListener.onEventArrival(cwmEvent);
                        break;
                    case SIGNIFICANT_EVENT_MESSAGE_ID:
                        cwmEvent = getInfomation(SIGNIFICANT_EVENT_MESSAGE_ID, value);
                        mListener.onEventArrival(cwmEvent);
                        break;
                    case HART_RATE_EVENT_MESSAGE_ID:
                        cwmEvent = getInfomation(HART_RATE_EVENT_MESSAGE_ID, value);
                        mListener.onEventArrival(cwmEvent);
                        break;
                    case TABATA_EVENT_MESSAGE_ID:
                        cwmEvent = getInfomation(TABATA_EVENT_MESSAGE_ID, value);
                        mListener.onEventArrival(cwmEvent);
                        break;
                    case SOFTWARE_VERSION_MESSAGE_ID:
                        cwmEvent = getInfomation(SOFTWARE_VERSION_MESSAGE_ID, value);
                        mListener.onEventArrival(cwmEvent);
                        break;
                    case SLEEP_REPORT_MESSAGE_ID:
                         cwmEvent = getInfomation(SLEEP_REPORT_MESSAGE_ID, value);
                         mListener.onEventArrival(cwmEvent);
                        break;
                    default:
                        break;
                }
            }
        }
    }
    private CwmEvents getInfomation(int messageId, byte[] value){
        if(messageId == MOTION_DATA_REPORT_MESSAGE_ID ){
           int[] output = new int[4];
            int walkStep = 0;
            int distance = 0;
            int calories = 0;
            int status = 0;
            /***********************************************************************/
            jniMgr.getCwmInformation(MOTION_DATA_REPORT_MESSAGE_ID,value,output);
            /***********************************************************************/
             walkStep = output[0];
             distance = output[1];
             calories = output[2];
             status = output[3];
            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(MOTION_DATA_REPORT_MESSAGE_ID);
            cwmEvents.setWalkStep(walkStep);
            cwmEvents.setDistance(distance);
            cwmEvents.setCalories(calories);
            cwmEvents.setStatus(status);
            return cwmEvents;
        }
        else if(messageId == BATTERY_STATUS_REPORT_MESSAGE_ID){
            int[] output = new int[1];
            int battery = 0;
            jniMgr.getCwmInformation(BATTERY_STATUS_REPORT_MESSAGE_ID,value,output);
            battery = output[0];
            CwmEvents cwmEvents = new CwmEvents();
            /*******************************************************/
            cwmEvents.setId(BATTERY_STATUS_REPORT_MESSAGE_ID);
            cwmEvents.setBattery(battery);
            return cwmEvents;
        }
        else if(messageId == TAP_EVENT_MESSAGE_ID){
            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(TAP_EVENT_MESSAGE_ID);
            return cwmEvents;
        }
        else if(messageId == WRIST_SCROLL_EVENT_MESSAGE_ID){
            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(WRIST_SCROLL_EVENT_MESSAGE_ID);
            return cwmEvents;
        }
        else if(messageId == SHAKE_EVENT_MESSAGE_ID){
            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(SHAKE_EVENT_MESSAGE_ID);
            return cwmEvents;
        }
        else if(messageId == SIGNIFICANT_EVENT_MESSAGE_ID){
            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(SIGNIFICANT_EVENT_MESSAGE_ID);
            return cwmEvents;
        }
        else if(messageId == HART_RATE_EVENT_MESSAGE_ID){
            int[] output = new int[2];
            int heartBeat = 0;

            jniMgr.getCwmInformation(HART_RATE_EVENT_MESSAGE_ID,value,output);
            heartBeat = output[0];
            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(HART_RATE_EVENT_MESSAGE_ID);
            cwmEvents.setHeartBeat(heartBeat);
            return cwmEvents;
        }
        else if(messageId == SEDENTARY_EVENT_MESSAGE_ID){
            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(SEDENTARY_EVENT_MESSAGE_ID);
            return cwmEvents;
    }
        else if(messageId == TABATA_EVENT_MESSAGE_ID){
            int[] output = new int[6];
            int items = 0;
            int count = 0;
            int calories = 0;
            int heartRate = 0;
            int strength = 0;
            int status = 0;

            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(TABATA_EVENT_MESSAGE_ID);
            /***************************************************************/
            jniMgr.getCwmInformation(TABATA_EVENT_MESSAGE_ID, value, output);
            /***************************************************************/

            //byte[] dest = new byte[4];
            //System.arraycopy(value, 5, dest, 0, 4);
            //Toast.makeText(mActivity,Float.toString(ByteBuffer.wrap(dest).order(ByteOrder.LITTLE_ENDIAN).getFloat()),Toast.LENGTH_SHORT).show();
            //Log.d("bernie","tabata message id");

            items = output[0];
            count = output[1];
            calories = output[2];
            heartRate = output[3];
            strength = output[4];
            status = output[5];

            cwmEvents.setExerciseItem(items);
            cwmEvents.setDoItemCount(count);
            cwmEvents.setCalories(calories);
            cwmEvents.setHeartBeat(heartRate);
            cwmEvents.setStrength(strength);
            cwmEvents.setTabataStatus(status);

            return cwmEvents;

        } else if(messageId == SOFTWARE_VERSION_MESSAGE_ID){
            int[] output = new int[2];
            float main = 0;
            float sub = 0;
            /***************************************************************/
            jniMgr.getCwmInformation(SOFTWARE_VERSION_MESSAGE_ID,value,output);
            /***************************************************************/
            main = (float)output[0];
            sub = (((float)output[1]) / 100);

            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(SOFTWARE_VERSION_MESSAGE_ID);
            cwmEvents.setSwVersion(main+sub);

            return cwmEvents;
        } else if(messageId == SLEEP_REPORT_MESSAGE_ID){
            int startPos = 5;
            int unit_sleep_log = 4;//byte
            int checksum_byte = 1;//byte
            int endPos = value.length -1 - checksum_byte;
            int dataLength = endPos - startPos +1;
            int j = 0;

            float[] output = new float[dataLength/unit_sleep_log];
            int[] convert = new int[dataLength/unit_sleep_log];
            byte[] temp = new byte[unit_sleep_log];
            /***************************************************************/
            //jniMgr.getCwmSleepInfomation(SLEEP_REPORT_MESSAGE_ID,value,output);
            /***************************************************************/
            for(int i = startPos ; i <= endPos; i+=unit_sleep_log) {
                System.arraycopy(value, i, temp, 0, unit_sleep_log);
                convert[j] = ByteBuffer.wrap(temp).order(ByteOrder.LITTLE_ENDIAN).getInt();
                j++;
            }
            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(SLEEP_REPORT_MESSAGE_ID);
            cwmEvents.setSleepLogLength(value.length);
            cwmEvents.setSleepCombined(value);
            cwmEvents.setSleepParser(convert);
            cwmEvents.setParserLength(convert.length);

            return cwmEvents;
        }
        return null;
    }

    public class Data{

          private int type;
          private int length;
          private int idType; // to differentiate between ack & nack and message id
          private int messageID;
          private byte[] value;

        private Data(int type, int length, int idType, int messageID, byte[] value) {
            this.type = type;
            this.length = length;
            this.idType = idType;
            this.messageID = messageID;
            this.value = value;
        }

        public int getIdType(){
            return idType;
        }

        public int getMessageID(){
            if(idType == ACK ||  idType == NACK)
                return messageID;
            else
                return idType;
        }

        public byte[] getValue(){return value;}
    }


}
