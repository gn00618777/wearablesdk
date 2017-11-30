package cwm.wearablesdk;

/**
 * Created by user on 2017/11/27.
 */
import android.util.Log;

import java.util.Calendar;

import cwm.wearablesdk.CwmManager.ErrorListener;

public class Task implements Runnable{
    static int id;
    static int time_expected;
    static int flashSyncType;

    SensorsRequestParameters sensorRequestObj;

    public SensorsRequestParameters getParametersObj(){
        return sensorRequestObj;
    }
    public void setSyncType(int type){
        flashSyncType = type;
    }
    @Override
    public void run(){
        if(CwmManager.mCurrentTask.getCommand() != ID.READ_FLASH_COMMAND_ID) {
            ErrorEvents errorEvents = new ErrorEvents();
            errorEvents.setId(0x01); //header lost
            errorEvents.setCommand(CwmManager.mCurrentTask.getCommand());
            CwmManager.mErrorListener.onErrorArrival(errorEvents);
            CwmManager.isTaskHasComplete = true;
        }
        else if(CwmManager.mCurrentTask.getCommand() == ID.READ_FLASH_COMMAND_ID){
            //CwmFlashSyncFail();
        }
    }

    public void doWork(){
        byte[] command;
        byte[] command1;
        switch (id){
            case ID.BATTERY_STATUS_REPORT_MESSAGE_ID:
                command = new byte[5];
                /*******************************************************************************/
                CwmManager.jniMgr.getRequestBatteryCommand(command);
                /******************************************************************************/
                if(CwmManager.mConnectStatus != false) {
                    CwmManager.mService.writeRXCharacteristic(command);
                    CwmManager.taskReceivedHandler.postDelayed(CwmManager.mCurrentTask, CwmManager.mCurrentTask.getTime());
                }
                break;
            case ID.SOFTWARE_VERSION_MESSAGE_ID:
                command = new byte[5];
                /*******************************************************************************/
                CwmManager.jniMgr.getRequestSwVersionCommand(command);
                /*******************************************************************************/
                if(CwmManager.mConnectStatus != false) {
                    CwmManager.mService.writeRXCharacteristic(command);
                    CwmManager.taskReceivedHandler.postDelayed(CwmManager.mCurrentTask, CwmManager.mCurrentTask.getTime());
                }
                break;
            case ID.SYNC_TIME_RESPONSE_ID:
                int[] time = new int[7];
                command = new byte[12];
                boolean isFirstSunday;
                Calendar c = Calendar.getInstance();
                time[0] = c.get(Calendar.YEAR) - 2000;
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
                CwmManager.jniMgr.getSyncCurrentCommand(time, command);
                /****************************************/
                if(CwmManager.mConnectStatus != false) {
                    CwmManager.mService.writeRXCharacteristic(command);
                    CwmManager.taskReceivedHandler.postDelayed(CwmManager.mCurrentTask, CwmManager.mCurrentTask.getTime());
                }
                break;

            case ID.BODY_PARAMETER_RESPONSE_ID:
                if(CwmManager.mConnectStatus == true) {
                    command = new byte[9];
                    int[] body = new int[4];

                    body[0] = CwmManager.bodySettings.getOld();
                    body[1] = CwmManager.bodySettings.getHight();
                    if (CwmManager.bodySettings.getSex() == 'm' || CwmManager.bodySettings.getSex() == 'M')
                        body[2] = 1;
                    else
                        body[2] = 2;
                    body[3] = CwmManager.bodySettings.getWeight();

                    /*******************************************************/
                    CwmManager.jniMgr.getSyncBodyCommandCommand(body,command);
                    /*********************************************************/
                    if(CwmManager.mConnectStatus != false) {
                        CwmManager.mService.writeRXCharacteristic(command);
                        CwmManager.taskReceivedHandler.postDelayed(CwmManager.mCurrentTask, CwmManager.mCurrentTask.getTime());
                    }
                }
                break;
            case ID.INTELLIGENT_FEATURE_RESPONSE_ID:
                if(CwmManager.mConnectStatus == true) {
                    command = new byte[9];
                    command1 = new byte[7];
                    boolean[] feature = new boolean[7];
                    int goal = CwmManager.intelligentSettings.getGoal();
                    int remindTIme = CwmManager.intelligentSettings.getTime();
                    feature[0] = CwmManager.intelligentSettings.getSedtentary();
                    feature[1] = CwmManager.intelligentSettings.getHangUp();
                    feature[2] = CwmManager.intelligentSettings.getOnWear();
                    feature[3] = CwmManager.intelligentSettings.getDoubleTap();
                    feature[4] = CwmManager.intelligentSettings.getWristSwitch();
                    feature[5] = CwmManager.intelligentSettings.getShakeSwitch();
                    feature[6] = CwmManager.intelligentSettings.getSignificant();

                    /***************************************************************/
                    CwmManager.jniMgr.getSyncIntelligentCommand(feature, goal, command);
                    CwmManager.jniMgr.getSedentaryRemindTimeCommand(remindTIme, command1);
                    /***********************************************************************************/
                    if(CwmManager.mConnectStatus != false) {
                        CwmManager.mService.writeRXCharacteristic(command1);
                        CwmManager.mService.writeRXCharacteristic(command);
                        CwmManager.taskReceivedHandler.postDelayed(CwmManager.mCurrentTask, CwmManager.mCurrentTask.getTime());
                    }
                }
                break;

            case ID.SLEEP_REPORT_MESSAGE_ID:
                command = new byte[5];
                CwmManager.jniMgr.getSleepLogCommand(command);
                if(CwmManager.mConnectStatus != false) {
                    CwmManager.mService.writeRXCharacteristic(command);
                    CwmManager.taskReceivedHandler.postDelayed(CwmManager.mCurrentTask, CwmManager.mCurrentTask.getTime());
                }
                break;

            case ID.SWITCH_OTA_ID:
                command = new byte[5];
                /*******************************************************************************/
                CwmManager.jniMgr.getSwitchOTACommand(command);
                /*******************************************************************************/
                if(CwmManager.mConnectStatus != false) {
                    CwmManager.mService.writeRXCharacteristic(command);
                    CwmManager.taskReceivedHandler.postDelayed(CwmManager.mCurrentTask, CwmManager.mCurrentTask.getTime());
                }
                break;

            case ID.TABATA_COMMAND_ID:
                command = new byte[9];
                /********************************************************************************/
                CwmManager.jniMgr.getTabataCommand(CwmManager.ITEMS.TABATA_INIT.ordinal(), 0, 0, 0, command);
                /********************************************************************************/
                if((CwmManager.mConnectStatus != false)){
                    CwmManager.mService.writeRXCharacteristic(command);
                    CwmManager.taskReceivedHandler.postDelayed(CwmManager.mCurrentTask, CwmManager.mCurrentTask.getTime());
                }
                break;

            case ID.READ_FLASH_COMMAND_ID:
                command = new byte[6];
                if(flashSyncType == CwmManager.FLASH_SYNC_TYPE.SYNC_START.ordinal()){
                    CwmManager.jniMgr.getReadFlashCommand(CwmManager.FLASH_SYNC_TYPE.SYNC_START.ordinal(), command);
                }
                else if(flashSyncType == CwmManager.FLASH_SYNC_TYPE.SYNC_SUCCESS.ordinal()){
                    CwmManager.jniMgr.getReadFlashCommand(CwmManager.FLASH_SYNC_TYPE.SYNC_SUCCESS.ordinal(), command);
                }
                else if(flashSyncType == CwmManager.FLASH_SYNC_TYPE.SYNC_FAIL.ordinal()){
                    CwmManager.jniMgr.getReadFlashCommand(CwmManager.FLASH_SYNC_TYPE.SYNC_FAIL.ordinal(), command);
                }
                else if(flashSyncType == CwmManager.FLASH_SYNC_TYPE.SYNC_ABORT.ordinal()){
                    CwmManager.jniMgr.getReadFlashCommand(CwmManager.FLASH_SYNC_TYPE.SYNC_ABORT.ordinal(), command);
                }
                else if(flashSyncType == CwmManager.FLASH_SYNC_TYPE.SYNC_DONE.ordinal()){
                    CwmManager.jniMgr.getReadFlashCommand(CwmManager.FLASH_SYNC_TYPE.SYNC_DONE.ordinal(), command);
                }
                else if(flashSyncType == CwmManager.FLASH_SYNC_TYPE.SYNC_ERASE.ordinal()){
                    CwmManager.jniMgr.getReadFlashCommand(CwmManager.FLASH_SYNC_TYPE.SYNC_ERASE.ordinal(), command);
                }
                if((CwmManager.mConnectStatus != false)){
                    CwmManager.mService.writeRXCharacteristic(command);
                    CwmManager.taskReceivedHandler.postDelayed(CwmManager.mCurrentTask, CwmManager.mCurrentTask.getTime());
                }
                break;

            case ID.REQUEST_MAX_LOG_PACKETS_ID:
                command = new byte[5];
                CwmManager.jniMgr.getRequestMaxLogPacketsCommand(command);
                if(CwmManager.mConnectStatus != false) {
                    CwmManager.mService.writeRXCharacteristic(command);
                    CwmManager.taskReceivedHandler.postDelayed(CwmManager.mCurrentTask, CwmManager.mCurrentTask.getTime());
                }
                break;

            case ID.REQUEST_GESTURE_LIST:
                command = new byte[5];
                CwmManager.jniMgr.getGestureListCommand(command);
                if(CwmManager.mConnectStatus != false){
                    CwmManager.mService.writeRXCharacteristic(command);
                    CwmManager.taskReceivedHandler.postDelayed(CwmManager.mCurrentTask, CwmManager.mCurrentTask.getTime());
                }
                break;
            case ID.RECORD_SENSOR_ID:
                int sensorType = sensorRequestObj.getSensorType();
                int odrType = sensorRequestObj.getOdrType();
                int sensorStatus = sensorRequestObj.getSensorStatus();
                command = new byte[8];
                CwmManager.jniMgr.getRecordSensorToFlashCommand(sensorType, odrType, sensorStatus, command);
                if(CwmManager.mConnectStatus != false){
                    CwmManager.mService.writeRXCharacteristic(command);
                    CwmManager.taskReceivedHandler.postDelayed(CwmManager.mCurrentTask, CwmManager.mCurrentTask.getTime());
                }
                break;
            case ID.REQUEST_ERASE_PROGRESS_ID:
                command = new byte[5];
                if(CwmManager.mConnectStatus != false){
                    CwmManager.mService.writeRXCharacteristic(command);
                    CwmManager.taskReceivedHandler.postDelayed(CwmManager.mCurrentTask, CwmManager.mCurrentTask.getTime());
                }
                break;
            case ID.CALIBRATE_COMMAND_ID:
                command = new byte[6];
                /********************************************************************************/
                CwmManager.jniMgr.getEnableCalibrateCommand(sensorRequestObj.getSensorType(), command);
                /********************************************************************************/
                if((CwmManager.mConnectStatus != false)){
                    CwmManager.mService.writeRXCharacteristic(command);
                }
                break;

        }
    }

    public Task(){
        id = 0;
        time_expected = 0;
    }

    public int getTime(){
        return time_expected*1000;
    }

    public Task(int command, int time, int type){
        this.id = command;
        time_expected = time;
        flashSyncType = 0;
        if(type == CwmManager.PARAMETERS_TYPE.SENSORREQUEST.ordinal())
            sensorRequestObj = new SensorsRequestParameters();
    }

    public int getCommand(){
        return id;
    }

}
