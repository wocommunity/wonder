package er.selenium.filters;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSRange;

import er.extensions.ERXFileUtilities;

import er.selenium.SeleniumTest;
import er.selenium.io.SeleniumImporterExporterFactory;
import er.selenium.io.SeleniumTestImporter;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class SeleniumIncludeTestFilter extends SeleniumTestFilterHelper implements SeleniumTestFilter {
	private static final Logger log = Logger.getLogger(SeleniumIncludeTestFilter.class);

	private static final int INCLUDE_LIMIT = 256;
	private final NSArray _searchPaths;
	
	public SeleniumIncludeTestFilter(NSArray searchPaths) {
		_searchPaths = searchPaths;
	}
	
	protected NSArray getIncludedArguments(String name) {
		SeleniumTestImporter importer = SeleniumImporterExporterFactory.instance().findImporterByExtension('.' + ERXFileUtilities.fileExtension(name));
		if (importer == null) {
			throw new RuntimeException("Can't find importer for included test file: " + name);
		}
		
		Iterator iter = _searchPaths.iterator();
		while (iter.hasNext()) {
			File fio = new File((String)iter.next() + "/" + name);
			if (fio.exists()) {
				String fileContents;
				try {
					fileContents = ERXFileUtilities.stringFromFile(fio, "UTF-8");
				} catch (IOException e) {
					log.error("Can't read " + fio.getAbsolutePath() + " contents");
					throw new RuntimeException(e);
				}
				SeleniumTest processedTest = importer.process(fileContents);
				return processedTest.elements();
			}
		}
		
		throw new RuntimeException("Included path not found: " + name);
	}
	
//	 @Override
	protected void processTestElements(NSMutableArray elements) {
		int includeCount = 0;
		int i = 0;
		while (i < elements.count()) {
			SeleniumTest.Element element = (SeleniumTest.Element)elements.get(i);
			if (element instanceof SeleniumTest.MetaCommand) {
				SeleniumTest.MetaCommand metaCommand = (SeleniumTest.MetaCommand)element;
				if (metaCommand.getName().equals("include")) {
					if (includeCount >= INCLUDE_LIMIT) {
						throw new RuntimeException("Too many @include commands (recursive include?)");
					}
					NSArray newElements = getIncludedArguments(metaCommand.argumentsString());
					
					NSArray tailElements = elements.subarrayWithRange(new NSRange(i + 1, elements.count() - i - 1));
					elements.removeObjectsInRange(new NSRange(i, elements.count() - i));
					elements.addObjectsFromArray(newElements);
					elements.addObjectsFromArray(tailElements);
					++includeCount;
				}
			}
			++i;
		}
	}

}
