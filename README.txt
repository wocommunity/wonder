Welcome to Project WONDER!
==========================
 
For those unfamiliar with Project WONDER in a nutshell it is the largest WebObjects open 
source project on the net. The goal of Project Wonder is to be an umbrella for open-source 
WebObjects related projects and frameworks.

Listed here is just a short summary, various sub projects also contain a "Documentation" 
folder where you can find more info about the specific projects. 

Common Frameworks & Applications
================================

These frameworks provide the core of Project WONDER. Originally seeded by Netstruxr
they have grown both in number and in size over the last few years.

ERExtensions
------------
Contributed by Netstruxr. One of the two core PW frameworks. Contains Foundation, 
WOF and WOF extensions, amongst them:
- Incredible localization with automatic encoding support for 
  Japanese, Italian, German, Norwegian and French:
- Automatic localization of number and date formatters in strings and text fields. 
  I.e. a german user will see "1.000,00" but an english user will see "1,000.00" 
  with the same formatter "#,##0.00"
- Localized display for relationship components.
- Validation templating support with built in hooks to the localization 
  system.
- Log4j extensions for WO including the ability to control logging 
  behavior at runtime.
- CompilerProxy, adds dynamic recompilations ala WebScript 
  at runtime. Not needed when developing with Eclipse, but useful with PBX/XCode.
- JUnit testing harness integrated with WO with support for dynamic 
  recompilations of tests.
- Lots of EOF additions, nice delegates, custom SQL qualifiers, custom 
  entity classdescriptions.
- WOF better handling of supported languages, i.e. "en-us" turns into 
  the language array ("en-us", "English")
- Javascript and DHTML WO components
- More WO conditional components (ListContainsItem, NonZero, InstanceOf, 
  NonNull, Equal)
- Lots of generic good WO components
- Custom Number and HTML formatters
- Nice wrapper around javax.crypto pacakges
- File changing notification center
- user-agent encapsulating Browser object, very handy for binding up 
  session.browser.isNetscape added by Tatsuya

ERDirectToWeb
-------------
Contributed by Netstruxr. One of the two core PW frameworks. Adds numerous D2W enhancements:
- Validation templating support with built in hooks to the localization system
- Much better rule caching system where significant keys are computed on a per key basis 
  built by Patrice
- Lots of custom assignments for handling all sorts of good things with the rule system
- Delayed assignments that resolve every time, instead of being cached.
- Added more templates, better piece components for building templates
- Integrated localization support out of the box
- Tons of property level components
- Added support for embedding configurations within each other
- Significantly improved validation handling
- Lots of handy NextPageDelegates
- Direct action D2W navigation support

ERNeutralLook
-------------
Contributed by Patrice Gautier and Max Muller. Re-implementation of the stock 
D2W NeutralLook using ERD2W, which means you get all the enhanced localization 
and validation features for free. Has Wizard for object creation, better Tab page
better Edit to-many page.

ERCoreBusinessLogic
-------------------
Contributed by Netstruxr. Meant as a collection point for business logic related objects.
Has common User logic for preferences in D2W apps, exception page, EOF based mail sending.

ERJavaMail
----------
Contributed by Camille Troillard. Provides more robust mail support than the built in 
WOMailDelivery. Handles validation of email, support for file attachments, native 
support for Japanese formated email content, mailing queuing and retry and much, much more.

ERChangeNotificationJMS
-----------------------
Contributed by Tatsuya Kawano. Adaptation of David Neuman's change notification 
framework except instead of having every WOApplication broadcast to every other 
WOApplication using a WOHTTPConnection this implementation uses a JMS publish/subscribe 
system. This version uses Exolab's OpenJMS implementation although it should be fairly 
straight forward to adapt to any other JMS provider.

ERPrototypes
------------
Contributed by Anjo Krank. Used by ERCoreBusinessLogic, it contains a set of prototypes
for various databases.

ERPlot
------------
Contributed by Anjo Krank. Simple charting frameworks that uses JFreeChart. Currently
contains bar and pie chart.

WOOgnl
------
Contributed by Max Muller. Integrates OGNL (Object Graph Navigation Language) 
syntax into WO Component bindings. Like Key-Value-Coding on steroids. 
Find more information about OGNL at
   http://www.ognl.org.

JavaWOExtensions
----------------
Originally by Apple and adapted by Anjo Krank. Meant as a collection point for 
bug fixes and localization of Apple's JavaWOExtension framework. This should be a 
drop-in replacement for the original JavaWOExtensions.

ERJars
------
Framework wrapper around log4j 1.2.8 and junit 3.7

