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

const jbyte HEADER1 = 0xE6;
const jbyte HEADER2 = 0x90;
const jbyte ACK = 0xAC;
const jbyte NACK = 0x15;

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

           (*env)->ReleaseBooleanArrayElements(env, input, rxData, 0);

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

          (*env)->ReleaseIntArrayElements(env, input, rxData, 0);

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

            (*env)->ReleaseIntArrayElements(env, input, rxData, 0);

            (*env)->SetByteArrayRegion(env, output, 0, 9, txData);
            free(txData);
}

JNIEXPORT void JNICALL Java_cwm_wearablesdk_JniManager_getCwmInformation
(JNIEnv * env, jobject obj, jint messageId, jbyteArray input, jintArray output)
{
         jbyte *rxData = (*env)->GetByteArrayElements(env, input, 0);

         if(messageId == 0xAF){ //motion
            jint step = 0;
            jint distance = 0;
            jint calories = 0;
            jint status = 0;
            jint *txData = malloc(sizeof(jint)*4);
            jbyte *dest = malloc(sizeof(jbyte)*4);
            dest[0] = rxData[4];
            dest[1] = rxData[5];
            dest[2] = rxData[6];
            dest[3] = rxData[7];
            step = (jint)(*(float *)dest);

            dest[0] = rxData[8];
            dest[1] = rxData[9];
            dest[2] = rxData[10];
            dest[3] = rxData[11];
            distance = (jint)(*(float *)dest);

            dest[0] = rxData[12];
            dest[1] = rxData[13];
            dest[2] = rxData[14];
            dest[3] = rxData[15];
            calories = (jint)(*(float *)dest);

            status = (jint)rxData[16];

            txData[0] = step;
            txData[1] = distance;
            txData[2] = calories;
            txData[3] = status;

            (*env)->SetIntArrayRegion(env, output, 0, 4, txData);
            free(txData);

         }
         else if(messageId == 0xED){ //battery
             jint *txData = malloc(sizeof(jint)*1);
             txData[0] = (jint)rxData[4];
             (*env)->SetIntArrayRegion(env, output, 0, 1, txData);
             free(txData);
         }
         else if(messageId == 0x01){ //tap

         }
         else if(messageId == 0x02){ //wrist

         }
         else if(messageId == 0x03){ //sedentary

         }
         else if(messageId == 0x04){ //heart
               jbyte *dest = malloc(sizeof(jbyte)*4);
                dest[0] = rxData[4];
                dest[1] = rxData[5];
                dest[2] = rxData[6];
                dest[3] = rxData[7];

                jint *txData = malloc(sizeof(jint)*2);
                txData[0] = (jint)(*(float *)dest);
                txData[1] = 0;

               (*env)->SetIntArrayRegion(env, output, 0, 2, txData);
                free(txData);
         }
         else if(messageId == 0x05){ //tabata response
              jint firstFloat = 0;
              jint secondFloat = 0;
              jint thirdFloat = 0;
              jint status = 0;
               status = (jint)rxData[4];
              jbyte *dest = malloc(sizeof(jbyte)*4);
                dest[0] = rxData[5];
                dest[1] = rxData[6];
                dest[2] = rxData[7];
                dest[3] = rxData[8];
                firstFloat = (jint)(*((float *)dest)); //item+count
                //memcpy(&firstFloat, dest, sizeof(float));

                dest[0] = rxData[9];
                dest[1] = rxData[10];
                dest[2] = rxData[11];
                dest[3] = rxData[12];
                secondFloat = (jint)(*((float *)dest));
                //memcpy(&secondFloat, dest, sizeof(float));

                dest[0] = rxData[13];
                dest[1] = rxData[14];
                dest[2] = rxData[15];
                dest[3] = rxData[16];
                thirdFloat = (jint)(*((float *)dest));
                //memcpy(&thirdFloat, dest, sizeof(float));
                free(dest);

                jint *txData = malloc(sizeof(jint)*5);

                txData[0] = (/*(jint)*/firstFloat) / 1000; // what item
                txData[1] = (/*(jint)*/firstFloat) % 1000; // count
                txData[2] = /*(jint)*/secondFloat;
                txData[3] = (/*(jint)*/thirdFloat) / 100; // heart rate
                txData[4] = (/*(jint)*/thirdFloat) % 100; //strength
                txData[5] = status;

                (*env)->SetIntArrayRegion(env, output, 0, 6, txData);
                 free(txData);


         }
         (*env)->ReleaseByteArrayElements(env, input, rxData, 0);
}

