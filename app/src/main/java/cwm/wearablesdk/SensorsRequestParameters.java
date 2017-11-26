package cwm.wearablesdk;

/**
 * Created by user on 2017/11/26.
 */

public class SensorsRequestParameters {

    int sensorType;
    int odrType;
    int sensorStatus;

    public void setParameters(int type, int odrType, int sensorStatus){
        this.sensorType = type;
        this.odrType = odrType;
        this.sensorStatus = sensorStatus;
    }

    public int getSensorType(){
        return this.sensorType;
    }
    public int getOdrType(){
        return this.odrType;
    }
    public int getSensorStatus(){
        return this.sensorStatus;
    }
}
