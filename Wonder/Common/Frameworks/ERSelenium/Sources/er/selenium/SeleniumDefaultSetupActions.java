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

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOCookie;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSTimestamp;

/**
 * Default "start tests" class.
 */
public class SeleniumDefaultSetupActions {
	
	public static void resetSession(WOResponse response, WOContext context) {
		// TODO: cookieKey should be refactored to the form of a method in DMSession
		WOCookie dummyCookie = new WOCookie(NSBundle.mainBundle().name()+"L", "dummy");
		dummyCookie.setPath("/");
		dummyCookie.setDomain(null);  // Let the browser set the domain
		dummyCookie.setExpires(new NSTimestamp().timestampByAddingGregorianUnits(0, -2, 0, 0, 0, 0));
		response.addCookie(dummyCookie);
		
		context.session().terminate();
	}
}