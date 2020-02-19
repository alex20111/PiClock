#include <jni.h>
#include <iostream>
#include "SparkFunSi4703.h"
#include "net_piclock_nativeImpl_SI4703.h"  //https://stackoverflow.com/questions/9796367/jni-keeping-a-global-reference-to-an-object-accessing-it-with-other-jni-methods
											//https://stackoverflow.com/questions/8397426/keep-some-sort-of-c-object-alive-over-multiple-jni-calls/8397907

 Si4703_Breakout radio(18,0);

using std::cerr;
using std::cout;
using std::endl;
  
  
/*
 * Class:     net_piclock_nativeImpl_SI4703
 * Method:    powerOn
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_piclock_nativeImpl_SI4703_powerOn
  (JNIEnv* env, jobject obj){
  
    Status s = radio.powerOn();
    
    radio.setVolume(1);
    
		
	if (s == Status::SUCCESS){
		return 0;
	}else{
		return 1;
	}  

  }

/*
 * Class:     net_piclock_nativeImpl_SI4703
 * Method:    setVolume
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_net_piclock_nativeImpl_SI4703_setVolume
  (JNIEnv* env, jobject obj, jint volume){
	radio.setVolume((int)volume);
  }

/*
 * Class:     net_piclock_nativeImpl_SI4703
 * Method:    powerOff
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_net_piclock_nativeImpl_SI4703_powerOff
  (JNIEnv* env, jobject obj){
	radio.powerOff();
  }

/*
 * Class:     net_piclock_nativeImpl_SI4703
 * Method:    setFerequency
 * Signature: (F)V
 */
JNIEXPORT void JNICALL Java_net_piclock_nativeImpl_SI4703_setFrequency
  (JNIEnv* env, jobject obj, jfloat freq){
  	
	radio.setFrequency((float)freq);
  
  }

/*
 * Class:     net_piclock_nativeImpl_SI4703
 * Method:    getFrequency
 * Signature: ()F
 */
JNIEXPORT jfloat JNICALL Java_net_piclock_nativeImpl_SI4703_getFrequency
  (JNIEnv* env, jobject obj){
  return radio.getFrequency();
  }

/*
 * Class:     net_piclock_nativeImpl_SI4703
 * Method:    minFrequency
 * Signature: ()F
 */
JNIEXPORT jfloat JNICALL Java_net_piclock_nativeImpl_SI4703_minFrequency
  (JNIEnv* env, jobject obj){
  return radio.minFrequency();
  }

/*
 * Class:     net_piclock_nativeImpl_SI4703
 * Method:    maxFrequency
 * Signature: ()F
 */
JNIEXPORT jfloat JNICALL Java_net_piclock_nativeImpl_SI4703_maxFrequency
  (JNIEnv* env, jobject obj){
    return 108.0;
  }

/*
 * Class:     net_piclock_nativeImpl_SI4703
 * Method:    getRDS
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_net_piclock_nativeImpl_SI4703_getRDS
  (JNIEnv* env, jobject obj){
  	char rds_buffer[9];
    radio.getRDS(rds_buffer);
    
     std::string rds(rds_buffer);
    return env->NewStringUTF(rds.c_str());
  }

/*
 * Class:     net_piclock_nativeImpl_SI4703
 * Method:    getSignalStrength
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_piclock_nativeImpl_SI4703_getSignalStrength
  (JNIEnv* env, jobject obj){
  return 1;
  }

/*
 * Class:     net_piclock_nativeImpl_SI4703
 * Method:    seek
 * Signature: (I)F
 */
JNIEXPORT jfloat JNICALL Java_net_piclock_nativeImpl_SI4703_seek
  (JNIEnv* env, jobject obj, jint seek){
     return 1.1;
  }

/*
 * Class:     net_piclock_nativeImpl_SI4703
 * Method:    init
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_net_piclock_nativeImpl_SI4703_init
  (JNIEnv* env, jobject obj, jint reset, jint sdio){
   // radio.setVolume(5);
  }

