package cwm.wearablesdk.handler;

import android.util.Log;

import cwm.wearablesdk.CwmManager;
import cwm.wearablesdk.Payload;
import cwm.wearablesdk.Task;
import cwm.wearablesdk.events.AckEvents;

/**
 * Created by user on 2018/1/7.
 */

public class AckHandler {

    public AckEvents processAck(Payload data){
        AckEvents ackEvents = null;
        byte[] packet = data.getPacket();
        //if the ble data recived is the response from the command we just sent, then cancling the timer.
        if (packet[3] == Task.currentTask.type && packet[4] == Task.currentTask.id) {
            Task.taskReceivedHandler.removeCallbacks(Task.currentTask);
            ackEvents = new AckEvents(packet[3], packet[4]);
        }
        return ackEvents;
    }
}
