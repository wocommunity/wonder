1 June, 2003

README for Apache2 WebObjects adaptor, hosted by Project WONDER.


Yes, it's true!  You can run the WebObjects adaptor on Apache2.


HOW TO GET STARTED
1. Get Apache2.  You may use either source, or precompiled binary.  If you'd prefer the binary, a good distribution may be found at Server Logistics, http://www.serverlogistics.com/apache2.php
2. Download the WOAdaptor source.  There are 2 ways: CVS or http.
For CVS access information, please visit http://sourceforge.net/projects/wonder/
CVS will always have the latest patches, so you are encouraged to get the sources via CVS
To download the source file via http, please visit:
http://homepage.mac.com/tcripps/software.html

3. Set the build variables appropriate to your system's configuration in the file "make.config" and set the adaptors variable to Apache2 (see comments in file.)

4. Build the adaptor.

5. Install the adaptor.  You may use the 'make install' command or manually enter the command:

$(PATH_TO_APACHE2)/bin/apxs -i -a -n WebObjects 'build product'

e.g.

/Library/Apache2/bin/apxs -i -a -n WebObjects mod_WebObjects.la

5. Set the correct entries in your Apache2 httpd.conf configuration file.  You'll need at least the following, which should be entered automatically when you use apxs to install the adaptor:

LoadModule WebObjects_module		modules/mod_WebObjects.so

See the sample configuration file for some appropriate settings.

6. Copy over the WebObjects directory from your previous installation to the new WebObjectsDocumentRoot $(APACHE2_DIR)/htdocs.

7. If you use Monitor to deploy your applications, make sure to set the correct adaptor path for your applications.


KNOWN ISSUES

Currently, there is preliminary support.  It compiles and installs fine on OS X with one caveat: SSL support.  If you enable SSL, the module will build just fine and dandy.  It will even install itself without complaining.  But when you run the apachectl configtest, it fails with the message:

Syntax error on line xxx of $(YourApache2Dir)/conf/httpd.conf:
Cannot load $(YourApache2Dir)/modules/mod_WebObjects.so into server: dyld: $(YourApache2Dir)/bin/httpd Undefined symbols:
_ssl_module


SUPPORT

If you have questions, suggestion, or problems, you may e-mail me at tcripps@mac.com.  Please realize that I may not know the answer to your questions, and that I only have a MacOS X machine, so I likely won't be able to answer questions about other platforms.


TODO

#1 on the list is to get the SSL bug fixed as soon as possible.  I'd really appreciate any help you can offer!

Testing!  The adaptor has had minimal testing.  That's not to say it won't work, but it definitely needs some testing before it's ready for major deployment.

Improve the Makefile.  If any libtool/gcc/Makefile experts would like to help, it would be welcome.


THANKS

Apple Computer, Inc. for the original code.
Senmeisha Inc., whose port came out about the same time, whose Linux build script I've borrowed, and who gave me the last piece of the puzzle for getting the project to compile with mod_ssl support. (Note the extant issues above.)
Jonathan Rentzsch for encouragement and inspiration.
Project WONDER, for being a home for all things WebObjects.
Homer and Bart Simpson, whose antics kept me sane while working on the port.
Travis Cripps, who did this port to Apache2. :)
You, for using it.


LICENSE
The original code was released under the APSL, and is courtesy of Apple, Computer, Inc.  You can see their copyright statements in the code files.  That license still applies.  Basically, it means you can use the code however you like.


LASTLY
There is no expressed warranty with this software.  This is not Apple's code.  I have altered their code to work with Apache2.  If you have problems with the software, if your machine melts down into a pool of silicone and plastic when you install this software, please don't come after me.  And certainly don't get mad at Apple--they did the Right Thingª by opening the code.

