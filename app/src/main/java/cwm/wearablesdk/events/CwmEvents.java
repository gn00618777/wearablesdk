package cwm.wearablesdk.events;

/**
 * Created by user on 2017/8/31.
 */

import cwm.wearablesdk.Bias;
import cwm.wearablesdk.CwmManager;
import cwm.wearablesdk.settings.AlarmSetting;
import cwm.wearablesdk.settings.BodySettings;
import cwm.wearablesdk.settings.SystemSetting;
import cwm.wearablesdk.settings.IntelligentSettings;

public class CwmEvents{
    private int mId = 0;

    /************walk info**************/
    private int mItem;
    private int mItemCount;
    private int mStrength;
    private int mLength;
    private int mParserLength;
    private byte[] value;
    private short[] parser;
    private int remindPackages;
    private int deviceCurrent;

    private int syncStatus;
    private int mProgress;
    private byte[] mRawByte;
    private float[] mSensor_acc;
    private float[] mSensor_gyro;

    /******new protocol****/
    private int mMsg_type;
    private int mSensorID;
    private int mMessageID;
    private int mSensorTag;
    private int mRawHeart;
    private int mWalkStep;
    private int mDistance;
    private int mStepFreq;
    private int mTabataCalories;
    private int mStatus;
    private int mBattery;
    private float mVersion;
    private int mSelfTest;
    private int mCalibrateStatus;
    private int mTabataHeartBeat;
    private int mMapId;
    private int mCurrentSize;
    private int mMaxSize;
    private Bias mBias;
    private BodySettings mBody;
    private IntelligentSettings mIntelligent;
    private AlarmSetting mAlarm;
    private SystemSetting mSystem;

     public CwmEvents(){
          mId = 0;
          mWalkStep = 0;
          mDistance = 0;
          mStepFreq = 0;
          mTabataCalories = 0;
          mStatus = 0;
          mTabataHeartBeat = 0;
          mBattery = 0;
          mItem = 0;
          mItemCount = 0;
          mStrength = 0;
          mLength = 0;
         mParserLength = 0;
         mSelfTest = 0;
         mBias = new Bias();
         mBody = new BodySettings();
         mIntelligent = new IntelligentSettings();
         mAlarm = new AlarmSetting();
         mMapId = 0;
         mMaxSize = 0;
         mCurrentSize = 0;
    }

    public void setId(int id){mId = id;}

    public void setStrength(int strength){mStrength = strength;}

    public void setSleepCombined(byte[] value){this.value = value;}
    public void setSleepLogLength(int length){mLength = length;}
    public void setSleepParser(short[] parser){this.parser = parser;}
    public void setParserLength(int length){ mParserLength = length;}
    public void setRemindPackages(int number){remindPackages = number;}
    public void setFlashSyncStatus(int status){syncStatus = status;}
    public void setEraseProgress(int progress){mProgress = progress;}

    /*******new protocol******/
    public void setMsgType(int type){
        mMsg_type = type;
    }
    public void setSensorID(int id){
        mSensorID = id;
    }
    public void setMessageID(int id){ mMessageID = id;}
    public void setSensorTag(int tag){
        mSensorTag = tag;
    }
    public void setAccSensor(float[] sensor_acc){
        mSensor_acc = sensor_acc;
    }
    public void setGyroSensor(float[] mSensor_gyro){this.mSensor_gyro = mSensor_gyro;}
    public void setHeartSensor(int heart){mRawHeart = heart;}
    public void setStepCount(int count){mWalkStep = count;}
    public void setDistance(int distance){mDistance = distance;}
    public void setStepFreq(int freq){mStepFreq = freq;}
    public void setExerciseItem(int item){ mItem = item;}
    public void setDoItemCount(int count){mItemCount = count;}
    public void setTabataHeart(int heart){mTabataHeartBeat = heart;}
    public void setTabataCalories(int calories){mTabataCalories = calories;}
    public void setBattery(int battery){mBattery = battery;}
    public void setVersion(float version){mVersion = version;}
    public void setmRawByte(byte[] raw){mRawByte = raw;}
    public void setDeviceRecord(int current){deviceCurrent = current;}
    public void setStatus(int status){mStatus = status;}
    public void setSelfTest(int selfTest){mSelfTest = selfTest;}
    public void setCalibateStatus(int calibrateStatus){mCalibrateStatus = calibrateStatus;}
    public void setBias(Bias bias){mBias = bias;}
    public void setCurrentSize(int size){mCurrentSize = size;}
    //public void setMaxSize(int size){mMaxSize = size;}

    public void setBody(BodySettings body){
        mBody = body;
    }
    public void setIntelligent(IntelligentSettings intelligent){
        mIntelligent = intelligent;
    }
    public void setAlarm(AlarmSetting alarm){
        mAlarm = alarm;
    }
    public void setSystem(SystemSetting system){mSystem = system;}
    public void setMapId(int id){mMapId = id;}

    public int getSleepLength(){return mLength;}
    public byte[] getSleepCombined(){return this.value;}
    public short[] getSleepParser(){return this.parser;}
    public int getParserLength(){return mParserLength;}
    public int getRemindPackages(){return remindPackages;}
    public int getDeviceCurrent(){return deviceCurrent;}
    public int getSyncStatus(){return syncStatus;}
    public int getEraseProgress(){return mProgress;}
    public int getId() {
        return mId;
    }
    public byte[] getRawBytes(){
        return mRawByte;
    }
    public float[] getSensorAccData(){return mSensor_acc;}
    public float[] getSensorGyroData(){return mSensor_gyro;}
    public int getMapId(){return mMapId;}


    /*******new protocol*****/
    public int getSensorID(){return mSensorID;}
    public int getMessageID(){return mMessageID;}
    public int getSensorTag(){return mSensorTag;}
    public int getMsgType(){return mMsg_type;}
    public int getSensorHeart(){return mRawHeart;}
    public int getStepCount(){return mWalkStep;}
    public int getDistance(){return mDistance;}
    public int getStepFreq(){ return mStepFreq;}
    public int getBattery(){return mBattery;}
    public float getVersion(){return mVersion;}
    public int getHeartBeat(){return mRawHeart;}
    public int getStatus(){return mStatus;}
    public int getStrength(){return mStrength;}
    public int getExerciseItem(){
        return mItem;
    }
    public int getDoItemCount(){return mItemCount;}
    public int getTabataCalories(){return mTabataCalories;}
    public int getTabataHeart(){return mTabataHeartBeat;}
    public int getSelfTestResult(){return mSelfTest;}
    public int getCalibrateStatus(){return mCalibrateStatus;}
    public int getCurrentMapSize(){return mCurrentSize;}
    //public long getMaxMapSize(){return mMaxSize;}

    public Bias getBias(){return mBias;}
    public BodySettings getBody(){return mBody;}
    public IntelligentSettings getIntelligent(){return mIntelligent;}
    public AlarmSetting getAlarmSetting(){return mAlarm;}
    public SystemSetting getSystemSetting(){return mSystem;}

}
