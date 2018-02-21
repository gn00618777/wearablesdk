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

    private CwmManager cwmManager;

    public AckHandler(CwmManager manager){
        cwmManager = manager;
    }

    public void processAck(Payload data){
        byte[] packet = data.getPacket();
        //if the ble data recived is the response from the command we just sent, then cancling the timer.
        for(int i = 0; i < Task.taskList.size() ; i++) {
            Task task = Task.taskList.get(i);
            if (packet[3] == task.type && packet[4] == task.id) {
                Task.taskReceivedHandler.removeCallbacks(task);
                Task.taskList.remove(i);
                AckEvents ackEvents = new AckEvents(packet[3], packet[4]);
                cwmManager.getAckListener().onAckArrival(ackEvents);
            }
        }
    }
}
