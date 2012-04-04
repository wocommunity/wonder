JasperReports distribution includes a lot of jar dependencies. In fact all of them together add up to 27MB. 
Many are used for very specific purposes and are not needed for the typical reporting needs. 
Thus not every jar library that is included with JasperReports distribution has been included in this framework.

However, in the rare case that your report generates a class not found error due to a specific dependency library
being missing, please file a JIRA with the details and the required jar will be added.

Which Version of iReport is Compatible?
---------------------------------------
The iReport MUST match the JasperReports version in this project. You have been warned.
Look in Libraries directory of this project for the jar named something like jasperreports-X.X.X.jar.
That file has the current version number.

Class Path
-------------

The following libraries MUST be on the classpath. Some of these are included in other frameworks. To minimize 
having multiple versions of the same java libs all over the place, we will link to frameworks in Wonder or of our own
that have the java libs that we need .... dependency management acrobatics is part of the game unfortunately :-)

Apache Commons
	WKFoundation has what we need.
	
iText
	WKPdf has the version 2.1.0 included with JasperReports 3.6.0. ERPDFGeneration has older 2.0.8
	
JFreeChart
	ERPlot has that jar
	
	
Using compiled reports (.jasper instead of .jrxml)
--------------------------------------------------
If you have dependencies that require older xml parsers in your classpath, then you may need to use pre-compiled reports.
This is more desirable since it can take 10 to 20 seconds to compile a report each time it is used from raw jrxml file.

Also, using compiled reports will ensure that your reports to not have errors that will cause compilation to fail in deployment.
The only reason you would need to compile in deployment is if you were dynamically editing the template on the fly for some reason.

Ideally, use an ant task to compile jrxml reports in advance.


Fonts and Cross Platform Deployment
======================================
WARNING: Just because Jasper can render a PDF report on your development Mac does not mean that the font is available on the target deployment environment. This is especially true when deploying on Linux. The objective here is to ensure that the fonts you need are bundled with your application. Sounds complicated but there is some shortcuts here if you are in a hurry.

If you are not sure what fonts you are actually using in a complex report, just ipen the report in a text edit and search for the 'fontName' attribute thoughout the jasper xml document and make a list of the fonts used.

Bundled Fonts  - Easy Solution #1
----------------------------------
Use one of the open source fonts that are already bundled into the iText jar that is included with JasperReports:

Deja Vu Sans
Deja Vu Serif
Deja Vu Mono

(More info here: http://dejavu-fonts.org/)

You will notice these are in a sub-section at the very top of the fonts popup menu in the iReport designer.

Also included are generic fonts (which may be just aliases to the Deja Vu fonts, or may just pick the system default for these basic font styles, I don't know). You may get less predictable output if you use these.

Monspaced
SansSerif
Serif

Bundled Fonts  - Easy Solution #2
----------------------------------
Use one of the "Base 14" PDF fonts (aka "Standard Type 1 Fonts", aka "Standard 14 Fonts"):
http://en.wikipedia.org/wiki/Portable_Document_Format#Standard_Type_1_Fonts_.28Standard_14_Fonts.29

The PDF standard specifies 14 fonts that should be available to all PDF viewers (and renderers?) on all systems. The 14 typefaces are in reality made up of 5 fonts and their regular, italic, bold and bold-italic variants. The standard fonts are:

Times (v3) (in regular, italic, bold, and bold italic)
Courier (in regular, oblique, bold and bold oblique)
Helvetica (v3) (in regular, oblique, bold and bold oblique)
Symbol
Zapf Dingbats


Bundled Fonts - More steps, but still easy Solution
----------------------------------------------------
Applies to TrueType fonts only. Embed the required TrueType fonts in a jar and add them to your classpath.

One way of doing this is:

iReport Preferences
	iReport -> Fonts
	By default this displays a list of the fonts that are embedded into Jasper/iText
	
	Add fonts from your operating system using Install Font button to the right.
		(Use this wizard to select and configure TrueType fonts from your OS file system that you are using for your reports).
		The fonts you add from uyour system will now show up in this list in iReport's Fonts prefs.
	
	To create the jasper jar containing fonts that can be added to your project, simply:
		Select one or more fonts from the list of font in iReport's Fonts preference pane. 
		Then use 'Export as extension' wizard to export them as a jar file into your project's Libraries folder.
			(Make sure to ad the .jar extension to the file name, for example jasper-Helvetica.jar)
	
	After that, simply add the jar to your class path and these fonts will be used in deployment even when not installed on the deployment OS.
	
