package cwm.wearablesdk;

/**
 * Created by user on 2017/11/2.
 */

public class TabataObject {

    private String itemName;
    private int itemPos;

    public TabataObject(){
        itemName = "";
        itemPos = 0;
    }

    public void setItemName(String name){
        itemName = name;
    }
    public void setItemPos(int pos){
        itemPos = pos;
    }
    public String getItemName(){
        return itemName;
    }
    public int getItemPos(){
        return itemPos;
    }
}
