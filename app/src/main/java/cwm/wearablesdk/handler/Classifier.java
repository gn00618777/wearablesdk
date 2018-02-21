package cwm.wearablesdk.handler;

import cwm.wearablesdk.CwmManager;
import cwm.wearablesdk.Payload;
import cwm.wearablesdk.QueueHandler;
import cwm.wearablesdk.constants.Type;

/**
 * Created by user on 2017/12/21.
 */

public class Classifier {
    CwmManager mCwmManager;
    QueueHandler mQhandler;

   public Classifier(CwmManager cwmManager){
        mCwmManager = cwmManager;
        mQhandler = new QueueHandler(mCwmManager);
    }

    public void classifyRawByteArray(byte[] rxBuffer)
    {
       if (rxBuffer[0] == (byte)0xE6) {
              /*get protocol info*/
              int packet_type = Type.BLE_PAKAGE_TYPE.SHORT_MESSAGE.ordinal();

              //Payload Header
              int packet_msg_type = rxBuffer[2] & 0xFF;
              int packet_message_id = rxBuffer[3] & 0xFF;

              byte[] newRxBuffer = new byte[rxBuffer.length - 1]; // - header1
              System.arraycopy(rxBuffer, 1, newRxBuffer, 0, rxBuffer.length - 1);

             Payload data = new Payload(packet_msg_type, packet_message_id,  newRxBuffer);
             data.packet_type = packet_type;

            mQhandler.enQueue(data);

        } else if (rxBuffer[0] == (byte)0xE7) {
             //Protocol Header
             int packet_type = Type.BLE_PAKAGE_TYPE.LONG_MESSAGE_START.ordinal();

             //Payload Header
             int packet_msg_type = rxBuffer[3] & 0xFF;
             int packet_message_id = rxBuffer[4] & 0xFF;

              byte[] newRxBuffer = new byte[rxBuffer.length - 1]; // - header1
              System.arraycopy(rxBuffer, 1, newRxBuffer, 0, rxBuffer.length - 1);

              Payload data = new Payload(packet_msg_type, packet_message_id, newRxBuffer);

              //Fill up protocol information
              data.packet_type = packet_type;

              mQhandler.enOtherQueue(data);
          } else if (rxBuffer[0] == (byte)0xE8) {
             //Protocol Header
             int packet_type = Type.BLE_PAKAGE_TYPE.LONG_MESSAGE_MID.ordinal();
           //Payload Head: Pure data
             int packet_msg_type = 0;
             int packet_message_id = 0;

             byte[] newRxBuffer = new byte[rxBuffer.length - 1]; // -  protocol header
             System.arraycopy(rxBuffer, 1, newRxBuffer, 0, rxBuffer.length - 1);

             Payload data = new Payload(packet_msg_type, packet_message_id, newRxBuffer);

             //Fill up protocol information
             data.packet_type = packet_type;

             mQhandler.enOtherQueue(data);

        } else if(rxBuffer[0] == (byte)0xE9) {
            //Protocol Header
            int packet_type = Type.BLE_PAKAGE_TYPE.LONG_MESSAGE_END.ordinal();

            int packet_msg_type = 0;
            int packet_message_id = 0;

            byte[] newRxBuffer = new byte[rxBuffer.length - 1]; // - header1
            System.arraycopy(rxBuffer, 1, newRxBuffer, 0, rxBuffer.length - 1);

            Payload data = new Payload(packet_msg_type, packet_message_id, newRxBuffer);

            //Fill up Protocol information
            data.packet_type = packet_type;

            mQhandler.enOtherQueue(data);
            mQhandler.combinePackeages();
        }
          mQhandler.deQueue();
    }

}

