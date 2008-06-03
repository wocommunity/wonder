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

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/**
 * Data object for a test.
 */
public class SeleniumTest implements Cloneable {	
    
	public static abstract class Element implements Cloneable {
		public abstract Object clone();
	}
	
	public static class Comment extends Element {
		protected String _value;
		
		public Comment(String value) {
			assert(value != null);
			_value = value;
		}
		
		public void setValue(String value) {
			assert(value != null);
			_value = value;
		}
		
		public String getValue() {
			return _value;
		}
		
		public Object clone() {
			return new Comment(_value);
		}
		
		public String toString() {
			return getClass().getCanonicalName() + ": " + _value;
		}
	}
	
	public static class MetaCommand extends Element {
		protected String _name;
		protected NSMutableArray _arguments;
		
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
			
			_name = name;
			_arguments = new NSMutableArray();
		}
		
		public MetaCommand(String name, NSArray arguments) {
			assert(name != null);
			assert(arguments != null);
			
			_name = name;
			_arguments = new NSMutableArray(_arguments);
		}
		
		public MetaCommand(String name, Object[] arguments) {
			assert(name != null);
			assert(arguments != null);
			
			_name = name;
			_arguments = new NSMutableArray(arguments);
		}
		
		public void setName(String name) {
			assert(name != null);
			_name = name;
		}
		
		public String getName() {
			return _name;
		}
		
		public void addArgument(String argument) {
			assert(argument != null);
			_arguments.add(argument);
		}
		
		public NSArray arguments() {
			return _arguments;
		}
		
		public String argumentsString() {
			StringBuilder result = new StringBuilder();
			Iterator iter = _arguments.iterator();
			while (iter.hasNext()) {
				result.append(iter.next());
				if (iter.hasNext())
					result.append(' ');
			}
			return result.toString();
		}
		
		public Object clone() {
			return new MetaCommand(_name, _arguments);
		}
		
		public String toString() {
			StringBuilder builder = new StringBuilder(getClass().getCanonicalName() + ": ");
			builder.append("@" + _name + " ");
			Iterator iter = _arguments.iterator();
			while (iter.hasNext()) {
				builder.append(iter.next().toString() + " ");
			}
			return builder.toString();
		}
	}
	
	public static class Command extends Element {
		protected String _name;
		protected String _target;
		protected String _value;
		
		public Command(String name, String target, String value) {
			assert(name != null);
			assert(target != null);
			assert(value != null);
			
			_name = name;
			_target = target;
			_value = value;
		}
		
		public void setName(String name) {
			assert(name != null);
			_name = name;
		}
		
		public String getName() {
			return _name;
		}
		
		public void setTarget(String target) {
			assert(target != null);
			_target = target;
		}
		
		public String getTarget() {
			return _target;
		}
		
		public void setValue(String value) {
			assert(value != null);
			_value = value;
		}
		
		public String getValue() {
			return _value;
		}
		
		public Object clone() {
			return new Command(_name, _target, _value);
		}
		
		public String toString() {
			return getClass().getCanonicalName() + ": name='" + _name + "', target='" + _target + "', value='" + _value + "'";
		}
	}
	
	private static final Logger log = Logger.getLogger(SeleniumTest.class);
	protected NSMutableArray _elements;
	protected String _name;
	
	public SeleniumTest(String name) {
		_name = name;
		_elements = new NSMutableArray();
	}
	
	public SeleniumTest(String name, NSArray elements) {
		_name = name;
		_elements = new NSMutableArray(elements);
	}
	
	public SeleniumTest(String name, Object[] elements) {
		_name = name;
		_elements = new NSMutableArray(elements);
	}
	
	public NSArray elements() {
		return _elements;
	}
	
	public void assignElements(NSArray elements) {
		assert(_elements != null);
		_elements = new NSMutableArray(elements);
	}
	
	public String name() {
		return _name;
	}
	
	public void setName(String name) {
		assert(name != null);
		_name = name;
	}
	
	public Object clone() {
		return new SeleniumTest(_name, _elements);
	}
	
	public void dump() {
		log.debug("Test name: " + _name);
		Iterator iter = _elements.iterator();
		while (iter.hasNext()) {
			log.debug(iter.next().toString());
		}
	}
}
