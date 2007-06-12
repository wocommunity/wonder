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

package er.selenium.io;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.ERXStatelessComponent;
import er.selenium.SeleniumTest;

public class SeleniumComponentExporterPage extends ERXStatelessComponent {
	private static final Logger log = Logger.getLogger(SeleniumComponentExporterPage.class);
	
	public SeleniumTest.Element element;
	
	protected SeleniumTest _test;
	
	public void setTest(SeleniumTest test) {
		_test = test;
	}
	
	public SeleniumTest getTest() {
		return _test;
	}
	
    public SeleniumComponentExporterPage(WOContext context) {
        super(context);
    }

    public boolean isCommand(SeleniumTest.Element element) {
    	return element instanceof SeleniumTest.Command;
    }
    
    public boolean isCommand() {
        return isCommand(element);
    }
    
    public boolean isMetaCommand(SeleniumTest.Element element) {
    	return element instanceof SeleniumTest.MetaCommand;
    }
    
    public boolean isMetaCommand() {
        return isMetaCommand(element);
    }

    public boolean isComment(SeleniumTest.Element element) {
    	return element instanceof SeleniumTest.Comment;
    }
    
    public boolean isComment() {
        return isComment(element);
    }

    public String timestamp() {
    	return new NSTimestamp().toString();
    }
}