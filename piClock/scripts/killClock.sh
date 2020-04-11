#!/bin/bash
PID=`ps -eaf | grep clock.py | grep -v grep | awk '{print $2}'`
if [[ "" !=  "$PID" ]]; then
   
  kill -SIGINT $PID
fi
