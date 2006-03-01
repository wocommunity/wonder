#!/usr/bin/env ruby


class StatModule

	attr_accessor :result_hash,:config_hash
	
	def initialize( config_hash, log_manager )
		puts(" + Initializing stat module: "+self.class.to_s)
		@config_hash = config_hash
		@cache_machine = log_manager.cache_machine
		@log_record_array = log_manager.log_record_array
		@type = nil
		@result_hash = Hash.new
    @config_hash.each_key {|stat_name|
      puts("    - Initializing #{stat_name}")
      class_desc = @config_hash[stat_name][STAT_TYPE]
      if class_desc!="X"
        @result_hash[stat_name] = class_desc.new( @config_hash[stat_name] )
      end
    }	

	end

	def generate_stats()
	end
		
	def find_starters()
		@log_record_array.find_all { |rec| rec.prev[@type] == nil }
	end

	def prepare_results()
		@result_hash.each_key {|stat_name|
			@result_hash[stat_name].generate_output		
		}
	end

end


class PageInfo

	RT_WARNING = 5
	
	attr_accessor :req_count, :total_req_time, :page_name
	def	initialize( page_name )
		@page_name=page_name
		@req_count=0
		@total_req_time=0
		@worst_requests=Array.new
	end

	def avg_req_time()
		if ( @req_count != 0 )
			return ( @total_req_time.to_f / @req_count.to_f * 1000.0 ).round.to_f / 1000
		end
		return 0
	end

	def update( record )
		req_time = record.info[ POS_REQ_TIME ].to_f
		if ( req_time > RT_WARNING ) 
			@worst_requests.push( record )	
		end
		@total_req_time = @total_req_time+req_time
		@req_count = @req_count + 1
	end

	def record_dictionaries(number)
		if (number == 0)
			number = MAXINT
		end
		return @worst_requests.sort{ |r1,r2| r2.info[ POS_REQ_TIME ] <=> r1.info[ POS_REQ_TIME ] }.collect{ |record| "[ #{record.info[ POS_REQ_TIME ]}s ] #{record.info[ POS_DICTIONARY_STRING ]}" }[0..number].join("\n")
	end

	def record_dicitonaries
		return record_dictionaries(0)
	end
end

class PageStatModule < StatModule

	def initialize( config_hash, log_manager )
		super( config_hash, log_manager )
		@type = POS_PAGE_NAME
	end

	def generate_stats()
			
		@tmp_array=Array.new
		find_starters.each { |record|
			key = record.info[ @type ]
			n = record
			page_info = PageInfo.new( key )
			while n != nil
				#puts( n.info[ POS_REQ_TIME ] )
				page_info.update( n )
				n = n.next[ @type ]
			end
			@tmp_array.push( page_info )
		}
		update_hash = Hash[ PAGE_ARRAY_KEY => @tmp_array ]
    @result_hash.each_key {|stat_name|
       #puts("STAT NAME: #{stat_name}")
       report = @result_hash[stat_name]
       report.update_stats(update_hash)
    }

		prepare_results()
	end


##	current_array = Array.new
##	@result_hash["PagesByVisits"] = current_array

##	current_array.push( [ "Page Name", "Hits" ])
##	@tmp_array.sort{ |i1,i2| i2.req_count <=> i1.req_count }.each {|page_info|
##		current_array.push( [ page_info.page_name, page_info.req_count ] )
##	}
##	
##	
##end
	
end
	

# ----------------------------------------------------------------------------

class SessionTrackStatModule < StatModule

	def initialize( config_hash, log_manager )
		super( config_hash, log_manager )
		@type = POS_SESSION_ID
	end

	def generate_stats()
		find_starters.each { |record|

			track_string = record.track_string( POS_SESSION_ID, POS_PAGE_NAME )
			
			update_hash = Hash[TRACK_STRING_KEY => track_string, RECORD_KEY => record]
			@result_hash.each_key {|stat_name|
				#puts("STAT NAME: #{stat_name}")
				report = @result_hash[stat_name]
				report.update_stats(update_hash)
			}
		}

		prepare_results()
	end

	
		
end


