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

    private Parser mParser = new Parser();

    private final Queue<Data> mOutPutQueue = new LinkedList<>();
    private final Queue<Data> mPendingQueue = new LinkedList<>();
    public static Task mCurrentTask = new Task(0,0,0);

    public enum ITEMS{
        TABATA_INIT,
        TABATA_PAUSE,
        TABATA_PREPARE_START,
        TABATA_PREPARE_COUNT,
        TABATA_PREARE_END,
        TABATA_REST_START,
        TABATA_REST_COUNT,
        TABATA_REST_END,
        TABATA_ACTION_ITEM,
        TABATA_ACTION_START,
        TABATA_ACTION_END,
        TABATA_REQUEST,
        TABATA_DONE,
        TABATA_RESUME
    };
    enum FLASH_SYNC_TYPE{
        SYNC_START,
        SYNC_SUCCESS,
        SYNC_FAIL,
        SYNC_ABORT,
        SYNC_RESUME,
        SYNC_ERASE,
        SYNC_ERASE_DONE,
        SYNC_DONE
    };
    enum PARAMETERS_TYPE{
        A,
        SENSORREQUEST
    }

    /******** protoco l************/
    private enum TYPE{ ACK, MESSAGE, LONG_MESSAGE, PENDING };
    /***************************/
    public static WearableService mService = null;
    private Context mContext = null;
    private Activity mActivity = null;
    private WearableServiceListener mStatusListener = null;
    private AckListener mAckListener = null;
    public static ErrorListener mErrorListener = null;
    private EventListener mListener = null;
    private LogSyncListener mLogListener = null;

    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_SELECT_DEVICE = 2;

    private final int TIMESTAMP_BYTE_LENGTH = 4;
    private final int HEADER_AND_CHECKSUM_LENGTH = 6;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothManager mBluetoothManager = null;

    public static boolean mConnectStatus = false;
    private boolean skipClassify = false;

    public static Handler taskReceivedHandler = new Handler();

    // Keep Settings
    public final static int BODY = 1;
    public final static int INTELLIGENT = 2;
    public static BodySettings bodySettings;
    public static IntelligentSettings  intelligentSettings;

    //JNI
    public static JniManager jniMgr;

    //the usage for combining packets to one packet
    private int lengthMeasure = 0;
    private int targetLength = 0;
    private int messageID = 0;
    private int tagID = 0;

    public static boolean isTaskHasComplete = true;
    private boolean hasLongMessage = false;

    // flash max bytes
    int maxBytes = 0;
    // flash accumulation
    int acculateByte = 0;

    private Handler longMessageHandler = new Handler();
    private Runnable mLongMessageTask = new Runnable() {
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

    public CwmManager(Activity activity, WearableServiceListener wListener,
                      EventListener iLlistener, AckListener ackListener, ErrorListener errorListener,
                      LogSyncListener logSyncListener){

        mActivity = activity;
        mStatusListener = wListener;
        mListener = iLlistener;
        mAckListener = ackListener;
        mErrorListener = errorListener;
        mLogListener = logSyncListener;

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

        Log.d("bernie","rxBuffer id: "+Integer.toHexString(rxBuffer[4] & 0xff)+" flash cmd: "+Integer.toHexString(rxBuffer[5] & 0xff));

        if(skipClassify){
            Data data;
            packet_type = PENDING;
            packet_length = rxBuffer.length;
            packet_id_type = 0;
            packet_message_id = 0;
            packet = rxBuffer;
            data = new Data(packet_type, packet_length, packet_id_type, packet_message_id, packet);
            enqueue(data);
            parser();
            return;
        }
        else {
            if (TYPE.ACK.ordinal() == jniMgr.getType(rxBuffer)) {
                Data data;
                packet_type = NON_PENDING;
                packet_length = ((rxBuffer[3] & 0xFF) << 8) | (rxBuffer[2] & 0xFF);
                packet_id_type = rxBuffer[4] & 0xFF;
                packet_message_id = rxBuffer[5] & 0xFF;
                packet = rxBuffer;
                data = new Data(packet_type, packet_length, packet_id_type, packet_message_id, packet);
                enqueue(data);
            } else if (TYPE.MESSAGE.ordinal() == jniMgr.getType(rxBuffer)) {
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
                enqueue(data);
            } else if (TYPE.LONG_MESSAGE.ordinal() == jniMgr.getType(rxBuffer)) {
                skipClassify = true;
                Data data;
                packet_type = LONE_MESSAGE;
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
                enqueue(data);
            } else if (TYPE.PENDING.ordinal() == jniMgr.getType(rxBuffer)) {
                Data data;
                packet_type = PENDING;
                packet_length = rxBuffer.length;
                packet_id_type = 0;
                packet_message_id = 0;
                packet = rxBuffer;
                data = new Data(packet_type, packet_length, packet_id_type, packet_message_id, packet);
                //Log.d("bernie","[0]:"+Byte.toString(rxBuffer[0])+" [1]:"+Byte.toString(rxBuffer[1])+" [2]:"+Byte.toString(rxBuffer[2])+" [3]:"+Byte.toString(rxBuffer[3]));
                enqueue(data);
            }
        }
        parser();
    }
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
        //byte[] command = new byte[5];
        /*******************************************************************************/
        //jniMgr.getSwitchOTACommand(command);
        /*******************************************************************************/
        //if((mConnectStatus != false) && (hasLongMessage == false))
         //   mService.writeRXCharacteristic(command);
    }

    public void CwmTabataCommand(int operate, int prepare, int interval, int action_item){

           if(operate == ITEMS.TABATA_INIT.ordinal()){
               Task task = new Task(ID.TABATA_COMMAND_ID, 2, 0); //ID, timer 2 sec
               if(isTaskHasComplete == true) {
                   mCurrentTask = task;
                   mCurrentTask.doWork();
               }
           }
           else if(operate == ITEMS.TABATA_PREPARE_START.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(ITEMS.TABATA_PREPARE_START.ordinal(), 0, 0, 0, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }
           }
           else if(operate == ITEMS.TABATA_PREPARE_COUNT.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(ITEMS.TABATA_PREPARE_COUNT.ordinal(), prepare, 0, 0, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }
           }
           else if(operate == ITEMS.TABATA_PREARE_END.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(ITEMS.TABATA_PREARE_END.ordinal(), 0, 0, 0, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }
           }
           else if(operate == ITEMS.TABATA_ACTION_ITEM.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(ITEMS.TABATA_ACTION_ITEM.ordinal(), 0, 0, action_item, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }
           }
           else if(operate == ITEMS.TABATA_ACTION_START.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(ITEMS.TABATA_ACTION_START.ordinal(), 0, 0, 0, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }

           }
           else if(operate == ITEMS.TABATA_ACTION_END.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(ITEMS.TABATA_ACTION_END.ordinal(), 0, 0, 0, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }
           }
           else if(operate == ITEMS.TABATA_REST_START.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(ITEMS.TABATA_REST_START.ordinal(), 0, 0, 0, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }
           }
           else if(operate == ITEMS.TABATA_REST_COUNT.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(ITEMS.TABATA_REST_COUNT.ordinal(), 0, interval, 0, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }
           }
           else if(operate == ITEMS.TABATA_REST_END.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(ITEMS.TABATA_REST_END.ordinal(), 0, 0, 0, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }
           }
           else if(operate == ITEMS.TABATA_PAUSE.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(ITEMS.TABATA_PAUSE.ordinal(), 0, 0, 0, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }
           }
           else if(operate == ITEMS.TABATA_REQUEST.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(ITEMS.TABATA_REQUEST.ordinal(), 0, 0, 0, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }
           }
           else if(operate == ITEMS.TABATA_DONE.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(ITEMS.TABATA_DONE.ordinal(), 0, 0, 0, command);
               /********************************************************************************/
               if((mConnectStatus != false)){
                   mService.writeRXCharacteristic(command);
               }
           }
           else if(operate == ITEMS.TABATA_RESUME.ordinal()){
               byte[] command = new byte[9];
               /********************************************************************************/
               jniMgr.getTabataCommand(ITEMS.TABATA_RESUME.ordinal(), 0, 0, 0, command);
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
        task.setSyncType(FLASH_SYNC_TYPE.SYNC_START.ordinal());
        tagID = FLASH_SYNC_TYPE.SYNC_START.ordinal();
        if(isTaskHasComplete == true) {
            mCurrentTask = task;
            mCurrentTask.doWork();
        }
    }
    public void CwmFlashSyncSuccess(){
        Task task = new Task(ID.READ_FLASH_COMMAND_ID, 2, 0); //ID, timer 2 sec
        task.setSyncType(FLASH_SYNC_TYPE.SYNC_SUCCESS.ordinal());
        tagID = FLASH_SYNC_TYPE.SYNC_SUCCESS.ordinal();
        if(isTaskHasComplete == true) {
            mCurrentTask = task;
            mCurrentTask.doWork();
        }
    }
    public void CwmFlashSyncFail(){
        Task task = new Task(ID.READ_FLASH_COMMAND_ID, 2, 0); //ID, timer 2 sec
        task.setSyncType(FLASH_SYNC_TYPE.SYNC_FAIL.ordinal());
        tagID = FLASH_SYNC_TYPE.SYNC_FAIL.ordinal();
        if(isTaskHasComplete == true) {
            mCurrentTask = task;
            mCurrentTask.doWork();
        }
    }

    public void CwmFlashErase(){
        Task task = new Task(ID.READ_FLASH_COMMAND_ID, 2, 0); //ID, timer 2 sec
        task.setSyncType(FLASH_SYNC_TYPE.SYNC_ERASE.ordinal());
        tagID = FLASH_SYNC_TYPE.SYNC_ERASE.ordinal();
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

    private void enqueue(Data data){
        if(data.type == NON_PENDING && data.length <= PACKET_SIZE && data.getIdType() == ID.ACK){
            mOutPutQueue.add(data);
        }
        else if (data.type == NON_PENDING && data.length <= PACKET_SIZE && data.getIdType() != ID.ACK) {
            //if we receive header in time, then cancle time out handler
            //because we send 0x20, but band will feedback 0x21
            if(data.getMessageID() == mCurrentTask.getCommand()) {
                if(data.getMessageID() != ID.READ_FLASH_COMMAND_ID){
                    isTaskHasComplete = false;
                    taskReceivedHandler.removeCallbacks(mCurrentTask);
                }
                if(data.getMessageID() == ID.READ_FLASH_COMMAND_ID){
                    Log.d("bernie","remove call back");
                    isTaskHasComplete = true;
                    taskReceivedHandler.removeCallbacks(mCurrentTask);
                    if(data.getTag() == FLASH_SYNC_TYPE.SYNC_ABORT.ordinal()){
                        ErrorEvents errorEvents = new ErrorEvents();
                        errorEvents.setId(0x03); //flash sync aborted
                        errorEvents.setCommand(mCurrentTask.getCommand());
                        mErrorListener.onErrorArrival(errorEvents);
                        Log.d("bernie","sync aborted");
                    }
                    else if(data.getTag() == FLASH_SYNC_TYPE.SYNC_DONE.ordinal()){
                        Log.d("bernie","sync done");
                        mLogListener.onSyncDone();
                    }
                }
            }
            if(mCurrentTask.getCommand() == ID.TABATA_COMMAND_ID && data.getMessageID() == ID.TABATA_EVENT_MESSAGE_ID ){
                isTaskHasComplete = false;
                taskReceivedHandler.removeCallbacks(mCurrentTask);
            }
            if( mCurrentTask.getCommand() == ID.READ_FLASH_COMMAND_ID && data.getMessageID() == ID.RECEIVED_FLASH_COMMAND_ID){
                isTaskHasComplete = false;
                taskReceivedHandler.removeCallbacks(mCurrentTask);
            }
            mOutPutQueue.add(data);
        }
        else if(data.type == LONE_MESSAGE){
            //if we receive header in time, then cancle time out handler
            if(data.getMessageID() == mCurrentTask.getCommand() ||
                    data.getMessageID() == ID.RECEIVED_FLASH_COMMAND_ID) {
                  isTaskHasComplete = false;
                  taskReceivedHandler.removeCallbacks(mCurrentTask);
            }
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
                skipClassify = false;
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
                messageID = 0;
            }
        }
    }
    private void parser(){
        if(mOutPutQueue.size() != 0){
            Data data = mOutPutQueue.poll();
            if(data.getMessageID() == mCurrentTask.getCommand() ||
                    data.getMessageID() == ID.RECEIVED_FLASH_COMMAND_ID){
                isTaskHasComplete = true;
            }
            else if(data.getMessageID() == ID.TABATA_EVENT_MESSAGE_ID &&
                    mCurrentTask.getCommand() == ID.TABATA_COMMAND_ID){
                isTaskHasComplete = true;
            }
            if(data.getIdType() == ID.ACK) {
                int id = data.getMessageID();
                AckEvents ackEvents = new AckEvents();
                switch (id) {
                    case ID.SYNC_TIME_RESPONSE_ID:
                        ackEvents.setId(ID.SYNC_TIME_RESPONSE_ID);
                        mAckListener.onAckArrival(ackEvents);
                        break;
                    case ID.BODY_PARAMETER_RESPONSE_ID:
                        ackEvents.setId(ID.BODY_PARAMETER_RESPONSE_ID);
                        mAckListener.onAckArrival(ackEvents);
                        break;
                    case ID.INTELLIGENT_FEATURE_RESPONSE_ID:
                        ackEvents.setId(ID.INTELLIGENT_FEATURE_RESPONSE_ID);
                        mAckListener.onAckArrival(ackEvents);
                        break;
                    case ID.CLEAN_BOND_RESPONSE_ID:
                        break;
                    case ID.SLEEP_REPORT_MESSAGE_ID:
                        ackEvents.setId(ID.SLEEP_REPORT_MESSAGE_ID);
                        mAckListener.onAckArrival(ackEvents);
                        break;
                    case ID.TABATA_COMMAND_ID:
                        ackEvents.setId(ID.TABATA_EVENT_MESSAGE_ID);
                        mAckListener.onAckArrival(ackEvents);
                        break;
                    case ID.RECORD_SENSOR_ID:
                        ackEvents.setId(ID.RECORD_SENSOR_ID);
                        mAckListener.onAckArrival(ackEvents);
                        break;
                    default:
                        break;
                }
            }
            else if(data.getIdType() == ID.NACK){

            }
            else{
                int id = data.getIdType();
                byte[] value = data.getValue();
                CwmEvents cwmEvent;
                switch (id){
                    case ID.MOTION_DATA_REPORT_MESSAGE_ID:
                         cwmEvent = getInfomation(ID.MOTION_DATA_REPORT_MESSAGE_ID, value);
                        mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.BATTERY_STATUS_REPORT_MESSAGE_ID:
                        cwmEvent = getInfomation(ID.BATTERY_STATUS_REPORT_MESSAGE_ID, value);
                        mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.TAP_EVENT_MESSAGE_ID:
                        cwmEvent = getInfomation(ID.TAP_EVENT_MESSAGE_ID, value);
                        mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.WRIST_SCROLL_EVENT_MESSAGE_ID:
                        cwmEvent = getInfomation(ID.WRIST_SCROLL_EVENT_MESSAGE_ID, value);
                        mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.SEDENTARY_EVENT_MESSAGE_ID:
                        cwmEvent = getInfomation(ID.SEDENTARY_EVENT_MESSAGE_ID, value);
                        mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.SHAKE_EVENT_MESSAGE_ID:
                        cwmEvent = getInfomation(ID.SHAKE_EVENT_MESSAGE_ID, value);
                        mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.SIGNIFICANT_EVENT_MESSAGE_ID:
                        cwmEvent = getInfomation(ID.SIGNIFICANT_EVENT_MESSAGE_ID, value);
                        mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.HART_RATE_EVENT_MESSAGE_ID:
                        cwmEvent = getInfomation(ID.HART_RATE_EVENT_MESSAGE_ID, value);
                        mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.TABATA_EVENT_MESSAGE_ID:
                        cwmEvent = getInfomation(ID.TABATA_EVENT_MESSAGE_ID, value);
                        mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.SOFTWARE_VERSION_MESSAGE_ID:
                        cwmEvent = getInfomation(ID.SOFTWARE_VERSION_MESSAGE_ID, value);
                        mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.SLEEP_REPORT_MESSAGE_ID:
                         cwmEvent = getInfomation(ID.SLEEP_REPORT_MESSAGE_ID, value);
                         mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.RECEIVED_FLASH_COMMAND_ID:
                        cwmEvent = getInfomation(ID.RECEIVED_FLASH_COMMAND_ID, value);
                        mParser.parseFlashInformation(data);
                        acculateByte ++;
                        if(maxBytes > 0) {
                            mLogListener.onProgressChanged(acculateByte, maxBytes);
                            if (acculateByte == maxBytes) {
                                acculateByte = 0;
                            }
                        }
                        //mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.READ_FLASH_COMMAND_ID:
                        cwmEvent = getInfomation(ID.READ_FLASH_COMMAND_ID, value);
                        mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.REQUEST_MAX_LOG_PACKETS_ID:
                        cwmEvent = getInfomation(ID.REQUEST_MAX_LOG_PACKETS_ID, value);
                        maxBytes = cwmEvent.getMaxByte();
                        //CwmFlashSyncStart();
                        mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.GESUTRE_EVENT_MESSAGE_ID:
                        cwmEvent = getInfomation(ID.GESUTRE_EVENT_MESSAGE_ID, value);
                        mListener.onEventArrival(cwmEvent);
                        break;
                    default:
                        break;
                }
            }
        }
    }
    private CwmEvents getInfomation(int messageId, byte[] value){
        if(messageId == ID.MOTION_DATA_REPORT_MESSAGE_ID ){
           int[] output = new int[4];
            int walkStep = 0;
            int distance = 0;
            int calories = 0;
            int status = 0;
            /***********************************************************************/
            jniMgr.getCwmInformation(ID.MOTION_DATA_REPORT_MESSAGE_ID,value,output);
            /***********************************************************************/
             walkStep = output[0];
             distance = output[1];
             calories = output[2];
             status = output[3];
            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(ID.MOTION_DATA_REPORT_MESSAGE_ID);
            cwmEvents.setWalkStep(walkStep);
            cwmEvents.setDistance(distance);
            cwmEvents.setCalories(calories);
            cwmEvents.setStatus(status);
            return cwmEvents;
        }
        else if(messageId == ID.BATTERY_STATUS_REPORT_MESSAGE_ID){
            int[] output = new int[1];
            int battery = 0;
            jniMgr.getCwmInformation(ID.BATTERY_STATUS_REPORT_MESSAGE_ID,value,output);
            battery = output[0];
            CwmEvents cwmEvents = new CwmEvents();
            /*******************************************************/
            cwmEvents.setId(ID.BATTERY_STATUS_REPORT_MESSAGE_ID);
            cwmEvents.setBattery(battery);
            return cwmEvents;
        }
        else if(messageId == ID.TAP_EVENT_MESSAGE_ID){
            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(ID.TAP_EVENT_MESSAGE_ID);
            return cwmEvents;
        }
        else if(messageId == ID.WRIST_SCROLL_EVENT_MESSAGE_ID){
            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(ID.WRIST_SCROLL_EVENT_MESSAGE_ID);
            return cwmEvents;
        }
        else if(messageId == ID.SHAKE_EVENT_MESSAGE_ID){
            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(ID.SHAKE_EVENT_MESSAGE_ID);
            return cwmEvents;
        }
        else if(messageId == ID.SIGNIFICANT_EVENT_MESSAGE_ID){
            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(ID.SIGNIFICANT_EVENT_MESSAGE_ID);
            return cwmEvents;
        }
        else if(messageId == ID.HART_RATE_EVENT_MESSAGE_ID){
            int[] output = new int[2];
            int heartBeat = 0;

            jniMgr.getCwmInformation(ID.HART_RATE_EVENT_MESSAGE_ID,value,output);
            heartBeat = output[0];
            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(ID.HART_RATE_EVENT_MESSAGE_ID);
            cwmEvents.setHeartBeat(heartBeat);
            return cwmEvents;
        }
        else if(messageId == ID.SEDENTARY_EVENT_MESSAGE_ID){
            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(ID.SEDENTARY_EVENT_MESSAGE_ID);
            return cwmEvents;
    }
        else if(messageId == ID.TABATA_EVENT_MESSAGE_ID){
            int[] output = new int[6];
            int items = 0;
            int count = 0;
            int calories = 0;
            int heartRate = 0;
            int strength = 0;
            int status = 0;

            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(ID.TABATA_EVENT_MESSAGE_ID);
            /***************************************************************/
            jniMgr.getCwmInformation(ID.TABATA_EVENT_MESSAGE_ID, value, output);
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

        } else if(messageId == ID.SOFTWARE_VERSION_MESSAGE_ID){
            int[] output = new int[2];
            float main = 0;
            float sub = 0;
            /***************************************************************/
            jniMgr.getCwmInformation(ID.SOFTWARE_VERSION_MESSAGE_ID,value,output);
            /***************************************************************/
            main = (float)output[0];
            sub = (((float)output[1]) / 100);

            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(ID.SOFTWARE_VERSION_MESSAGE_ID);
            cwmEvents.setSwVersion(main+sub);

            return cwmEvents;
        } else if(messageId == ID.SLEEP_REPORT_MESSAGE_ID){
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
            cwmEvents.setId(ID.SLEEP_REPORT_MESSAGE_ID);
            cwmEvents.setSleepLogLength(value.length);
            cwmEvents.setSleepCombined(value);
            cwmEvents.setSleepParser(convert);
            cwmEvents.setParserLength(convert.length);

            return cwmEvents;
        }
        else if(messageId == ID.READ_FLASH_COMMAND_ID){
            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(ID.READ_FLASH_COMMAND_ID);
            cwmEvents.setFlashSyncStatus(value[5] & 0xFF);
            return cwmEvents;
        }
        else if(messageId == ID.RECEIVED_FLASH_COMMAND_ID){
            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(ID.RECEIVED_FLASH_COMMAND_ID);
            return cwmEvents;
        }
        else if(messageId == ID.REQUEST_MAX_LOG_PACKETS_ID){
            byte[] temp = new byte[4];
            int max_packets = 0;
            System.arraycopy(value, 9, temp, 0, 4);
            max_packets = ByteBuffer.wrap(temp).order(ByteOrder.LITTLE_ENDIAN).getInt();
            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(ID.REQUEST_MAX_LOG_PACKETS_ID);
            cwmEvents.setMaxByte(max_packets);
            return cwmEvents;
        }
        else if(messageId == ID.GESUTRE_EVENT_MESSAGE_ID){
            int[] output = new int[7];
            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(ID.GESUTRE_EVENT_MESSAGE_ID);
            jniMgr.getGestureListInfomation(ID.GESUTRE_EVENT_MESSAGE_ID,value,output);
            cwmEvents.setGestureList(output);
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
          private int tag;

        private Data(int type, int length, int idType, int messageID, byte[] value) {
            this.type = type;
            this.length = length;
            this.idType = idType;
            this.messageID = messageID;
            this.value = value;
            this.tag = 0;
        }
        private Data(int type, int length, int idType, int messageID, int tag, byte[] value) {
            this.type = type;
            this.length = length;
            this.idType = idType;
            this.messageID = messageID;
            this.tag = tag;
            this.value = value;
        }

        public int getLength(){return length;}

        public int getIdType(){
            return idType;
        }

        public int getMessageID(){
            if(idType == ID.ACK ||  idType == ID.NACK)
                return messageID;
            else
                return idType;
        }

        public int getTag(){return tag;}
        public byte[] getValue(){return value;}
    }

}
