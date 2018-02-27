# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\user\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class cwm.wearablesdk.CwmManager
-keep class cwm.wearablesdk.events.CwmEvents
-keep class cwm.wearablesdk.events.AckEvents
-keep class cwm.wearablesdk.events.ErrorEvents
-keep class cwm.wearablesdk.TabataSettings
-keep class cwm.wearablesdk.TabataObject
-keep class cwm.wearablesdk.TabataTask
-keep class cwm.wearablesdk.CwmManager$EventListener{*;}
-keep class cwm.wearablesdk.CwmManager$WearableServiceListener{*;}
-keep class cwm.wearablesdk.CwmManager$BleScannerListener{*;}
-keep class cwm.wearablesdk.CwmManager$AckListener{*;}
-keep class cwm.wearablesdk.CwmManager$ErrorListener{*;}
-keep class cwm.wearablesdk.CwmManager$LogSyncListener{*;}
-keep class cwm.wearablesdk.CwmManager$RawDataListener{*;}
-keep class cwm.wearablesdk.Task
-keep class cwm.wearablesdk.constants.Type
-keep class cwm.wearablesdk.constants.ID
-keep class cwm.wearablesdk.settings.UserConfig
-keep class cwm.wearablesdk.NotificationData
-keep class cwm.wearablesdk.settings.AlarmSetting
-keep class cwm.wearablesdk.settings.SystemSetting
-keep class cwm.wearablesdk.settings.BodySettings
-keep class cwm.wearablesdk.settings.IntelligentSettings
-keep class cwm.wearablesdk.Bias
-keep class java.lang.String


-keepclassmembers class cwm.wearablesdk.NotificationData {
   public *;
  public java.lang.String getAppName();
  public java.lang.String getPersoneName();
}

-keepclassmembers class cwm.wearablesdk.settings.AlarmSetting {
   public *;
}

-keepclassmembers class cwm.wearablesdk.settings.SystemSetting {
   public *;
}

-keepclassmembers class cwm.wearablesdk.settings.UserConfig {
   public *;
}

-keepclassmembers class cwm.wearablesdk.Task {
   public *;
}

-keepclassmembers class cwm.wearablesdk.CwmManager {
   public *;
}
-keepclassmembers class cwm.wearablesdk.events.CwmEvents {
   public int getDistance();
  public int getStepFreq();
   public int getRunStep();
   public int getHeartBeat();
   public int getStatus();
   public int getBattery();
   public int getId();
   public int  getTabataStatus();
   public int getDoItemCount();
   public int getExerciseItem();
    public int getTabataCalories();
    public int getTabataHeart();
   public int getStrength();
   public float getVersion();
  public short[] getSleepLength();
  public byte[] getSleepCombined();
  public int[] getSleepParser();
  public int getParserLength();
 public int getRemindPackages();
 public int getDeviceCurrent();
public boolean[] getGesture();
 public int getSyncStatus();
 public int getEraseProgress();
 public int getTag();
 public byte[] getRawBytes();
public float[] getSensorAccData();
public float[] getSensorGyroData();
 public int getSensorTag();
 public int getMsgType();
 public int getSensorID();
 public int getMessageID();
 public int getStepCount();
 public int getSelfTestResult();
 public int getCalibrateStatus();
 public int getMapId();
 public int getCurrentMapSize();
 public cwm.wearablesdk.Bias getBias();
 public cwm.wearablesdk.settings.BodySettings getBody();
 public cwm.wearablesdk.settings.IntelligentSettings getIntelligent();
 public cwm.wearablesdk.settings.AlarmSetting getAlarmSetting();
 public cwm.wearablesdk.settings.SystemSetting getSystemSetting();
}
-keepclassmembers class cwm.wearablesdk.events.AckEvents{
public *;
}

-keepclassmembers class cwm.wearablesdk.constants.Type{
public *;
}

-keepclassmembers class cwm.wearablesdk.constants.ID {
   public *;
}

-keepclassmembers class cwm.wearablesdk.Bias {
   public *;
}

-keepclassmembers class cwm.wearablesdk.settings.BodySettings {
   public *;
}
-keepclassmembers class cwm.wearablesdk.settings.IntelligentSettings {
   public *;
}
-keepclassmembers class cwm.wearablesdk.TabataSettings {
 public *;
}
-keepclassmembers class cwm.wearablesdk.events.ErrorEvents {
 public *;
}
-keepclassmembers class cwm.wearablesdk.TabataObject {
 public *;
}
-keepclassmembers class cwm.wearablesdk.TabataTask{
public *;
}