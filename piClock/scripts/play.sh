#!/bin/bash
cd /home/pi/piClock
sudo arecord --format=cd --rate=16000 | aplay --format=cd --rate=16000

