package cwm.wearablesdk.settings;

/**
 * Created by user on 2018/1/19.
 */

public class AlarmSetting {
    private int hour_1;
    private int minute_1;
    private int vibrate_1;
    private int week_1;
    private int hour_2;
    private int minute_2;
    private int vibrate_2;
    private int week_2;
    private int hour_3;
    private int minute_3;
    private int vibrate_3;
    private int week_3;
    private int hour_4;
    private int minute_4;
    private int vibrate_4;
    private int week_4;
    private int hour_5;
    private int minute_5;
    private int vibrate_5;
    private int week_5;
    private int hour_6;
    private int minute_6;
    private int vibrate_6;
    private int week_6;

    public AlarmSetting(){
        hour_1 = 0;
        minute_1 = 0;
        vibrate_1 = 1;
        week_1 = 0;
        hour_2 = 0;
        minute_2 = 0;
        vibrate_2 = 1;
        week_2 = 0;
        hour_3 = 0;
        minute_3 = 0;
        vibrate_3 = 0;
        week_3 = 0;
        hour_4 = 0;
        minute_4 = 0;
        vibrate_4 = 1;
        week_4 = 0;
        hour_5 = 0;
        minute_5 = 0;
        vibrate_5 = 1;
        week_5 = 0;
        hour_6 = 0;
        minute_6 = 0;
        vibrate_6 = 1;
        week_6 = 0;
    }

    public void setTime(int hour, int minute, int group){
        if(1 <= group && group <= 6){
            switch (group){
                case 1:
                    if(0 <= hour && hour <= 23)
                       hour_1 = hour;
                    if(0 <= minute && minute <= 59)
                       minute_1 = minute;
                break;
                case 2:
                    if(0 <= hour && hour <= 23)
                       hour_2 = hour;
                    if(0 <= minute && minute <= 59)
                       minute_2 = minute;
                    break;
                case 3:
                    if(0 <= hour && hour <= 23)
                      hour_3 = hour;
                    if(0 <= minute && minute <= 59)
                      minute_3 = minute;
                    break;
                case 4:
                    if(0 <= hour && hour <= 23)
                      hour_4 = hour;
                    if(0 <= minute && minute <= 59)
                      minute_4 = minute;
                    break;
                case 5:
                    if(0 <= hour && hour <= 23)
                      hour_5 = hour;
                    if(0 <= minute && minute <= 59)
                      minute_5 = minute;
                    break;
                case 6:
                    if(0 <= hour && hour <= 23)
                      hour_6 = hour;
                    if(0 <= minute && minute <= 59)
                      minute_6 = minute;
                    break;
                default:
                    break;
            }
        }
    }

    public void setVibrate(int second, int group){
        if( 1 <= group && group <= 6 && (second <= 0xFF && second >= 0)){
            switch (group){
                case 1:
                    vibrate_1 = second;
                break;
                case 2:
                    vibrate_2 = second;
                    break;
                case 3:
                    vibrate_3 = second;
                    break;
                case 4:
                    vibrate_4 = second;
                    break;
                case 5:
                    vibrate_5 = second;
                    break;
                case 6:
                    vibrate_6 = second;
                    break;
            }
        }
    }

    public void setWeek(int week, int group){
            if(1 <= group && group <= 6){
                switch (group){
                    case 1:
                        if(week > 0)
                            week_1 = week;
                        else
                            week_1 = 0;
                    break;
                    case 2:
                        if(week > 0)
                            week_2 = week;
                        else
                            week_2 = 0;
                        break;
                    case 3:
                        if(week > 0)
                            week_3 = week;
                        else
                            week_3 = 0;
                        break;
                    case 4:
                        if(week > 0)
                            week_4 = week;
                        else
                            week_4 = 0;
                        break;
                    case 5:
                        if(week > 0)
                            week_5 = week;
                        else
                            week_5 = 0;
                        break;
                    case 6:
                        if(week > 0)
                            week_6 = week;
                        else
                            week_6 = 0;
                        break;
                }
            }
    }
    public int getWeek(int group){

        if(group == 1) return week_1;
        else if(group == 2) return week_2;
        else if(group == 3) return week_3;
        else if(group == 4) return week_4;
        else if(group == 5) return week_5;
        else if(group == 6) return week_6;
        else
            return 0;
     }

     public int getHour(int group){
         if(group == 1) return hour_1;
         else if(group == 2) return hour_2;
         else if(group == 3) return hour_3;
         else if(group == 4) return hour_4;
         else if(group == 5) return hour_5;
         else if(group == 6) return hour_6;
         else
             return 0;
     }
    public int getMinute(int group){
        if(group == 1) return minute_1;
        else if(group == 2) return minute_2;
        else if(group == 3) return minute_3;
        else if(group == 4) return minute_4;
        else if(group == 5) return minute_5;
        else if(group == 6) return minute_6;
        else
            return 0;
    }
    public int getVibrat(int group){
        if(group == 1) return vibrate_1;
        else if(group == 2) return vibrate_2;
        else if(group == 3) return vibrate_3;
        else if(group == 4) return vibrate_4;
        else if(group == 5) return vibrate_5;
        else if(group == 6) return vibrate_6;
        else
            return 1;
    }
}
