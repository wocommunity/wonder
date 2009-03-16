#!/bin/sh

woadaptor_enable="${woadaptor_enable:-NO}"
woadaptor_user="${woadaptor_user:-_appserver}" 
woadaptor_socket="${woadaptor_socket:-/tmp/woadaptor.sock}"
woadaptor_processes="${woadaptor_processes:-10}"
woadaptor_configurl="${woadaptor_configurl:-http://localhost:1085}"
woadaptor_username="${woadaptor_username:-disabled}"
woadaptor_password="${woadaptor_password:-disabled}"

runuser="${woadaptor_user}"
username="${woadaptor_username}"
password="${woadaptor_password}"

procname="/usr/local/libexec/WebObjects-fcgi"
fcgi_command="/usr/local/bin/cgi-fcgi"
fcgi_args="-start -connect ${woadaptor_socket} ${procname} ${woadaptor_processes}"
#command="/usr/sbin/daemon"
command_args="${fcgi_command} ${fcgi_args}"

start_precmd()
{
  if [ "x${runuser}" = "xroot" ]; then
    err 1 "You have to set woadaptor_user to a non-root user for security reasons"
  fi

  if [ "x${username}" != "x" -a "x${username}" != "disabled" ]; then
    WO_ADAPTOR_INFO_USERNAME=${username}
    if [ "${username}" != "public" ]; then
      WO_ADAPTOR_INFO_PASSWORD=${password}
    fi
  else
    unset WO_ADAPTOR_INFO_USERNAME
    unset WO_ADAPTOR_INFO_PASSWORD
  fi
  export WO_CONFIG_URL=${woadaptor_configurl}
  export WO_ADAPTOR_INFO_USERNAME WO_ADAPTOR_INFO_PASSWORD
  rm -f /tmp/WOAdaptorState
}

start_precmd
$command $command_args "$1"
