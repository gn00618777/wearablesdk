package cwm.wearablesdk;

/**
 * Created by user on 2017/9/8.
 */

public class Settings {
   public int tag;
   public int year;
   public int month;
   public int day;
   public int dayOfWeek;
   public int hour;
   public int minute;
   public int second;
    public Settings(){
        tag = 0;
        year = 17;
        month = 9;
        day = 9;
        dayOfWeek = 6;
        hour = 12;
        minute = 10;
        second = 0;
    }
    public int getYear(){return this.year;}
    public int getMonth(){return this.month;}
    public int getDay(){return this.day;}
    public int getDayOfWeek(){return this.dayOfWeek;}
    public int getHour(){return this.hour;}
    public int getMinute(){return this.minute;}
    public int getSecond(){return this.second;}

    public void setYear(int year){this.year = year;}
    public void setMonth(int month){this.month = month;}
    public void setDay(int day){this.day = day;}
    public void setDayOfWeek(int dayOfWeek){this.dayOfWeek = dayOfWeek;}
    public void setHour(int hour){this.hour = hour;}
    public void setMinute(int minute){this.minute = minute;}
    public void setSecond(int second){this.second = second;}
}
