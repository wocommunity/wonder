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

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

/**
 * Starts testing of a suite of tests (a directory)
 */
public class SeleniumTestSuite extends WODirectAction {
	
	public SeleniumTestSuite(WORequest request) {
		super(request);
	}
	
	// @Override
	public WOActionResults defaultAction() {
	    return pageWithName(SeleniumTestSuitePage.class.getName());
	}
		
	// @Override
	public WOActionResults performActionNamed(String anActionName) {
	    if(!ERSelenium.testsEnabled()) {
	        return new WOResponse();
	    }
	    if (anActionName.equals("default")) {
	        return defaultAction();
	    }
	    String testDirectory = null;
	    String test = null;

	    int splitterPos = anActionName.indexOf(ERSelenium.SUITE_SEPERATOR);
	    if (splitterPos == -1) {
	        testDirectory = anActionName;
	    } else {
	        testDirectory = anActionName.substring(0, splitterPos);
	        test = anActionName.substring(splitterPos + 1, anActionName.length());
	    }

	    SeleniumTestSuitePage page = (SeleniumTestSuitePage)pageWithName(SeleniumTestSuitePage.class.getName());
	    page.setTestDirectory(testDirectory);
	    page.setTestName(test);
	    return page;
	}
}