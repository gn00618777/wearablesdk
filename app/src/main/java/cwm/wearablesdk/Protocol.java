package cwm.wearablesdk;

import android.util.Log;

import cwm.wearablesdk.constants.Type;

/**
 * Created by user on 2017/11/23.
 */

public class Protocol {

    public int packet_type;

    Protocol() {
        this.packet_type = 0;
    }

    public static Command addBleProtocol(byte[] data) {

        byte[] newBuffer;
        int checksum = 0;
        Command command;

        if (data.length <= 17) {
            newBuffer = new byte[data.length + 3]; //header1+length+checksum
            newBuffer[0] = (byte) 0xE6;
            newBuffer[1] = (byte) (data.length + 3);

            System.arraycopy(data, 0, newBuffer, 2, data.length);

            for (int i = 1; i < (data.length + 2); i++) { //header1 is not part of checksum
                checksum += newBuffer[i];
            }
            newBuffer[data.length + 2] = (byte) checksum;
            command = new Command(Type.SINGLE);
        }
        else{
            newBuffer = new byte[data.length + 3]; // length1+length2+checksum

            newBuffer[0] = (byte)((data.length + 4) & 0xFF);
            newBuffer[1] = (byte) (((data.length + 4) >> 8) & 0xFF);

            System.arraycopy(data, 0, newBuffer, 2, data.length);

            for(int i = 0 ; i < (data.length + 2); i++){
               checksum += newBuffer[i];
            }
            newBuffer[data.length + 2] = (byte) checksum;
            command = new Command(Type.MULTI);
        }

        command.setTransmitted(newBuffer);

        return command;
    }
}
