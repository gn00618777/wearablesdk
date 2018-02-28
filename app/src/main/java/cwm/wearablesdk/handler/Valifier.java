package cwm.wearablesdk.handler;

import cwm.wearablesdk.Payload;
import cwm.wearablesdk.Protocol;
import cwm.wearablesdk.constants.Type;

/**
 * Created by user on 2017/12/26.
 */

public class Valifier {

    public boolean check(Payload data){
        byte[] packet = data.getPacket();
        int checksum = packet[(packet.length - 1)];
        int sum = 0;

        for(int i = 0 ; i < (packet.length - 1) ; i++){
            sum += packet[i];
        }

        if((byte)sum == (byte)checksum)
            return true;

        return false;
    }
    public static void checkHandler(Protocol data) {
        if(data.packet_type == Type.BLE_PAKAGE_TYPE.LONG_MESSAGE_START.ordinal()){

        }
    }
}
