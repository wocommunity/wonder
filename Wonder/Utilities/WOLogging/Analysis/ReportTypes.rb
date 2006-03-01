#!/usr/bin/env ruby

require 'Recording'

# REPORT TYPES is the class hierarchy of implemented report types.
# Base class is Report. All other reports should inherit from one of its subclasses.
# See README and conf/README for details.

PAGE_STATS = "PageStats"
SESSION_TRACK_STATS = "SessionTrackStats"
STAT_TYPE = "StatType"

class Report

	# superclass for all reports
	
	attr_accessor :result_hash, :output
	def initialize(config_hash)
		@config_hash = config_hash
		@result_hash = Hash.new
		@output = Array.new
		@treshold = 1 #customize in subclasses, treshold for displaying multiple occurances.
                  #if an event occered more than once (treshold=1) the event will be displayed
                  #if an event occured just once, it won't.
	end

	def update_stats(object)
	end

	def generate_output()
	end
end

###############################################################################
####################### PageReport and its subclasses #########################
###############################################################################

class PageReport < Report 

	# superclass for all page-info-related reports.
	
	def initialize( config_hash )
		super( config_hash )
		@type = POS_PAGE_NAME
	end

	def update_stats(update_hash)
		@tmp_array = update_hash[PAGE_ARRAY_KEY]
	end
end

class AvgReqTimeReport < PageReport

	# calculates average request time per page. 
	# Apache log can contain request times in seconds or microseconds. 
	# The tool assumes which one is the case based on the average value. 
	# Microseconds are recommenede for greater accuracy.
	#
	# Additionally this report lists the worst requests (slowest response)
	# and if there was any custom info given (like customer id or email address or validation message)
	# this info is listed together with the request time.
	# This allows to locate problematic pages. 
	# If the problem (long response) occurs for a certain group of user ids, it can be easily reproduced.
	# 
	
	def generate_output
		current_array = @output
	  current_array.push( [ "Page Name", "Count", "Total Req.Time [s]", "Average Req.Time [s]", "Notes" ] )
    @tmp_array.sort { |i1,i2| i2.avg_req_time <=> i1.avg_req_time }.each { |page_info|
    current_array.push( [ page_info.page_name, page_info.req_count, page_info.total_req_time, page_info.avg_req_time, page_info.record_dictionaries(15) ] )
    }
	end

end

class PageVisitsReport < PageReport

	# very simple. Calculates number of total requests to respective pages.
	
		def generate_output
			current_array = @output
		  current_array.push( [ "Page Name", "Hits" ])
			@tmp_array.sort{ |i1,i2| i2.req_count <=> i1.req_count }.each {|page_info|
				current_array.push( [ page_info.page_name, page_info.req_count ] )
			}
		end
end


###############################################################################
####################### SessionTrackReport and its subclasses #################
###############################################################################

class SessionTrackReport < Report

	# superclass for all session-tracking-related reports.
	
	def initialize( config_hash )
		super( config_hash )
		@type = POS_SESSION_ID
	end

	def read_session_track_hash(update_hash)
    @track_string = update_hash[TRACK_STRING_KEY]
    @record       = update_hash[RECORD_KEY]
		if ( @track_string == nil )
			puts(" ! invalid input dictionary for SessionTrackReport, missing TRACK_STRING_KEY.")
			return
		end
		if ( @record == nil )
			puts(" ! invalid input dictionary for SessionTrackReport, missing RECORD.")
			return
		end
	end
end

TRACK_STRING_KEY="TrackString"
RECORD_KEY="Record"
PAGE_ARRAY_KEY="PageArray"

# session types
ST_VALIDATION_FAILURE = "[  fail ]"
ST_VALIDATION_SUCCESS = "[success]"
ST_VALIDATION_IRRELEVANT = "[ ----- ]"

