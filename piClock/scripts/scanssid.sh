#!/bin/bash
FILE=${1}
rm $FILE
iwlist wlan0 scan | grep -i essid | awk -F'"' '{ print $2 }' >> $FILE
