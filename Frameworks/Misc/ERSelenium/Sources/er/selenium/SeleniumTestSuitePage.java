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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.components.ERXStatelessComponent;
import er.extensions.foundation.ERXProperties;
import er.selenium.filters.SeleniumCompositeTestFilter;
import er.selenium.filters.SeleniumIncludeTestFilter;
import er.selenium.filters.SeleniumOverrideOpenTestFilter;
import er.selenium.filters.SeleniumRepeatExpanderTestFilter;
import er.selenium.filters.SeleniumTestFilter;
import er.selenium.io.SeleniumImporterExporterFactory;
import er.selenium.io.SeleniumTestExporter;

public class SeleniumTestSuitePage extends ERXStatelessComponent {	
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(SeleniumTestSuitePage.class);
	
	// NSProjectBundleEnabled is true if we have the newer "bundle-less builds" WOLips feature in use
	// and that expects the FBL project layout, so the default location is different to a built runtime bundle
	private static final String DEFAULT_SELENIUM_TESTS_ROOT = 
		ERXProperties.booleanForKeyWithDefault("NSProjectBundleEnabled", false) ? "./Resources/Selenium" : "./Contents/Resources/Selenium";

	private static final String DEFAULT_EXPORTER_NAME = "xhtml";

	private SeleniumCompositeTestFilter testFilter;
	
	private NSArray<File> testFiles;
	public File testFile;
	private File groupDir;
	private String testPath;
	
	public NSArray<File> testFiles() {
		if (testFiles == null) {
			testFiles = ERSelenium.testFilesFinder().findTests(groupDir != null ? groupDir : testsRoot());
		}
		return testFiles;
	}

	public void setTestPath(String testPath) {
		this.testPath = testPath;
		File fpath = new File(testsRoot().getAbsolutePath() + "/" + testPath);
		if (fpath.isDirectory()){
			groupDir = fpath;
		} else {
			testFile = fpath;
		}
	}
	
	public String testPath() {
		return testPath;
	}
	
	protected File testsRoot() {
		return new File(ERXProperties.stringForKeyWithDefault("SeleniumTestsRoot", DEFAULT_SELENIUM_TESTS_ROOT));		
	}
	
	protected File testDir() {
		return testFile.getParentFile();
	}
	
	public String testGroupName() {
		File parentFile = testFile.getParentFile();
		try {
			return parentFile.getCanonicalPath().equals(testsRoot().getCanonicalPath()) ? "" : parentFile.getName();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public boolean hasTestGroup() {
		return testGroupName().length() > 0;
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
			if (!ERXProperties.booleanForKey("er.selenium.filters.overrideopen.disable")) {
				testFilter.addTestFilter(new SeleniumOverrideOpenTestFilter(context().urlWithRequestHandlerKey(null, null, null)));
			}
		}
		
		return testFilter;
	}
	
    public SeleniumTestSuitePage(WOContext context) {
        super(context);
    }
    
    public String testLink() {
    	NSMutableDictionary<String, Object> queryArgs = new NSMutableDictionary<String, Object>();
    	String format = context().request().stringFormValueForKey("format");
    	if (format != null)
    		queryArgs.setObjectForKey(format, "format");
    	context().generateCompleteURLs();
     	String link = context().directActionURLForActionNamed(String.format("SeleniumTestSuite/%s%s", (hasTestGroup() ? testGroupName() + ERSelenium.SUITE_SEPERATOR : ""), testFile.getName()), queryArgs, context().request().isSecure(), false);
    	context().generateRelativeURLs();
    	return link;
    }
    
    public String testContents() {
    	if (testPath != null && groupDir == null) {
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
    			log.debug("Test export failed.", e);
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
    	groupDir = null;
    }
    
}
