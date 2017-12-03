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
            //int checksum = 0;
           // for(int i = 0 ; i < value.length-1 ; i++){
             //   checksum += (value[i] & 0xFF);
            //}
           // if((byte)checksum == value[value.length-1]) {
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
           // }
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
}
