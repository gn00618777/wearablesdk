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
-keep class cwm.wearablesdk.CwmEvents
-keep class cwm.wearablesdk.AckEvents
-keep class cwm.wearablesdk.Information
-keep class cwm.wearablesdk.Settings
-keep class cwm.wearablesdk.BodySettings
-keep class cwm.wearablesdk.IntelligentSettings
-keep class cwm.wearablesdk.TabataSettings
-keep class cwm.wearablesdk.TabataObject
-keep class cwm.wearablesdk.TabataTask
-keep class cwm.wearablesdk.ErrorEvents
-keep class cwm.wearablesdk.CwmManager$EventListener{*;}
-keep class cwm.wearablesdk.CwmManager$WearableServiceListener{*;}
-keep class cwm.wearablesdk.CwmManager$BleScannerListener{*;}
-keep class cwm.wearablesdk.CwmManager$AckListener{*;}
-keep class cwm.wearablesdk.CwmManager$ErrorListener{*;}
-keep class cwm.wearablesdk.CwmManager$LogSyncListener{*;}
-keep class cwm.wearablesdk.Task

-keepclassmembers class cwm.wearablesdk.Task {
   public *;
}

-keepclassmembers class cwm.wearablesdk.CwmManager {
   public *;
}
-keepclassmembers class cwm.wearablesdk.CwmEvents {
   public int getWalkStep();
   public int getDistance();
   public int getCalories();
   public int getRunStep();
   public int getHeartBeat();
   public int getStatus();
   public int getBattery();
   public int getId();
   public int  getTabataStatus();
   public int getDoItemCount();
   public int getExerciseItem();
   public int getStrength();
   public float getVersion();
  public int getSleepLength();
  public byte[] getSleepCombined();
  public int[] getSleepParser();
  public int getParserLength();
 public int getMaxByte();
 public int getDeviceCurrent();
 public int[] getGestureList();
 public int getSyncStatus();
}
-keepclassmembers class cwm.wearablesdk.AckEvents{
public *;
}

-keepclassmembers class cwm.wearablesdk.BodySettings {
   public *;
}
-keepclassmembers class cwm.wearablesdk.IntelligentSettings {
   public *;
}
-keepclassmembers class cwm.wearablesdk.Settings {
   public *;
}
-keepclassmembers class cwm.wearablesdk.TabataSettings {
 public *;
}
-keepclassmembers class cwm.wearablesdk.ErrorEvents {
 public *;
}
-keepclassmembers class cwm.wearablesdk.TabataObject {
 public *;
}
-keepclassmembers class cwm.wearablesdk.TabataTask{
public *;
}