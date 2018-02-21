package cwm.wearablesdk.settings;

import cwm.wearablesdk.settings.AlarmSetting;
import cwm.wearablesdk.settings.BodySettings;
import cwm.wearablesdk.settings.IntelligentSettings;
import cwm.wearablesdk.settings.SystemSetting;

/**
 * Created by user on 2018/1/8.
 */

public class UserConfig {
    private AlarmSetting mAlarmSetting;
    private BodySettings mBodySetting;
    private IntelligentSettings mIntelligentSettings;
    private SystemSetting mSystemSetting;

    public UserConfig(){
        mAlarmSetting = new AlarmSetting();
        mBodySetting = new BodySettings();
        mIntelligentSettings = new IntelligentSettings();
        mSystemSetting = new SystemSetting();
    }
    public void setAlarmSetting(AlarmSetting settings){
        mAlarmSetting = settings;
    }
    public void setPersonProfile(BodySettings bodySettings){
         mBodySetting = bodySettings;
    }
    public void setSystemSetting(SystemSetting systemSetting){mSystemSetting = systemSetting;}
    public void setIntelligentSetting(IntelligentSettings intelligentSetting){mIntelligentSettings = intelligentSetting;}
    public AlarmSetting getAlarmSetting(){
        return mAlarmSetting;
    }
    public BodySettings getBodySetting(){
        return mBodySetting;
    }
    public IntelligentSettings getIntelligentSetting(){
        return mIntelligentSettings;
    }
    public SystemSetting getSystemSetting(){return mSystemSetting;}

}