class IndividualTracksReport < SessionTrackReport

	# tracks individual sessions (records paths that users follow through the application)
	# e.g. Session N entered page "Main", then page "LoginPage", then "LoginPage again, then proceeded to "ConfirmPage"
	# will be recorded as Main->LoginPage(2)->ConfirmPage
	# The number of occurences of identical paths will be groupped by displayed.
	# e.g. the following entries
	# Main -> LoginPage                   | 2011
	# Main -> LoginPage(2) -> ExitPage    | 520
	# mean that 2011 users went from "Main" to "LoginPage", whereas 520 users followed the other path and left on ExitPage.
  #
	# This statistics is useful for detecting UI bottlenecks (pages troublesome for users) and common paths.
	
	def update_stats(update_hash)
		
		read_session_track_hash(update_hash)
		
      if ( @result_hash[ @track_string ] != nil )
        @result_hash[ @track_string ][0] = @result_hash[ @track_string ][0]+ 1
        @result_hash[ @track_string ][1].push( @record.info[POS_SESSION_ID] )
      else
        @result_hash[ @track_string ] = Array [ 1, Array[ @record.info[POS_SESSION_ID] ] ]
      end

	end

	def generate_output
		current_array = @output
    current_array.push( ["Path through the application","Occurances","Session id's"] )
    @result_hash.keys.sort{ |key1,key2| @result_hash[key2] <=> @result_hash[key1] }.each { |track_string|
          if @result_hash[ track_string ][0]>@treshold
              session_list = "#{@result_hash[ track_string][1].join(", ")[0..105]} .."
              current_array.push( [ track_string , @result_hash[track_string][0], session_list ] )
          end
    }
	end
end


CV_CONVERSION_PAGES = "ConversionPages"

