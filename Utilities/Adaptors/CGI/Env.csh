foreach env (`printenv | awk -F= '{ print $1 }'`)
	unsetenv $env
end
setenv HTTP_USER_AGENT "OmniWeb/2.7-beta-2 OWF/1.0"
setenv HTTP_PRAGMA "no-cache"
setenv HTTP_HOST "localhost"
setenv HTTP_ACCEPT "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, image/png, image/tiff, multipart/x-mixed-replace, */*"
setenv HTTP_ACCEPT_LANGUAGE "en"
setenv PATH "/etc:/usr/etc:/usr/ucb:/bin:/usr/bin:/usr/local/bin"
setenv SERVER_SOFTWARE "Apache/1.3.6"
setenv SERVER_NAME "localhost"
setenv SERVER_PORT "80"
setenv REMOTE_HOST "localhost"
setenv REMOTE_ADDR "127.0.0.1"
setenv DOCUMENT_ROOT "/Local/Library/WebServer/Documents"
setenv SERVER_ADMIN "root"
setenv SCRIPT_FILENAME "/Local/Library/WebServer/CGI-Executables/WebObjects"
setenv GATEWAY_INTERFACE "CGI/1.1"
setenv SERVER_PROTOCOL "HTTP/1.0"
setenv REQUEST_METHOD "GET"
setenv QUERY_STRING 
setenv SCRIPT_NAME "/cgi-bin/WebObjects"
setenv PATH_INFO "/HelloWorld"
setenv PATH_TRANSLATED "/Local/Library/WebServer/Documents/HelloWorld"

