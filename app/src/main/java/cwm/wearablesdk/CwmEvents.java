package cwm.wearablesdk;

/**
 * Created by user on 2017/8/31.
 */

public class CwmEvents extends Information{
    private int mId = 0;

    /************walk info**************/
    private int mWalkStep;
    private int mDistance;
    private int mCalories;
    private int mRunStep;
    private int mStatus;
    private int mBattery;
    private int mItem;
    private int mItemCount;
    private int mTabataStatus;
    private int mStrength;
    private float mVersion;
    private int mLength;
    private int mParserLength;
    private byte[] value;
    private int[] parser;
    private int maxByte;
    private int deviceCurrent;
    private int[] mGestureList;
    private int syncStatus;
    private int mProgress;
    private byte[] mRawByte;
    private float[] mSensor_acc;
    private float[] mSensor_gyro;
    private int sensorType;
    private int mTrustLevel;
    private short mSignalGrade;
    private float mTemperature;
    private float mPressure;


    /**************heart beat*************/
    private int mHeartBeat;

     CwmEvents(){
          mId = 0;
          mWalkStep = 0;
          mDistance = 0;
          mCalories = 0;
          mStatus = 0;
          mHeartBeat = 0;
          mBattery = 0;
          mItem = 0;
          mItemCount = 0;
          mTabataStatus = 0;
          mStrength = 0;
          mLength = 0;
         mParserLength = 0;
         sensorType = 0;
    }

    public void setId(int id){mId = id;}
    public void setHeartBeat(int heartBeat){mHeartBeat = heartBeat;}
    public void setWalkStep(int walkStep){mWalkStep = walkStep;}
    public void setDistance(int distance){mDistance = distance;}
    public void setCalories(int calories){mCalories = calories;}
    public void setStatus(int status){mStatus = status;}
    public void setBattery(int battery){mBattery = battery;}
    public void setExerciseItem(int item){ mItem = item;}
    public void setDoItemCount(int count){mItemCount = count;}
    public void setTabataStatus(int status){mTabataStatus = status;}
    public void setStrength(int strength){mStrength = strength;}
    public void setSwVersion(float version){mVersion = version;}
    public void setSleepCombined(byte[] value){this.value = value;}
    public void setSleepLogLength(int length){mLength = length;}
    public void setSleepParser(int[] parser){this.parser = parser;}
    public void setParserLength(int length){ mParserLength = length;}
    public void setMaxByte(int max){maxByte = max;}
    public void setGestureList(int[] gesture) {mGestureList = gesture;}
    public void setFlashSyncStatus(int status){syncStatus = status;}
    public void setEraseProgress(int progress){mProgress = progress;}
    public void setSensors(float[] sensor_acc, float[] sensor_gyro){
        mSensor_acc = sensor_acc;
        mSensor_gyro = sensor_gyro;
    }
    public void setTrustLevel(int trustLevel){mTrustLevel = trustLevel;}
    public void setSignalGrade(short signal){mSignalGrade = signal;}
    public void setSensorType(int type){sensorType = type;}
    public void setTemperature(float temperature){mTemperature = temperature;}
    public void setPressure(float pressure){mPressure = pressure;}
    public void setmRawByte(byte[] raw){mRawByte = raw;}
    public void setDeviceRecord(int current){deviceCurrent = current;}
    public int getWalkStep(){
       return mWalkStep;
    }
    public int getDistance(){
        return mDistance;
    }
    public int getCalories(){ return mCalories;}
    public int getRunStep(){ return mRunStep;}
    public int getHeartBeat(){return mHeartBeat;}
    public int getStatus(){return mStatus;}
    public int getTabataStatus(){return mTabataStatus;}
    public int getBattery(){return mBattery;}
    public int getExerciseItem(){
      return mItem;
    }
    public int getDoItemCount(){return mItemCount;}
    public int getStrength(){return mStrength;}
    public float getVersion(){return mVersion;}
    public int getSleepLength(){return mLength;}
    public byte[] getSleepCombined(){return this.value;}
    public int[] getSleepParser(){return this.parser;}
    public int getParserLength(){return mParserLength;}
    public int getMaxByte(){return maxByte;}
    public int getDeviceCurrent(){return deviceCurrent;}
    public int[] getGestureList(){return mGestureList;}
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
    public int getSensorType(){return sensorType;}
    public int getTrustLevel(){return mTrustLevel;}
    public short getSignalGrade(){return mSignalGrade;}
    public float getTemperature(){return mTemperature;}
    public float getPressure(){return mPressure;}
}
