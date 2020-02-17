#include "TM1637.h"
  
#define clk 4//pins definitions for TM1637 and can be changed to other ports       
#define dio 5
  
int setup()
{
  
  if(wiringPiSetup()==-1)
  {
     printf("setup wiringPi failed ! n");
     return 1;
  }
  
  pinMode(clk,INPUT);
  pinMode(dio,INPUT);
  delay(200); 
  
  TM1637_init(clk,dio);
  TM1637_set(BRIGHTEST,0x40,0xc0);//BRIGHT_TYPICAL = 2,BRIGHT_DARKEST = 0,BRIGHTEST = 7;
  
  printf("Digital Tube test code!n");
  printf("Using DATA = GPIO5, CLK = GPIO4.n");   
  
}
  
int main()
{
  
  int8_t NumTab[] = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};//0~9,A,b,C,d,E,F
  int8_t ListDisp[4];
  unsigned char i = 0;
  unsigned char count = 0;
  delay(150);
  setup();
  while(1)
  {
    unsigned char BitSelect = 0;
    i = count;
    count ++;
    if(count == sizeof(NumTab)) count = 0;
    for(BitSelect = 0;BitSelect < 4;BitSelect ++)
    {
      ListDisp[BitSelect] = NumTab[i];
      i ++;
      if(i == sizeof(NumTab)) i = 0;
    }
    TM1637_display(0,ListDisp[0]);
    TM1637_display(1,ListDisp[1]); 
    TM1637_display(2,ListDisp[2]);
    TM1637_display(3,ListDisp[3]);
    delay(500);
  }
}