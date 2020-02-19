/**************************************************************************/
/*! 
    @file     Adafruit_TSL2591.cpp
    @author   KT0WN (adafruit.com)
    This is a library for the Adafruit TSL2591 breakout board
    This library works with the Adafruit TSL2591 breakout 
    ----> https://www.adafruit.com/products/1980
	
    Check out the links above for our tutorials and wiring diagrams 
    These chips use I2C to communicate
	
    Adafruit invests time and resources providing this open source code, 
    please support Adafruit and open-source hardware by purchasing 
    products from Adafruit!
    @section LICENSE
    Software License Agreement (BSD License)
    Copyright (c) 2014 Adafruit Industries
    All rights reserved.
    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:
    1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
    3. Neither the name of the copyright holders nor the
    names of its contributors may be used to endorse or promote products
    derived from this software without specific prior written permission.
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS ''AS IS'' AND ANY
    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY
    DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
/*! 
    @file     2591a.cpp
    @author   hivetool.org
    This started as Adafruit's TSL2591 library.  The calls to Adrino funtions were commented out 
    and replaced with calls to similar functions in the wiringPi library.

*/

/**************************************************************************/

#include <stdlib.h>
#include "2591a.h"

Adafruit_TSL2591::Adafruit_TSL2591(int32_t sensorID, tsl2591IntegrationTime_t intg, tsl2591Gain_t  gain) 
{
  _initialized = false;
  _integration = intg;
//  _gain        = TSL2591_GAIN_MED;
  _gain        = gain;
  _sensorID    = sensorID;

  // we cant do wire initialization till later, because we havent loaded Wire yet
}

bool Adafruit_TSL2591::begin(int fd) 
{
//  Wire.begin();
   uint8_t d; 

//  for (uint8_t i=0; i<0x20; i++) 
//  {
//    wiringPiI2CReadReg8(fd,012);
//    uint8_t id = read8(0x12);
//    Serial.print("$"); Serial.print(i, HEX); 
//    Serial.print(" = 0x"); Serial.println(read8(i), HEX);
//    d = read8(i);
//    printf ("%x=%x\n",i,d);
//  }

 wiringPiI2CWriteReg8(fd, TSL2591_COMMAND_BIT | TSL2591_REGISTER_ENABLE, TSL2591_ENABLE_POWERON | TSL2591_ENABLE_AEN | TSL2591_ENABLE_AIEN);

// TSL2591_CONFIG_RW, [TSL2591_DEVICE_RESET_VALUE],
//  uint8_t id = read8(0x12);

  uint8_t id  = wiringPiI2CReadReg8(fd,0x80 | 0x20 | 0x12);
//  printf("id=%x\n",id);

  if (id == 0x50 ) 
  {
    //Serial.println("Found Adafruit_TSL2591");
//     printf("Found Adafruit_TSL2591\n");
  } 
  else 
  {
    printf("ERROR: Could not find Adafruit_TSL2591 (id 0x50). Found: %x\n", id);
    return false;
  }
  
  _initialized = true;

  // Set default integration time and gain
  setTiming(fd, _integration);
  setGain(fd,  _gain);
  
  // Note: by default, the device is in power down mode on bootup
  disable(fd);

  return true;
}

void Adafruit_TSL2591::enable(int fd)
{
  if (!_initialized)
  {
    if (!begin(fd))
    {
      return;
    }
  }

  // Enable the device by setting the control bit to 0x01
//  write8(TSL2591_COMMAND_BIT | TSL2591_REGISTER_ENABLE, TSL2591_ENABLE_POWERON | TSL2591_ENABLE_AEN | TSL2591_ENABLE_AIEN);
//  wiringPiI2CWriteReg8(fd, TSL2561_COMMAND_BIT, TSL2561_CONTROL_POWERON); //enable the device
  wiringPiI2CWriteReg8(fd, TSL2591_COMMAND_BIT | TSL2591_REGISTER_ENABLE, TSL2591_ENABLE_POWERON | TSL2591_ENABLE_AEN | TSL2591_ENABLE_AIEN);

  
}

