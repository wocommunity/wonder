#!/usr/bin/env ruby

OUTPUT_DIR = "/var/www/wologging"
OUTPUT_FORMATS = ["html","text"]      # currently only html and text available
OUTPUT_FILE_NAME = "Store"


AddStatistics "SessionTrack", "Session Tracking"
AddStatisticsWithParams "SessionTrack", "Session Tracking", Array.new(1,"Main,LoginPage,OrderInfoInputPage,ConfirmationPage,ReceiptPage")

APPNAME_PATTERN = /^Store$/ # put your regular expression here
