package cwm.wearablesdk;

/**
 * Created by user on 2018/1/28.
 */

public class Bias {
    float x;
    float y;
    float z;

    public Bias(){
        x = 0;
        y = 0;
        z = 0;
    }

    public void set(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public float getX(){return x;}
    public float getY(){return y;}
    public float getZ(){return z;}
}
