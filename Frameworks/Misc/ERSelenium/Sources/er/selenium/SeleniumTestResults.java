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

import org.apache.commons.lang3.CharEncoding;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;

import er.extensions.appserver.ERXHttpStatusCodes;
import er.extensions.appserver.ERXResponse;
import er.extensions.foundation.ERXFileUtilities;
import er.extensions.foundation.ERXProperties;

/**
 * Direct action that returns the test results. This class is not used by users.
 */
public class SeleniumTestResults extends WODirectAction {
	private static final Logger log = Logger.getLogger(SeleniumAction.class);
	
	public static final String DEFAULT_REPORT_PATH = "./";
	
	public SeleniumTestResults(WORequest request) {
		super(request);
	}
	
    protected String _report;
    protected String buildReport() {
    	StringBuilder result = new StringBuilder();
    	
    	NSArray keys = context().request().formValueKeys();
    	try {
    		keys = keys.sortedArrayUsingComparator(NSComparator.AscendingStringComparator);
    	} catch (NSComparator.ComparisonException e) {
    		log.debug("can't sort results' dictionary keys");
    	}
    	Iterator iter = keys.iterator();
    	while (iter.hasNext()) {
    		String key = (String)iter.next();
    		result.append(key);
    		result.append(": ");
    		result.append(context().request().stringFormValueForKey(key));
    		result.append("\n\n");
    	}
    	return result.toString();
    }
    
    public String report() {
    	if (_report == null)
    		_report = buildReport();
    	
    	return _report;
    }
    
    protected WOActionResults processReport(String filename) {
    	if (filename != null) {
    		filename = ERXProperties.stringForKeyWithDefault("SeleniumReportPath", DEFAULT_REPORT_PATH) + "/" + filename;
    		try {
    			ERXFileUtilities.stringToFile(report(), new File(filename), CharEncoding.UTF_8);
    		} catch (Exception e) {
    			log.debug(e.getMessage());
    		}
    	}
    	
    	return new ERXResponse(report());
    }
    
    @Override
    public WOActionResults defaultAction() {
        return processReport(null);
    }

    @Override
    public WOActionResults performActionNamed(String actionName) {
        if(!ERSelenium.testsEnabled()) {
            return new ERXResponse(ERXHttpStatusCodes.FORBIDDEN);
        }
        if (actionName.equals("default"))
            return defaultAction();
        return processReport(actionName);
    }
}