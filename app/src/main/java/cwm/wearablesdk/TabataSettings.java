package cwm.wearablesdk;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by user on 2017/9/14.
 */

public class TabataSettings {
    public static final int COUNT_DOWN = 0;
    public static final int COUNT_UP = 1;
    public enum ITEMS{
        NULL,
        PUSHUP,
        CRUNCH,
        SQUART,
        JUMPING_JACK,
        DIPS,
        HIGH_KNESSRUNNING,
        LUNGES,
        BURPEES,
        STEP_ON_CHAIR,
        PUSHUP_ROTATION
    };
    private int prepareTime; //sec
    private int actionType; //count down or count times
    private int actionTime; //sec
    private int actionTimes;
    private int intervalTime; //sec
    private int cycle;
    private boolean[] items;
    private String itemName;

    public TabataSettings(){
        itemName = "";
        prepareTime = 0;
        actionType = 3;
        actionTime = 0;
        actionTimes = 0;
        intervalTime = 0;
        cycle = 0;
        items = new boolean[11];
        items[ITEMS.PUSHUP.ordinal()] = false;
        items[ITEMS.CRUNCH.ordinal()] = false;
        items[ITEMS.SQUART.ordinal()] = false;
        items[ITEMS.JUMPING_JACK.ordinal()] = false;
        items[ITEMS.DIPS.ordinal()] = false;
        items[ITEMS.HIGH_KNESSRUNNING.ordinal()] = false;
        items[ITEMS.LUNGES.ordinal()] = false;
        items[ITEMS.BURPEES.ordinal()] = false;
        items[ITEMS.STEP_ON_CHAIR.ordinal()] = false;
        items[ITEMS.PUSHUP_ROTATION.ordinal()] = false;

    }
    public void setItemName(String name){
        itemName = name;
    }

    public void setPrepareTime(int prepareTime) {
        if(prepareTime >= 5 && prepareTime <= 255)
           this.prepareTime = prepareTime;
    }
    public void setActionType(int actionType){
        if(actionType == 0x0) {
            this.actionType = COUNT_DOWN;
        }
        else if(actionType == 0x1)
            this.actionType = COUNT_UP;
    }
    public void setActionTime(int actionTime){
        if(actionTime >= 5 && actionTime <= 65535 && actionType == COUNT_DOWN){
            this.actionTime = actionTime;
        }
    }
    public void setActionTimes(int actionTimes){
        if(actionTimes >= 3 && actionTimes <= 65535 && actionType == COUNT_UP){
            this.actionTimes = actionTimes;
        }
    }
    public void setIntervalTime(int intervalTime){
        if(intervalTime >= 5 && intervalTime <= 65535){
            this.intervalTime = intervalTime;
        }
    }
    public void setCycle(int cycle){
        if(cycle >= 1 && cycle <= 255){
            this.cycle = cycle;
        }
    }
    public int getTotalItemsNumber(){
        int total = 0;

        for(int i = ITEMS.PUSHUP.ordinal() ; i <= ITEMS.PUSHUP_ROTATION.ordinal() ; i++){
            if(items[i] == true)
                total++;
        }

        return total;
    }
    public int getPrepareTime(){
        return this.prepareTime;
    }
    public int getActionType(){
        return this.actionType;
    }
    public int getActionTime(){
        return this.actionTime;
    }
    public int getActionTimes(){
        return this.actionTimes;
    }
    public int getIntervalTime(){
        return this.intervalTime;
    }
    public int getCycle(){
        return this.cycle;
    }
    public void enableItem(int item){
        if(item == ITEMS.PUSHUP.ordinal()){
            this.items[item] = true;
        }
        else if(item == ITEMS.CRUNCH.ordinal()){
            this.items[item] = true;
        }
        else if(item == ITEMS.SQUART.ordinal()){
            this.items[item] = true;
        }
        else if(item == ITEMS.JUMPING_JACK.ordinal()){
            this.items[item] = true;
        }
        else if(item == ITEMS.DIPS.ordinal()){
            this.items[item] = true;
        }
        else if(item == ITEMS.HIGH_KNESSRUNNING.ordinal()){
            this.items[item] = true;
        }
        else if(item == ITEMS.LUNGES.ordinal()){
            this.items[item] = true;
        }
        else if(item == ITEMS.BURPEES.ordinal()){
            this.items[item] = true;
        }
        else if(item == ITEMS.STEP_ON_CHAIR.ordinal()){
            this.items[item] = true;
        }
        else if(item == ITEMS.PUSHUP_ROTATION.ordinal()){
            this.items[item] = true;
        }
    }
    public void disableItem(int item){
        if(item == ITEMS.PUSHUP.ordinal()){
            this.items[item] = false;
        }
        else if(item == ITEMS.CRUNCH.ordinal()){
            this.items[item] = false;
        }
        else if(item == ITEMS.SQUART.ordinal()){
            this.items[item] = false;
        }
        else if(item == ITEMS.JUMPING_JACK.ordinal()){
            this.items[item] = false;
        }
        else if(item == ITEMS.DIPS.ordinal()){
            this.items[item] = false;
        }
        else if(item == ITEMS.HIGH_KNESSRUNNING.ordinal()){
            this.items[item] = false;
        }
        else if(item == ITEMS.LUNGES.ordinal()){
            this.items[item] = false;
        }
        else if(item == ITEMS.BURPEES.ordinal()){
            this.items[item] = false;
        }
        else if(item == ITEMS.STEP_ON_CHAIR.ordinal()){
            this.items[item] = false;
        }
        else if(item == ITEMS.PUSHUP_ROTATION.ordinal()){
            this.items[item] = false;
        }
    }
    public boolean isEnabled(int item){
        if(item == ITEMS.NULL.ordinal()){
         return this.items[item];
        }
        if(item == ITEMS.PUSHUP.ordinal()){
            return this.items[item];
        }
        else if(item == ITEMS.CRUNCH.ordinal()){
            return this.items[item];
        }
        else if(item == ITEMS.JUMPING_JACK.ordinal()){
            return this.items[item];
        }
        else if(item == ITEMS.DIPS.ordinal()){
            return this.items[item];
        }
        else if(item == ITEMS.SQUART.ordinal()){
            return this.items[item];
        }
        else if(item == ITEMS.PUSHUP_ROTATION.ordinal()){
            return this.items[item];
        }
        else if(item == ITEMS.LUNGES.ordinal()){
            return this.items[item];
        }
        else if(item == ITEMS.BURPEES.ordinal()){
            return this.items[item];
        }
        else if(item == ITEMS.STEP_ON_CHAIR.ordinal()){
            return this.items[item];
        }
        else if(item == ITEMS.HIGH_KNESSRUNNING.ordinal()){
            return this.items[item];
        }
        else
            return false;
    }

