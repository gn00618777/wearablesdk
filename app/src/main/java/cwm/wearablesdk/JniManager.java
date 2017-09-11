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
    public native void getCwmInformation(int id, byte[] input, int[] info);
}
