package cwm.wearablesdk;

/**
 * Created by user on 2017/11/27.
 */
import android.os.Handler;

import java.util.ArrayList;

import cwm.wearablesdk.constants.ID;
import cwm.wearablesdk.events.ErrorEvents;

public class Task implements Runnable{
    public byte type;
    public byte id;
    public CwmManager mManager;

    public static Task currentTask;
    public static Handler taskReceivedHandler = new Handler();

    @Override
    public void run()
    {
        ErrorEvents errorEvents = new ErrorEvents();
        errorEvents.setErrorId(ID.NO_ACK);
        errorEvents.setMsgCmdType(type);
        errorEvents.setId(id);
        mManager.getErrorListener().onErrorArrival(errorEvents);
    }

    public void registerManager(CwmManager manager){
        mManager = manager;
    }


    public Task(byte msgCmdType, byte id){
        this.type = msgCmdType;
        this.id = id;
    }


}