    public boolean[] getItems(){
        return items;
    }
    public String getItemName(){
        if(items[ITEMS.PUSHUP.ordinal()])
            itemName = "Push Up";
        else if(items[ITEMS.CRUNCH.ordinal()])
            itemName = "Crunch";
        else if(items[ITEMS.SQUART.ordinal()])
            itemName = "Squart";
        else if(items[ITEMS.JUMPING_JACK.ordinal()])
            itemName = "Jumping Jack";
        else if(items[ITEMS.DIPS.ordinal()])
            itemName = "Dips";
        else if(items[ITEMS.HIGH_KNESSRUNNING.ordinal()])
            itemName = "High Kniess Running";
        else if(items[ITEMS.LUNGES.ordinal()])
            itemName = "Lunges";
        else if(items[ITEMS.BURPEES.ordinal()])
            itemName = "Burpees";
        else if(items[ITEMS.STEP_ON_CHAIR.ordinal()])
            itemName = "Step On Chair";
        else if(items[ITEMS.PUSHUP_ROTATION.ordinal()])
            itemName = "PushUp Rotation";
        return itemName;
    }
    public int getItemPos(){
        int pos = 0;

        if(items[ITEMS.PUSHUP.ordinal()])
            pos = ITEMS.PUSHUP.ordinal();
        else if(items[ITEMS.CRUNCH.ordinal()])
            pos = ITEMS.CRUNCH.ordinal();
        else if(items[ITEMS.SQUART.ordinal()])
            pos = ITEMS.SQUART.ordinal();
        else if(items[ITEMS.JUMPING_JACK.ordinal()])
            pos = ITEMS.JUMPING_JACK.ordinal();
        else if(items[ITEMS.DIPS.ordinal()])
            pos = ITEMS.DIPS.ordinal();
        else if(items[ITEMS.HIGH_KNESSRUNNING.ordinal()])
            pos = ITEMS.HIGH_KNESSRUNNING.ordinal();
        else if(items[ITEMS.LUNGES.ordinal()])
            pos = ITEMS.LUNGES.ordinal();
        else if(items[ITEMS.BURPEES.ordinal()])
            pos = ITEMS.BURPEES.ordinal();
        else if(items[ITEMS.STEP_ON_CHAIR.ordinal()])
            pos = ITEMS.STEP_ON_CHAIR.ordinal();
        else if(items[ITEMS.PUSHUP_ROTATION.ordinal()])
            pos = ITEMS.PUSHUP_ROTATION.ordinal();
        return pos;
    }
}
