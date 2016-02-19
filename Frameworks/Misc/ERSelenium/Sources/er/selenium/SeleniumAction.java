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

import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOCookie;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.appserver.ERXDirectAction;
import er.extensions.appserver.ERXResponse;

/**
 * Default handler class, gets replaced by the startup process.
 *
 */
public class SeleniumAction extends ERXDirectAction {
	private static final Logger log = LoggerFactory.getLogger(SeleniumAction.class);

	public SeleniumAction(WORequest request) {
		super(request);
	}

    protected String[] cookieKeys() {
        return new String[]{};
    }
    
    protected void resetSession(WOResponse response) {
        if(context().hasSession() || true) {
            session().terminate();
        }
        String[] keys = cookieKeys();
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            WOCookie dummyCookie = new WOCookie(key, "dummy");
            dummyCookie.setPath("/");
            dummyCookie.setDomain(null);  // Let the browser set the domain
            dummyCookie.setExpires(new NSTimestamp().timestampByAddingGregorianUnits(0, -2, 0, 0, 0, 0));
            response.addCookie(dummyCookie);
        }
    }

    
    protected WOResponse dictionaryResponse(NSDictionary<?,?> dict) {
        ERXResponse response = new ERXResponse();
        response.appendContentString("<html><body>");
        for (Enumeration<?> e = dict.keyEnumerator(); e.hasMoreElements();) {
            Object key = e.nextElement();
            Object value = dict.objectForKey(key);
            response.appendContentString("<span id='" + key + "'>" + value + "</span>\n");
        }
        response.appendContentString("</body></html>");
        return response;
    }
    
    protected WOResponse stringResponse(String s) {
        return new ERXResponse(s);
    }
    
    protected WOResponse success() {
        return stringResponse(ERSelenium.ACTION_COMMAND_SUCCEEDED_MESSAGE);
    }
    
    protected WOResponse fail() {
    	return stringResponse(ERSelenium.ACTION_COMMAND_FAILED_MESSAGE);
    }

    protected WOResponse fail(String s) {
        return stringResponse(ERSelenium.ACTION_COMMAND_FAILED_MESSAGE + " " + s);
    }

    @Override
    public WOActionResults performActionNamed(String anActionName) {
    	log.debug("Selenium Action: {}", anActionName);
        WOActionResults result = null;
        if(ERSelenium.testsEnabled()) {
        	result = super.performActionNamed(anActionName);
        } else {
            log.error("Selenium tests support is disabled. You can turn them on using SeleniumTestsEnabled=true in Properties files");
            result = stringResponse(ERSelenium.SELENIUM_TESTS_DISABLED_MESSAGE);
        }
        WOResponse response = result.generateResponse();
        if(!session().isTerminating()) {
            result = response;
            session()._appendCookieToResponse(response);
        }
        log.debug("Out Session: {}{}", session().sessionID(), response.cookies());
        return result;
    }
}