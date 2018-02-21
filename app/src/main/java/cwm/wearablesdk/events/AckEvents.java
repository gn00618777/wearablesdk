package cwm.wearablesdk.events;

import cwm.wearablesdk.CwmManager;

/**
 * Created by user on 2017/8/31.
 */

public class AckEvents{
    private int mType;
    private int mId;

    public AckEvents(int msgCmdType, int msgCmdId){
          mType = msgCmdType;
          mId = msgCmdId;
    }

    public int getType(){return mType;}
    public int getId() {
        return mId;
    }

    public int getCurrentMapSize(){return CwmManager.endPos;}
    public long getMaxMapSize(){return CwmManager.bitMapLength;}
}
