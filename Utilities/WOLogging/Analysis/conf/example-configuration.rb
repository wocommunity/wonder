#!/usr/bin/env ruby

require 'ReportTypes'

APPNAME_PATTERN = /^ExampleAppName$/ # put your regular expression here

OUTPUT_DIR = "/var/www/localhost/wologging/output/"
OUTPUT_FORMATS = ["html","text"]      # currently only html and text available
OUTPUT_FILE_NAME = "example-%Y-%m-%d-%H%M%S"

# currently SessionTrack and Page statistics available
AddStatistics SESSION_TRACK_STATS, 
	Hash[ "Validation Messages" => 
								Hash[ STAT_TYPE 										=> ValidationReport,
											VL_VALIDATION_KEY 						=> "validationMessage",
											VL_VALIDATION_PAGE 						=> "RegistrationPage",
											VL_VALIDATION_SUCCESS_PAGE 		=> "RegistrationConfirmPage"],
	
				"Conversion Rate" =>
								Hash[ STAT_TYPE											=> ConversionReport,
											CV_CONVERSION_PAGES						=> ["RegistrationPage","RegistrationConfirmPage","RegistrationDonePage"]],

				"Individual User Tracking" =>
								Hash[ STAT_TYPE											=> IndividualTracksReport]#IT_INDIVIDUAL_TRACKS]
			]

AddStatistics PAGE_STATS,
	Hash[ "Average request time" =>
								Hash[ STAT_TYPE 										=> AvgReqTimeReport],
				"Page visits" =>
								Hash[ STAT_TYPE											=> PageVisitsReport]
			]


