
Welcome to the ER Frameworks!

Introduction
	
Requirements
	These frameworks work with WebObjects 5.x.

The Frameworks and Dependencies


ERCalendar - This framework contains a WOComponent for dynamically generated iCalendar documents. ERCalendar depends only on the standard WebObjects and Java frameworks.

The response created by ERPublishCalendarPage is an iCalendar document (.ics) containing the events added to ERPublishCalendarPage by the application (see addEvent). An iCalendar-aware application, such as Apple's iCal, can subscribe to such a calendar, provided that the page has a fixed URL (either is the "Main" page, or a direct action serves the page).

Events added to a ERPublishCalendarPage is objects of any class that implements the ERCalendarEvent interface. Existing classes (for example EOCustomObject subclasses), that correspond to calendar events, can easily be modified to implement ERCalendarEvent and thus be added directly to ERPublishCalendarPage. If existing classes does not directly correspond to calendar events, create events from business data (or some algorithm) using either the included ERSimpleEvent class, a subclass of ERSimpleEvent, or any other class implementing the ERCalendarEvent interface.


Installation Instructions

Everything should be pretty staight forward.  Just be aware of the above dependencies.  The only action that needs to be taken before running an application with the frameworks linked in is to set one default:  ERConfigurationPath to point to a Java properties configuration file.  This is the file that is used to initialize the log4j system.  A sample configuration has been included in the file SampleConfiguration.config.

Installing Jikes

    Windows

    1) Copy jikesSpec.plist to $(NEXT_ROOT)/Developer/Makefiles/Resources.
    2) (Windows) Copy jikes.bat to $(NEXT_ROOT)/Local/Library/Executables.
    3) Download and unpack Jikes from IBM.
         http://www10.software.ibm.com/developerworks/opensource/jikes/
    4) Copy jikes.exe to $(NEXT_ROOT)/Local/Library/Executables.
    5) Make sure X:\Apple\Local\Library\Executables is in your PATH.
         (Where X is the drive WebObjects is installed on)
    
    Mac OS X Server
    
    1) Follow the instructions on
         http://www.xanthippe.ping.de/jikes/
         
    *** To use Jikes, select IBM Jikes as your compiler in the Project Inspector.
         
    Mac OS X
    
    Mac OS X includes jikes in the Developer Tools installation CD.  You should be all set if you already have WebObjects installed.



Known Issues

We would definitely like to see other people contributing to these frameworks, please let us know of any way we can help.  Most of all, please send us feedback about what you like, don't like, what you want to add/remove, suggestions, movies you have seen recently ....
  
Contacting Us:
website: http://wonder.sourceforge.net
email: wonder-disc@lists.sourceforge.net




