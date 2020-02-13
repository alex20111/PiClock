#define TSL2561_ADDR_LOW                   (0x29)
#define TSL2561_ADDR_FLOAT                 (0x39)    
#define TSL2561_ADDR_HIGH                   (0x49)


#include "2591a.h"
#include <unistd.h> 

int main(){

//fprintf(stderr, "I will be printed immediately");
	
    Adafruit_TSL2591 tsl = Adafruit_TSL2591(2591, TSL2591_INTEGRATIONTIME_200MS , TSL2591_GAIN_LOW ); // pass in a number for the sensor identifier (for your use later)

    int fd = 0;
    fd = wiringPiI2CSetup(0x29);
    uint16_t ch0,ch1;
    uint32_t visible_and_ir, lux;
	
	//fprintf(stderr, "I will be printed immediatel2y");
	
	
	
//	while(true){
//  for(int i = 0; i < 10; i ++){
    visible_and_ir = tsl.Adafruit_TSL2591::getFullLuminosity (fd);
    sleep(1);
    visible_and_ir = tsl.Adafruit_TSL2591::getFullLuminosity (fd);

    // Reads two byte value from channel 0 (visible + infrared)
    ch0 =  (visible_and_ir & 0xFFFF);
    // Reads two byte value from channel 1 (infrared)
    ch1 = (visible_and_ir >> 16);

    lux = tsl.Adafruit_TSL2591::calculateLux(ch0, ch1);

int visible = 0;

   // printf("TSL2591 Full Spectrum: %zu\n",ch0);
   // printf("TSL2591 Infrared: %zu\n",ch1);
    if ( ch0 >= ch1 ) visible = ch0-ch1;
//		printf("TSL2591 Visible: %zu\n",ch0-ch1);
    //printf("TSL2591 Lux: %zu\n", lux);
	
	printf("%zu,%zu,%zu,%zu", ch0, ch1, visible, lux);
	fflush(stdout);
	//}
}