package cwm.wearablesdk;

/**
 * Created by user on 2018/4/12.
 */

public class LifeData {
     long timeStamp;
     int stepCount;
     int distance;
     int calories;
     int heartRate;
     int minHeartRate;
     int maxHeartRate;
     long sedentaryTriggerTime;
     int tpCount;
     int notificationCount;
     long displayOnTime;
     long vibOnTime;
     int bleSendCount;
     int bleReceiveCount;
     long tabataActionTime;
     int batteryLevel;
     int batteryCharge;
     int handUpDownCount;

    public void setTimeStamp(int timeStamp){
        this.timeStamp = timeStamp;
    }
    public void setStepCount(int stepCount){
        this.stepCount = stepCount;
    }
    public void setDistance(int distance){
        this.distance = distance;
    }
    public void setCalories(int calories){
        this.calories = calories;
    }
    public void setHeartRate(int heartRate){
        this.heartRate = heartRate;
    }
    public void setMinHeartRate(int minHeartRate){
        this.minHeartRate = minHeartRate;
    }
    public void setMaxHeartRate(int maxHeartRate){
        this.maxHeartRate = maxHeartRate;
    }
    public void setSedentaryTriggerTime(int sedentaryTriggerTime){
        this.sedentaryTriggerTime = sedentaryTriggerTime;
    }
    public void setTpCount(int tpCount){
        this.tpCount = tpCount;
    }
    public void setNotificationCount(int notificationCount){
        this.notificationCount = notificationCount;
    }
    public void setDisplayOnTime(int displayOnTime){
        this.displayOnTime = displayOnTime;
    }
    public void setVibOnTime(int vibOnTime){
        this.vibOnTime = vibOnTime;
    }
    public void setBleSendCount(int bleSendCount){
        this.bleSendCount = bleSendCount;
    }
    public void setBleReceiveCount(int bleReceiveCount){
        this.bleReceiveCount = bleReceiveCount;
    }
    public void setTabataActionTime(int tabataActionTime){
        this.tabataActionTime = tabataActionTime;
    }
    public void setBatteryLevel(int batteryLevel){
        this.batteryLevel = batteryLevel;
    }
    public void setBatteryCharge(int batteryCharge){
        this.batteryCharge = batteryCharge;
    }
    public void setHandUpDownCount(int handUpDownCount){
        this.handUpDownCount = handUpDownCount;
    }

    public long getTimeStamp(){
        return this.timeStamp;
    }
    public int getStepCount(){
        return this.stepCount;
    }
    public int getDistance(){
        return this.distance;
    }
    public int getCalories(){
        return this.calories;
    }
    public int getHeartRate(){
        return this.heartRate;
    }
    public int getMinHeartRate(){
        return this.minHeartRate;
    }
    public int getMaxHeartRate(){
        return this.maxHeartRate;
    }
    public long getSedentaryTriggerTime(){
        return this.sedentaryTriggerTime;
    }
    public int getTpCount(){
        return this.tpCount;
    }
    public int getNotificationCount(){
        return this.notificationCount;
    }
    public long getDisplayOnTime(){
        return this.displayOnTime;
    }
    public long getVibOnTime(){
        return this.vibOnTime;
    }
    public int getBleSendCount(){
        return this.bleSendCount;
    }
    public int getBleReceiveCount(){
        return this.bleReceiveCount;
    }
    public long getTabataActionTime(){
        return this.tabataActionTime;
    }
    public int getBatteryLevel(){
        return this.batteryLevel;
    }
    public int getBatteryCharge(){
        return this.batteryCharge;
    }
    public int getHandUpDownCount(){
        return this.handUpDownCount;
    }
}
