21 June, 2003

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


IMPORTANT NOTE ON USE WITH SSL

You cannot use an adaptor module build with -DAPACHE_SECURITY_ENABLED if you don't have your server set up to use SSL.  You must use apachectl -D SSL -k start or httpd -D SSL if you use the default httpd.conf for Apache2.  If you don't, the webserver won't run, complaining about an unresolved alias _ssl_module in mod_WebObjects.so.  The solution is simple.  If you're not using SSL, change the ENABLE_SSL_SUPPORT flag in the Makefile to reflect that fact.


KNOWN ISSUES

None.  Please let us know if you find bugs.


SUPPORT

If you have questions, suggestion, or problems, you may e-mail me at tcripps@mac.com.  Please realize that I may not know the answer to your questions, and that I only have a MacOS X machine, so I likely won't be able to answer questions about other platforms.


TODO

Testing!  The adaptor has had minimal testing.

Improve the Makefile for multiple platform support.


THANKS

Apple Computer, Inc. for the original code.
Hideshi Nakase of Senmeisha Inc., who has graciously worked with me and provided valuable assistance and code.
Jonathan Rentzsch for encouragement and inspiration.
Piotr Intres, for fixing a configuration bug.
Project WONDER, for being a home for all things WebObjects.
Travis Cripps, who did this port to Apache2. :)
All others who have contributed.
You, for using it.


LICENSE
The original code was released under the APSL, and is courtesy of Apple, Computer, Inc.  You can see their copyright statements in the code files.  That license still applies.  Basically, it means you can use the code however you like.


LASTLY -- ALMOST
There is no expressed warranty with this software.  This is not Apple's code.  I have altered their code to work with Apache2.  If you have problems with the software, if your machine melts down into a pool of silicone and plastic when you install this software, please don't come after me.  And certainly don't get mad at Apple--they did the Right Thingª by opening the code.


HISTORY

21 June, 2003 - Enhanced makefile; fixed bugs in configuration parameters, ssl, and building with Apache2's apxs; cleaned up code in general.

3 June, 2001 - Initial version.

