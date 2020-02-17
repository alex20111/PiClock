#!/bin/bash
cd /home/pi/piClock
sudo java -Djava.library.path=/home/pi/piClock/nativeLib -jar piClock-0.0.1-SNAPSHOT.jar
