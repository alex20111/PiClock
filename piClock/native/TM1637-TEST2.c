/*
 * Adapted from http://learn.linksprite.com/raspberry-pi/how-to-use-4-digit-7-segment-module-on-raspberrypi/
 * Removes unwanted leading 0 digit on the time, goes with 12-hour format, sets brightness down to typical.
 */
#include "TM1637.h"
#include <stdio.h>
#include <time.h>

#define clk 4//pins definitions for TM1637 and can be changed to other ports    
#define dio 5

int setup() {
  if(wiringPiSetup()==-1) {
     printf("setup wiringPi failed!");
     return 1;
  }

  pinMode(clk,INPUT);
  pinMode(dio,INPUT);
  delay(200);
  TM1637_init(clk,dio);
  TM1637_set(BRIGHT_TYPICAL,0x40,0xc0);//BRIGHT_TYPICAL = 2,BRIGHT_DARKEST = 0,BRIGHTEST = 7;
  TM1637_point(POINT_ON);
}

int main(int argc, char **argv) {
  unsigned char i = 0;
  unsigned char count = 0;
 // delay(150);
  int rt = setup();
  
  if (rt == 1){
	exit(1);
  }
  
  int mode = atoi(argv[0]);  
  
  if (mode == 1){
  
  TM1637_clearDisplay();
  
	  while(1) {
		time_t rawtime;
		struct tm *info;
		time(&rawtime);
		info = localtime(&rawtime);
		int hour = info->tm_hour;
		int min = info->tm_min;
		/* strftime(hour,3,"%I", info); */

		/* printf("Formatted date & time : |%d|\n", min ); */

		/* hours */
		if (hour == 0) {
		  TM1637_display(0,1);
		  TM1637_display(1,2);
		} else if (hour > 21) {
		  TM1637_display(0,1);
		  TM1637_display(1,hour - 22);
		} else if (hour > 12) {
		  TM1637_display(0,0x7f);
		  TM1637_display(1,hour - 12);
		} else if (hour > 9) {
		  TM1637_display(0,1);
		  TM1637_display(1,hour - 10);
		} else {
		  TM1637_display(0,0x7f);
		  TM1637_display(1,hour);
		}
		/* minutes */
		int min1 = min/10;
		int min2 = min % 10;
		TM1637_display(2,min1);
		TM1637_display(3,min2);

		delay(2000);
	  }
  }else{
  
	TM1637_clearDisplay();
  }

}
