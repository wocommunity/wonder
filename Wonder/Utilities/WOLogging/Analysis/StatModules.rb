#!/usr/bin/env ruby


class StatModule

	attr_accessor :result_hash, :title
	
	def initialize( title, log_manager )
		puts(" + Initializing stat module: "+self.class.to_s)
		@title = title
		@cache_machine = log_manager.cache_machine
		@log_record_array = log_manager.log_record_array
		@type = nil
		@result_hash = Hash.new
	end

	def generate_stats()
	end
		
	def find_starters()
		@log_record_array.find_all { |rec| rec.prev[@type] == nil }
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

	def initialize( title, log_manager )
		super( title, log_manager )
		@type = POS_PAGE_NAME
		@treshold = 1
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
		
		prepare_results()
	end

	def prepare_results()

		current_array = Array.new
		@result_hash["AvgReqTime"] = current_array
		
		current_array.push( [ "Page Name", "Count", "Total Req.Time [s]", "Average Req.Time [s]", "Notes" ] )
		@tmp_array.sort { |i1,i2| i2.avg_req_time <=> i1.avg_req_time }.each { |page_info| 
			current_array.push( [ page_info.page_name, page_info.req_count, page_info.total_req_time, page_info.avg_req_time, page_info.record_dictionaries(15) ] )
		}

		current_array = Array.new
		@result_hash["PagesByVisits"] = current_array

		current_array.push( [ "Page Name", "Hits" ])
		@tmp_array.sort{ |i1,i2| i2.req_count <=> i1.req_count }.each {|page_info|
			current_array.push( [ page_info.page_name, page_info.req_count ] )
		}
		
		
	end
	
end
	

# ----------------------------------------------------------------------------

class SessionTrackStatModule < StatModule
	def initialize( title, log_manager )
		super( title, log_manager )
		@type = POS_SESSION_ID
		@tmp_hash = Hash.new
		@treshold = 1
	end

	def generate_stats()
		find_starters.each { |record|
			#puts( record.as_string)
			track_string = record.track_string( POS_SESSION_ID, POS_PAGE_NAME )
			if ( @tmp_hash[ track_string ] != nil )
				@tmp_hash[ track_string ][0] = @tmp_hash[ track_string ][0]+ 1
				@tmp_hash[ track_string ][1].push( record.info[POS_SESSION_ID] )
			else
				@tmp_hash[ track_string ] = Array [ 1, Array[ record.info[POS_SESSION_ID] ] ]
			end
		}

		prepare_results()
	end

	def prepare_results()
		current_array = Array.new
		@result_hash["SessionTrack"] = current_array
		
		current_array.push( ["Path through the application","Occurances","Session id's"] )
		@tmp_hash.keys.sort{ |key1,key2| @tmp_hash[key2] <=> @tmp_hash[key1] }.each { |track_string|
					if @tmp_hash[ track_string ][0]>@treshold
							session_list = "#{@tmp_hash[ track_string][1].join(", ")[0..105]} .."
							current_array.push( [ track_string , @tmp_hash[track_string][0], session_list ] )
					end
		}
	end
	
		
end




