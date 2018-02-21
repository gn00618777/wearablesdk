package cwm.wearablesdk.settings;

/**
 * Created by user on 2018/1/21.
 */
import cwm.wearablesdk.constants.Type;

public class SystemSetting {
    private int osType;
    private int timeFomat;
    private int historyDetectPeriod;
    private int screenTimeOut;
    private int mScreens;
    private int mFunctions;
    private int noDisturbStart;
    private int noDisturbStop;
    private int sleepStart;
    private int sleepStop;
    private int mBrightness;

    public SystemSetting(){
        osType = Type.OS_CONFIG.ANDROID.ordinal();
        timeFomat = Type.TIME_FORMAT.TIME_HOUR_0_23.ordinal();
        historyDetectPeriod = 0x0A;
        screenTimeOut = 0x05;
        mScreens = 31;
        noDisturbStart = 12;
        noDisturbStop = 14;
        sleepStart = 21;
        sleepStop = 6;
        mBrightness = 0;
    }

    public void setOSType(int type){
        osType = type;
    }
    public void setTimeFormat(int type){
        timeFomat = type;
    }
    public void setHistoryDetectPeriod(int period){
        historyDetectPeriod = period;
    }
    public void setScreenTimeOut(int time){
        screenTimeOut = time;
    }
    public void setScreens(int screens){mScreens = screens;}
    public void setFunctions(int functions){mFunctions = functions;}
    public void setNoDisturbInterval(int start, int stop){
        if(timeFomat == 0) { //0~23
            if (start >= 0 && start <= 23) noDisturbStart = start;
            else noDisturbStart = 12;
            if (stop >= 0 && stop <= 23) noDisturbStop = stop;
            else noDisturbStop = 14;
        }
        else if(timeFomat == 1){
            if (start >= 1 && start <= 12) noDisturbStart = start;
            else noDisturbStart = 12;
            if (stop >= 1 && stop <= 12) noDisturbStop = stop;
            else noDisturbStop = 2;
        }
    }
    public void setSleepTimeInterval(int start, int stop){
        if(start >= 0 && start <= 23){
            sleepStart = start;
        }
        if(stop >= 0 && stop <= 23){
            sleepStop = stop;
        }
    }
    public void setBrightness(int brightness){
        mBrightness = brightness;
    }

    public int getOsType(){return osType;}
    public int getTimeFormat(){return timeFomat;}
    public int getHistoryDetectPeriod(){return historyDetectPeriod;}
    public int getScreenTimeOut(){return screenTimeOut;}
    public int getScreens(){return mScreens;}
    public int getFunctions(){return mFunctions;}
    public int getNoDisturbStart(){return noDisturbStart;}
    public int getNoDisturbStop(){return noDisturbStop;}
    public int getSleepStart(){return sleepStart;}
    public int getSleepStop(){return sleepStop;}
    public int getBrightness(){return mBrightness;}

}
