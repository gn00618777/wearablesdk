package cwm.wearablesdk;

/**
 * Created by user on 2017/8/31.
 */

public class CwmInformation extends Information{
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


    /**************heart beat*************/
    private int mHeartBeat;

     CwmInformation(){
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

    public int getId() {
        return mId;
    }
}
