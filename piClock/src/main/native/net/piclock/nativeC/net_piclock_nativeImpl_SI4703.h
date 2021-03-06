/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class net_piclock_nativeImpl_SI4703 */

#ifndef _Included_net_piclock_nativeImpl_SI4703
#define _Included_net_piclock_nativeImpl_SI4703
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     net_piclock_nativeImpl_SI4703
 * Method:    powerOn
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_piclock_nativeImpl_SI4703_powerOn
  (JNIEnv *, jobject);

/*
 * Class:     net_piclock_nativeImpl_SI4703
 * Method:    setVolume
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_net_piclock_nativeImpl_SI4703_setVolume
  (JNIEnv *, jobject, jint);

/*
 * Class:     net_piclock_nativeImpl_SI4703
 * Method:    powerOff
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_net_piclock_nativeImpl_SI4703_powerOff
  (JNIEnv *, jobject);

/*
 * Class:     net_piclock_nativeImpl_SI4703
 * Method:    setFerequency
 * Signature: (F)V
 */
JNIEXPORT void JNICALL Java_net_piclock_nativeImpl_SI4703_setFrequency
  (JNIEnv *, jobject, jfloat);

/*
 * Class:     net_piclock_nativeImpl_SI4703
 * Method:    getFrequency
 * Signature: ()F
 */
JNIEXPORT jfloat JNICALL Java_net_piclock_nativeImpl_SI4703_getFrequency
  (JNIEnv *, jobject);

/*
 * Class:     net_piclock_nativeImpl_SI4703
 * Method:    minFrequency
 * Signature: ()F
 */
JNIEXPORT jfloat JNICALL Java_net_piclock_nativeImpl_SI4703_minFrequency
  (JNIEnv *, jobject);

/*
 * Class:     net_piclock_nativeImpl_SI4703
 * Method:    maxFrequency
 * Signature: ()F
 */
JNIEXPORT jfloat JNICALL Java_net_piclock_nativeImpl_SI4703_maxFrequency
  (JNIEnv *, jobject);

/*
 * Class:     net_piclock_nativeImpl_SI4703
 * Method:    getRDS
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_net_piclock_nativeImpl_SI4703_getRDS
  (JNIEnv *, jobject);

/*
 * Class:     net_piclock_nativeImpl_SI4703
 * Method:    getSignalStrength
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_piclock_nativeImpl_SI4703_getSignalStrength
  (JNIEnv *, jobject);

/*
 * Class:     net_piclock_nativeImpl_SI4703
 * Method:    seek
 * Signature: (I)F
 */
JNIEXPORT jfloat JNICALL Java_net_piclock_nativeImpl_SI4703_seek
  (JNIEnv *, jobject, jint);

/*
 * Class:     net_piclock_nativeImpl_SI4703
 * Method:    init
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_net_piclock_nativeImpl_SI4703_init
  (JNIEnv *, jobject, jint, jint);

#ifdef __cplusplus
}
#endif
#endif