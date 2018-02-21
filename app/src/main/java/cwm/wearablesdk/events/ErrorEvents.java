package cwm.wearablesdk.events;

/**
 * Created by user on 2017/10/31.
 */

public class ErrorEvents {
    int errorID;

    int type;
    int id;

    public void setErrorId(int id){
        errorID = id;
    }
    public int getErrorId(){
         return errorID;
    }

    public void setMsgCmdType(int type){this.type = type; }
    public void setId(int id){this.id = id;}

    public int getMsgCmdType(){return this.type;}
    public int getId(){return this.id;}

}
