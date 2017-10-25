package cwm.wearablesdk;

/**
 * Created by user on 2017/8/31.
 */

public class AckEvents extends Information{
    private int mId = 0;

     AckEvents(){
          mId = 0;
    }

    public void setId(int id){mId = id;}


    public int getId() {
        return mId;
    }
}
