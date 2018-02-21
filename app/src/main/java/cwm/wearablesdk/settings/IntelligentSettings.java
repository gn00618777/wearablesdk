package cwm.wearablesdk.settings;

/**
 * Created by user on 2017/9/8.
 */

public class IntelligentSettings {
      private boolean[] gesture;
      private int gestureValue;
      private int goal;
      private int time;
      public IntelligentSettings(){
          gesture = new boolean[14];
          gesture[3] = true;
          gesture[5] = true;
          gesture[8] = true;
          gesture[9] = true;
          gestureValue = 0;
          goal = 0;
          time = 0;
      }
      public void enableGesture(int index){
           gesture[index] = true;
      }
      public void disableGesture(int index){
          gesture[index] = false;
      }

      public boolean[] getGesture(){
          return gesture;
      }
      public int getGestureValue(){return gestureValue;}

    public void setGoal(int g){this.goal = g;}
    public void setSedentaryTime(int s){this.time = s;}
    public void setGestureValue(int v){gestureValue = v;}

    public int getGoal(){return this.goal;}
    public int getTime(){return this.time;}


}
