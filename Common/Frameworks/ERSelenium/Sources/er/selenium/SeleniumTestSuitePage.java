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
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.ERXFileUtilities;
import er.extensions.ERXProperties;
import er.extensions.ERXStatelessComponent;
import er.extensions.ERXUtilities;
import er.selenium.filters.SeleniumCompositeTestFilter;
import er.selenium.filters.SeleniumOverrideOpenTestFilter;
import er.selenium.filters.SeleniumPresentationFilter;
import er.selenium.filters.SeleniumRepeatExpanderTestFilter;
import er.selenium.filters.SeleniumTestFilter;
import er.selenium.io.SeleniumImporterExporterFactory;
import er.selenium.io.SeleniumTestExporter;
import er.selenium.io.SeleniumTestImporter;

public class SeleniumTestSuitePage extends ERXStatelessComponent {	
	private static final Logger log = Logger.getLogger(SeleniumTestSuitePage.class);
	
	private static final String DEFAULT_SELENIUM_TESTS_ROOT = "./Contents/Resources/Selenium";
	private static final String DEFAULT_EXPORTER_NAME = "xhtml";

	protected static class TestDirectory {
		public File directory;
		public NSArray testFiles;
		
		public TestDirectory(File aDirectory, NSArray aTestFiles) {
			directory = aDirectory;
			testFiles = aTestFiles;
		}
	}
	
	protected String getFileExtension(String filename) {
		assert(filename != null);
		int index = filename.lastIndexOf(".");
		if (index > 0) {
			return filename.substring(index);
		} else {
			return null;
		}
	}
	
	protected NSArray buildTestsListForDirectory(File directory) {
		NSMutableArray result = new NSMutableArray();
		
		log.debug("Inspecting contents of directory '" + directory.getName());
		NSArray filesList = ERXFileUtilities.arrayByAddingFilesInDirectory(directory, false);
		assert(filesList != null);
		
		Iterator i = filesList.iterator();
		while (i.hasNext()) {
			File file = (File)i.next();
			String extension = getFileExtension(file.getName());

			if (extension != null) {
				SeleniumTestImporter importer = SeleniumImporterExporterFactory.instance().findImporterByExtension(extension);
				if (importer != null) {
					result.add(file);
					log.debug("Test file '" + file.getName() + "' of type '" + importer.name() + "'");
				} else {
					log.debug("Can't find importer for extension '" + extension + "' for file '" + file.getName() + "'");
				}
			} else {
				log.debug("File type cannot be determined due to the lack of extension for file '" + file.getName() + "'");
			}
		}

		return result;
	}
	
	protected NSMutableDictionary buildTestDirectoriesList() {
		NSMutableDictionary result = new NSMutableDictionary();
		
		String testsRoot = ERXProperties.stringForKeyWithDefault("SeleniumTestsRoot", DEFAULT_SELENIUM_TESTS_ROOT);
		File rootDir = new File(testsRoot);
		
		NSArray files = new NSArray(rootDir.listFiles());
		Iterator iter = files.iterator();
		while (iter.hasNext()) {
			File directory = (File)iter.next();
			NSArray testFilesList = buildTestsListForDirectory(directory);
			if (testFilesList.count() > 0) {
				TestDirectory testDirectory = new TestDirectory(directory, testFilesList);
				result.setObjectForKey(testDirectory, directory.getName());
			}
		}
		
		return result;
	}
	
	protected SeleniumTest buildTest(String directory, String testName) {
		assert(directory != null);
		assert(testName != null);
		
		String extension = getFileExtension(testName);
		if (extension == null) {
			throw new RuntimeException("Invalid testname '" + testName + "'");
		}
		
		SeleniumTestImporter importer = SeleniumImporterExporterFactory.instance().findImporterByExtension(extension);
		if (importer == null) {
			throw new RuntimeException("Unsupported file type ('" + extension + "')");
		}
		
		
    	File testFile = new File(ERXProperties.stringForKeyWithDefault("SeleniumTestsRoot", DEFAULT_SELENIUM_TESTS_ROOT) + "/" + directory + "/" + testName);
    	try {
    		String fileContents = ERXFileUtilities.stringFromFile(testFile, "UTF-8");
    		return importer.process(fileContents);
    	} catch (Exception e) {
    		log.debug(ERXUtilities.stackTrace(e));
    		throw new RuntimeException("Test import for '" + testName + "' failed.", e);
    	}
	}
	
	protected SeleniumCompositeTestFilter _testFilter;
	protected SeleniumCompositeTestFilter _testPresentationFilter;
	protected String _testDirectory;
	protected String _testName;
	
	protected NSMutableDictionary _testDirectories;
	public TestDirectory repDirectory;
	public File repTestFile;
	
	public void setTestDirectory(String name) {
		_testDirectory = name;
	}
	
	public String getTestDirectory() {
		return _testDirectory;
	}
	
	public void setTestName(String name) {
		_testName = name;
	}
	
	public String getTestName() {
		return _testName;
	}
	
	public SeleniumTestFilter testFilter() {
		if (_testFilter == null) {
			_testFilter = new SeleniumCompositeTestFilter();
			_testFilter.addTestFilter(new SeleniumRepeatExpanderTestFilter());
			_testFilter.addTestFilter(new SeleniumOverrideOpenTestFilter(context().urlWithRequestHandlerKey(null, null, null)));
		}
		
		return _testFilter;
	}
	
	public SeleniumTestFilter testPresentationFilter() {
		if (_testPresentationFilter == null) {
			_testPresentationFilter = new SeleniumCompositeTestFilter();
			_testPresentationFilter.addTestFilter(new SeleniumPresentationFilter());
		}
		
		return _testPresentationFilter;
	}
	
    public SeleniumTestSuitePage(WOContext context) {
        super(context);
    }
    
    public NSArray testDirectories() {
    	if (_testDirectories == null) {
    		_testDirectories = buildTestDirectoriesList();
    	}
    	
    	return _testDirectories.allValues();
    }
    
    public String testLink() {
    	NSMutableDictionary queryArgs = new NSMutableDictionary();
    	String format = context().request().stringFormValueForKey("format");
    	if (format != null)
    		queryArgs.setObjectForKey(format, "format");
    	return context().directActionURLForActionNamed("SeleniumTestSuite/" + repDirectory.directory.getName() +'_' + repTestFile.getName(), queryArgs);
    }
    
    public String testContents() {
    	if (_testDirectory != null && _testName != null) {
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
    			SeleniumTest test = buildTest(_testDirectory, _testName);
    			if (context().request().formValueForKey("noFilters") == null) {
    				test = ("presentation".equals(format) ? testPresentationFilter() : testFilter()).processTest(test);
    				assert(test != null);
    			}
    			String result = exporter.process(test);
    			return result;
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
    	_testDirectories = null;
    }
}
