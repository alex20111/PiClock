#ifndef _TSL2591_H_
#define _TSL2591_H_

#include <stdio.h>
#include <inttypes.h>
#include <wiringPi.h>
#include <wiringPiI2C.h>
#include <errno.h>


#define TSL2591_VISIBLE           (2)       // channel 0 - channel 1
#define TSL2591_INFRARED          (1)       // channel 1
#define TSL2591_FULLSPECTRUM      (0)       // channel 0

#define TSL2591_ADDR              (0x29)
#define TSL2591_READBIT           (0x01)

#define TSL2591_COMMAND_BIT       (0xA0)    // bits 7 and 5 for 'command normal'
#define TSL2591_CLEAR_BIT         (0x40)    // Clears any pending interrupt (write 1 to clear)
#define TSL2591_WORD_BIT          (0x20)    // 1 = read/write word (rather than byte)
#define TSL2591_BLOCK_BIT         (0x10)    // 1 = using block read/write

#define TSL2591_ENABLE_POWERON    (0x01)
#define TSL2591_ENABLE_POWEROFF   (0x00)
#define TSL2591_ENABLE_AEN        (0x02)
#define TSL2591_ENABLE_AIEN       (0x10)

#define TSL2591_CONTROL_RESET     (0x80)

#define TSL2591_LUX_DF            (408.0F)
#define TSL2591_LUX_COEFB         (1.64F)  // CH0 coefficient 
#define TSL2591_LUX_COEFC         (0.59F)  // CH1 coefficient A
#define TSL2591_LUX_COEFD         (0.86F)  // CH2 coefficient B

typedef enum
{
  TSL2591_REGISTER_ENABLE           = 0x00,
  TSL2591_REGISTER_CONTROL          = 0x01,
  TSL2591_REGISTER_THRESHHOLDL_LOW  = 0x02,
  TSL2591_REGISTER_THRESHHOLDL_HIGH = 0x03,
  TSL2591_REGISTER_THRESHHOLDH_LOW  = 0x04,
  TSL2591_REGISTER_THRESHHOLDH_HIGH = 0x05,
  TSL2591_REGISTER_INTERRUPT        = 0x06,
  TSL2591_REGISTER_CRC              = 0x08,
  TSL2591_REGISTER_ID               = 0x0A,
  TSL2591_REGISTER_CHAN0_LOW        = 0x14,
  TSL2591_REGISTER_CHAN0_HIGH       = 0x15,
  TSL2591_REGISTER_CHAN1_LOW        = 0x16,
  TSL2591_REGISTER_CHAN1_HIGH       = 0x17
};

typedef enum
{
  TSL2591_INTEGRATIONTIME_100MS     = 0x00,
  TSL2591_INTEGRATIONTIME_200MS     = 0x01,
  TSL2591_INTEGRATIONTIME_300MS     = 0x02,
  TSL2591_INTEGRATIONTIME_400MS     = 0x03,
  TSL2591_INTEGRATIONTIME_500MS     = 0x04,
  TSL2591_INTEGRATIONTIME_600MS     = 0x05,
}
tsl2591IntegrationTime_t;

typedef enum
{
  TSL2591_GAIN_LOW                  = 0x00,    // low gain (1x)
  TSL2591_GAIN_MED                  = 0x10,    // medium gain (25x)
  TSL2591_GAIN_HIGH                 = 0x20,    // medium gain (428x)
  TSL2591_GAIN_MAX                  = 0x30,    // max gain (9876x)
}
tsl2591Gain_t;

class Adafruit_TSL2591
{
 public:
  Adafruit_TSL2591(int32_t sensorID = -1, tsl2591IntegrationTime_t intg =  TSL2591_INTEGRATIONTIME_300MS, tsl2591Gain_t  gain = TSL2591_GAIN_MED );
  
  bool      begin   ( int fd);
  void      enable  ( int fd);
  void      disable ( int fd);
  void      write8  ( uint8_t r, uint8_t v );
  uint16_t  read16  ( uint8_t reg );
  uint8_t   read8   ( uint8_t reg );

  uint32_t  calculateLux  ( uint16_t ch0, uint16_t ch1 );
  void      setGain       ( int fd, tsl2591Gain_t gain );
  void      setTiming     ( int fd, tsl2591IntegrationTime_t integration );
  uint16_t  getLuminosity (int fd, uint8_t channel );
  uint32_t  getFullLuminosity (int fd );

  tsl2591IntegrationTime_t getTiming();
  tsl2591Gain_t            getGain();
  
  /* Unified Sensor API Functions */  
//  bool getEvent  ( sensors_event_t* );
//  void getSensor ( sensor_t* );

 private:
  tsl2591IntegrationTime_t _integration;
  tsl2591Gain_t _gain;
  int32_t _sensorID;

  bool    _initialized;
};
#endif