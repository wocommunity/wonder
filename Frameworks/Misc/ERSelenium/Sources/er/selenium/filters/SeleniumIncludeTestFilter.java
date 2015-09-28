package er.selenium.filters;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.CharEncoding;
import org.apache.log4j.Logger;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSRange;

import er.extensions.foundation.ERXFileUtilities;
import er.selenium.SeleniumTest;
import er.selenium.io.SeleniumImporterExporterFactory;
import er.selenium.io.SeleniumTestImporter;

public class SeleniumIncludeTestFilter extends SeleniumTestFilterHelper implements SeleniumTestFilter {
	private static final Logger log = Logger.getLogger(SeleniumIncludeTestFilter.class);

	private static final int INCLUDE_LIMIT = 256;
	private final NSArray<File> _searchPaths;
	
	public SeleniumIncludeTestFilter(NSArray<File> searchPaths) {
		_searchPaths = searchPaths;
	}
	
	protected NSArray<SeleniumTest.Element> getIncludedArguments(String name) {
		SeleniumTestImporter importer = SeleniumImporterExporterFactory.instance().findImporterByExtension('.' + ERXFileUtilities.fileExtension(name));
		if (importer == null) {
			throw new RuntimeException("Can't find importer for included test file: " + name);
		}
	
		for (File sp : _searchPaths) {
			File fio = new File(sp.getAbsolutePath() + "/" + name);
			if (fio.exists()) {
				String fileContents;
				try {
					fileContents = ERXFileUtilities.stringFromFile(fio, CharEncoding.UTF_8);
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

	@Override
	protected void processTestElements(NSMutableArray<SeleniumTest.Element> elements) {
		int includeCount = 0;
		int i = 0;
		while (i < elements.count()) {
			SeleniumTest.Element element = elements.get(i);
			if (element instanceof SeleniumTest.MetaCommand) {
				SeleniumTest.MetaCommand metaCommand = (SeleniumTest.MetaCommand)element;
				if (metaCommand.getName().equals("include")) {
					if (includeCount >= INCLUDE_LIMIT) {
						throw new RuntimeException("Too many @include commands (recursive include?)");
					}
					NSArray<SeleniumTest.Element> newElements = getIncludedArguments(metaCommand.argumentsString());
					
					NSArray<SeleniumTest.Element> tailElements = elements.subarrayWithRange(new NSRange(i + 1, elements.count() - i - 1));
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
