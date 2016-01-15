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

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.selenium.SeleniumTest;
import er.selenium.SeleniumTest.Element;

public class SeleniumRepeatExpanderTestFilter extends SeleniumTestFilterHelper implements SeleniumTestFilter {
	protected static class LoopData {
		enum PlacementType { Target, Value };
		
		NSArray<String> values;
		int targetOffset;
		PlacementType placement;
		
		LoopData(NSArray<String> aValues, int aTargetOffset, PlacementType aPlacement) {
			values = aValues;
			targetOffset = aTargetOffset;
			placement = aPlacement;
		}
	}
	
	protected void generateIterations(NSMutableArray<SeleniumTest.Element> elements, int repeatIndex, int doneIndex) {
		NSMutableDictionary<Integer, LoopData> loopData = new NSMutableDictionary<Integer, LoopData>();
		
		int repetitionCount = -1;
		for (int i = repeatIndex + 1; i < doneIndex; ++i) {
			SeleniumTest.Element element = elements.get(i);
			if (element instanceof SeleniumTest.MetaCommand) {
				SeleniumTest.MetaCommand metaCommand = (SeleniumTest.MetaCommand)element;
				String mcName = metaCommand.getName();
				if (mcName.equals("values") || mcName.equals("targets")) {
					if (!(elements.get(i + 1) instanceof SeleniumTest.Command)) {
						throw new RuntimeException("There must be a valid command immediately after 'values' or 'targets' metacommand");
					}

					int relTargetIndex = i + 1 - repeatIndex;
					loopData.setObjectForKey(new LoopData(metaCommand.arguments(), relTargetIndex, mcName.equals("values") ? LoopData.PlacementType.Value : LoopData.PlacementType.Target), Integer.valueOf(relTargetIndex));
					repetitionCount = metaCommand.arguments().count();
					elements.set(i, new SeleniumTest.Comment('#' + mcName));
				}
			}
		}
		
		if (loopData.count() == 0) {
			throw new RuntimeException("No 'values' or 'targets' metacommands specified between 'repeat' and 'done'");
		}

		for (LoopData ld : loopData.allValues()) {
			if (ld.values.count() != repetitionCount) {
				throw new RuntimeException("All 'values' and 'targets' metacommands inside 'repeat'-'done' repetition must have equal number of arguments");
			}
		}
		
		elements.set(repeatIndex, new SeleniumTest.Comment("#repeat"));
		elements.set(doneIndex, new SeleniumTest.Comment("#done"));
		int insertIndex = doneIndex;
		for (int j = 0; j < repetitionCount; ++j) {
			elements.insertObjectAtIndex(new SeleniumTest.Comment("#iteration"), insertIndex++);
			for (int i = repeatIndex + 1; i < doneIndex; ++i) {
				LoopData data = loopData.objectForKey(Integer.valueOf(i - repeatIndex));
				
				if (data != null) {
					SeleniumTest.Command newCommand = ((SeleniumTest.Command)elements.objectAtIndex(i)).clone();
					switch (data.placement) {
					case Target:
						elements.insertObjectAtIndex(new SeleniumTest.Comment("#target " + data.values.get(j)), insertIndex++);
						newCommand.setTarget(data.values.get(j));
						break;
					case Value:
						elements.insertObjectAtIndex(new SeleniumTest.Comment("#value " + data.values.get(j)), insertIndex++);
						newCommand.setValue(data.values.get(j));
						break;
					default:
						break;
					}
					
					elements.insertObjectAtIndex(newCommand, insertIndex++);
				} else {
					elements.insertObjectAtIndex(elements.get(i).clone(), insertIndex++);
				}
			}
		}
	}

	@Override
	protected void processTestElements(NSMutableArray<Element> elements) {
		int repeatIndex = -1;
		boolean shouldProcess;
		
		do {
			shouldProcess = false;
			for (int i = 0; i < elements.count(); ++ i) {
				SeleniumTest.Element element = elements.get(i);
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