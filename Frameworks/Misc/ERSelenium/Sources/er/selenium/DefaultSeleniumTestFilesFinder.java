package er.selenium;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXFileUtilities;
import er.selenium.io.SeleniumImporterExporterFactory;
import er.selenium.io.SeleniumTestImporter;

public class DefaultSeleniumTestFilesFinder implements SeleniumTestFilesFinder {
	private static final Logger log = LoggerFactory.getLogger(DefaultSeleniumTestFilesFinder.class);
	
	public NSArray<File> findTests(File rootDir) {
		NSMutableArray<File> result = new NSMutableArray<File>();
		
		log.debug("Inspecting contents of directory '{}'.", rootDir.getAbsolutePath());
		NSArray<File> filesList = ERXFileUtilities.arrayByAddingFilesInDirectory(rootDir, true);
		
		for (File file : filesList) {
			String fname = file.getName();

			if (!file.isFile()) {
				log.debug("Ignoring {} as it is not a regular file.", fname);
				continue;
			}
			
			if (fname.startsWith("_")) {
				log.debug("Ignoring {} because of the starting '_'.", fname);
				continue;
			}
			String extension = "." + ERXFileUtilities.fileExtension(fname);
			SeleniumTestImporter importer = SeleniumImporterExporterFactory.instance().findImporterByExtension(extension);
			if (importer != null) {
				result.add(file);
				log.debug("Test file '{}' of type '{}'.", file.getName(), importer.name());
			} else {
				log.debug("Can't find importer for extension '{}' for file '{}'.", extension, file.getName());
			}
		}
		
		ERXArrayUtilities.sortArrayWithKey(result, "absolutePath");
		return result;
	}
	
}
