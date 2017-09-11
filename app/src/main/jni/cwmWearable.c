//
// Created by user on 2017/8/17.
//
#include "cwm_wearablesdk_JniManager.h"
#include <stdlib.h>

const jint CLICK_BTN = 0x01;
const jint HOME_BTN = 0x02;
const jint APP_BTN = 0x04;
const jint VOL_DOWN_BTN = 0x08;
const jint VOL_UP_BTN = 0x10;

JNIEXPORT void JNICALL Java_cwm_wearablesdk_JniMgr_getSyncIntelligentCommand
  (JNIEnv * env, jobject obj, jbooleanArray input, jint goal, jbyteArray output)
{

       // end = now_ms();
        //sum += ((end - start)/1000000);
      //}
     /* else{
         mAcc.x = 9999.0;
         mAcc.y = 9999.0;
         mAcc.z = 9999.0;
         mGyro.x = 0;
         mGyro.y = 0;
         mGyro.z = 0;
         mOri.x = 0;
         mOri.y = 0;
         mOri.z = 0;
         mBtns[0] = mBtns[1] = mBtns[2] = mBtns[3] = mBtns[4] = 0;
      }*/

}
JNIEXPORT void JNICALL Java_cwm_wearablesdk_JniManager_getSyncCurrentCommand
  (JNIEnv * env, jobject obj, jintArray input, jbyteArray output)
  {
          jint *rxData = (*env)->GetIntArrayElements(env, input, 0);
          jint checksum = 0xE6+0x90+0x0C+0x02+rxData[0]+rxData[1]+rxData[2]+rxData[3]+rxData[4]+
                           rxData[5]+rxData[6];
          jbyte *txData = malloc(sizeof(jbyte)*12);

          txData[0] = (jbyte)0xE6;
          txData[1] = (jbyte)0x90;
          txData[2] = (jbyte)0x0C;
          txData[3] = (jbyte)0x02;
          txData[4] = (jbyte)rxData[0]; //year
          txData[5] = (jbyte)rxData[1]; // month
          txData[6] = (jbyte)rxData[2]; // day
          txData[7] = (jbyte)rxData[3]; //day of week
          txData[8] = (jbyte)rxData[4]; // hour
          txData[9] = (jbyte)rxData[5]; //minute
          txData[10] = (jbyte)rxData[6]; // second
          txData[11] = (jbyte)checksum; //checksum

          (*env)->SetByteArrayRegion(env, output, 0, 12, txData);
          free(txData);
  }

