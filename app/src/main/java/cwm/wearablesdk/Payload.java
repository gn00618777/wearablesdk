package cwm.wearablesdk;

/**
 * Created by user on 2018/1/11.
 */

public class Payload extends Protocol {
    private int msgCmdType;
    private int id;
    private byte[] packet;

    public Payload(int msgCmdType, int id, byte[] value){
        this.msgCmdType = msgCmdType;
        this.id = id;
        packet = value;
    }
    public int getMsgCmdType(){return this.msgCmdType;}
    public int getMsgCmdId(){return this.id;}
    public byte[] getPacket(){return packet;}
}
