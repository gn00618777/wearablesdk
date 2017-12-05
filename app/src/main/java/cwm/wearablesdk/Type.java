package cwm.wearablesdk;

/**
 * Created by user on 2017/12/3.
 */

public class Type {

    public enum BLE_PAKAGE_TYPE{
        ACK,
        MESSAGE,
        LONG_MESSAGE,
        PENDING
    };
    public enum FLASH_SYNC_TYPE{
        SYNC_START,
        SYNC_SUCCESS,
        SYNC_FAIL,
        SYNC_ABORT,
        SYNC_RESUME,
        SYNC_ERASE,
        SYNC_ERASE_DONE,
        SYNC_DONE
    };
    public enum ITEMS{
        TABATA_INIT,
        TABATA_PAUSE,
        TABATA_PREPARE_START,
        TABATA_PREPARE_COUNT,
        TABATA_PREARE_END,
        TABATA_REST_START,
        TABATA_REST_COUNT,
        TABATA_REST_END,
        TABATA_ACTION_ITEM,
        TABATA_ACTION_START,
        TABATA_ACTION_END,
        TABATA_REQUEST,
        TABATA_DONE,
        TABATA_RESUME
    };
    public enum PARAMETERS_TYPE{
        A,
        SENSORREQUEST
    }
}