void Adafruit_TSL2591::disable(int fd)
{
  if (!_initialized)
  {
    if (!begin(fd))
    {
      return;
    }
  }

  // Disable the device by setting the control bit to 0x00
//  write8(TSL2591_COMMAND_BIT | TSL2591_REGISTER_ENABLE, TSL2591_ENABLE_POWEROFF);
//  wiringPiI2CWriteReg8(fd, TSL2561_COMMAND_BIT, TSL2561_CONTROL_POWEROFF); //disable the device
  wiringPiI2CWriteReg8(fd, TSL2591_COMMAND_BIT | TSL2591_REGISTER_ENABLE, TSL2591_ENABLE_POWEROFF);

  
}

void Adafruit_TSL2591::setGain(int fd, tsl2591Gain_t gain) 
{
  if (!_initialized)
  {
    if (!begin(fd))
    {
      return;
    }
  }

  enable(fd);
  _gain = gain;
//  write8(TSL2591_COMMAND_BIT | TSL2591_REGISTER_CONTROL, _integration | _gain);  
  wiringPiI2CWriteReg8(fd, TSL2591_COMMAND_BIT | TSL2591_REGISTER_CONTROL, _integration | _gain);  
  disable(fd);
}

tsl2591Gain_t Adafruit_TSL2591::getGain()
{
  return _gain;
}

void Adafruit_TSL2591::setTiming(int fd, tsl2591IntegrationTime_t integration)
{
  if (!_initialized)
  {
    if (!begin(fd))
    {
      return;
    }
  }

  enable(fd);
  _integration = integration;
//  write8(TSL2591_COMMAND_BIT | TSL2591_REGISTER_CONTROL, _integration | _gain);  
  wiringPiI2CWriteReg8(fd, TSL2591_COMMAND_BIT | TSL2591_REGISTER_CONTROL, _integration | _gain);  
  disable(fd);
}

tsl2591IntegrationTime_t Adafruit_TSL2591::getTiming()
{
  return _integration;
}

float Adafruit_TSL2591::calculateLux(uint16_t ch0, uint16_t ch1)
{
  float atime, again;
  float    cpl, lux1, lux2, lux;
  uint32_t chan0, chan1;

  // Check for overflow conditions first
  if ((ch0 == 0xFFFF) | (ch1 == 0xFFFF))
  {
    // Signal an overflow
    return -1;
  }

  // Note: This algorithm is based on preliminary coefficients
  // provided by AMS and may need to be updated in the future
  
  switch (_integration)
  {
    case TSL2591_INTEGRATIONTIME_100MS :
      atime = 100.0F;
      break;
    case TSL2591_INTEGRATIONTIME_200MS :
      atime = 200.0F;
      break;
    case TSL2591_INTEGRATIONTIME_300MS :
      atime = 300.0F;
      break;
    case TSL2591_INTEGRATIONTIME_400MS :
      atime = 400.0F;
      break;
    case TSL2591_INTEGRATIONTIME_500MS :
      atime = 500.0F;
      break;
    case TSL2591_INTEGRATIONTIME_600MS :
      atime = 600.0F;
      break;
    default: // 100ms
      atime = 100.0F;
      break;
  }
  
  switch (_gain)
  {
    case TSL2591_GAIN_LOW :
      again = 1.0F;
      break;
    case TSL2591_GAIN_MED :
      again = 25.0F;
      break;
    case TSL2591_GAIN_HIGH :
      again = 428.0F;
      break;
    case TSL2591_GAIN_MAX :
      again = 9876.0F;
      break;
    default:
      again = 1.0F;
      break;
  }

  // cpl = (ATIME * AGAIN) / DF
  cpl = (atime * again) / TSL2591_LUX_DF;
  
//printf("ch0= %zu   ch1=%zu\n",ch0,ch1);

  //lux1 = ( (float)ch0 - (TSL2591_LUX_COEFB * (float)ch1) ) / cpl;
  //lux2 = ( ( TSL2591_LUX_COEFC * (float)ch0 ) - ( TSL2591_LUX_COEFD * (float)ch1 ) ) / cpl;

//  printf("Lux1= %f   Lux2=%f\n",lux1,lux2); 
  // The highest value is the approximate lux equivalent
  //lux = lux1 > lux2 ? lux1 : lux2;

  // Alternate lux calculation 1
  // See: https://github.com/adafruit/Adafruit_TSL2591_Library/issues/14
  lux = ( ((float)ch0 - (float)ch1 )) * (1.0F - ((float)ch1/(float)ch0) ) / cpl;
  // Alternate lux calculation 2
  //lux = ( (float)ch0 - ( 1.7F * (float)ch1 ) ) / cpl;
  // Signal I2C had no errors
  return lux;
}

