#!/usr/bin/env ruby

class Statistics
	attr_accessor :id, :stat_type, :stat_title, :options
	def initialize( value )
		@id,@stat_type,@stat_title,@options = value.split(/,/)
	end

	
end

