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
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by user on 2017/8/31.
 */

public class CwmManager{

    private String TAG = "CwmManager";

    private String SDK_VERSION = "V0.2";

    public static final int NON_PENDING = 1;
    public static final int PENDING = 2;
    public static final int LONE_MESSAGE = 3;

    final static int PACKET_SIZE = 20;

    public static final Queue<Data> mOutPutQueue = new LinkedList<>();
    public static final Queue<Data> mPendingQueue = new LinkedList<>();
    public static Task mCurrentTask = new Task(0,0,0);

    public static WearableService mService = null;
    private Context mContext = null;
    private Activity mActivity = null;
    private WearableServiceListener mStatusListener = null;
    public static AckListener mAckListener = null;
    public static ErrorListener mErrorListener = null;
    public static EventListener mListener = null;
    public static LogSyncListener mLogListener = null;
    private RawDataListener mRawListener = null;

    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_SELECT_DEVICE = 2;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothManager mBluetoothManager = null;

    public static boolean mConnectStatus = false;
    public static boolean skipClassify = false;
    private boolean calibrate = false;

    public static Handler taskReceivedHandler = new Handler();

    // Keep Settings
    public final static int BODY = 1;
    public final static int INTELLIGENT = 2;
    public static BodySettings bodySettings;
    public static IntelligentSettings  intelligentSettings;

    //JNI
    public static JniManager jniMgr;

    //the usage for combining packets to one packet
    public static int lengthMeasure = 0;
    public static int targetLength = 0;
    public static int messageID = 0;
    public static int tagID = 0;

    public static boolean isTaskHasComplete = true;
    public static boolean hasLongMessage = false;

    // flash max bytes
    public static int maxBytes = 0;
    // flash accumulation
    public static int acculateByte = 0;

    private QueueHandler mQueueHandler = new QueueHandler();

    public static Handler longMessageHandler = new Handler();
    public static Runnable mLongMessageTask = new Runnable() {
        @Override
        public void run() {
            skipClassify = false;
            ErrorEvents errorEvents = new ErrorEvents();
            errorEvents.setId(0x02); //packets lost
            errorEvents.setCommand(messageID);
            isTaskHasComplete = true;
            if(mCurrentTask.getCommand() == ID.READ_FLASH_COMMAND_ID) {
                Log.d("bernie","sync failed");
                errorEvents.setTag(tagID);
                //CwmFlashSyncFail();
            }
            if(mErrorListener != null)
                mErrorListener.onErrorArrival(errorEvents);
            hasLongMessage = false;
            mPendingQueue.clear();
            lengthMeasure = 0;
            targetLength = 0;
            messageID = 0;
            tagID = 0;
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
        void onUnknownProblem();
    }

    public interface AckListener{
        void onAckArrival(AckEvents ackEvents);
    }

    public interface ErrorListener{
        void onErrorArrival(ErrorEvents errorEvents);
    }

    public interface LogSyncListener{
        void onSyncFailed();
        void onProgressChanged(int currentpart, int partsTotal);
        void onSyncDone();
    }
    public interface RawDataListener{
        void onRawDataArrival(byte[] rawByte);
    }

    public CwmManager(Activity activity, WearableServiceListener wListener,
                      EventListener iLlistener, AckListener ackListener, ErrorListener errorListener,
                      LogSyncListener logSyncListener, RawDataListener rawDataListener ){

        mActivity = activity;
        mStatusListener = wListener;
        mListener = iLlistener;
        mAckListener = ackListener;
        mErrorListener = errorListener;
        mLogListener = logSyncListener;
        mRawListener = rawDataListener;

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
                if(mService.enableTXNotification())
                   mStatusListener.onServiceDiscovery(deviceName, deviceAddress);
                CwmRemoveLog();
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

            if(action.equals(WearableService.MCU_HAS_UNKNOWN_PROBLEM)){
                mConnectStatus = false;
                mService.disconnect();
                mStatusListener.onUnknownProblem();
            }

        }
    };

