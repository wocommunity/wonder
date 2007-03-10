/**
 * 
 */
package er.indexing;

import java.text.Format;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.NumberTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSNumberFormatter;
import com.webobjects.foundation.NSTimestampFormatter;

import er.extensions.ERXKeyValueCodingUtilities;

class ERIndexAttribute {
	String _name;
	TermVector _termVector;
	Store _store;
	Index _index;
	Analyzer _analyzer;
	Format _format;
	ERIndex _model;
	
	ERIndexAttribute(ERIndex index, String name, NSDictionary dict) {
		_name = name;
		_termVector = (TermVector) classValue(dict, "termVector", TermVector.class, "YES");
		_store = (Store) classValue(dict, "store", Store.class, "NO");
		_index = (Index) classValue(dict, "index", Index.class, "TOKENIZED");
		String analyzerClass = (String) dict.objectForKey("analyzer");
		if(analyzerClass == null) {
			analyzerClass = StandardAnalyzer.class.getName();
		}
		_analyzer = (Analyzer) create(analyzerClass);
		if(_analyzer == null && name.matches("\\w+_(\\w+)")) {
			String locale = name.substring(name.lastIndexOf('_') + 1);
		}
		_format = (Format) create((String) dict.objectForKey("format"));
		String numberFormat = (String) dict.objectForKey("numberformat");
		if(numberFormat != null) {
			_format = new NSNumberFormatter(numberFormat);
		}
		String dateformat = (String) dict.objectForKey("dateformat");
		if(dateformat != null) {
			_format = new NSTimestampFormatter(dateformat);
		}
	}
	
	private Object create(String className) {
		if(className != null) {
			try {
				return Class.forName(className).newInstance();
			} catch (InstantiationException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (IllegalAccessException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (ClassNotFoundException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
		}
		return null;
	}

	private Object classValue(NSDictionary dict, String key, Class c, String defaultValue) {
		Object result;
		String code = (String) dict.objectForKey(key);
		if(code == null) {
			code = defaultValue;
		}
		result = ERXKeyValueCodingUtilities.classValueForKey(c, code);
		return result;
	}
	
	public TermVector termVector() {
		return _termVector;
	}

	public Index index() {
		return _index;
	}

	public Store store() {
		return _store;
	}

	public String name() {
		return _name;
	}

	public Analyzer analyzer() {
		return _analyzer;
	}
	
	public String formatValue(Object value) {
		if(_format != null) {
			return _format.format(value);
		}
		if(value instanceof Number) {
			return NumberTools.longToString(((Number)value).longValue());
		}
		if(value instanceof Date) {
			return DateTools.dateToString((Date)value, Resolution.MILLISECOND);
		}
		if(value instanceof NSArray) {
			return ((NSArray)value).componentsJoinedByString(" ");
		}
		return (value != null ? value.toString() : null);
	}
}