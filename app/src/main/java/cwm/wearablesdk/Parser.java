package cwm.wearablesdk;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import cwm.wearablesdk.constants.ID;
import cwm.wearablesdk.constants.Type;
import cwm.wearablesdk.events.AckEvents;
import cwm.wearablesdk.events.CwmEvents;
import cwm.wearablesdk.events.ErrorEvents;
import cwm.wearablesdk.handler.AckHandler;
import cwm.wearablesdk.handler.BleReceiver;
import cwm.wearablesdk.settings.AlarmSetting;
import cwm.wearablesdk.settings.BodySettings;
import cwm.wearablesdk.settings.SystemSetting;
import cwm.wearablesdk.settings.IntelligentSettings;

/**
 * Created by user on 2017/11/22.
 */

public class Parser {
    private AckHandler mAckHandler;
    final static float M_PI = 3.14159265358979323846f;
    final static float GYRO_CONVERT_2000DPS = (float)(M_PI/((float)16.4 * (float)180));

    final static float GRAVITY_EARTH = 9.8066f;
    final static float ACC_RANGE_16G_CONVERT = (GRAVITY_EARTH*(1.0f/2048.0f));
    final float ADXL362_8G_CONVERTER = GRAVITY_EARTH*(1.0f/250.0f);

    /*History usage*/
    int packetsNumber = 0;
    int currentPackets = 0;

    public Parser(){
        mAckHandler = new AckHandler();
    }

