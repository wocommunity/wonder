#!/usr/bin/env ruby

def require_config( filename, *required_args )
	begin
		config_name = filename.gsub( /\.rb$/, "")
		require "#{config_name}"
		conf_ok=true
		required_args.each { |name|
			begin
				Object.const_get(name)
			rescue
				puts(" - '#{name}' is not defined in the config file #{ARGV[0]}.")
				conf_ok=false
			end
		}
		if !conf_ok
			exit
		else
			puts("Configuration loaded successfully")
		end

	rescue
		puts("Unable to load config file.")
		raise
	end

end


