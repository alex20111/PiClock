https://www.programiz.com/c-programming/c-file-input-output
https://fresh2refresh.com/c-programming/c-file-handling/fseek-seek_set-seek_cur-seek_end-functions-c/


https://stackoverflow.com/questions/53426768/create-share-library-with-wiringpi    example to try
https://www.raspberrypi.org/forums/viewtopic.php?t=174501    - for importing libraries....  Or put it into the current folder

#define TSL2561_ADDR_LOW                   (0x29)
#define TSL2561_ADDR_FLOAT                 (0x39)    
#define TSL2561_ADDR_HIGH                   (0x49)

#include <stdio.h>
#include <stdlib.h>
#include "2591a.h"
#include <unistd.h> 

int main(int argc, char **argv){

	//file path
	//integration
	//gain

	printf("PATH:  %s\n", argv[0]);  
	printf("INTG:  %d\n", argv[1]);  
	printf("GAIN:  %d\n", argv[2]);  
	
	int intg = atoi(argv[1]);
	int gain = atoi(argv[2]);	

	FILE *fptr;
	
	ptr = fopen("home/pi/native/TSL2561/tslReading.rdr","w");
	
	if(fptr == NULL)
   {
      printf("File Error %s",argv[0] );   
      exit(1);             
   }
	
    //Adafruit_TSL2591 tsl = Adafruit_TSL2591(2591, TSL2591_INTEGRATIONTIME_200MS , TSL2591_GAIN_LOW ); // pass in a number for the sensor identifier (for your use later)
	Adafruit_TSL2591 tsl = Adafruit_TSL2591(2591, processIntegration(intg) , processGain(gain) ); // pass in a number for the sensor identifier (for your use later)

    int fd = 0;
    fd = wiringPiI2CSetup(0x29);
    uint16_t full ,ir;
    uint32_t visible_and_ir, lux;	

	//loop until program terminated
	while(1){
		 fseek(fp, 0, SEEK_SET);

		visible_and_ir = tsl.Adafruit_TSL2591::getFullLuminosity (fd);
	//	sleep(1);
   //	visible_and_ir = tsl.Adafruit_TSL2591::getFullLuminosity (fd);

		// Reads two byte value from channel 0 (visible + infrared)
		full =  (visible_and_ir & 0xFFFF);
		// Reads two byte value from channel 1 (infrared)
		ir = (visible_and_ir >> 16);

		lux = tsl.Adafruit_TSL2591::calculateLux(full , ir);

		int visible = 0;

	   // printf("TSL2591 Full Spectrum: %zu\n",full);
	   // printf("TSL2591 Infrared: %zu\n",ir);
		if ( full >= ir ) visible = full-ir;
	//		printf("TSL2591 Visible: %zu\n",full-ir);
		//printf("TSL2591 Lux: %zu\n", lux);
		
		fprintf(fptr, "%zu,%zu,%zu,%zu", full, ir, visible, lux);
		sleep(1000);
	}
}

tsl2591IntegrationTime_t processIntegration(int intg){
	switch (intg)
​	{
		case 0:
		  return TSL2591_INTEGRATIONTIME_100MS;
		  break;
		case 1:
		  return TSL2591_INTEGRATIONTIME_200MS;
		  break;  
		case 2:
		  return TSL2591_INTEGRATIONTIME_300MS ;
		  break; 
		case 3:
		  return TSL2591_INTEGRATIONTIME_400MS;
		  break; 
		case 4:
		  return TSL2591_INTEGRATIONTIME_500MS;
		  break; 
		case 5:
		  return TSL2591_INTEGRATIONTIME_600MS;
		  break; 	  
		default:
			return TSL2591_INTEGRATIONTIME_100MS;
	}
}

tsl2591Gain_t processGain(int gain){
	switch (gain)
​	{
		case 0:
		  return TSL2591_GAIN_LOW;
		  break;
		case 1:
		  return TSL2591_GAIN_MED;
		  break;  
		case 2:
		  return TSL2591_GAIN_HIGH ;
		  break; 
		case 3:
		  return TSL2591_GAIN_MAX;
		  break; 	  
		default:
			return TSL2591_GAIN_LOW;
	}
}
