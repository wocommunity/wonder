package er.selenium;

import java.io.File;

import org.apache.commons.lang3.CharEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import er.extensions.foundation.ERXFileUtilities;
import er.selenium.filters.SeleniumTestFilter;
import er.selenium.io.SeleniumImporterExporterFactory;
import er.selenium.io.SeleniumTestImporter;

public class SeleniumTestFileProcessor {
	private static final Logger log = LoggerFactory.getLogger(SeleniumTestFileProcessor.class);
	
	private final File testFile;
	private final SeleniumTestFilter filter;

	public SeleniumTestFileProcessor(File testFile, SeleniumTestFilter filter) {
		this.testFile = testFile;
		this.filter = filter;
	}
	
	public SeleniumTest process() {
		String extension = "." + ERXFileUtilities.fileExtension(testFile.getName()); 
		
		SeleniumTestImporter importer = SeleniumImporterExporterFactory.instance().findImporterByExtension(extension);
		if (importer == null) {
			throw new RuntimeException("Can't process '" + testFile.getAbsolutePath() + "': unsupported file type ('" + extension + "')");
		}
		
    	try {
    		String fileContents = ERXFileUtilities.stringFromFile(testFile, CharEncoding.UTF_8);
    		SeleniumTest result = importer.process(fileContents);
    		if (filter != null) {
    			result = filter.processTest(result);
    		}
    		return result;
    	} catch (Exception e) {
    		log.debug("Test import for '{}' failed.", testFile.getAbsolutePath(), e);
    		throw new RuntimeException("Test import for '" + testFile.getAbsolutePath() + "' failed.", e);
    	}

	}
}