    public CwmEvents parsePayload(Payload data){
        int msg_type = data.getMsgCmdType();
        int message_id;
        int sensor_tag;
        int j = 0;

        CwmEvents cwmEvent = null;

        byte[] temp = new byte[2];
        byte[] temp1 = new byte[4];
        short[] output_16 = new short[6];
        float[] output_32 = new float[3];

        byte[] packet = data.getPacket();

        //Log.d("bernie","sdk msg_type:"+Integer.toString(msg_type));
        switch (msg_type){
            case Type.CHECKSUM_ERROR:
                ErrorEvents errorEvents = new ErrorEvents();
                errorEvents.setErrorId(ID.CHECKSUM_ERROR);
                cwmEvent = new CwmEvents();
                cwmEvent.setEventType(Type.ERROR_EVENT);
                cwmEvent.setErrorEvent(errorEvents);
                break;
            case Type.ACK_INFORMATION:
                AckEvents ackEvents = mAckHandler.processAck(data);
                cwmEvent = new CwmEvents();
                cwmEvent.setEventType(Type.ACK_EVENT);
                cwmEvent.setAckEvent(ackEvents);
                break;
            case Type.SYSTTEM_INFORMATION:
                message_id = data.getMsgCmdId();
                switch (message_id){
                    case ID.USER_CONFIG_INFO:
                        LongTask.longTaskReceivedHandler.removeCallbacks(LongTask.currentLongTask);
                        BleReceiver.hasLongTask = false;
                        cwmEvent = new CwmEvents();
                        cwmEvent.setMsgType(msg_type);
                        cwmEvent.setMessageID(message_id);
                        cwmEvent.setEventType(Type.EVENT);

                        SystemSetting system = new SystemSetting();

                        int osType = packet[8] & 0xFF;
                        int timeFormat = packet[9] & 0xFF;
                        int historyDetect = packet[10] & 0xFF;
                        int screenTimeout = packet[11] & 0xFF;
                        int screens = packet[12] & 0xFF;
                        int functions_I = packet[14] & 0xFF;
                        int functions_II = packet[15] & 0xFF;
                        int functions = functions_I | functions_II << 8;
                        int gestrue_I = packet[16] & 0xFF;
                        int gesture_II = packet[17] & 0xFF;
                        int gesutre = gestrue_I | gesture_II << 8;
                        int sleepStartTime = packet[68] & 0xFF;
                        int sleepStopTime = packet[69] & 0xFF;
                        int noDisturbStartTime = packet[73] & 0xFF;
                        int noDisturbStopTime = packet[74] & 0xFF;
                        int brightness = packet[76] & 0xFF;

                        system.setOSType(osType);
                        system.setTimeFormat(timeFormat);
                        system.setHistoryDetectPeriod(historyDetect);
                        system.setScreenTimeOut(screenTimeout);
                        system.setScreens(screens);
                        system.setFunctions(functions);
                        system.setNoDisturbInterval(noDisturbStartTime, noDisturbStopTime);
                        system.setSleepTimeInterval(sleepStartTime, sleepStopTime);
                        system.setBrightness(brightness);
                        cwmEvent.setSystem(system);

                        int gender = (int)packet[28];
                        int age = packet[29] & 0xFF;
                        int height = packet[30] & 0xFF;
                        int weight = packet[31] & 0xFF;
                        int time = packet[72] & 0xFF;

                        int alarm1Week = packet[44] & 0xFF;
                        int alarm1Vib = packet[45] & 0xFF;
                        int alarm1Hour = packet[46] & 0xFF;
                        int alarm1Minute = packet[47] & 0xFF;
                        int alarm2Week = packet[48] & 0xFF;
                        int alarm2Vib = packet[49] & 0xFF;
                        int alarm2Hour = packet[50] & 0xFF;
                        int alarm2Minute = packet[51] & 0xFF;
                        int alarm3Week = packet[52] & 0xFF;
                        int alarm3Vib = packet[53] & 0xFF;
                        int alarm3Hour = packet[54] & 0xFF;
                        int alarm3Minute = packet[55] & 0xFF;
                        int alarm4Week = packet[56] & 0xFF;
                        int alarm4Vib = packet[57] & 0xFF;
                        int alarm4Hour = packet[58] & 0xFF;
                        int alarm4Minute = packet[59] & 0xFF;
                        int alarm5Week = packet[60] & 0xFF;
                        int alarm5Vib = packet[61] & 0xFF;
                        int alarm5Hour = packet[62] & 0xFF;
                        int alarm5Minute = packet[63] & 0xFF;
                        int alarm6Week = packet[64] & 0xFF;
                        int alarm6Vib = packet[65] & 0xFF;
                        int alarm6Hour = packet[66] & 0xFF;
                        int alarm6Minute = packet[67] & 0xFF;

                        AlarmSetting alarmSetting = new AlarmSetting();
                        alarmSetting.setWeek(alarm1Week,1);
                        alarmSetting.setWeek(alarm2Week,2);
                        alarmSetting.setWeek(alarm3Week,3);
                        alarmSetting.setWeek(alarm4Week,4);
                        alarmSetting.setWeek(alarm5Week,5);
                        alarmSetting.setWeek(alarm6Week,6);

                        alarmSetting.setVibrate(alarm1Vib,1);
                        alarmSetting.setVibrate(alarm2Vib,2);
                        alarmSetting.setVibrate(alarm3Vib,3);
                        alarmSetting.setVibrate(alarm4Vib,4);
                        alarmSetting.setVibrate(alarm5Vib,5);
                        alarmSetting.setVibrate(alarm6Vib,6);

                        alarmSetting.setTime(alarm1Hour, alarm1Minute, 1);
                        alarmSetting.setTime(alarm2Hour, alarm2Minute, 2);
                        alarmSetting.setTime(alarm3Hour, alarm3Minute, 3);
                        alarmSetting.setTime(alarm4Hour, alarm4Minute, 4);
                        alarmSetting.setTime(alarm5Hour, alarm5Minute, 5);
                        alarmSetting.setTime(alarm6Hour, alarm6Minute, 6);

                        cwmEvent.setAlarm(alarmSetting);

                        System.arraycopy(packet, 32, temp1, 0, 4);
                        int goal = (int)ByteBuffer.wrap(temp1).order(ByteOrder.LITTLE_ENDIAN).getInt();

                        BodySettings  body = new BodySettings();

                        body.setSex(gender);
                        body.setOld(age);
                        body.setHight(height);
                        body.setWeight(weight);

                        cwmEvent.setBody(body);

                        IntelligentSettings intelligent = new IntelligentSettings();

                        if((packet[16] & 0x01) != 0)
                            intelligent.enableGesture(Type.GESTURE.STEP_COINTER.ordinal());
                        else
                            intelligent.enableGesture(Type.GESTURE.STEP_COINTER.ordinal());

                        if((packet[16] & 0x02) != 0)
                            intelligent.enableGesture(Type.GESTURE.CUSTOMISED_PEDOMETER.ordinal());
                        else
                            intelligent.enableGesture(Type.GESTURE.CUSTOMISED_PEDOMETER.ordinal());

                        if((packet[16] & 0x04) != 0)
                            intelligent.enableGesture(Type.GESTURE.SIGNIFICANT_MOTION.ordinal());
                        else
                            intelligent.enableGesture(Type.GESTURE.SIGNIFICANT_MOTION.ordinal());

                        if((packet[16] & 0x08) != 0) //Hand up
                           intelligent.enableGesture(Type.GESTURE.HAND_UP.ordinal());
                        else
                           intelligent.disableGesture(Type.GESTURE.HAND_UP.ordinal());

                        if((packet[16] & 0x10) != 0)
                            intelligent.enableGesture(Type.GESTURE.TAP.ordinal());
                        else
                            intelligent.disableGesture(Type.GESTURE.TAP.ordinal());

                        if((packet[16] & 0x20) != 0)
                            intelligent.enableGesture(Type.GESTURE.WATCH_TAKE_OFF.ordinal());
                        else
                            intelligent.disableGesture(Type.GESTURE.WATCH_TAKE_OFF.ordinal());

                        if((packet[16] & 0x40) != 0)
                            intelligent.enableGesture(Type.GESTURE.ACTIVITY_RECOGNITION.ordinal());
                        else
                            intelligent.disableGesture(Type.GESTURE.ACTIVITY_RECOGNITION.ordinal());

                        if((packet[16] & 0x80) != 0)
                            intelligent.enableGesture(Type.GESTURE.SLEEPING.ordinal());
                        else
                            intelligent.disableGesture(Type.GESTURE.SLEEPING.ordinal());

                        if((packet[17] & 0x01) != 0)
                            intelligent.enableGesture(Type.GESTURE.SEDENTARY.ordinal());
                        else
                            intelligent.disableGesture(Type.GESTURE.SEDENTARY.ordinal());

                        if((packet[17] & 0x02) != 0)
                            intelligent.enableGesture(Type.GESTURE.WRIST_SCROLL.ordinal());
                        else
                            intelligent.disableGesture(Type.GESTURE.WRIST_SCROLL.ordinal());

                        if((packet[17] & 0x04) != 0)
                            intelligent.enableGesture(Type.GESTURE.SHAKE.ordinal());
                        else
                            intelligent.disableGesture(Type.GESTURE.SHAKE.ordinal());

                        if((packet[17] & 0x08) != 0)
                            intelligent.enableGesture(Type.GESTURE.FALL.ordinal());
                        else
                            intelligent.disableGesture(Type.GESTURE.FALL.ordinal());

                        if((packet[17] & 0x10) != 0)
                            intelligent.enableGesture(Type.GESTURE.FLOOR_CLIMBED.ordinal());
                        else
                            intelligent.disableGesture(Type.GESTURE.FLOOR_CLIMBED.ordinal());

                        if((packet[17] & 0x20) != 0)
                            intelligent.enableGesture(Type.GESTURE.SKIPPING.ordinal());
                        else
                            intelligent.disableGesture(Type.GESTURE.SKIPPING.ordinal());

                        intelligent.setGestureValue(gesutre);
                        intelligent.setSedentaryTime(time);
                        intelligent.setGoal(goal);

                        cwmEvent.setIntelligent(intelligent);

                        Log.d("bernie","sdk osType:"+Integer.toString(osType));
                        Log.d("bernie","sdk timeFormat:"+Integer.toString(timeFormat));
                        Log.d("bernie","sdk historyDetect:"+Integer.toString(historyDetect));
                        Log.d("bernie","sdk screenTimeOut:"+Integer.toString(screenTimeout));
                        Log.d("bernie","sdk screens:"+Integer.toString(screens));
                        Log.d("bernie","sdk functionI:"+Integer.toString(functions_I));
                        Log.d("bernie","sdk functions:"+Integer.toString(functions));
                        Log.d("bernie","sdk gesture:"+Integer.toString(gesutre));
                        Log.d("bernie","sdk sleepStartTime:"+Integer.toString(sleepStartTime));
                        Log.d("bernie","sdk sleepStopTime:"+Integer.toString(sleepStopTime));
                        Log.d("bernie","sdk noDisrurbStart:"+Integer.toString(noDisturbStartTime));
                        Log.d("bernie","sdk noDistrbStop:"+Integer.toString(noDisturbStopTime));
                        Log.d("bernie","sdk brigtness:"+Integer.toString(brightness));
                        Log.d("bernie","sdk gender:"+Integer.toString(gender));
                        Log.d("bernie","sdk age:"+Integer.toString(age));
                        Log.d("bernie","sdk height:"+Integer.toString(height));
                        Log.d("bernie","sdk weight:"+Integer.toString(weight));
                        Log.d("bernie","sdk goal:"+Integer.toString(goal));
                        Log.d("bernie","sdk sedentary time:"+Integer.toString(time));
                        Log.d("bernie","sdk alarm1 week:"+Integer.toString(alarm1Week)+" vib:"+Integer.toString(alarm1Vib)+" hour:"+Integer.toString(alarm1Hour)+" minute:"+Integer.toString(alarm1Minute));
                        Log.d("bernie","sdk alarm2 week:"+Integer.toString(alarm2Week)+" vib:"+Integer.toString(alarm2Vib)+" hour:"+Integer.toString(alarm2Hour)+" minute:"+Integer.toString(alarm2Minute));
                        Log.d("bernie","sdk alarm3 week:"+Integer.toString(alarm3Week)+" vib:"+Integer.toString(alarm3Vib)+" hour:"+Integer.toString(alarm3Hour)+" minute:"+Integer.toString(alarm3Minute));
                        Log.d("bernie","sdk alarm4 week:"+Integer.toString(alarm4Week)+" vib:"+Integer.toString(alarm4Vib)+" hour:"+Integer.toString(alarm4Hour)+" minute:"+Integer.toString(alarm4Minute));
                        Log.d("bernie","sdk alarm5 week:"+Integer.toString(alarm5Week)+" vib:"+Integer.toString(alarm5Vib)+" hour:"+Integer.toString(alarm5Hour)+" minute:"+Integer.toString(alarm5Minute));
                        Log.d("bernie","sdk alarm6 week:"+Integer.toString(alarm6Week)+" vib:"+Integer.toString(alarm6Vib)+" hour:"+Integer.toString(alarm6Hour)+" minute:"+Integer.toString(alarm6Minute));

                        //for(int i = 0 ; i < packet.length ; i++ )
                         //   Log.d("bernie varify", "i: "+Integer.toString(i)+" "+Integer.toHexString(packet[i] & 0xFF));

                        break;
                    case ID.BATTERY_INFO:
                        int battery = (int)packet[3];

                        cwmEvent = new CwmEvents();
                        cwmEvent.setEventType(Type.EVENT);
                        cwmEvent.setMsgType(msg_type);
                        cwmEvent.setMessageID(message_id);
                        cwmEvent.setBattery(battery);

                        break;
                    case ID.DEVICE_VERSION_INFO:
                        float main = (float)packet[5];
                        float sub = (((float)packet[6]) / 100);

                        cwmEvent = new CwmEvents();
                        cwmEvent.setEventType(Type.EVENT);
                        cwmEvent.setMsgType(msg_type);
                        cwmEvent.setMessageID(message_id);
                        cwmEvent.setVersion(main+sub);

                        break;
                    case ID.CURRENT:
                        LongTask.longTaskReceivedHandler.removeCallbacks(LongTask.currentLongTask);
                        BleReceiver.hasLongTask = false;

                        byte[] lifeTemp1 = new byte[56];
                        int unit_life_data1 = 56;
                        System.arraycopy(packet, 4, lifeTemp1, 0, unit_life_data1);
                        byte[] fourByteTemp1 = new byte[4];
                        byte[] twoByteTemp1 = new byte[2];

                        int timeStamp1 = 0;
                        int stepCount1 = 0;
                        int distance1 = 0;
                        int calorie1 = 0;
                        int hearRate1 = 0;
                        int minHeartRate1 = 0;
                        int maxHeartRate1 = 0;
                        int sedentaryTriggerTime1 = 0;
                        int batteryLevel1 = 0;
                        int batteryCharge1 = 0;
                        int notificationCount1 = 0;
                        int displayOnTime1 = 0;
                        int vibOnTime1 = 0;
                        int bleSendCount1 = 0;
                        int bleReceivedCount1 = 0;
                        int tpCount1 = 0;
                        int tabataActionTime1 = 0;
                        int handUpDownCount1 = 0;

                        System.arraycopy(lifeTemp1, 0, fourByteTemp1, 0, 4);
                        timeStamp1 = ByteBuffer.wrap(fourByteTemp1).order(ByteOrder.LITTLE_ENDIAN).getInt();
                        System.arraycopy(lifeTemp1, 4, fourByteTemp1, 0, 4);
                        stepCount1 = (int) ByteBuffer.wrap(fourByteTemp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        System.arraycopy(lifeTemp1, 8, fourByteTemp1, 0, 4);
                        distance1 = (int) ByteBuffer.wrap(fourByteTemp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        System.arraycopy(lifeTemp1, 12, fourByteTemp1, 0, 4);
                        calorie1 = (int) ByteBuffer.wrap(fourByteTemp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        System.arraycopy(lifeTemp1, 16, fourByteTemp1, 0, 4);
                        hearRate1 = (int) ByteBuffer.wrap(fourByteTemp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        System.arraycopy(lifeTemp1, 20, fourByteTemp1, 0, 4);
                        minHeartRate1 = (int) ByteBuffer.wrap(fourByteTemp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        System.arraycopy(lifeTemp1, 24, fourByteTemp1, 0, 4);
                        maxHeartRate1 = (int) ByteBuffer.wrap(fourByteTemp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        System.arraycopy(lifeTemp1, 28, fourByteTemp1, 0, 4);
                        sedentaryTriggerTime1 = ByteBuffer.wrap(fourByteTemp1).order(ByteOrder.LITTLE_ENDIAN).getInt();
                        System.arraycopy(lifeTemp1, 32, twoByteTemp1, 0, 2);
                        tpCount1 = ByteBuffer.wrap(twoByteTemp1).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;
                        System.arraycopy(lifeTemp1, 34, twoByteTemp1, 0, 2);
                        notificationCount1 = ByteBuffer.wrap(twoByteTemp1).order(ByteOrder.LITTLE_ENDIAN).getShort();
                        System.arraycopy(lifeTemp1, 36, fourByteTemp1, 0, 4);
                        displayOnTime1 =ByteBuffer.wrap(fourByteTemp1).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFFFFFF;
                        System.arraycopy(lifeTemp1, 40, fourByteTemp1, 0, 4);
                        vibOnTime1 = ByteBuffer.wrap(fourByteTemp1).order(ByteOrder.LITTLE_ENDIAN).getInt();
                        System.arraycopy(lifeTemp1, 44, twoByteTemp1, 0, 2);
                        bleSendCount1 = ByteBuffer.wrap(twoByteTemp1).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF ;
                        System.arraycopy(lifeTemp1, 46, twoByteTemp1, 0, 2);
                        bleReceivedCount1 = ByteBuffer.wrap(twoByteTemp1).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;
                        System.arraycopy(lifeTemp1, 48, fourByteTemp1, 0, 4);
                        tabataActionTime1 = ByteBuffer.wrap(fourByteTemp1).order(ByteOrder.LITTLE_ENDIAN).getInt()  & 0xFFFFFFFF;
                        batteryLevel1 = lifeTemp1[52] & 0xFF;
                        batteryCharge1 = lifeTemp1[53] & 0xFF;
                        handUpDownCount1 = lifeTemp1[54] & 0xFF;

                        LifeData life = new LifeData();
                        life.setTimeStamp(timeStamp1);
                        life.setStepCount(stepCount1);
                        life.setDistance(distance1);
                        life.setCalories(calorie1);
                        life.setHeartRate(hearRate1);
                        life.setMinHeartRate(minHeartRate1);
                        life.setMaxHeartRate(maxHeartRate1);
                        life.setSedentaryTriggerTime(sedentaryTriggerTime1);
                        life.setTpCount(tpCount1);
                        life.setNotificationCount(notificationCount1);
                        life.setDisplayOnTime(displayOnTime1);
                        life.setVibOnTime(vibOnTime1);
                        life.setBleSendCount(bleSendCount1);
                        life.setBleReceiveCount(bleReceivedCount1);
                        life.setTabataActionTime(tabataActionTime1);
                        life.setBatteryLevel(batteryLevel1);
                        life.setBatteryCharge(batteryCharge1);
                        life.setHandUpDownCount(handUpDownCount1);

                        cwmEvent = new CwmEvents();
                        cwmEvent.setEventType(Type.EVENT);
                        cwmEvent.setMsgType(msg_type);
                        cwmEvent.setMessageID(message_id);
                        cwmEvent.setLifData(life);

                        break;
                    default:
                        break;
                }
                break;
            case Type.SENSOR_GESTURE_REPORT_MESSAGE:
                message_id = data.getMsgCmdId();
                switch (message_id){
                    case ID.ACCELERATION_RAW_DATA_REPORT:
                        for (int i = 3; i < 9; i += 2) {
                            System.arraycopy(packet, i, temp, 0, 2);
                            output_16[j] = ByteBuffer.wrap(temp).order(ByteOrder.LITTLE_ENDIAN).getShort();
                            j++;
                        }
                        sensor_tag = (int)packet[9]; //ADXL or BMI160
                        if(sensor_tag == 1) {
                            output_32[0] = (float) output_16[0] * ACC_RANGE_16G_CONVERT;
                            output_32[1] = (float) output_16[1] * ACC_RANGE_16G_CONVERT;
                            output_32[2] = (float) output_16[2] * ACC_RANGE_16G_CONVERT;
                        }
                        else if(sensor_tag == 0){
                            output_32[0] = (float) output_16[0] *ADXL362_8G_CONVERTER;
                            output_32[1] = (float) output_16[1] *ADXL362_8G_CONVERTER;
                            output_32[2] = (float) output_16[2] *ADXL362_8G_CONVERTER;
                        }

                        cwmEvent = new CwmEvents();
                        cwmEvent.setEventType(Type.EVENT);
                        cwmEvent.setMsgType(msg_type);
                        cwmEvent.setMessageID(message_id);
                        cwmEvent.setAccSensor(output_32);
                        cwmEvent.setSensorTag(sensor_tag);

                        break;
                    case ID.GYRO_RAW_DATA_REPORT:
                         j = 0;
                         for (int i = 3; i < 9; i += 2) {
                            System.arraycopy(packet, i, temp, 0, 2);
                            output_16[j] = ByteBuffer.wrap(temp).order(ByteOrder.LITTLE_ENDIAN).getShort();
                            j++;
                        }
                        output_32[0] = (float) output_16[0] * GYRO_CONVERT_2000DPS;
                        output_32[1] = (float) output_16[1] * GYRO_CONVERT_2000DPS;
                        output_32[2] = (float) output_16[2] * GYRO_CONVERT_2000DPS;

                        sensor_tag = (int)packet[9]; //ADXL or BMI160

                        cwmEvent = new CwmEvents();
                        cwmEvent.setEventType(Type.EVENT);
                        cwmEvent.setMsgType(msg_type);
                        cwmEvent.setMessageID(message_id);
                        cwmEvent.setGyroSensor(output_32);
                        cwmEvent.setSensorTag(sensor_tag);

                        break;
                    case ID.HEART_RATE_RAW_DATA_REPORT:
                        System.arraycopy(packet, 3, temp1, 0, 4);
                        int heartBeat = (int)ByteBuffer.wrap(temp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        cwmEvent = new CwmEvents();
                        cwmEvent.setEventType(Type.EVENT);
                        cwmEvent.setMsgType(msg_type);
                        cwmEvent.setMessageID(message_id);
                        cwmEvent.setHeartSensor(heartBeat);

                        break;
                    case ID.WRIST_SCROLL_EVENT_RESPONSE_MESSAGE:
                    case ID.SEDENTARY_RESPONSE_MESSAGE:
                        cwmEvent = new CwmEvents();
                        cwmEvent.setEventType(Type.EVENT);
                        cwmEvent.setMsgType(msg_type);
                        cwmEvent.setMessageID(message_id);

                        break;
                    case ID.ACTIVITY_PEDOMETER_DATA_REPORT_MESSAGE:
                        System.arraycopy(packet, 3, temp1, 0, 4);
                        int step_count = (int)ByteBuffer.wrap(temp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        System.arraycopy(packet, 7, temp1, 0, 4);
                        int distance = (int)ByteBuffer.wrap(temp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        System.arraycopy(packet, 11, temp1, 0, 4);
                        int stepFreq = (int)ByteBuffer.wrap(temp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();

                        int status = (int)packet[15];

                        cwmEvent = new CwmEvents();
                        cwmEvent.setEventType(Type.EVENT);
                        cwmEvent.setMsgType(msg_type);
                        cwmEvent.setMessageID(message_id);
                        cwmEvent.setStepCount(step_count);
                        cwmEvent.setDistance(distance);
                        cwmEvent.setStepFreq(stepFreq);

                        cwmEvent.setStatus(status);

                        break;
                    case ID.TABATA_RESPONSE_MESSAGE:
                        int items = packet[3] & 0xFF;
                        Log.d("bernie","sdk item: "+Integer.toString(items));
                        int initialCode = packet[4] & 0xFF;
                        Log.d("bernie","sdk initial code:"+Integer.toString(initialCode));
                        int count = packet[5] & 0xFF;
                        Log.d("bernie","sdk count:"+Integer.toString(count));
                        System.arraycopy(packet, 7, temp1, 0, 4);
                        int strength = (int)ByteBuffer.wrap(temp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        Log.d("bernie","sdk strength: "+Integer.toString(strength));
                        System.arraycopy(packet, 11, temp1, 0, 4);
                        int tabataCaloris = (int)ByteBuffer.wrap(temp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        Log.d("bernie","sdk calories: "+Integer.toString(tabataCaloris));

                        cwmEvent = new CwmEvents();
                        cwmEvent.setEventType(Type.EVENT);
                        cwmEvent.setMsgType(msg_type);
                        cwmEvent.setMessageID(message_id);
                        cwmEvent.setExerciseItem(items);
                        cwmEvent.setDoItemCount(count);
                        cwmEvent.setTabataCalories(tabataCaloris);
                        cwmEvent.setTabataInitialCode(initialCode);
                        cwmEvent.setStrength(strength);

                        break;

                    case ID.SPORT_RESPONSE_MESSAGE:
                        System.arraycopy(packet, 4, temp1, 0, 4);
                        int stepCount = (int)ByteBuffer.wrap(temp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        System.arraycopy(packet, 8, temp1, 0, 4);
                        int stepFrequncy = (int)ByteBuffer.wrap(temp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        System.arraycopy(packet, 12, temp1, 0, 4);
                        int sportHeart = (int)ByteBuffer.wrap(temp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();

                        cwmEvent = new CwmEvents();
                        cwmEvent.setEventType(Type.EVENT);
                        cwmEvent.setMsgType(msg_type);
                        cwmEvent.setMessageID(message_id);
                        cwmEvent.setStepCount(stepCount);
                        cwmEvent.setStepFreq(stepFrequncy);
                        cwmEvent.setHeartSensor(sportHeart);

                        break;
                    default:
                        break;
                }
                break;
            case Type.HISTORY_DATA_RESPONSE:
                message_id = data.getMsgCmdId();
                int startPos = 0;
                int endPos = 0;
               // Log.d("bernie","sdk message_id:"+Integer.toString(message_id));
                switch (message_id) {
                    case ID.SLEEP_HISTORY:
                        LongTask.longTaskReceivedHandler.removeCallbacks(LongTask.currentLongTask);
                        BleReceiver.hasLongTask = false;
                        Log.d("bernie","sdk sleep history");
                         int unit_sleep_log = 2;//byte
                         int dataLength;
                        if (data.packet_type == Type.BLE_PAKAGE_TYPE.SHORT_MESSAGE.ordinal()) {
                            dataLength = packet.length - 1 - 2 - 1; // - 1 byte length  - msgtype - msgid - checksum
                            startPos = 3;
                        }
                        else {
                            dataLength = packet.length - 2 - 2 - 1; // - 2 byte length  - msgtype - msgid - checksum
                            startPos = 4;
                        }
                        endPos = packet.length - 3;
                         j = 0;
                         short[] convert = new short[dataLength / unit_sleep_log];
                         byte[] sleepTemp = new byte[unit_sleep_log];

                        StringBuilder sleepString = new StringBuilder();

                         for (int i = startPos; i <= endPos; i += unit_sleep_log) {
                              System.arraycopy(packet, i, sleepTemp, 0, unit_sleep_log);
                              convert[j] = ByteBuffer.wrap(sleepTemp).order(ByteOrder.LITTLE_ENDIAN).getShort();
                              sleepString.append(Short.toString(convert[j])+"\n");
                              j++;
                         }

                         writeToFile("CwmSleepLog.txt", sleepString.toString());

                        currentPackets+=1;
                        cwmEvent = new CwmEvents();
                        cwmEvent.setEventType(Type.EVENT);
                        cwmEvent.setMsgType(msg_type);
                        cwmEvent.setMessageID(message_id);
                        cwmEvent.setCurrentPackages(currentPackets);
                        cwmEvent.setMaxPackages(packetsNumber);
                        if(currentPackets == packetsNumber)
                            currentPackets = 0;
                        //cwmEvent.setSleepParser(convert);
                     break;
                     case ID.LIFE_HISTORY:
                         Log.d("bernie","sdk life history");
                         LongTask.longTaskReceivedHandler.removeCallbacks(LongTask.currentLongTask);
                         BleReceiver.hasLongTask = false;
                          byte[] lifeTemp = new byte[56];
                          byte[] fourByteTemp = new byte[4];
                          byte[] twoByteTemp = new byte[2];
                          int unit_life_data = 56;
                          int timeStamp = 0;
                          float stepCount = 0;
                          float distance = 0;
                          float calorie = 0;
                          float hearRate = 0;
                          float minHeartRate = 0;
                          float maxHeartRate = 0;
                          int sedentaryTriggerTime = 0;
                          int batteryLevel = 0;
                          int batteryCharge = 0;
                          int notificationCount = 0;
                          int displayOnTime = 0;
                          int vibOnTime = 0;
                          int bleSendCount = 0;
                          int bleReceivedCount = 0;
                          int tpCount = 0;
                          int tabataActionTime = 0;
                          int handUpDownCount = 0;
                          startPos = 4;

                          endPos = packet.length - (unit_life_data + 1); //1: checksum byte

                          for (int i = startPos; i <= endPos; i += unit_life_data) {
                               System.arraycopy(packet, i, lifeTemp, 0, unit_life_data);

                               System.arraycopy(lifeTemp, 0, fourByteTemp, 0, 4);
                               timeStamp = ByteBuffer.wrap(fourByteTemp).order(ByteOrder.LITTLE_ENDIAN).getInt();
                               System.arraycopy(lifeTemp, 4, fourByteTemp, 0, 4);
                               stepCount = (int) ByteBuffer.wrap(fourByteTemp).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                               System.arraycopy(lifeTemp, 8, fourByteTemp, 0, 4);
                               distance = (int) ByteBuffer.wrap(fourByteTemp).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                                System.arraycopy(lifeTemp, 12, fourByteTemp, 0, 4);
                                calorie = (int) ByteBuffer.wrap(fourByteTemp).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                                System.arraycopy(lifeTemp, 16, fourByteTemp, 0, 4);
                                hearRate = (int) ByteBuffer.wrap(fourByteTemp).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                                System.arraycopy(lifeTemp, 20, fourByteTemp, 0, 4);
                                minHeartRate =  ByteBuffer.wrap(fourByteTemp).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                                System.arraycopy(lifeTemp, 24, fourByteTemp, 0, 4);
                                maxHeartRate =  ByteBuffer.wrap(fourByteTemp).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                                System.arraycopy(lifeTemp, 28, fourByteTemp, 0, 4);
                                sedentaryTriggerTime = ByteBuffer.wrap(fourByteTemp).order(ByteOrder.LITTLE_ENDIAN).getInt();
                                System.arraycopy(lifeTemp, 32, twoByteTemp, 0, 2);
                                tpCount = ByteBuffer.wrap(twoByteTemp).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;
                                System.arraycopy(lifeTemp, 34, twoByteTemp, 0, 2);
                                notificationCount = ByteBuffer.wrap(twoByteTemp).order(ByteOrder.LITTLE_ENDIAN).getShort();
                                System.arraycopy(lifeTemp, 36, fourByteTemp, 0, 4);
                                displayOnTime =ByteBuffer.wrap(fourByteTemp).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFFFFFF;
                                System.arraycopy(lifeTemp, 40, fourByteTemp, 0, 4);
                                vibOnTime = ByteBuffer.wrap(fourByteTemp).order(ByteOrder.LITTLE_ENDIAN).getInt();
                                System.arraycopy(lifeTemp, 44, twoByteTemp, 0, 2);
                                bleSendCount = ByteBuffer.wrap(twoByteTemp).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF ;
                                System.arraycopy(lifeTemp, 46, twoByteTemp, 0, 2);
                                bleReceivedCount = ByteBuffer.wrap(twoByteTemp).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;
                                System.arraycopy(lifeTemp, 48, fourByteTemp, 0, 4);
                                tabataActionTime = ByteBuffer.wrap(fourByteTemp).order(ByteOrder.LITTLE_ENDIAN).getInt()  & 0xFFFFFFFF;
                                batteryLevel = lifeTemp[52] & 0xFF;
                                batteryCharge = lifeTemp[53] & 0xFF;
                                handUpDownCount = lifeTemp[54] & 0xFF;

                              StringBuilder lifeString = new StringBuilder();
                              Log.d("bernie","sdk history timeStamp:"+Integer.toString(timeStamp));
                              Log.d("bernie","sdk history stepCount:"+Float.toString(stepCount));
                              Log.d("bernie","sdk history distance:" +Float.toString(distance));
                              Log.d("bernie","sdk history calories: " + Float.toString(calorie));
                              Log.d("bernie","sdk history heartRate:" + Float.toString(hearRate));
                              Log.d("bernie","sdk history minHeartRate:" + Float.toString(minHeartRate));
                              Log.d("bernie","sdk history maxHeartRate:" + Float.toString(maxHeartRate));
                              Log.d("bernie","sdk history sedentaryTriggerTime:" +Integer.toString(sedentaryTriggerTime));
                              Log.d("bernie","sdk history battleLevel:" +Integer.toString(batteryLevel));
                              Log.d("bernie","sdk history battleCharge:" +Integer.toString(batteryCharge));
                              Log.d("bernie","sdk history notify count:" +Integer.toString(notificationCount));
                              Log.d("bernie","sdk history display on time::" +Integer.toString(displayOnTime));
                              Log.d("bernie","sdk history vib on time:" +Integer.toString(vibOnTime));
                              Log.d("bernie","sdk history ble Send Count::" +Integer.toString(bleSendCount));
                              Log.d("bernie","sdk history ble Received Count::" +Integer.toString(bleReceivedCount));
                              Log.d("bernie","sdk history tp Count:" +Integer.toString(tpCount));
                              Log.d("bernie","sdk history Tabata action time:" +Integer.toString(tabataActionTime));
                              Log.d("bernie","sdk history Hand up Down: " +Integer.toString(handUpDownCount));
                              lifeString.append(" history timeStamp:"+Integer.toString(timeStamp)+"\n");
                              lifeString.append(" history stepCount:"+Float.toString(stepCount)+"\n");
                              lifeString.append(" history distance:" +Float.toString(distance)+"\n");
                              lifeString.append(" history calories: " + Float.toString(calorie)+"\n");
                              lifeString.append(" history heartRate:" + Float.toString(hearRate)+"\n");
                              lifeString.append(" history minHeartRate:" + Float.toString(minHeartRate)+"\n");
                              lifeString.append(" history maxHeartRate:" + Float.toString(maxHeartRate)+"\n");
                              lifeString.append(" history sedentaryTriggerTime:" +Integer.toString(sedentaryTriggerTime)+"\n");
                              lifeString.append(" history battleLevel:" +Integer.toString(batteryLevel)+"\n");
                              lifeString.append(" history battleCharge:" +Integer.toString(batteryCharge)+"\n");
                              lifeString.append(" history notify count:" +Integer.toString(notificationCount)+"\n");
                              lifeString.append(" history display on time::" +Integer.toString(displayOnTime)+"\n");
                              lifeString.append(" history vib on time:" +Integer.toString(vibOnTime)+"\n");
                              lifeString.append(" history ble Send Count::" +Integer.toString(bleSendCount)+"\n");
                              lifeString.append(" history ble Received Count::" +Integer.toString(bleReceivedCount)+"\n");
                              lifeString.append(" history tp Count:" +Integer.toString(tpCount)+"\n");
                              lifeString.append(" history Tabata action time:" +Integer.toString(tabataActionTime)+"\n");
                              lifeString.append(" history Hand up Down: " +Integer.toString(handUpDownCount)+"\n");

                              writeToFile("CwmHistoryLife.txt", lifeString.toString());

                              currentPackets+=1;
                              cwmEvent = new CwmEvents();
                              cwmEvent.setEventType(Type.EVENT);
                              cwmEvent.setMsgType(msg_type);
                              cwmEvent.setMessageID(message_id);
                              cwmEvent.setCurrentPackages(currentPackets);
                              cwmEvent.setMaxPackages(packetsNumber);
                              if(currentPackets == packetsNumber)
                                  currentPackets = 0;
                          }
                    break;
                    case ID.LOG_HISTORY:
                        Log.d("bernie","sdk log history");
                        LongTask.longTaskReceivedHandler.removeCallbacks(LongTask.currentLongTask);
                        BleReceiver.hasLongTask = false;
                        byte[] newPacket = new byte[packet.length -3];
                        System.arraycopy(packet, 3, newPacket, 0, (packet.length -3));

                        writeToFile("CwmLog.txt", newPacket);

                        currentPackets+=1;
                        cwmEvent = new CwmEvents();
                        cwmEvent.setEventType(Type.EVENT);
                        cwmEvent.setMsgType(msg_type);
                        cwmEvent.setMessageID(message_id);
                        cwmEvent.setCurrentPackages(currentPackets);
                        Log.d("bernie","sdk log history packet max:"+Integer.toString(packetsNumber));
                        cwmEvent.setMaxPackages(packetsNumber);
                        if(currentPackets == packetsNumber)
                            currentPackets = 0;
                        break;
                    case ID.HISTORY_PACKAGES: //History length
                        byte[] lengthByte = new byte[4];
                        System.arraycopy(packet,3, lengthByte, 0, 4);
                        int packages = ByteBuffer.wrap(lengthByte).order(ByteOrder.LITTLE_ENDIAN).getInt();
                        packetsNumber = packages;
                        cwmEvent = new CwmEvents();
                        cwmEvent.setEventType(Type.EVENT);
                        cwmEvent.setMsgType(msg_type);
                        cwmEvent.setMessageID(message_id);
                        //cwmEvent.setRemindPackages(packages);
                        cwmEvent.setMaxPackages(packetsNumber);
                        Log.d("bernie","sdk packages length: "+Integer.toString(packages));
                        break;

                    case ID.HISTORY_ERASE_DONE:
                    case ID.SYNC_DONE:
                    case ID.SYNC_ABORTED:
                        currentPackets = 0;
                        cwmEvent = new CwmEvents();
                        cwmEvent.setEventType(Type.EVENT);
                        cwmEvent.setMsgType(msg_type);
                        cwmEvent.setMessageID(message_id);
                        break;
                }


                break;
            case Type.COMMAND_RESPONSE:
                break;
            case Type.FACTORY_RESPONSE:
                message_id = data.getMsgCmdId();
                int sensor_id;
                switch (message_id) {
                    case ID.SELF_TEST_RESULT:
                       sensor_id = packet[3];
                        int selfTest = packet[4];
                        cwmEvent = new CwmEvents();
                        cwmEvent.setEventType(Type.EVENT);
                        cwmEvent.setMsgType(msg_type);
                        cwmEvent.setMessageID(message_id);
                        cwmEvent.setSensorID(sensor_id);
                        cwmEvent.setSelfTest(selfTest);

                        break;
                    case ID.CALIBRATION_RESULT:
                        Log.d("bernie","sdk calibreate result");
                        sensor_id = packet[3];
                        int calbrateStatus = packet[4];
                        cwmEvent = new CwmEvents();
                        cwmEvent.setEventType(Type.EVENT);
                        Bias bias = new Bias();

                        cwmEvent.setMsgType(msg_type);
                        cwmEvent.setMessageID(message_id);
                        cwmEvent.setSensorID(sensor_id);
                        cwmEvent.setCalibateStatus(calbrateStatus);
                        if(calbrateStatus == Type.CALIBRATE_RESULT.CALIB_STATUS_PASS.ordinal() ) {
                            Log.d("bernie", "sdk calibreate mid");
                            System.arraycopy(packet, 5, temp1, 0, 4);
                            float biasX = ByteBuffer.wrap(temp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                            Log.d("bernie", "sdk calibreate mid I");
                            System.arraycopy(packet, 9, temp1, 0, 4);
                            float biasY = ByteBuffer.wrap(temp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                            Log.d("bernie", "sdk calibreate mid II");
                            System.arraycopy(packet, 13, temp1, 0, 4);
                            float biasZ = ByteBuffer.wrap(temp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                            Log.d("bernie", "sdk calibreate mid III");
                            bias.set(biasX, biasY, biasZ);
                            cwmEvent.setBias(bias);
                        }
                        Log.d("bernie","sdk calibreate result after");
                        break;
                    case ID.MAP_ERASE_DONE:
                         int mapId = packet[3] & 0xFF;

                        cwmEvent = new CwmEvents();
                        cwmEvent.setEventType(Type.EVENT);
                        cwmEvent.setMsgType(msg_type);
                        cwmEvent.setMessageID(message_id);
                        cwmEvent.setMapId(mapId);

                        break;
                    case ID.MAP_WRITE_DONE:
                        CwmManager.endPos = CwmManager.endPos + 128;
                        CwmManager.currentMapSize = CwmManager.currentMapSize + 128;
                        CwmManager.mapSize = CwmManager.mapSize + 128;
                        if(CwmManager.mapSize == Type.OLED_PAGE_SIZE && CwmManager.oledAccomplish == false) {
                            Log.d("bernie","endPos size is oled page size ");
                            CwmManager.oledAccomplish = true;
                            CwmManager.mapSize = 0;
                            CwmManager.endPos = 0xE000;
                        }
                        else if(CwmManager.mapSize == Type.BITMAP_PAHE_SIZE && CwmManager.oledAccomplish == true) {
                            Log.d("bernie","endPos size is bitmap page size ");
                            CwmManager.bitMapAccomplish = true;
                            CwmManager.mapSize = 0;
                            CwmManager.endPos = 0x50000;
                        }
                        else if(CwmManager.mapSize == Type.FONT_LIB && CwmManager.oledAccomplish == true && CwmManager.bitMapAccomplish == true){
                            Log.d("bernie","endPos size is font lib page size ");
                            CwmManager.fontLitAccomplish = true;
                            CwmManager.mapSize = 0;
                            CwmManager.endPos = 0x1000;
                        }
                        cwmEvent = new CwmEvents();
                        cwmEvent.setEventType(Type.EVENT);
                        cwmEvent.setMsgType(msg_type);
                        cwmEvent.setMessageID(message_id);
                        cwmEvent.setCurrentSize(CwmManager.currentMapSize);
                        //cwmEvents.setMaxSize((int)CwmManager.bitMapLength);
                        break;
                }
                break;
            case Type.BLE_CONNECT_STATUS:
                message_id = data.getMsgCmdId();
                cwmEvent = new CwmEvents();
                cwmEvent.setEventType(Type.EVENT);
                cwmEvent.setMsgType(msg_type);
                cwmEvent.setMessageID(message_id);
                break;
        }
        return cwmEvent;
    }

    private void writeToFile(String fileName, String content){
        try{
            FileWriter fw = new FileWriter(Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.newLine();
            bw.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    private void writeToFile(String fileName, byte[] raw){
        try{
            File file = new File(Environment.getExternalStorageDirectory().toString() + "/Download/"+fileName);
            FileWriter txt = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(txt);
            String log = new String(raw, "UTF-8");
            bw.write(log);
            bw.newLine();
            bw.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
