/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.util.Enumeration;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

/**
 * Configures and manages the log4j logging system. Will also configure the
 * system for rapid turn around, ie when WOCaching is disabled when the conf
 * file changes it will get reloaded.<br />
 * 
 */

public class ERXLog4JConfiguration extends WOComponent {

	public ERXLog4JConfiguration(WOContext aContext) {
		super(aContext);
	}

	private Logger _logger;
	private String _filterString;
	private String _ruleKey;
	private String _loggerName;

	public boolean showAll = false;

	public Logger logger() {
		return _logger;
	}

	public void setLogger(Logger newValue) {
		_logger = newValue;
	}

	public String filterString() {
		return _filterString;
	}

	public void setFilterString(String newValue) {
		_filterString = newValue;
	}

	public String loggerName() {
		return _loggerName;
	}

	public void setLoggerName(String newValue) {
		_loggerName = newValue;
	}

	public String ruleKey() {
		return _ruleKey;
	}

	public void setRuleKey(String newValue) {
		_ruleKey = newValue;
	}

	public final static EOSortOrdering NAME_SORT_ORDERING = new EOSortOrdering("name", EOSortOrdering.CompareAscending);
	public final static NSMutableArray SORT_BY_NAME = new NSMutableArray(NAME_SORT_ORDERING);

	public Logger parentForLogger(Logger l) {
		Logger result = (Logger) l.getParent();
		/*
		 * String name=l.getName(); int i=name.lastIndexOf('.'); if (i!=-1) {
		 * String parentName=name.substring(0,i);
		 * result=Logger.getLogger(parentName); }
		 */
		return result;
	}

	public NSArray loggers() {
		NSMutableArray result = new NSMutableArray();
		for (Enumeration e = LogManager.getCurrentLoggers(); e.hasMoreElements();) {
			Logger log = (Logger) e.nextElement();
			while (log != null) {
				addLogger(log, result);
				log = parentForLogger(log);
			}
		}
		EOSortOrdering.sortArrayUsingKeyOrderArray(result, SORT_BY_NAME);
		return result;
	}

	public void addLogger(Logger log, NSMutableArray result) {
		if ((filterString() == null || filterString().length() == 0 || log.getName().toLowerCase().indexOf(filterString().toLowerCase()) != -1) && (showAll || log.getLevel() != null) && !result.containsObject(log)) {
			result.addObject(log);
		}
	}

	public WOComponent filter() {
		return null;
	}

	public WOComponent resetFilter() {
		_filterString = null;
		return null;
	}

	public WOComponent update() {
		ERXExtensions.configureAdaptorContext();
		return null;
	}

	public WOComponent showAll() {
		showAll = true;
		return null;
	}

	public WOComponent showExplicitlySet() {
		showAll = false;
		return null;
	}

	public WOComponent addLogger() {
		Logger.getLogger(loggerName());
		setFilterString(loggerName());
		return null;
	}

	// This functionality depends on ERDirectToWeb's presence..
	public WOComponent addRuleKey() {
		String prefix = "er.directtoweb.rules." + ruleKey();
		Logger.getLogger(prefix + ".fire");
		Logger.getLogger(prefix + ".cache");
		Logger.getLogger(prefix + ".candidates");
		showAll = true;
		setFilterString(prefix);
		return null;
	}

	public Integer offLevel() {
		return ERXConstant.integerForInt(Level.OFF.toInt());
	}

	public Integer debugLevel() {
		return ERXConstant.integerForInt(Level.DEBUG.toInt());
	}

	public Integer infoLevel() {
		return ERXConstant.integerForInt(Level.INFO.toInt());
	}

	public Integer warnLevel() {
		return ERXConstant.integerForInt(Level.WARN.toInt());
	}

	public Integer errorLevel() {
		return ERXConstant.integerForInt(Level.ERROR.toInt());
	}

	public Integer fatalLevel() {
		return ERXConstant.integerForInt(Level.FATAL.toInt());
	}

	public Integer unsetLevel() {
		return ERXConstant.MinusOneInteger;
	}

	public Integer loggerLevelValue() {
		return logger() != null && logger().getLevel() != null ? ERXConstant.integerForInt(logger().getLevel().toInt()) : ERXConstant.MinusOneInteger;
	}

	public boolean loggerIsNotOff() {
		return logger() != null && logger().getLevel() != Level.OFF;
	}

	public boolean loggerIsNotDebug() {
		return logger() != null && logger().getLevel() != Level.DEBUG;
	}

	public boolean loggerIsNotInfo() {
		return logger() != null && logger().getLevel() != Level.INFO;
	}

	public boolean loggerIsNotWarn() {
		return logger() != null && logger().getLevel() != Level.WARN;
	}

	public boolean loggerIsNotError() {
		return logger() != null && logger().getLevel() != Level.ERROR;
	}

	public boolean loggerIsNotFatal() {
		return logger() != null && logger().getLevel() != Level.FATAL;
	}

	public String loggerPropertiesString() {
		String result = "";
		for (Enumeration e = loggers().objectEnumerator(); e.hasMoreElements();) {
			Logger log = (Logger) e.nextElement();
			String name = log.getName();
			Level level = log.getLevel();
			if (level != null && !"root".equals(name)) {
				result += "log4j.category." + log.getName() + "=" + log.getLevel() + "\n";
			}
		}
		return result;
	}

	public void setLoggerLevelValue(Integer newValue) {
		int lvl = newValue != null ? newValue.intValue() : -1;
		logger().setLevel(lvl != -1 ? Level.toLevel(lvl) : null);
	}

	private final static NSDictionary BG_COLORS = new NSDictionary(
			new Object[] { "#ffbbbb", "#eeccbb", "#ddddbb", "#cceebb", "#bbffbb"}, 
			new Object[] { 
					ERXConstant.integerForInt(Level.DEBUG.toInt()), 
					ERXConstant.integerForInt(Level.WARN.toInt()), 
					ERXConstant.integerForInt(Level.ERROR.toInt()), 
					ERXConstant.integerForInt(Level.FATAL.toInt()), 
					ERXConstant.integerForInt(Level.OFF.toInt())
			}
	);

	public String bgColor() {
		return (String) BG_COLORS.objectForKey(loggerLevelValue());
	}

	public int indentLevel() {
		return ERXStringUtilities.numberOfOccurrencesOfCharInString('.', logger().getName());
	}

	public void appendToResponse(WOResponse r, WOContext c) {
		if (session().objectForKey("ERXLog4JConfiguration.enabled") != null) {
			super.appendToResponse(r, c);
		}
		else {
			r.appendContentString("please use the ERXDirectAction log4jAction to login first!");
		}
	}

	// * this assumes you use ERXPatternLayout
	public String conversionPattern() {
		return ERXPatternLayout.instance().getConversionPattern();
	}

	public void setConversionPattern(String newPattern) {
		ERXPatternLayout.instance().setConversionPattern(newPattern);
	}

	public WOComponent updateConversionPattern() {
		return null;
	}

}
