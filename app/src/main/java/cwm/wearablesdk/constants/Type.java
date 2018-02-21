package cwm.wearablesdk.constants;

/**
 * Created by user on 2017/12/3.
 */

public class Type {

    public enum BLE_PAKAGE_TYPE{
        SHORT_MESSAGE,
        LONG_MESSAGE_START,
        LONG_MESSAGE_MID,
        LONG_MESSAGE_END,
    };
    public enum FLASH_SYNC_TYPE{
        SYNC_DATA_LENGTH,
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

    public enum OS_CONFIG{
        ANDROID,
        IOS
    }

    public enum TIME_FORMAT{
        TIME_HOUR_0_23,
        TIME_HOUR_1_12
    }

    public enum GENDER{
        MALE,
        FEMALE
    }

    public enum GESTURE{
        STEP_COINTER,
        CUSTOMISED_PEDOMETER,
        SIGNIFICANT_MOTION,
        HAND_UP,
        TAP,
        WATCH_TAKE_OFF,
        ACTIVITY_RECOGNITION,
        SLEEPING,
        SEDENTARY,
        WRIST_SCROLL,
        SHAKE,
        FALL,
        FLOOR_CLIMBED,
        SKIPPING,
    }

    public enum APPIDENTIFIER{
        QQ_MESSAGE,
        WECHART_MESSAGE,
        DOBAN_MESSAGE,
        OTHER
    }

    public enum FACTORY_OPERATE{
        NULL,
        DFU,
        SELF_TEST,
        CALIBRATE,
        RECORD_SENSOR_DATA,
        UPDATE_BITMAP,
        UPDATE_FONT_LIB

    }

    public enum CALIBRATE_RESULT {
        NULL,
        CALIB_STATUS_PASS,
        CALIB_STATUS_OUT_OF_RANGE,
        CALIB_STATUS_FAIL,
        CALIB_STATUS_OUT,
        CALIB_STATUS_NO_TEST_ITEM
    };

    public static final int SYSTTEM_INFORMATION = 0x01;
    public static final int SENSOR_GESTURE_REPORT_MESSAGE = 0x02;
    public static final int HISTORY_DATA_RESPONSE = 0x03;
    public static final int COMMAND_RESPONSE = 0x04;
    public static final int FACTORY_RESPONSE = 0x05;
    public static final int ACK_INFORMATION = 0x7F;

    public static final int SYSTEM_INFORMATION_COMMAND = 0x81;
    public static final int SENSOR_GESTURE_COMMAND = 0x82;
    public static final int HISTORY_DTAA_COMMAND = 0x83;
    public static final int COMMAND_COMMAND = 0x84;
    public static final int FACTORY_DATA_COMMAND = 0x85;

    public static final int QQ_MESSAGE = 0x00;
    public static final int WECHART_MESSAGE = 0x01;
    public static final int DOBAN_MESSAGE = 0x02;
    public static final int OTHER = 0x03;

    //OLED page size
    public static final int OLED_PAGE_SIZE = 48*1024;
    public static final int BITMAP_PAHE_SIZE = 264*1024;
    public static final int FONT_LIB = 32*1024;
    //

}
