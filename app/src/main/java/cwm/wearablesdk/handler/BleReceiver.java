package cwm.wearablesdk.handler;

import android.util.Log;

import cwm.wearablesdk.Parser;
import cwm.wearablesdk.Payload;
import cwm.wearablesdk.QueueHandler;
import cwm.wearablesdk.constants.Type;
import cwm.wearablesdk.CwmManager;

/**
 * Created by user on 2017/12/21.
 */

public class BleReceiver {

    private CwmManager mCwmManager;
    private Classifier mClassifier;
    private QueueHandler mQhandler;
    private Parser mParser;
    private Valifier mValifier;

   public BleReceiver(CwmManager manager){
        mCwmManager = manager;
        mClassifier = new Classifier();
        mQhandler = new QueueHandler(mCwmManager);
        mParser = new Parser(mCwmManager);
        mValifier = new Valifier();
    }

    public void receiveRawByte(byte[] rxBuffer)
    {
        for(int i = 0 ; i < rxBuffer.length ; i++)
           Log.d("bernie2",Integer.toHexString(rxBuffer[i] & 0xFF));
        Log.d("bernie1","\n");
        Payload data = mClassifier.classifyRawByteArray(rxBuffer);

        if(data.packet_type == 0xFF)
            return;

        if(data.packet_type == Type.BLE_PAKAGE_TYPE.SHORT_MESSAGE.ordinal())
            mQhandler.enQueue(data);
        else if(data.packet_type == Type.BLE_PAKAGE_TYPE.LONG_MESSAGE_START.ordinal())
            mQhandler.enOtherQueue(data);
        else if(data.packet_type == Type.BLE_PAKAGE_TYPE.LONG_MESSAGE_MID.ordinal())
            mQhandler.enOtherQueue(data);
        else if(data.packet_type == Type.BLE_PAKAGE_TYPE.LONG_MESSAGE_END.ordinal()) {
            mQhandler.enOtherQueue(data);
            mQhandler.combinePackeages();
        }

        Payload entry = mQhandler.deQueue();

        if(entry != null){
            boolean isValid = mValifier.check(entry);
            if(isValid) {
                mParser.parsePayload(entry);
            }
        }
    }
}
