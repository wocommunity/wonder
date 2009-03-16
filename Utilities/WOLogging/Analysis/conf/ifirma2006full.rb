#!/usr/bin/env ruby

require 'ReportTypes'

APPNAME_PATTERN = /^ifirma200[56]$/ # put your regular expression here

OUTPUT_DIR = "/var/www/localhost/wologging/ifirma/irrelevant"
OUTPUT_FORMATS = ["html"]#,"text"]      # currently only html and text available
OUTPUT_FILE_NAME = "ifirma-%Y-%m-%d-%H%M%S"

# currently SessionTrack and Page statistics available
AddStatistics SESSION_TRACK_STATS, 
	Hash[ "Validation Messages" => 
								Hash[ STAT_TYPE 										=> ValidationReport,
											VL_VALIDATION_KEY 						=> "validationMessage",
											VL_VALIDATION_PAGE 						=> "Registration",
											VL_VALIDATION_SUCCESS_PAGE 		=> "RegistrationConfirm"],
	
				"Conversion Rate" =>
								Hash[ STAT_TYPE											=> ConversionReport,
											CV_CONVERSION_PAGES						=> ["Registration","RegistrationConfirm","RegistrationDone"]],

				"Individual User Tracking" =>
								Hash[ STAT_TYPE											=> IndividualTracksReport]#IT_INDIVIDUAL_TRACKS]
			]

AddStatistics PAGE_STATS,
	Hash[ "Average request time" =>
								Hash[ STAT_TYPE 										=> AvgReqTimeReport],
				"Page visits" =>
								Hash[ STAT_TYPE											=> PageVisitsReport]
			]


