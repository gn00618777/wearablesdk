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

JNIEXPORT void JNICALL Java_cwm_wearablesdk_JniManager_getSyncIntelligentCommand
  (JNIEnv * env, jobject obj, jbooleanArray input, jint goal, jbyteArray output)
{
           jboolean *rxData = (*env)->GetBooleanArrayElements(env, input, 0);
           jint features = 0;
           jint onWearMask = 32;
           jint sedentaryRemindMask = 8;
           jint handUpMask = 4;
           jint tapMask = 2;
           jint wristMask = 1;
           jint targetStepL = 0;
           jint targetStepH = 0;

           if (rxData[0] == 1)
             features = features | sedentaryRemindMask;
           else
             features = features & ~(sedentaryRemindMask);
           if (rxData[1] == 1)
             features = features | handUpMask;
           else
              features = features & ~(handUpMask);
            if (rxData[2] == 1)
                features = features | onWearMask;
            else
                features = features & ~(onWearMask);
            if (rxData[3] == 1)
                features = features | tapMask;
            else
                features = features & ~(tapMask);
            if (rxData[4] == 1)
               features = features | wristMask;
            else
               features = features & ~(wristMask);

           targetStepL = goal & 0xFF;
           targetStepH = (goal >> 8) & 0xFF;

           jint checksum = 0xE6+0x90+0x09+0x12+features+0x00+targetStepL+targetStepH;

           jbyte *txData = malloc(sizeof(jbyte)*9);

           txData[0] = (jbyte)0xE6;
           txData[1] = (jbyte)0x90;
           txData[2] = (jbyte)0x09;
           txData[3] = (jbyte)0x12;
           txData[4] = (jbyte)features;
           txData[5] = (jbyte)0x00;
           txData[6] = (jbyte)targetStepL;
           txData[7] = (jbyte)targetStepH;
           txData[8] = (jbyte)checksum;

           (*env)->SetByteArrayRegion(env, output, 0, 9, txData);
            free(txData);

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

JNIEXPORT void JNICALL Java_cwm_wearablesdk_JniManager_getRequestBatteryCommand
(JNIEnv * env, jobject obj, jbyteArray output)
{
           jint checksum = 0xE6+0x90+0x05+0x50;
           jbyte *txData = malloc(sizeof(jbyte)*5);

           txData[0] = (jbyte)0xE6;
           txData[1] = (jbyte)0x90;
           txData[2] = (jbyte)0x05;
           txData[3] = (jbyte)0x50;
           txData[4] = (jbyte)checksum;

           (*env)->SetByteArrayRegion(env, output, 0, 5, txData);
           free(txData);
}

JNIEXPORT void JNICALL Java_cwm_wearablesdk_JniManager_getSyncBodyCommandCommand
(JNIEnv * env, jobject obj, jintArray input, jbyteArray output)
{
           jint *rxData = (*env)->GetIntArrayElements(env, input, 0);

           jint checksum = 0xE6+0x90+0x09+0x14+rxData[2]+rxData[0]+rxData[1]+rxData[3];

           jbyte *txData = malloc(sizeof(jbyte)*9);

            txData[0] = (jbyte)0xE6;
            txData[1] = (jbyte)0x90;
            txData[2] = (jbyte)0x09;
            txData[3] = (jbyte)0x14;
            txData[4] = (jbyte)rxData[2];
            txData[5] = (jbyte)rxData[0];
            txData[6] = (jbyte)rxData[1];
            txData[7] = (jbyte)rxData[3];
            txData[8] = (jbyte)checksum;

            (*env)->SetByteArrayRegion(env, output, 0, 9, txData);
            free(txData);
}

