#!/usr/bin/env ruby

def require_config( filename, *required_args )
	begin
		require filename
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


