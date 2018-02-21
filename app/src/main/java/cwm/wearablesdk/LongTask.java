package cwm.wearablesdk;

import android.content.Intent;
import android.os.Handler;

import cwm.wearablesdk.constants.ID;
import cwm.wearablesdk.events.ErrorEvents;

/**
 * Created by user on 2018/1/11.
 */

public class LongTask implements Runnable {
    private int type;
    private int id;
    private CwmManager mManager;

    public static LongTask currentLongTask;
    public static Handler longTaskReceivedHandler = new Handler();

    LongTask(int msgCmdType, int id){
        this.type = msgCmdType;
        this.id = id;
    }

    public void registerManager(CwmManager manager) {
        mManager = manager;
    }

    @Override
    public void run()
    {
        QueueHandler.mPendingQueue.clear();
        ErrorEvents errorEvents = new ErrorEvents();
        errorEvents.setErrorId(ID.PACKET_LOST);
        errorEvents.setMsgCmdType(type);
        errorEvents.setId(id);
        mManager.getErrorListener().onErrorArrival(errorEvents);
    }
}
