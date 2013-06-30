#!/bin/sh

# To enable logging of instance startup run the command 'touch /tmp/logWebObjects'

# Log messages will be written to: 
LOG=/Library/WebObjects/Logs/SpawnOfWotaskd.log

if [ -f /tmp/logWebObjects ]; then 

	mkdir -p `dirname "$LOG"`

	echo "************" >>${LOG}
	echo "date: `date`" >>${LOG}
	echo "args: $@" >>${LOG}
	$@ 1>>${LOG} 2>&1 &

else

	$@ 1>/dev/null 2>&1 &

fi