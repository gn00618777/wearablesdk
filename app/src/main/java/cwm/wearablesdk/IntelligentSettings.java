package cwm.wearablesdk;

/**
 * Created by user on 2017/9/8.
 */

public class IntelligentSettings {
      private int tag;
      private boolean sedtentary;
      private boolean hangUp;
      private boolean onWear;
      private boolean doubleTap;
      private boolean wristSwitch;
      private int goal;
      public IntelligentSettings(){
          tag = CwmManager.INTELLIGENT;
          sedtentary = false;
          hangUp = true;
          onWear = true;
          doubleTap = false;
          wristSwitch = false;
          goal = 8000;
      }

      public void setSedtentary(boolean s){this.sedtentary = s;}
      public void setHangUp(boolean s){this.hangUp = s;}
      public void setOnWear(boolean s){this.onWear = s;}
      public void setDoubleTap(boolean s){this.doubleTap = s;}
      public void setWristSwitch(boolean s){this.wristSwitch = s;}
      public void setGoal(int g){this.goal = g;}

      public boolean getSedtentary(){return this.sedtentary;}
      public boolean getHangUp(){return this.hangUp;}
      public boolean getOnWear(){return this.onWear;}
      public boolean getDoubleTap(){return this.doubleTap;}
      public boolean getWristSwitch(){return this.wristSwitch;}
      public int getGoal(){return this.goal;}



}
