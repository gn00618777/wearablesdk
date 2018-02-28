package cwm.wearablesdk;

import android.os.Environment;
import android.os.MemoryFile;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.FormatFlagsConversionMismatchException;

import cwm.wearablesdk.constants.ID;
import cwm.wearablesdk.constants.Type;
import cwm.wearablesdk.events.CwmEvents;
import cwm.wearablesdk.events.ErrorEvents;
import cwm.wearablesdk.handler.AckHandler;
import cwm.wearablesdk.settings.AlarmSetting;
import cwm.wearablesdk.settings.BodySettings;
import cwm.wearablesdk.settings.SystemSetting;
import cwm.wearablesdk.settings.IntelligentSettings;

/**
 * Created by user on 2017/11/22.
 */

public class Parser {

    private CwmManager cwmManager;
    private AckHandler mAckHandler;
    final static float M_PI = 3.14159265358979323846f;
    final static float GYRO_CONVERT_2000DPS = (float)(M_PI/((float)16.4 * (float)180));

    final static float GRAVITY_EARTH = 9.8066f;
    final static float ACC_RANGE_16G_CONVERT = (GRAVITY_EARTH*(1.0f/2048.0f));
    final float ADXL362_8G_CONVERTER = GRAVITY_EARTH*(1.0f/250.0f);

    /*History usage*/
    int packetsNumber = 0;
    int currentPackets = 0;

    public enum DataType{
        CALIBRATION,
        OLED_PAGE,
        PEDOMETER,
        SLEEP,
        EXERCISE,
        HEART_RATE,
        DEBUG_LOG,
        BMI160,
        DEBUG_MSG,
        NUM_OF_DATATYPES
    }
    final  int dataTypeByte = 5;
    final  int dataStart = 6;

    public Parser(CwmManager manager){
        cwmManager = manager;
        mAckHandler = new AckHandler(cwmManager);
    }

    /*public void parseFlashInformation(Data aPackage){
        int packageLength = aPackage.getLength();
        byte[] value = aPackage.getValue();
        int dataType = (int)value[dataTypeByte];
        byte[] timeTemp = new byte[4];
        byte[] elementTemp = new byte[2];
        int timeStamp;
        short element;
        float n_element;
        if(dataType == DataType.BMI160.ordinal()) {
            StringBuilder builder = new StringBuilder();

                for (int i = dataStart; i <= (packageLength - 17); i += 16) {
                    System.arraycopy(value, i, timeTemp, 0, 4);
                    timeStamp = ByteBuffer.wrap(timeTemp).order(ByteOrder.LITTLE_ENDIAN).getInt();
                    builder.append("BMI16 ");
                    builder.append(Integer.toString(timeStamp & 0xFFFFFFFF) + ",");

                    System.arraycopy(value, i + 4, elementTemp, 0, 2);
                    element = ByteBuffer.wrap(elementTemp).order(ByteOrder.LITTLE_ENDIAN).getShort();
                    n_element = ((float) element) * ACC_RANGE_16G_CONVERT;

                    builder.append(Float.toString(n_element) + ",");

                    System.arraycopy(value, i + 6, elementTemp, 0, 2);
                    element = ByteBuffer.wrap(elementTemp).order(ByteOrder.LITTLE_ENDIAN).getShort();
                    n_element = ((float) element) * ACC_RANGE_16G_CONVERT;

                    builder.append(Float.toString(n_element) + ",");

                    System.arraycopy(value, i + 8, elementTemp, 0, 2);
                    element = ByteBuffer.wrap(elementTemp).order(ByteOrder.LITTLE_ENDIAN).getShort();
                    n_element = ((float) element) * ACC_RANGE_16G_CONVERT;

                    builder.append(Float.toString(n_element) + ",");

                    System.arraycopy(value, i + 10, elementTemp, 0, 2);
                    element = ByteBuffer.wrap(elementTemp).order(ByteOrder.LITTLE_ENDIAN).getShort();
                    n_element = ((float) element) * GYRO_CONVERT_2000DPS;
                    builder.append(n_element + ",");

                    System.arraycopy(value, i + 12, elementTemp, 0, 2);
                    element = ByteBuffer.wrap(elementTemp).order(ByteOrder.LITTLE_ENDIAN).getShort();
                    n_element = ((float) element) * GYRO_CONVERT_2000DPS;
                    builder.append(n_element + ",");

                    System.arraycopy(value, i + 14, elementTemp, 0, 2);
                    element = ByteBuffer.wrap(elementTemp).order(ByteOrder.LITTLE_ENDIAN).getShort();
                    n_element = ((float) element) * GYRO_CONVERT_2000DPS;
                    builder.append(n_element + "\n");
                }
            try {
                File file = new File(Environment.getExternalStorageDirectory().toString() + "/Download/CwmLog.txt");
                FileWriter txt = new FileWriter(file, true);
                BufferedWriter bw = new BufferedWriter(txt);
                bw.write(builder.toString());
                bw.newLine();
                bw.close();
            } catch (IOException e){
                e.printStackTrace();
            }

        }
        else if(dataType == DataType.DEBUG_MSG.ordinal()) {
            try{
            File file = new File(Environment.getExternalStorageDirectory().toString() + "/Download/CwmLog.txt");
            FileWriter txt = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(txt);
            String log = new String(aPackage.getValue(), "UTF-8");
            bw.write(log);
            bw.newLine();
            bw.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }*/
    /*public CwmEvents getInfomation(int messageId, byte[] value){
        if(messageId == ID.TABATA_EVENT_MESSAGE_ID){
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
          /*  CwmManager.jniMgr.getCwmInformation(ID.TABATA_EVENT_MESSAGE_ID, value, output);
            /***************************************************************/
            /*items = output[0];
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
           /* for(int i = startPos ; i <= endPos; i+=unit_sleep_log) {
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
        else if(messageId == ID.REQUEST_ERASE_EVENT_MESSAGE_ID){
            int[] output = new int[1];
            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(ID.REQUEST_ERASE_EVENT_MESSAGE_ID);
            CwmManager.jniMgr.getCwmInformation(ID.REQUEST_ERASE_EVENT_MESSAGE_ID,value,output);
            cwmEvents.setEraseProgress(output[0]);
            return cwmEvents;
        }
        else if(messageId == ID.CALIBRATE_DONE_MESSAGE_ID){
            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(ID.CALIBRATE_DONE_MESSAGE_ID);
            return cwmEvents;
        }
        return null;
    }*/

