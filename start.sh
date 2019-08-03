#!/bin/bash

if [ -f "RUNNING_PID" ]
then
	kill "$(cat RUNNING_PID)"
	echo "Server is stopped."
	sleep 5
else
	echo "Server is not running. New server is ready to start."
fi

nohup java -jar target/musicbot_bot-*-jar-with-dependencies.jar > logs.txt 2>&1 &

PID=$!

sleep 5

if ps -p $PID > /dev/null
then
  echo "Server is running."
  echo $PID > RUNNING_PID
  exit 0
else
  echo "Server is not running."
  wait $PID
  exit $?
fi