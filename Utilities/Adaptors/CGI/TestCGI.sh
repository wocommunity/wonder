#!/bin/sh

export SCRIPT_NAME PATH_INFO SERVER_PROTOCOL DOCUMENT_ROOT REQUEST_METHOD QUERY_STRING REQUEST_URI WO_ADAPTOR_INFO_USERNAME WO_ADAPTOR_INFO_PASSWORD

if [ "$1" != "" ]; then
  APPNAME=$1
else
  APPNAME=WOAdaptorInfo
fi

if [ "$2" != "" ]; then
  REQUEST_METHOD=$2
  if [ "$REQUEST_METHOD" = "POST" ]; then
    export CONTENT_LENGTH; CONTENT_LENGTH=5
  fi
else
  REQUEST_METHOD=GET
fi


SCRIPT_NAME=/cgi-bin/WebObjects
PATH_INFO=/$APPNAME
SERVER_PROTOCOL=HTTP/1.0
DOCUMENT_ROOT=/Local/Library/WebServer/Documents
QUERY_STRING=tom+swift
REQUEST_URI=/cgi-bin/WebObjects/$APPNAME

# For debugging ...
gdb ./WebObjects
#./WebObjects
