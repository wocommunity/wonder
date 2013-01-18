/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.logging;

import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggerRepository;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.ERXExtensions;
import er.extensions.foundation.ERXArrayUtilities;

/**
 * Configures and manages the log4j logging system. Will also configure the system for rapid turn around, i.e. when
 * WOCaching is disabled when the conf file changes it will get reloaded.
 */
public class ERXLog4JConfiguration extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /**
     * A representation of the various Logger levels.
     */
    public enum LoggerLevel {
        ALL(Level.ALL, "All"),
        TRACE(Level.TRACE, "Trace"),
        DEBUG(Level.DEBUG, "Debug"),
        INFO(Level.INFO, "Info"),
        WARN(Level.WARN, "Warn"),
        ERROR(Level.ERROR, "Error"),
        FATAL(Level.FATAL, "Fatal"),
        OFF(Level.OFF, "Off"),
        UNSET(null, "Unset"); // Unset is a "fake" level that doesn't correspond to an actual Log4J Level.

        private Level level;
        private String displayName;

        private static Map<Level, LoggerLevel> levelsByLog4JLevel;

        static {
            levelsByLog4JLevel = new HashMap<Level, LoggerLevel>(8);
            for (LoggerLevel level : LoggerLevel.values()) {
                levelsByLog4JLevel.put(level.level(), level);
            }
        }

        LoggerLevel(Level level, String displayName) {
            this.level = level;
            this.displayName = displayName;
        }

        public Level level() { return level; }
        public String displayName() { return displayName; }
        
        public static LoggerLevel loggerLevelForLog4JLevel(Level level) { return levelsByLog4JLevel.get(level); }
    }

    /**
     * A representation of the available page sections/views.
     */
    public enum PageSection {
        LOGGERS("Loggers", "Loggers"),
        REPOSITORY("Repository", "Repository"),
        APPENDERS("Appenders", "Appenders"),
        OTHER("Other", "Other Settings");

        private String displayName;
        private String id;
        private static Map<String, PageSection> sectionsById;
        static {
            sectionsById = new HashMap<String, PageSection>(4);
            for (PageSection section : PageSection.values()) {
                sectionsById.put(section.id(), section);
            }
        }

        PageSection(String id, String displayName) {
            this.displayName = displayName;
            this.id = id;
        }

        public String displayName() { return displayName; }
        public String id() { return id; }
        public static PageSection sectionWithId(String name) { return sectionsById.get(name); }
    }


    private Logger _logger;
    private String _filterString;
    private String _ruleKey;
    private String _loggerName;
    public LoggerLevel filterLevel;

    public LoggerLevel newLoggerLevel = null;

    private transient NSArray _appenders;
    public AppenderSkeleton anAppender;
    public Level aLevel;
    public LoggerLevel aLoggerLevel;

    public boolean isNewLoggerARuleLogger = false;
    public boolean showAll = false;
    public int rowIndex = 0;

    private static final NSArray _pageSections = new NSArray(PageSection.values());
    public PageSection aPageSection;
    private PageSection _activeSection = PageSection.LOGGERS;

    public final static EOSortOrdering NAME_SORT_ORDERING = new EOSortOrdering("name", EOSortOrdering.CompareAscending);
    public final static NSMutableArray SORT_BY_NAME = new NSMutableArray(NAME_SORT_ORDERING);

    public ERXLog4JConfiguration(WOContext aContext) {
        super(aContext);
    }

    public Logger logger() { return _logger; }
    public void setLogger(Logger newValue) { _logger = newValue; }

    public String filterString() { return _filterString; }
    public void setFilterString(String newValue) { _filterString = newValue; }

    public String loggerName() { return _loggerName; }
    public void setLoggerName(String newValue) { _loggerName = newValue; }

    public String ruleKey() { return _ruleKey; }
    public void setRuleKey(String newValue) { _ruleKey = newValue; }

    public NSArray pageSections() { return _pageSections; }
    public String activeSection() { return _activeSection.displayName(); }
    public void setActiveSection(String name) {
        _activeSection = PageSection.sectionWithId(name);
        if (null == _activeSection) {
            _activeSection = PageSection.LOGGERS;
        }
    }

    /**
     * Gets all of the configured {@link Logger loggers} that pass the filters for logger name and level.
     * @return the loggers
     */
    public NSArray loggers() {
        NSMutableArray result = new NSMutableArray();
        for (Enumeration e = allLoggers().objectEnumerator(); e.hasMoreElements();) {
            Logger log = (Logger)e.nextElement();
            while (log != null) {
                addLogger(log, result);
                log = (Logger)log.getParent();
            }
        }
        return result;
    }

    private NSArray allLoggers() {
        NSMutableArray result = new NSMutableArray();
        Logger rootLogger = LogManager.getRootLogger(); // Float the root logger to the top.
        addLogger(rootLogger, result);

        NSMutableArray otherLoggers = new NSMutableArray();
        for (Enumeration e = LogManager.getCurrentLoggers(); e.hasMoreElements();) {
            Logger log = (Logger)e.nextElement();
            while (log != null) {
                if (log != rootLogger) {
                    otherLoggers.addObject(log);
                }
                log = (Logger)log.getParent();
            }
        }
        EOSortOrdering.sortArrayUsingKeyOrderArray(otherLoggers, SORT_BY_NAME);
        result.addObjectsFromArray(otherLoggers);

        return result;
    }

    /**
     * Adds a logger instance to the provided array, filtering those that don't fit the filter string / filter level.
     * @param log to add
     * @param result array to which the logger will be added if it passes the filter constraint
     */
    public void addLogger(Logger log, NSMutableArray result) {
        if (!result.containsObject(log)) {
            boolean passesFilterString = false;
            boolean passesFilterLevel = false;
            if ((filterString() == null || filterString().length() == 0 || log.getName().toLowerCase().indexOf(filterString().toLowerCase()) != -1) &&
                (showAll || log.getLevel() != null)) {
                passesFilterString = true;
            }

            if (null == filterLevel || LoggerLevel.loggerLevelForLog4JLevel(log.getLevel()) == filterLevel) {
                passesFilterLevel = true;
            }

            if (passesFilterString && passesFilterLevel) {
                result.addObject(log);
            }
        }
    }

    public LoggerLevel currentLoggerLevel() {
        return _logger != null ? LoggerLevel.loggerLevelForLog4JLevel(_logger.getLevel()) : LoggerLevel.UNSET;
    }

    public void setCurrentLoggerLevel(LoggerLevel loggerLevel) {
        _logger.setLevel(loggerLevel.level());
    }

    public String classNameForLoggerLevelName() {
        NSMutableArray classes = new NSMutableArray();
        if (aLoggerLevel == LoggerLevel.UNSET) {
            classes.addObject("unset");
        }
        if (currentLoggerLevel() == aLoggerLevel) {
            classes.addObject("selected");
        }
        return classes.componentsJoinedByString(" ");
    }

    public String classForLoggerRow() {
        NSMutableArray array = new NSMutableArray();
        Level level = logger().getLevel();
        if (level != null) {
            array.addObject(level.toString().toLowerCase());
        }
        if (rowIndex % 2 == 0) {
            array.addObject("alt");
        }
        return array.componentsJoinedByString(" ");
    }

    public boolean omitLoggerLevelSettingDecoration() {
        return classNameForLoggerLevelName().length() == 0;
    }

    public NSArray loggerLevels() {
        return new NSArray(LoggerLevel.values());
    }

    public NSArray loggerLevelsWithoutUnset() {
        return ERXArrayUtilities.arrayMinusObject(new NSArray(LoggerLevel.values()), LoggerLevel.UNSET);
    }

    public LoggerRepository loggerRepository() {
        return LogManager.getLoggerRepository();
    }

    public String classNameForLoggerRepositoryThresholdName() {
        return loggerRepository().getThreshold() == aLevel ? "selected" : null;
    }

    public boolean omitLoggerRepositoryThresholdSettingDecoration() {
        return null == classNameForLoggerRepositoryThresholdName();
    }

    /**
     * Gets the attached to the loggers.  This class currently only knows how to work with appenders that subclass
     * {@link AppenderSkeleton}.
     * @return the array of appenders
     */
    public NSArray appenders() {
        if (null == _appenders) {
            Set<AppenderSkeleton> appenders = new TreeSet<AppenderSkeleton>(new Comparator<AppenderSkeleton>() {
                public int compare(AppenderSkeleton o1, AppenderSkeleton o2) {
                    int result = 0;
                    if (o1 == o2)  {
                        result = 0;
                    } else {
                        String name1 = o1.getName();
                        String name2 = o2.getName();
                        if (name1.equals(name2)) { // Two appenders share the same name.  Probably a misconfiguration...
                            name1 = o1.getName() + "_" + o1.hashCode();
                            name2 = o2.getName() + "_" + o2.hashCode();
                        }
                        result = name1.compareTo(name2);
                    }
                    return result;
                }
            });

            // Gather appenders attached to the root logger.
            Logger rootLogger = LogManager.getRootLogger();
            Enumeration rootAppendersEnum = rootLogger.getAllAppenders();
            while (rootAppendersEnum.hasMoreElements()) {
                Appender appender = (Appender)rootAppendersEnum.nextElement();
                if (appender instanceof AppenderSkeleton) {
                    appenders.add((AppenderSkeleton)appender);
                }
            }

            // Gather appenders attached to other loggers.
            Enumeration loggersEnum = LogManager.getCurrentLoggers();
            while (loggersEnum.hasMoreElements()) {
                Logger logger = (Logger)loggersEnum.nextElement();
                Enumeration appendersEnum = logger.getAllAppenders();
                while (appendersEnum.hasMoreElements()) {
                    Appender appender = (Appender)appendersEnum.nextElement();
                    if (appender instanceof AppenderSkeleton) {
                        appenders.add((AppenderSkeleton)appender);
                    }
                }
            }
            _appenders = new NSArray(appenders.toArray());
        }
        return _appenders;
    }

    public NSArray levelsWithoutUnset() {
        NSArray applicableLevels = ERXArrayUtilities.arrayMinusObject(new NSArray(LoggerLevel.values()), LoggerLevel.UNSET);
        return (NSArray)applicableLevels.valueForKey("level");
    }

    public LoggerLevel currentAppenderLevel() {
        Priority threshold = anAppender.getThreshold();
        return LoggerLevel.loggerLevelForLog4JLevel(threshold != null ? Level.toLevel(threshold.toInt()) : null);
    }

    public void setCurrentAppenderLevel(LoggerLevel loggerLevel) {
        anAppender.setThreshold(loggerLevel.level());
    }

    public String classForAppenderRow() {
        NSMutableArray array = new NSMutableArray();
        Level level = currentAppenderLevel().level();
        if (level != null) {
            array.addObject(level);
        }
        if (rowIndex % 2 == 0) {
            array.addObject("alt");
        }
        return array.componentsJoinedByString(" ");
    }

    public String classNameForAppenderThresholdName() {
        NSMutableArray classes = new NSMutableArray();
        if (aLoggerLevel == LoggerLevel.UNSET) {
            classes.addObject("unset");
        }
        if (currentAppenderLevel() == aLoggerLevel) {
            classes.addObject("selected");
        }
        return  classes.componentsJoinedByString(" ");
    }

    public boolean omitAppenderThresholdSettingDecoration() {
        return null == classNameForAppenderThresholdName();
    }

    public WOComponent updateAppenderSettings() { return null; }
    public WOComponent updateRepositorySettings() { return null; }
    
    public WOComponent filter() { return null; }
    public WOComponent resetFilter() { _filterString = null; filterLevel = null; return null; }
    public WOComponent update() {
        ERXExtensions.configureAdaptorContext();
        return null;
    }

    public String showAllLoggersSelection() { return showAll ? "all" : "explicit"; }
    public void setShowAllLoggersSelection(String value) { showAll = "all".equals(value); }

    public WOComponent addLogger() {
        if (isNewLoggerARuleLogger) {
            _addRuleKeyLogger();
        } else {
            _addLogger();
        }
        return null;
    }

    private void _addLogger() {
        final Logger log = Logger.getLogger(loggerName());
        if (newLoggerLevel != null) {
            filterLevel = newLoggerLevel;
            log.setLevel(newLoggerLevel.level());
        } else {
            showAll = true;
            filterLevel = null;
        }
        setFilterString(loggerName());
    }
    
    // This functionality depends on ERDirectToWeb's presence..    
    private void _addRuleKeyLogger() {
    	final String prefix = "er.directtoweb.rules." + loggerName();
    	final Logger ruleFireLog = Logger.getLogger(prefix + ".fire");
    	final Logger ruleCacheHitLog = Logger.getLogger(prefix + ".cache");
    	final Logger ruleCandidatesLog = Logger.getLogger(prefix + ".candidates");
        if (newLoggerLevel != null) {
            filterLevel = newLoggerLevel;
            Level level = newLoggerLevel.level();
            ruleFireLog.setLevel(level);
            ruleCacheHitLog.setLevel(level);
            ruleCandidatesLog.setLevel(level);
        } else {
            showAll = true;
            filterLevel = null;
        }
    	setFilterString(prefix);
    }

    public String loggerPropertiesString() {
    	String result = "";
    	for (Enumeration e = allLoggers().objectEnumerator(); e.hasMoreElements();) {
    		Logger log = (Logger)e.nextElement();
    		String name = log.getName();
    		Level level = log.getLevel();
    		if (level != null && !"root".equals(name)) {
    			result += "log4j.logger." + log.getName() + "=" + log.getLevel() + "\n";
    		}
    	}
    	return result;
    }

    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        if (session().objectForKey("ERXLog4JConfiguration.enabled") != null) {
            super.appendToResponse(response, context);
        } else {
            response.appendContentString("please use the ERXDirectAction log4jAction to login first!");
        }
    }
    
    public int instanceNumber() {
        return context().request().applicationNumber();
    }
    
    public boolean knowsAppNumber() {
        return context().request().applicationNumber() != -1;
    }
    
    //* this assumes you use ERXPatternLayout
    public String conversionPattern() {
        return ERXPatternLayout.instance().getConversionPattern();
    }

    public void setConversionPattern(String newPattern) {
        ERXPatternLayout.instance().setConversionPattern(newPattern);
    }
    
    public WOComponent updateConversionPattern() { return null; }

    public String classForNavItem() {
        return aPageSection == _activeSection ? "active" : null;
    }

    public String classForLoggersDiv() {
        return PageSection.LOGGERS == _activeSection ? "active" : null;
    }

    public String classForRepositoryDiv() {
        return PageSection.REPOSITORY == _activeSection ? "active" : null;
    }

    public String classForAppendersDiv() {
        return PageSection.APPENDERS == _activeSection ? "active" : null;
    }

    public String classForOtherSettingsDiv() {
        return PageSection.OTHER == _activeSection ? "active" : null;
    }

    public String classForLoggerConfigurationControlBar() {
        return PageSection.LOGGERS == _activeSection ? "active" : null;
    }

    @Override
    public void awake() {
        super.awake();

        _appenders = null;
    }

}
