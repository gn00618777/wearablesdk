package cwm.wearablesdk.handler;

import android.util.Log;

import cwm.wearablesdk.handler.Classifier;
import cwm.wearablesdk.CwmManager;

/**
 * Created by user on 2017/12/21.
 */

public class BleReceiver {

    private CwmManager mCwmManager;
    private Classifier mClassifier;

   public BleReceiver(CwmManager manager){
        mCwmManager = manager;
        mClassifier = new Classifier(mCwmManager);
    }

    public void receiveRawByte(byte[] rxBuffer)
    {
       // for(int i = 0 ; i < rxBuffer.length ; i++)
       //    Log.d("bernie2",Integer.toHexString(rxBuffer[i] & 0xFF));
       // Log.d("bernie1","\n");
        mClassifier.classifyRawByteArray(rxBuffer);
    }
}
