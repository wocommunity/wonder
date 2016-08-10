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

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/**
 * Data object for a test.
 */
public class SeleniumTest implements Cloneable {	
    
	public static abstract class Element implements Cloneable {
		@Override
		public abstract Element clone();
	}
	
	public static class Comment extends Element {
		protected String value;
		
		public Comment(String value) {
			assert(value != null);
			this.value = value;
		}
		
		public void setValue(String value) {
			assert(value != null);
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
		
		@Override
		public Comment clone() {
			return new Comment(value);
		}
		
		@Override
		public String toString() {
			return getClass().getCanonicalName() + ": " + value;
		}
	}
	
	public static class MetaCommand extends Element {
		protected String name;
		protected NSMutableArray<String> arguments;
		
		public static MetaCommand metaCommandFromString(String str) {
			String[] args = str.split(" ");
			MetaCommand metaCommand = new MetaCommand(args[0]);
			for (int j = 1; j < args.length; ++j) {
				metaCommand.addArgument(args[j]);
			}
			return metaCommand;
		}
		
		public MetaCommand(String name) {
			assert(name != null);
			
			this.name = name;
			arguments = new NSMutableArray<String>();
		}
		
		public MetaCommand(String name, NSArray<String> arguments) {
			assert(name != null);
			assert(arguments != null);
			
			this.name = name;
			this.arguments = new NSMutableArray<String>(arguments);
		}
		
		public MetaCommand(String name, String[] arguments) {
			assert(name != null);
			assert(arguments != null);
			
			this.name = name;
			this.arguments = new NSMutableArray<String>(arguments);
		}
		
		public void setName(String name) {
			assert(name != null);
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		public void addArgument(String argument) {
			assert(argument != null);
			arguments.add(argument);
		}
		
		public NSArray<String> arguments() {
			return arguments;
		}
		
		public String argumentsString() {
			StringBuilder result = new StringBuilder();
			Iterator<String> iter = arguments.iterator();
			while (iter.hasNext()) {
				result.append(iter.next());
				if (iter.hasNext())
					result.append(' ');
			}
			return result.toString();
		}
		
		@Override
		public MetaCommand clone() {
			return new MetaCommand(name, arguments);
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder(getClass().getCanonicalName() + ": ");
			builder.append("@" + name + " ");
			Iterator<String> iter = arguments.iterator();
			while (iter.hasNext()) {
				builder.append(iter.next().toString() + " ");
			}
			return builder.toString();
		}
	}
	
	public static class Command extends Element {
		protected String name;
		protected String target;
		protected String value;
		
		public Command(String name, String target, String value) {
			assert(name != null);
			assert(target != null);
			assert(value != null);
			
			this.name = name;
			this.target = target;
			this.value = value;
		}
		
		public void setName(String name) {
			assert(name != null);
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		public void setTarget(String target) {
			assert(target != null);
			this.target = target;
		}
		
		public String getTarget() {
			return target;
		}
		
		public void setValue(String value) {
			assert(value != null);
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
		
		@Override
		public Command clone() {
			return new Command(name, target, value);
		}
		
		@Override
		public String toString() {
			return getClass().getCanonicalName() + ": name='" + name + "', target='" + target + "', value='" + value + "'";
		}
	}
	
	private static final Logger log = LoggerFactory.getLogger(SeleniumTest.class);
	protected NSMutableArray<SeleniumTest.Element> elements;
	protected String name;
	
	public SeleniumTest(String name) {
		this.name = name;
		elements = new NSMutableArray<Element>();
	}
	
	public SeleniumTest(String name, NSArray<Element> elements) {
		this.name = name;
		this.elements = new NSMutableArray<Element>(elements);
	}
	
	public SeleniumTest(String name, SeleniumTest.Element[] elements) {
		this.name = name;
		this.elements = new NSMutableArray<Element>(elements);
	}
	
	public NSArray<Element> elements() {
		return elements;
	}
	
	public void assignElements(NSArray<Element> elements) {
		assert(elements != null);
		this.elements = new NSMutableArray<Element>(elements);
	}
	
	public String name() {
		return name;
	}
	
	public void setName(String name) {
		assert(name != null);
		this.name = name;
	}
	
	@Override
	public Object clone() {
		return new SeleniumTest(name, elements);
	}
	
	public void dump() {
		log.debug("Test name: {}", name);
		Iterator iter = elements.iterator();
		while (iter.hasNext()) {
			log.debug("{}", iter.next());
		}
	}
}
