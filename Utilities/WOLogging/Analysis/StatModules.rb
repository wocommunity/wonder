#!/usr/bin/env ruby


class StatModule

	attr_accessor :result_hash, :title
	
	def initialize( title, args, log_manager )
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

	def initialize( title, args, log_manager )
		super( title, args, log_manager )
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

	# session types
	ST_VALIDATION_FAILURE = "[  fail ]"
	ST_VALIDATION_SUCCESS = "[success]"
	ST_VALIDATION_IRRELEVANT = "[ ----- ]"
	
	def initialize( title, args, log_manager )
		super( title, args, log_manager )
		@type = POS_SESSION_ID
		@tmp_hash = Hash.new
		@conv_hash = Hash.new
		@valid_hash = Hash.new
		@treshold = 1
		@conv_pages = nil
		if args!=nil && args.length>0	
			@conv_pages = args[0].split(/,/)
		end
		if args!=nil && args.length>2
			@validation_source_page = args[1].split(/,/)[0]
			@validation_destination_page = args[1].split(/,/)[1]
			@validation_message = args[2]
			puts("   - Got validation parameters: #{@validation_source_page}, #{@validation_destination_page}, #{@validation_message}")
			
		end
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
     
			# validation statistics
			if @validation_message
				#puts( "'#{track_string}'" )
				src_page_index =  track_string.rindex( " #{@validation_source_page} ")
				dst_page_index =  track_string.rindex( " #{@validation_destination_page} ")
				#puts( src_page_index )
				#puts( dst_page_index )

				type = ST_VALIDATION_IRRELEVANT
				
				if src_page_index && dst_page_index
					#puts("SUCCESS")
					type = ST_VALIDATION_SUCCESS
				elsif src_page_index
					#puts("FAILURE")
					type = ST_VALIDATION_FAILURE
				else
					#puts("irrelevant")
				end
			
				if type!=ST_VALIDATION_IRRELEVANT

					#if we know that this use path contains relevant pages (validation and validation destination page)
					#we collect info on validation messages that emerged in an array, sort them and
					#store in hash along with success/total ratio.
					#this way we know that e.g.
					# for messages 'Invalid password' success ratio is 5/9
					# for messages 'Invalid password' and 'Wrong user' success ratio is 7/8
					# etc.
					#puts("processing..")
					n = record
					messages = Array.new
					while n!=nil
						current_page = n.info[POS_PAGE_NAME]
						#puts("   #{current_page}")
						if current_page == @validation_source_page 
							#puts("   BINGO")
							#if message not added to array, add it
							if n.info[POS_ARBITRARY_ARGS]
								validation_text = n.info[POS_ARBITRARY_ARGS][@validation_message] 					
								if validation_text && !messages.include?(validation_text)	
									messages.push( validation_text )
									#puts( "VT:#{validation_text}" )
									#if validation_text.rindex("Pole")
									#	puts( "VT:#{validation_text}" )
									#end
								end
							end
						end #if on the validation src page
					  n = n.next[ @type ]	
					end # while
				
					messages.sort!{|m1,m2| m1<=>m2}
					messages_text=messages.join("\n")
					#puts("MESSAGES_TEXT: '#{messages_text}'")
					if messages_text.length>0
						#puts("..")
						#value to add depends on whether user succeeded to reach dest.page
						#(whether we will or not increase the success counter)
						value=0
						if type==ST_VALIDATION_SUCCESS
							value=1
						end
						if !@valid_hash.has_key?( messages_text )
							#puts("new entry: #{messages_text}")
							tuple = Tuple.new(value,1)
							@valid_hash[messages_text]=tuple
						else
							#puts("existing entry #{messages_text}")
							tuple = @valid_hash[messages_text]
						
							
							#puts("Track string: #{track_string} #{type}")
							#puts("Messages: #{messages_text}")
							#puts("Befor:#{tuple}")
							tuple.i1 = tuple.i1+value
							tuple.i2 = tuple.i2+1
							#puts("After:#{tuple}")
						end
					end #if there were validation messages
				end # relevant
		
				
			end
			

			
			# conversion statistics
			if @conv_pages
				n = record
				i = 0
				milestone_page = nil
				prev_page = nil
				current_page = nil
				while n != nil && i < @conv_pages.length
				
					prev_page = current_page
					milestone_page = @conv_pages[i]
					current_page = n.info[POS_PAGE_NAME]
					#puts("* #{current_page}<=>#{milestone_page}")
					if current_page == milestone_page
						#puts("   * prev: #{prev_page}, #{current_page}")
						if (prev_page != current_page)
							if @conv_hash[milestone_page]!=nil
								#puts("incrementing for page #{milestone_page}: #{@conv_hash[milestone_page]}")
								@conv_hash[milestone_page]=@conv_hash[milestone_page]+1
							else
								#puts("putting 1 for page #{milestone_page}")
								@conv_hash[milestone_page]=1
							end
							i=i+1
						end		
					end
					n = n.next[ @type ]
			  end
			end # if @conv_pages available
		}
		#puts("HASH SIZE: #{@valid_hash.size}")
		@valid_hash.each_key {|key|
			#puts("KEY: #{key}")
			#puts("VALUE: #{@valid_hash[key]}")
			#puts("=====")
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

		if @validation_message
			current_array = Array.new
			@result_hash["Validation"] = current_array
			current_array.push( ["Validation messages", "Success", "Fail", "Total", "Conversion"] )
			current_array.push( ["Message name is: #{@validation_message}\nMessage occurs on page: #{@validation_source_page}\nSuccess means that user reached page: #{@validation_destination_page}","","","",""])
			@valid_hash.keys.sort{ |k2,k1| (100*@valid_hash[k2].i1)/@valid_hash[k2].i2 <=> (100*@valid_hash[k1].i1)/@valid_hash[k1].i2  }.each {|k|
				i1 = @valid_hash[k].i1
				i2 = @valid_hash[k].i2
				percentage = (100*i1)/i2
				current_array.push( [k, i1, i2-i1, i2, "#{percentage}%%" ])
			}
			current_array.push(["This statistics says that a customer encountered a specific validation message or any other message on his path through the application. This particular message '#{@validation_message}' occurs on page named '#{@validation_source_page}' and if user overcomes the validation problem and reaches page '#{@validation_destination_page}' it is recorded as a Success. Otherwise it's recorded as Failure. If the same user encounters multiple, different validation messages then they are all listed (so this statistics is groupped by encountered message group and not by message).","","","",""])


			
		end

		
		if @conv_pages
			current_array = Array.new
			@result_hash["Conversion"] = current_array
			prev_value = nil
			first_value = nil
			current_array.push( [ "Page name", "Occurances", "LCR", "GCR" ] )
			@conv_hash.keys.sort { |k1,k2| @conv_hash[k2] <=> @conv_hash[k1] }.each { |page|
				value = @conv_hash[page]
				lcr ="--"
				if prev_value
					lcr = (value.to_f / prev_value.to_f * 1000).to_i / 10 
				end
				gcr = "--"
				if first_value
					gcr = (value.to_f / first_value.to_f * 1000).to_i / 10
				end
				current_array.push( [ page, value, "#{lcr} %%", "#{gcr} %%" ] )
				prev_value = value
				if first_value==nil
					first_value = value
				end
			}
			current_array.push( [ "Conversion statistics means that out of the number of users who reached page number 1 on the list,\n a certain percentage of users managed to reach respective pages.\nThe second percentage is relative to the first number (first page on the list).\nThe first percentage is relative the previous (N-1) page on the list.","","",""])
		end # if conv_pages available		
	end
	
		
end

class Tuple 
	attr_accessor :i1, :i2

	def initialize(i1, i2)
		@i1 = i1;
		@i2 = i2;
	end

	def to_s
		return "#{@i1}/#{@i2}"
	end
	
end


