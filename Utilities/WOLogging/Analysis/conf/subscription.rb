#!/usr/bin/env ruby

OUTPUT_DIR = "/var/www/wologging"
OUTPUT_FORMATS = ["html","text"]      # currently only html and text available
OUTPUT_FILE_NAME = "Subscription"


AddStatistics "SessionTrack", "Session Tracking"
AddStatistics "Page", 				"Page Stats"

APPNAME_PATTERN = /^Subscription$/ # put your regular expression here
