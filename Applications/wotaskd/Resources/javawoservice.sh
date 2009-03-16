#!/bin/sh
#   javawoservice:  A Bourne shell [sh(1)] script for configuring automated restarts
#                   of WebObjects applications. `javawoservice -help` will give a 
#                   descriptive summary.
#
#
#   IMPORTANT NOTE: This script will eat your output.  Don't expect to be able to 
#                   redirect output from this -- the output goes to aid in timing
#                   applications, etc.  If you want output, use logging (NSLog, 
#                   for instance) in your app and do it properly.
#

##
#  this is intended to be a hook for a later programmer to add support for manually
#   ensuring that child processes get killed; this does NOT always happen with WO5.x
#   apps by default (e.g. javawoservice.sh dies and children live on!), particularly
#   with signal 15 (normal `kill').
##
#trap "echo; echo 'Not gonna quit!';jobs -l; echo" 1 15

###
##
# load subroutines into shell (easy to read/debug/maintain)
##
###
print_help()
{
  echo
  echo
  echo "WebObjects 5.3 javawoservice help"
  echo 
  echo "Usage:  javawoservice [ -help ] | [ [ -waitTime <seconds> ] [ retries <attempts> ]"
  echo "              [ -scaleTimeouts YES|NO ] [ -scaleFactor <amount> ] ]"
  echo "              -appPath <path> [ <application-specific arguments> ] "
  echo 
  echo "Overview:"
  echo "  (general flags)"
  echo "    -help		bring up this help screen"
  echo 
  echo "  (javawoservice flags)"
  echo "    -waitTime		period to wait between restarts"
  echo "    -retries		number of restarts to attempt"
  echo "    -scaleTimeouts	scaled increases in wait-times, or not"
  echo "    -scaleFactor	amount by which wait-times are scaled"
  echo 
  echo "  (application flags)"
  echo "    -appPath		full path to WebObjects application executable"
  echo "    <other flags>	your application's runtime flags"
  echo 
  echo " Wait Time Value:"
  echo "     This interval, specified in seconds, is the time that javawoservice"
  echo "   should wait before restarting your application after a death."
  echo
  echo "   NOTE: Default value is 5 seconds."
  echo 
  echo " Application Restarts:"
  echo "     This flag accepts an integer amount that signifies the number of"
  echo "   times that javawoservice should attempt to restart your application"
  echo "   before giving up and allowing it to stay down."
  echo 
  echo "   NOTE: Default value is 5 attempts."
  echo
  echo " Scale Timeouts:" 
  echo "     Using this flag, the application 'wait time' period may be scaled so"
  echo "   that, with each successive death, the wait time increases.  This can"
  echo "   be helpful in preventing a needless, memory-wasting cycle whereupon "
  echo "   an application is relaunched in too-quick succession."
  echo "     If used, upon an application death that fails to meet the 'success time',"
  echo "   the 'wait time' period will increase by 'scale factor'.  This scaling will"
  echo "   occur each time that an application dies too soon."
  echo
  echo "   NOTE: Default value is YES."
  echo
  echo " Scale Factor:"
  echo "     Simply, this flag specifies the factor by which the wait-time is scaled"
  echo "   in the event of an unsuccessful application run (e.g. the app died before"
  echo "   it met the minimum 'success time').  If 'scale timeouts' is set to NO, "
  echo "   this flag's value is ignored."
  echo
  echo "   NOTE: Default value is 2."
  echo
  echo 
  echo "Command line examples: "
  echo "  % javawoservice.sh -waitTime 10 -retries 5 -scaledTimeout YES"
  echo "  >  -appPath /Local/Library/Apps/MyApp.woa/MyApp"
  echo "  >  -WOAutoOpenInBrowser NO -WOPort 2001 -WOMonitorEnabled YES"
  echo 
  echo "    (launches MyApp with inital 10 second wait time, 5 retries, "
  echo "     the scaled timeout feature; 'scale factor' is the default of 2)"
  echo 
  echo
  echo "IMPORTANT NOTE:"
  echo "     javawoservice.sh eats output from applications that it launches.  If you"
  echo "   expect to get output, use proper logging (like NSLog) programmatically from"
  echo "   inside your application.  You should not rely on a service to do proper"
  echo "   logging, in any case."
  echo 
  echo "javawoservice.sh: End of helpfile."
}


##
#  do some autosensing stuff
##
check_setup()
{
  ## guess the app's name
  #
  APP_NAME=`echo $APP_PATH | sed 's/.*\/\([^\/]*\)$/\1/'`
  #echo "App's name is: $APP_NAME"

}