ERCalendar
----------
Contributed by Johan Carlberg. Provides an elegant framework for 
delivering dynamic calendars that can be used by iCal or any other application 
that understands the .ics format. Fully JavaDoc'd.

ERMailer
--------
Contributed by Max Muller. Simple application that sits on top of 
ERCoreBusinessLogic that can deliver email saved into a database. 
Uses ERJavaMail for the actual sending of email.

ERWorkerChannel
---------------
Contributed by Tatsuya Kawano. A worker channel is a way of spinning 
off a set of work to be done by another thread or another application. 
Fully queued and multithreaded.


Adaptors & Plugins
==================

Adaptors
--------
Thanks to the effort of Jonathan 'Wolf' Rentzsch, Project Wonder is now hosting a 
mirror of Apple's open sourced Adaptors. The goal of this part of the project is 
to stay in synch with Apple's version plus providing pre-built adaptor binaries 
for platforms not officially supported by Apple (like RH Linux and OpenBSD). We 
also are looking at incorporating in patches that others have made to an adaptor 
to either correct an issue or improve the performance. Currently three issues have 
been corrected as well as adding support for Linux. See the full explanation here 
and volunteer to submit a build: 
 http://wonder.sourceforge.net/WOAdaptor.html
Contains several enhancements, amongst them the ability to send other headers than
GET, HEAD and POST to the application.

FastCGI Adaptor 
---------------
Contributed by Wojtek. WebObjects adaptor that speaks FastCGI. From his initial release notes: Dont be misguided by the 'CGI' acronym - FastCGI is ultra fast, and ultra cool. The basic idea is to multiplex everything (requests, responses, error info, management records) between the http server and web application over one bidirectional communication channel, socket to be exact. Check it out at http://www.fastcgi.com!

SAPDB_PlugIn
------------ 
Contributed by Wojtek. EOModeler database plugin for use with SAPDB. 
Uses the native SAPDB libraries to provide support within EOModeler for 
SQL generation and column types.


Applications
============

BugTracker
----------
Contributed by Netstruxr and adapted by Anjo Krank. 
BugTracking application originally adapted from an example shipped by Apple with 
WO 4.5. Contains a useful system for tracking bugs and requests. Shows of a number
of advanced ERD2W features.

Examples
--------
Contributed by Max Muller. ERMovies, Wrox and Stepwise examples show of a number of 
advanced ERExtensions and ERD2W features.

Vacation
--------
Contributed by Ash Mishra. Nice app to track your users holidays.


Subprojects
===========

DevStudio
---------
Contributed by Anjo Krank. Provides a Web-based development studio. 
Web-based applications include graphical D2W rule inspector, JavaBrowser 
with source code/decompile option, and EOModeler inspection. Should be considered
highly experimental.

DynaReporting
-------------
Originally written by David Neumann, adapted and contributed by Anjo Krank.
DynaReporting is a powerful reporting framework developed by David to specifically 
address the issue of reporting on groups of records within WebObjects. 
For more information see the DynaReporting examples as well as David's post 
talking about DynaReporting here: 
  http://sourceforge.net/mailarchive/message.php?msg_id=3894801
Consists of DRGrouping for the grouping, WRReporting for the HTML output and two
example applications. Needs WOOgnl to provide highly advanced grouping and display.

ExcelGeneration
---------------
Contributed by Anjo Krank. Uses POI from apache to transform HTML tables into
Excel speadsheets. Complete support of styles, formulas, sheets etc.

PayPal
------
Donated by Travis Cripps, provides a nice set of WO components and delegates for 
adding PayPal support to a WebObjects application.

SVGObjects
----------
Contributed by Ravi Mendis. SVGObjects provides a bunch of cool components 
that can be used to create dynamic SVG content with WebObjects. Very mature 
framework with several well done examples. The framework is also featured 
in Ravi's book: WebObjects Developers Guide.

Validity
--------
Donated by GammaStream technologies (www.gammastream.com). 
Validity is a framework (Validity) and a WebObjects application (Validity Modeler) 
which combine to provide a powerful data validation engine and validation 
rule modeler. Using an EOModel as a reference point, one may assign rules 
to the various attributes of the entities. One may choose from any of Validity's 
predefined rules, or write his/her own rules in Java. The rules are 
stored in the Validity model file (Validity.model) inside the 
corresponding '.eomodeld' wrapper.

WOWebLog
--------
Contributed by Jonathan 'Wolf' Rentzsch. A bookmarkable stateless web logging 
application with a D2W admin backend. Contains lots of goodies, find out 
more info about the application and associated frameworks here: 
  http://rentzsch.com/woWebLog 
(don't forget to check out the slides!).
