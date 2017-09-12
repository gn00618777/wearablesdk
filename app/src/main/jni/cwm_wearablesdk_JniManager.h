/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class cwm_wearablesdk_JniManager */

#ifndef _Included_cwm_wearablesdk_JniManager
#define _Included_cwm_wearablesdk_JniManager
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     cwm_wearablesdk_JniManager
 * Method:    getSyncIntelligentCommand
 * Signature: ([ZI[B)V
 */
JNIEXPORT void JNICALL Java_cwm_wearablesdk_JniManager_getSyncIntelligentCommand
  (JNIEnv *, jobject, jbooleanArray, jint, jbyteArray);

/*
 * Class:     cwm_wearablesdk_JniManager
 * Method:    getSyncBodyCommandCommand
 * Signature: ([I[B)V
 */
JNIEXPORT void JNICALL Java_cwm_wearablesdk_JniManager_getSyncBodyCommandCommand
  (JNIEnv *, jobject, jintArray, jbyteArray);

/*
 * Class:     cwm_wearablesdk_JniManager
 * Method:    getSyncCurrentCommand
 * Signature: ([I[B)V
 */
JNIEXPORT void JNICALL Java_cwm_wearablesdk_JniManager_getSyncCurrentCommand
  (JNIEnv *, jobject, jintArray, jbyteArray);

/*
 * Class:     cwm_wearablesdk_JniManager
 * Method:    getRequestBatteryCommand
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_cwm_wearablesdk_JniManager_getRequestBatteryCommand
  (JNIEnv *, jobject, jbyteArray);

/*
 * Class:     cwm_wearablesdk_JniManager
 * Method:    getCwmInformation
 * Signature: (I[B[I)V
 */
JNIEXPORT void JNICALL Java_cwm_wearablesdk_JniManager_getCwmInformation
  (JNIEnv *, jobject, jint, jbyteArray, jintArray);

/*
 * Class:     cwm_wearablesdk_JniManager
 * Method:    getType
 * Signature: ([B)I
 */
JNIEXPORT jint JNICALL Java_cwm_wearablesdk_JniManager_getType
  (JNIEnv *, jobject, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif
