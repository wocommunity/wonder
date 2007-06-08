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

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.selenium.SeleniumTest;

public class SeleniumRepeatExpanderTestFilter extends SeleniumTestFilterHelper implements SeleniumTestFilter {
	private static final Logger log = Logger.getLogger(SeleniumRepeatExpanderTestFilter.class);

	protected static class ValueData {
		NSArray values;
		int offset;
		
		public ValueData(NSArray aValues, int aOffset) {
			values = aValues;
			offset = aOffset;
		}
	}
	
	protected void generateIterations(NSMutableArray elements, int repeatIndex, int doneIndex) {
		NSMutableDictionary valuesData = new NSMutableDictionary();
		
		int repetitionCount = -1;
		for (int i = repeatIndex + 1; i < doneIndex; ++i) {
			SeleniumTest.Element element = (SeleniumTest.Element)elements.get(i);
			if (element instanceof SeleniumTest.MetaCommand) {
				SeleniumTest.MetaCommand metaCommand = (SeleniumTest.MetaCommand)element;
				if (metaCommand.getName().equals("values")) {
					if (!(elements.get(i + 1) instanceof SeleniumTest.Command)) {
						throw new RuntimeException("There must be a valid command after 'values' metacommand");
					}
					
					valuesData.setObjectForKey(new ValueData(metaCommand.arguments(), i - repeatIndex), i - repeatIndex);
					repetitionCount = metaCommand.arguments().count();
					elements.set(i, new SeleniumTest.Comment("#values"));
				}
			}
		}
		
		if (valuesData.count() == 0) {
			throw new RuntimeException("No 'values' metacommands specified between 'repeat' and 'done'");
		}
				
		for (int i = 1; i < valuesData.count(); ++i) {
			if (((ValueData)valuesData.get(i)).values.count() != repetitionCount) {
				throw new RuntimeException("All 'values' metacommands inside 'repeat'-'done' repetition must have equal number of arguments");
			}
		}
		
		elements.set(repeatIndex, new SeleniumTest.Comment("#repeat"));
		elements.set(doneIndex, new SeleniumTest.Comment("#done"));
		int insertIndex = doneIndex;
		for (int j = 0; j < repetitionCount; ++j) {
			elements.insertObjectAtIndex(new SeleniumTest.Comment("#iteration"), insertIndex++);
			for (int i = repeatIndex + 1; i < doneIndex; ++i) {
				ValueData data = (ValueData)valuesData.objectForKey(i - repeatIndex);
				if (data != null) {
					elements.insertObjectAtIndex(new SeleniumTest.Comment("#value " + data.values.get(j).toString()), insertIndex++);
					SeleniumTest.Command nextCommand = ((SeleniumTest.Command)elements.get(i + 1)).clone();
					nextCommand.setValue(data.values.get(j).toString());
					elements.insertObjectAtIndex(nextCommand, insertIndex++);
					++i;
				} else {
					elements.insertObjectAtIndex(((SeleniumTest.Element)elements.get(i)).clone(), insertIndex++);
				}
			}
		}
	}
	
	@Override
	public void processTestElements(NSMutableArray elements) {
		int repeatIndex = -1;
		boolean shouldProcess;
		
		do {
			shouldProcess = false;
			for (int i = 0; i < elements.count(); ++ i) {
				SeleniumTest.Element element = (SeleniumTest.Element)elements.get(i);
				if (element instanceof SeleniumTest.MetaCommand) {
					SeleniumTest.MetaCommand metaCommand = (SeleniumTest.MetaCommand)element;
					if (metaCommand.getName().equals("repeat")) {
						repeatIndex = i;
					} else if (metaCommand.getName().equals("done")) {
						generateIterations(elements, repeatIndex, i);
						shouldProcess = true;
					}
				}
			}
		} while (shouldProcess);
	}
}