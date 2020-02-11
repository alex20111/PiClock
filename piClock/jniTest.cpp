//#include "SparkFunSi4703.h"
#include <iostream>
#include <thread>
#include "com_jniTest_jniTest.h"

int resetPin = 23;  // GPIO_23.
int sdaPin = 0;     // GPIO_0 (SDA).

//Si4703_Breakout radio(resetPin, sdaPin);


/*
 * Class:     com_jniTest_JniTest
 * Method:    powerOn
 * Signature: ()V
 */
JNIEXPORT jint JNICALL Java_com_jniTest_JniTest_powerOn
  (JNIEnv* env, jobject javaObj){
  
	std::cout << "power ON" << std::endl;
	
	//Status stat = radio.powerOn();
	
//	 if (stat == Status::SUCCESS){
//	 return 0;
//	}else{
//	 return 1;
//	}  
	return 0;
  }
  
  /*
 * Class:     com_jniTest_JniTest
 * Method:    powerOff
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_jniTest_JniTest_powerOff
  (JNIEnv* env, jobject javaObj){
  
	std::cout << "power OFF" << std::endl;
	
	//radio.powerOff();
  
  }
/*
 * Class:     com_jniTest_JniTest
 * Method:    setFerequency
 * Signature: (F)V
 */
JNIEXPORT void JNICALL Java_com_jniTest_JniTest_setFerequency
  (JNIEnv* env, jobject javaObj, jfloat freq){
	std::cout << "Set Frequency: " << freq << std::endl;
	
	//radio.setFrequency(freq);
  }
/*
 * Class:     com_jniTest_JniTest
 * Method:    getFrequency
 * Signature: ()F
 */
JNIEXPORT jfloat JNICALL Java_com_jniTest_JniTest_getFrequency
  (JNIEnv* env, jobject javaObj){
  
	std::cout << "GET Frequency: " <<  std::endl;
	
	//float frequency = radio.getFrequency();
	
	return (float)1.1;
  }

/*
 * Class:     com_jniTest_JniTest
 * Method:    setVolume
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_jniTest_JniTest_setVolume
  (JNIEnv* env, jobject javaObj, jint volume){
  std::cout << "set volume: " <<  volume << std::endl;
  
	//radio.setVolume(5);
  
  }
  
  
  public class JniTest {
	
	static {
        System.loadLibrary("radioNative");
    }	

	public static void main(String[] args) {
		//Power on,		// 0 success, 1 error
		//power Off		
		//get RDS		
		//setFerequency		
		//getFrequency		
		//setVolume
	}
	
	private native int powerOn();
	private native void powerOff();
	private native void setFerequency(float frequency);
	private native float getFrequency();
	private native void setVolume(int volume);

}
  
  