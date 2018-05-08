package cwm.wearablesdk;

import java.io.LineNumberInputStream;

import cwm.wearablesdk.constants.Type;

/**
 * Created by user on 2018/5/7.
 */

public class Command {
    private int mType;
    private int mIndex;
    private byte[] transmitted;
    private int mLength;

    private final int UNIT = 19;

    public Command(int type){
        mType = type;
    }

    public void setTransmitted(byte[] partial){
        transmitted = partial;
        mLength = transmitted.length;
    }

    public byte[] getTransmitted(){
        if(mType == Type.SINGLE) {
            return transmitted;
        }
        else{
            byte[] newBuffer;

            if(mLength > UNIT) {
                newBuffer = new byte[20];
                System.arraycopy(transmitted, mIndex, newBuffer, 1, UNIT);
                if (mIndex == 0)
                    newBuffer[0] = (byte) 0xE7;
                else
                    newBuffer[0] = (byte) 0xE8;
            }
            else if(mLength >= 0 && mLength <= UNIT){
                    newBuffer = new byte[mLength + 1];
                    System.arraycopy(transmitted, mIndex, newBuffer, 1, mLength);
                    newBuffer[0] = (byte) 0xE9;
            }
            else{
                newBuffer = new byte[1];
            }

            return newBuffer;
        }

    }
    public void moveNext(){
        mIndex += UNIT;
        mLength -= UNIT;
    }
}
