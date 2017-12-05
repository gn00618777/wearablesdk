package cwm.wearablesdk;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by user on 2017/11/22.
 */

public class Parser {

    final  float M_PI = 3.14159265358979323846f;
    final float GYRO_CONVERT_2000DPS = (float)(M_PI/((float)16.4 * (float)180));

    float GRAVITY_EARTH = 9.8066f;
    float ACC_RANGE_16G_CONVERT = (GRAVITY_EARTH*(1.0f/2048.0f));
    final float ADXL362_8G_CONVERTER = GRAVITY_EARTH*(1.0f/250.0f);

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

    public void parseFlashInformation(Data aPackage){
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
            String log = aPackage.getValue().toString();
            bw.write(log);
            bw.newLine();
            bw.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    public CwmEvents getInfomation(int messageId, byte[] value){
        if(messageId == ID.MOTION_DATA_REPORT_MESSAGE_ID ){
            int[] output = new int[4];
            int walkStep = 0;
            int distance = 0;
            int calories = 0;
            int status = 0;
            /***********************************************************************/
            CwmManager.jniMgr.getCwmInformation(ID.MOTION_DATA_REPORT_MESSAGE_ID,value,output);
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
            CwmManager.jniMgr.getCwmInformation(ID.BATTERY_STATUS_REPORT_MESSAGE_ID,value,output);
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

            CwmManager.jniMgr.getCwmInformation(ID.HART_RATE_EVENT_MESSAGE_ID,value,output);
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
            CwmManager.jniMgr.getCwmInformation(ID.TABATA_EVENT_MESSAGE_ID, value, output);
            /***************************************************************/
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
            CwmManager.jniMgr.getCwmInformation(ID.SOFTWARE_VERSION_MESSAGE_ID,value,output);
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
            CwmManager.jniMgr.getGestureListInfomation(ID.GESUTRE_EVENT_MESSAGE_ID,value,output);
            cwmEvents.setGestureList(output);
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
        else if(messageId == ID.SENSOR_REPORT_MESSAGE_ID){
            short[] output_16 = new short[6];
            float[] output_32_acc = new float[3];
            float[] output_32_gyro = new float[3];
            int trustLevel = 0;
            int heartBeat = 0;
            short signalGrade = 0;
            float temperature = 0;
            float pressure = 0;
            byte[] temp = new byte[2];
            byte[] temp1 = new byte[4];
            int j = 0;
            CwmEvents cwmEvents = new CwmEvents();
            cwmEvents.setId(ID.SENSOR_REPORT_MESSAGE_ID);
            cwmEvents.setSensorType(value[5]);
            if(value[5] == 0x02) { //BMI160
                for (int i = 6; i < 18; i += 2) {
                    System.arraycopy(value, i, temp, 0, 2);
                    output_16[j] = ByteBuffer.wrap(temp).order(ByteOrder.LITTLE_ENDIAN).getShort();
                    j++;
                }
                output_32_acc[0] = (float) output_16[0] * ACC_RANGE_16G_CONVERT;
                output_32_acc[1] = (float) output_16[1] * ACC_RANGE_16G_CONVERT;
                output_32_acc[2] = (float) output_16[2] * ACC_RANGE_16G_CONVERT;

                output_32_gyro[0] = (float) output_16[3] * GYRO_CONVERT_2000DPS;
                output_32_gyro[1] = (float) output_16[4] * GYRO_CONVERT_2000DPS;
                output_32_gyro[2] = (float) output_16[5] * GYRO_CONVERT_2000DPS;
            }
            else if(value[5] == 0x01) { //ADXL
                for (int i = 6; i < 12; i += 2) {
                    System.arraycopy(value, i, temp, 0, 2);
                    output_16[j] = ByteBuffer.wrap(temp).order(ByteOrder.LITTLE_ENDIAN).getShort();
                    j++;
                }
                output_32_acc[0] = (float) output_16[0] * ADXL362_8G_CONVERTER;
                output_32_acc[1] = (float) output_16[1] * ADXL362_8G_CONVERTER;
                output_32_acc[2] = (float) output_16[2] * ADXL362_8G_CONVERTER;
            }
            else if(value[5] == 0x03){ //HEART
                System.arraycopy(value, 6, temp1, 0, 4);
                trustLevel = ByteBuffer.wrap(temp1).order(ByteOrder.LITTLE_ENDIAN).getInt();
                System.arraycopy(value, 10, temp1, 0, 4);
                heartBeat = (int)ByteBuffer.wrap(temp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                System.arraycopy(value, 14, temp, 0, 2);
                signalGrade = ByteBuffer.wrap(temp).order(ByteOrder.LITTLE_ENDIAN).getShort();
            }
            else if(value[5] == 0x04) { //Pressure
                System.arraycopy(value, 6, temp1, 0, 4);
                temperature = ByteBuffer.wrap(temp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                System.arraycopy(value, 10, temp1, 0, 4);
                pressure = (int)ByteBuffer.wrap(temp1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            }
            cwmEvents.setSensors(output_32_acc, output_32_gyro);
            cwmEvents.setTrustLevel(trustLevel);
            cwmEvents.setSignalGrade(signalGrade);
            cwmEvents.setHeartBeat(heartBeat);
            cwmEvents.setTemperature(temperature);
            cwmEvents.setPressure(pressure);
            return cwmEvents;
        }
        return null;
    }
}
