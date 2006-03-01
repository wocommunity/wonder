#!/usr/bin/env ruby

#$LOAD_PATH.unshift(File.dirname(File.expand_path(__FILE__)))
$stats = Array.new

require 'Recording'
require 'ReportGenerator'
require 'StatModules'
require 'Utils'
require 'ReportTypes'

# Miner
# ========================
# Dec 2003 Jacek Kaczmarek
#
# Miner is a tool that puts together all parts of the WOAnalysis tool:
# - LogManager - which parses the logfiles,
# - CacheMachine - which is the repository,
# - StatModules - which analyze the repository and create stats
# - ReportGenerator - which transforms stat module results into HTML/CSV reports
# See README file.


	TOOL_VERSION="1.0.1"

	STAT_NAME_MAPPINGS = Hash[
	PAGE_STATS => PageStatModule,
	SESSION_TRACK_STATS => SessionTrackStatModule
	]



	def add_statistics( class_desc, config_hash )
		$stats.push( STAT_NAME_MAPPINGS[class_desc].new( config_hash,$log_manager ) )
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
	

	# 'mine' is a facade function for all the classes in the WOLogging tool.
	# All you have to do is to make changes to the config file (located in conf/)
	def mine()
		puts("---------------------------------------------------------------")
		$input_files=Array.new
		$stat_types=Array.new
		$config_file=nil
		$first_arg=true

		parse_args()

		$time = Time.now

		$log_manager = LogManager.new( $input_files )
		require_config( $config_file, 'OUTPUT_DIR', 'OUTPUT_FORMATS', "OUTPUT_FILE_NAME", "APPNAME_PATTERN" )
		$log_manager.process()

		$stats.each_with_index { |stat,i|
			puts(" + Generating statistics '#{stat}'..")
			puts("    - calculating..")
			stat.generate_stats()
			puts("    - generating output..")
			$rGenerator = ReportGenerator.new( stat, $time, i )
			$rGenerator.generate_reports()
		}

		puts(" + Report files should be now available in your output directory.")
		puts("---------------------------------------------------------------")	
		puts()

	end




mine()

