package cwm.wearablesdk;

/**
 * Created by user on 2017/10/31.
 */

public class ErrorEvents {
    int errorID;
    int errorCommand;

    public int getId(){
         return errorID;
    }
    public int getCommand(){
        return errorCommand;
    }
    public void setId(int id){
        errorID = id;
    }
    public void setCommand(int command){
        errorCommand = command;
    }

}
