package cwm.wearablesdk;

/**
 * Created by user on 2017/9/8.
 */

public class BodySettings{
    private int tag;
    private int old;
    private int hight;
    private char sex;
    private int weight;

    public BodySettings(){
        tag = CwmManager.BODY;
        old = 30;
        hight = 165;
        sex = 'M';
        weight = 55;
    }

    public int getOld(){return old;}
    public int getHight(){return hight;}
    public char getSex(){return sex;}
    public int getWeight(){return weight;}

    public void setOld(int old){this.old = old;}
    public void setHight(int hight){this.hight = hight;}
    public void setSex(char sex){
        if(sex == 'm' || sex == 'M' || sex == 'f' || sex == 'F')
           this.sex = sex;
    }
    public void setWeight(int weight){this.weight = weight;}
}
