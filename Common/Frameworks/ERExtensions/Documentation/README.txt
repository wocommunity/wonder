
Welcome to the ER Frameworks!

Introduction
	
Requirements
	These frameworks work with WebObjects 5.x.

The Frameworks and Dependencies


ERExtensions - This framework is a great collection of a bunch of EOF and WOF extensions.  See the ClassList.txt file in the Documentation directory for a short one sentence description of the classes included.  This framework depends on Log4j.


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

This should definitely be considered an alpha release, not because the code is buggy but because the documentation is sorely lacking.  We have been working hard to remove all of the dependencies between these frameworks and the applications and other NetStruxr specific frameworks that are built upon them.  So if you stumble across a "/nsi" image ref or a component that doesn't exist please report it to us (I have already spotted a few image references myself).  We would definitely like to see other people contributing to these frameworks, please let us know of any way we can help.  Most of all, please send us feedback about what you like, don't like, what you want to add/remove, suggestions, movies you have seen recently ....
  
Contacting Us:
website: http://wonder.sourceforge.net
email: wonder-disc@lists.sourceforge.net




