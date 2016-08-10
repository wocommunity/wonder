package er.selenium.rc;

import java.io.File;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.varia.LevelRangeFilter;

import com.webobjects.foundation.NSArray;

import er.extensions.foundation.ERXProperties;
import er.selenium.DefaultSeleniumTestFilesFinder;
import er.selenium.ERSelenium;
import er.selenium.SeleniumTest;
import er.selenium.SeleniumTestFileProcessor;
import er.selenium.filters.SeleniumCompositeTestFilter;
import er.selenium.filters.SeleniumIncludeTestFilter;
import er.selenium.filters.SeleniumOverrideOpenTestFilter;
import er.selenium.filters.SeleniumRepeatExpanderTestFilter;

public class StandaloneRunner {
	private static final Logger log = Logger.getLogger(StandaloneRunner.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
        Logger root = Logger.getRootLogger();
        ConsoleAppender appender = new ConsoleAppender(new PatternLayout("%r [%t] %p %c %x - %m%n"));
        appender.setTarget("System.err");
        LevelRangeFilter filter = new LevelRangeFilter();
        filter.setLevelMin(Level.DEBUG);
        filter.setLevelMax(Level.DEBUG);
        appender.addFilter(filter);
        appender.activateOptions();
        root.addAppender(appender);
        
        appender = new ConsoleAppender(new PatternLayout());
        appender.setTarget("System.out");
        filter = new LevelRangeFilter();
        filter.setLevelMin(Level.INFO);
        filter.setLevelMax(Level.FATAL);
        appender.addFilter(filter);
        appender.activateOptions();
        root.addAppender(appender);

		ERSelenium.registerImportersExporters();
		
		File testsRoot = new File(args[0]);
		NSArray<File> testsFiles = new DefaultSeleniumTestFilesFinder().findTests(testsRoot);
		log.debug(testsFiles);
		
		String appHost = args[1];
		String host = args[2];
		int port = args.length >= 4 ? Integer.parseInt(args[3]) : 4444;
		String browserType = args.length >= 5 ? args[4] : "*firefox";
		boolean takeScreenshots = ERXProperties.booleanForKeyWithDefault("er.selenium.screenshotEnabled", false);
		String screenshotPath = ERXProperties.stringForKeyWithDefault("er.selenium.screenshotPath", System.getProperty("java.io.tmpdir"));
				
		boolean failed = false;
		SeleniumTestRCRunner runner = new SeleniumTestRCRunner(host, port, browserType, appHost);
		runner.prepare();
		try {
			for (File testFile : testsFiles) {
				SeleniumTest test = null;
				try {
					SeleniumCompositeTestFilter testFilter = new SeleniumCompositeTestFilter();
					File[] searchPaths = {testFile.getAbsoluteFile().getParentFile(), testsRoot.getAbsoluteFile()}; 
					testFilter.addTestFilter(new SeleniumIncludeTestFilter(new NSArray<File>(searchPaths)));
					testFilter.addTestFilter(new SeleniumRepeatExpanderTestFilter());
					testFilter.addTestFilter(new SeleniumOverrideOpenTestFilter(appHost));
					
					test = new SeleniumTestFileProcessor(testFile, testFilter).process();
					log.debug("running: " + testFile);
					runner.run(test);
					
					log.info(String.format("test '%s' PASSED", testFile));
					log.info("");
				} catch (SeleniumTestFailureException e) {
					failed = true;
					
					log.error(String.format("test '%s' FAILED: %s", testFile, e));
					log.error("test log:");
					int curCommand = 0;
					for (SeleniumTest.Element elem: test.elements()) {
						if (elem instanceof SeleniumTest.Command) {
							if (curCommand++ > e.processedCommands()) {
								break;
							}
							SeleniumTest.Command command = (SeleniumTest.Command)elem;
							log.error(String.format("%s|%s|%s", command.getName(), command.getTarget(), command.getValue()));
						}
						
					}
					if (takeScreenshots) {
					  String pathPrefix = screenshotPath + "/" + testFile.getName();
					  log.error("Saving screenshot to " + pathPrefix + ".png");
					  if ("*firefox".equals(browserType)) {
	            log.error("Saving full screenshot to " + pathPrefix + "-full.png");
					    runner.captureEntirePageScreenshot(pathPrefix + "-full.png");
					  } 
					  runner.captureScreenshot(pathPrefix + ".png");
					}
          log.error("");
				}
			}
		} finally {
			runner.finish();
		}
		System.exit(failed ? 1 : 0);
	}

}
