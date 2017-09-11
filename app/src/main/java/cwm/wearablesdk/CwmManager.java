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

    InformationListener mListener = null;

    private final int NON_PENDING = 1;
    private final int PENDING = 2;

    final static int PACKET_SIZE = 20;

    private final Queue<Data> mOutPutQueue = new LinkedList<>();

    /******** protoco l************/
    private final int HEADER1 = 0xE6;
    private final int HEADER2 = 0x90;
    private final int ACK = 0xAC;
    private final int NACK = 0x15;
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

    private WearableService mService = null;
    private Context mContext = null;
    private Activity mActivity = null;
    private WearableServiceListener mStatusListener = null;
    private AckListener mAckListener = null;

    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_SELECT_DEVICE = 2;

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

    // interface -----------------------------------------------------------------------------------
    public interface InformationListener {
        void onGetCwmRunData(CwmInformation runInfo);
        void onGetCwmWalkData(CwmInformation walkInfo);
        void onGetBikeData(CwmInformation bikeInfo);
        void onGetHeartData(CwmInformation heartInfo);
        void onGetActivity(CwmInformation activityInfo);
        void onGetBattery(CwmInformation batteryInfo);
    } // onDataArrivalListener()

    public interface WearableServiceListener {
        void onConnected();
        void onDisconnected();
        void onServiceDiscovery(String deviceName, String deviceAddress);
        void onNotSupport();
    }

    public interface AckListener{
        void onSyncTimeAckArrival();
        void onSyncIntelligentAckArrival();
        void onSyncPersonInfoAckArrival();

    }

    public CwmManager(Activity activity, WearableServiceListener wListener,
                      InformationListener iLlistener, AckListener ackListener){

        mActivity = activity;
        mStatusListener = wListener;
        mListener = iLlistener;
        mAckListener = ackListener;

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


        if((rxBuffer[0] & 0xFF) == HEADER1 && (rxBuffer[1] & 0xFF) == HEADER2 && (((rxBuffer[3] & 0xFF) == ACK) || ((rxBuffer[3]) == NACK))) {
            packet_type = NON_PENDING;
            packet_length = rxBuffer[2] & 0xFF;
            packet_id_type = rxBuffer[3] & 0xFF;
            packet_message_id = rxBuffer[4] & 0xFF;
            packet = rxBuffer;
            Data data = new Data(packet_type, packet_length, packet_id_type, packet_message_id, packet);
            enqueue(data);
        }
        else if((rxBuffer[0] & 0xFF) == HEADER1 && (rxBuffer[1] & 0xFF) == HEADER2 && (rxBuffer[3] & 0xFF) != ACK && (rxBuffer[3] & 0xFF) != NACK){
            packet_type = NON_PENDING;
            packet_length = rxBuffer[2] & 0xFF;
            packet_id_type = rxBuffer[3] & 0xFF;
            packet_message_id = 0;
            packet = rxBuffer;
            Data data = new Data(packet_type, packet_length, packet_id_type, packet_message_id, packet);
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
        mService.connect(address);
    }

    public void CwmBleDisconnect(){
        mService.disconnect();
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
            int[] body = new int[4];

            body[0] = bodySettings.getOld();
            body[1] = bodySettings.getHight();
            if (bodySettings.getSex() == 'm' || bodySettings.getSex() == 'M')
                body[2] = 1;
            else
                body[2] = 2;
            body[3] = bodySettings.getWeight();
            /*******************************************************/
            int checksum = 0xE6 + 0x90 + 0x09 + 0x14 + body[2] + body[0] + body[1] + body[3];
            byte[] command = {(byte) 0xE6, (byte) 0x90, (byte) 0x09, (byte) 0x14, (byte) body[2], (byte) body[0],
                    (byte) body[1], (byte) body[3], (byte) checksum};
            /*********************************************************/
            mService.writeRXCharacteristic(command);
        }
    }

    public void CwmSyncIntelligentSettings(){
        if(mConnectStatus == true) {
            boolean[] feature = new boolean[5];
            int goal = intelligentSettings.getGoal();
            feature[0] = intelligentSettings.getSedtentary();
            feature[1] = intelligentSettings.getHangUp();
            feature[2] = intelligentSettings.getOnWear();
            feature[3] = intelligentSettings.getDoubleTap();
            feature[4] = intelligentSettings.getWristSwitch();

            /***************************************************************/
            int features = 0;
            int onWearMask = 32;
            int sedentaryRemindMask = 8;
            int handUpMask = 4;
            int tapMask = 2;
            int wristMask = 1;
            int targetStepL = 0;
            int targetStepH = 0;
            if (feature[0] == true)
                features = features | sedentaryRemindMask;
            else
                features = features & ~(sedentaryRemindMask);
            if (feature[1] == true)
                features = features | handUpMask;
            else
                features = features & ~(handUpMask);
            if (feature[2] == true)
                features = features | onWearMask;
            else
                features = features & ~(onWearMask);
            if (feature[3] == true)
                features = features | tapMask;
            else
                features = features & ~(tapMask);
            if (feature[4] == true)
                features = features | wristMask;
            else
                features = features & ~(wristMask);

            targetStepL = goal & 0xFF;
            targetStepH = (goal >> 8) & 0xFF;

            int checksum = 0xE6 + 0x90 + 0x09 + 0x12 + features + 0x00 + targetStepL + targetStepH;
            byte[] command = {(byte) 0xE6, (byte) 0x90, (byte) 0x09, (byte) 0x12, (byte) features, (byte) 0x0,
                    (byte) targetStepL, (byte) targetStepH, (byte) checksum};
            /***********************************************************************************/
            mService.writeRXCharacteristic(command);
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
        time[4] = c.get(Calendar.HOUR);
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

        mService.writeRXCharacteristic(command);

    }

    public void CwmRequestBattery(){
        byte[] command = new byte[5];
        /*******************************************************************************/
        jniMgr.getRequestBatteryCommand(command);
         /******************************************************************************/
         mService.writeRXCharacteristic(command);
    }

    private void enqueue(Data data){
        if (data.type == NON_PENDING && data.length <= PACKET_SIZE) {
            mOutPutQueue.add(data);
        }
    }
    private void parser(){
        if(mOutPutQueue.size() != 0){
            Data data = mOutPutQueue.poll();
            if(data.getIdType() == ACK) {
                int id = data.getMessageID();
                switch (id) {
                    case SYNC_TIME_RESPONSE_ID:
                        mAckListener.onSyncTimeAckArrival();
                        break;
                    case BODY_PARAMETER_RESPONSE_ID:
                        mAckListener.onSyncPersonInfoAckArrival();
                        break;
                    case INTELLIGENT_FEATURE_RESPONSE_ID:
                        mAckListener.onSyncIntelligentAckArrival();
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
                CwmInformation cwmInfo;
                switch (id){
                    case MOTION_DATA_REPORT_MESSAGE_ID:
                         cwmInfo = getInfomation(MOTION_DATA_REPORT_MESSAGE_ID, value);
                        mListener.onGetCwmWalkData(cwmInfo);
                        break;
                    case BATTERY_STATUS_REPORT_MESSAGE_ID:
                        cwmInfo = getInfomation(BATTERY_STATUS_REPORT_MESSAGE_ID, value);
                        mListener.onGetBattery(cwmInfo);
                        break;
                    case TAP_EVENT_MESSAGE_ID:
                        cwmInfo = getInfomation(TAP_EVENT_MESSAGE_ID, value);
                        mListener.onGetActivity(cwmInfo);
                        break;
                    case WRIST_SCROLL_EVENT_MESSAGE_ID:
                        cwmInfo = getInfomation(WRIST_SCROLL_EVENT_MESSAGE_ID, value);
                        mListener.onGetActivity(cwmInfo);
                        break;
                    case SEDENTARY_EVENT_MESSAGE_ID:
                        cwmInfo = getInfomation(SEDENTARY_EVENT_MESSAGE_ID, value);
                        mListener.onGetActivity(cwmInfo);
                        break;
                    case HART_RATE_EVENT_MESSAGE_ID:
                        cwmInfo = getInfomation(HART_RATE_EVENT_MESSAGE_ID, value);
                        mListener.onGetHeartData(cwmInfo);
                        break;
                    default:
                        break;
                }
            }
        }
    }
    private CwmInformation getInfomation(int messageId, byte[] value){
        if(messageId == MOTION_DATA_REPORT_MESSAGE_ID ){
/****************************************************************************/
            byte[] dest = new byte[4];

            System.arraycopy(value, 4, dest, 0, 4);
            int walkStep = (int) ByteBuffer.wrap(dest).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            System.arraycopy(value, 8, dest, 0, 4);
            int distance = (int)ByteBuffer.wrap(dest).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            System.arraycopy(value, 12, dest, 0, 4);
            int calories = (int)ByteBuffer.wrap(dest).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            int status = value[16] & 0xFF;
            /***********************************************************************/
            CwmInformation cwmInfo = new CwmInformation();
            cwmInfo.setId(MOTION_DATA_REPORT_MESSAGE_ID);
            cwmInfo.setWalkStep(walkStep);
            cwmInfo.setDistance(distance);
            cwmInfo.setCalories(calories);
            cwmInfo.setStatus(status);
            return cwmInfo;
        }
        else if(messageId == BATTERY_STATUS_REPORT_MESSAGE_ID){
            CwmInformation cwmInfo = new CwmInformation();
            /*******************************************************/
            cwmInfo.setBattery(value[4] & 0xFF);
            return cwmInfo;
        }
        else if(messageId == TAP_EVENT_MESSAGE_ID){
            CwmInformation cwmInfo = new CwmInformation();
            cwmInfo.setId(TAP_EVENT_MESSAGE_ID);
            return cwmInfo;
        }
        else if(messageId == WRIST_SCROLL_EVENT_MESSAGE_ID){
            CwmInformation cwmInfo = new CwmInformation();
            cwmInfo.setId(WRIST_SCROLL_EVENT_MESSAGE_ID);
            return cwmInfo;
        }
        else if(messageId == HART_RATE_EVENT_MESSAGE_ID){
            int heartBeat = 0;
            byte[] dest = new byte[4];

            System.arraycopy(value, 4, dest, 0, 4);
            heartBeat = (int) ByteBuffer.wrap(dest).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            CwmInformation cwmInfo = new CwmInformation();
            cwmInfo.setId(HART_RATE_EVENT_MESSAGE_ID);
            cwmInfo.setHeartBeat(heartBeat);
            return cwmInfo;
        }
        else if(messageId == SEDENTARY_EVENT_MESSAGE_ID){
            CwmInformation cwmInfo = new CwmInformation();
            cwmInfo.setId(SEDENTARY_EVENT_MESSAGE_ID);
            return cwmInfo;
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
        private Data(int type, byte[] value){
            this.type = type;
            this.length = 0;
            this.idType = 0;
            this.messageID = 0;
            this.value = value;
        }
        private Data(int type, int length, int id, byte[] value){
            this.type = type;
            this.length = length;
            this.idType = id;
            this.messageID = 0;
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
