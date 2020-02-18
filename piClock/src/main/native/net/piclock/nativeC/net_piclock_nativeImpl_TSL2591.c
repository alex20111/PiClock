#include <jni.h>
#include "2591a.h"
#include "net_piclock_nativeImpl_TSL2591.h"
#include <iostream>
#include <wiringPi.h>
#include <wiringPiI2C.h>

#include <unistd.h> 

int fd;
Adafruit_TSL2591	tsl;

void read(){
}

/*
 * Class:     net_piclock_nativeImpl_TSL2591
 * Method:    init
 * Signature: (I)V
 */
 					   
JNIEXPORT void JNICALL Java_net_piclock_nativeImpl_TSL2591_init
  (JNIEnv* env, jobject obj, jint port){
  
    wiringPiSetup();
  
  std::cout << "power ON gain:" <<  TSL2591_GAIN_MED << std::endl;
  	tsl = Adafruit_TSL2591(2591, TSL2591_INTEGRATIONTIME_300MS, TSL2591_GAIN_MED);
  	     
  	fd = wiringPiI2CSetup(port);
  
  }

/*
 * Class:     net_piclock_nativeImpl_TSL2591
 * Method:    getFullSpectrum
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_piclock_nativeImpl_TSL2591_getFullSpectrum
  (JNIEnv* env, jobject obj){
  
	
  	uint16_t full;
    uint32_t visible_and_ir;
	
    visible_and_ir = tsl.Adafruit_TSL2591::getFullLuminosity (fd);

    // Reads two byte value from channel 0 (visible + infrared)
    full =  (visible_and_ir & 0xFFFF);
     
    
   return full;
  }

/*
 * Class:     net_piclock_nativeImpl_TSL2591
 * Method:    getInfrared
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_piclock_nativeImpl_TSL2591_getInfrared
  (JNIEnv* env, jobject obj){
  	uint16_t ir;
    uint32_t visible_and_ir;
	
    visible_and_ir = tsl.Adafruit_TSL2591::getFullLuminosity (fd);

    ir = (visible_and_ir >> 16); 
	
  
  return ir;
  }

/*
 * Class:     net_piclock_nativeImpl_TSL2591
 * Method:    getVisible
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_piclock_nativeImpl_TSL2591_getVisible
  (JNIEnv* env, jobject obj){
  
    	uint16_t full,ir;
    uint32_t visible_and_ir;
	
    visible_and_ir = tsl.Adafruit_TSL2591::getFullLuminosity (fd);

    // Reads two byte value from channel 0 (visible + infrared)
    full =  (visible_and_ir & 0xFFFF);
    // Reads two byte value from channel 1 (infrared)
    ir = (visible_and_ir >> 16);

	int visible = 0;

   
    if ( full >= ir ) visible = full-ir;
  return visible;
  }

/*
 * Class:     net_piclock_nativeImpl_TSL2591
 * Method:    getLux
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_piclock_nativeImpl_TSL2591_getLux
  (JNIEnv* env, jobject obj){
  
  	uint16_t full,ir;
    uint32_t visible_and_ir, lux;
	
    visible_and_ir = tsl.Adafruit_TSL2591::getFullLuminosity (fd);

    // Reads two byte value from channel 0 (visible + infrared)
    full =  (visible_and_ir & 0xFFFF);
    // Reads two byte value from channel 1 (infrared)
    ir = (visible_and_ir >> 16);

    lux = tsl.Adafruit_TSL2591::calculateLux(full, ir);

  return lux;
  }

/*
 * Class:     net_piclock_nativeImpl_TSL2591
 * Method:    setGain
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_net_piclock_nativeImpl_TSL2591_setGain
  (JNIEnv* env, jobject obj, jint gain){
  
  tsl2591Gain_t cGain = static_cast<tsl2591Gain_t>(gain);  
  
  
  	tsl.Adafruit_TSL2591::setGain (fd, cGain);
  }

/*
 * Class:     net_piclock_nativeImpl_TSL2591
 * Method:    setIntegrationTime
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_net_piclock_nativeImpl_TSL2591_setIntegrationTime
  (JNIEnv* env, jobject obj, jint intg){
  
  tsl2591IntegrationTime_t cIntg = static_cast<tsl2591IntegrationTime_t>(intg);
  tsl.Adafruit_TSL2591::setTiming (fd, cIntg);
  
  }  
  
  	//uint16_t full,ir;
    //uint32_t visible_and_ir, lux;
	
//    visible_and_ir = tsl.Adafruit_TSL2591::getFullLuminosity (fd);

    // Reads two byte value from channel 0 (visible + infrared)
    //full =  (visible_and_ir & 0xFFFF);
    // Reads two byte value from channel 1 (infrared)
   // ir = (visible_and_ir >> 16);

    //lux = tsl.Adafruit_TSL2591::calculateLux(full, ir);

	//int visible = 0;

   
   // if ( full >= ir ) visible = full-ir;
  