    // function
    private void receiveRawByte(byte[] rxBuffer){
        int packet_type = 0;
        int packet_length = 0;
        int packet_id_type = 0;
        int packet_message_id = 0;
        int packet_tag = 0;
        byte[] packet = null;

        Log.d("bernie","rxBuffer[0] "+Integer.toHexString(rxBuffer[0] & 0xff)+" rxBuffer[1] "+Integer.toHexString(rxBuffer[1] & 0xff)+
                " rxBuffer[2] "+Integer.toHexString(rxBuffer[2] & 0xff)+" rxBuffer[3] "+
                Integer.toHexString(rxBuffer[3] & 0xff)+" rxBuffer[4]: "+Integer.toHexString(rxBuffer[4] & 0xff)+" rxBuffer[5]: "+Integer.toHexString(rxBuffer[5] & 0xff));

        mRawListener.onRawDataArrival(rxBuffer);

        if(skipClassify){
            Data data;
            packet_type = PENDING;
            packet_length = rxBuffer.length;
            packet_id_type = 0;
            packet_message_id = 0;
            packet = rxBuffer;
            data = new Data(packet_type, packet_length, packet_id_type, packet_message_id, packet);
            mQueueHandler.enOtherQueue(data);
            mQueueHandler.deQueue();
            return;
        }
        else {
            if (Type.BLE_PAKAGE_TYPE.ACK.ordinal() == jniMgr.getType(rxBuffer)) {
                Data data;
                packet_type = NON_PENDING;
                packet_length = ((rxBuffer[3] & 0xFF) << 8) | (rxBuffer[2] & 0xFF);
                packet_id_type = rxBuffer[4] & 0xFF;
                packet_message_id = rxBuffer[5] & 0xFF;
                packet = rxBuffer;
                data = new Data(packet_type, packet_length, packet_id_type, packet_message_id, packet);
                mQueueHandler.enQueue(data);
            } else if (Type.BLE_PAKAGE_TYPE.MESSAGE.ordinal() == jniMgr.getType(rxBuffer)) {
                Data data;
                packet_type = NON_PENDING;
                packet_length = ((rxBuffer[3] & 0xFF) << 8) | (rxBuffer[2] & 0xFF);
                packet_id_type = rxBuffer[4] & 0xFF;
                packet_message_id = 0;
                packet = rxBuffer;
                if (packet_id_type == ID.READ_FLASH_COMMAND_ID) {
                    packet_tag = rxBuffer[5] & 0xFF;
                    data = new Data(packet_type, packet_length, packet_id_type, packet_message_id, packet_tag, packet);
                } else {
                    data = new Data(packet_type, packet_length, packet_id_type, packet_message_id, packet);
                }
                mQueueHandler.enQueue(data);
            } else if (Type.BLE_PAKAGE_TYPE.LONG_MESSAGE.ordinal() == jniMgr.getType(rxBuffer)) {
                skipClassify = true;
                Data data;
                packet_type = LONE_MESSAGE;
                packet_length = ((rxBuffer[3] & 0xFF) << 8) | (rxBuffer[2] & 0xFF);
                Log.d("bernie","packet length "+Integer.toString(packet_length));
                packet_id_type = rxBuffer[4] & 0xFF;
                packet_message_id = 0;
                packet = rxBuffer;
                if (packet_id_type == ID.READ_FLASH_COMMAND_ID) {
                    packet_tag = rxBuffer[5] & 0xFF;
                    data = new Data(packet_type, packet_length, packet_id_type, packet_message_id, packet_tag, packet);
                } else {
                    data = new Data(packet_type, packet_length, packet_id_type, packet_message_id, packet);
                }
                mQueueHandler.enOtherQueue(data);
            } else if (Type.BLE_PAKAGE_TYPE.PENDING.ordinal() == jniMgr.getType(rxBuffer)) {
                Data data;
                packet_type = PENDING;
                packet_length = rxBuffer.length;
                packet_id_type = 0;
                packet_message_id = 0;
                packet = rxBuffer;
                data = new Data(packet_type, packet_length, packet_id_type, packet_message_id, packet);
                //Log.d("bernie","[0]:"+Byte.toString(rxBuffer[0])+" [1]:"+Byte.toString(rxBuffer[1])+" [2]:"+Byte.toString(rxBuffer[2])+" [3]:"+Byte.toString(rxBuffer[3]));
                mQueueHandler.enOtherQueue(data);
            }
        }
        mQueueHandler.deQueue();
    }
    public boolean CwmBleStatus(){
        if(mBluetoothAdapter.isEnabled())
          return true;
        else
            return false;
    }

    public void CwmSensorReport(int sensorType){
        byte[] command = new byte[10];
        /********************************************************************************/
        CwmManager.jniMgr.getEnaableSensorCommand(sensorType, command);
        /********************************************************************************/
        if((CwmManager.mConnectStatus != false)){
            CwmManager.mService.writeRXCharacteristic(command);
        }
    }

