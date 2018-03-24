package cwm.wearablesdk.constants;

/**
 * Created by user on 2017/11/27.
 */

public class ID {

    //Message ID
    public static final int ACCELERATION_RAW_DATA_REPORT = 0x01;
    public static final int GYRO_RAW_DATA_REPORT = 0x03;
    public static final int HEART_RATE_RAW_DATA_REPORT = 0x07;
    public static final int WRIST_SCROLL_EVENT_RESPONSE_MESSAGE = 0x12;
    public static final int SEDENTARY_RESPONSE_MESSAGE = 0x13;
    public static final int ACTIVITY_PEDOMETER_DATA_REPORT_MESSAGE = 0x14;
    public static final int TABATA_RESPONSE_MESSAGE = 0x15;
    public static final int SPORT_RESPONSE_MESSAGE = 0x20;
    public static final int ACK = 0x7F;
    public static final int NACK = 0xFF;

    //Message ID
    public static final int USER_CONFIG_INFO = 0x01;
    public static final int BATTERY_INFO = 0x02;
    public static final int DEVICE_VERSION_INFO = 0x03;
    public static final int RESET_USERCONFIG = 0x05;
    public static final int UNBOND = 0x06;

    /*Factory Mode*/
    //COMMAND ID
    public static final int DFU = 0x01;
    public static final int SELF_TEST_RESULT = 0x02;
    public static final int CALIBRATION_RESULT = 0x03;
    public static final int RECORD_SENSOR_DATA = 0x04;
    public static final int UPDATE_BASEMAP = 0x05;
    public static final int MAP_WRITE_DONE = 0x08;
    public static final int MAP_ERASE_DONE = 0x09;

    /*Factory Mode*/
    //Self-Test sensor id
    public static final int ADXL_ACC = 0x01;
    public static final int BMI160_ACC = 0x02;
    public static final int BMI160_GYRO = 0x03;
    public static final int HEART_RATAE = 0x04;
    public static final int VIBRATE_RATE = 0x05;
    public static final int BUTTON = 0x06;
    public static final int OLED = 0x07;
    public static final int FLASH = 0x08;

    public static final int INCOMING_CALL = 0x01;
    public static final int SOCIAL = 0x02;
    public static final int EMAIL = 0x03;
    public static final int NEWS = 0x04;
    public static final int MISSING_CALL = 0x05;
    public static final int PICK_UP = 0x06;

    //Error Event
    public static final int NO_ACK = 0x01;
    public static final int PACKET_LOST = 0x02;
    public static final int CHECKSUM_ERROR = 0x03;

    public static final int OLED_PAGE = 0x01;
    public static final int BITMAP_PAGE  = 0x02;
    public static final int FONT_LIB = 0x03;

    //History
    public static final int SLEEP_HISTORY = 0x01;
    public static final int LIFE_HISTORY = 0x02;
    public static final int HISTORY_PACKAGES = 0x03;
    public static final int LOG_HISTORY = 0x04;
    public static final int SYNC_ABORTED = 0x05;
    public static final int SYNC_DONE = 0x08;
    public static final int HISTORY_ERASE_DONE = 0x09;

    /*History command*/
    //message id
    public static final int REQUEST_HISTORY = 0x01;
    public static final int ERASE_HISTORY = 0x02;


}
