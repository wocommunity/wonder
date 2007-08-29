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

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSSelector;

public class SeleniumAction extends WODirectAction {

	private static final Logger log = Logger.getLogger(SeleniumAction.class);

	public SeleniumAction(WORequest request) {
		super(request);
	}
    
    protected WOResponse simpleResponse(String s) {
        WOResponse response = new WOResponse();
        response.appendContentString(s);
        return response;
    }
    
    protected WOResponse success() {
        return simpleResponse(ERSelenium.ACTION_COMMAND_SUCCEEDED_MESSAGE);
    }
    
    protected WOResponse fail(String s) {
        return simpleResponse(ERSelenium.ACTION_COMMAND_FAILED_MESSAGE + s);
    }
    
    protected WOResponse fail() {
        return fail("");
    }

    protected WOActionResults _perform(String anActionName) {
        try {
            return super.performActionNamed(anActionName);
        } catch(Exception ex) {
            log.error(ex, ex);
        }
        return simpleResponse(ERSelenium.ACTION_COMMAND_FAILED_MESSAGE);
    }
	
	// @Override
    public WOActionResults performActionNamed(String anActionName) {
        if(ERSelenium.testsEnabled()) {
            if(ERSelenium.isDirectAction()) {
                if(new NSSelector(anActionName + "Action").implementedByObject(this)) {
                    return _perform(anActionName);
                } else {
                    return simpleResponse(ERSelenium.INVALID_ACTION_COMMAND_MESSAGE);
                }
            } else {
                WOComponent resultPage = pageWithName(SeleniumActionResultPage.class.getSimpleName());
                assert(resultPage != null);
                resultPage.takeValueForKey(anActionName, SeleniumActionResultPage.ACTION_NAME_KEY);
                return resultPage;
            }
        }
        log.error("Selenium tests support is disabled. You can turn them on using SeleniumTestsEnabled=true in Properties files");
        return simpleResponse(ERSelenium.SELENIUM_TESTS_DISABLED_MESSAGE);
    }
}