    public void CwmReleaseResource(){
        if(mActivity != null) {
            LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(UARTStatusChangeReceiver);
            mActivity.unbindService(mServiceConnection);
            taskReceivedHandler.removeCallbacks(mCurrentTask);
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
        //mService.disconnect();
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
        Task task = new Task(ID.BODY_PARAMETER_RESPONSE_ID, 2, 0);
        if(isTaskHasComplete == true) {
            mCurrentTask = task;
            mCurrentTask.doWork();
        }
    }

    public void CwmSyncIntelligentSettings(){
        Task task = new Task(ID.INTELLIGENT_FEATURE_RESPONSE_ID, 2, 0);
        if(isTaskHasComplete == true) {
            mCurrentTask = task;
            mCurrentTask.doWork();
        }
    }

    public void CwmSyncCurrentTime(){
        Task task = new Task(ID.SYNC_TIME_RESPONSE_ID, 2, 0);
        if(isTaskHasComplete == true) {
            mCurrentTask = task;
            mCurrentTask.doWork();
        }
    }

    public void CwmRequestBattery(){
        Task task = new Task(ID.BATTERY_STATUS_REPORT_MESSAGE_ID, 2, 0); //ID, timer 2 sec
        if(isTaskHasComplete == true) {
            mCurrentTask = task;
            mCurrentTask.doWork();
        }
    }

    public void CwmRequestSwVersion(){
        Task task = new Task(ID.SOFTWARE_VERSION_MESSAGE_ID, 2, 0); //ID, timer 2 sec
        if(isTaskHasComplete == true) {
            mCurrentTask = task;
            mCurrentTask.doWork();
        }
    }

    public void CwmRequestEraseProgress(){
        Task task = new Task(ID.REQUEST_ERASE_PROGRESS_ID, 2, 0);
        if(isTaskHasComplete == true) {
            mCurrentTask = task;
            mCurrentTask.doWork();
        }
    }

    public void CwmCalibrate(int sensor){
        Task task = new Task(ID.CALIBRATE_COMMAND_ID, 2, 1);
        task.getParametersObj().setParameters(sensor, 0, 0);
        if(isTaskHasComplete == true) {
            mCurrentTask = task;
            mCurrentTask.doWork();
        }
    }

    public void CwmRecordSensorToFlash(int sensorType, int odrType, int sensorStatus){
        Task task = new Task(ID.RECORD_SENSOR_ID, 2, 1); //ID, timer 2 sec
        task.getParametersObj().setParameters(sensorType, odrType, sensorStatus);
        if(isTaskHasComplete == true) {
            mCurrentTask = task;
            mCurrentTask.doWork();
        }
    }

    public void CwmSwitchOTA(){
        Task task = new Task(ID.SWITCH_OTA_ID, 2, 0); //ID, timer 2 sec
        if(isTaskHasComplete == true) {
            mCurrentTask = task;
            mCurrentTask.doWork();
        }
    }

    public void CwmTabataCommand(int operate, int prepare, int interval, int action_item){

           if(operate == Type.ITEMS.TABATA_INIT.ordinal()){
               Task task = new Task(ID.TABATA_COMMAND_ID, 2, 0); //ID, timer 2 sec
               if(isTaskHasComplete == true) {
                   mCurrentTask = task;
                   mCurrentTask.doWork();
               }
           }
           else if(operate == Type.ITEMS.TABATA_PREPARE_START.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(Type.ITEMS.TABATA_PREPARE_START.ordinal(), 0, 0, 0, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }
           }
           else if(operate == Type.ITEMS.TABATA_PREPARE_COUNT.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(Type.ITEMS.TABATA_PREPARE_COUNT.ordinal(), prepare, 0, 0, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }
           }
           else if(operate == Type.ITEMS.TABATA_PREARE_END.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(Type.ITEMS.TABATA_PREARE_END.ordinal(), 0, 0, 0, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }
           }
           else if(operate == Type.ITEMS.TABATA_ACTION_ITEM.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(Type.ITEMS.TABATA_ACTION_ITEM.ordinal(), 0, 0, action_item, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }
           }
           else if(operate == Type.ITEMS.TABATA_ACTION_START.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(Type.ITEMS.TABATA_ACTION_START.ordinal(), 0, 0, 0, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }

           }
           else if(operate == Type.ITEMS.TABATA_ACTION_END.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(Type.ITEMS.TABATA_ACTION_END.ordinal(), 0, 0, 0, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }
           }
           else if(operate == Type.ITEMS.TABATA_REST_START.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(Type.ITEMS.TABATA_REST_START.ordinal(), 0, 0, 0, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }
           }
           else if(operate == Type.ITEMS.TABATA_REST_COUNT.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(Type.ITEMS.TABATA_REST_COUNT.ordinal(), 0, interval, 0, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }
           }
           else if(operate == Type.ITEMS.TABATA_REST_END.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(Type.ITEMS.TABATA_REST_END.ordinal(), 0, 0, 0, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }
           }
           else if(operate == Type.ITEMS.TABATA_PAUSE.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(Type.ITEMS.TABATA_PAUSE.ordinal(), 0, 0, 0, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }
           }
           else if(operate == Type.ITEMS.TABATA_REQUEST.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(Type.ITEMS.TABATA_REQUEST.ordinal(), 0, 0, 0, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }
           }
           else if(operate == Type.ITEMS.TABATA_DONE.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(Type.ITEMS.TABATA_DONE.ordinal(), 0, 0, 0, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }
           }
           else if(operate == Type.ITEMS.TABATA_RESUME.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(Type.ITEMS.TABATA_RESUME.ordinal(), 0, 0, 0, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }
           }
    }

    public void CwmRequestSleepLog(){
        Task task = new Task(ID.SLEEP_REPORT_MESSAGE_ID, 5, 0); //ID, timer 2 sec
        if(isTaskHasComplete == true) {
            mCurrentTask = task;
            mCurrentTask.doWork();
        }
    }

    public String CwmSdkVersion(){
        return SDK_VERSION;
    }

    public void CwmRequestMaxLogPackets(){
        Task task = new Task(ID.REQUEST_MAX_LOG_PACKETS_ID, 2, 0); //ID, timer 2 sec
        if(isTaskHasComplete == true) {
            mCurrentTask = task;
            mCurrentTask.doWork();
        }
    }

    public void CwmRequestGestureList(){
        Task task = new Task(ID.REQUEST_GESTURE_LIST, 2, 0); //ID, timer 2 sec
        if(isTaskHasComplete == true) {
            mCurrentTask = task;
            mCurrentTask.doWork();
        }
    }

    public void CwmRemoveLog(){
        File file = new File(Environment.getExternalStorageDirectory().toString() + "/Download/CwmLog.txt");
        if(file.exists()){
            file.delete();
        }
    }

    public void CwmFlashSyncStart(){
        Task task = new Task(ID.READ_FLASH_COMMAND_ID, 2, 0); //ID, timer 2 sec
        task.setSyncType(Type.FLASH_SYNC_TYPE.SYNC_START.ordinal());
        tagID = Type.FLASH_SYNC_TYPE.SYNC_START.ordinal();
        if(isTaskHasComplete == true) {
            mCurrentTask = task;
            mCurrentTask.doWork();
        }
    }
    public void CwmFlashSyncSuccess(){
        Task task = new Task(ID.READ_FLASH_COMMAND_ID, 2, 0); //ID, timer 2 sec
        task.setSyncType(Type.FLASH_SYNC_TYPE.SYNC_SUCCESS.ordinal());
        tagID = Type.FLASH_SYNC_TYPE.SYNC_SUCCESS.ordinal();
        if(isTaskHasComplete == true) {
            mCurrentTask = task;
            mCurrentTask.doWork();
        }
    }
    public void CwmFlashSyncFail(){
        Task task = new Task(ID.READ_FLASH_COMMAND_ID, 2, 0); //ID, timer 2 sec
        task.setSyncType(Type.FLASH_SYNC_TYPE.SYNC_FAIL.ordinal());
        tagID = Type.FLASH_SYNC_TYPE.SYNC_FAIL.ordinal();
        if(isTaskHasComplete == true) {
            mCurrentTask = task;
            mCurrentTask.doWork();
        }
    }

    public void CwmFlashErase(){
        Task task = new Task(ID.READ_FLASH_COMMAND_ID, 2, 0); //ID, timer 2 sec
        task.setSyncType(Type.FLASH_SYNC_TYPE.SYNC_ERASE.ordinal());
        tagID = Type.FLASH_SYNC_TYPE.SYNC_ERASE.ordinal();
        if(isTaskHasComplete == true) {
            mCurrentTask = task;
            mCurrentTask.doWork();
        }
    }

    public void CwmTestRequest(){
        byte[] command = new byte[1];
        command[0] = 0xf;
        if(mConnectStatus != false)
            mService.writeRXCharacteristic(command);
    }
}
