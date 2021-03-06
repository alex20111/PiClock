/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class net_piclock_nativeImpl_TSL2591 */

#ifndef _Included_net_piclock_nativeImpl_TSL2591
#define _Included_net_piclock_nativeImpl_TSL2591
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     net_piclock_nativeImpl_TSL2591
 * Method:    init
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_net_piclock_nativeImpl_TSL2591_init
  (JNIEnv *, jobject, jint);

/*
 * Class:     net_piclock_nativeImpl_TSL2591
 * Method:    getFullSpectrum
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_piclock_nativeImpl_TSL2591_getFullSpectrum
  (JNIEnv *, jobject);

/*
 * Class:     net_piclock_nativeImpl_TSL2591
 * Method:    getInfrared
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_piclock_nativeImpl_TSL2591_getInfrared
  (JNIEnv *, jobject);

/*
 * Class:     net_piclock_nativeImpl_TSL2591
 * Method:    getVisible
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_piclock_nativeImpl_TSL2591_getVisible
  (JNIEnv *, jobject);

/*
 * Class:     net_piclock_nativeImpl_TSL2591
 * Method:    getLux
 * Signature: ()I
 */
JNIEXPORT jfloat JNICALL Java_net_piclock_nativeImpl_TSL2591_getLux
  (JNIEnv *, jobject);

/*
 * Class:     net_piclock_nativeImpl_TSL2591
 * Method:    setGain
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_net_piclock_nativeImpl_TSL2591_setGain
  (JNIEnv *, jobject, jint);

/*
 * Class:     net_piclock_nativeImpl_TSL2591
 * Method:    setIntegrationTime
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_net_piclock_nativeImpl_TSL2591_setIntegrationTime
  (JNIEnv *, jobject, jint);

#ifdef __cplusplus
}
#endif
#endif
