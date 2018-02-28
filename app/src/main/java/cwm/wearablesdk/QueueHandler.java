package cwm.wearablesdk;

import java.util.LinkedList;
import java.util.Queue;

import cwm.wearablesdk.constants.Type;
import cwm.wearablesdk.handler.Valifier;

/**
 * Created by user on 2017/12/3.
 */

public class QueueHandler {
    private CwmManager mCwmManager;
    public static final Queue<Payload> mOutPutQueue = new LinkedList<>();
    public static final Queue<Payload> mPendingQueue = new LinkedList<>();

   public QueueHandler(CwmManager manager){
        mCwmManager= manager;
    }

    public Payload deQueue(){
        Payload data = mOutPutQueue.poll();
        return data;
    }

    public void enQueue(Payload data){
            mOutPutQueue.add(data);
    }

    public void enOtherQueue(Payload data){
        if(data.packet_type == Type.BLE_PAKAGE_TYPE.LONG_MESSAGE_START.ordinal()){
            int msgCmdType = data.getMsgCmdType();
            int msgCmdId = data.getMsgCmdId();
            LongTask task = new LongTask(msgCmdType, msgCmdId);
            task.registerManager(mCwmManager);
            LongTask.currentLongTask = task;
            LongTask.longTaskReceivedHandler.postDelayed(LongTask.currentLongTask, 5000);
        }
        mPendingQueue.add(data);
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
