#/bin/bash

OLDPID=`sockstat -4l -p 8000 | awk '{if ($3!="PID") print $3}'`

if [ -n "$OLDPID" ]
then
	kill $OLDPID
	echo "Server is stopped."
	sleep 5
else
	echo "Server is not running. New server is ready to start."
fi

java -jar target/musicbot_bot-*-jar-with-dependencies.jar > logs.txt 2>&1 &

PID=$!

sleep 5

if ps -p $PID > /dev/null
then
  echo "Server is running."
  exit 0
else
  echo "Server is not running."
  wait $PID
  exit $?
fi