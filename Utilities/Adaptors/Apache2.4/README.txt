3 July, 2006

README for Apache2 WebObjects adaptor, hosted by Project WONDER.


Yes, it's true!  You can run the WebObjects adaptor on Apache2.4.x.


HOW TO GET STARTED
1. Get Apache2.4.x.  You may use either source, or precompiled binary.
2. Download the WOAdaptor source.
For CVS access information, please visit http://sourceforge.net/projects/wonder/
CVS will always have the latest patches, so you are encouraged to get the sources via CVS.

3. Set the build variables appropriate to your system's configuration in the file "make.config" and set the adaptors variable to Apache2.4 (see comments in file.)

4. Build the adaptor. (make)

5. Install the adaptor.

cd Apache2.4

$(PATH_TO_APACHE2)/bin/apxs -i -a -n WebObjects 'build product'

e.g.

/usr/local/Apache2.4/bin/apxs -i -a -n WebObjects mod_WebObjects.la

or just 'sudo make install'.

5. Set the correct entries in your Apache2.4.x httpd.conf configuration file.  You'll need at least the following, which should be entered automatically when you use apxs to install the adaptor:

LoadModule WebObjects_module		modules/mod_WebObjects.so

See the sample configuration file for some appropriate settings.

Apache 2.4.x is more restrictive in its default permissions.  You'll need to specifically allow access to the virtual location used by WebObjects.  It may also be necessary to change the name of the WebObjectsAlias setting from /cgi-bin/WebObjects to <foo>/WebObjects or comment out the ScriptAlias definition for the /cgi-bin/ directory.  The following directive is url dependent, so you may need another solution if you also use mod_rewrite to mask WebObjects from the url.

<Location ~ "/Apps/WebObjects">
	Require all granted
</Location>

6. Copy over the WebObjects directory from your previous installation to the new WebObjectsDocumentRoot $(APACHE2_DIR)/htdocs.  If you prefer, a symlink works fine.

7. If you use Monitor to deploy your applications, make sure to set the correct adaptor path for your applications.


IMPORTANT NOTE ON USE WITH SSL

You cannot use an adaptor module build with -DAPACHE_SECURITY_ENABLED if you don't have your server compiled with SSL support.  mod_ssl is now Apache's standard way to do SSL. If you're not using SSL, change the ENABLE_SSL_SUPPORT flag in the Makefile to reflect that fact.

COMPILING FOR APACHE ON WINDOWS

Use the binaries supplied by http://www.apachelounge.com/
axps is not supplied with Windows binaries, but its not really needed.
Makefile and NMakefile have been modified to compile the module without axps.

1. If you prefer MingW (32 bit or 64 bit editing)
   a) configure make.config: ADAPTOR_OS=MINGW, APACHE path etc...
   b) for 64bit Apache, patch %APACHE%/include/apr.h to additionally #include <ws2tcpip.h>
   c) for 32bit and 64bit Apache, patch %APACHE%/include/apr_config.h to NOT #include "ap_config_auto.h"

2. If you use VisualStudio for compiling
   a) open VS command line 
   b) use supplied NMakefile

KNOWN ISSUES

None.  Please let us know if you find bugs.


SUPPORT

If you have questions, suggestion, or problems, you may e-mail me at tcripps@gmail.com.  Please realize that I may not know the answer to your questions, and that I only have a MacOS X machine, so I likely won't be able to answer questions about other platforms.


TODO

Testing!  The adaptor has had minimal testing.

Improve the Makefile for multiple platform support.


THANKS

Apple Computer, Inc. for the original code.
Hideshi Nakase of Senmeisha Inc., who has graciously worked with me and provided valuable assistance and code.
Jonathan Rentzsch for encouragement and inspiration.
Piotr Intres, for fixing a configuration bug.
Project WONDER, for being a home for all things WebObjects.
Travis Cripps, who did this port to Apache2/2.2. :)
All others who have contributed.
You, for using it.


LICENSE
The original code was released under the APSL, and is courtesy of Apple, Computer, Inc.  You can see their copyright statements in the code files.  That license still applies.  Basically, it means you can use the code however you like.


LASTLY -- ALMOST
There is no expressed warranty with this software.  This is not Apple's code.  I have altered their code to work with Apache2.4.x.  If you have problems with the software, if your machine melts down into a pool of silicone and plastic when you install this software, please don't come after me.  And certainly don't get mad at Apple--they did the Right Thingª by opening the code.


HISTORY

27 Sept, 2006 - Updated to work on both 32 and 64-bit architectures.  Most changes are to Adaptor/shmem.c.

3 July, 2006 - Ported to work with Apache 2.2.x.

21 June, 2003 - Enhanced makefile; fixed bugs in configuration parameters, ssl, and building with Apache2's apxs; cleaned up code in general.

3 June, 2001 - Initial version.

