package cwm.wearablesdk;

import android.util.Log;

/**
 * Created by user on 2017/12/3.
 */

public class QueueHandler {

    private Parser mParser = new Parser();

    public void deQueue(){
        if(CwmManager.mOutPutQueue.size() != 0){
            Data data = CwmManager.mOutPutQueue.poll();
            if(data.getMessageID() == CwmManager.mCurrentTask.getCommand() ||
                    data.getMessageID() == ID.RECEIVED_FLASH_COMMAND_ID){
                CwmManager.isTaskHasComplete = true;
            }
            else if(data.getMessageID() == ID.TABATA_EVENT_MESSAGE_ID &&
                    CwmManager.mCurrentTask.getCommand() == ID.TABATA_COMMAND_ID){
                CwmManager.isTaskHasComplete = true;
            }
            if(data.getIdType() == ID.ACK) {
                int id = data.getMessageID();
                AckEvents ackEvents = new AckEvents();
                switch (id) {
                    case ID.SYNC_TIME_RESPONSE_ID:
                        ackEvents.setId(ID.SYNC_TIME_RESPONSE_ID);
                        CwmManager.mAckListener.onAckArrival(ackEvents);
                        break;
                    case ID.BODY_PARAMETER_RESPONSE_ID:
                        ackEvents.setId(ID.BODY_PARAMETER_RESPONSE_ID);
                        CwmManager.mAckListener.onAckArrival(ackEvents);
                        break;
                    case ID.INTELLIGENT_FEATURE_RESPONSE_ID:
                        ackEvents.setId(ID.INTELLIGENT_FEATURE_RESPONSE_ID);
                        CwmManager.mAckListener.onAckArrival(ackEvents);
                        break;
                    case ID.CLEAN_BOND_RESPONSE_ID:
                        break;
                    case ID.SLEEP_REPORT_MESSAGE_ID:
                        ackEvents.setId(ID.SLEEP_REPORT_MESSAGE_ID);
                        CwmManager.mAckListener.onAckArrival(ackEvents);
                        break;
                    case ID.TABATA_COMMAND_ID:
                        ackEvents.setId(ID.TABATA_EVENT_MESSAGE_ID);
                        CwmManager.mAckListener.onAckArrival(ackEvents);
                        break;
                    case ID.RECORD_SENSOR_ID:
                        ackEvents.setId(ID.RECORD_SENSOR_ID);
                        CwmManager.mAckListener.onAckArrival(ackEvents);
                        break;
                    case ID.CALIBRATE_COMMAND_ID:
                        ackEvents.setId(ID.CALIBRATE_COMMAND_ID);
                        CwmManager.mAckListener.onAckArrival(ackEvents);
                        break;
                    default:
                        break;
                }
            }
            else if(data.getIdType() == ID.NACK){

            }
            else{
                int id = data.getIdType();
                byte[] value = data.getValue();
                CwmEvents cwmEvent;
                switch (id){
                    case ID.MOTION_DATA_REPORT_MESSAGE_ID:
                        cwmEvent = mParser.getInfomation(ID.MOTION_DATA_REPORT_MESSAGE_ID, value);
                        CwmManager.mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.BATTERY_STATUS_REPORT_MESSAGE_ID:
                        cwmEvent = mParser.getInfomation(ID.BATTERY_STATUS_REPORT_MESSAGE_ID, value);
                        CwmManager.mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.TAP_EVENT_MESSAGE_ID:
                        cwmEvent = mParser.getInfomation(ID.TAP_EVENT_MESSAGE_ID, value);
                        CwmManager.mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.WRIST_SCROLL_EVENT_MESSAGE_ID:
                        cwmEvent = mParser.getInfomation(ID.WRIST_SCROLL_EVENT_MESSAGE_ID, value);
                        CwmManager.mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.SEDENTARY_EVENT_MESSAGE_ID:
                        cwmEvent = mParser.getInfomation(ID.SEDENTARY_EVENT_MESSAGE_ID, value);
                        CwmManager.mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.SHAKE_EVENT_MESSAGE_ID:
                        cwmEvent = mParser.getInfomation(ID.SHAKE_EVENT_MESSAGE_ID, value);
                        CwmManager.mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.SIGNIFICANT_EVENT_MESSAGE_ID:
                        cwmEvent = mParser.getInfomation(ID.SIGNIFICANT_EVENT_MESSAGE_ID, value);
                        CwmManager.mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.HART_RATE_EVENT_MESSAGE_ID:
                        cwmEvent = mParser.getInfomation(ID.HART_RATE_EVENT_MESSAGE_ID, value);
                        CwmManager.mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.TABATA_EVENT_MESSAGE_ID:
                        cwmEvent = mParser.getInfomation(ID.TABATA_EVENT_MESSAGE_ID, value);
                        CwmManager.mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.SOFTWARE_VERSION_MESSAGE_ID:
                        cwmEvent = mParser.getInfomation(ID.SOFTWARE_VERSION_MESSAGE_ID, value);
                        CwmManager.mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.SLEEP_REPORT_MESSAGE_ID:
                        cwmEvent = mParser.getInfomation(ID.SLEEP_REPORT_MESSAGE_ID, value);
                        CwmManager.mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.RECEIVED_FLASH_COMMAND_ID:
                        mParser.parseFlashInformation(data);
                        CwmManager.acculateByte++;
                        if(CwmManager.maxBytes > 0) {
                            CwmManager.mLogListener.onProgressChanged(CwmManager.acculateByte, CwmManager.maxBytes);
                            if (CwmManager.acculateByte == CwmManager.maxBytes) {
                                CwmManager.acculateByte = 0;
                            }
                        }
                        break;
                    case ID.READ_FLASH_COMMAND_ID:
                        cwmEvent = mParser.getInfomation(ID.READ_FLASH_COMMAND_ID, value);
                        CwmManager.mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.REQUEST_MAX_LOG_PACKETS_ID:
                        cwmEvent = mParser.getInfomation(ID.REQUEST_MAX_LOG_PACKETS_ID, value);
                        CwmManager.maxBytes = cwmEvent.getMaxByte();
                        CwmManager.mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.GESUTRE_EVENT_MESSAGE_ID:
                        cwmEvent = mParser.getInfomation(ID.GESUTRE_EVENT_MESSAGE_ID, value);
                        CwmManager.mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.REQUEST_ERASE_EVENT_MESSAGE_ID:
                        cwmEvent = mParser.getInfomation(ID.REQUEST_ERASE_EVENT_MESSAGE_ID, value);
                        CwmManager.mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.CALIBRATE_DONE_MESSAGE_ID:
                        cwmEvent = mParser.getInfomation(ID.CALIBRATE_DONE_MESSAGE_ID, value);
                        CwmManager.mListener.onEventArrival(cwmEvent);
                        break;
                    case ID.SENSOR_REPORT_MESSAGE_ID:
                        cwmEvent = mParser.getInfomation(ID.SENSOR_REPORT_MESSAGE_ID, value);
                        CwmManager.mListener.onEventArrival(cwmEvent);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void enQueue(Data data){
        if(data.getDataType() == CwmManager.NON_PENDING && data.getLength() <= CwmManager.PACKET_SIZE && data.getIdType() == ID.ACK){
            CwmManager.mOutPutQueue.add(data);
            if(data.getMessageID() == CwmManager.mCurrentTask.getCommand()) {
                CwmManager.isTaskHasComplete = false;
                CwmManager.taskReceivedHandler.removeCallbacks(CwmManager.mCurrentTask);
            }
        }
        else if (data.getDataType() == CwmManager.NON_PENDING && data.getLength() <= CwmManager.PACKET_SIZE && data.getIdType() != ID.ACK) {
            //if we receive header in time, then cancle time out handler
            //because we send 0x20, but band will feedback 0x21
            if(data.getMessageID() == CwmManager.mCurrentTask.getCommand()) {
                if(data.getMessageID() != ID.READ_FLASH_COMMAND_ID){
                    CwmManager.isTaskHasComplete = false;
                    CwmManager.taskReceivedHandler.removeCallbacks(CwmManager.mCurrentTask);
                }
                if(data.getMessageID() == ID.READ_FLASH_COMMAND_ID){
                    Log.d("bernie","remove call back");
                    CwmManager.isTaskHasComplete = true;
                    CwmManager.taskReceivedHandler.removeCallbacks(CwmManager.mCurrentTask);
                    if(data.getTag() == Type.FLASH_SYNC_TYPE.SYNC_ABORT.ordinal()){
                        ErrorEvents errorEvents = new ErrorEvents();
                        errorEvents.setId(0x03); //flash sync aborted
                        errorEvents.setCommand(CwmManager.mCurrentTask.getCommand());
                        CwmManager.mErrorListener.onErrorArrival(errorEvents);
                        Log.d("bernie","sync aborted");
                    }
                    else if(data.getTag() == Type.FLASH_SYNC_TYPE.SYNC_DONE.ordinal()){
                        Log.d("bernie","sync done");
                        CwmManager.mLogListener.onSyncDone();
                    }
                }
            }
            if(CwmManager.mCurrentTask.getCommand() == ID.TABATA_COMMAND_ID && data.getMessageID() == ID.TABATA_EVENT_MESSAGE_ID ){
                CwmManager.isTaskHasComplete = false;
                CwmManager.taskReceivedHandler.removeCallbacks(CwmManager.mCurrentTask);
            }
            if( CwmManager.mCurrentTask.getCommand() == ID.READ_FLASH_COMMAND_ID && data.getMessageID() == ID.RECEIVED_FLASH_COMMAND_ID){
                CwmManager.isTaskHasComplete = false;
                CwmManager.taskReceivedHandler.removeCallbacks(CwmManager.mCurrentTask);
            }
            CwmManager.mOutPutQueue.add(data);
        }
    }

    public void enOtherQueue(Data data){
        if(data.getDataType() == CwmManager.LONE_MESSAGE){
            //if we receive header in time, then cancle time out handler
            if(data.getMessageID() == CwmManager.mCurrentTask.getCommand() ||
                    data.getMessageID() == ID.RECEIVED_FLASH_COMMAND_ID) {
                CwmManager.isTaskHasComplete = false;
                CwmManager.taskReceivedHandler.removeCallbacks(CwmManager.mCurrentTask);
            }
            CwmManager.hasLongMessage = true;
            CwmManager.targetLength = data.getLength() - CwmManager.PACKET_SIZE;
            CwmManager.messageID = data.getMessageID();
            data.setLength(CwmManager.PACKET_SIZE);
            CwmManager.mPendingQueue.add(data);

            //The timer for receiving long message
            CwmManager.longMessageHandler.postDelayed(CwmManager.mLongMessageTask,5000);
        }
        else if((data.getDataType() == CwmManager.PENDING) && (CwmManager.hasLongMessage == true)){
            CwmManager.lengthMeasure += data.getLength();
            CwmManager.mPendingQueue.add(data);
            Log.d("bernie","lengthMeasure:"+Integer.toString(CwmManager.lengthMeasure)+" targetLength:"+Integer.toString(CwmManager.targetLength));
            if(CwmManager.lengthMeasure == CwmManager.targetLength){
                CwmManager.skipClassify = false;
                CwmManager.longMessageHandler.removeCallbacks(CwmManager.mLongMessageTask);
                CwmManager.hasLongMessage = false; //long message has been received completely.
                byte[] value = new byte[CwmManager.targetLength+CwmManager.PACKET_SIZE];
                int queueSize = 0;
                int desPos = 0;

                queueSize = CwmManager.mPendingQueue.size();

                for(int i = 0 ; i < queueSize ; i++) {
                    Data entry = CwmManager.mPendingQueue.poll();
                    System.arraycopy(entry.getValue(), 0, value, desPos, entry.getLength());
                    desPos += entry.getLength();
                }

                CwmManager.mOutPutQueue.add(new Data(CwmManager.LONE_MESSAGE, (CwmManager.targetLength+CwmManager.PACKET_SIZE), CwmManager.messageID, 0, value));

                CwmManager.lengthMeasure = 0;
                CwmManager.targetLength = 0;
                CwmManager.messageID = 0;
            }
        }
    }

}