uint32_t Adafruit_TSL2591::getFullLuminosity (int fd)
{

  if (!_initialized)
  {
    if (!begin(fd))
    {
      return 0;
    }
  }

  // Enable the device
  enable(fd);
  // Wait x ms for ADC to complete
  for (uint8_t d=0; d<=_integration; d++) 
  {
    delay(120);
  }

  // CHAN0 must be read before CHAN1
  // See: https://forums.adafruit.com/viewtopic.php?f=19&t=124176
  uint32_t x;
  uint16_t y;
  
  y = wiringPiI2CReadReg16(fd,  TSL2591_COMMAND_BIT | TSL2591_REGISTER_CHAN0_LOW);
  x = wiringPiI2CReadReg16(fd, TSL2591_COMMAND_BIT | TSL2591_REGISTER_CHAN1_LOW);
  x <<= 16;
//  x |= read16(TSL2591_COMMAND_BIT | TSL2591_REGISTER_CHAN0_LOW);
  
// printf("Chan0=%u\n",y);

  x |= y;
 
  disable(fd);

  return x;
}

uint16_t Adafruit_TSL2591::getLuminosity (int fd, uint8_t channel) 
{
  uint32_t x = getFullLuminosity(fd);

  if (channel == TSL2591_FULLSPECTRUM) 
  {
    // Reads two byte value from channel 0 (visible + infrared)
    return (x & 0xFFFF);
  } 
  else if (channel == TSL2591_INFRARED) 
  {
    // Reads two byte value from channel 1 (infrared)
    return (x >> 16);
  } 
  else if (channel == TSL2591_VISIBLE) 
  {
    // Reads all and subtracts out just the visible!
    return ( (x & 0xFFFF) - (x >> 16));
  }
  
  // unknown channel!
  return 0;
}



/**************************************************************************/
/*!
    @brief  Gets the most recent sensor event
*/
/**************************************************************************/
//bool Adafruit_TSL2591::getEvent(sensors_event_t *event)
//{
//  uint16_t ir, full;
//  uint32_t lum = getFullLuminosity();
  /* Early silicon seems to have issues when there is a sudden jump in */
  /* light levels. :( To work around this for now sample the sensor 2x */
//  lum = getFullLuminosity();
//  ir = lum >> 16;
//  full = lum & 0xFFFF;  
  
  /* Clear the event */
//  memset(event, 0, sizeof(sensors_event_t));
  
//  event->version   = sizeof(sensors_event_t);
//  event->sensor_id = _sensorID;
//  event->type      = SENSOR_TYPE_LIGHT;
//  event->timestamp = millis();

  /* Calculate the actual lux value */
  /* 0 = sensor overflow (too much light) */
//  event->light = calculateLux(full, ir);
  
//  return true;
//}

/**************************************************************************/
/*!
    @brief  Gets the sensor_t data
*/
/**************************************************************************/
/*
void Adafruit_TSL2591::getSensor(sensor_t *sensor)
{
  // Clear the sensor_t object
  memset(sensor, 0, sizeof(sensor_t));

  // Insert the sensor name in the fixed length char array 
  strncpy (sensor->name, "TSL2591", sizeof(sensor->name) - 1);
  sensor->name[sizeof(sensor->name)- 1] = 0;
  sensor->version     = 1;
  sensor->sensor_id   = _sensorID;
  sensor->type        = SENSOR_TYPE_LIGHT;
  sensor->min_delay   = 0;
  sensor->max_value   = 88000.0;
  sensor->min_value   = 0.0;
  sensor->resolution  = 1.0;
}
*/
