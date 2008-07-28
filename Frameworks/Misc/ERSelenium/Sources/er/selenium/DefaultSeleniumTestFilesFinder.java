package er.selenium;

import java.io.File;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXFileUtilities;
import er.selenium.io.SeleniumImporterExporterFactory;
import er.selenium.io.SeleniumTestImporter;

public class DefaultSeleniumTestFilesFinder implements SeleniumTestFilesFinder {
	private static final Logger log = Logger.getLogger(DefaultSeleniumTestFilesFinder.class);
	
	public NSArray<File> findTests(File rootDir) {
		NSMutableArray<File> result = new NSMutableArray<File>();
		
		log.debug("Inspecting contents of directory '" + rootDir.getAbsolutePath());
		NSArray<File> filesList = ERXFileUtilities.arrayByAddingFilesInDirectory(rootDir, true);
		
		for (File file : filesList) {
			String fname = file.getName();

			if (!file.isFile()) {
				log.debug("Ignoring " + fname + " as it is not a regular file");
				continue;
			}
			
			if (fname.startsWith("_")) {
				log.debug("Ignoring " + fname + " because of the starting _");
				continue;
			}
			String extension = "." + ERXFileUtilities.fileExtension(fname);
			SeleniumTestImporter importer = SeleniumImporterExporterFactory.instance().findImporterByExtension(extension);
			if (importer != null) {
				result.add(file);
				log.debug("Test file '" + file.getName() + "' of type '" + importer.name() + "'");
			} else {
				log.debug("Can't find importer for extension '" + extension + "' for file '" + file.getName() + "'");
			}
		}
		
		ERXArrayUtilities.sortArrayWithKey(result, "name");
		return result;
	}
	
}