##
#  print all execution args
##
print_flags()
{
echo
echo "NEXTROOT: $NEXTROOT"
echo "RETRIES:  $RETRIES"
echo "SCALED:   $SCALE_TIMEOUTS"
echo "INITIAL:  $INITIAL_WAIT_TIME"
echo "FACTOR:   $SCALE_FACTOR"
echo "APP_PATH: $APP_PATH"
echo "APP_NAME: $APP_NAME"
echo "APP_ARGS: $APP_ARGS"
echo "'GET_SECS:' '${GET_SECONDS}'"
echo
}

###
##
#  begin normal execution
##
###


##
# variables employed
##
#NEXTROOT=$NEXTROOT
INITIAL_WAIT_TIME=5			# default
CURRENT_WAIT_TIME=5
RETRIES=10				# default
CURRENT_TRY=0
SCALE_TIMEOUTS=YES			# default
SCALE_FACTOR=2				# default
#SUCCESS_TIME=43200     		# default (12 hours)
#SUCCESS_ON="NO"
APP_PATH=""
APP_NAME=""
APP_ARGS=""
EXIT_CODE="0"
#GET_SECONDS=""

##
# process the args
##
if [ $# = 0 ]
then
  echo
  echo "javawoservice.sh: Try using \`javawoservice.sh -help' for information."
  exit 0
fi

while [ $# -gt 0 ]
do
  case "$1" in
    "-help")				# print out help information
	print_help
	exit 0
	;;


    "-waitTime")       			# get timeout interval
	if [ `echo "$2" | grep "[^0-9]"` ]
	then
	  echo
	  echo "javawoservice.sh: Invalid \`$1' interval, \`$2'."
	  exit 1
        else
	  INITIAL_WAIT_TIME="$2"
	  CURRENT_WAIT_TIME="$2"	# use as starting point
	fi		
	shift
	shift				# once for flag, once for value
	;;


    "-retries")				# get number of retries
        if [ `echo "$2" | grep "[^0-9]"` ]
	then
	  echo
	  echo "javawoservice.sh: Invalid \`$1' amount, \`$2'."
	  exit 1
        else
	  RETRIES="$2"
	fi
	shift
	shift				# once for flag, once for value
	;;


    "-scaleTimeouts")			# scale timeouts?
	if [ "$2" = "YES" ]
	then
	  SCALE_TIMEOUTS=YES
	elif [ "$2" = "NO" ]
	then
	  SCALE_TIMEOUTS=NO
	else
	  echo
	  echo "javawoservice.sh: Invalid assignment to YES/NO flag (\`$1 $2')."
	  exit 1
	fi
	shift
	shift				# once for flag, once for value
	;;


    "-appPath")				# get the path to the app
	APP_PATH="$2"
	shift
	shift				# once for flag, once for value
	;;


    *)					# everything else
	if [ -z "$APP_PATH" -o ! -r "$APP_PATH" ]
	then
	  echo
	  echo "javawoservice.sh: Unable to launch -- lacking proper path to application."
	  exit 1
	else				# showtime! don't shift args here!
	  while [ $# -gt 0 ]
	  do
	    APP_ARGS="$APP_ARGS $1"	# prep-load all the args into a single var
	    shift
	  done	
	fi
	break				# continue script execution
	;;
  esac
done

check_setup
#print_flags		# this is great for debugging

while [ $CURRENT_TRY -lt $RETRIES ]
do
  echo
  echo -------------------------------------------------------------------------
  echo "javawoservice.sh: \`$APP_NAME' is starting up ..."

  # so run the app already, sheesh
  $APP_PATH 2>&1 >/dev/null $APP_ARGS
  EXIT_CODE="$?"
  
  echo "javawoservice.sh: \`$APP_NAME' exited."
  if [ "$EXIT_CODE" = "0" ] 
  then
    echo  " .  \`$APP_NAME' terminated without error.  Resetting wait time ..."
    echo  " .  Execution counted as a success."
    CURRENT_WAIT_TIME=$INITIAL_WAIT_TIME
  else
    echo  " .  \`$APP_NAME' terminated with an error (\`$EXIT_CODE').  Scaling wait time ..."
    echo  " .  Execution not counted as a success."
    CURRENT_TRY=`expr "$CURRENT_TRY" + 1`
  fi


  if [ $CURRENT_TRY -lt $RETRIES ]
  then
	echo "javawoservice.sh: \`$APP_NAME' will restart in $CURRENT_WAIT_TIME seconds ..."
	`sleep "$CURRENT_WAIT_TIME"`
	[ "$SCALE_TIMEOUTS" = "YES" ] && CURRENT_WAIT_TIME=`expr "$CURRENT_WAIT_TIME" \* "$SCALE_FACTOR"`
  else
	echo "javawoservice.sh: \`$APP_NAME' has died the maximum number of times ($RETRIES)."
	echo "javawoservice.sh: I'm giving up."
        echo
	exit 0
  fi
done

echo "javawoservice.sh: Exiting ..."





