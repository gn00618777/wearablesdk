package cwm.wearablesdk.settings;

import cwm.wearablesdk.constants.Type;

/**
 * Created by user on 2017/9/8.
 */

public class BodySettings{
    private int old;
    private int hight;
    private int sex;
    private int weight;

    public BodySettings(){
        old = 0;
        hight = 0;
        sex = Type.GENDER.MALE.ordinal();
        weight = 0;
    }

    public int getOld(){return old;}
    public int getHight(){return hight;}
    public int getSex(){return sex;}
    public int getWeight(){return weight;}

    public void setOld(int old){this.old = old;}
    public void setHight(int hight){this.hight = hight;}
    public void setSex(int sex){
        if(sex >= 0 && sex <=1)
           this.sex = sex;
    }
    public void setWeight(int weight){this.weight = weight;}
}
