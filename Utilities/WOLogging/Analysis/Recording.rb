#!/usr/bin/env ruby

require 'ReportGenerator'
require 'StatModules'


# RECORDING is the part of the system responsible for parsing the log file
# and storing it in memory.
# Respective classes described below.

# 84.128.218.82 [27/Mar/2006:00:00:00 +0200] 0 "satw;KWJ3H1J;com.logicunited.gaia.front.wo.Are
# aDetailPage;feature=OnlineDeliverables;entity=PrivateCam;country=de;site=satw;category=girls
# ;supplier=coha;area=HPCamAreaAll;lang=de;deliverable=59717:27278;" "http://www.sex-and-the-w
# eb.de/ecl/acams/amateur_cams/girls.html"

# SEARCH_PATTERN=/^(\S+)\s+(\S+)\s+(\S+)\s+\[(.*)\]\s+"([^"]+)"\s+(\S+)\s+(\S+)\s+"([^"]+)"\s+"([^"]+)"\s+(\S+)\s+"(.*;)"\s*$/
SEARCH_PATTERN=/^(\S+)\s+\[(.*)\]\s+(\d+)\s+"([^"]+)"\s+"([^"]+)"$/

SIMPLE_SEARCH_PATTERN=/./

# locations of elements in the pattern and in the array

POS_IP                  = 0
POS_DATE                = 1
POS_REQUEST             = 0
POS_RESPONSE            = 0
POS_BYTES               = 0
POS_REFERER             = 4
POS_AGENT               = 0
POS_REQ_TIME            = 2
TMP_POS_X_WO_CUSTOM_ENV = 3
POS_APP_NAME            = 5
POS_SESSION_ID          = 6
POS_PAGE_NAME           = 7
POS_ARBITRARY_ARGS		= 8
POS_DICTIONARY_STRING	= 9

CACHEABLE_ELEMENTS = [ POS_IP, POS_APP_NAME, POS_AGENT, POS_SESSION_ID, POS_PAGE_NAME, POS_DICTIONARY_STRING ]


# locations of elements in the x_wo_custom_env

XPOS_APP_NAME 				= 0
XPOS_SESSION_ID 			= 1
XPOS_PAGE_NAME				= 2



class LogManager

	# responsible for parsing the log file and updating cache (CacheMachine)
	
	attr_accessor :log_record_array,:cache_machine
	
	def initialize( input_files )
		@input_files = input_files
		@log_record_array = Array.new
		@cache_machine = CacheMachine.new
	end

	def process()
		@input_files.each { |file|
			process_file( file )
		}
		if @log_record_array.length == 0
			puts("No matching lines found in log. Probably your file format is discordant with the regular expression parsing the file.")
			exit 1
		end
		
		detect_microseconds()
		
	end
	
	def process_line( line )
		line = line.chomp
		if line=~SIMPLE_SEARCH_PATTERN
				parse_line( line )
		end

	end

	def process_file( input_file )
		line_count=0
		puts("---------------------------------------------------------------")
		puts(" + Processing logfile")
		File.new("#{input_file}").each_line { |line|
			process_line(line)
			line_count=line_count+1
			if line_count%1000==0
				printf "     #{line_count} lines |  caches are: "
				for i in 0..CACHEABLE_ELEMENTS.length-1
					printf("#{cache_machine.caches[i].length}, ")
				end
				printf("\n")	
			end
		}
		puts("---------------------------------------------------------------")
	end


	# based on average request handling time the tool determines whether Apache writes seconds or microseconds (probably a better solution will be a setting in config file)
	def detect_microseconds()
		sum = 0.0
		limit = [ 100, @log_record_array.length()-1 ].min
		for i in 0..limit
			sum = sum + @log_record_array[ i ].info[ POS_REQ_TIME ]	
		end
		treshold = 1000
		if ( sum / i > treshold )
			puts(" + Guessing microseconds: average request time is greater than #{treshold}")
			@log_record_array.each {|rec| rec.info[ POS_REQ_TIME ] = rec.info[ POS_REQ_TIME ].to_f / 1000000.to_f }	
		else
			puts(" + Guessing seconds: average request time is lesser than #{treshold}")
		end

					
		
	end

	
	def parse_line( line )
		match_data = SEARCH_PATTERN.match( line )
		if match_data
			@parsed_array = match_data.captures
			@wo_custom_env = @parsed_array[ TMP_POS_X_WO_CUSTOM_ENV ]			
			if ( parse_wo_custom_env( @wo_custom_env ) )
				@parsed_array = @parsed_array + @wo_record.args()
				log_record = LogRecord.new( @parsed_array )
				# log_record.display_record(@log_record_array.length)
		
				if  log_record.info[POS_APP_NAME] =~ APPNAME_PATTERN 
					@log_record_array.push( log_record )
					@cache_machine.update( log_record )
				end
				log_record.print_lengths()
			end
		end
	end

	def parse_wo_custom_env( env )
		if ( env != "-" )
			#puts(env)
			@wo_record = XWOCustomEnvRecord.new( env )
			return true
		else
			return false
		end
	end


end


class XWOCustomEnvRecord

	attr_accessor :app_name, :session_id, :page_name, :arbitrary_args, :dictionary_string

	def initialize( env )
		@arbitrary_args=Hash.new()
		array = env.split(/;/)
		@app_name 	= array[ XPOS_APP_NAME ]
		@session_id = array[ XPOS_SESSION_ID ]
		@page_name 	= array[ XPOS_PAGE_NAME ]
		@dictionary_string=""
		for i in 3..array.length-1
			@dictionary_string = "#{dictionary_string}#{array[i]},"
			key,value=array[i].split(/=/)
			@arbitrary_args[key]=value
		end
	end

	def args
		return Array[ @app_name, @session_id, @page_name, @arbitrary_args, @dictionary_string ]
	end
end


# locations of elements in links for double-linked-list

#LN_ALL			= 0
#LN_APP 			= 1
#LN_SESSION  = 2
#LN_PAGE			= 3


# instances of this class are object representations of lines in log.
class LogRecord

	attr_accessor :info,:next,:prev
	#:links,:ip,:date,:request,:response,:bytes,:referer,:agent,:req_time,:wo_record,:previous,:next

	def initialize( array )
		@info = array
		@next = Array.new(15)
		@prev = Array.new(15)

		elements = @info[ POS_PAGE_NAME ].split(/\./)
		@info[ POS_PAGE_NAME ] = elements[ elements.length - 1 ]
		@info[ POS_REQ_TIME ] = @info[ POS_REQ_TIME ].to_f
		@info[ POS_DATE ] = nil
		@info[ POS_REQUEST ] = nil
		@info[ POS_REFERER ] = nil
		@info[ POS_AGENT ] = nil
		@info[ TMP_POS_X_WO_CUSTOM_ENV ] = nil
		#@info[ POS_PAGE_NAME ] = "#{@info[ POS_APP_NAME ]}:#{@info[ POS_PAGE_NAME ]}"

	end

	
	def display_record( prefix )
		i=0
		@info.each { |val|
					puts("#{i}:#{val}, ")
			i=i+1
			}
				
			puts( "#{prefix} #{as_string}" )
			puts( track_string( POS_SESSION_ID, POS_PAGE_NAME )  )
		end

		def as_string()
			return "#{@info[POS_APP_NAME]}, #{@info[POS_SESSION_ID]}, #{@info[POS_PAGE_NAME]}"
		end


		# track string is a user-readable record of a single session
		# it says which pages and how many times the user visited.
		# e.g. Main -> LoginPage(2) -> ConfirmationPage -> ReceiptPage -> InfoPage(3)
		# The number in bracket means two or more consecutive visits to the same page
		def track_string( info_index, result_index )
			result=" " # This space is necessary for proper identification of whole page names
			n = self
			repeat_counter = 1
			while n != nil

				next_value = nil
				display_value = n.info[ result_index ]
				if ( n.next[ info_index ] != nil )
					next_value = n.next[ info_index ].info[ result_index ]
				end

				if ( display_value != next_value || n.next[ info_index ] == nil )
					result="#{result}#{display_value}"
					if repeat_counter > 1
						result = "#{result} (#{repeat_counter})"
					end
					if ( n.next[ info_index ] != nil )
						result = "#{result} -> "
					end
				end

				if ( display_value == next_value)
					repeat_counter = repeat_counter + 1
				else 
					repeat_counter = 1
				end


				n = n.next[ info_index ]
				previous_display_value = display_value
			end
			result = "#{result} " # this space is necessary for proper identification of whole names
			
			return result
		end

		def print_lengths()
			@info.each_with_index { |item,idx|
					if ( item != nil && item.to_s.length > 15 )
						#puts("#{idx}:#{item.to_s.length}  " )
				end
					}
		end
		
	end

 

  # Instance of this class holds cache of parsed requests in memory.
	# They are linked in both directions by all reasonable criteria (like appname, session id, ip, etc.)

	class CacheMachine

		attr_accessor :caches
		
		def initialize()
			@caches = Array.new( CACHEABLE_ELEMENTS.length ) { Hash.new() }
		end

		def update( new_record ) 

			@caches.each_with_index { |cache, cache_index|
			
				# find index from which particular value will be taken
				info_index = CACHEABLE_ELEMENTS[ cache_index ]
			
				# get the value
				value = new_record.info[ info_index ]
				
				# this value from the record is a key in one of the cache's hashes
				# get the value from that cache, the value is a record
				existing_record = cache[ value ]
				if ( existing_record != nil )

					#puts("Found #{value}")
					new_record.prev[ info_index ] = existing_record #bind back
					existing_record.next[ info_index ] = new_record #and forth
					new_record.info[ info_index ] = existing_record.info[ info_index ] #use reference, not copy

					cache[ value ] = new_record

				else
					#puts("Not found #{value}")
					cache[ value ] = new_record
					new_record.prev[ info_index ] = nil

				end
			}

			end

		end

