#!/usr/bin/env ruby

$LOAD_PATH.unshift(File.dirname(__FILE__))
$stats = Array.new

require 'Recording'
require 'ReportGenerator'
require 'StatModules'
require 'Statistics'
require 'Utils'

# Miner
# ========================
# Dec 2003 Jacek Kaczmarek
#
# Miner is a tool that puts together all parts of the WOAnalysis tool:
# - LogManager - which parses the logfiles,
# - CacheMachine - which is the repository,
# - StatModules - which analyze the repository and create stats
# - ReportGenerator - which transforms stat module results into HTML/CSV reports


	TOOL_VERSION="0.9"

	STAT_NAME_MAPPINGS = Hash[
	"Page" => PageStatModule,
	"SessionTrack" => SessionTrackStatModule
	]


	def add_statistics( class_desc, title ) 
		#puts("Adding '#{class_desc}'")
		$stats.push( STAT_NAME_MAPPINGS[class_desc].new( title,$log_manager ) )
	end

	Object.send(:alias_method, :AddStatistics, :add_statistics )


	def parse_args()
		if ARGV.length < 2
			puts("Usage:")
			puts("  Miner.rb <config-file> <log-file>+")
			exit
		end

		ARGV.each { |arg|
			if $first_arg
				$config_file = arg
				$first_arg = false
			else
				$input_files.push(arg)
			end
		}
	end
	
	def mine()

		$input_files=Array.new
		$stat_types=Array.new
		$config_file=nil
		$first_arg=true

		parse_args()

		$time = Time.now

		$log_manager = LogManager.new( $input_files )
		require_config( $config_file, 'OUTPUT_DIR', 'OUTPUT_FORMATS', "OUTPUT_FILE_NAME", "APPNAME_PATTERN" )
		$log_manager.process()

		$stats.each { |stat|
			stat.generate_stats()
			$rGenerator = ReportGenerator.new( stat, $time )
			$rGenerator.generate_reports()
		}

		puts(" + Report files should be now available in your output directory.")
		puts()

	end




mine()

