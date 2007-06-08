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

package er.selenium.filters;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.selenium.SeleniumTest;

public class SeleniumCompositeTestFilter extends SeleniumTestFilterHelper implements SeleniumTestFilter {
	private static final Logger log = Logger.getLogger(SeleniumCompositeTestFilter.class);
	protected NSMutableArray _testFilters;
	
	public SeleniumCompositeTestFilter() {
		_testFilters = new NSMutableArray();
	}
	
	public void addTestFilter(SeleniumTestFilter filter) {
		assert(filter != null);
		_testFilters.add(filter);
	}
	
	public NSArray getTestFilters() {
		return _testFilters;
	}
	
	public SeleniumTest processTest(SeleniumTest test) {
		Iterator iter = _testFilters.iterator();
		log.debug("processing " + _testFilters.count() + " filters in chain");
		while (iter.hasNext()) {
			SeleniumTestFilter filter = (SeleniumTestFilter)iter.next();
			log.debug("applying " + filter.toString());
			test = filter.processTest(test);
		}
		
		return test;
	}
}