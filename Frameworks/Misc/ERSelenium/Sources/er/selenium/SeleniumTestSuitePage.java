/*
 * Copyright (c) 2007 Design Maximum - http://www.designmaximum.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */

package er.selenium;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.components.ERXStatelessComponent;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXUtilities;
import er.selenium.filters.SeleniumCompositeTestFilter;
import er.selenium.filters.SeleniumIncludeTestFilter;
import er.selenium.filters.SeleniumOverrideOpenTestFilter;
import er.selenium.filters.SeleniumRepeatExpanderTestFilter;
import er.selenium.filters.SeleniumTestFilter;
import er.selenium.io.SeleniumImporterExporterFactory;
import er.selenium.io.SeleniumTestExporter;

public class SeleniumTestSuitePage extends ERXStatelessComponent {	
	private static final Logger log = Logger.getLogger(SeleniumTestSuitePage.class);
	
	private static final String DEFAULT_SELENIUM_TESTS_ROOT = "./Contents/Resources/Selenium";
	private static final String DEFAULT_EXPORTER_NAME = "xhtml";

	private SeleniumCompositeTestFilter testFilter;
	
	private NSArray<File> testFiles;
	public File testFile;
	private String testPath;
	
	public NSArray<File> testFiles() {
		if (testFiles == null) {
			testFiles = new SeleniumTestFilesFinder(testsRoot()).findTests();
		}
		return testFiles;
	}

	public void setTestPath(String testPath) {
		this.testPath = testPath;
		testFile = new File(testsRoot().getAbsolutePath() + "/" + testPath);
	}
	
	public String testPath() {
		return testPath;
	}
	
	protected File testsRoot() {
		return new File(ERXProperties.stringForKeyWithDefault("SeleniumTestsRoot", DEFAULT_SELENIUM_TESTS_ROOT));		
	}
	
	public File testDir() {
		return testFile.getParentFile();
	}
	
	protected void checkTestPath() {
		try {
			String cnTestPath = testFile.getCanonicalPath();
			String cnTestsRoot = testsRoot().getCanonicalPath();
			if (!cnTestPath.startsWith(cnTestsRoot)) {
				throw new RuntimeException("Trying to reach file (" + cnTestPath + ") ouside of the tests root (" + cnTestsRoot + ")");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}		
	}
	
	public SeleniumTestFilter testFilter() {
		if (testFilter == null) {
			testFilter = new SeleniumCompositeTestFilter();
			
			File[] searchPaths = {testDir().getAbsoluteFile(), testsRoot().getAbsoluteFile()}; 
			testFilter.addTestFilter(new SeleniumIncludeTestFilter(new NSArray<File>(searchPaths)));

			testFilter.addTestFilter(new SeleniumRepeatExpanderTestFilter());
			testFilter.addTestFilter(new SeleniumOverrideOpenTestFilter(context().urlWithRequestHandlerKey(null, null, null)));
		}
		
		return testFilter;
	}
	
    public SeleniumTestSuitePage(WOContext context) {
        super(context);
    }
    
    public String testLink() {
    	NSMutableDictionary<String, String> queryArgs = new NSMutableDictionary<String, String>();
    	String format = context().request().stringFormValueForKey("format");
    	if (format != null)
    		queryArgs.setObjectForKey(format, "format");
    	return context().directActionURLForActionNamed("SeleniumTestSuite/" + testDir().getName() +ERSelenium.SUITE_SEPERATOR + testFile.getName(), queryArgs);
    }
    
    public String testContents() {
    	if (testPath != null) {
    		SeleniumTestExporter exporter = null;
    		String format = context().request().stringFormValueForKey("format");
    		if (format != null) {
    			exporter = SeleniumImporterExporterFactory.instance().findExporterByName(format);
    			if (exporter == null) {
    				throw new RuntimeException("Unsupported output format specified ('" + format + "')");
    			}
    		} else {
    			exporter = SeleniumImporterExporterFactory.instance().findExporterByName(DEFAULT_EXPORTER_NAME);
    			assert(exporter != null);
    		}
    		
    		try {
    			SeleniumTest test = new SeleniumTestFileProcessor(testFile, context().request().formValueForKey("noFilters") == null ? testFilter() : null).process();
    			return exporter.process(test);
    		} catch (Exception e) {
    			log.debug(ERXUtilities.stackTrace(e));
    			throw new RuntimeException("Test export failed", e);
    		}
    	} else {
    		return null;
    	}
    }
    
    @Override
    public void reset() {
    	super.reset();
    	testPath = null;
    }
    
}
