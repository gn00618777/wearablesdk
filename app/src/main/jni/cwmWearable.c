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
const jint HEADER_LENGTH = 2;
const jint LENGTH_BYTE_LENGTH = 2;
const jint MESSAGE_ID_LENGTH = 1;
const jint CHECKSUM_LENGTH = 1;
const jint SLEEP_LOG_SIZE = 12; //byte

const jint BLE_SIZE = 20;

JNIEXPORT void JNICALL Java_cwm_wearablesdk_JniManager_getSyncIntelligentCommand
  (JNIEnv * env, jobject obj, jbooleanArray input, jint goal, jbyteArray output)
{
           jboolean *rxData = (*env)->GetBooleanArrayElements(env, input, 0);
           jint features = 0;
           jint features1 = 0;
           jint onWearMask = 32;
           jint shakeMask  = 64;
           jint significantMask = 1;
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

            if (rxData[5] == 1)
               features = features | shakeMask;
            else
               features = features & ~(shakeMask);

            if (rxData[6] == 1)
               features1 = features1 | significantMask;
            else
                features1 = features1 & ~(significantMask);

           (*env)->ReleaseBooleanArrayElements(env, input, rxData, 0);

           targetStepL = goal & 0xFF;
           targetStepH = (goal >> 8) & 0xFF;

           jint checksum = 0xE6+0x90+0x09+0x12+features+features1+targetStepL+targetStepH;

           jbyte *txData = malloc(sizeof(jbyte)*9);

           txData[0] = (jbyte)0xE6;
           txData[1] = (jbyte)0x90;
           txData[2] = (jbyte)0x09;
           txData[3] = (jbyte)0x12;
           txData[4] = (jbyte)features;
           txData[5] = (jbyte)features1;
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

JNIEXPORT void JNICALL Java_cwm_wearablesdk_JniManager_getSleepLogCommand
(JNIEnv * env, jobject obj, jbyteArray output)
{
             jint checksum = 0xE6+0x90+0x05+0x30;
             jbyte *txData = malloc(sizeof(jbyte)*5);

             txData[0] = (jbyte)0xE6;
             txData[1] = (jbyte)0x90;
             txData[2] = (jbyte)0x05;
             txData[3] = (jbyte)0x30;
             txData[4] = (jbyte)checksum;

             (*env)->SetByteArrayRegion(env, output, 0, 5, txData);
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
            dest[0] = rxData[5];
            dest[1] = rxData[6];
            dest[2] = rxData[7];
            dest[3] = rxData[8];
            step = (jint)(*(float *)dest);

            dest[0] = rxData[9];
            dest[1] = rxData[10];
            dest[2] = rxData[11];
            dest[3] = rxData[12];
            distance = (jint)(*(float *)dest);

            dest[0] = rxData[13];
            dest[1] = rxData[14];
            dest[2] = rxData[15];
            dest[3] = rxData[16];
            calories = (jint)(*(float *)dest);

            status = (jint)rxData[17];

            txData[0] = step;
            txData[1] = distance;
            txData[2] = calories;
            txData[3] = status;

            (*env)->SetIntArrayRegion(env, output, 0, 4, txData);
            free(txData);

         }
         else if(messageId == 0xED){ //battery
             jint *txData = malloc(sizeof(jint)*1);
             txData[0] = (jint)rxData[5];
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
                dest[0] = rxData[5];
                dest[1] = rxData[6];
                dest[2] = rxData[7];
                dest[3] = rxData[8];

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
               status = (jint)rxData[5];
              jbyte *dest = malloc(sizeof(jbyte)*4);
                dest[0] = rxData[6];
                dest[1] = rxData[7];
                dest[2] = rxData[8];
                dest[3] = rxData[9];
                firstFloat = (jint)(*((float *)dest)); //item+count
                //memcpy(&firstFloat, dest, sizeof(float));

                dest[0] = rxData[10];
                dest[1] = rxData[11];
                dest[2] = rxData[12];
                dest[3] = rxData[13];
                secondFloat = (jint)(*((float *)dest));
                //memcpy(&secondFloat, dest, sizeof(float));

                dest[0] = rxData[14];
                dest[1] = rxData[15];
                dest[2] = rxData[16];
                dest[3] = rxData[17];
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
         else if(messageId == 0x90){
                jint *txData = malloc(sizeof(jint)*2);
                txData[0] = (jint)rxData[5];
                txData[1] = (jint)rxData[6];
                (*env)->SetIntArrayRegion(env, output, 0, 2, txData);
                free(txData);
         }
         (*env)->ReleaseByteArrayElements(env, input, rxData, 0);
}

JNIEXPORT jint JNICALL Java_cwm_wearablesdk_JniManager_getType
        (JNIEnv * env, jobject obj, jbyteArray input)
{
         jbyte *rxData = (*env)->GetByteArrayElements(env, input, 0);
         jint type = 0;
         jint data_length = 0;
         data_length = ((rxData[3] & 0xFF) << 8) | (rxData[2] & 0xFF);

         if(((rxData[0] == HEADER1) && (rxData[1] == HEADER2)) && ((rxData[4] == ACK) || (rxData[4] == NACK)))
            type = 0;
         else if(((rxData[0] == HEADER1) && (rxData[1] == HEADER2)) && ((rxData[4] != ACK) && (rxData[4] != NACK)) && data_length <= BLE_SIZE )
            type = 1;
         else if(((rxData[0] == HEADER1) && (rxData[1] == HEADER2)) && ((rxData[4] != ACK) && (rxData[4] != NACK)) && data_length > BLE_SIZE)
            type = 2;
         else if(rxData[0] != HEADER1 || rxData[1] != HEADER2)
            type = 3;

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
JNIEXPORT void JNICALL Java_cwm_wearablesdk_JniManager_getRequestSwVersionCommand
(JNIEnv * env, jobject jobj, jbyteArray output)
{
        jint checksum = 0xE6+0x90+0x05+0x10;
        jbyte *txData = malloc(sizeof(jbyte)*5);

        txData[0] = (jbyte)0xE6;
        txData[1] = (jbyte)0x90;
        txData[2] = (jbyte)0x05;
        txData[3] = (jbyte)0x10;
        txData[4] = (jbyte)checksum;

        (*env)->SetByteArrayRegion(env, output, 0, 5, txData);
        free(txData);
}

JNIEXPORT void JNICALL Java_cwm_wearablesdk_JniManager_getSwitchOTACommand
(JNIEnv * env, jobject jobj, jbyteArray output)
{
        jint checksum = 0xE6+0x90+0x05+0x1F;
        jbyte *txData = malloc(sizeof(jbyte)*5);

        txData[0] = (jbyte)0xE6;
        txData[1] = (jbyte)0x90;
        txData[2] = (jbyte)0x05;
        txData[3] = (jbyte)0x1F;
        txData[4] = (jbyte)checksum;

        (*env)->SetByteArrayRegion(env, output, 0, 5, txData);
        free(txData);
}
JNIEXPORT void JNICALL Java_cwm_wearablesdk_JniManager_getCwmSleepInfomation
(JNIEnv *env, jobject jobj, jint id, jbyteArray input, jfloatArray output)
{
        jbyte *rxData = (*env)->GetByteArrayElements(env, input, 0);
        jint rawByteLength = sizeof(rxData)/sizeof(jbyte);
        jint dataLength = rawByteLength - HEADER_LENGTH - LENGTH_BYTE_LENGTH - MESSAGE_ID_LENGTH - CHECKSUM_LENGTH;
        jint startPos = HEADER_LENGTH + LENGTH_BYTE_LENGTH + MESSAGE_ID_LENGTH;
        jint endPos = (rawByteLength-1) - CHECKSUM_LENGTH;
        jbyte *readyToTransform = malloc(sizeof(jbyte)*dataLength);
        jfloat *readyToReceived = malloc(sizeof(jfloat)*((dataLength) / 4));
        jint j = 0;
        //jint *temp = malloc(sizeof(jint)*4);

         for(jint i = startPos ; i <= endPos ; i++){
             readyToTransform[j] = rxData[i];
             j++;
         }

        /* for(jint i  = 0 ; i < dataLength ; i = i + 4){
            temp[0] = readyToTransform[i];
            temp[1] = readyToTransform[i+1];
            temp[2] = readyToTransform[i+2];
            temp[3] = readyToTransform[i+3];
            readyToReceived[j] = *((int *)temp);
         }*/

         memcpy(readyToReceived, readyToTransform, dataLength);
         //readyToReceived = (int *)readyToTransform;
         (*env)->SetFloatArrayRegion(env, output, 0, (dataLength / 4), readyToReceived);
         //free(readyToTransform);
         //free(readyToReceived);

}

JNIEXPORT void JNICALL Java_cwm_wearablesdk_JniManager_getSedentaryRemindTimeCommand
(JNIEnv * env, jobject jobj, jint time, jbyteArray output)
{
         jbyte *txData = malloc(sizeof(jbyte)*7);
         jint remindTime_L = 0;
         jint remindTime_H = 0;

         remindTime_L = time & 0xFF;
         remindTime_H = (time >> 8) & 0xFF;

         txData[0] = (jbyte)0xE6;
         txData[1] = (jbyte)0x90;
         txData[2] = (jbyte)0x07;
         txData[3] = (jbyte)0x15;
         txData[4] = (jbyte)remindTime_L;
         txData[5] = (jbyte)remindTime_H;

         jint checksum = txData[0]+txData[1]+txData[2]+txData[3]+txData[4]+txData[5];

         txData[6] = (jbyte) checksum;

         (*env)->SetByteArrayRegion(env, output, 0, 7, txData);
         free(txData);
}
JNIEXPORT void JNICALL Java_cwm_wearablesdk_JniManager_getTabataCommand
(JNIEnv * env, jobject jobj, jint operate, jint prepare, jint interval, jint action_item, jbyteArray output)
{

           jbyte *txData = malloc(sizeof(jbyte)*9);

           if(operate == 0){ // tabata init

              txData[0] = (jbyte)0xE6;
              txData[1] = (jbyte)0x90;
              txData[2] = (jbyte)0x09;
              txData[3] = (jbyte)0x17;
              txData[4] = (jbyte)0x0;// tabata flag
              txData[5] = (jbyte)0x0;
              txData[6] = (jbyte)0x0;
              txData[7] = (jbyte)0x0;

              jint checksum = txData[0]+txData[1]+txData[2]+txData[3]+txData[4]+txData[5]+
                      txData[6]+txData[7];

               txData[8] = (jbyte)checksum;

              (*env)->SetByteArrayRegion(env, output, 0, 9, txData);
              free(txData);
          }
          else if(operate == 1){ //tabata pause

                 txData[0] = (jbyte)0xE6;
                 txData[1] = (jbyte)0x90;
                 txData[2] = (jbyte)0x09;
                 txData[3] = (jbyte)0x17;
                 txData[4] = (jbyte)0x01; // tabata flag
                 txData[5] = (jbyte)0x00;
                 txData[6] = (jbyte)0x00;
                 txData[7] = (jbyte)0x00;

                 jint checksum = txData[0]+txData[1]+txData[2]+txData[3]+txData[4]+txData[5]+
                              txData[6]+txData[7];

                txData[8] = (jbyte)checksum;

                (*env)->SetByteArrayRegion(env, output, 0, 9, txData);
                free(txData);
          }
          else if(operate == 8){ //select tabat item

              txData[0] = (jbyte)0xE6;
              txData[1] = (jbyte)0x90;
              txData[2] = (jbyte)0x09;
              txData[3] = (jbyte)0x17;
              txData[4] = (jbyte)0x8; //tabata flag
              txData[5] = (jbyte)0x0;
              txData[6] = (jbyte)0x0;
              txData[7] = (jbyte)action_item;

              jint checksum = txData[0]+txData[1]+txData[2]+txData[3]+txData[4]+txData[5]+
                              txData[6]+txData[7];

              txData[8] = (jbyte)checksum;

              (*env)->SetByteArrayRegion(env, output, 0, 9, txData);
              free(txData);
          }
          else if(operate == 3){ //tabata prepare count

                 jint prepareTime_L = 0;
                 jint prepareTime_H = 0;

                 prepareTime_L = prepare & 0xFF;
                 prepareTime_H = (prepare >> 8) & 0xFF;

                 txData[0] = (jbyte)0xE6;
                 txData[1] = (jbyte)0x90;
                 txData[2] = (jbyte)0x09;
                 txData[3] = (jbyte)0x17;
                 txData[4] = (jbyte)0x3; // tabata flag
                 txData[5] = (jbyte)prepareTime_L;
                 txData[6] = (jbyte)prepareTime_H;
                 txData[7] = (jbyte)0x0;

                 jint checksum = txData[0]+txData[1]+txData[2]+txData[3]+txData[4]+txData[5]+
                                  txData[6]+txData[7];

                 txData[8] = (jbyte)checksum;

                 (*env)->SetByteArrayRegion(env, output, 0, 9, txData);
                 free(txData);
          }
          else if(operate == 2){ //tabata prepare start

                 txData[0] = (jbyte)0xE6;
                 txData[1] = (jbyte)0x90;
                 txData[2] = (jbyte)0x09;
                 txData[3] = (jbyte)0x17;
                 txData[4] = (jbyte)0x2; // tabata flag
                 txData[5] = (jbyte)0x00;
                 txData[6] = (jbyte)0x00;
                 txData[7] = (jbyte)0x00;

                 jint checksum = txData[0]+txData[1]+txData[2]+txData[3]+txData[4]+txData[5]+
                                 txData[6]+txData[7];

                 txData[8] = (jbyte)checksum;

                 (*env)->SetByteArrayRegion(env, output, 0, 9, txData);
                 free(txData);
          }
          else if(operate == 4){ //tabata prepare end

                txData[0] = (jbyte)0xE6;
                txData[1] = (jbyte)0x90;
                txData[2] = (jbyte)0x09;
                txData[3] = (jbyte)0x17;
                txData[4] = (jbyte)0x4; // tabata flag
                txData[5] = (jbyte)0x00;
                txData[6] = (jbyte)0x00;
                txData[7] = (jbyte)0x00;

                jint checksum = txData[0]+txData[1]+txData[2]+txData[3]+txData[4]+txData[5]+
                               txData[6]+txData[7];

                txData[8] = (jbyte)checksum;

                (*env)->SetByteArrayRegion(env, output, 0, 9, txData);
                free(txData);
          }
          else if(operate == 5){ //tabata reset start

                txData[0] = (jbyte)0xE6;
                txData[1] = (jbyte)0x90;
                txData[2] = (jbyte)0x09;
                txData[3] = (jbyte)0x17;
                txData[4] = (jbyte)0x5; // tabata flag
                txData[5] = (jbyte)0x00;
                txData[6] = (jbyte)0x00;
                txData[7] = (jbyte)0x00;

                jint checksum = txData[0]+txData[1]+txData[2]+txData[3]+txData[4]+txData[5]+
                               txData[6]+txData[7];

                txData[8] = (jbyte)checksum;

                (*env)->SetByteArrayRegion(env, output, 0, 9, txData);
                free(txData);
          }
          else if(operate == 6){ //tabata reset count

                 jint intervalTime_L = 0;
                 jint intervalTime_H = 0;

                 intervalTime_L = interval & 0xFF;
                 intervalTime_H = (interval >> 8) & 0xFF;

                 txData[0] = (jbyte)0xE6;
                 txData[1] = (jbyte)0x90;
                 txData[2] = (jbyte)0x09;
                 txData[3] = (jbyte)0x17;
                 txData[4] = (jbyte)0x6; // tabata flag
                 txData[5] = (jbyte)intervalTime_L;
                 txData[6] = (jbyte)intervalTime_H;
                 txData[7] = (jbyte)0x0;

                 jint checksum = txData[0]+txData[1]+txData[2]+txData[3]+txData[4]+txData[5]+
                                txData[6]+txData[7];

                 txData[8] = (jbyte)checksum;

                 (*env)->SetByteArrayRegion(env, output, 0, 9, txData);
                 free(txData);
          }
          else if(operate == 7){ //tabata reset end

                 txData[0] = (jbyte)0xE6;
                 txData[1] = (jbyte)0x90;
                 txData[2] = (jbyte)0x09;
                 txData[3] = (jbyte)0x17;
                 txData[4] = (jbyte)0x7; // tabata flag
                 txData[5] = (jbyte)0x00;
                 txData[6] = (jbyte)0x00;
                 txData[7] = (jbyte)0x00;

                 jint checksum = txData[0]+txData[1]+txData[2]+txData[3]+txData[4]+txData[5]+
                                txData[6]+txData[7];

                 txData[8] = (jbyte)checksum;

                (*env)->SetByteArrayRegion(env, output, 0, 9, txData);
                free(txData);
          }
          else if(operate == 9) { //tabata action start

                 txData[0] = (jbyte)0xE6;
                 txData[1] = (jbyte)0x90;
                 txData[2] = (jbyte)0x09;
                 txData[3] = (jbyte)0x17;
                 txData[4] = (jbyte)0x9; // tabata flag
                 txData[5] = (jbyte)0x00;
                 txData[6] = (jbyte)0x00;
                 txData[7] = (jbyte)0x00;

                 jint checksum = txData[0]+txData[1]+txData[2]+txData[3]+txData[4]+txData[5]+
                               txData[6]+txData[7];

                 txData[8] = (jbyte)checksum;

                 (*env)->SetByteArrayRegion(env, output, 0, 9, txData);
                 free(txData);
          }
          else if(operate == 10) { //tabata action end

                 txData[0] = (jbyte)0xE6;
                 txData[1] = (jbyte)0x90;
                 txData[2] = (jbyte)0x09;
                 txData[3] = (jbyte)0x17;
                 txData[4] = (jbyte)0xA; // tabata flag
                 txData[5] = (jbyte)0x00;
                 txData[6] = (jbyte)0x00;
                 txData[7] = (jbyte)0x00;

                 jint checksum = txData[0]+txData[1]+txData[2]+txData[3]+txData[4]+txData[5]+
                                txData[6]+txData[7];

                 txData[8] = (jbyte)checksum;

                 (*env)->SetByteArrayRegion(env, output, 0, 9, txData);
                 free(txData);
          }
          else if(operate == 11){ //tabata request

                txData[0] = (jbyte)0xE6;
                txData[1] = (jbyte)0x90;
                txData[2] = (jbyte)0x09;
                txData[3] = (jbyte)0x17;
                txData[4] = (jbyte)0xB; // tabata flag
                txData[5] = (jbyte)0x00;
                txData[6] = (jbyte)0x00;
                txData[7] = (jbyte)0x00;

                jint checksum = txData[0]+txData[1]+txData[2]+txData[3]+txData[4]+txData[5]+
                    txData[6]+txData[7];

                txData[8] = (jbyte)checksum;

                (*env)->SetByteArrayRegion(env, output, 0, 9, txData);
                free(txData);
         }
          else if(operate == 12){ //tabata done

                 txData[0] = (jbyte)0xE6;
                 txData[1] = (jbyte)0x90;
                 txData[2] = (jbyte)0x09;
                 txData[3] = (jbyte)0x17;
                 txData[4] = (jbyte)0xC; // tabata flag
                 txData[5] = (jbyte)0x00;
                 txData[6] = (jbyte)0x00;
                 txData[7] = (jbyte)0x00;

                 jint checksum = txData[0]+txData[1]+txData[2]+txData[3]+txData[4]+txData[5]+
                               txData[6]+txData[7];

                 txData[8] = (jbyte)checksum;

                 (*env)->SetByteArrayRegion(env, output, 0, 9, txData);
                 free(txData);
          }
          else if(operate == 13){ //tabata resume

                 txData[0] = (jbyte)0xE6;
                 txData[1] = (jbyte)0x90;
                 txData[2] = (jbyte)0x09;
                 txData[3] = (jbyte)0x17;
                 txData[4] = (jbyte)0xD; // tabata flag
                 txData[5] = (jbyte)0x00;
                 txData[6] = (jbyte)0x00;
                 txData[7] = (jbyte)0x00;

                 jint checksum = txData[0]+txData[1]+txData[2]+txData[3]+txData[4]+txData[5]+
                       txData[6]+txData[7];

                  txData[8] = (jbyte)checksum;

                (*env)->SetByteArrayRegion(env, output, 0, 9, txData);
               free(txData);
         }

}
JNIEXPORT void JNICALL Java_cwm_wearablesdk_JniManager_getReadFlashCommand
(JNIEnv *env, jobject jobj, jint type, jbyteArray output)
{
      jbyte *txData = malloc(sizeof(jbyte)*6);
      jint checksum = 0;

      txData[0] = (jbyte)0xE6;
      txData[1] = (jbyte)0x90;
      txData[2] = (jbyte)0x06;
      txData[3] = (jbyte)0x20; //command id

      if(type == 0){ //sync start
         txData[4] = (jbyte)0x0;
      }
      else if(type == 1){ //sync success
         txData[4] = (jbyte)0x1;
      }
      else if(type == 2){ //sync fail
         txData[4] = (jbyte)0x2;
      }
      else if(type == 3){ //sync abort
          txData[4] = (jbyte)0x3;
      }
      else if(type == 4){ //sync done
          txData[4] = (jbyte)0x4;
      }
       else if(type == 5){ //sync erase
          txData[4] = (jbyte)0x5;
        }

       checksum = txData[0]+txData[1]+txData[2]+txData[3]+txData[4];

       txData[5] = (jbyte) checksum;

       (*env)->SetByteArrayRegion(env, output, 0, 6, txData);
       free(txData);

}
JNIEXPORT void JNICALL Java_cwm_wearablesdk_JniManager_getRequestMaxLogPacketsCommand
(JNIEnv *env, jobject jobj, jbyteArray output)
{

       jint checksum = 0xE6+0x90+0x05+0x22;
       jbyte *txData = malloc(sizeof(jbyte)*5);

       txData[0] = (jbyte)0xE6;
       txData[1] = (jbyte)0x90;
       txData[2] = (jbyte)0x05;
       txData[3] = (jbyte)0x22;
       txData[4] = (jbyte)checksum;

      (*env)->SetByteArrayRegion(env, output, 0, 5, txData);
      free(txData);
}
JNIEXPORT void JNICALL Java_cwm_wearablesdk_JniManager_getGestureListCommand
(JNIEnv *env, jobject jobj, jbyteArray output)
{
       jint checksum = 0xE6+0x90+0x05+0x18;
       jbyte *txData = malloc(sizeof(jbyte)*5);

       txData[0] = (jbyte)0xE6;
       txData[1] = (jbyte)0x90;
       txData[2] = (jbyte)0x05;
       txData[3] = (jbyte)0x18;
       txData[4] = (jbyte)checksum;

       (*env)->SetByteArrayRegion(env, output, 0, 5, txData);
       free(txData);
}
JNIEXPORT void JNICALL Java_cwm_wearablesdk_JniManager_getGestureListInfomation
(JNIEnv *env, jobject jobj, jint id, jbyteArray input, jintArray gestureOutput)
{


    jbyte *rxData = (*env)->GetByteArrayElements(env, input, 0);
    jbyte features, features1;
    jint onWearMask = 32;
    jint shakeMask  = 64;
    jint significantMask = 1;
    jint sedentaryRemindMask = 8;
    jint handUpMask = 4;
    jint tapMask = 2;
    jint wristMask = 1;

     features = rxData[5];
     features1 = rxData[6];

     jint *txData = malloc(sizeof(jint)*7);

     for(int i = 0 ; i < 7 ; i++)
         txData[0] = 0;

     if(features & wristMask)
         txData[0]=1;
     else
         txData[0]=0;
     if(features & tapMask)
         txData[1]=1;
     else
         txData[1]=0;
     if(features & handUpMask)
        txData[2]=1;
     else
        txData[2]=0;
     if(features & sedentaryRemindMask)
        txData[3]=1;
     else
        txData[3]=0;
     if(features & onWearMask)
       txData[4]=1;
     else
       txData[4]=0;
     if(features & shakeMask)
       txData[5]=1;
     else
       txData[5]=0;
     if(features1 & significantMask)
       txData[6]=1;
     else
        txData[6]=0;

     (*env)->SetIntArrayRegion(env, gestureOutput, 0, 7, txData);
     (*env)->ReleaseByteArrayElements(env, input, rxData, 0);
     free(txData);

}
JNIEXPORT void JNICALL Java_cwm_wearablesdk_JniManager_getRecordSensorToFlashCommand
(JNIEnv *env, jobject jobj, jint sensorType, jint odrType, jint sensorStatus, jbyteArray output)
{

      int checksum = 0;
      jbyte *txData = malloc(sizeof(jbyte)*8);

      txData[0] = 0xE6;
      txData[1] = 0x90;
      txData[2] = 0x8;
      txData[3] = 0x82;

      if(sensorType == 0x01)
         txData[4] = 0x01;
      else if(sensorType == 0x02)
         txData[4] = 0x02;

      if(odrType == 0x01)
         txData[5] = 0x01;
      else if(odrType == 0x02)
         txData[5] = 0x02;
      else if(odrType == 0x03)
         txData[5] = 0x03;

      if(sensorStatus == 0x0)
         txData[6] = 0x0;
      else if(sensorStatus == 0x01)
         txData[6] = 0x01;

      checksum = txData[0]+txData[1]+txData[2]+txData[3]+txData[4]+txData[5]+txData[6];
      txData[7] = (jbyte)checksum;

       (*env)->SetByteArrayRegion(env, output, 0, 8, txData);
       free(txData);

}
