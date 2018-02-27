package cwm.wearablesdk;

/**
 * Created by user on 2018/1/17.
 */

public class NotificationData {

   private int notifyId;
   private int appIdentifier;
   private String appName;
   private String personeName;

    public NotificationData(){
        notifyId = 0;
        appIdentifier = 0;
        appName = "";
        personeName = "";
    }

    public void setNotifyId(int id){
        notifyId = id;
    }
    public void setAppIdentifier(int identifier){
        appIdentifier = identifier;
    }
    public void setAppName(String name){appName = name;}
    public void setPersonName(String name){personeName = name;}

    public int getNotifyId(){return notifyId;}
    public int getAppIdentifier(){return appIdentifier;}
    public String getAppName(){return appName;}
    public String getPersoneName(){return personeName;}
}