JNIEXPORT jint JNICALL Java_cwm_wearablesdk_JniManager_getType
        (JNIEnv * env, jobject obj, jbyteArray input)
{
         jbyte *rxData = (*env)->GetByteArrayElements(env, input, 0);
         jint type = 0;

         if(((rxData[0] == HEADER1) && (rxData[1] == HEADER2)) && ((rxData[3] == ACK) || (rxData[3] == NACK)))
            type = 0;
         else if(rxData[0] == HEADER1 && rxData[1] == HEADER2 && rxData[3] != ACK && rxData[3] != NACK)
            type = 1;

         (*env)->ReleaseByteArrayElements(env, input, rxData, 0);

         return type;
}

JNIEXPORT void JNICALL Java_cwm_wearablesdk_JniManager_getTabataParameterCommand
(JNIEnv * env, jobject obj, jintArray input1, jbooleanArray input2, jbyteArray output)
{
        jint *rxData1 = (*env)->GetIntArrayElements(env, input1, 0);
        jboolean *rxData2 = (*env)->GetBooleanArrayElements(env, input2, 0);
        jint prepareTime = 0;
        jint actionType = 0;
        jint actionTime_s = 0;
        jint actionTime_s_L = 0;
        jint actionTime_s_H = 0;
        jint intervalTime_L = 0;
        jint intervalTime_H = 0;
        jint intervalTime = 0;
        jint cycle = 0;
        jint actionItems1 = 0;
        jint actionItems2 = 0;

        jboolean pushUp = rxData2[1];
        jboolean crunch = rxData2[2];
        jboolean squart = rxData2[3];
        jboolean jumpingJack  = rxData2[4];
        jboolean dips = rxData2[5];
        jboolean highKneesRunning = rxData2[6];
        jboolean lunges = rxData2[7];
        jboolean burpees = rxData2[8];
        jboolean stepOnChair = rxData2[9];
        jboolean pushUpRotation = rxData2[10];

        (*env)->ReleaseBooleanArrayElements(env, input2, rxData2, 0);

        jint pushUpMask = 1;
        jint crunchMask = 2;
        jint jumpingJackMask = 4;
        jint dipsMask = 8;
        jint squartMask = 16;
        jint pushUpRotationMask = 32;
        jint lungesMask = 64;
        jint burpeesMask = 128;
        jint stepOnChairMask = 1;
        jint highKneesRunningMask = 2;

        prepareTime = rxData1[0];
        actionType = rxData1[1];
        if(actionType == 1)
           actionTime_s = rxData1[2];
        else
           actionTime_s = rxData1[3];
        intervalTime = rxData1[4];
        cycle = rxData1[5];

        (*env)->ReleaseIntArrayElements(env, input1, rxData1, 0);

        actionTime_s_L = actionTime_s & 0xFF;
        actionTime_s_H = (actionTime_s >> 8) & 0xFF;
        intervalTime_L = intervalTime & 0xFF;
        intervalTime_H = (intervalTime >> 8) & 0xFF;

        if(pushUp)
           actionItems1 = actionItems1 | pushUpMask;
        if(crunch)
           actionItems1 = actionItems1 | crunchMask;
        if(jumpingJack)
           actionItems1 = actionItems1 | jumpingJackMask;
        if(dips)
           actionItems1 = actionItems1 | dipsMask;
        if(squart)
           actionItems1 = actionItems1 | squartMask;
        if(pushUpRotation)
           actionItems1 = actionItems1 | pushUpRotationMask;
        if(lunges)
           actionItems1 = actionItems1 | lungesMask;
        if(burpees)
           actionItems1 = actionItems1 | burpeesMask;
        if(stepOnChair)
           actionItems2 = actionItems2 | stepOnChairMask;
        if(highKneesRunning)
           actionItems2 = actionItems2 | highKneesRunningMask;

        jbyte *txData = malloc(sizeof(jbyte)*14);

        txData[0] = (jbyte)0xE6;
        txData[1] = (jbyte)0x90;
        txData[2] = (jbyte)0x0E;
        txData[3] = (jbyte)0x16;
        txData[4] = (jbyte)prepareTime;
        txData[5] = (jbyte)actionType;
        txData[6] = (jbyte)actionTime_s_L;
        txData[7] = (jbyte)actionTime_s_H;
        txData[8] = (jbyte)intervalTime_L;
        txData[9] = (jbyte)intervalTime_H;
        txData[10] = (jbyte)actionItems1;
        txData[11] = (jbyte)actionItems2;
        txData[12] = (jbyte)cycle;

        jint checksum = txData[0]+txData[1]+txData[2]+txData[3]+txData[4]+txData[5]+txData[6]+txData[7]+
                        txData[8]+txData[9]+txData[10]+txData[11]+txData[12];

        txData[13] = (jbyte)checksum;

        (*env)->SetByteArrayRegion(env, output, 0, 14, txData);
        free(txData);
}

