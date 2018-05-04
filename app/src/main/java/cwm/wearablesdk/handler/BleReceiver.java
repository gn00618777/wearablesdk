package cwm.wearablesdk.handler;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

import cwm.wearablesdk.LongTask;
import cwm.wearablesdk.Parser;
import cwm.wearablesdk.Payload;
import cwm.wearablesdk.constants.Type;
import cwm.wearablesdk.CwmManager;
import cwm.wearablesdk.events.AckEvents;
import cwm.wearablesdk.events.CwmEvents;
import cwm.wearablesdk.events.ErrorEvents;

/**
 * Created by user on 2017/12/21.
 */

public class BleReceiver {

    private CwmManager mCwmManager;
    private Classifier mClassifier;
    private Parser mParser;
    private Valifier mValifier;

    public static final Queue<Payload> mOutPutQueue = new LinkedList<>();
    public static final Queue<Payload> mPendingQueue = new LinkedList<>();
    public static boolean hasLongTask = false;

   public BleReceiver(CwmManager manager){
        mCwmManager = manager;
        mClassifier = new Classifier();
        mParser = new Parser();
        mValifier = new Valifier();
    }

    public void receiveRawByte(byte[] rxBuffer)
    {
       // Log.d("bernie","rxbuffer length:"+Integer.toString(rxBuffer.length));
        //for(int i = 0 ; i < rxBuffer.length ; i++)
           //Log.d("bernie2",Integer.toHexString(rxBuffer[rxBuffer.length-1] & 0xFF));
        //Log.d("bernie1","\n");
        Payload data = mClassifier.classifyRawByteArray(rxBuffer);

        if(data.packet_type == 0xFF)
            return;

        if(data.packet_type == Type.BLE_PAKAGE_TYPE.SHORT_MESSAGE.ordinal())
            mOutPutQueue.add(data);
        else if(data.packet_type == Type.BLE_PAKAGE_TYPE.LONG_MESSAGE_START.ordinal()) {
                hasLongTask = true;
                int msgCmdType = data.getMsgCmdType();
                int msgCmdId = data.getMsgCmdId();
                LongTask task = new LongTask(msgCmdType, msgCmdId);
                task.registerManager(mCwmManager);
                LongTask.currentLongTask = task;
                LongTask.longTaskReceivedHandler.postDelayed(LongTask.currentLongTask, 5000);

                mPendingQueue.add(data);
        }
        else if(data.packet_type == Type.BLE_PAKAGE_TYPE.LONG_MESSAGE_MID.ordinal()){
           if(hasLongTask)
               mPendingQueue.add(data);
        }
        else if(data.packet_type == Type.BLE_PAKAGE_TYPE.LONG_MESSAGE_END.ordinal()) {
            if(hasLongTask){
                mPendingQueue.add(data);
                combinePackeages();
            }

        }

        Payload entry = mOutPutQueue.poll();

        if(entry != null){
            boolean isValid = mValifier.check(entry);
            if(isValid) {
                CwmEvents event = mParser.parsePayload(entry);
                if(event != null) {
                    int type = event.getEventType();
                    switch (type) {
                        case Type.EVENT:
                            mCwmManager.getListener().onEventArrival(event);
                            break;
                        case Type.ACK_EVENT:
                            AckEvents ackEvent = event.getAckEvent();

                            if((ackEvent.getType() & 0xFF) == Type.FACTORY_DATA_COMMAND &&
                                    (((ackEvent.getId() & 0xFF) == 0xE7) || ((ackEvent.getId() & 0xFF) == 0xE8))){
                                mCwmManager.sendRemindCommand();
                            }
                            else
                                mCwmManager.getAckListener().onAckArrival(ackEvent);
                            break;
                        case Type.ERROR_EVENT:
                            ErrorEvents errorEvent = event.getErrorEvent();
                            mCwmManager.getErrorListener().onErrorArrival(errorEvent);
                            break;
                    }
                }
            }
        }
    }

    public void combinePackeages()
    {
        int queueSize = 0;
        int desPos = 0;
        byte[] value = new byte[256];
        int msgCmdType = 0;
        int msgCmdId = 0;

        queueSize = mPendingQueue.size();

        for(int i = 0 ; i < queueSize ; i++) {
            Payload entry = mPendingQueue.poll();
            if(i == 0){
                msgCmdType = entry.getMsgCmdType();
                msgCmdId = entry.getMsgCmdId();
            }
            byte[] packet = entry.getPacket();
            System.arraycopy(packet, 0, value, desPos, packet.length);
            desPos += packet.length;
        }

        byte[] newValue = new byte[desPos];
        System.arraycopy(value, 0, newValue, 0, desPos);

        Payload data = new Payload(msgCmdType, msgCmdId, newValue);
        mOutPutQueue.add(data);
    }

}
