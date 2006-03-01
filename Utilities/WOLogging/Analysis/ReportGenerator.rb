#!/usr/bin/env ruby

require 'fileutils'
require 'StatModules'

# REPORT GENERATOR creates output files (like html or text) from data received from Report classes (ReportTypes.rb).
# See README and conf/README for more details.

RT_HTML = "html"

RT_TEXT = "text"
TEXT_SEPARATOR = ":"

class ReportGenerator


	def initialize(stat_module, time, report_number)
		@stat_module = stat_module
		@report_number = report_number
		@time = time
		@output_dir = OUTPUT_DIR+"/"
		begin
			FileUtils.mkdir_p( @output_dir )
		rescue
			puts(" - Unable to create directory '#{@output_dir}'..")
			@output_dir = "output/"
			puts(" + Will save files in '#{@output_dir}'")

		end
	end

	def generate_reports() 
		OUTPUT_FORMATS.each { |type| 
			generate_report( type )
		}
	end
	
	def generate_report( type )
		@stat_module.result_hash.each_key {|key|
			write_to_file( key, type, @stat_module.result_hash[key].output, 1 )
		}
	end

	def write_to_file( stat_type, type, array, mode )
		common_name=@time.strftime( OUTPUT_FILE_NAME )
		file=nil
		extension = type

		final_file_name = "#{common_name}-#{@report_number}-#{stat_type}.#{extension}"
		begin
			if mode==1
				@file=File.new("#{@output_dir}#{final_file_name}",File::CREAT|File::TRUNC|File::RDWR,0644)
			else
				@file=File.open("#{@output_dir}#{final_file_name}",File::RDWR)
			end
		rescue
			puts(" - Unable to write in the directory '#{@output_dir}'! Writing in the 'output/' directory instead.")
			@file=File.new("output/#{final_file_name}",File::CREAT|File::TRUNC|File::RDWR,0644)
		end
		#puts( "ST: #{stats( type, array )}" )
		printf( @file, stats( stat_type, type, array ) )
	end


	def stats( key, type, array )
		result = ""
		if ( type == RT_TEXT ) 
			array.each { |inner_array|
				inner_array.each { |element|
					result = "#{result}#{TEXT_SEPARATOR}#{element}"
				}
				result = "#{result}\n"
			}
		elsif ( type == RT_HTML )
			result = "<HTML><HEAD><LINK REL='stylesheet' TYPE='text/css' HREF='style.css'></HEAD><BODY><SPAN CLASS='large'>#{key}</SPAN><TABLE class='page'>"
			i=0
			array.each { |inner_array|
				if (i==0)
					cssClass="header"
				elsif (i%2==0)
					cssClass="minorEven"
				else
					cssClass="minorOdd"
				end
				result = "#{result}<TR VALIGN=TOP class='#{cssClass}'><TD>"
				i=i+1
				inner_array.each { |element|
					element = element.to_s
					helement = element.gsub(/\n/,"<BR>")
					result = "#{result}</TD><TD>#{helement}"
				}
				result = "#{result}</TD></TR>\n"
			}
			result = "#{result}</TABLE></BODY></HTML>"
		end

		return result
	end

end


