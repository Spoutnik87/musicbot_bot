#!/bin/bash

if [ -f "/root/musicbotBot/RUNNING_PID" ]
then
	kill "$(cat /root/musicbotBot/RUNNING_PID)"
	echo "Server is stopped."
	rm /root/musicbotBot/RUNNING_PID
	sleep 5
else
	echo "Server is not running. New server is ready to start."
fi

set -a
source .env
set +a

nohup java -jar target/musicbot_bot-*-jar-with-dependencies.jar > logs.txt 2>&1 &

PID=$!

sleep 5

if ps -p $PID > /dev/null
then
  echo "Server is running."
  echo $PID > /root/musicbotBot/RUNNING_PID
  exit 0
else
  echo "Server is not running."
  wait $PID
  exit $?
fi