class ConversionReport < SessionTrackReport

	# This report is useful for eCommerce sites and various registration forms.
	# It says what percentage of customers managed to reach a certain goal, 
	# e.g. what percentage of customers who entered web store eventually managed to place an order.
	# It also gives intermediate info on other pages.
	#
	# Therefore this statistics requires an additional parameter (parameter name is: CV_CONVERSION_PAGE).
	# As a value you enter page names as an array chronologically.
	# e.g.
	# CV_CONVERSION_PAGES => ["StartPage", "ShippingInfoPage", "PaymentMethodPage", "ReceiptPage"]
	#
	# The report will then provide intermediate info
	# e.g.
	# StartPage         | 100%  100%
	# ShippingInfoPage  |  50%   50%
	# ConfirmationPage  |  50%   25%
	# ReceiptPage       |  50%   12.5%
	#
	# The first percentage (local conversion rate) is relative percentage to the preceeding page, 
	# the second percentage (global conversion rate) is relative to the start page.
	# e.g. ConfirmationPage 50% 25% means that only 50% of the ShippingInfoPage visitors proceed to ConfirmationPage
	# and this equals 25% of total StartPage visitors.
	
	def initialize( config_hash )
		super( config_hash )
		@conv_pages = config_hash[CV_CONVERSION_PAGES]
	end

	def update_stats(update_hash)
		read_session_track_hash(update_hash)
        n = @record
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
              if @result_hash[milestone_page]!=nil
                #puts("incrementing for page #{milestone_page}: #{@conv_hash[milestone_page]}")
                @result_hash[milestone_page]=@result_hash[milestone_page]+1
              else
                #puts("putting 1 for page #{milestone_page}")
                @result_hash[milestone_page]=1
              end
              i=i+1
            end   
          end
          n = n.next[ @type ]
        end
	
	end

	def generate_output
		current_array = @output
      prev_value = nil
      first_value = nil
      current_array.push( [ "Page name", "Occurances", "LCR", "GCR" ] )
      @result_hash.keys.sort { |k1,k2| @result_hash[k2] <=> @result_hash[k1] }.each { |page|
        value = @result_hash[page]
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
	end
end


VL_VALIDATION_KEY = "ValidationKey"
VL_VALIDATION_PAGE = "ValidationPage"
VL_VALIDATION_SUCCESS_PAGE = "ValidationSuccessPage"

class ValidationReport < SessionTrackReport

  # Helps gather useful information from error messages and alike.
	# 
	# e.g.
	# We have a registration page called "RegistrationPage" ;-).
	# Then we have a page where users proceed after successfully completing registration,
	# let's say "RegistrationSuccessPage".
	#
	# Some people fail to accomplish registration due to annoying validation messages, like
	# "password too simple", "login already taken" etc.
	#
	# This report shows which error messages are the most harmful for the registration process.
	#
	# e.g.
	# message                      | success | fail | total | conversion |
	# --------------------------------------------------------------------
	# "login already taken"        | 10      | 5    | 15    | 66%        |
	# "password too simple"        | 1       | 9    | 10    | 10%        |
	#
	# For "login already taken" it means that out of 15 people who had this error message appear
	# on the RegistrationPage, 10 managed to fix the problem and proceeded to RegistrationSuccessPage.
	# Conversion is 10/15 (66%).
	# For "password too simple" conversion is 10% which means that his error message is a problem
	# for most customers.
	#
	# Validation report requires the following additional attributes:
	# VL_VALIDATION_KEY: key representing validation messages in the custom attributes dictionary.
	# e.g. if your log entries look like this:
	# "AppName;sessionid;RegistrationPage;errorMessage=password too simple;"
	# your VL_VALIDATION_KEY should be errorMessage.
	#
	# VL_VALIDATION_PAGE: page name where the relevant message occurs (in this case RegistrationPage)
	# 
	# VL_VALIDATION_SUCCESS_PAGE: page name which means that the users successfully overcame the problem 
	# (in this case RegistrationSuccessPage).
	#
	# 


	
	
	def initialize( config_hash )
		super( config_hash )
		@validation_message = config_hash[VL_VALIDATION_KEY]
		@validation_source_page = config_hash[VL_VALIDATION_PAGE]	
		@validation_destination_page = config_hash[VL_VALIDATION_SUCCESS_PAGE]
	end

	def update_stats(update_hash)

		read_session_track_hash(update_hash)
		
    #puts( "'#{@track_string}'" )
		#puts( "#{@validation_source_page}, #{@validation_destination_page}")
    src_page_index =  @track_string.rindex( " #{@validation_source_page} ")
    dst_page_index =  @track_string.rindex( " #{@validation_destination_page} ")
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
          n = @record
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
                  # puts( "VT:#{validation_text}" )
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
            if !@result_hash.has_key?( messages_text )
              #puts("new entry: #{messages_text}")
              tuple = Tuple.new(value,1)
              @result_hash[messages_text]=tuple
            else
              #puts("existing entry #{messages_text}")
              tuple = @result_hash[messages_text]

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

	def generate_output
      current_array = @output
      current_array.push( ["Validation messages", "Success", "Fail", "Total", "Conversion"] )
      current_array.push( ["Message name is: #{@validation_message}\nMessage occurs on page: #{@validation_source_page}\nSuccess means that user reached page: #{@validation_destination_page}","","","",""])
      @result_hash.keys.sort{ |k2,k1| (100*@result_hash[k2].i1)/@result_hash[k2].i2 <=> (100*@result_hash[k1].i1)/@result_hash[k1].i2  }.each {|k|
        i1 = @result_hash[k].i1
        i2 = @result_hash[k].i2
        percentage = (100*i1)/i2
        current_array.push( [k, i1, i2-i1, i2, "#{percentage}%%" ])
      }
      current_array.push(["This statistics says that a customer encountered a specific validation message or any other message on his path through the application. This particular message '#{@validation_message}' occurs on page named '#{@validation_source_page}' and if user overcomes the validation problem and reaches page '#{@validation_destination_page}' it is recorded as a Success. Otherwise it's recorded as Failure. If the same user encounters multiple, different validation messages then they are all listed (so this statistics is groupped by encountered message group and not by message).","","","",""])
	end
end


# this object simply wraps two numbers together and allows to keep them as a single entry in a dictionary.
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
