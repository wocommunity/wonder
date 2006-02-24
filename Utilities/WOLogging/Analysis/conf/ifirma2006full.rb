#!/usr/bin/env ruby

OUTPUT_DIR = "/var/www/localhost/wologging/ifirma/irrelevant"
OUTPUT_FORMATS = ["html"]#,"text"]      # currently only html and text available

OUTPUT_FILE_NAME = "ifirma-%Y-%m-%d-%H%M%S"

# currently SessionTrack and Page statistics available

AddStatistics "Page", 				"Page Stats"
AddStatisticsWithParams "SessionTrack", "Session Tracking", [ "Registration,RegistrationConfirm,RegistrationDone", "Registration,RegistrationConfirm", "validationMessage" ]

APPNAME_PATTERN = /^ifirma200[56]$/ # put your regular expression here
