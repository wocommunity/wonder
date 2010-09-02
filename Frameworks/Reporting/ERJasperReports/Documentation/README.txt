JasperReports distribution includes a lot of jar dependencies. In fact all of them together add up to 27MB. 
Many are used for very specific purposes and are not needed for the typical reporting needs. 
Thus not every jar library that is included with JasperReports distribution has been included in this framework.

However, in the rare case that your report generates a class not found error due to a specific dependency library
being missing, please file a JIRA with the details and the required jar will be added.



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

Kieran 1/14/2010