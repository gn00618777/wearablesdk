package cwm.wearablesdk;

/**
 * Created by user on 2017/9/10.
 */

public class JniManager {

    static {
        System.loadLibrary("wearable");
    }
    public native void getSyncIntelligentCommand(boolean[] feature, int goal, byte[] command);
    public native void getSyncBodyCommandCommand(int[] body, byte[] command);
    public native void getSyncCurrentCommand(int[] time, byte[] command);
    public native void getRequestBatteryCommand(byte[] command);
    public native void getTabataCommand(int operate, int prepare, int interval, int action_item, byte[] command);
    public native void getCwmInformation(int id, byte[] input, int[] info);
    public native int getType(byte[] rxBuffer);
    public native void getSleepLogCommand(byte[] command);
    public native void getRequestSwVersionCommand(byte[] command);
    public native void getSwitchOTACommand(byte[] command);
    public native void getCwmSleepInfomation(int id, byte[] input, float[] output);
    public native void getSedentaryRemindTimeCommand(int time, byte[] command);
    public native void getReadFlashCommand(int type, byte[] command);
    public native void getRequestMaxLogPacketsCommand(byte[] command);
}