    public void parsePayload(Payload data){
        int msg_type = data.getMsgCmdType();
        int message_id;
        int sensor_tag;
        int j = 0;

        CwmEvents cwmEvents;

        byte[] temp = new byte[2];
        byte[] temp1 = new byte[4];
        short[] output_16 = new short[6];
        float[] output_32 = new float[3];

        byte[] packet = data.getPacket();

        LongTask.longTaskReceivedHandler.removeCallbacks(LongTask.currentLongTask);
        //Log.d("bernie","sdk msg_type:"+Integer.toString(msg_type));
        switch (msg_type){
            case Type.CHECKSUM_ERROR:
                ErrorEvents errorEvents = new ErrorEvents();
                errorEvents.setErrorId(0x03);
                cwmManager.getErrorListener().onErrorArrival(errorEvents);
                break;
            case Type.ACK_INFORMATION:
                 mAckHandler.processAck(data);
                break;
            case Type.SYSTTEM_INFORMATION:
                message_id = data.getMsgCmdId();
                switch (message_id){
                    case ID.USER_CONFIG_INFO:

                        CwmEvents cwmEvent = new CwmEvents();
                        cwmEvent.setMsgType(msg_type);
                        cwmEvent.setMessageID(message_id);

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

                        cwmManager.getListener().onEventArrival(cwmEvent);

                        break;
                    case ID.BATTERY_INFO:
                        int battery = (int)packet[3];

                        cwmEvents = new CwmEvents();
                        cwmEvents.setMsgType(msg_type);
                        cwmEvents.setMessageID(message_id);
                        cwmEvents.setBattery(battery);

                        cwmManager.getListener().onEventArrival(cwmEvents);

                        break;
                    case ID.DEVICE_VERSION_INFO:
                        float main = (float)packet[5];
                        float sub = (((float)packet[6]) / 100);

                        cwmEvents = new CwmEvents();
                        cwmEvents.setMsgType(msg_type);
                        cwmEvents.setMessageID(message_id);
                        cwmEvents.setVersion(main+sub);

                        cwmManager.getListener().onEventArrival(cwmEvents);

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

                        cwmEvents = new CwmEvents();
                        cwmEvents.setMsgType(msg_type);
                        cwmEvents.setMessageID(message_id);
                        cwmEvents.setAccSensor(output_32);
                        cwmEvents.setSensorTag(sensor_tag);

                        cwmManager.getListener().onEventArrival(cwmEvents);

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

                        cwmEvents = new CwmEvents();
                        cwmEvents.setMsgType(msg_type);
                        cwmEvents.setMessageID(message_id);
                        cwmEvents.setGyroSensor(output_32);
                        cwmEvents.setSensorTag(sensor_tag);

                        cwmManager.getListener().onEventArrival(cwmEvents);

                        break;
                    case ID.HEART_RATE_RAW_DATA_REPORT:
                        System.arraycopy(packet, 3, temp1, 0, 4);
                        int heartBeat = (int)ByteBuffer.wrap(temp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        cwmEvents = new CwmEvents();

                        cwmEvents.setMsgType(msg_type);
                        cwmEvents.setMessageID(message_id);
                        cwmEvents.setHeartSensor(heartBeat);

                        cwmManager.getListener().onEventArrival(cwmEvents);
                        break;
                    case ID.WRIST_SCROLL_EVENT_RESPONSE_MESSAGE:
                        cwmEvents = new CwmEvents();

                        cwmEvents.setMsgType(msg_type);
                        cwmEvents.setMessageID(message_id);

                        cwmManager.getListener().onEventArrival(cwmEvents);
                        break;
                    case ID.SEDENTARY_RESPONSE_MESSAGE:
                        cwmEvents = new CwmEvents();

                        cwmEvents.setMsgType(msg_type);
                        cwmEvents.setMessageID(message_id);

                        cwmManager.getListener().onEventArrival(cwmEvents);
                        break;
                    case ID.ACTIVITY_PEDOMETER_DATA_REPORT_MESSAGE:
                        System.arraycopy(packet, 3, temp1, 0, 4);
                        int step_count = (int)ByteBuffer.wrap(temp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        System.arraycopy(packet, 7, temp1, 0, 4);
                        int distance = (int)ByteBuffer.wrap(temp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        System.arraycopy(packet, 11, temp1, 0, 4);
                        int stepFreq = (int)ByteBuffer.wrap(temp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();

                        int status = (int)packet[15];

                        cwmEvents = new CwmEvents();

                        cwmEvents.setMsgType(msg_type);
                        cwmEvents.setMessageID(message_id);
                        cwmEvents.setStepCount(step_count);
                        cwmEvents.setDistance(distance);
                        cwmEvents.setStepFreq(stepFreq);

                        cwmEvents.setStatus(status);

                        cwmManager.getListener().onEventArrival(cwmEvents);
                        break;
                    case ID.TABATA_RESPONSE_MESSAGE:
                        System.arraycopy(packet, 3, temp1, 0, 4);
                        int tabataData = (int)ByteBuffer.wrap(temp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        //Log.d("bernie","sdk item count: "+Integer.toString(tabataData));
                        System.arraycopy(packet, 7, temp1, 0, 4);
                        int tabataCaloris = (int)ByteBuffer.wrap(temp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        //Log.d("bernie","sdk calories: "+Integer.toString(tabataCaloris));
                        System.arraycopy(packet, 11, temp1, 0, 4);
                        int tabataHeartStrength = (int)ByteBuffer.wrap(temp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        //Log.d("bernie","sdk heartStrength: "+Integer.toString(tabataHeartStrength));

                        int items = tabataData / 1000;
                        //Log.d("bernie","sdk items:"+Integer.toString(items));
                        int count = (tabataData % 1000);
                        int caloris = 0;
                        caloris = tabataCaloris;
                        int heart = tabataHeartStrength /100;
                        int strength = tabataHeartStrength % 100;

                        cwmEvents = new CwmEvents();
                        cwmEvents.setMsgType(msg_type);
                        cwmEvents.setMessageID(message_id);
                        cwmEvents.setExerciseItem(items);
                        cwmEvents.setDoItemCount(count);
                        cwmEvents.setTabataCalories(caloris);
                        cwmEvents.setTabataHeart(heart);
                        cwmEvents.setStrength(strength);

                        cwmManager.getListener().onEventArrival(cwmEvents);
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
                        Log.d("bernie","sdk sleep history");
                        startPos = 4;
                         int unit_sleep_log = 2;//byte
                         int dataLength = packet.length - 2 - 2 - 1; // - 2 byte length  - msgtype - msgid - checksum
                        endPos = packet.length - 3;
                         j = 0;
                         short[] convert = new short[dataLength / unit_sleep_log];
                         byte[] sleepTemp = new byte[unit_sleep_log];

                         for (int i = startPos; i <= endPos; i += unit_sleep_log) {
                              System.arraycopy(packet, i, sleepTemp, 0, unit_sleep_log);
                              convert[j] = ByteBuffer.wrap(sleepTemp).order(ByteOrder.LITTLE_ENDIAN).getShort();
                              j++;
                         }
                        cwmEvents = new CwmEvents();
                        cwmEvents.setMsgType(msg_type);
                        cwmEvents.setMessageID(message_id);
                        cwmEvents.setSleepParser(convert);
                     break;
                     case ID.LIFE_HISTORY:
                         Log.d("bernie","sdk life history");
                          byte[] lifeTemp = new byte[46];
                          byte[] fourByteTemp = new byte[4];
                          byte[] twoByteTemp = new byte[2];
                          int unit_life_data = 46;
                          int timeStamp = 0;
                          float stepCount = 0;
                          float distance = 0;
                          float calorie = 0;
                          float hearRate = 0;
                          int sedentaryTriggerTime = 0;
                          int batteryLevel = 0;
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
                          int packetLength = (packet[0] & 0xFF) | (packet[1] << 8);
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
                                sedentaryTriggerTime = ByteBuffer.wrap(fourByteTemp).order(ByteOrder.LITTLE_ENDIAN).getInt();
                                batteryLevel = lifeTemp[24] & 0xFF;
                                System.arraycopy(lifeTemp, 25, twoByteTemp, 0, 2);
                                notificationCount = ByteBuffer.wrap(twoByteTemp).order(ByteOrder.LITTLE_ENDIAN).getShort();
                                System.arraycopy(lifeTemp, 27, fourByteTemp, 0, 4);
                                displayOnTime = ByteBuffer.wrap(fourByteTemp).order(ByteOrder.LITTLE_ENDIAN).getInt();
                                System.arraycopy(lifeTemp, 31, fourByteTemp, 0, 4);
                                vibOnTime = ByteBuffer.wrap(fourByteTemp).order(ByteOrder.LITTLE_ENDIAN).getInt();
                                System.arraycopy(lifeTemp, 35, twoByteTemp, 0, 2);
                                bleSendCount = ByteBuffer.wrap(twoByteTemp).order(ByteOrder.LITTLE_ENDIAN).getShort();
                                System.arraycopy(lifeTemp, 37, twoByteTemp, 0, 2);
                                bleReceivedCount = ByteBuffer.wrap(twoByteTemp).order(ByteOrder.LITTLE_ENDIAN).getShort();
                                System.arraycopy(lifeTemp, 39, twoByteTemp, 0, 2);
                                tpCount = ByteBuffer.wrap(twoByteTemp).order(ByteOrder.LITTLE_ENDIAN).getShort();
                                System.arraycopy(lifeTemp, 41, fourByteTemp, 0, 4);
                                tabataActionTime = ByteBuffer.wrap(fourByteTemp).order(ByteOrder.LITTLE_ENDIAN).getInt();
                                handUpDownCount = lifeTemp[45] & 0xFF;

                              StringBuilder lifeString = new StringBuilder();
                              Log.d("bernie","sdk history timeStamp:"+Integer.toString(timeStamp));
                              Log.d("bernie","sdk history stepCount:"+Float.toString(stepCount));
                              Log.d("bernie","sdk history distance:" +Float.toString(distance));
                              Log.d("bernie","sdk history calories: " + Float.toString(calorie));
                              Log.d("bernie","sdk history heartRate:" + Float.toString(hearRate));
                              Log.d("bernie","sdk history sedentaryTriggerTime:" +Integer.toString(sedentaryTriggerTime));
                              Log.d("bernie","sdk history battleLevel:" +Integer.toString(batteryLevel));
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
                              lifeString.append(" history sedentaryTriggerTime:" +Integer.toString(sedentaryTriggerTime)+"\n");
                              lifeString.append(" history battleLevel:" +Integer.toString(batteryLevel)+"\n");
                              lifeString.append(" history notify count:" +Integer.toString(notificationCount)+"\n");
                              lifeString.append(" history display on time::" +Integer.toString(displayOnTime)+"\n");
                              lifeString.append(" history vib on time:" +Integer.toString(vibOnTime)+"\n");
                              lifeString.append(" history ble Send Count::" +Integer.toString(bleSendCount)+"\n");
                              lifeString.append(" history ble Received Count::" +Integer.toString(bleReceivedCount)+"\n");
                              lifeString.append(" history tp Count:" +Integer.toString(tpCount)+"\n");
                              lifeString.append(" history Tabata action time:" +Integer.toString(tabataActionTime)+"\n");
                              lifeString.append(" history Hand up Down: " +Integer.toString(handUpDownCount)+"\n");

                              try{
                                  FileWriter fw = new FileWriter(Environment.getExternalStorageDirectory().toString() + "/Download/CwmHistoryLife.txt", true);
                                  BufferedWriter bw = new BufferedWriter(fw);
                                  bw.write(lifeString.toString());
                                  bw.newLine();
                                  bw.close();
                              }catch(IOException e){
                                  e.printStackTrace();
                              }

                              currentPackets+=1;
                              if(packetsNumber != 0)
                                cwmManager.getLogListener().onProgressChanged(currentPackets, packetsNumber);
                          }
                    break;
                    case ID.HISTORY_PACKAGES: //History length
                        byte[] lengthByte = new byte[4];
                        System.arraycopy(packet,3, lengthByte, 0, 4);
                        int packages = ByteBuffer.wrap(lengthByte).order(ByteOrder.LITTLE_ENDIAN).getInt();
                        packetsNumber = packages;
                        cwmEvents = new CwmEvents();
                        cwmEvents.setMsgType(msg_type);
                        cwmEvents.setMessageID(message_id);
                        cwmEvents.setRemindPackages(packages);
                        Log.d("bernie","sdk packages length: "+Integer.toString(packages));
                        cwmManager.getListener().onEventArrival(cwmEvents);
                        break;

                    case ID.HISTORY_ERASE_DONE:
                        cwmEvents = new CwmEvents();
                        cwmEvents.setMsgType(msg_type);
                        cwmEvents.setMessageID(message_id);
                        cwmManager.getListener().onEventArrival(cwmEvents);
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
                        cwmEvents = new CwmEvents();

                        cwmEvents.setMsgType(msg_type);
                        cwmEvents.setMessageID(message_id);
                        cwmEvents.setSensorID(sensor_id);
                        cwmEvents.setSelfTest(selfTest);

                        cwmManager.getListener().onEventArrival(cwmEvents);

                        break;
                    case ID.CALIBRATION_RESULT:
                        Log.d("bernie","sdk calibreate result");
                        sensor_id = packet[3];
                        int calbrateStatus = packet[4];
                        cwmEvents = new CwmEvents();
                        Bias bias = new Bias();

                        cwmEvents.setMsgType(msg_type);
                        cwmEvents.setMessageID(message_id);
                        cwmEvents.setSensorID(sensor_id);
                        cwmEvents.setCalibateStatus(calbrateStatus);
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
                            cwmEvents.setBias(bias);
                        }
                        Log.d("bernie","sdk calibreate result after");
                        cwmManager.getListener().onEventArrival(cwmEvents);
                        break;
                    case ID.MAP_ERASE_DONE:
                         int mapId = packet[3] & 0xFF;

                        cwmEvents = new CwmEvents();
                        cwmEvents.setMsgType(msg_type);
                        cwmEvents.setMessageID(message_id);
                        cwmEvents.setMapId(mapId);

                        cwmManager.getListener().onEventArrival(cwmEvents);
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
                        cwmEvents = new CwmEvents();
                        cwmEvents.setMsgType(msg_type);
                        cwmEvents.setMessageID(message_id);
                        cwmEvents.setCurrentSize(CwmManager.currentMapSize);
                        //cwmEvents.setMaxSize((int)CwmManager.bitMapLength);
                        cwmManager.getListener().onEventArrival(cwmEvents);
                        break;

                }
                break;
        }
    }
    private char[] getChars (byte[] bytes) {
        Charset cs = Charset.forName ("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate (bytes.length);
        bb.put (bytes);
        bb.flip ();
        CharBuffer cb = cs.decode (bb);

        return cb.array();
    }
}
